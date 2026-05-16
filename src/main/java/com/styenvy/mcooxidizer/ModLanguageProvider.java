package com.styenvy.mcooxidizer;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

final class ModLanguageProvider extends LanguageProvider {
    ModLanguageProvider(PackOutput output) {
        super(output, MCOxidizer.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addBlock(ModContent.OXIDIZER_BLOCK, "Oxidizer");
        addItem(ModContent.CHIP_EXPOSED, "Exposed Stage Chip");
        addItem(ModContent.CHIP_WEATHERED, "Weathered Stage Chip");
        addItem(ModContent.CHIP_OXIDIZED, "Oxidized Stage Chip");
        add(ModCreative.MAIN_TAB_TRANSLATION_KEY, "Oxidation Machine");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.title", "Oxidizer");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.active_lanes", "Active: %s/%s");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.conversions", "Conversion Lanes");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.energy", "FE");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.energy_hint", "Each active lane consumes FE every tick.");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.copper", "Cu");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.chip", "Chip");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.wax", "Wax");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.result", "Out");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.lane", "Lane %s");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.progress", "Lane %s Progress");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.progress_hint", "Runs while copper, a chip, output space, and FE are available.");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.copper", "Copper Input");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.copper.desc", "Accepts supported copper blocks and variants.");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.chip", "Stage Chip");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.chip.desc", "Controls the target oxidation stage for this lane.");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.wax", "Wax Additive");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.wax.desc", "Optional honeycomb or wax precursor for waxed output.");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.output", "Output");
        add("gui." + MCOxidizer.MOD_ID + ".oxidizer.slot.output.desc", "Finished conversions appear here.");
        add("jei." + MCOxidizer.MOD_ID + ".oxidizing", "Oxidizing");
    }
}
