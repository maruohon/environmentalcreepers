package fi.dy.masa.environmentalcreepers;

import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.event.ExplosionEventHandler;
import fi.dy.masa.environmentalcreepers.proxy.ServerProxy;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, certificateFingerprint = Reference.FINGERPRINT,
    guiFactory = "fi.dy.masa.environmentalcreepers.config.EnvironmentalCreepersGuiFactory",
    updateJSON = "https://raw.githubusercontent.com/maruohon/environmentalcreepers/master/update.json",
    acceptableRemoteVersions = "*", acceptedMinecraftVersions = "1.12")
public class EnvironmentalCreepers
{
    @Mod.Instance(Reference.MOD_ID)
    public static EnvironmentalCreepers instance;

    @SidedProxy(clientSide = "fi.dy.masa.environmentalcreepers.proxy.ClientProxy", serverSide = "fi.dy.masa.environmentalcreepers.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        Configs.setGlobalConfigDirAndLoadConfigs(event.getModConfigurationDirectory());

        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        proxy.registerEventHandlers();
    }

    @Mod.EventHandler
    public void onServerAboutToStart(FMLServerAboutToStartEvent event)
    {
        File worldDir = new File(((AnvilSaveConverter) event.getServer().getActiveAnvilConverter()).savesDirectory, event.getServer().getFolderName());
        Configs.loadConfigsFromPerWorldConfigIfExists(worldDir);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        // (Re-)read the global configs after closing a world
        Configs.loadConfigsFromGlobalConfigFile();
    }

    public static void logInfo(String message, Object ... params)
    {
        if (Configs.verboseLogging)
        {
            logger.info(message, params);
        }
    }

    @Mod.EventHandler
    public void onFingerPrintViolation(FMLFingerprintViolationEvent event)
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
}
