package com.styenvy.mcooxidizer;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

final class ModCreative {
    static void addToTabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(ModContent.OXIDIZER_ITEM.get());
        }
        if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            e.accept(ModContent.CHIP_EXPOSED.get());
            e.accept(ModContent.CHIP_WEATHERED.get());
            e.accept(ModContent.CHIP_OXIDIZED.get());
        }
    }
}
