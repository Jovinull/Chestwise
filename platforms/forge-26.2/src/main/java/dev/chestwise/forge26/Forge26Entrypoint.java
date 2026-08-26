package dev.chestwise.forge26;

import dev.chestwise.BuildInfo;
import dev.chestwise.minecraft.ChestwiseConfig;
import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.platform.ChestwiseNetworking;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Forge26Entrypoint.MOD_ID)
public final class Forge26Entrypoint {
    public static final String MOD_ID = "chestwise";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Forge26Entrypoint(FMLJavaModLoadingContext context) {
        ChestwiseContent.configure(ChestwiseConfig.load(FMLPaths.CONFIGDIR.get().resolve("chestwise-server.properties")));
        ChestwiseContent.registerForge(context.getModBusGroup());
        ChestwiseNetworking.registerServer();
        LOGGER.info("Chestwise {} initializing on Forge 26.2", BuildInfo.VERSION);
    }
}
