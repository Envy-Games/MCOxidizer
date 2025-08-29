package com.styenvy.mcooxidizer;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.bus.api.IEventBus;

public final class ModClient {
    private ModClient() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(ModClient::onRegisterScreens);
    }

    private static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModContent.OXIDIZER_MENU.get(), OxidizerScreen::new);
    }
}
