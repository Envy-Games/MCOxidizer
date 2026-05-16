package com.styenvy.mcooxidizer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

final class ModCreative {
    static final String MAIN_TAB_TRANSLATION_KEY = "itemGroup." + MCOxidizer.MOD_ID + ".main";

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MCOxidizer.MOD_ID);

    static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable(MAIN_TAB_TRANSLATION_KEY))
                    .icon(() -> new ItemStack(ModContent.OXIDIZER_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModContent.OXIDIZER_ITEM.get());
                        output.accept(ModContent.CHIP_EXPOSED.get());
                        output.accept(ModContent.CHIP_WEATHERED.get());
                        output.accept(ModContent.CHIP_OXIDIZED.get());
                    })
                    .build()
    );

    static void init(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }

    private ModCreative() {}
}
