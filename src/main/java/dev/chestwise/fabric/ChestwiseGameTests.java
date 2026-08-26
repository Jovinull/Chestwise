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
            List<IndexedItem> diamonds = terminal.search("diamond", SortMode.QUANTITY);
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

    private static void assertCount(GameTestHelper helper, StorageTerminalBlockEntity terminal, long expected) {
        List<IndexedItem> diamonds = terminal.search("diamond", SortMode.QUANTITY);
        helper.assertTrue(diamonds.size() == 1, "Expected one indexed diamond variant");
        helper.assertTrue(diamonds.get(0).totalCount() == expected, "Unexpected indexed diamond count");
    }
}
//?}
