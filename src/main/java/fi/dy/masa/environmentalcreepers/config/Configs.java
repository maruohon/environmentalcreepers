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
    public static boolean disableCreeperExplosionItemDamage;
    public static boolean disableOtherExplosionBlockDamage;
    public static boolean disableOtherExplosionItemDamage;
    public static boolean enableCreeperExplosionChainReaction;
    public static boolean modifyCreeperExplosionDropChance;
    public static boolean modifyCreeperExplosionStrength;
    public static boolean modifyOtherExplosionDropChance;
    public static double creeperChainReactionChance;
    public static double creeperChainReactionMaxDistance;
    public static double creeperExplosionBlockDropChance;
    public static double creeperExplosionStrengthNormal;
    public static double creeperExplosionStrengthCharged;
    public static double otherExplosionBlockDropChance;
    public static boolean verboseLogging;

    public static File configurationFile;
    public static Configuration config;
    
    public static final String CATEGORY_GENERIC = "Generic";

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
        config = new Configuration(configFile, null, false);
        config.load();

        loadConfigs(config);
    }

    public static void loadConfigs(Configuration conf)
    {
        Property prop;

        prop = conf.get(CATEGORY_GENERIC, "creeperChainReactionChance", 1.0);
        prop.setComment("The chance of Creeper explosions to cause other Creepers to trigger within range. Set to 1.0 to always trigger.");
        creeperChainReactionChance = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "creeperChainReactionMaxDistance", 16.0);
        prop.setComment("The maximum distance within which a Creeper exploding will cause a chain reaction.");
        creeperChainReactionMaxDistance = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "creeperExplosionBlockDropChance", 1.0);
        prop.setComment("The chance of Creeper explosions to drop the blocks as items. Set to 1.0 to always drop.");
        creeperExplosionBlockDropChance = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "creeperExplosionStrengthNormal", 3.0);
        prop.setComment("The strength of Creeper explosions. Default in vanilla: 3.0 for normal Creepers (becomes double ie. 6.0 for Charged Creepers).");
        creeperExplosionStrengthNormal = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "creeperExplosionStrengthCharged", 6.0);
        prop.setComment("The strength of Charged Creeper explosions. Default in vanilla: 6.0 (double of normal Creepers).");
        creeperExplosionStrengthCharged = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "disableCreeperExplosionBlockDamage", false);
        prop.setComment("Completely disable Creeper explosion from damaging blocks");
        disableCreeperExplosionBlockDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "disableCreeperExplosionItemDamage", false);
        prop.setComment("Disable Creeper explosions from damaging items on the ground");
        disableCreeperExplosionItemDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "disableOtherExplosionBlockDamage", false);
        prop.setComment("Completely disable other explosions than Creepers from damaging blocks");
        disableOtherExplosionBlockDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "disableOtherExplosionItemDamage", false);
        prop.setComment("Disable other explosions than Creepers from damaging items on the ground");
        disableOtherExplosionItemDamage = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "enableCreeperExplosionChainReaction", false);
        prop.setComment("When enabled, a Creeper exploding has a chance to trigger other nearby Creepers.");
        enableCreeperExplosionChainReaction = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "modifyCreeperExplosionDropChance", true);
        prop.setComment("Modify the chance of Creeper explosions to drop the blocks as items. Set the chance in creeperExplosionBlockDropChance.");
        modifyCreeperExplosionDropChance = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "modifyCreeperExplosionStrength", false);
        prop.setComment("Modify the strength of Creeper explosions.");
        modifyCreeperExplosionStrength = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "modifyOtherExplosionDropChance", true);
        prop.setComment("Modify the chance of other explosions than Creepers to drop the blocks as items. Set the chance in otherExplosionBlockDropChance.");
        modifyOtherExplosionDropChance = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "otherExplosionBlockDropChance", 1.0);
        prop.setComment("The chance of other explosions than Creepers to drop the blocks as items. Set to 1.0 to always drop.");
        otherExplosionBlockDropChance = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "verboseLogging", false);
        prop.setComment("Log some messages on each explosion, for debugging purposes. Leave disabled for normal use.");
        verboseLogging = prop.getBoolean();

        if (conf.hasChanged())
        {
            conf.save();
        }
    }
}
