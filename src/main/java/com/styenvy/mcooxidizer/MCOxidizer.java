package com.styenvy.mcooxidizer;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;

@Mod(MCOxidizer.MOD_ID)
public final class MCOxidizer {
    public static final String MOD_ID = "mcooxidizer";

    public MCOxidizer(IEventBus modBus, ModContainer container) {
        ModContent.init(modBus);
        ModConfigs.register(container);
        modBus.addListener(ModCreative::addToTabs);
        modBus.addListener(ModCapabilities::register);
    }
}
