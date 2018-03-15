package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.world.Explosion;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.Reference;
import fi.dy.masa.environmentalcreepers.event.CreeperEventHandler;

public class Configs
{
    private static boolean copyConfigToWorld;
    private static boolean usePerWorldConfig;

    public static boolean disableCreeperExplosionBlockDamage;
    public static boolean disableCreeperExplosionCompletely;
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
    public static boolean listIsWhitelist;
    public static boolean verboseLogging;
    private static String[] explosionBlacklistClassNames;
    private static String[] explosionWhitelistClassNames;
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_BLACKLIST = new HashSet<Class<? extends Explosion>>();
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_WHITELIST = new HashSet<Class<? extends Explosion>>();

    private static File configFileGlobal;
    private static Configuration config;
    
    public static final String CATEGORY_GENERIC = "Generic";
    public static final String CATEGORY_LISTS = "Lists";

    public static Configuration getConfig()
    {
        return config;
    }

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.getModID()))
        {
            loadConfigs(config);
        }
    }

    public static void setGlobalConfigDirAndLoadConfigs(File configDirCommon)
    {
        File configFile = new File(configDirCommon, Reference.MOD_ID + ".cfg");
        configFileGlobal = configFile;

        loadConfigsFromGlobalConfigFile();
    }

    public static void loadConfigsFromPerWorldConfigIfExists(@Nullable File worldDir)
    {
        if (worldDir != null)
        {
            File configDir = new File(new File(worldDir, "data"), Reference.MOD_ID);
            File configFile = new File(configDir, Reference.MOD_ID + ".cfg");

            if (copyConfigToWorld)
            {
                ConfigFileUtils.createDirIfNotExists(configDir);
                ConfigFileUtils.tryCopyConfigIfMissing(configFile, configFileGlobal);
            }

            if (usePerWorldConfig && configFile.exists() && configFile.isFile() && configFile.canRead())
            {
                loadConfigsFromFile(configFile);
                return;
            }
        }

        // Fall-back
        loadConfigsFromGlobalConfigFile();
    }

    public static void loadConfigsFromGlobalConfigFile()
    {
        loadConfigsFromFile(configFileGlobal);
    }

    private static void loadConfigsFromFile(File configFile)
    {
        config = new Configuration(configFile, null, true);

        if (config != null)
        {
            EnvironmentalCreepers.logInfo("Reloading the configs from file '{}'", config.getConfigFile().getAbsolutePath());
            config.load();
            loadConfigs(config);
        }
    }

    private static void loadConfigs(Configuration conf)
    {
        Property prop;

        prop = conf.get(CATEGORY_GENERIC, "copyConfigToWorld", true);
        prop.setComment("If true, then the global config file is copied to the world\n (in data/environmentalcreepers/environmentalcreepers.cfg), if one doesn't exist there yet.");
        copyConfigToWorld = prop.getBoolean();

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

        prop = conf.get(CATEGORY_GENERIC, "disableCreeperExplosionCompletely", false);
        prop.setComment("Completely disable Creepers from exploding");
        disableCreeperExplosionCompletely = prop.getBoolean();

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

        prop = conf.get(CATEGORY_GENERIC, "usePerWorldConfig", true);
        prop.setComment("If true, then configs are attempted to be read from a config inside the world\n(in data/environmentalcreepers/environmentalcreepers.cfg), if one exists there.");
        usePerWorldConfig = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "verboseLogging", false);
        prop.setComment("Log some messages on each explosion, for debugging purposes. Leave disabled for normal use.");
        verboseLogging = prop.getBoolean();

        // Explosion type control lists

        prop = conf.get(CATEGORY_LISTS, "listIsWhitelist", false);
        prop.setComment("If true, then the whitelist is used. If false, then the blacklist is used.");
        listIsWhitelist = prop.getBoolean();

        prop = conf.get(CATEGORY_LISTS, "explosionTypeBlacklist", new String[] { "slimeknights.tconstruct.gadgets.entity.ExplosionEFLN" });
        prop.setComment("A list of full class names of explosions that should be ignored. Used if listIsWhitelist = false.");
        explosionBlacklistClassNames = prop.getStringList();

        prop = conf.get(CATEGORY_LISTS, "explosionTypeWhitelist", new String[0]);
        prop.setComment("A list of full class names of explosions that are the only ones that should be acted on. Used if listIsWhitelist = true.");
        explosionWhitelistClassNames = prop.getStringList();

        clearAndSetExplosionClasses(EXPLOSION_BLACKLIST, explosionBlacklistClassNames);
        clearAndSetExplosionClasses(EXPLOSION_WHITELIST, explosionWhitelistClassNames);

        if (disableCreeperExplosionCompletely)
        {
            CreeperEventHandler.getInstance().register();
        }
        else
        {
            CreeperEventHandler.getInstance().unregister();
        }

        if (conf.hasChanged())
        {
            conf.save();
        }
    }

    @SuppressWarnings("unchecked")
    private static void clearAndSetExplosionClasses(HashSet<Class<? extends Explosion>> set, String[] classNames)
    {
        set.clear();

        for (String name : classNames)
        {
            try
            {
                Class<?> clazz = Class.forName(name);

                if (Explosion.class.isAssignableFrom(clazz))
                {
                    set.add((Class<? extends Explosion>) clazz);
                }
            }
            catch (Exception e) {}
        }
    }
}
