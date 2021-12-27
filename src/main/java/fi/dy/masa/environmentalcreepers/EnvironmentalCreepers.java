package fi.dy.masa.environmentalcreepers;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;
import net.minecraftforge.fmlserverevents.FMLServerStoppingEvent;
import fi.dy.masa.environmentalcreepers.commands.CommandReloadConfig;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.event.ExplosionEventHandler;

@Mod(Reference.MOD_ID)
public class EnvironmentalCreepers
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    public EnvironmentalCreepers()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.COMMON_CONFIG, Reference.MOD_ID + ".toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

        // Make sure the mod being absent on the other network side does not cause
        // the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "", (incoming, isNetwork) -> true));

        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);

        Configs.loadConfig(FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + ".toml"));
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        Configs.setGlobalConfigDirAndLoadConfigs(FMLPaths.CONFIGDIR.get().toFile());
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent event)
    {
        File dataDir = event.getServer().getWorldPath(new LevelResource(Reference.MOD_ID)).toFile();
        Configs.loadConfigsFromPerWorldConfigIfExists(dataDir);
    }

    private void onRegisterCommands(final RegisterCommandsEvent event)
    {
        CommandReloadConfig.register(event.getDispatcher());
    }

    private void serverStopping(final FMLServerStoppingEvent event)
    {
        // (Re-)read the global configs after closing a world
        Configs.loadConfigsFromGlobalConfigFile();
    }

    public static void logInfo(String message, Object ... params)
    {
        if (Configs.Generic.verboseLogging)
        {
            logger.info(message, params);
        }
    }
}
