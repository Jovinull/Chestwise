/*? if fabric {*/
package dev.chestwise.fabric;

import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.client.StorageTerminalScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class ChestwiseFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ChestwiseContent.STORAGE_TERMINAL_MENU, StorageTerminalScreen::new);
    }
}
/*?}*/
