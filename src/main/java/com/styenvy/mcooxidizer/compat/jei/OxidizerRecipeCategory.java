package com.styenvy.mcooxidizer.compat.jei;

import com.styenvy.mcooxidizer.MCOxidizer;
import com.styenvy.mcooxidizer.MCOxTags;
import com.styenvy.mcooxidizer.ModConfigs;
import com.styenvy.mcooxidizer.ModContent;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public final class OxidizerRecipeCategory implements IRecipeCategory<OxidizerJeiRecipe> {
    public static final RecipeType<OxidizerJeiRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath(MCOxidizer.MOD_ID, "oxidizing"),
            OxidizerJeiRecipe.class
    );

    private static final int WIDTH = 126;
    private static final int HEIGHT = 54;
    private static final int SLOT_Y = 18;

    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable plus;

    public OxidizerRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModContent.OXIDIZER_ITEM.get()));
        this.arrow = guiHelper.getRecipeArrow();
        this.plus = guiHelper.getRecipePlusSign();
    }

    @Override
    public @NotNull RecipeType<OxidizerJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.mcooxidizer.oxidizing");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull OxidizerJeiRecipe recipe, @NotNull IFocusGroup focuses) {
        int inputX = recipe.usesWax() ? 1 : 14;
        builder.addInputSlot(inputX, SLOT_Y)
                .setSlotName("copper")
                .setStandardSlotBackground()
                .addItemStack(recipe.input());
        builder.addInputSlot(inputX + 25, SLOT_Y)
                .setSlotName("chip")
                .setStandardSlotBackground()
                .addItemStack(recipe.chip());

        if (recipe.usesWax()) {
            addWaxSlot(builder.addInputSlot(inputX + 50, SLOT_Y)
                    .setSlotName("wax")
                    .setStandardSlotBackground());
        }

        builder.addOutputSlot(104, SLOT_Y)
                .setSlotName("output")
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    private static void addWaxSlot(IRecipeSlotBuilder waxSlot) {
        if (ModConfigs.ALLOW_HONEYCOMB.get()) {
            waxSlot.addItemStack(new ItemStack(Items.HONEYCOMB));
        }
        if (ModConfigs.ALLOW_OIL_TAG.get()) {
            Ingredient waxPrecursors = Ingredient.of(MCOxTags.WAX_PRECURSORS);
            if (!waxPrecursors.isEmpty()) {
                waxSlot.addIngredients(waxPrecursors);
            }
        }
    }

    @Override
    public void draw(
            @NotNull OxidizerJeiRecipe recipe,
            @NotNull IRecipeSlotsView recipeSlotsView,
            @NotNull GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        int plusY = SLOT_Y + 3;
        if (recipe.usesWax()) {
            plus.draw(guiGraphics, 20, plusY);
            plus.draw(guiGraphics, 45, plusY);
            arrow.draw(guiGraphics, 76, SLOT_Y + 1);
        } else {
            plus.draw(guiGraphics, 33, plusY);
            arrow.draw(guiGraphics, 70, SLOT_Y + 1);
        }
    }

    @Override
    public ResourceLocation getRegistryName(@NotNull OxidizerJeiRecipe recipe) {
        return recipe.id();
    }
}
