package com.styenvy.mcooxidizer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class OxidizerScreen extends AbstractContainerScreen<OxidizerMenu> {
    private static final int PANEL = 0xFF22272A;
    private static final int PANEL_DARK = 0xFF171B1D;
    private static final int PANEL_MID = 0xFF30373A;
    private static final int SLOT_BG = 0xFF555C5B;
    private static final int SLOT_INNER = 0xFF8B9290;
    private static final int TEXT = 0xFFEDE6D3;
    private static final int TEXT_DIM = 0xFFAEB6B1;
    private static final int TEAL = 0xFF42B6A8;
    private static final int COPPER = 0xFFD9823B;
    private static final int BAR_EMPTY = 0xFF111414;
    private static final int SCREEN_RAISE = 18;
    private static final int INVENTORY_PANEL_TOP = 122;
    private static final int INVENTORY_PANEL_BOTTOM = OxidizerMenu.IMAGE_HEIGHT - 6;
    private static final int ENERGY_LABEL_Y = 32;

    public static final int JEI_CLICK_X = OxidizerMenu.PROGRESS_X - 2;
    public static final int JEI_CLICK_Y = OxidizerMenu.LANE_Y + OxidizerMenu.PROGRESS_Y_OFFSET - 2;
    public static final int JEI_CLICK_W = OxidizerMenu.PROGRESS_W + 4;
    public static final int JEI_CLICK_H = OxidizerMenu.LANE_SPACING * (OxidizerMenu.LANE_COUNT - 1)
            + OxidizerMenu.PROGRESS_H + 4;

    public OxidizerScreen(OxidizerMenu menu, Inventory inv, Component ignoredTitle) {
        super(menu, inv, Component.translatable("gui.mcooxidizer.oxidizer.title"));
        this.imageWidth = OxidizerMenu.IMAGE_WIDTH;
        this.imageHeight = OxidizerMenu.IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.topPos = Math.max(6, this.topPos - SCREEN_RAISE);
        this.titleLabelX = 0;
        this.titleLabelY = 0;
        this.inventoryLabelX = 0;
        this.inventoryLabelY = 0;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partial, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        drawPanel(g);
        drawHeader(g);
        drawMachineSection(g);
        drawInventorySection(g);
        drawEnergyBar(g);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        drawHeaderLabels(guiGraphics);
        drawColumnLabels(guiGraphics);
        drawLaneLabels(guiGraphics);
        drawEnergyLabel(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        renderHoverTooltips(g, mouseX, mouseY);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected @NotNull List<Component> getTooltipFromContainerItem(@NotNull ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if (hoveredSlot != null) {
            List<Component> slotInfo = slotTooltip(hoveredSlot);
            if (!slotInfo.isEmpty()) {
                tooltip.add(Component.empty());
                tooltip.addAll(slotInfo);
            }
        }
        return tooltip;
    }

    private void drawPanel(GuiGraphics g) {
        int x = leftPos;
        int y = topPos;
        g.fill(x + 4, y + 4, x + imageWidth + 4, y + imageHeight + 4, 0xAA000000);
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xFF0D0F10);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, PANEL);
        g.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFF262D2F);
    }

    private void drawHeader(GuiGraphics g) {
        int x = leftPos;
        int y = topPos;
        g.fill(x + 8, y + 8, x + imageWidth - 8, y + 27, PANEL_DARK);
        g.fill(x + 8, y + 26, x + imageWidth - 8, y + 27, COPPER);
    }

    private void drawHeaderLabels(GuiGraphics g) {
        g.drawString(font, Component.translatable("gui.mcooxidizer.oxidizer.title"), 14, 14, TEXT, false);
        Component status = Component.translatable(
                "gui.mcooxidizer.oxidizer.active_lanes",
                menu.activeLanes(),
                OxidizerMenu.LANE_COUNT
        );
        g.drawString(font, status, 103, 14, TEXT_DIM, false);
    }

    private void drawMachineSection(GuiGraphics g) {
        int x = leftPos;
        int y = topPos;
        g.fill(x + 10, y + 32, x + 184, y + 118, PANEL_DARK);
        g.fill(x + 11, y + 33, x + 183, y + 117, PANEL_MID);

        for (int lane = 0; lane < OxidizerMenu.LANE_COUNT; lane++) {
            drawLane(g, lane);
        }
    }

    private void drawColumnLabels(GuiGraphics g) {
        int y = 36;
        drawCenteredLabel(g, Component.translatable("gui.mcooxidizer.oxidizer.copper"), OxidizerMenu.COPPER_X + 8, y);
        drawCenteredLabel(g, Component.translatable("gui.mcooxidizer.oxidizer.chip"), OxidizerMenu.CHIP_X + 8, y);
        drawCenteredLabel(g, Component.translatable("gui.mcooxidizer.oxidizer.wax"), OxidizerMenu.WAX_X + 8, y);
        drawCenteredLabel(g, Component.translatable("gui.mcooxidizer.oxidizer.result"), OxidizerMenu.OUTPUT_X + 8, y);
    }

    private void drawCenteredLabel(GuiGraphics g, Component label, int centerX, int y) {
        g.drawString(font, label, centerX - font.width(label) / 2, y, TEXT_DIM, false);
    }

    private void drawLane(GuiGraphics g, int lane) {
        int y = topPos + OxidizerMenu.laneY(lane);
        int rowTop = y - 3;
        int color = lane % 2 == 0 ? 0xFF252D2F : 0xFF202729;

        g.fill(leftPos + 16, rowTop, leftPos + 180, rowTop + 23, color);
        g.fill(leftPos + 17, rowTop, leftPos + 19, rowTop + 23, laneAccent(lane));

        drawSlot(g, OxidizerMenu.COPPER_X, OxidizerMenu.laneY(lane), false);
        drawSlot(g, OxidizerMenu.CHIP_X, OxidizerMenu.laneY(lane), false);
        drawSlot(g, OxidizerMenu.WAX_X, OxidizerMenu.laneY(lane), false);
        drawSlot(g, OxidizerMenu.OUTPUT_X, OxidizerMenu.laneY(lane), true);

        Slot chipSlot = menu.slots.get(OxidizerMenu.chipIndex(lane));
        if (!chipSlot.hasItem()) {
            drawChipOutline(g, leftPos + OxidizerMenu.CHIP_X, y);
        }

        float progress = menu.progress(lane) / (float) menu.maxProgress();
        drawProgressArrow(
                g,
                leftPos + OxidizerMenu.PROGRESS_X,
                y + OxidizerMenu.PROGRESS_Y_OFFSET,
                OxidizerMenu.PROGRESS_W,
                OxidizerMenu.PROGRESS_H,
                progress,
                laneAccent(lane)
        );
    }

    private void drawLaneLabels(GuiGraphics g) {
        for (int lane = 0; lane < OxidizerMenu.LANE_COUNT; lane++) {
            g.drawString(font, Component.literal(String.valueOf(lane + 1)), 11, OxidizerMenu.laneY(lane) + 5, TEXT_DIM, false);
        }
    }

    private void drawInventorySection(GuiGraphics g) {
        int x = leftPos;
        int y = topPos;
        g.fill(x + 10, y + INVENTORY_PANEL_TOP, x + imageWidth - 10, y + INVENTORY_PANEL_BOTTOM, PANEL_DARK);
        g.fill(x + 11, y + INVENTORY_PANEL_TOP + 1, x + imageWidth - 11, y + INVENTORY_PANEL_BOTTOM - 1, 0xFF2B3032);

        for (int row = 0; row < OxidizerMenu.PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < OxidizerMenu.PLAYER_INV_COLS; col++) {
                drawSlot(g, OxidizerMenu.PLAYER_INV_X + col * 18, OxidizerMenu.PLAYER_INV_Y + row * 18, false);
            }
        }
        for (int hotbar = 0; hotbar < OxidizerMenu.PLAYER_HOTBAR_SLOTS; hotbar++) {
            drawSlot(g, OxidizerMenu.PLAYER_INV_X + hotbar * 18, OxidizerMenu.PLAYER_HOTBAR_Y, false);
        }
    }

    private void drawEnergyBar(GuiGraphics g) {
        int x = leftPos + OxidizerMenu.ENERGY_X;
        int y = topPos + OxidizerMenu.ENERGY_Y;
        g.fill(x - 2, y - 2, x + OxidizerMenu.ENERGY_W + 2, y + OxidizerMenu.ENERGY_H + 2, 0xFF0B0D0E);
        g.fill(x, y, x + OxidizerMenu.ENERGY_W, y + OxidizerMenu.ENERGY_H, BAR_EMPTY);

        int fill = Math.min(OxidizerMenu.ENERGY_H, (int) (OxidizerMenu.ENERGY_H * (menu.energy() / (float) menu.energyMax())));
        int fillY = y + OxidizerMenu.ENERGY_H - fill;
        g.fill(x, fillY, x + OxidizerMenu.ENERGY_W, y + OxidizerMenu.ENERGY_H, 0xFF117CC8);
        g.fill(x + 1, fillY, x + OxidizerMenu.ENERGY_W - 1, y + OxidizerMenu.ENERGY_H, 0xFF23B3F1);
    }

    private void drawEnergyLabel(GuiGraphics g) {
        drawCenteredLabel(
                g,
                Component.translatable("gui.mcooxidizer.oxidizer.energy"),
                OxidizerMenu.ENERGY_X + OxidizerMenu.ENERGY_W / 2,
                ENERGY_LABEL_Y
        );
    }

    private void drawSlot(GuiGraphics g, int x, int y, boolean output) {
        int sx = leftPos + x;
        int sy = topPos + y;
        g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF0F1112);
        g.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
        g.fill(sx + 1, sy + 1, sx + 15, sy + 15, output ? 0xFF737A75 : SLOT_INNER);
        g.fill(sx, sy, sx + 16, sy + 1, 0xFFCBD0CB);
        g.fill(sx, sy, sx + 1, sy + 16, 0xFFCBD0CB);
        g.fill(sx + 15, sy + 1, sx + 16, sy + 16, 0xFF3A3F3E);
        g.fill(sx + 1, sy + 15, sx + 16, sy + 16, 0xFF3A3F3E);
    }

    private static void drawChipOutline(GuiGraphics g, int x, int y) {
        int color = 0x7732383A;
        g.fill(x + 4, y + 4, x + 13, y + 5, color);
        g.fill(x + 4, y + 12, x + 13, y + 13, color);
        g.fill(x + 4, y + 5, x + 5, y + 12, color);
        g.fill(x + 12, y + 5, x + 13, y + 12, color);
        g.fill(x + 6, y + 7, x + 8, y + 9, color);
        g.fill(x + 9, y + 7, x + 11, y + 9, color);
        g.fill(x + 7, y + 11, x + 10, y + 12, color);
        g.fill(x + 3, y + 6, x + 4, y + 7, color);
        g.fill(x + 3, y + 9, x + 4, y + 10, color);
        g.fill(x + 13, y + 6, x + 14, y + 7, color);
        g.fill(x + 13, y + 9, x + 14, y + 10, color);
    }

    private static void drawProgressArrow(GuiGraphics g, int x, int y, int w, int h, float progress, int accent) {
        progress = Math.max(0f, Math.min(1f, progress));
        g.fill(x, y, x + w, y + h, 0xFF0F1212);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF1B2020);

        int filled = Math.round((w - 2) * progress);
        if (filled > 0) {
            g.fill(x + 1, y + 1, x + 1 + filled, y + h - 1, accent);
            g.fill(x + 1, y + 1, x + 1 + filled, y + 2, TEAL);
        }

        int mid = y + h / 2;
        g.fill(x + w - 6, mid - 4, x + w - 5, mid + 4, 0xFF0F1212);
        g.fill(x + w - 5, mid - 3, x + w - 4, mid + 3, 0xFF0F1212);
        g.fill(x + w - 4, mid - 2, x + w - 3, mid + 2, 0xFF0F1212);
    }

    private void renderHoverTooltips(GuiGraphics g, int mouseX, int mouseY) {
        if (!menu.getCarried().isEmpty()) {
            return;
        }

        if (isHoveringAbsolute(leftPos + OxidizerMenu.ENERGY_X - 2, topPos + OxidizerMenu.ENERGY_Y - 2,
                OxidizerMenu.ENERGY_W + 4, OxidizerMenu.ENERGY_H + 4, mouseX, mouseY)) {
            g.renderComponentTooltip(font, List.of(
                    Component.translatable("gui.mcooxidizer.oxidizer.energy"),
                    Component.literal(menu.energy() + " / " + menu.energyMax() + " FE").withStyle(ChatFormatting.AQUA),
                    Component.translatable("gui.mcooxidizer.oxidizer.energy_hint").withStyle(ChatFormatting.GRAY)
            ), mouseX, mouseY);
            return;
        }

        for (int lane = 0; lane < OxidizerMenu.LANE_COUNT; lane++) {
            int x = leftPos + OxidizerMenu.PROGRESS_X;
            int y = topPos + OxidizerMenu.laneY(lane) + OxidizerMenu.PROGRESS_Y_OFFSET;
            if (isHoveringAbsolute(x, y, OxidizerMenu.PROGRESS_W, OxidizerMenu.PROGRESS_H, mouseX, mouseY)) {
                int pct = Math.min(100, Math.round(menu.progress(lane) * 100f / menu.maxProgress()));
                g.renderComponentTooltip(font, List.of(
                        Component.translatable("gui.mcooxidizer.oxidizer.progress", lane + 1),
                        Component.literal(pct + "%").withStyle(ChatFormatting.AQUA),
                        Component.translatable("gui.mcooxidizer.oxidizer.progress_hint").withStyle(ChatFormatting.GRAY)
                ), mouseX, mouseY);
                return;
            }
        }

        if (hoveredSlot != null && !hoveredSlot.hasItem()) {
            List<Component> tooltip = slotTooltip(hoveredSlot);
            if (!tooltip.isEmpty()) {
                g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }
    }

    private List<Component> slotTooltip(Slot slot) {
        int containerIndex = menu.slots.indexOf(slot);
        if (containerIndex < 0 || containerIndex >= OxidizerMenu.MACHINE_SLOTS) {
            return List.of();
        }

        int lane = containerIndex / OxidizerMenu.SLOTS_PER_LANE;
        int offset = containerIndex % OxidizerMenu.SLOTS_PER_LANE;
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.lane", lane + 1).withStyle(ChatFormatting.GRAY));

        switch (offset) {
            case OxidizerMenu.CONTAINER_OFFSET_COPPER -> {
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.copper").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.copper.desc").withStyle(ChatFormatting.GRAY));
            }
            case OxidizerMenu.CONTAINER_OFFSET_CHIP -> {
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.chip").withStyle(ChatFormatting.AQUA));
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.chip.desc").withStyle(ChatFormatting.GRAY));
            }
            case OxidizerMenu.CONTAINER_OFFSET_WAX -> {
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.wax").withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.wax.desc").withStyle(ChatFormatting.GRAY));
            }
            case OxidizerMenu.CONTAINER_OFFSET_OUTPUT -> {
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.output").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("gui.mcooxidizer.oxidizer.slot.output.desc").withStyle(ChatFormatting.GRAY));
            }
            default -> {
                return List.of();
            }
        }
        return tooltip;
    }

    private static boolean isHoveringAbsolute(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static int laneAccent(int lane) {
        return switch (lane) {
            case 0 -> 0xFF3DAE9F;
            case 1 -> 0xFFD9823B;
            default -> 0xFF7CBF62;
        };
    }
}
