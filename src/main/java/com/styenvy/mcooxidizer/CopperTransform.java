package com.styenvy.mcooxidizer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class CopperTransform {
    private static final List<CopperFamily> FAMILIES = List.of(
            new CopperFamily(
                    Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER,
                    Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER
            ),
            new CopperFamily(
                    Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER,
                    Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER
            ),
            new CopperFamily(
                    Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB,
                    Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB
            ),
            new CopperFamily(
                    Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS,
                    Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS
            ),
            new CopperFamily(
                    Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER,
                    Blocks.WAXED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER
            ),
            new CopperFamily(
                    Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE,
                    Blocks.WAXED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE
            ),
            new CopperFamily(
                    Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR,
                    Blocks.WAXED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR
            ),
            new CopperFamily(
                    Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR,
                    Blocks.WAXED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR
            ),
            new CopperFamily(
                    Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB,
                    Blocks.WAXED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB
            )
    );

    private CopperTransform() {}

    public static ItemStack compute(ItemStack input, StageChipItem.Stage target, boolean wantWaxed) {
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (CopperFamily family : FAMILIES) {
            if (family.contains(input.getItem())) {
                return family.stackFor(target, wantWaxed);
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean isSupportedInput(ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }
        for (CopperFamily family : FAMILIES) {
            if (family.contains(input.getItem())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWaxedInput(ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }
        for (CopperFamily family : FAMILIES) {
            if (family.isWaxed(input.getItem())) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> supportedInputs() {
        List<ItemStack> inputs = new ArrayList<>();
        for (CopperFamily family : FAMILIES) {
            family.addInputs(inputs);
        }
        return List.copyOf(inputs);
    }

    private static int targetIndex(StageChipItem.Stage target) {
        return switch (target) {
            case EXPOSED -> 1;
            case WEATHERED -> 2;
            case OXIDIZED -> 3;
        };
    }

    private record CopperFamily(ItemLike[] raw, ItemLike[] waxed) {
        private CopperFamily(
                ItemLike rawUnaffected, ItemLike rawExposed, ItemLike rawWeathered, ItemLike rawOxidized,
                ItemLike waxedUnaffected, ItemLike waxedExposed, ItemLike waxedWeathered, ItemLike waxedOxidized
        ) {
            this(
                    new ItemLike[] { rawUnaffected, rawExposed, rawWeathered, rawOxidized },
                    new ItemLike[] { waxedUnaffected, waxedExposed, waxedWeathered, waxedOxidized }
            );
        }

        private boolean contains(Item item) {
            return contains(raw, item) || contains(waxed, item);
        }

        private boolean isWaxed(Item item) {
            return contains(waxed, item);
        }

        private ItemStack stackFor(StageChipItem.Stage target, boolean wantWaxed) {
            ItemLike[] items = wantWaxed ? waxed : raw;
            return new ItemStack(items[targetIndex(target)]);
        }

        private void addInputs(List<ItemStack> inputs) {
            addInputs(inputs, raw);
            addInputs(inputs, waxed);
        }

        private static void addInputs(List<ItemStack> inputs, ItemLike[] items) {
            for (ItemLike item : items) {
                inputs.add(new ItemStack(item));
            }
        }

        private static boolean contains(ItemLike[] items, Item item) {
            for (ItemLike candidate : items) {
                if (candidate.asItem() == item) {
                    return true;
                }
            }
            return false;
        }
    }
}
