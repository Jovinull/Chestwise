package dev.chestwise.neoforge;

//? if neoforge && > 1.20.1 {
/*import dev.chestwise.Chestwise;
import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.client.StorageTerminalScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Chestwise.MOD_ID, value = Dist.CLIENT)
public final class ChestwiseNeoForgeClient {
    private ChestwiseNeoForgeClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ChestwiseContent.STORAGE_TERMINAL_MENU, StorageTerminalScreen::new);
    }
}
*///?}
