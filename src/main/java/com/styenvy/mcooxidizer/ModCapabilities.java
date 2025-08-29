package com.styenvy.mcooxidizer;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ModCapabilities {
    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModContent.OXIDIZER_BE.get(),
                OxidizerBlockEntity::getItemHandlerForSide
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModContent.OXIDIZER_BE.get(),
                (OxidizerBlockEntity be, net.minecraft.core.Direction side) -> be.getExternalEnergy()
        );
    }
}