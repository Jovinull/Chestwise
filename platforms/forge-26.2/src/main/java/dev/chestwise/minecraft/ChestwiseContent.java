package dev.chestwise.minecraft;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Forge 26.2 registry bridge. All gameplay types remain loader-neutral. */
public final class ChestwiseContent {
    public static ChestwiseConfig CONFIG = ChestwiseConfig.DEFAULT;
    public static StorageTerminalBlock STORAGE_TERMINAL;
    public static BlockEntityType<StorageTerminalBlockEntity> STORAGE_TERMINAL_ENTITY;
    public static MenuType<StorageTerminalMenu> STORAGE_TERMINAL_MENU;

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, "chestwise");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "chestwise");
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "chestwise");
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, "chestwise");

    private static final RegistryObject<Block> TERMINAL = BLOCKS.register("storage_terminal", () ->
        STORAGE_TERMINAL = new StorageTerminalBlock(
            terminalProperties().setId(ResourceKey.create(Registries.BLOCK, id("storage_terminal")))
        )
    );
    private static final RegistryObject<Item> TERMINAL_ITEM = ITEMS.register("storage_terminal", () ->
        new BlockItem(
            TERMINAL.get(),
            new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id("storage_terminal")))
                .useBlockDescriptionPrefix()
        )
    );

    private ChestwiseContent() {
    }

    public static void configure(ChestwiseConfig config) {
        CONFIG = java.util.Objects.requireNonNull(config, "config");
    }

    public static void registerForge(BusGroup modBusGroup) {
        BLOCKS.register(modBusGroup);
        ITEMS.register(modBusGroup);
        BLOCK_ENTITIES.register("storage_terminal", () ->
            STORAGE_TERMINAL_ENTITY = new BlockEntityType<>(StorageTerminalBlockEntity::new, Set.of(TERMINAL.get()))
        );
        MENUS.register("storage_terminal", () ->
            STORAGE_TERMINAL_MENU = new MenuType<>(StorageTerminalMenu::new, FeatureFlags.VANILLA_SET)
        );
        BLOCK_ENTITIES.register(modBusGroup);
        MENUS.register(modBusGroup);
        BuildCreativeModeTabContentsEvent.BUS.addListener(ChestwiseContent::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(TERMINAL_ITEM.get());
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("chestwise", path);
    }

    private static BlockBehaviour.Properties terminalProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).strength(2.5F);
    }
}
