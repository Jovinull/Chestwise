package dev.chestwise.minecraft;

import net.minecraft.core.registries.BuiltInRegistries;
//? if < 26.2 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
*///?}
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

//? if fabric {
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
//? if < 26.2 {
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
//?} else {
/*import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
*///?}
import net.minecraft.core.Registry;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
//?}

//? if forge || (neoforge && <= 1.20.1) {
/*import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
*///?}

//? if neoforge && > 1.20.1 {
/*import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

public final class ChestwiseContent {
    public static ChestwiseConfig CONFIG = ChestwiseConfig.DEFAULT;
    public static StorageTerminalBlock STORAGE_TERMINAL;
    public static BlockEntityType<StorageTerminalBlockEntity> STORAGE_TERMINAL_ENTITY;
    public static MenuType<StorageTerminalMenu> STORAGE_TERMINAL_MENU;

    //? if forge || (neoforge && <= 1.20.1) {
    /*private static final DeferredRegister<Block> FORGE_BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "chestwise");
    private static final DeferredRegister<Item> FORGE_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "chestwise");
    private static final DeferredRegister<BlockEntityType<?>> FORGE_BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "chestwise");
    private static final DeferredRegister<MenuType<?>> FORGE_MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "chestwise");
    private static final RegistryObject<Block> FORGE_TERMINAL = FORGE_BLOCKS.register("storage_terminal", () ->
        STORAGE_TERMINAL = new StorageTerminalBlock(terminalProperties())
    );
    private static final RegistryObject<Item> FORGE_TERMINAL_ITEM = FORGE_ITEMS.register("storage_terminal", () ->
        new BlockItem(FORGE_TERMINAL.get(), new Item.Properties())
    );
    *///?}

    //? if neoforge && > 1.20.1 {
    /*private static final DeferredRegister.Blocks NEO_BLOCKS = DeferredRegister.createBlocks("chestwise");
    private static final DeferredRegister.Items NEO_ITEMS = DeferredRegister.createItems("chestwise");
    private static final DeferredRegister<BlockEntityType<?>> NEO_BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "chestwise");
    private static final DeferredRegister<MenuType<?>> NEO_MENUS = DeferredRegister.create(Registries.MENU, "chestwise");
    //? if < 26.2 {
    private static final DeferredBlock<StorageTerminalBlock> NEO_TERMINAL = NEO_BLOCKS.register("storage_terminal", () ->
        STORAGE_TERMINAL = new StorageTerminalBlock(terminalProperties())
    );
    private static final DeferredItem<BlockItem> NEO_TERMINAL_ITEM = NEO_ITEMS.register("storage_terminal", () ->
        new BlockItem(NEO_TERMINAL.get(), new Item.Properties())
    );
    //?} else {
    private static final DeferredBlock<StorageTerminalBlock> NEO_TERMINAL = NEO_BLOCKS.registerBlock(
        "storage_terminal",
        properties -> STORAGE_TERMINAL = new StorageTerminalBlock(properties),
        ChestwiseContent::terminalProperties
    );
    private static final DeferredItem<BlockItem> NEO_TERMINAL_ITEM = NEO_ITEMS.registerItem(
        "storage_terminal",
        properties -> new BlockItem(NEO_TERMINAL.get(), properties.useBlockDescriptionPrefix())
    );
    //?}
    *///?}

    private ChestwiseContent() {
    }

    public static void configure(ChestwiseConfig config) {
        CONFIG = java.util.Objects.requireNonNull(config, "config");
    }

    //? if fabric {
    public static void registerFabric() {
        //? if < 26.2 {
        ResourceLocation terminalId = id("storage_terminal");
        //?} else {
        /*Identifier terminalId = id("storage_terminal");
        *///?}
        STORAGE_TERMINAL = Registry.register(
            BuiltInRegistries.BLOCK,
            terminalId,
            new StorageTerminalBlock(
                //? if < 26.2 {
                terminalProperties()
                //?} else {
                /*terminalProperties().setId(ResourceKey.create(Registries.BLOCK, terminalId))
                *///?}
            )
        );
        Registry.register(
            BuiltInRegistries.ITEM,
            terminalId,
            new BlockItem(
                STORAGE_TERMINAL,
                //? if < 26.2 {
                new Item.Properties()
                //?} else {
                /*new Item.Properties().setId(ResourceKey.create(Registries.ITEM, terminalId)).useBlockDescriptionPrefix()
                *///?}
            )
        );
        STORAGE_TERMINAL_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            terminalId,
            //? if < 26.2 {
            BlockEntityType.Builder.of(StorageTerminalBlockEntity::new, STORAGE_TERMINAL).build(null)
            //?} else {
            /*new BlockEntityType<>(StorageTerminalBlockEntity::new, java.util.Set.of(STORAGE_TERMINAL))
            *///?}
        );
        STORAGE_TERMINAL_MENU = Registry.register(
            BuiltInRegistries.MENU,
            terminalId,
            new MenuType<>(StorageTerminalMenu::new, FeatureFlags.VANILLA_SET)
        );
        //? if < 26.2 {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(entries -> entries.accept(STORAGE_TERMINAL));
        //?} else {
        /*CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register(entries -> entries.accept(STORAGE_TERMINAL));
        *///?}
    }
    //?}

    //? if forge || (neoforge && <= 1.20.1) {
    /*public static void registerForge(IEventBus modBus) {
        FORGE_BLOCKS.register(modBus);
        FORGE_ITEMS.register(modBus);
        FORGE_BLOCK_ENTITIES.register("storage_terminal", () ->
            STORAGE_TERMINAL_ENTITY = BlockEntityType.Builder.of(StorageTerminalBlockEntity::new, FORGE_TERMINAL.get()).build(null)
        );
        FORGE_MENUS.register("storage_terminal", () ->
            STORAGE_TERMINAL_MENU = new MenuType<>(StorageTerminalMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET)
        );
        FORGE_BLOCK_ENTITIES.register(modBus);
        FORGE_MENUS.register(modBus);
        modBus.addListener(ChestwiseContent::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(FORGE_TERMINAL_ITEM.get());
        }
    }
    *///?}

    //? if neoforge && > 1.20.1 {
    /*public static void registerNeoForge(IEventBus modBus) {
        NEO_BLOCKS.register(modBus);
        NEO_ITEMS.register(modBus);
        NEO_BLOCK_ENTITIES.register("storage_terminal", () ->
            //? if < 26.2 {
            STORAGE_TERMINAL_ENTITY = BlockEntityType.Builder.of(StorageTerminalBlockEntity::new, NEO_TERMINAL.get()).build(null)
            //?} else {
            STORAGE_TERMINAL_ENTITY = new BlockEntityType<>(StorageTerminalBlockEntity::new, java.util.Set.of(NEO_TERMINAL.get()))
            //?}
        );
        NEO_MENUS.register("storage_terminal", () ->
            STORAGE_TERMINAL_MENU = new MenuType<>(StorageTerminalMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET)
        );
        NEO_BLOCK_ENTITIES.register(modBus);
        NEO_MENUS.register(modBus);
        modBus.addListener(ChestwiseContent::addToNeoCreativeTab);
    }

    private static void addToNeoCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(NEO_TERMINAL_ITEM.get());
        }
    }
    *///?}

    @SuppressWarnings({"deprecation", "removal"})
    //? if < 26.2 {
    public static ResourceLocation id(String path) {
    //?} else {
    /*public static Identifier id(String path) {
    *///?}
        //? if <= 1.20.1 {
        return new ResourceLocation("chestwise", path);
        //?}
        /*? if > 1.20.1 {*/
        /*//? if < 26.2 {
        return ResourceLocation.fromNamespaceAndPath("chestwise", path);
        //?} else {
        return Identifier.fromNamespaceAndPath("chestwise", path);
        //?}
        *//*?}*/
    }

    private static BlockBehaviour.Properties terminalProperties() {
        // noOcclusion: the terminal is a desk, not a full cube, so neighbouring
        // faces must not be culled against it.
        //? if <= 1.20.1 {
        return BlockBehaviour.Properties.copy(Blocks.BARREL).strength(2.5F).noOcclusion();
        //?}
        /*? if > 1.20.1 {*/
        /*return BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).strength(2.5F).noOcclusion();
        *//*?}*/
    }
}
