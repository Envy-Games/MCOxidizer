package com.styenvy.mcooxidizer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class MCOxTags {
    public static final TagKey<Item> WAX_PRECURSORS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MCOxidizer.MOD_ID, "wax_precursors"));

    public static final TagKey<Item> COPPER_INPUTS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MCOxidizer.MOD_ID, "copper_inputs"));

    public static final TagKey<Item> WAXED_COPPER_INPUTS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MCOxidizer.MOD_ID, "waxed_copper_inputs"));

}
