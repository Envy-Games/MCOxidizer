package com.styenvy.mcooxidizer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public final class CopperTransform {
    private CopperTransform() {}

    public static ItemStack compute(ItemStack input, StageChipItem.Stage target, boolean wantWaxed) {
        // full blocks
        if (input.is(Blocks.COPPER_BLOCK.asItem()) || input.is(Blocks.EXPOSED_COPPER.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER.asItem()) || input.is(Blocks.OXIDIZED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_COPPER_BLOCK.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER.asItem()   : Blocks.EXPOSED_COPPER.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER.asItem() : Blocks.WEATHERED_COPPER.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER.asItem()  : Blocks.OXIDIZED_COPPER.asItem());
            };
        }

        // cut blocks
        if (input.is(Blocks.CUT_COPPER.asItem()) || input.is(Blocks.EXPOSED_CUT_COPPER.asItem()) ||
                input.is(Blocks.WEATHERED_CUT_COPPER.asItem()) || input.is(Blocks.OXIDIZED_CUT_COPPER.asItem()) ||
                input.is(Blocks.WAXED_CUT_COPPER.asItem()) || input.is(Blocks.WAXED_EXPOSED_CUT_COPPER.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CUT_COPPER.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CUT_COPPER.asItem()   : Blocks.EXPOSED_CUT_COPPER.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CUT_COPPER.asItem() : Blocks.WEATHERED_CUT_COPPER.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CUT_COPPER.asItem()  : Blocks.OXIDIZED_CUT_COPPER.asItem());
            };
        }

        // slabs
        if (input.is(Blocks.CUT_COPPER_SLAB.asItem()) || input.is(Blocks.EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                input.is(Blocks.WEATHERED_CUT_COPPER_SLAB.asItem()) || input.is(Blocks.OXIDIZED_CUT_COPPER_SLAB.asItem()) ||
                input.is(Blocks.WAXED_CUT_COPPER_SLAB.asItem()) || input.is(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB.asItem()   : Blocks.EXPOSED_CUT_COPPER_SLAB.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB.asItem() : Blocks.WEATHERED_CUT_COPPER_SLAB.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB.asItem()  : Blocks.OXIDIZED_CUT_COPPER_SLAB.asItem());
            };
        }

        // stairs
        if (input.is(Blocks.CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                input.is(Blocks.WEATHERED_CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.OXIDIZED_CUT_COPPER_STAIRS.asItem()) ||
                input.is(Blocks.WAXED_CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS.asItem()   : Blocks.EXPOSED_CUT_COPPER_STAIRS.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS.asItem() : Blocks.WEATHERED_CUT_COPPER_STAIRS.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS.asItem()  : Blocks.OXIDIZED_CUT_COPPER_STAIRS.asItem());
            };
        }

        // chiseled
        if (input.is(Blocks.CHISELED_COPPER.asItem()) || input.is(Blocks.EXPOSED_CHISELED_COPPER.asItem()) ||
                input.is(Blocks.WEATHERED_CHISELED_COPPER.asItem()) || input.is(Blocks.OXIDIZED_CHISELED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_CHISELED_COPPER.asItem()) || input.is(Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem()) || input.is(Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_CHISELED_COPPER.asItem()   : Blocks.EXPOSED_CHISELED_COPPER.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_CHISELED_COPPER.asItem() : Blocks.WEATHERED_CHISELED_COPPER.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_CHISELED_COPPER.asItem()  : Blocks.OXIDIZED_CHISELED_COPPER.asItem());
            };
        }

        // grates
        if (input.is(Blocks.COPPER_GRATE.asItem()) || input.is(Blocks.EXPOSED_COPPER_GRATE.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_GRATE.asItem()) || input.is(Blocks.OXIDIZED_COPPER_GRATE.asItem()) ||
                input.is(Blocks.WAXED_COPPER_GRATE.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_GRATE.asItem()   : Blocks.EXPOSED_COPPER_GRATE.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_GRATE.asItem() : Blocks.WEATHERED_COPPER_GRATE.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_GRATE.asItem()  : Blocks.OXIDIZED_COPPER_GRATE.asItem());
            };
        }

        // doors
        if (input.is(Blocks.COPPER_DOOR.asItem()) || input.is(Blocks.EXPOSED_COPPER_DOOR.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_DOOR.asItem()) || input.is(Blocks.OXIDIZED_COPPER_DOOR.asItem()) ||
                input.is(Blocks.WAXED_COPPER_DOOR.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_DOOR.asItem()   : Blocks.EXPOSED_COPPER_DOOR.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_DOOR.asItem() : Blocks.WEATHERED_COPPER_DOOR.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_DOOR.asItem()  : Blocks.OXIDIZED_COPPER_DOOR.asItem());
            };
        }

        // trapdoors
        if (input.is(Blocks.COPPER_TRAPDOOR.asItem()) || input.is(Blocks.EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_TRAPDOOR.asItem()) || input.is(Blocks.OXIDIZED_COPPER_TRAPDOOR.asItem()) ||
                input.is(Blocks.WAXED_COPPER_TRAPDOOR.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR.asItem()   : Blocks.EXPOSED_COPPER_TRAPDOOR.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR.asItem() : Blocks.WEATHERED_COPPER_TRAPDOOR.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR.asItem()  : Blocks.OXIDIZED_COPPER_TRAPDOOR.asItem());
            };
        }

        // bulbs (lamps)
        if (input.is(Blocks.COPPER_BULB.asItem()) || input.is(Blocks.EXPOSED_COPPER_BULB.asItem()) ||
                input.is(Blocks.WEATHERED_COPPER_BULB.asItem()) || input.is(Blocks.OXIDIZED_COPPER_BULB.asItem()) ||
                input.is(Blocks.WAXED_COPPER_BULB.asItem()) || input.is(Blocks.WAXED_EXPOSED_COPPER_BULB.asItem()) ||
                input.is(Blocks.WAXED_WEATHERED_COPPER_BULB.asItem()) || input.is(Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem())) {
            return switch (target) {
                case EXPOSED   -> new ItemStack(wantWaxed ? Blocks.WAXED_EXPOSED_COPPER_BULB.asItem()   : Blocks.EXPOSED_COPPER_BULB.asItem());
                case WEATHERED -> new ItemStack(wantWaxed ? Blocks.WAXED_WEATHERED_COPPER_BULB.asItem() : Blocks.WEATHERED_COPPER_BULB.asItem());
                case OXIDIZED  -> new ItemStack(wantWaxed ? Blocks.WAXED_OXIDIZED_COPPER_BULB.asItem()  : Blocks.OXIDIZED_COPPER_BULB.asItem());
            };
        }

        return ItemStack.EMPTY;
    }
}
