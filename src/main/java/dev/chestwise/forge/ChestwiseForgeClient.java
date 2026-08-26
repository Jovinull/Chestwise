package dev.chestwise.forge;

//? if forge || (neoforge && <= 1.20.1) {
/*import dev.chestwise.Chestwise;
import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.client.StorageTerminalScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Chestwise.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ChestwiseForgeClient {
    private ChestwiseForgeClient() {
    }

    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ChestwiseContent.STORAGE_TERMINAL_MENU, StorageTerminalScreen::new));
    }
}
*///?}
