package com.styenvy.mcooxidizer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

final class ModDataGenerators {
    static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
    }

    private ModDataGenerators() {}
}
