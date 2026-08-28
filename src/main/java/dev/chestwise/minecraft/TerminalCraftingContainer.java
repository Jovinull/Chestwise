package dev.chestwise.minecraft;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
//? if < 26.2 {
import net.minecraft.world.entity.player.StackedContents;
//?} else {
/*import net.minecraft.world.entity.player.StackedItemContents;
*///?}

/**
 * A 3x3 crafting grid whose stacks live on the terminal block entity rather than
 * in the menu.
 *
 * <p>Every screen opened on the same terminal shares this one list. The matching
 * result container is also block-owned, so menu slot diffing can never expose a
 * stale craft result to a second player.
 */
final class TerminalCraftingContainer implements CraftingContainer {
    private final AbstractContainerMenu menu;
    private final StorageTerminalBlockEntity terminal;

    TerminalCraftingContainer(AbstractContainerMenu menu, StorageTerminalBlockEntity terminal) {
        this.menu = menu;
        this.terminal = terminal;
    }

    private NonNullList<ItemStack> items() {
        return terminal.craftingGrid();
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public List<ItemStack> getItems() {
        return items();
    }

    @Override
    public int getContainerSize() {
        return items().size();
    }

    @Override
    public boolean isEmpty() {
        return items().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        NonNullList<ItemStack> items = items();
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        NonNullList<ItemStack> items = items();
        if (slot < 0 || slot >= items.size() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = existing.split(amount);
        if (existing.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        if (!removed.isEmpty()) {
            setChanged();
            menu.slotsChanged(this);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        NonNullList<ItemStack> items = items();
        if (slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return existing;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        NonNullList<ItemStack> items = items();
        if (slot < 0 || slot >= items.size()) {
            return;
        }
        items.set(slot, stack);
        setChanged();
        menu.slotsChanged(this);
    }

    @Override
    public void setChanged() {
        terminal.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return terminal.canUse(player);
    }

    @Override
    public void clearContent() {
        items().clear();
        setChanged();
    }

    @Override
    //? if < 26.2 {
    public void fillStackedContents(StackedContents contents) {
    //?} else {
    /*public void fillStackedContents(StackedItemContents contents) {
    *///?}
        for (ItemStack stack : items()) {
            contents.accountSimpleStack(stack);
        }
    }
}
