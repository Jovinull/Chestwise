package dev.chestwise.compat.jei;

import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.core.RecipeSlotCodec;
import dev.chestwise.minecraft.ItemStackIdentity;
import dev.chestwise.minecraft.StorageTerminalMenu;
import dev.chestwise.platform.ChestwiseNetworking;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * Lays a JEI recipe onto the terminal's crafting grid, pulling from the player's
 * inventory first and from the surrounding containers after that.
 *
 * <p>The handler deliberately reads its ingredients from {@link IRecipeSlotsView}
 * rather than from the recipe object. JEI's crafting recipe type is shaped
 * differently on every generation it supports (a bare recipe on 1.20.1, a
 * {@code RecipeHolder} later, reached through a renamed type interface on 26.2),
 * whereas the slot view and {@link RecipeIngredientRole} have been identical
 * throughout. Sending the acceptable item ids also means a slot that accepts a
 * tag is satisfied by whichever member the player actually owns, instead of only
 * the variant JEI happened to display. The recipe argument is intentionally
 * {@code Object}: the handler never reads it, and JEMI supplies an EMI recipe
 * object when an EMI-native recipe has no vanilla raw recipe.
 */
public final class StorageTerminalTransferHandler
    implements IRecipeTransferHandler<StorageTerminalMenu, Object> {

    @Override
    public Class<? extends StorageTerminalMenu> getContainerClass() {
        return StorageTerminalMenu.class;
    }

    @Override
    public Optional<MenuType<StorageTerminalMenu>> getMenuType() {
        return Optional.ofNullable(ChestwiseContent.STORAGE_TERMINAL_MENU);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    //? if <= 1.20.1 {
    public mezz.jei.api.recipe.RecipeType<Object> getRecipeType() {
    //?}
    /*? if > 1.20.1 && < 26.2 {*/
    /*public mezz.jei.api.recipe.RecipeType<Object> getRecipeType() {
    *//*?}*/
    /*? if >= 26.2 {*/
    /*public mezz.jei.api.recipe.types.IRecipeType<Object> getRecipeType() {
    *//*?}*/
        //? if < 26.2 {
        return (mezz.jei.api.recipe.RecipeType) RecipeTypes.CRAFTING;
        //?} else {
        /*return (mezz.jei.api.recipe.types.IRecipeType) RecipeTypes.CRAFTING;
        *///?}
    }

    @Override
    public IRecipeTransferError transferRecipe(
        StorageTerminalMenu menu,
        Object recipe,
        IRecipeSlotsView slotsView,
        Player player,
        boolean maxTransfer,
        boolean doTransfer
    ) {
        List<IRecipeSlotView> inputs = slotsView.getSlotViews(RecipeIngredientRole.INPUT);
        if (inputs.isEmpty() || inputs.size() > StorageTerminalMenu.RECIPE_SLOTS) {
            return null;
        }
        if (doTransfer) {
            ChestwiseNetworking.sendRecipe(encode(inputs));
        }
        // The server owns the decision: only it can see the surrounding
        // containers, so refusing here would reject recipes the storage can fill.
        return null;
    }

    private static String encode(List<IRecipeSlotView> inputs) {
        List<List<String>> slots = new ArrayList<>();
        for (IRecipeSlotView view : inputs) {
            List<String> ids = new ArrayList<>();
            for (ItemStack stack : view.getItemStacks().toList()) {
                if (!stack.isEmpty()) {
                    ids.add(ItemStackIdentity.identity(stack).itemId());
                }
            }
            slots.add(ids);
        }
        return RecipeSlotCodec.encode(slots);
    }
}
