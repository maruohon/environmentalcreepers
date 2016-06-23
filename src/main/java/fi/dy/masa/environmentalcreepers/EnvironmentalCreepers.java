package fi.dy.masa.environmentalcreepers;

import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.event.ExplosionEventHandler;
import fi.dy.masa.environmentalcreepers.proxy.ServerProxy;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
    guiFactory = "fi.dy.masa.environmentalcreepers.config.EnvironmentalCreepersGuiFactory",
    updateJSON = "https://raw.githubusercontent.com/maruohon/environmentalcreepers/master/update.json",
    acceptableRemoteVersions = "*", acceptedMinecraftVersions = "[1.9.4,1.10]")
public class EnvironmentalCreepers
{
    @Instance(Reference.MOD_ID)
    public static EnvironmentalCreepers instance;

    @SidedProxy(clientSide = "fi.dy.masa.environmentalcreepers.proxy.ClientProxy", serverSide = "fi.dy.masa.environmentalcreepers.proxy.ServerProxy")
    public static ServerProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        Configs.loadConfigsFromFile(event.getSuggestedConfigurationFile());

        MinecraftForge.EVENT_BUS.register(new ExplosionEventHandler());
        proxy.registerEventHandlers();
    }
}
