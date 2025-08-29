package com.styenvy.mcooxidizer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public final class OxidizerScreen extends AbstractContainerScreen<OxidizerMenu> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("mcooxidizer", "textures/gui/oxidizer_screen.png");

    // Layout constants
    private static final int TITLE_X = 9,   TITLE_Y = 7;
    private static final int PROG_X  = 24,  PROG_Y  = 27;
    private static final int OUT_X   = 135, OUT_Y  = 46;
    private static final int FE_X    = 158, FE_Y   = 15;

    // Slot geometry (for centering the arrow)
    private static final int SLOT_W = 18, SLOT_H = 18;
    private static final int SLOT3_X = 66, SLOT3_Y = 46; // third input

    public OxidizerScreen(OxidizerMenu menu, Inventory inv, Component ignoredTitle) {
        super(menu, inv, Component.literal("Oxidizer"));
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = TITLE_X;
        this.titleLabelY = TITLE_Y;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        RenderSystem.enableBlend();
        g.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Centered arrow between 3rd input (right edge) and output (left edge)
        int thirdRight = leftPos + 66 + 18; // SLOT3_X + SLOT_W
        int outputLeft = leftPos + OUT_X;
        int gap = Math.max(0, outputLeft - thirdRight);

        int aw = Math.min(40, Math.max(24, gap - 8));
        int ah = 12;

        int centerX = thirdRight + gap / 2;
        int ax = centerX - aw / 2;
        int ay = topPos + 46 + (18 / 2) - (ah / 2); // SLOT3_Y + SLOT_H/2

        float prog = menu.progress() / (float)menu.maxProgress();
        drawProgressArrow(g, ax, ay, aw, ah, prog);

        // FE bar
        int eW = 8, eH = 58;
        int eX = leftPos + FE_X, eY = topPos + FE_Y;
        g.drawString(this.font, Component.literal("FE"), eX - 1, eY - 10, 0xFF000000, false);
        g.fill(eX - 1, eY - 1, eX + eW + 1, eY + eH + 1, 0xFF202020);
        g.fill(eX, eY, eX + eW, eY + eH, 0xFF2A2A2A);

        int eFill = (int)(eH * (menu.energy() / (float)menu.energyMax()));
        g.fill(eX, eY + (eH - eFill), eX + eW, eY + eH, 0xFF0090FF);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g, mx, my, partial);
        super.render(g, mx, my, partial);

        int eW = 8, eH = 58;
        int eX = leftPos + FE_X, eY = topPos + FE_Y;
        if (mx >= eX && mx < eX + eW && my >= eY && my < eY + eH) {
            g.renderTooltip(font, Component.literal(menu.energy() + " / " + menu.energyMax() + " FE"), mx, my);
        }
        renderTooltip(g, mx, my);
    }

    // Draws a horizontal progress arrow (left → right)
    private static void drawProgressArrow(GuiGraphics g, int x, int y, int w, int h, float progress) {
        progress = Math.max(0f, Math.min(1f, progress));
        if (w <= 4 || h <= 3) return;

        // Border
        g.fill(x, y, x + w, y + 1, -14671840);
        g.fill(x, y + h - 1, x + w, y + h, -14671840);
        g.fill(x, y, x + 1, y + h, -14671840);
        g.fill(x + w - 1, y, x + w, y + h, -14671840);

        // Background
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, -15066598);

        int innerX = x + 1, innerY = y + 1, innerW = w - 2, innerH = h - 2;
        int headW = Math.max(4, h / 2);
        int shaftW = Math.max(0, innerW - headW);

        int filled = Math.round(progress * innerW);

        // Fill shaft
        int shaftFill = Math.min(filled, shaftW);
        if (shaftFill > 0) g.fill(innerX, innerY, innerX + shaftFill, innerY + innerH, -16724907);

        // Fill head (triangular)
        int remaining = filled - shaftFill;
        if (remaining > 0) {
            int headStartX = innerX + shaftW;
            int slices = Math.min(remaining, headW);
            for (int i = 0; i < slices; i++) {
                int sliceX = headStartX + i;
                float t = (i + 0.5f) / headW;
                int half = Math.max(0, Math.round((innerH / 2f) * (1f - t)));
                int y1 = innerY + (innerH / 2) - half;
                int y2 = innerY + (innerH / 2) + half + 1;
                g.fill(sliceX, y1, sliceX + 1, y2, -16724907);
            }
        }
    }
}
