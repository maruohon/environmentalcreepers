package fi.dy.masa.environmentalcreepers;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.environmentalcreepers.commands.CommandReloadConfig;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.event.ExplosionEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(Reference.MOD_ID)
public class EnvironmentalCreepers
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    public EnvironmentalCreepers()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configs.COMMON_CONFIG, Reference.MOD_ID + ".toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onFingerPrintViolation);

        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);

        Configs.loadConfig(FMLPaths.CONFIGDIR.get().resolve(Reference.MOD_ID + ".toml"));
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        Configs.setGlobalConfigDirAndLoadConfigs(FMLPaths.CONFIGDIR.get().toFile());
    }

    private void onFingerPrintViolation(final FMLFingerprintViolationEvent event)
    {
        // Not running in a dev environment
        if (event.isDirectory() == false)
        {
            logger.warn("*********************************************************************************************");
            logger.warn("*****                                    WARNING                                        *****");
            logger.warn("*****                                                                                   *****");
            logger.warn("*****   The signature of the mod file '{}' does not match the expected fingerprint!     *****", event.getSource().getName());
            logger.warn("*****   This might mean that the mod file has been tampered with!                       *****");
            logger.warn("*****   If you did not download the mod {} directly from Curse/CurseForge,       *****", Reference.MOD_NAME);
            logger.warn("*****   or using one of the well known launchers, and you did not                       *****");
            logger.warn("*****   modify the mod file at all yourself, then it's possible,                        *****");
            logger.warn("*****   that it may contain malware or other unwanted things!                           *****");
            logger.warn("*********************************************************************************************");
        }
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent event)
    {
        File dataDir = event.getServer().getActiveAnvilConverter().getFile(event.getServer().getFolderName(), Reference.MOD_ID);
        Configs.loadConfigsFromPerWorldConfigIfExists(dataDir);
    }

    private void onServerStarting(final FMLServerStartingEvent event)
    {
        CommandReloadConfig.register(event.getCommandDispatcher());
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
