package dev.chestwise.fabric;

//? if fabric {
import dev.chestwise.core.IndexedItem;
import dev.chestwise.core.SortMode;
import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.StorageTerminalBlockEntity;
import dev.chestwise.minecraft.StorageTerminalMenu;
import java.util.List;
import net.minecraft.core.BlockPos;
//? if < 26.2 {
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
//?} else {
/*import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
*///?}
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
//? if < 26.2 {
import net.minecraft.world.inventory.ClickType;
//?} else {
/*import net.minecraft.world.inventory.ContainerInput;
*///?}
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** In-game regression tests executed by the dedicated GameTest server. */
@SuppressWarnings({"deprecation", "removal"})
//? if < 26.2 {
public final class ChestwiseGameTests implements FabricGameTest {
//?} else {
/*public final class ChestwiseGameTests {
*///?}
    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void discoversUpdatesAndInvalidatesLoadedContainers(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        BlockPos chestPosition = new BlockPos(3, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        helper.setBlock(chestPosition, Blocks.CHEST);

        //? if < 26.2 {
        Container chest = (Container) helper.getBlockEntity(chestPosition);
        //?} else {
        /*Container chest = helper.getBlockEntity(chestPosition, ChestBlockEntity.class);
        *///?}
        chest.setItem(0, new ItemStack(Items.DIAMOND, 37));
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}

        helper.runAfterDelay(5, () -> {
            assertCount(helper, terminal, 37);
            ItemStack withdrawn = terminal.withdraw(dev.chestwise.minecraft.ItemStackIdentity.identity(new ItemStack(Items.DIAMOND)), 20);
            helper.assertTrue(withdrawn.is(Items.DIAMOND) && withdrawn.getCount() == 20, "Server extraction returned the wrong stack");
            helper.assertTrue(chest.getItem(0).getCount() == 17, "Physical chest was not updated by extraction");
            int deposited = terminal.deposit(withdrawn, true);
            helper.assertTrue(deposited == 20 && withdrawn.isEmpty(), "Matching deposit did not consume the transferred stack");
            helper.assertTrue(chest.getItem(0).getCount() == 37, "Physical chest was not updated by deposit");
            chest.setItem(0, new ItemStack(Items.DIAMOND, 12));
            chest.setChanged();
        });
        helper.runAfterDelay(10, () -> {
            assertCount(helper, terminal, 12);
            helper.destroyBlock(chestPosition);
        });
        helper.runAfterDelay(15, () -> {
            List<IndexedItem> diamonds = exactDiamonds(terminal);
            helper.assertTrue(diamonds.isEmpty(), "Removed container remained in the terminal index");
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    //?} else {
    /*@GameTest(maxTicks = 40)
    *///?}
    public void craftingGridUsesTheServerRecipeManager(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        StorageTerminalMenu menu = new StorageTerminalMenu(1, player.getInventory(), terminal);

        menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START + 1).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START + 3).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START + 4).set(new ItemStack(Items.OAK_PLANKS));
        menu.slotsChanged(menu.getSlot(StorageTerminalMenu.CRAFT_START).container);

        helper.assertTrue(menu.getSlot(StorageTerminalMenu.RESULT_SLOT).getItem().is(Items.CRAFTING_TABLE),
            "3x3 terminal grid did not resolve the vanilla crafting-table recipe");
        menu.removed(player);
        helper.succeed();
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void keepsDamagedToolsAsExactSeparateVariants(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        BlockPos chestPosition = new BlockPos(3, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        helper.setBlock(chestPosition, Blocks.CHEST);
        //? if < 26.2 {
        Container chest = (Container) helper.getBlockEntity(chestPosition);
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*Container chest = helper.getBlockEntity(chestPosition, ChestBlockEntity.class);
        StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ItemStack lightlyUsed = new ItemStack(Items.DIAMOND_SWORD);
        lightlyUsed.setDamageValue(1);
        ItemStack heavilyUsed = new ItemStack(Items.DIAMOND_SWORD);
        heavilyUsed.setDamageValue(100);
        chest.setItem(0, lightlyUsed);
        chest.setItem(1, heavilyUsed);

        helper.runAfterDelay(5, () -> {
            List<IndexedItem> swords = terminal.search("diamond_sword", SortMode.REGISTRY);
            helper.assertTrue(swords.size() == 2, "Different damage components were aggregated together");
            ItemStack withdrawn = terminal.withdraw(dev.chestwise.minecraft.ItemStackIdentity.identity(lightlyUsed), 1);
            helper.assertTrue(withdrawn.is(Items.DIAMOND_SWORD) && withdrawn.getDamageValue() == 1,
                "Withdrawing one exact tool variant returned another variant");
            helper.assertTrue(chest.getItem(1).getDamageValue() == 100,
                "Withdrawing one exact tool variant mutated another variant");
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void menuIntentsRespectMatchingAndProtectedSlots(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        BlockPos chestPosition = new BlockPos(3, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        helper.setBlock(chestPosition, Blocks.CHEST);
        //? if < 26.2 {
        Container chest = (Container) helper.getBlockEntity(chestPosition);
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*Container chest = helper.getBlockEntity(chestPosition, ChestBlockEntity.class);
        StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        chest.setItem(0, new ItemStack(Items.GOLD_INGOT, 10));

        helper.runAfterDelay(5, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockPos terminalWorldPosition = terminal.getBlockPos();
            player.setPos(terminalWorldPosition.getX() + 0.5, terminalWorldPosition.getY() + 0.5,
                terminalWorldPosition.getZ() + 0.5);
            player.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 5));
            player.getInventory().setItem(10, new ItemStack(Items.EMERALD, 4));
            player.getInventory().setItem(36, new ItemStack(Items.IRON_BOOTS));
            player.getInventory().setItem(40, new ItemStack(Items.TORCH, 7));
            StorageTerminalMenu menu = new StorageTerminalMenu(2, player.getInventory(), terminal);
            menu.updateQuery("gold ingot");
            helper.assertTrue(menu.displayCount(0) == 10,
                "Menu did not synchronize the exact aggregate count for its display slot");

            //? if < 26.2 {
            menu.clicked(StorageTerminalMenu.PLAYER_START, 2, ClickType.PICKUP, player);
            //?} else {
            /*menu.clicked(StorageTerminalMenu.PLAYER_START, 2, ContainerInput.PICKUP, player);
            *///?}
            helper.assertTrue(menu.isProtectedInventorySlot(9), "Middle click did not protect the player slot");
            helper.assertTrue(menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_DEPOSIT_MATCHING),
                "Server rejected a valid deposit-matching intent");
            helper.assertTrue(chest.getItem(0).getCount() == 10 && player.getInventory().getItem(9).getCount() == 5,
                "Bulk deposit ignored slot protection");
            helper.assertTrue(player.getInventory().getItem(10).getCount() == 4,
                "Deposit matching moved an unrepresented item");

            helper.assertTrue(menu.clickMenuButton(
                player,
                StorageTerminalMenu.BUTTON_PROTECT_BASE + 9
            ), "Server rejected a valid persisted-slot protection intent");
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_DEPOSIT_MATCHING);
            helper.assertTrue(chest.getItem(0).getCount() == 15 && player.getInventory().getItem(9).isEmpty(),
                "Deposit matching did not move the represented exact variant");
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_DEPOSIT_ALL);
            helper.assertTrue(player.getInventory().getItem(10).isEmpty(), "Deposit all left an eligible stack behind");
            helper.assertTrue(chest.getItem(1).is(Items.EMERALD) && chest.getItem(1).getCount() == 4,
                "Deposit all did not update the physical container");
            helper.assertTrue(player.getInventory().getItem(36).is(Items.IRON_BOOTS),
                "Deposit all moved equipped armor outside the 36 storage slots");
            helper.assertTrue(player.getInventory().getItem(40).is(Items.TORCH)
                    && player.getInventory().getItem(40).getCount() == 7,
                "Deposit all moved the offhand slot outside the 36 storage slots");
            menu.removed(player);
            helper.succeed();
        });
    }

    private static void assertCount(GameTestHelper helper, StorageTerminalBlockEntity terminal, long expected) {
        List<IndexedItem> diamonds = exactDiamonds(terminal);
        helper.assertTrue(diamonds.size() == 1, "Expected one indexed diamond variant");
        helper.assertTrue(diamonds.get(0).totalCount() == expected, "Unexpected indexed diamond count");
    }

    private static List<IndexedItem> exactDiamonds(StorageTerminalBlockEntity terminal) {
        return terminal.search("diamond", SortMode.QUANTITY).stream()
            .filter(item -> item.descriptor().identity().itemId().equals("minecraft:diamond"))
            .toList();
    }
}
//?}
