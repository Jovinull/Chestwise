package dev.chestwise.fabric;

//? if fabric {
import dev.chestwise.core.IndexedItem;
import dev.chestwise.core.RecipeSlotCodec;
import dev.chestwise.core.RestockTargetBook;
import dev.chestwise.core.SortMode;
import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.ItemStackIdentity;
import dev.chestwise.minecraft.RestockSavedData;
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
import net.minecraft.server.level.ServerLevel;
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

        //? if <= 1.20.1 {
        long loadedRecipeIds = player.level().getServer().getRecipeManager().getRecipeIds().count();
        helper.assertTrue(loadedRecipeIds > 1_000L,
            "RecipeManager exposed only " + loadedRecipeIds + " recipe ids; expected the vanilla recipe set");
        //?}

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
    public void craftingGridSurvivesClosingAndIsSharedBetweenScreens(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}

        helper.runAfterDelay(5, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            StorageTerminalMenu first = new StorageTerminalMenu(2, player.getInventory(), terminal);
            first.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.BRICK, 4));
            first.removed(player);

            helper.assertTrue(terminal.craftingGrid().get(0).is(Items.BRICK),
                "Closing the terminal discarded the crafting grid instead of leaving it on the block");

            // A second screen must see the same stacks, not a private copy: two
            // copies is what would let the grid be duplicated or overwritten.
            StorageTerminalMenu second = new StorageTerminalMenu(3, player.getInventory(), terminal);
            helper.assertTrue(second.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.BRICK),
                "Reopening the terminal did not restore the crafting grid");
            second.getSlot(StorageTerminalMenu.CRAFT_START).set(ItemStack.EMPTY);
            helper.assertTrue(terminal.craftingGrid().get(0).isEmpty(),
                "The crafting grid is not shared with the block entity");
            second.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    //?} else {
    /*@GameTest(maxTicks = 40)
    *///?}
    public void sharedCraftResultInvalidatesForEveryOpenMenu(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ServerPlayer firstPlayer = helper.makeMockServerPlayerInLevel();
        ServerPlayer secondPlayer = helper.makeMockServerPlayerInLevel();
        StorageTerminalMenu first = new StorageTerminalMenu(20, firstPlayer.getInventory(), terminal);
        StorageTerminalMenu second = new StorageTerminalMenu(21, secondPlayer.getInventory(), terminal);

        first.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.OAK_PLANKS));
        first.getSlot(StorageTerminalMenu.CRAFT_START + 1).set(new ItemStack(Items.OAK_PLANKS));
        first.getSlot(StorageTerminalMenu.CRAFT_START + 3).set(new ItemStack(Items.OAK_PLANKS));
        first.getSlot(StorageTerminalMenu.CRAFT_START + 4).set(new ItemStack(Items.OAK_PLANKS));
        helper.assertTrue(first.getSlot(StorageTerminalMenu.RESULT_SLOT).getItem().is(Items.CRAFTING_TABLE),
            "First menu did not resolve the shared crafting result");
        helper.assertTrue(second.getSlot(StorageTerminalMenu.RESULT_SLOT).getItem().is(Items.CRAFTING_TABLE),
            "Second menu retained a private or stale crafting result");

        first.getSlot(StorageTerminalMenu.CRAFT_START).set(ItemStack.EMPTY);
        helper.assertTrue(second.getSlot(StorageTerminalMenu.RESULT_SLOT).getItem().isEmpty(),
            "A second open menu retained a result after the shared grid became invalid");
        first.removed(firstPlayer);
        second.removed(secondPlayer);
        helper.succeed();
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void breakingTheTerminalReturnsItsCraftingGridToStorage(GameTestHelper helper) {
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
        terminal.craftingGrid().set(0, new ItemStack(Items.LAPIS_LAZULI, 7));

        // 15 ticks: discovery has to have found the chest before deposit can
        // reach it. Asserting sooner made this test flaky.
        helper.runAfterDelay(15, () -> {
            helper.destroyBlock(terminalPosition);
            helper.runAfterDelay(5, () -> {
                int stored = 0;
                for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                    ItemStack candidate = chest.getItem(slot);
                    if (candidate.is(Items.LAPIS_LAZULI)) {
                        stored += candidate.getCount();
                    }
                }
                helper.assertTrue(stored == 7,
                    "Breaking the terminal did not move its crafting grid into storage; found " + stored);
                helper.succeed();
            });
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void recipeTransferTakesFromTheInventoryBeforeStorage(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.AMETHYST_SHARD, 5));

        // 15 ticks: the chest must be both discovered and polled into the index
        // before a withdrawal can find the redstone.
        helper.runAfterDelay(15, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockPos world = terminal.getBlockPos();
            player.setPos(world.getX() + 0.5, world.getY() + 0.5, world.getZ() + 0.5);
            player.getInventory().setItem(9, new ItemStack(Items.COPPER_INGOT, 3));
            StorageTerminalMenu menu = new StorageTerminalMenu(4, player.getInventory(), terminal);

            // Slot 0 is held by the player, slot 1 only by the chest.
            menu.fillRecipe(player, RecipeSlotCodec.encode(List.of(
                List.of("minecraft:copper_ingot"),
                List.of("minecraft:amethyst_shard")
            )));

            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.COPPER_INGOT),
                "Recipe transfer did not take the ingredient held by the player");
            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START + 1).getItem().is(Items.AMETHYST_SHARD),
                "Recipe transfer did not pull the missing ingredient out of storage");
            helper.assertTrue(player.getInventory().getItem(9).getCount() == 2,
                "Recipe transfer took more than one item from the player's inventory");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void insufficientRecipeTransferDoesNotWithdrawPartialIngredients(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.DIAMOND));
        helper.runAfterDelay(15, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockPos world = terminal.getBlockPos();
            player.setPos(world.getX() + 0.5, world.getY() + 0.5, world.getZ() + 0.5);
            player.getInventory().setItem(9, new ItemStack(Items.DIAMOND));
            StorageTerminalMenu menu = new StorageTerminalMenu(28, player.getInventory(), terminal);
            menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.GOLD_INGOT));

            // Two diamonds exist, but the requested recipe requires three.
            menu.fillRecipe(player, RecipeSlotCodec.encode(List.of(
                List.of("minecraft:diamond"),
                List.of("minecraft:diamond"),
                List.of("minecraft:diamond")
            )));

            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.GOLD_INGOT),
                "An insufficient transfer changed the pre-existing crafting grid");
            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START + 1).getItem().isEmpty()
                    && menu.getSlot(StorageTerminalMenu.CRAFT_START + 2).getItem().isEmpty(),
                "An insufficient transfer populated part of the crafting grid");
            helper.assertTrue(chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 1,
                "An insufficient transfer withdrew a partial ingredient from storage");
            helper.assertTrue(player.getInventory().getItem(9).is(Items.DIAMOND)
                    && player.getInventory().getItem(9).getCount() == 1,
                "An insufficient transfer consumed a partial player ingredient");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    //?} else {
    /*@GameTest(maxTicks = 40)
    *///?}
    public void breakingTheTerminalClearsAnActiveCraftingResult(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        StorageTerminalMenu menu = new StorageTerminalMenu(24, player.getInventory(), terminal);
        menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START + 1).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START + 3).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START + 4).set(new ItemStack(Items.OAK_PLANKS));
        helper.assertTrue(menu.getSlot(StorageTerminalMenu.RESULT_SLOT).getItem().is(Items.CRAFTING_TABLE),
            "Terminal did not expose an active crafting result before breaking");

        helper.destroyBlock(terminalPosition);
        helper.assertTrue(terminal.craftingResult().getItem(0).isEmpty(),
            "Breaking the terminal left a stale result available to an open menu");
        helper.assertTrue(terminal.craftingGrid().stream().allMatch(ItemStack::isEmpty),
            "Breaking the terminal left crafting inputs behind after they were returned or dropped");
        menu.removed(player);
        helper.succeed();
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    //?} else {
    /*@GameTest(maxTicks = 40)
    *///?}
    public void craftingPreservesVanillaRemainders(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        StorageTerminalMenu menu = new StorageTerminalMenu(25, player.getInventory(), terminal);
        ItemStack[] cake = {
            new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET), new ItemStack(Items.MILK_BUCKET),
            new ItemStack(Items.SUGAR), new ItemStack(Items.EGG), new ItemStack(Items.SUGAR),
            new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT), new ItemStack(Items.WHEAT)
        };
        for (int slot = 0; slot < cake.length; slot++) {
            menu.getSlot(StorageTerminalMenu.CRAFT_START + slot).set(cake[slot]);
        }
        helper.assertTrue(menu.getSlot(StorageTerminalMenu.RESULT_SLOT).getItem().is(Items.CAKE),
            "Terminal did not resolve the vanilla cake recipe");
        //? if < 26.2 {
        menu.clicked(StorageTerminalMenu.RESULT_SLOT, 0, ClickType.PICKUP, player);
        //?} else {
        /*menu.clicked(StorageTerminalMenu.RESULT_SLOT, 0, ContainerInput.PICKUP, player);
        *///?}
        helper.assertTrue(menu.getCarried().is(Items.CAKE), "Crafting did not move the result to the cursor");
        for (int slot = 0; slot < 3; slot++) {
            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START + slot).getItem().is(Items.BUCKET),
                "Crafting lost the milk-bucket remainder in slot " + slot);
        }
        for (int slot = 3; slot < StorageTerminalMenu.RECIPE_SLOTS; slot++) {
            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START + slot).getItem().isEmpty(),
                "Crafting left a consumed ingredient in slot " + slot);
        }
        menu.removed(player);
        helper.succeed();
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    //?} else {
    /*@GameTest(maxTicks = 40)
    *///?}
    public void clearingTheSharedGridPreservesItsNineSlots(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        StorageTerminalMenu menu = new StorageTerminalMenu(27, player.getInventory(), terminal);
        menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.OAK_PLANKS));
        menu.getSlot(StorageTerminalMenu.CRAFT_START).container.clearContent();
        helper.assertTrue(terminal.craftingGrid().size() == StorageTerminalMenu.RECIPE_SLOTS,
            "Clearing the terminal crafting container changed its fixed grid size");
        menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.OAK_PLANKS));
        helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.OAK_PLANKS),
            "The terminal crafting grid could not be reused after it was cleared");
        menu.removed(player);
        helper.succeed();
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 40)
    //?} else {
    /*@GameTest(maxTicks = 40)
    *///?}
    public void recipeTransferNeverOverwritesAnUnreturnedGrid(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos terminalWorldPosition = terminal.getBlockPos();
        player.setPos(terminalWorldPosition.getX() + 0.5, terminalWorldPosition.getY() + 0.5,
            terminalWorldPosition.getZ() + 0.5);
        player.getInventory().setItem(9, new ItemStack(Items.COPPER_INGOT));
        StorageTerminalMenu menu = new StorageTerminalMenu(22, player.getInventory(), terminal);
        menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.DRAGON_BREATH));

        // No discovery tick has run, so the grid item has no valid destination.
        // A transfer must leave it in place rather than overwriting it.
        menu.fillRecipe(player, RecipeSlotCodec.encode(List.of(List.of("minecraft:copper_ingot"))));
        helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.DRAGON_BREATH),
            "Recipe transfer overwrote a grid item that storage could not accept");
        helper.assertTrue(player.getInventory().getItem(9).is(Items.COPPER_INGOT),
            "Recipe transfer consumed an ingredient after failing to clear the grid");
        menu.removed(player);
        helper.succeed();
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void malformedRecipeTransferDoesNotMutateItems(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.COPPER_INGOT, 4));
        helper.runAfterDelay(15, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockPos world = terminal.getBlockPos();
            player.setPos(world.getX() + 0.5, world.getY() + 0.5, world.getZ() + 0.5);
            player.getInventory().setItem(9, new ItemStack(Items.GOLD_INGOT, 2));
            StorageTerminalMenu menu = new StorageTerminalMenu(26, player.getInventory(), terminal);
            menu.getSlot(StorageTerminalMenu.CRAFT_START).set(new ItemStack(Items.GOLD_INGOT));

            menu.fillRecipe(player, "not-an-item; ;\u0000;,,;");

            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.GOLD_INGOT),
                "Malformed recipe transfer changed an existing crafting input");
            helper.assertTrue(chest.getItem(0).is(Items.COPPER_INGOT) && chest.getItem(0).getCount() == 4,
                "Malformed recipe transfer changed storage");
            helper.assertTrue(player.getInventory().getItem(9).is(Items.GOLD_INGOT)
                    && player.getInventory().getItem(9).getCount() == 2,
                "Malformed recipe transfer changed the player inventory");
            menu.fillRecipe(player, "");
            helper.assertTrue(menu.getSlot(StorageTerminalMenu.CRAFT_START).getItem().is(Items.GOLD_INGOT),
                "An empty recipe transfer changed an existing crafting input");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void displayClickDepositsCarriedStacksAndIgnoresQuickCraft(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        BlockPos chestPosition = new BlockPos(3, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        helper.setBlock(chestPosition, Blocks.CHEST);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}
        helper.runAfterDelay(15, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BlockPos terminalWorldPosition = terminal.getBlockPos();
            player.setPos(terminalWorldPosition.getX() + 0.5, terminalWorldPosition.getY() + 0.5,
                terminalWorldPosition.getZ() + 0.5);
            StorageTerminalMenu menu = new StorageTerminalMenu(23, player.getInventory(), terminal);
            menu.setCarried(new ItemStack(Items.GLOWSTONE_DUST, 3));
            //? if < 26.2 {
            menu.clicked(0, 0, ClickType.PICKUP, player);
            //?} else {
            /*menu.clicked(0, 0, ContainerInput.PICKUP, player);
            *///?}
            helper.assertTrue(menu.getCarried().isEmpty(), "Clicking a display slot did not deposit the carried stack");
            helper.assertTrue(terminal.search("glowstone dust", SortMode.REGISTRY).stream()
                    .anyMatch(item -> item.descriptor().identity().itemId().equals("minecraft:glowstone_dust")
                        && item.totalCount() == 3),
                "Carried-stack deposit did not reach terminal storage");

            menu.setCarried(new ItemStack(Items.GLOWSTONE_DUST, 2));
            //? if < 26.2 {
            menu.clicked(0, 0, ClickType.PICKUP_ALL, player);
            //?} else {
            /*menu.clicked(0, 0, ContainerInput.PICKUP_ALL, player);
            *///?}
            helper.assertTrue(menu.getCarried().getCount() == 2
                    && terminal.search("glowstone dust", SortMode.REGISTRY).stream()
                        .anyMatch(item -> item.descriptor().identity().itemId().equals("minecraft:glowstone_dust")
                            && item.totalCount() == 3),
                "Double-click on a display slot withdrew items instead of preserving the cursor and storage");

            //? if < 26.2 {
            menu.clicked(0, 0, ClickType.QUICK_CRAFT, player);
            //?} else {
            /*menu.clicked(0, 0, ContainerInput.QUICK_CRAFT, player);
            *///?}
            helper.assertTrue(menu.getCarried().getCount() == 2,
                "Quick-craft over a display slot changed the carried stack");
            menu.removed(player);
            helper.succeed();
        });
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

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockFillsOnlyTheTargetDeficit(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.PRISMARINE_SHARD, 64));
        chest.setItem(1, new ItemStack(Items.PRISMARINE_SHARD, 64));

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            player.getInventory().setItem(0, new ItemStack(Items.PRISMARINE_SHARD, 16));
            setRestockTarget(player, new ItemStack(Items.PRISMARINE_SHARD), 64);
            StorageTerminalMenu menu = new StorageTerminalMenu(20, player.getInventory(), terminal);
            helper.assertTrue(menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK), "Restock intent was rejected");
            helper.assertTrue(count(player, new ItemStack(Items.PRISMARINE_SHARD)) == 64,
                "Restock did not fill the exact target");
            helper.assertTrue(chest.getItem(0).getCount() + chest.getItem(1).getCount() == 80,
                "Restock removed more than the target deficit");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockAcceptsPartialSourceAvailability(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.PHANTOM_MEMBRANE, 20));

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            player.getInventory().setItem(0, new ItemStack(Items.PHANTOM_MEMBRANE, 10));
            setRestockTarget(player, new ItemStack(Items.PHANTOM_MEMBRANE), 64);
            StorageTerminalMenu menu = new StorageTerminalMenu(21, player.getInventory(), terminal);
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK);
            helper.assertTrue(count(player, new ItemStack(Items.PHANTOM_MEMBRANE)) == 30,
                "A partial source should still restock the amount that exists");
            helper.assertTrue(chest.getItem(0).isEmpty(), "Partial source was not fully consumed");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockNeverExtractsBeyondPlayerCapacity(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.GLOW_INK_SAC, 64));
        chest.setItem(1, new ItemStack(Items.GLOW_INK_SAC, 64));

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            player.getInventory().setItem(0, new ItemStack(Items.GLOW_INK_SAC, 16));
            for (int slot = 1; slot < 36; slot++) {
                player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
            }
            setRestockTarget(player, new ItemStack(Items.GLOW_INK_SAC), 64);
            StorageTerminalMenu menu = new StorageTerminalMenu(22, player.getInventory(), terminal);
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK);
            helper.assertTrue(count(player, new ItemStack(Items.GLOW_INK_SAC)) == 64,
                "Restock did not use the remaining capacity in the matching stack");
            helper.assertTrue(chest.getItem(0).getCount() + chest.getItem(1).getCount() == 80,
                "Restock extracted items that could not fit in the player inventory");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockCountsButNeverMutatesProtectedSlots(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.HONEYCOMB, 20));

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            player.getInventory().setItem(0, new ItemStack(Items.HONEYCOMB, 60));
            setRestockTarget(player, new ItemStack(Items.HONEYCOMB), 64);
            StorageTerminalMenu menu = new StorageTerminalMenu(23, player.getInventory(), terminal);
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_PROTECT_BASE);
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK);
            helper.assertTrue(player.getInventory().getItem(0).getCount() == 60,
                "Restock changed a protected stack");
            helper.assertTrue(count(player, new ItemStack(Items.HONEYCOMB)) == 64,
                "Protected items were not counted toward the target");
            helper.assertTrue(chest.getItem(0).getCount() == 16,
                "Restock extracted more than the unprotected deficit");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockUsesTheConfiguredExactItemVariant(GameTestHelper helper) {
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
        ItemStack wrong = new ItemStack(Items.DIAMOND_PICKAXE);
        wrong.setDamageValue(10);
        ItemStack desired = new ItemStack(Items.DIAMOND_PICKAXE);
        desired.setDamageValue(1);
        chest.setItem(0, wrong.copy());
        chest.setItem(1, desired.copy());

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            setRestockTarget(player, desired, 1);
            StorageTerminalMenu menu = new StorageTerminalMenu(24, player.getInventory(), terminal);
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK);
            ItemStack received = findVariant(player, desired);
            helper.assertTrue(!received.isEmpty() && received.getDamageValue() == 1,
                "Restock did not insert the configured damage variant");
            helper.assertTrue(chest.getItem(0).getDamageValue() == 10 && chest.getItem(1).isEmpty(),
                "Restock extracted a different item variant");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockProcessesTargetsIndependently(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.SLIME_BALL, 4));

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            player.getInventory().setItem(0, new ItemStack(Items.MAGMA_CREAM, 64));
            setRestockTarget(player, new ItemStack(Items.MAGMA_CREAM), 64);
            setRestockTarget(player, new ItemStack(Items.SLIME_BALL), 16);
            setRestockTarget(player, new ItemStack(Items.DRAGON_BREATH), 4);
            StorageTerminalMenu menu = new StorageTerminalMenu(25, player.getInventory(), terminal);
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK);
            helper.assertTrue(count(player, new ItemStack(Items.MAGMA_CREAM)) == 64,
                "Already satisfied target changed");
            helper.assertTrue(count(player, new ItemStack(Items.SLIME_BALL)) == 4 && chest.getItem(0).isEmpty(),
                "Available partial target was not restocked");
            helper.assertTrue(count(player, new ItemStack(Items.DRAGON_BREATH)) == 0,
                "Missing target unexpectedly appeared");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void concurrentRestockCannotConsumeSourceTwice(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.FEATHER, 20));

        helper.runAfterDelay(15, () -> {
            ServerPlayer first = restockPlayer(helper, terminal);
            ServerPlayer second = restockPlayer(helper, terminal);
            setRestockTarget(first, new ItemStack(Items.FEATHER), 64);
            setRestockTarget(second, new ItemStack(Items.FEATHER), 64);
            StorageTerminalMenu firstMenu = new StorageTerminalMenu(26, first.getInventory(), terminal);
            StorageTerminalMenu secondMenu = new StorageTerminalMenu(27, second.getInventory(), terminal);
            firstMenu.clickMenuButton(first, StorageTerminalMenu.BUTTON_RESTOCK);
            secondMenu.clickMenuButton(second, StorageTerminalMenu.BUTTON_RESTOCK);
            long total = count(first, new ItemStack(Items.FEATHER)) + count(second, new ItemStack(Items.FEATHER))
                + chest.getItem(0).getCount();
            helper.assertTrue(total == 20, "Concurrent restock duplicated or lost source items");
            helper.assertTrue(Math.max(count(first, new ItemStack(Items.FEATHER)), count(second, new ItemStack(Items.FEATHER))) == 20,
                "The source was not consumed at most once");
            firstMenu.removed(first);
            secondMenu.removed(second);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockMenuActionsAreValidatedAndTargetsAreUpdatedNotDuplicated(GameTestHelper helper) {
        BlockPos terminalPosition = new BlockPos(1, 1, 1);
        BlockPos chestPosition = new BlockPos(3, 1, 1);
        helper.setBlock(terminalPosition, ChestwiseContent.STORAGE_TERMINAL);
        helper.setBlock(chestPosition, Blocks.CHEST);
        //? if < 26.2 {
        StorageTerminalBlockEntity terminal = (StorageTerminalBlockEntity) helper.getBlockEntity(terminalPosition);
        //?} else {
        /*StorageTerminalBlockEntity terminal = helper.getBlockEntity(terminalPosition, StorageTerminalBlockEntity.class);
        *///?}

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            player.getInventory().setItem(0, new ItemStack(Items.PRISMARINE_CRYSTALS, 1));
            StorageTerminalMenu menu = new StorageTerminalMenu(28, player.getInventory(), terminal);
            helper.assertTrue(menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_TARGET_VIEW), "Target view did not open");
            helper.assertTrue(menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_ADD_TARGET), "Add-target mode was rejected");
            clickPickup(menu, StorageTerminalMenu.PLAYER_START + 27, player);
            helper.assertTrue(restockData(player).targets(player.getUUID()).size() == 1,
                "Adding an item from inventory did not create exactly one target");
            clickPickup(menu, StorageTerminalMenu.RESTOCK_START, player);
            helper.assertTrue(menu.clickMenuButton(
                player,
                StorageTerminalMenu.BUTTON_SET_TARGET_BASE + 128
            ), "Target quantity update was rejected");
            helper.assertTrue(menu.selectedTargetCount() == 128, "Target quantity did not update");
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_ADD_TARGET);
            clickPickup(menu, StorageTerminalMenu.PLAYER_START + 27, player);
            List<RestockSavedData.Target> targets = restockData(player).targets(player.getUUID());
            helper.assertTrue(targets.size() == 1 && targets.get(0).desiredCount() == 128,
                "Selecting an existing target duplicated it or reset its quantity");
            menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_REMOVE_TARGET);
            helper.assertTrue(restockData(player).targets(player.getUUID()).isEmpty(),
                "Removing a selected target did not remove it");
            menu.removed(player);
            helper.succeed();
        });
    }

    //? if < 26.2 {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    //?} else {
    /*@GameTest(maxTicks = 100)
    *///?}
    public void restockRejectsAnInvalidTerminalDistance(GameTestHelper helper) {
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
        chest.setItem(0, new ItemStack(Items.RABBIT_FOOT, 5));

        helper.runAfterDelay(15, () -> {
            ServerPlayer player = restockPlayer(helper, terminal);
            setRestockTarget(player, new ItemStack(Items.RABBIT_FOOT), 5);
            player.setPos(100.0, 80.0, 100.0);
            StorageTerminalMenu menu = new StorageTerminalMenu(29, player.getInventory(), terminal);
            helper.assertTrue(!menu.clickMenuButton(player, StorageTerminalMenu.BUTTON_RESTOCK),
                "Out-of-range restock intent was accepted");
            helper.assertTrue(chest.getItem(0).getCount() == 5,
                "Out-of-range restock mutated physical storage");
            menu.removed(player);
            helper.succeed();
        });
    }

    private static ServerPlayer restockPlayer(GameTestHelper helper, StorageTerminalBlockEntity terminal) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos position = terminal.getBlockPos();
        player.setPos(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
        return player;
    }

    private static void setRestockTarget(ServerPlayer player, ItemStack sample, int desiredCount) {
        restockData(player).setTarget(player.getUUID(), sample, desiredCount);
    }

    private static RestockSavedData restockData(ServerPlayer player) {
        //? if < 26.2 {
        return RestockSavedData.get((ServerLevel) player.level());
        //?} else {
        /*return RestockSavedData.get(player.level());
        *///?}
    }

    private static long count(ServerPlayer player, ItemStack exactStack) {
        long count = 0;
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (ItemStackIdentity.sameVariant(stack, exactStack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ItemStack findVariant(ServerPlayer player, ItemStack exactStack) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (ItemStackIdentity.sameVariant(stack, exactStack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void clickPickup(StorageTerminalMenu menu, int slotId, ServerPlayer player) {
        //? if < 26.2 {
        menu.clicked(slotId, 0, ClickType.PICKUP, player);
        //?} else {
        /*menu.clicked(slotId, 0, ContainerInput.PICKUP, player);
        *///?}
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
