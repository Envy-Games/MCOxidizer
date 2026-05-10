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
    public static final int SLOT_INPUT_COPPER = 0;
    public static final int SLOT_INPUT_WAX = 1;
    public static final int SLOT_INPUT_CHIP = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int DATA_COUNT = 4;

    private final ItemStackHandler inv = new ItemStackHandler(SLOT_COUNT) {
        @Override protected void onContentsChanged(int slot){ setChanged(); }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack){
            return switch (slot) {
                case SLOT_INPUT_COPPER -> OxidizerIngredients.isCopperInput(stack);
                case SLOT_INPUT_WAX -> OxidizerIngredients.isWaxPrecursor(stack);
                case SLOT_INPUT_CHIP -> stack.getItem() instanceof StageChipItem;
                default -> false;
            };
        }
    };
    private final EnergyStorage energy = new NotifyingEnergyStorage(ModConfigs.BUFFER_FE.get(), this::sync);

    private static final class NotifyingEnergyStorage extends EnergyStorage {
        private final Runnable onChange;
        private NotifyingEnergyStorage(int capacity, Runnable onChange) {
            super(capacity);
            this.onChange = onChange;
        }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) {
            int r = super.receiveEnergy(maxReceive, simulate);
            if (r > 0 && !simulate) onChange.run();
            return r;
        }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            int r = super.extractEnergy(maxExtract, simulate);
            if (r > 0 && !simulate) onChange.run();
            return r;
        }
    }
    // Call this to mark dirty AND push a block update packet to clients.
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState st = getBlockState();
            level.sendBlockUpdated(worldPosition, st, st, 3);
        }
    }
    // --- networking hooks so the client BE receives state ---
    @Override
    public net.minecraft.network.protocol.game.@NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.@NotNull Provider lookup) {
        // send full BE state used by your GUI (includes FE via saveAdditional)
        return this.saveWithoutMetadata(lookup);
    }

    @Override
    public void handleUpdateTag(@NotNull net.minecraft.nbt.CompoundTag tag, HolderLookup.@NotNull Provider lookup) {
        // apply the update on the client
        this.loadAdditional(tag, lookup);
    }

    private int progress = 0;
    private int maxProgress = ModConfigs.PROCESS_TICKS.get();

    private final ContainerData data = new SimpleContainerData(DATA_COUNT) {
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
        @Override public int getCount(){ return DATA_COUNT; }
    };

    private final IItemHandler itemIOAnySideHandler = new IItemHandler() {
        @Override public int getSlots() { return SLOT_COUNT; }

        @Override public @NotNull ItemStack getStackInSlot(int slot) {
            return inv.getStackInSlot(slot);
        }

        @Override public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot == SLOT_INPUT_COPPER || slot == SLOT_INPUT_WAX) {
                if (!inv.isItemValid(slot, stack)) return stack;
                return inv.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == SLOT_OUTPUT) {
                return inv.extractItem(SLOT_OUTPUT, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override public int getSlotLimit(int slot) { return inv.getSlotLimit(slot); }

        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return (slot == SLOT_INPUT_COPPER || slot == SLOT_INPUT_WAX) && inv.isItemValid(slot, stack);
        }
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, OxidizerBlockEntity be) {
        OxidizerServerTicker.tick(level, pos, state, be);
    }

    public IEnergyStorage getEnergy(){ return energy; }
    public ItemStackHandler getInv(){ return inv; }
    public ContainerData getData(){ return data; }
    public IItemHandler getItemHandlerForSide(@Nullable Direction side) {
        return itemIOAnySideHandler;
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
