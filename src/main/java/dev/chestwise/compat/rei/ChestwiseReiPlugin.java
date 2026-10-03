//? if fabric || neoforge || <= 1.20.1 {
package dev.chestwise.compat.rei;

import dev.chestwise.core.RecipeSlotCodec;
import dev.chestwise.minecraft.ItemStackIdentity;
import dev.chestwise.minecraft.StorageTerminalMenu;
import dev.chestwise.platform.ChestwiseNetworking;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.world.item.ItemStack;

/**
 * Recipe transfer for Roughly Enough Items.
 *
 * <p>EMI needs no counterpart: it bundles JEMI, whose {@code JemiRecipeHandler}
 * wraps a JEI {@code IRecipeTransferHandler} and synthesises the slot view our
 * JEI plugin reads, so the JEI plugin already serves EMI. REI ships no such
 * bridge, hence this class.
 *
 * <p>Chestwise currently compiles this plugin for Forge only on 1.20.1. The
 * later Forge nodes deliberately guard it out and omit the API dependency.
 */
//? if !fabric {
/*@me.shedaniel.rei.forge.REIPluginClient
*///?}
public final class ChestwiseReiPlugin implements REIClientPlugin {
    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(new StorageTerminalTransfer());
    }

    /** Mirrors the JEI handler: send the acceptable ids, let the server source them. */
    private static final class StorageTerminalTransfer implements TransferHandler {
        @Override
        public Result handle(Context context) {
            if (!(context.getMenu() instanceof StorageTerminalMenu)) {
                return Result.createNotApplicable();
            }
            List<EntryIngredient> inputs = context.getDisplay().getInputEntries();
            if (inputs.isEmpty() || inputs.size() > StorageTerminalMenu.RECIPE_SLOTS) {
                return Result.createNotApplicable();
            }
            if (context.isActuallyCrafting()) {
                ChestwiseNetworking.sendRecipe(encode(inputs));
            }
            // Only the server can see the surrounding containers, so refusing here
            // would reject recipes the storage is perfectly able to fill.
            return Result.createSuccessful();
        }

        private static String encode(List<EntryIngredient> inputs) {
            List<List<String>> slots = new ArrayList<>();
            for (EntryIngredient ingredient : inputs) {
                List<String> ids = new ArrayList<>();
                for (EntryStack<?> entry : ingredient) {
                    if (!entry.isEmpty() && entry.getValue() instanceof ItemStack stack && !stack.isEmpty()) {
                        ids.add(ItemStackIdentity.identity(stack).itemId());
                    }
                }
                slots.add(ids);
            }
            return RecipeSlotCodec.encode(slots);
        }
    }
}
//?}
