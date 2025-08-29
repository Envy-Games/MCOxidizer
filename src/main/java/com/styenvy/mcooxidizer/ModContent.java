package com.styenvy.mcooxidizer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MCOxidizer.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MCOxidizer.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BEs =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MCOxidizer.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MCOxidizer.MOD_ID);

    public static final DeferredHolder<Block, OxidizerBlock> OXIDIZER_BLOCK = BLOCKS.register(
            "oxidizer",
            () -> new OxidizerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Item, BlockItem> OXIDIZER_ITEM = ITEMS.register(
            "oxidizer",
            () -> new BlockItem(OXIDIZER_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, StageChipItem> CHIP_EXPOSED = ITEMS.register(
            "chip_exposed",
            () -> new StageChipItem(StageChipItem.Stage.EXPOSED, new Item.Properties())
    );
    public static final DeferredHolder<Item, StageChipItem> CHIP_WEATHERED = ITEMS.register(
            "chip_weathered",
            () -> new StageChipItem(StageChipItem.Stage.WEATHERED, new Item.Properties())
    );
    public static final DeferredHolder<Item, StageChipItem> CHIP_OXIDIZED = ITEMS.register(
            "chip_oxidized",
            () -> new StageChipItem(StageChipItem.Stage.OXIDIZED, new Item.Properties())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxidizerBlockEntity>> OXIDIZER_BE =
            BEs.register("oxidizer",
                    () -> BlockEntityType.Builder.of(OxidizerBlockEntity::new, OXIDIZER_BLOCK.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<OxidizerMenu>> OXIDIZER_MENU =
            MENUS.register("oxidizer",
                    () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(
                            OxidizerMenu::fromNetwork
                    ));

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BEs.register(bus);
        MENUS.register(bus);
    }

    private ModContent() {}
}
