package com.styenvy.mcooxidizer.compat.jei;

import com.styenvy.mcooxidizer.CopperTransform;
import com.styenvy.mcooxidizer.MCOxidizer;
import com.styenvy.mcooxidizer.ModContent;
import com.styenvy.mcooxidizer.OxidizerIngredients;
import com.styenvy.mcooxidizer.StageChipItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record OxidizerJeiRecipe(ResourceLocation id, ItemStack input, ItemStack chip, boolean usesWax, ItemStack output) {
    public OxidizerJeiRecipe {
        input = input.copy();
        chip = chip.copy();
        output = output.copy();
    }

    public static List<OxidizerJeiRecipe> createAll() {
        List<OxidizerJeiRecipe> recipes = new ArrayList<>();

        for (ItemStack input : CopperTransform.supportedInputs()) {
            for (StageChipItem.Stage stage : StageChipItem.Stage.values()) {
                ItemStack chip = chipFor(stage);
                boolean inputIsWaxed = OxidizerIngredients.isWaxedCopperInput(input);

                addIfChanged(recipes, input, chip, false, CopperTransform.compute(input, stage, inputIsWaxed));

                if (!inputIsWaxed && OxidizerIngredients.hasWaxPrecursorOption()) {
                    addIfChanged(recipes, input, chip, true, CopperTransform.compute(input, stage, true));
                }
            }
        }

        return List.copyOf(recipes);
    }

    private static void addIfChanged(
            List<OxidizerJeiRecipe> recipes,
            ItemStack input,
            ItemStack chip,
            boolean usesWax,
            ItemStack output
    ) {
        if (output.isEmpty() || ItemStack.isSameItemSameComponents(input, output)) {
            return;
        }
        recipes.add(new OxidizerJeiRecipe(idFor(input, chip, usesWax, output), input, chip, usesWax, output));
    }

    private static ItemStack chipFor(StageChipItem.Stage stage) {
        return switch (stage) {
            case EXPOSED -> new ItemStack(ModContent.CHIP_EXPOSED.get());
            case WEATHERED -> new ItemStack(ModContent.CHIP_WEATHERED.get());
            case OXIDIZED -> new ItemStack(ModContent.CHIP_OXIDIZED.get());
        };
    }

    private static ResourceLocation idFor(ItemStack input, ItemStack chip, boolean usesWax, ItemStack output) {
        return ResourceLocation.fromNamespaceAndPath(
                MCOxidizer.MOD_ID,
                "oxidizing/"
                        + itemPath(input)
                        + "_with_"
                        + itemPath(chip)
                        + (usesWax ? "_and_wax" : "")
                        + "_to_"
                        + itemPath(output)
        );
    }

    private static String itemPath(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }
}
