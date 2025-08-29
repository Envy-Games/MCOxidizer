package com.styenvy.mcooxidizer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class StageChipItem extends Item {
    public enum Stage { EXPOSED, WEATHERED, OXIDIZED }
    private final Stage stage;

    public StageChipItem(Stage stage, Properties props){ super(props); this.stage = stage; }
    public Stage getStage(){ return stage; }

    public static Stage fromStack(ItemStack stack){
        if (stack.getItem() instanceof StageChipItem chip) return chip.getStage();
        return null;
    }
}
