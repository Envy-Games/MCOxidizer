package com.styenvy.mcooxidizer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public final class OxidizerMenu extends AbstractContainerMenu {
    // Indices
    public static final int IDX_INPUT_COPPER = 0; // (25,46)
    public static final int IDX_INPUT_CHIP   = 1; // (46,46)
    public static final int IDX_INPUT_WAX    = 2; // (68,46)
    public static final int IDX_OUTPUT       = 3; // (135,46)

    private static final int MACHINE_SLOTS = 4;

    private static final int PLAYER_INV_ROWS = 3, PLAYER_INV_COLS = 9;
    private static final int PLAYER_INV_SLOTS = PLAYER_INV_ROWS * PLAYER_INV_COLS; // 27
    private static final int PLAYER_HOTBAR_SLOTS = 9;

    private final ContainerData data;

    public static OxidizerMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var be = (OxidizerBlockEntity) inv.player.level().getBlockEntity(pos);
        assert be != null;
        return new OxidizerMenu(id, inv, be, be.getData());
    }

    public OxidizerMenu(int id, Inventory playerInv, OxidizerBlockEntity be, ContainerData data) {
        super(ModContent.OXIDIZER_MENU.get(), id);
        this.data = data;
        addDataSlots(data);

        // Machine slots — EXACT coords you provided
        this.addSlot(new SlotItemHandler(be.getInv(), IDX_INPUT_COPPER, 25, 46));
        this.addSlot(new SlotItemHandler(be.getInv(), IDX_INPUT_CHIP,   46, 46));
        this.addSlot(new SlotItemHandler(be.getInv(), IDX_INPUT_WAX,    68, 46));
        this.addSlot(new SlotItemHandler(be.getInv(), IDX_OUTPUT,      135, 46) {
            @Override public boolean mayPlace(@NotNull ItemStack s) { return false; }
        });

        // Player inventory (3x9)
        for (int r = 0; r < PLAYER_INV_ROWS; r++)
            for (int c = 0; c < PLAYER_INV_COLS; c++)
                this.addSlot(new Slot(playerInv, 9 + r * 9 + c, 8 + c * 18, 84 + r * 18));
        // Hotbar
        for (int h = 0; h < PLAYER_HOTBAR_SLOTS; h++)
            this.addSlot(new Slot(playerInv, h, 8 + h * 18, 142));
    }

    @Override public boolean stillValid(@NotNull Player p) { return true; }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            ret = stack.copy();

            int playerEndEx = MACHINE_SLOTS + PLAYER_INV_SLOTS + PLAYER_HOTBAR_SLOTS; // 4..40

            if (index < MACHINE_SLOTS) {
                if (!this.moveItemStackTo(stack, MACHINE_SLOTS, playerEndEx, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, ret);
            } else {
                // Player -> inputs (chip → wax → copper)
                if (!this.moveItemStackTo(stack, IDX_INPUT_CHIP, IDX_INPUT_CHIP + 1, false))
                    if (!this.moveItemStackTo(stack, IDX_INPUT_WAX, IDX_INPUT_WAX + 1, false))
                        if (!this.moveItemStackTo(stack, IDX_INPUT_COPPER, IDX_INPUT_COPPER + 1, false))
                            return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return ret;
    }

    // data: 0=progress, 1=maxProgress, 2=energy, 3=energyMax
    public int progress()    { return data.get(0); }
    public int maxProgress() { return Math.max(1, data.get(1)); }
    public int energy()      { return data.get(2); }
    public int energyMax()   { return Math.max(1, data.get(3)); }
}
