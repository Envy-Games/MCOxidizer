package com.styenvy.mcooxidizer;

import net.minecraft.core.BlockPos;
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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class OxidizerBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    // Slots: 0=input (copper), 1=wax precursor, 2=chip, 3=output
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

    public OxidizerBlockEntity(BlockPos pos, BlockState state){ super(ModContent.OXIDIZER_BE.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OxidizerBlockEntity be){
        ItemStack in = be.inv.getStackInSlot(0);
        ItemStack chip = be.inv.getStackInSlot(2);
        if (in.isEmpty() || !(chip.getItem() instanceof StageChipItem)) { be.progress = 0; be.setChanged(); return; }

        ItemStack result = be.computeResult(in, ((StageChipItem)chip.getItem()).getStage(), be.inv.getStackInSlot(1));
        if (result.isEmpty()) { be.progress = 0; be.setChanged(); return; }
        if (!be.canOutput(result)) { be.progress = 0; be.setChanged(); return; }

        final int ept = ModConfigs.ENERGY_PER_TICK.get();

        // Only require/extract FE when ept > 0. When ept == 0, process without power.
        if (ept > 0) {
            if (be.energy.getEnergyStored() < ept) {
                be.setChanged();
                return;
            }
            be.energy.extractEnergy(ept, false);
        }

        be.progress++;
        //Uncomment this to return power requirement to normal
        //int ept = ModConfigs.ENERGY_PER_TICK.get();
        //if (be.energy.getEnergyStored() < ept) return;

        //be.energy.extractEnergy(ept, false);
        //be.progress++;
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
        return s.is(Blocks.COPPER_BLOCK.asItem()) || s.is(Blocks.EXPOSED_COPPER.asItem()) || s.is(Blocks.WEATHERED_COPPER.asItem()) || s.is(Blocks.OXIDIZED_COPPER.asItem())
                || s.is(Blocks.WAXED_COPPER_BLOCK.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER.asItem()) || s.is(Blocks.WAXED_WEATHERED_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER.asItem());
    }
    private static boolean isWaxedCopperBlock(ItemStack s){
        return s.is(Blocks.WAXED_COPPER_BLOCK.asItem()) || s.is(Blocks.WAXED_EXPOSED_COPPER.asItem()) || s.is(Blocks.WAXED_WEATHERED_COPPER.asItem()) || s.is(Blocks.WAXED_OXIDIZED_COPPER.asItem());
    }

    private static boolean isWaxPrecursor(ItemStack s){
        if (s.isEmpty()) return false;
        if (ModConfigs.ALLOW_HONEYCOMB.get() && s.is(net.minecraft.world.item.Items.HONEYCOMB)) return true;
        return ModConfigs.ALLOW_OIL_TAG.get() && s.is(MCOxTags.WAX_PRECURSORS);
    }

    private ItemStack computeResult(ItemStack input, StageChipItem.Stage target, ItemStack precursor){
        boolean inputWaxed = isWaxedCopperBlock(input);
        boolean wantWaxed = inputWaxed || isWaxPrecursor(precursor);
        return switch (target) {
            case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER.asItem()   : Blocks.EXPOSED_COPPER.asItem());
            case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER.asItem() : Blocks.WEATHERED_COPPER.asItem());
            case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER.asItem()  : Blocks.OXIDIZED_COPPER.asItem());
        };
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
