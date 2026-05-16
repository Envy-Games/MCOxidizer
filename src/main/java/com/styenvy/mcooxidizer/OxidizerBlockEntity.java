package com.styenvy.mcooxidizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
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

import java.util.Arrays;

public final class OxidizerBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    public static final int LANE_COUNT = 3;
    public static final int SLOTS_PER_LANE = 4;

    public static final int SLOT_INPUT_COPPER = 0;
    public static final int SLOT_INPUT_WAX = 1;
    public static final int SLOT_INPUT_CHIP = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = LANE_COUNT * SLOTS_PER_LANE;

    public static final int DATA_MAX_PROGRESS = LANE_COUNT;
    public static final int DATA_ENERGY = LANE_COUNT + 1;
    public static final int DATA_ENERGY_MAX = LANE_COUNT + 2;
    public static final int DATA_ACTIVE_LANES = LANE_COUNT + 3;
    public static final int DATA_COUNT = LANE_COUNT + 4;

    private final int[] progress = new int[LANE_COUNT];
    private int maxProgress = ModConfigs.PROCESS_TICKS.get();
    private int activeLanes = 0;

    private final ItemStackHandler inv = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isValidForSlot(slot, stack);
        }
    };
    private final EnergyStorage energy = new NotifyingEnergyStorage(ModConfigs.BUFFER_FE.get(), this::sync);

    private static final class NotifyingEnergyStorage extends EnergyStorage {
        private final Runnable onChange;

        private NotifyingEnergyStorage(int capacity, Runnable onChange) {
            super(capacity);
            this.onChange = onChange;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (received > 0 && !simulate) {
                onChange.run();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (extracted > 0 && !simulate) {
                onChange.run();
            }
            return extracted;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, Tag tag) {
            if (tag instanceof IntTag intTag) {
                this.energy = Math.max(0, Math.min(this.capacity, intTag.getAsInt()));
            }
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public net.minecraft.network.protocol.game.@NotNull ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider lookup) {
        return this.saveWithoutMetadata(lookup);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookup) {
        this.loadAdditional(tag, lookup);
    }

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            if (index >= 0 && index < LANE_COUNT) {
                return progress[index];
            }
            return switch (index) {
                case DATA_MAX_PROGRESS -> maxProgress;
                case DATA_ENERGY -> energy.getEnergyStored();
                case DATA_ENERGY_MAX -> energy.getMaxEnergyStored();
                case DATA_ACTIVE_LANES -> activeLanes;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < LANE_COUNT) {
                progress[index] = value;
            } else if (index == DATA_MAX_PROGRESS) {
                maxProgress = value;
            } else if (index == DATA_ACTIVE_LANES) {
                activeLanes = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private final IItemHandler itemIOAnySideHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inv.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (isInputSlot(slot) && inv.isItemValid(slot, stack)) {
                return inv.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (isOutputSlot(slot)) {
                return inv.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inv.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isInputSlot(slot) && inv.isItemValid(slot, stack);
        }
    };

    private final IEnergyStorage externalEnergy = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return energy.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energy.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public OxidizerBlockEntity(BlockPos pos, BlockState state) {
        super(ModContent.OXIDIZER_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OxidizerBlockEntity be) {
        OxidizerServerTicker.tick(level, pos, state, be);
    }

    public static int slotFor(int lane, int slotOffset) {
        return lane * SLOTS_PER_LANE + slotOffset;
    }

    public static int copperSlot(int lane) {
        return slotFor(lane, SLOT_INPUT_COPPER);
    }

    public static int waxSlot(int lane) {
        return slotFor(lane, SLOT_INPUT_WAX);
    }

    public static int chipSlot(int lane) {
        return slotFor(lane, SLOT_INPUT_CHIP);
    }

    public static int outputSlot(int lane) {
        return slotFor(lane, SLOT_OUTPUT);
    }

    public static boolean isValidForSlot(int slot, @NotNull ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return false;
        }
        return switch (slot % SLOTS_PER_LANE) {
            case SLOT_INPUT_COPPER -> OxidizerIngredients.isCopperInput(stack);
            case SLOT_INPUT_WAX -> OxidizerIngredients.isWaxPrecursor(stack);
            case SLOT_INPUT_CHIP -> stack.getItem() instanceof StageChipItem;
            default -> false;
        };
    }

    public static boolean isInputSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return false;
        }
        int slotOffset = slot % SLOTS_PER_LANE;
        return slotOffset != SLOT_OUTPUT;
    }

    public static boolean isOutputSlot(int slot) {
        return slot >= 0 && slot < SLOT_COUNT && slot % SLOTS_PER_LANE == SLOT_OUTPUT;
    }

    public IEnergyStorage getEnergy() {
        return energy;
    }

    public ItemStackHandler getInv() {
        return inv;
    }

    public ContainerData getData() {
        return data;
    }

    public IItemHandler getItemHandlerForSide(@Nullable Direction side) {
        return itemIOAnySideHandler;
    }

    public IEnergyStorage getExternalEnergy() {
        return externalEnergy;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookup) {
        super.loadAdditional(tag, lookup);
        Arrays.fill(progress, 0);
        int[] savedProgress = tag.getIntArray("Progresses");
        if (savedProgress.length > 0) {
            System.arraycopy(savedProgress, 0, progress, 0, Math.min(savedProgress.length, progress.length));
        } else {
            progress[0] = tag.getInt("Progress");
        }
        maxProgress = Math.max(1, tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : ModConfigs.PROCESS_TICKS.get());
        activeLanes = 0;
        if (tag.contains("FE", Tag.TAG_INT)) {
            energy.deserializeNBT(lookup, tag.get("FE"));
        }
        if (tag.contains("Items")) {
            loadInventory(tag.getCompound("Items"), lookup);
        }
    }

    private void loadInventory(CompoundTag itemsTag, HolderLookup.Provider lookup) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            inv.setStackInSlot(slot, ItemStack.EMPTY);
        }

        ListTag tagList = itemsTag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot >= 0 && slot < SLOT_COUNT) {
                ItemStack.parse(lookup, itemTags).ifPresent(stack -> inv.setStackInSlot(slot, stack));
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.putIntArray("Progresses", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.put("FE", energy.serializeNBT(lookup));
        tag.put("Items", inv.serializeNBT(lookup));
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new OxidizerMenu(id, inv, this, data);
    }

    @Override
    public net.minecraft.network.chat.@NotNull Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.mcooxidizer.oxidizer");
    }
}
