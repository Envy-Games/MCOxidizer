package com.styenvy.mcooxidizer;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public final class OxidizerMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 214;
    public static final int IMAGE_HEIGHT = 206;

    public static final int LANE_COUNT = OxidizerBlockEntity.LANE_COUNT;
    public static final int SLOTS_PER_LANE = OxidizerBlockEntity.SLOTS_PER_LANE;
    public static final int MACHINE_SLOTS = OxidizerBlockEntity.SLOT_COUNT;

    public static final int CONTAINER_OFFSET_COPPER = 0;
    public static final int CONTAINER_OFFSET_CHIP = 1;
    public static final int CONTAINER_OFFSET_WAX = 2;
    public static final int CONTAINER_OFFSET_OUTPUT = 3;

    public static final int COPPER_X = 24;
    public static final int CHIP_X = 52;
    public static final int WAX_X = 80;
    public static final int PROGRESS_X = 110;
    public static final int PROGRESS_Y_OFFSET = 5;
    public static final int PROGRESS_W = 38;
    public static final int PROGRESS_H = 8;
    public static final int OUTPUT_X = 162;
    public static final int LANE_Y = 50;
    public static final int LANE_SPACING = 24;

    public static final int PLAYER_INV_X = 27;
    public static final int PLAYER_INV_Y = 127;
    public static final int PLAYER_HOTBAR_Y = 183;

    public static final int ENERGY_X = 192;
    public static final int ENERGY_Y = 43;
    public static final int ENERGY_W = 8;
    public static final int ENERGY_H = 62;

    public static final int PLAYER_INV_ROWS = 3;
    public static final int PLAYER_INV_COLS = 9;
    public static final int PLAYER_INV_SLOTS = PLAYER_INV_ROWS * PLAYER_INV_COLS;
    public static final int PLAYER_HOTBAR_SLOTS = 9;
    public static final int PLAYER_SLOTS = PLAYER_INV_SLOTS + PLAYER_HOTBAR_SLOTS;
    public static final int PLAYER_START = MACHINE_SLOTS;

    private final ContainerData data;
    private final ContainerLevelAccess access;

    public OxidizerMenu(int id, Inventory playerInv) {
        this(
                id,
                playerInv,
                createClientInventory(),
                new SimpleContainerData(OxidizerBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public OxidizerMenu(int id, Inventory playerInv, OxidizerBlockEntity be, ContainerData data) {
        this(
                id,
                playerInv,
                be.getInv(),
                data,
                be.getLevel() == null
                        ? ContainerLevelAccess.NULL
                        : ContainerLevelAccess.create(be.getLevel(), be.getBlockPos())
        );
    }

    private OxidizerMenu(int id, Inventory playerInv, ItemStackHandler machineInv, ContainerData data, ContainerLevelAccess access) {
        super(ModContent.OXIDIZER_MENU.get(), id);
        this.data = data;
        this.access = access;
        checkContainerDataCount(data, OxidizerBlockEntity.DATA_COUNT);
        addDataSlots(data);

        for (int lane = 0; lane < LANE_COUNT; lane++) {
            int y = laneY(lane);
            this.addSlot(new SlotItemHandler(machineInv, OxidizerBlockEntity.copperSlot(lane), COPPER_X, y));
            this.addSlot(new SlotItemHandler(machineInv, OxidizerBlockEntity.chipSlot(lane), CHIP_X, y));
            this.addSlot(new SlotItemHandler(machineInv, OxidizerBlockEntity.waxSlot(lane), WAX_X, y));
            this.addSlot(new SlotItemHandler(machineInv, OxidizerBlockEntity.outputSlot(lane), OUTPUT_X, y) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return false;
                }
            });
        }

        for (int row = 0; row < PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < PLAYER_INV_COLS; col++) {
                this.addSlot(new Slot(playerInv, 9 + row * 9 + col, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int hotbar = 0; hotbar < PLAYER_HOTBAR_SLOTS; hotbar++) {
            this.addSlot(new Slot(playerInv, hotbar, PLAYER_INV_X + hotbar * 18, PLAYER_HOTBAR_Y));
        }
    }

    private static ItemStackHandler createClientInventory() {
        return new ItemStackHandler(OxidizerBlockEntity.SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return OxidizerBlockEntity.isValidForSlot(slot, stack);
            }
        };
    }

    public static int laneY(int lane) {
        return LANE_Y + lane * LANE_SPACING;
    }

    public static int copperIndex(int lane) {
        return lane * SLOTS_PER_LANE + CONTAINER_OFFSET_COPPER;
    }

    public static int chipIndex(int lane) {
        return lane * SLOTS_PER_LANE + CONTAINER_OFFSET_CHIP;
    }

    public static int waxIndex(int lane) {
        return lane * SLOTS_PER_LANE + CONTAINER_OFFSET_WAX;
    }

    public static int outputIndex(int lane) {
        return lane * SLOTS_PER_LANE + CONTAINER_OFFSET_OUTPUT;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(access, player, ModContent.OXIDIZER_BLOCK.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        if (index < 0 || index >= this.slots.size()) {
            return ret;
        }

        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ret;
        }

        ItemStack stack = slot.getItem();
        ret = stack.copy();

        int playerInvEndEx = PLAYER_START + PLAYER_INV_SLOTS;
        int playerEndEx = PLAYER_START + PLAYER_SLOTS;
        if (index < MACHINE_SLOTS) {
            if (!this.moveItemStackTo(stack, PLAYER_START, playerEndEx, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, ret);
        } else if (!moveToMachineInput(stack)) {
            if (index < playerInvEndEx) {
                if (!this.moveItemStackTo(stack, playerInvEndEx, playerEndEx, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < playerEndEx) {
                if (!this.moveItemStackTo(stack, PLAYER_START, playerInvEndEx, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == ret.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return ret;
    }

    private boolean moveToMachineInput(ItemStack stack) {
        if (stack.getItem() instanceof StageChipItem) {
            return moveToLaneSlots(stack, CONTAINER_OFFSET_CHIP);
        }
        if (OxidizerIngredients.isWaxPrecursor(stack)) {
            return moveToLaneSlots(stack, CONTAINER_OFFSET_WAX);
        }
        if (OxidizerIngredients.isCopperInput(stack)) {
            return moveToLaneSlots(stack, CONTAINER_OFFSET_COPPER);
        }
        return false;
    }

    private boolean moveToLaneSlots(ItemStack stack, int offset) {
        boolean moved = false;
        for (int lane = 0; lane < LANE_COUNT; lane++) {
            int slot = lane * SLOTS_PER_LANE + offset;
            if (this.moveItemStackTo(stack, slot, slot + 1, false)) {
                moved = true;
                if (stack.isEmpty()) {
                    break;
                }
            }
        }
        return moved;
    }

    public int progress(int lane) {
        return Math.max(0, Math.min(data.get(lane), maxProgress()));
    }

    public int maxProgress() {
        return Math.max(1, data.get(OxidizerBlockEntity.DATA_MAX_PROGRESS));
    }

    public int energy() {
        return Math.max(0, Math.min(data.get(OxidizerBlockEntity.DATA_ENERGY), energyMax()));
    }

    public int energyMax() {
        return Math.max(1, data.get(OxidizerBlockEntity.DATA_ENERGY_MAX));
    }

    public int activeLanes() {
        return data.get(OxidizerBlockEntity.DATA_ACTIVE_LANES);
    }
}
