package com.styenvy.mcooxidizer;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;

@Mod(MCOxidizer.MOD_ID)
public final class MCOxidizer {
    public static final String MOD_ID = "mcooxidizer";

    public MCOxidizer(IEventBus modBus) {
        ModContent.init(modBus);
        ModConfigs.register(modBus);
        ModClient.register(modBus);
        modBus.addListener(ModCreative::addToTabs);
    }
}
