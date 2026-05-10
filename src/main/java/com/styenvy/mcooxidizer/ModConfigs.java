package com.styenvy.mcooxidizer;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public final class ModConfigs {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ENERGY_PER_TICK =
            B.defineInRange("energyPerTick", 20, 1, 100000);
    public static final ModConfigSpec.IntValue PROCESS_TICKS =
            B.defineInRange("processTicks", 80, 1, 12000);
    public static final ModConfigSpec.IntValue BUFFER_FE =
            B.defineInRange("bufferFE", 100_000, 1000, 10_000_000);
    public static final ModConfigSpec.BooleanValue ALLOW_HONEYCOMB =
            B.define("allowHoneycomb", true);
    public static final ModConfigSpec.BooleanValue ALLOW_OIL_TAG =
            B.define("allowOilTag", true);

    public static final ModConfigSpec SPEC = B.build();

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}
