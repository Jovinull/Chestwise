package dev.chestwise.compat.jei;

import dev.chestwise.minecraft.ChestwiseContent;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
//? if < 26.2 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;
*///?}

/**
 * Adds the "move ingredients" button to the Storage Terminal's crafting grid.
 *
 * <p>Chestwise never requires JEI: the API is a compileOnly dependency and this
 * class is only ever loaded because JEI itself scans for {@link JeiPlugin}. With
 * JEI absent, nothing here is referenced.
 */
@JeiPlugin
public final class ChestwiseJeiPlugin implements IModPlugin {
    @Override
    //? if < 26.2 {
    public ResourceLocation getPluginUid() {
    //?} else {
    /*public Identifier getPluginUid() {
    *///?}
        return ChestwiseContent.id("jei");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
            (mezz.jei.api.recipe.transfer.IRecipeTransferHandler) new StorageTerminalTransferHandler(),
            RecipeTypes.CRAFTING
        );
    }
}
