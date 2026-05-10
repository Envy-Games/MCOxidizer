package com.styenvy.mcooxidizer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = MCOxidizer.MOD_ID, dist = Dist.CLIENT)
public final class ModClient {
    public ModClient(IEventBus modBus) {
        modBus.addListener(ModClient::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.OXIDIZER_MENU.get(), OxidizerScreen::new);
    }
}
