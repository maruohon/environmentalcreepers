package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.environmentalcreepers.Reference;

public class Configs
{
    public static boolean disableCreeperExplosionBlockDamage;
    public static boolean disableOtherExplosionBlockDamage;
    public static boolean disableCreeperExplosionItemDamage;
    public static boolean disableOtherExplosionItemDamage;
    public static boolean modifyCreeperExplosionDropChance;
    public static boolean modifyOtherExplosionDropChance;
    public static double creeperExplosionBlockDropChance;
    public static double otherExplosionBlockDropChance;

    public static File configurationFile;
    public static Configuration config;
    
    public static final String CATEGORY_GENERIC = "Generic";
    public static final String CATEGORY_INFO_TOGGLE = "InfoTypes";

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.getModID()) == true)
        {
            loadConfigs(config);
        }
    }

    public static void loadConfigsFromFile(File configFile)
    {
        configurationFile = configFile;
        config = new Configuration(configFile, null, true);
        config.load();

        loadConfigs(config);
    }

    public static void loadConfigs(Configuration conf)
    {
        Property prop;

        prop = conf.get(CATEGORY_GENERIC, "disableCreeperExplosionBlockDamage", false);
        prop.setComment("Completely disable Creeper explosion from damaging blocks");
        disableCreeperExplosionBlockDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "disableOtherExplosionBlockDamage", false);
        prop.setComment("Completely disable other explosions than Creepers from damaging blocks");
        disableOtherExplosionBlockDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "disableCreeperExplosionItemDamage", false);
        prop.setComment("Disable Creeper explosions from damaging items on the ground");
        disableCreeperExplosionItemDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "disableOtherExplosionItemDamage", false);
        prop.setComment("Disable other explosions than Creepers from damaging items on the ground");
        disableOtherExplosionItemDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "modifyCreeperExplosionDropChance", true);
        prop.setComment("Modify the chance of Creeper explosions to drop the blocks as items. Set the chance in creeperExplosionBlockDropChance.");
        modifyCreeperExplosionDropChance = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "modifyOtherExplosionDropChance", true);
        prop.setComment("Modify the chance of other explosions than Creepers to drop the blocks as items. Set the chance in otherExplosionBlockDropChance.");
        modifyOtherExplosionDropChance = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "creeperExplosionBlockDropChance", 1.0);
        prop.setComment("The chance of Creeper explosions to drop the blocks as items. Set to 1.0 to always drop.");
        creeperExplosionBlockDropChance = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "otherExplosionBlockDropChance", 1.0);
        prop.setComment("The chance of other explosions than Creepers to drop the blocks as items. Set to 1.0 to always drop.");
        otherExplosionBlockDropChance = prop.getDouble();

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }
}
