package com.styenvy.mcooxidizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class OxidizerBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    private final ItemStackHandler inv = new ItemStackHandler(4) {
        @Override protected void onContentsChanged(int slot){ setChanged(); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack){
            return switch (slot) {
                case 0 -> isCopperBlock(stack);
                case 1 -> isWaxPrecursor(stack);
                case 2 -> stack.getItem() instanceof StageChipItem;
                case 3 -> false;
                default -> false;
            };
        }
    };
    private final EnergyStorage energy = new EnergyStorage(ModConfigs.BUFFER_FE.get());
    private int progress = 0;
    private int maxProgress = ModConfigs.PROCESS_TICKS.get();

    private final ContainerData data = new SimpleContainerData(4) {
        @Override public int get(int i){
            return switch (i){
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                default -> 0;
            };
        }
        @Override public void set(int i, int v){
            if (i==0) progress=v;
            else if (i==1) maxProgress=v;
        }
        @Override public int getCount(){ return 4; }
    };

    // expose only copper (slot 0) and wax (slot 1) for insertion; no extraction from inputs
    private final IItemHandler itemInputHandler = new IItemHandler() {
        @Override public int getSlots(){ return 2; }
        @Override public @NotNull ItemStack getStackInSlot(int slot){ return inv.getStackInSlot(slot); } // 0->inv0, 1->inv1
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate){
            if (slot < 0 || slot > 1) return stack;
            if (!inv.isItemValid(slot, stack)) return stack;
            return inv.insertItem(slot, stack, simulate);
        }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate){ return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot){ return inv.getSlotLimit(slot); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack){
            return (slot == 0 || slot == 1) && inv.isItemValid(slot, stack);
        }
    };

    // expose only output (slot 3) for extraction; deny insertion
    private final IItemHandler itemOutputHandler = new IItemHandler() {
        @Override public int getSlots(){ return 1; }
        @Override public @NotNull ItemStack getStackInSlot(int slot){ return inv.getStackInSlot(3); }
        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate){ return stack; }
        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate){ return inv.extractItem(3, amount, simulate); }
        @Override public int getSlotLimit(int slot){ return inv.getSlotLimit(3); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack){ return false; }
    };

    private final IEnergyStorage externalEnergy = new IEnergyStorage() {
        @Override public int receiveEnergy(int maxReceive, boolean simulate){ return energy.receiveEnergy(maxReceive, simulate); }
        @Override public int extractEnergy(int maxExtract, boolean simulate){ return 0; }
        @Override public int getEnergyStored(){ return energy.getEnergyStored(); }
        @Override public int getMaxEnergyStored(){ return energy.getMaxEnergyStored(); }
        @Override public boolean canExtract(){ return false; }
        @Override public boolean canReceive(){ return true; }
    };

    public OxidizerBlockEntity(BlockPos pos, BlockState state){ super(ModContent.OXIDIZER_BE.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OxidizerBlockEntity be){
        ItemStack in = be.inv.getStackInSlot(0);
        ItemStack chip = be.inv.getStackInSlot(2);
        if (in.isEmpty() || !(chip.getItem() instanceof StageChipItem)) { be.progress = 0; be.setChanged(); return; }

        ItemStack result = be.computeResult(in, ((StageChipItem)chip.getItem()).getStage(), be.inv.getStackInSlot(1));
        if (result.isEmpty()) { be.progress = 0; be.setChanged(); return; }
        if (!be.canOutput(result)) { be.progress = 0; be.setChanged(); return; }

        //Energy Tick Bypass Starts Here -> Comment These Lines Out with //
        //final int ept = ModConfigs.ENERGY_PER_TICK.get();
        //if (ept > 0) {
        //    if (be.energy.getEnergyStored() < ept) {
        //        be.setChanged();
        //        return;
        //    }
        //    be.energy.extractEnergy(ept, false);
        //}
        //be.progress++;
        //Energy Tick Bypass Stops Here -> Comment These Lines Out with //
        //Comment or Uncomment below to set power requirement to normal. Comment out for testing without FE power source.

        int ept = ModConfigs.ENERGY_PER_TICK.get();
        if (be.energy.getEnergyStored() < ept) return;

        be.energy.extractEnergy(ept, false);
        be.progress++;
        //Make changes in ModConfigs as well -> //Set to ENERGY_PER_TICK 0 0 and then inside of runs>client>config> mcooxidizer-common.toml set energyPerTick = 0
        if (be.progress >= be.maxProgress) {
            boolean inputWasWaxed = isWaxedCopperBlock(in);
            boolean needConsumeWax = !inputWasWaxed && isWaxPrecursor(be.inv.getStackInSlot(1));
            in.shrink(1);
            if (needConsumeWax) be.inv.extractItem(1, 1, false);
            be.insertOutput(result);
            be.progress = 0;
        }
        be.setChanged();
    }

    private static boolean isCopperBlock(ItemStack s){
        return
                // full blocks
                s.is(Blocks.COPPER_BLOCK.asItem()) || s.is(Blocks.EXPOSED_COPPER.asItem()) ||
                        s.is(Blocks.WEATHERED_COPPER.asItem()) || s.is(Blocks.OXIDIZED_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_COPPER_BLOCK.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER.asItem()) ||

                        // cut blocks
                        s.is(Blocks.CUT_COPPER.asItem()) || s.is(Blocks.EXPOSED_CUT_COPPER.asItem()) ||
                        s.is(Blocks.WEATHERED_CUT_COPPER.asItem()) || s.is(Blocks.OXIDIZED_CUT_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_CUT_COPPER.asItem()) || s.is(Blocks.WAXED_EXPOSED_CUT_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CUT_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem()) ||

                        // slabs
                        s.is(Blocks.CUT_COPPER_SLAB.asItem()) || s.is(Blocks.EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                        s.is(Blocks.WEATHERED_CUT_COPPER_SLAB.asItem()) || s.is(Blocks.OXIDIZED_CUT_COPPER_SLAB.asItem()) ||
                        s.is(Blocks.WAXED_CUT_COPPER_SLAB.asItem()) || s.is(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem()) ||

                        // stairs
                        s.is(Blocks.CUT_COPPER_STAIRS.asItem()) || s.is(Blocks.EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                        s.is(Blocks.WEATHERED_CUT_COPPER_STAIRS.asItem()) || s.is(Blocks.OXIDIZED_CUT_COPPER_STAIRS.asItem()) ||
                        s.is(Blocks.WAXED_CUT_COPPER_STAIRS.asItem()) || s.is(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem()) ||

                        // chiseled
                        s.is(Blocks.CHISELED_COPPER.asItem()) || s.is(Blocks.EXPOSED_CHISELED_COPPER.asItem()) ||
                        s.is(Blocks.WEATHERED_CHISELED_COPPER.asItem()) || s.is(Blocks.OXIDIZED_CHISELED_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_CHISELED_COPPER.asItem()) || s.is(Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem()) ||

                        // grates
                        s.is(Blocks.COPPER_GRATE.asItem()) || s.is(Blocks.EXPOSED_COPPER_GRATE.asItem()) ||
                        s.is(Blocks.WEATHERED_COPPER_GRATE.asItem()) || s.is(Blocks.OXIDIZED_COPPER_GRATE.asItem()) ||
                        s.is(Blocks.WAXED_COPPER_GRATE.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem()) ||

                        // doors
                        s.is(Blocks.COPPER_DOOR.asItem()) || s.is(Blocks.EXPOSED_COPPER_DOOR.asItem()) ||
                        s.is(Blocks.WEATHERED_COPPER_DOOR.asItem()) || s.is(Blocks.OXIDIZED_COPPER_DOOR.asItem()) ||
                        s.is(Blocks.WAXED_COPPER_DOOR.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem()) ||

                        // trapdoors
                        s.is(Blocks.COPPER_TRAPDOOR.asItem()) || s.is(Blocks.EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                        s.is(Blocks.WEATHERED_COPPER_TRAPDOOR.asItem()) || s.is(Blocks.OXIDIZED_COPPER_TRAPDOOR.asItem()) ||
                        s.is(Blocks.WAXED_COPPER_TRAPDOOR.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem()) ||

                        // bulbs (lamps)
                        s.is(Blocks.COPPER_BULB.asItem()) || s.is(Blocks.EXPOSED_COPPER_BULB.asItem()) ||
                        s.is(Blocks.WEATHERED_COPPER_BULB.asItem()) || s.is(Blocks.OXIDIZED_COPPER_BULB.asItem()) ||
                        s.is(Blocks.WAXED_COPPER_BULB.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_BULB.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_BULB.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem());
    }
    private static boolean isWaxedCopperBlock(ItemStack s){
        return
                // blocks
                s.is(Blocks.WAXED_COPPER_BLOCK.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER.asItem()) ||

                        // cut
                        s.is(Blocks.WAXED_CUT_COPPER.asItem()) || s.is(Blocks.WAXED_EXPOSED_CUT_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CUT_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem()) ||

                        // slabs
                        s.is(Blocks.WAXED_CUT_COPPER_SLAB.asItem()) || s.is(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem()) ||

                        // stairs
                        s.is(Blocks.WAXED_CUT_COPPER_STAIRS.asItem()) || s.is(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem()) ||

                        // chiseled
                        s.is(Blocks.WAXED_CHISELED_COPPER.asItem()) || s.is(Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem()) ||

                        // grates
                        s.is(Blocks.WAXED_COPPER_GRATE.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem()) ||

                        // doors
                        s.is(Blocks.WAXED_COPPER_DOOR.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem()) ||

                        // trapdoors
                        s.is(Blocks.WAXED_COPPER_TRAPDOOR.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem()) ||

                        // bulbs (lamps)
                        s.is(Blocks.WAXED_COPPER_BULB.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER_BULB.asItem()) ||
                        s.is(Blocks.WAXED_WEATHERED_COPPER_BULB.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem());
    }

    private static boolean isWaxPrecursor(ItemStack s){
        if (s.isEmpty()) return false;
        if (ModConfigs.ALLOW_HONEYCOMB.get() && s.is(net.minecraft.world.item.Items.HONEYCOMB)) return true;
        return ModConfigs.ALLOW_OIL_TAG.get() && s.is(MCOxTags.WAX_PRECURSORS);
    }

    private ItemStack computeResult(ItemStack input, StageChipItem.Stage target, ItemStack precursor){
        final boolean inputWaxed = isWaxedCopperBlock(input);
        final boolean wantWaxed  = inputWaxed || isWaxPrecursor(precursor);

        // full blocks
        if (input.is(Blocks.COPPER_BLOCK.asItem()) || input.is(Blocks.EXPOSED_COPPER.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER.asItem()) || input.is(Blocks.OXIDIZED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_COPPER_BLOCK.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER.asItem()   : Blocks.EXPOSED_COPPER.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER.asItem() : Blocks.WEATHERED_COPPER.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER.asItem()  : Blocks.OXIDIZED_COPPER.asItem());
            };
        }

        // cut blocks
        if (input.is(Blocks.CUT_COPPER.asItem()) || input.is(Blocks.EXPOSED_CUT_COPPER.asItem()) ||
                input.is(Blocks.WEATHERED_CUT_COPPER.asItem()) || input.is(Blocks.OXIDIZED_CUT_COPPER.asItem()) ||
                input.is(Blocks.WAXED_CUT_COPPER.asItem()) || input.is(Blocks.WAXED_EXPOSED_CUT_COPPER.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CUT_COPPER.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CUT_COPPER.asItem()   : Blocks.EXPOSED_CUT_COPPER.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CUT_COPPER.asItem() : Blocks.WEATHERED_CUT_COPPER.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem()  : Blocks.OXIDIZED_CUT_COPPER.asItem());
            };
        }

        // slabs
        if (input.is(Blocks.CUT_COPPER_SLAB.asItem()) || input.is(Blocks.EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                input.is(Blocks.WEATHERED_CUT_COPPER_SLAB.asItem()) || input.is(Blocks.OXIDIZED_CUT_COPPER_SLAB.asItem()) ||
                input.is(Blocks.WAXED_CUT_COPPER_SLAB.asItem()) || input.is(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem()   : Blocks.EXPOSED_CUT_COPPER_SLAB.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem() : Blocks.WEATHERED_CUT_COPPER_SLAB.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem()  : Blocks.OXIDIZED_CUT_COPPER_SLAB.asItem());
            };
        }

        // stairs
        if (input.is(Blocks.CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                input.is(Blocks.WEATHERED_CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.OXIDIZED_CUT_COPPER_STAIRS.asItem()) ||
                input.is(Blocks.WAXED_CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem()   : Blocks.EXPOSED_CUT_COPPER_STAIRS.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem() : Blocks.WEATHERED_CUT_COPPER_STAIRS.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem()  : Blocks.OXIDIZED_CUT_COPPER_STAIRS.asItem());
            };
        }

        // chiseled
        if (input.is(Blocks.CHISELED_COPPER.asItem()) || input.is(Blocks.EXPOSED_CHISELED_COPPER.asItem()) ||
                input.is(Blocks.WEATHERED_CHISELED_COPPER.asItem()) || input.is(Blocks.OXIDIZED_CHISELED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_CHISELED_COPPER.asItem()) || input.is(Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem()   : Blocks.EXPOSED_CHISELED_COPPER.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem() : Blocks.WEATHERED_CHISELED_COPPER.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem()  : Blocks.OXIDIZED_CHISELED_COPPER.asItem());
            };
        }

        // grates
        if (input.is(Blocks.COPPER_GRATE.asItem()) || input.is(Blocks.EXPOSED_COPPER_GRATE.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_GRATE.asItem()) || input.is(Blocks.OXIDIZED_COPPER_GRATE.asItem()) ||
                input.is(Blocks.WAXED_COPPER_GRATE.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem()   : Blocks.EXPOSED_COPPER_GRATE.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem() : Blocks.WEATHERED_COPPER_GRATE.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem()  : Blocks.OXIDIZED_COPPER_GRATE.asItem());
            };
        }

        // doors
        if (input.is(Blocks.COPPER_DOOR.asItem()) || input.is(Blocks.EXPOSED_COPPER_DOOR.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_DOOR.asItem()) || input.is(Blocks.OXIDIZED_COPPER_DOOR.asItem()) ||
                input.is(Blocks.WAXED_COPPER_DOOR.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem()   : Blocks.EXPOSED_COPPER_DOOR.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem() : Blocks.WEATHERED_COPPER_DOOR.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem()  : Blocks.OXIDIZED_COPPER_DOOR.asItem());
            };
        }

        // trapdoors
        if (input.is(Blocks.COPPER_TRAPDOOR.asItem()) || input.is(Blocks.EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_TRAPDOOR.asItem()) || input.is(Blocks.OXIDIZED_COPPER_TRAPDOOR.asItem()) ||
                input.is(Blocks.WAXED_COPPER_TRAPDOOR.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem()   : Blocks.EXPOSED_COPPER_TRAPDOOR.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem() : Blocks.WEATHERED_COPPER_TRAPDOOR.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem()  : Blocks.OXIDIZED_COPPER_TRAPDOOR.asItem());
            };
        }

        // bulbs (lamps)
        if (input.is(Blocks.COPPER_BULB.asItem()) || input.is(Blocks.EXPOSED_COPPER_BULB.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_BULB.asItem()) || input.is(Blocks.OXIDIZED_COPPER_BULB.asItem()) ||
                input.is(Blocks.WAXED_COPPER_BULB.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_BULB.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_BULB.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_BULB.asItem()   : Blocks.EXPOSED_COPPER_BULB.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_BULB.asItem() : Blocks.WEATHERED_COPPER_BULB.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem()  : Blocks.OXIDIZED_COPPER_BULB.asItem());
            };
        }

        return ItemStack.EMPTY;
    }

    private boolean canOutput(ItemStack stack){
        ItemStack cur = inv.getStackInSlot(3);
        if (cur.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(cur, stack)) return false;
        return cur.getCount() + stack.getCount() <= cur.getMaxStackSize();
    }
    private void insertOutput(ItemStack stack){
        ItemStack cur = inv.getStackInSlot(3);
        if (cur.isEmpty()) inv.setStackInSlot(3, stack.copy());
        else { cur.grow(stack.getCount()); inv.setStackInSlot(3, cur); }
    }

    public IEnergyStorage getEnergy(){ return energy; }
    public ItemStackHandler getInv(){ return inv; }
    public ContainerData getData(){ return data; }

    public IItemHandler getItemHandlerForSide(@Nullable Direction side){
        return (side == Direction.DOWN) ? itemOutputHandler : itemInputHandler;
    }
    public IEnergyStorage getExternalEnergy(){ return externalEnergy; }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookup) {
        super.loadAdditional(tag, lookup);
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        if (tag.contains("FE")) energy.deserializeNBT(lookup, Objects.requireNonNull(tag.get("FE")));
        if (tag.contains("Items")) inv.deserializeNBT(lookup, tag.getCompound("Items"));
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.put("FE", energy.serializeNBT(lookup));
        tag.put("Items", inv.serializeNBT(lookup));
    }

    @Override public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player){ return new OxidizerMenu(id, inv, this, data); }
    @Override public net.minecraft.network.chat.@NotNull Component getDisplayName(){ return net.minecraft.network.chat.Component.translatable("block.mcooxidizer.oxidizer"); }
}
