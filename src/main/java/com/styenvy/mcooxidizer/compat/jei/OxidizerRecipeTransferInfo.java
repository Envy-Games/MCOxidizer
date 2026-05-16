package com.styenvy.mcooxidizer.compat.jei;

import com.styenvy.mcooxidizer.ModContent;
import com.styenvy.mcooxidizer.OxidizerMenu;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class OxidizerRecipeTransferInfo implements IRecipeTransferInfo<OxidizerMenu, OxidizerJeiRecipe> {
    @Override
    public @NotNull Class<? extends OxidizerMenu> getContainerClass() {
        return OxidizerMenu.class;
    }

    @Override
    public @NotNull Optional<MenuType<OxidizerMenu>> getMenuType() {
        return Optional.of(ModContent.OXIDIZER_MENU.get());
    }

    @Override
    public @NotNull RecipeType<OxidizerJeiRecipe> getRecipeType() {
        return OxidizerRecipeCategory.TYPE;
    }

    @Override
    public boolean canHandle(@NotNull OxidizerMenu container, @NotNull OxidizerJeiRecipe recipe) {
        return true;
    }

    @Override
    public @NotNull List<Slot> getRecipeSlots(@NotNull OxidizerMenu container, @NotNull OxidizerJeiRecipe recipe) {
        int lane = chooseLane(container, recipe);
        List<Slot> recipeSlots = new ArrayList<>();
        recipeSlots.add(container.slots.get(OxidizerMenu.copperIndex(lane)));
        recipeSlots.add(container.slots.get(OxidizerMenu.chipIndex(lane)));
        if (recipe.usesWax()) {
            recipeSlots.add(container.slots.get(OxidizerMenu.waxIndex(lane)));
        }
        return recipeSlots;
    }

    @Override
    public @NotNull List<Slot> getInventorySlots(@NotNull OxidizerMenu container, @NotNull OxidizerJeiRecipe recipe) {
        int start = OxidizerMenu.PLAYER_START;
        int end = start + OxidizerMenu.PLAYER_SLOTS;
        return container.slots.subList(start, end);
    }

    @Override
    public boolean requireCompleteSets(@NotNull OxidizerMenu container, @NotNull OxidizerJeiRecipe recipe) {
        return true;
    }

    private static int chooseLane(OxidizerMenu container, OxidizerJeiRecipe recipe) {
        for (int lane = 0; lane < OxidizerMenu.LANE_COUNT; lane++) {
            if (!container.slots.get(OxidizerMenu.copperIndex(lane)).hasItem()
                    && !container.slots.get(OxidizerMenu.chipIndex(lane)).hasItem()
                    && (!recipe.usesWax() || !container.slots.get(OxidizerMenu.waxIndex(lane)).hasItem())) {
                return lane;
            }
        }
        return 0;
    }
}
