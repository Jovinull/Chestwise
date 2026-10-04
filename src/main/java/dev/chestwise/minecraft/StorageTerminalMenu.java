package dev.chestwise.minecraft;

import dev.chestwise.core.IndexedItem;
import dev.chestwise.core.ItemIdentity;
import dev.chestwise.core.RecipeSlotCodec;
import dev.chestwise.core.RecipeSlotPlanner;
import dev.chestwise.core.SortMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
//? if < 26.2 {
import net.minecraft.world.inventory.ClickType;
//?} else {
/*import net.minecraft.world.inventory.ContainerInput;
*///?}
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;

public final class StorageTerminalMenu extends AbstractContainerMenu {
    public static final int COLUMNS = 9;
    public static final int ROWS = 5;
    public static final int DISPLAY_SLOTS = COLUMNS * ROWS;
    public static final int PLAYER_STORAGE_SLOTS = 36;
    public static final int RECIPE_SLOTS = RecipeSlotCodec.SLOTS;
    public static final int CRAFT_START = DISPLAY_SLOTS;
    public static final int CRAFT_END = CRAFT_START + 9;
    public static final int RESULT_SLOT = CRAFT_END;
    public static final int PLAYER_START = RESULT_SLOT + 1;
    public static final int BUTTON_PREVIOUS = 0;
    public static final int BUTTON_NEXT = 1;
    public static final int BUTTON_SORT = 2;
    public static final int BUTTON_DEPOSIT_MATCHING = 3;
    public static final int BUTTON_DEPOSIT_ALL = 4;
    public static final int BUTTON_SCROLL_WITHDRAW_BASE = 100;
    public static final int BUTTON_SCROLL_DEPOSIT_BASE = 200;
    public static final int BUTTON_PROTECT_BASE = 300;

    private final SimpleContainer display = new SimpleContainer(DISPLAY_SLOTS);
    private final long[] displayCounts = new long[DISPLAY_SLOTS];
    private final List<ItemIdentity> identities = new ArrayList<>(DISPLAY_SLOTS);
    private final Set<Integer> protectedSlots = new HashSet<>();
    private final net.minecraft.world.inventory.CraftingContainer crafting;
    private final ResultContainer craftingResult;
    private final StorageTerminalBlockEntity terminal;
    private final Player menuPlayer;
    private String query = "";
    private SortMode sortMode = SortMode.QUANTITY;
    private int page;
    private int totalPages = 1;

    public StorageTerminalMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, null);
    }

    public StorageTerminalMenu(int containerId, Inventory inventory, StorageTerminalBlockEntity terminal) {
        super(ChestwiseContent.STORAGE_TERMINAL_MENU, containerId);
        this.terminal = terminal;
        this.menuPlayer = inventory.player;
        // Server side the grid lives on the block so every screen shares it; the
        // client menu has no block entity and only mirrors what the server sends.
        this.crafting = terminal != null
            ? new TerminalCraftingContainer(this, terminal)
            : new TransientCraftingContainer(this, 3, 3);
        // The output must be shared with the block-backed grid as well.  A
        // transient client menu still needs its own mirror container.
        this.craftingResult = terminal != null ? terminal.craftingResult() : new ResultContainer();
        for (int slot = 0; slot < DISPLAY_SLOTS; slot++) {
            identities.add(null);
            addSlot(new Slot(display, slot, 8 + slot % COLUMNS * 18, 32 + slot / COLUMNS * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        addCraftingSlots(inventory.player);
        addPlayerSlots(inventory);
        addSynchronization();
        refresh();
        // Opening a second menu must immediately resolve the output already on
        // the shared grid, rather than waiting for that player to edit a slot.
        slotsChanged(crafting);
    }

    private void addCraftingSlots(Player player) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(crafting, column + row * 3, 181 + column * 18, 32 + row * 18));
            }
        }
        // The result must not share a cell with an input: 184+2*18 / 142+1*18
        // put it exactly on top of the middle-right input slot.
        addSlot(new ResultSlot(player, crafting, craftingResult, 0, 199, 100));
    }

    private void addSynchronization() {
        addDataSlot(new DataSlot() {
            @Override public int get() { return page; }
            @Override public void set(int value) { page = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return totalPages; }
            @Override public void set(int value) { totalPages = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return sortMode.ordinal(); }
            @Override public void set(int value) {
                SortMode[] values = SortMode.values();
                sortMode = values[Math.floorMod(value, values.length)];
            }
        });
        addDataSlot(protectionData(0));
        addDataSlot(protectionData(16));
        addDataSlot(protectionData(32));
        for (int slot = 0; slot < DISPLAY_SLOTS; slot++) {
            for (int part = 0; part < Long.SIZE / Short.SIZE; part++) {
                addDataSlot(countData(slot, part));
            }
        }
    }

    private DataSlot countData(int slot, int part) {
        int shift = part * Short.SIZE;
        return new DataSlot() {
            @Override
            public int get() {
                return (int) (displayCounts[slot] >>> shift & 0xffffL);
            }

            @Override
            public void set(int value) {
                long mask = 0xffffL << shift;
                displayCounts[slot] = displayCounts[slot] & ~mask | (long) (value & 0xffff) << shift;
            }
        };
    }

    private DataSlot protectionData(int offset) {
        return new DataSlot() {
            @Override
            public int get() {
                int mask = 0;
                for (int bit = 0; bit < 16; bit++) {
                    if (protectedSlots.contains(offset + bit)) {
                        mask |= 1 << bit;
                    }
                }
                return mask;
            }

            @Override
            public void set(int value) {
                for (int bit = 0; bit < 16; bit++) {
                    int slot = offset + bit;
                    if ((value & 1 << bit) != 0) {
                        protectedSlots.add(slot);
                    } else {
                        protectedSlots.remove(slot);
                    }
                }
            }
        };
    }

    private void addPlayerSlots(Inventory inventory) {
        int inventoryY = 140;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, inventoryY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, inventoryY + 58));
        }
    }

    public void updateQuery(String updated) {
        query = updated.length() > 80 ? updated.substring(0, 80) : updated;
        page = 0;
        refresh();
    }

    private void refresh() {
        if (terminal == null) {
            return;
        }
        List<IndexedItem> results = terminal.search(query, sortMode);
        totalPages = Math.max(1, (results.size() + DISPLAY_SLOTS - 1) / DISPLAY_SLOTS);
        page = Math.min(page, totalPages - 1);
        int offset = page * DISPLAY_SLOTS;
        for (int slot = 0; slot < DISPLAY_SLOTS; slot++) {
            int resultIndex = offset + slot;
            if (resultIndex >= results.size()) {
                display.setItem(slot, ItemStack.EMPTY);
                displayCounts[slot] = 0;
                identities.set(slot, null);
                continue;
            }
            IndexedItem indexed = results.get(resultIndex);
            ItemStack representative = terminal.representative(indexed.descriptor().identity());
            if (representative.isEmpty()) {
                display.setItem(slot, ItemStack.EMPTY);
                displayCounts[slot] = 0;
                identities.set(slot, null);
                continue;
            }
            representative.setCount(1);
            display.setItem(slot, representative);
            displayCounts[slot] = indexed.totalCount();
            identities.set(slot, indexed.descriptor().identity());
        }
        broadcastChanges();
    }

    @Override
    public void clicked(
        int slotId,
        int button,
        //? if < 26.2 {
        ClickType clickType,
        //?} else {
        /*ContainerInput clickType,
        *///?}
        Player player
    ) {
        if (terminal != null && slotId >= 0 && slotId < DISPLAY_SLOTS) {
            // Vanilla's drag-to-distribute carries its stage in `button`, which
            // withdraw() would misread as a mouse button. A virtual grid has
            // nothing to distribute across, so ignore it outright.
            if (isQuickCraft(clickType)) {
                return;
            }
            // Virtual display slots only implement ordinary pickup and
            // quick-move. In particular, vanilla's double-click PICKUP_ALL
            // must not be interpreted as another withdrawal from this slot.
            if (!isPickup(clickType) && !isQuickMove(clickType)) {
                return;
            }
            // Dropping a held stack onto the grid has to store it. Without this
            // the only way in was a shift-click.
            if (!getCarried().isEmpty() && isPickup(clickType) && (button == 0 || button == 1)) {
                depositCarried(player, button == 1);
                return;
            }
            withdraw(slotId, button, clickType, player);
            return;
        }
        if (terminal != null && slotId >= PLAYER_START && button == 2 && isPickup(clickType)) {
            int inventorySlot = slots.get(slotId).getContainerSlot();
            toggleProtected(inventorySlot);
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void withdraw(
        int displaySlot,
        int button,
        //? if < 26.2 {
        ClickType clickType,
        //?} else {
        /*ContainerInput clickType,
        *///?}
        Player player
    ) {
        ItemIdentity identity = identities.get(displaySlot);
        ItemStack shown = display.getItem(displaySlot);
        if (identity == null || shown.isEmpty() || !terminal.canUse(player)) {
            return;
        }
        if (button == 2 && isPickup(clickType) && player instanceof ServerPlayer serverPlayer) {
            terminal.locateNearest(identity, player).ifPresent(position -> {
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "gui.chestwise.located",
                    position.getX(),
                    position.getY(),
                    position.getZ()
                ), true);
                //? if < 26.2 {
                serverPlayer.serverLevel().sendParticles(
                    serverPlayer,
                    ParticleTypes.END_ROD,
                    true,
                    position.getX() + 0.5,
                    position.getY() + 1.1,
                    position.getZ() + 0.5,
                    18,
                    0.45,
                    0.55,
                    0.45,
                    0.01
                );
                //?} else {
                /*serverPlayer.level().sendParticles(
                    serverPlayer,
                    ParticleTypes.END_ROD,
                    true,
                    true,
                    position.getX() + 0.5,
                    position.getY() + 1.1,
                    position.getZ() + 0.5,
                    18,
                    0.45,
                    0.55,
                    0.45,
                    0.01
                );
                *///?}
            });
            return;
        }
        boolean quickMove = isQuickMove(clickType);
        int requested;
        if (quickMove) {
            requested = shown.getMaxStackSize();
        } else if (button == 1) {
            long visibleStack = Math.min(shown.getMaxStackSize(), displayCounts[displaySlot]);
            requested = Math.max(1, (int) visibleStack / 2);
        } else {
            ItemStack carried = getCarried();
            if (!carried.isEmpty() && !ItemStackIdentity.sameVariant(carried, shown)) {
                return;
            }
            requested = carried.isEmpty() ? shown.getMaxStackSize() : carried.getMaxStackSize() - carried.getCount();
        }
        if (requested <= 0) {
            return;
        }
        ItemStack extracted = terminal.withdraw(identity, requested);
        if (extracted.isEmpty()) {
            refresh();
            return;
        }
        if (quickMove) {
            if (!player.getInventory().add(extracted)) {
                player.drop(extracted, false);
            } else if (!extracted.isEmpty()) {
                player.drop(extracted, false);
            }
        } else if (getCarried().isEmpty()) {
            setCarried(extracted);
        } else {
            getCarried().grow(extracted.getCount());
        }
        refresh();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        if (terminal == null || slotId < CRAFT_START || slotId >= slots.size() || !terminal.canUse(player)) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotId);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (slotId == RESULT_SLOT) {
            ItemStack crafted = slot.getItem();
            ItemStack original = crafted.copy();
            if (!moveItemStackTo(crafted, PLAYER_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(crafted, original);
            if (crafted.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            slot.onTake(player, crafted);
            return original;
        }
        if (slotId >= CRAFT_START && slotId < CRAFT_END) {
            ItemStack original = slot.getItem().copy();
            if (!moveItemStackTo(slot.getItem(), PLAYER_START, slots.size(), false)) {
                return ItemStack.EMPTY;
            }
            if (slot.getItem().isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return original;
        }
        int inventorySlot = slot.getContainerSlot();
        if (!slot.hasItem() || protectedSlots.contains(inventorySlot)) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        terminal.deposit(slot.getItem(), false);
        slot.setChanged();
        refresh();
        return original;
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container changed) {
        if (changed == crafting && !isClientSide(menuPlayer) && menuPlayer instanceof ServerPlayer serverPlayer) {
            ItemStack result = ItemStack.EMPTY;
            //? if <= 1.20.1 {
            java.util.Optional<CraftingRecipe> recipe = menuPlayer.level().getServer().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, crafting, menuPlayer.level());
            if (recipe.isPresent() && craftingResult.setRecipeUsed(menuPlayer.level(), serverPlayer, recipe.get())) {
                ItemStack assembled = recipe.get().assemble(crafting, menuPlayer.level().registryAccess());
                if (assembled.isItemEnabled(menuPlayer.level().enabledFeatures())) {
                    result = assembled;
                }
            }
            //?}
            /*? if > 1.20.1 {*/
            /*net.minecraft.world.item.crafting.CraftingInput input = crafting.asCraftInput();
            java.util.Optional<net.minecraft.world.item.crafting.RecipeHolder<CraftingRecipe>> recipe =
                menuPlayer.level().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, menuPlayer.level());
            if (recipe.isPresent()) {
                craftingResult.setRecipeUsed(recipe.get());
                //? if < 26.2 {
                ItemStack assembled = recipe.get().value().assemble(input, menuPlayer.level().registryAccess());
                //?} else {
                ItemStack assembled = recipe.get().value().assemble(input);
                //?}
                if (assembled.isItemEnabled(menuPlayer.level().enabledFeatures())) {
                    result = assembled;
                }
            }
            *//*?}*/
            craftingResult.setItem(0, result);
            setRemoteSlot(RESULT_SLOT, result);
            serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                containerId,
                incrementStateId(),
                RESULT_SLOT,
                result
            ));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (terminal == null) {
            craftingResult.clearContent();
        }
        if (!isClientSide(player)) {
            // The grid lives on the terminal, so there is nothing to hand back.
            // Only a client-side menu without a block entity owns its own items.
            if (terminal == null) {
                clearContainer(player, crafting);
            }
        }
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftingResult && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (terminal == null || !terminal.canUse(player)) {
            return false;
        }
        if (id >= BUTTON_SCROLL_WITHDRAW_BASE && id < BUTTON_SCROLL_WITHDRAW_BASE + DISPLAY_SLOTS) {
            scrollWithdraw(player, id - BUTTON_SCROLL_WITHDRAW_BASE);
            refresh();
            return true;
        }
        if (id >= BUTTON_SCROLL_DEPOSIT_BASE && id < BUTTON_SCROLL_DEPOSIT_BASE + DISPLAY_SLOTS) {
            scrollDeposit(player, id - BUTTON_SCROLL_DEPOSIT_BASE);
            refresh();
            return true;
        }
        if (id >= BUTTON_PROTECT_BASE && id < BUTTON_PROTECT_BASE + PLAYER_STORAGE_SLOTS) {
            toggleProtected(id - BUTTON_PROTECT_BASE);
            broadcastChanges();
            return true;
        }
        switch (id) {
            case BUTTON_PREVIOUS -> page = Math.max(0, page - 1);
            case BUTTON_NEXT -> page = Math.min(totalPages - 1, page + 1);
            case BUTTON_SORT -> sortMode = SortMode.values()[(sortMode.ordinal() + 1) % SortMode.values().length];
            case BUTTON_DEPOSIT_MATCHING -> depositInventory(player, true);
            case BUTTON_DEPOSIT_ALL -> depositInventory(player, false);
            default -> {
                return false;
            }
        }
        refresh();
        return true;
    }

    private void scrollWithdraw(Player player, int displaySlot) {
        ItemIdentity identity = identities.get(displaySlot);
        if (identity == null) {
            return;
        }
        ItemStack extracted = terminal.withdraw(identity, 1);
        if (!extracted.isEmpty() && !player.getInventory().add(extracted)) {
            player.drop(extracted, false);
        }
    }

    /** Stores the stack on the cursor: the whole thing, or one item at a time. */
    private void depositCarried(Player player, boolean single) {
        ItemStack carried = getCarried();
        if (carried.isEmpty() || !terminal.canUse(player)) {
            return;
        }
        if (single) {
            ItemStack one = carried.copy();
            one.setCount(1);
            terminal.deposit(one, false);
            if (one.isEmpty()) {
                carried.shrink(1);
                setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
            }
        } else {
            terminal.deposit(carried, false);
            setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
        }
        refresh();
    }

    private void scrollDeposit(Player player, int displaySlot) {
        ItemIdentity identity = identities.get(displaySlot);
        ItemStack shown = display.getItem(displaySlot);
        if (identity == null || shown.isEmpty()) {
            return;
        }
        Inventory inventory = player.getInventory();
        for (int inventorySlot = 0;
             inventorySlot < Math.min(PLAYER_STORAGE_SLOTS, inventory.getContainerSize());
             inventorySlot++) {
            if (protectedSlots.contains(inventorySlot)) {
                continue;
            }
            ItemStack candidate = inventory.getItem(inventorySlot);
            if (candidate.isEmpty() || !ItemStackIdentity.sameVariant(candidate, shown)) {
                continue;
            }
            ItemStack single = candidate.split(1);
            terminal.deposit(single, true);
            if (!single.isEmpty()) {
                candidate.grow(single.getCount());
            }
            inventory.setChanged();
            return;
        }
    }

    /**
     * Fills the crafting grid for a recipe, taking from the player's inventory
     * first and only then from the surrounding containers.
     *
     * <p>{@code encoded} is slot-major: nine {@code ';'}-separated groups, each a
     * {@code ','}-separated list of item ids the slot accepts. Sending the
     * acceptable ids rather than a recipe id keeps this free of the recipe API,
     * which is shaped differently on every Minecraft generation, and still lets a
     * tag ingredient be satisfied by any member the player actually owns.
     */
    public void fillRecipe(Player player, String encoded) {
        if (terminal == null || isClientSide(player) || !terminal.canUse(player)) {
            return;
        }
        List<List<String>> wanted = RecipeSlotCodec.decode(encoded);
        Map<String, Long> available = new LinkedHashMap<>(terminal.availableItemCounts());
        Map<String, Long> playerAvailable = new LinkedHashMap<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Math.min(PLAYER_STORAGE_SLOTS, inventory.getContainerSize()); slot++) {
            if (!protectedSlots.contains(slot)) {
                ItemStack stack = inventory.getItem(slot);
                addAvailable(available, stack);
                addAvailable(playerAvailable, stack);
            }
        }
        for (int slot = 0; slot < crafting.getContainerSize(); slot++) {
            addAvailable(available, crafting.getItem(slot));
        }
        List<String> plan = RecipeSlotPlanner.plan(wanted, available, playerAvailable).orElse(null);
        if (plan == null) {
            return;
        }
        // Clear first so transferring twice cannot pile ingredients up.
        // Never overwrite a grid stack that could not be returned.  Keeping the
        // existing grid intact is preferable to a partial recipe and, crucially,
        // makes a full or incompatible storage network lossless.
        if (!returnGridToStorage()) {
            return;
        }
        for (int slot = 0; slot < RECIPE_SLOTS && slot < plan.size(); slot++) {
            String itemId = plan.get(slot);
            if (itemId.isEmpty()) {
                continue;
            }
            ItemStack found = takeOneFromInventory(player, List.of(itemId));
            if (found.isEmpty()) {
                found = terminal.withdrawAnyOf(List.of(itemId), 1);
            }
            if (!found.isEmpty()) {
                crafting.setItem(slot, found);
            }
        }
        player.getInventory().setChanged();
        slotsChanged(crafting);
        refresh();
    }

    private static void addAvailable(Map<String, Long> available, ItemStack stack) {
        if (!stack.isEmpty()) {
            String itemId = ItemStackIdentity.identity(stack).itemId();
            available.merge(itemId, (long) stack.getCount(), Long::sum);
        }
    }

    /** Moves anything already on the grid back into storage, without discarding a remainder. */
    private boolean returnGridToStorage() {
        for (int slot = 0; slot < crafting.getContainerSize(); slot++) {
            ItemStack existing = crafting.getItem(slot);
            if (existing.isEmpty()) {
                continue;
            }
            terminal.deposit(existing, false);
            crafting.setItem(slot, existing.isEmpty() ? ItemStack.EMPTY : existing);
            if (!existing.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private ItemStack takeOneFromInventory(Player player, List<String> options) {
        Inventory inventory = player.getInventory();
        int limit = Math.min(PLAYER_STORAGE_SLOTS, inventory.getContainerSize());
        for (int slot = 0; slot < limit; slot++) {
            if (protectedSlots.contains(slot)) {
                continue;
            }
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.isEmpty()) {
                continue;
            }
            if (options.contains(ItemStackIdentity.identity(candidate).itemId())) {
                return candidate.split(1);
            }
        }
        return ItemStack.EMPTY;
    }

    private void depositInventory(Player player, boolean matchingOnly) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Math.min(PLAYER_STORAGE_SLOTS, inventory.getContainerSize()); slot++) {
            if (protectedSlots.contains(slot)) {
                continue;
            }
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) {
                terminal.deposit(stack, matchingOnly);
            }
        }
        inventory.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return terminal == null || terminal.canUse(player);
    }

    public int page() {
        return page;
    }

    public int totalPages() {
        return totalPages;
    }

    public SortMode sortMode() {
        return sortMode;
    }

    public boolean isProtectedInventorySlot(int inventorySlot) {
        return protectedSlots.contains(inventorySlot);
    }

    private void toggleProtected(int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= PLAYER_STORAGE_SLOTS) {
            return;
        }
        if (!protectedSlots.add(inventorySlot)) {
            protectedSlots.remove(inventorySlot);
        }
    }

    public long displayCount(int displaySlot) {
        return displaySlot >= 0 && displaySlot < DISPLAY_SLOTS ? displayCounts[displaySlot] : 0;
    }

    private static boolean isClientSide(Player player) {
        //? if < 26.2 {
        return player.level().isClientSide;
        //?} else {
        /*return player.level().isClientSide();
        *///?}
    }

    private static boolean isPickup(
        //? if < 26.2 {
        ClickType input
        //?} else {
        /*ContainerInput input
        *///?}
    ) {
        //? if < 26.2 {
        return input == ClickType.PICKUP;
        //?} else {
        /*return input == ContainerInput.PICKUP;
        *///?}
    }

    private static boolean isQuickCraft(
        //? if < 26.2 {
        ClickType input
        //?} else {
        /*ContainerInput input
        *///?}
    ) {
        //? if < 26.2 {
        return input == ClickType.QUICK_CRAFT;
        //?} else {
        /*return input == ContainerInput.QUICK_CRAFT;
        *///?}
    }

    private static boolean isQuickMove(
        //? if < 26.2 {
        ClickType input
        //?} else {
        /*ContainerInput input
        *///?}
    ) {
        //? if < 26.2 {
        return input == ClickType.QUICK_MOVE;
        //?} else {
        /*return input == ContainerInput.QUICK_MOVE;
        *///?}
    }
}
