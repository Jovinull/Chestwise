package dev.chestwise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.ChestwiseConfig;
import dev.chestwise.platform.ChestwiseNetworking;
import java.nio.file.Path;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
//?}

//? if forge {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?}

//? if neoforge && > 1.20.1 {
/*import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.IEventBus;
*///?}

//? if neoforge && <= 1.20.1 {
/*import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
*///?}

//? if forgeLike {
/*@Mod(Chestwise.MOD_ID)
public final class Chestwise {
*///?}

//? if fabric {
public final class Chestwise implements ModInitializer {
//?}
    public static final String MOD_ID = "chestwise";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //? if forge || (neoforge && <= 1.20.1) {
    /*public Chestwise() {
        initialize();
    }
    *///?}

    //? if neoforge && > 1.20.1 {
    /*public Chestwise(IEventBus modBus) {
        initializeNeoForge(modBus);
    }
    *///?}

    //? if fabric {
    @Override
    public void onInitialize() {
        initialize();
    }
    //?}

    @SuppressWarnings({"deprecation", "removal"})
    private static void initialize() {
        //? if fabric {
        Path configDirectory = FabricLoader.getInstance().getConfigDir();
        //?}
        /*? if forge || (neoforge && <= 1.20.1) {*/
        /*Path configDirectory = FMLPaths.CONFIGDIR.get();
        *//*?}*/
        /*? if neoforge && > 1.20.1 {*/
        /*Path configDirectory = FMLPaths.CONFIGDIR.get();
        *//*?}*/
        ChestwiseContent.configure(ChestwiseConfig.load(configDirectory.resolve("chestwise-server.properties")));
        //? if fabric {
        ChestwiseContent.registerFabric();
        //?}
        /*? if forge || (neoforge && <= 1.20.1) {*/
        /*ChestwiseContent.registerForge(FMLJavaModLoadingContext.get().getModEventBus());
        *//*?}*/
        ChestwiseNetworking.registerServer();
        LOGGER.info("Chestwise {} initializing", BuildInfo.VERSION);
    }

    //? if neoforge && > 1.20.1 {
    /*private static void initializeNeoForge(IEventBus modBus) {
        Path configDirectory = FMLPaths.CONFIGDIR.get();
        ChestwiseContent.configure(ChestwiseConfig.load(configDirectory.resolve("chestwise-server.properties")));
        ChestwiseContent.registerNeoForge(modBus);
        ChestwiseNetworking.registerNeoForge(modBus);
        LOGGER.info("Chestwise {} initializing", BuildInfo.VERSION);
    }
    *///?}
}
