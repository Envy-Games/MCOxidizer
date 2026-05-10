package com.styenvy.mcooxidizer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class OxidizerIngredients {
    private OxidizerIngredients() {}

    public static boolean isCopperInput(ItemStack stack) {
        return CopperTransform.isSupportedInput(stack);
    }

    public static boolean isWaxedCopperInput(ItemStack stack) {
        return CopperTransform.isWaxedInput(stack);
    }

    public static boolean isWaxPrecursor(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (ModConfigs.ALLOW_HONEYCOMB.get() && stack.is(Items.HONEYCOMB)) {
            return true;
        }
        return ModConfigs.ALLOW_OIL_TAG.get() && stack.is(MCOxTags.WAX_PRECURSORS);
    }

    public static boolean hasWaxPrecursorOption() {
        return ModConfigs.ALLOW_HONEYCOMB.get()
                || (ModConfigs.ALLOW_OIL_TAG.get() && !Ingredient.of(MCOxTags.WAX_PRECURSORS).isEmpty());
    }
}
