package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import java.util.HashSet;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
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
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_CLASS_BLACKLIST = new HashSet<Class<? extends Explosion>>();
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_CLASS_WHITELIST = new HashSet<Class<? extends Explosion>>();
    public static final HashSet<Class<? extends Entity>> EXPLOSION_ENTITY_BLACKLIST = new HashSet<Class<? extends Entity>>();
    public static final HashSet<Class<? extends Entity>> EXPLOSION_ENTITY_WHITELIST = new HashSet<Class<? extends Entity>>();

    private static boolean copyConfigToWorld;
    private static boolean usePerWorldConfig;

    public static boolean disableCreeperExplosionBlockDamage;
    public static boolean disableCreeperExplosionCompletely;
    public static boolean disableCreeperExplosionItemDamage;
    public static boolean disableOtherExplosionBlockDamage;
    public static boolean disableOtherExplosionItemDamage;
    public static boolean enableCreeperExplosionChainReaction;
    public static boolean enableCreeperAltitudeCondition;
    public static boolean modifyCreeperExplosionDropChance;
    public static boolean modifyCreeperExplosionStrength;
    public static boolean modifyOtherExplosionDropChance;
    public static double creeperAltitudeDamageMaxY;
    public static double creeperAltitudeDamageMinY;
    public static double creeperChainReactionChance;
    public static double creeperChainReactionMaxDistance;
    public static double creeperExplosionBlockDropChance;
    public static double creeperExplosionStrengthNormal;
    public static double creeperExplosionStrengthCharged;
    public static double otherExplosionBlockDropChance;
    public static ListType explosionClassListType = ListType.NONE;
    public static ListType explosionEntityListType = ListType.NONE;
    public static boolean verboseLogging;
    private static String[] explosionEntityBlacklistClassNames;
    private static String[] explosionEntityWhitelistClassNames;
    private static String[] explosionBlacklistClassNames;
    private static String[] explosionWhitelistClassNames;

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
            reloadConfig();
        }
    }

    public static void reloadConfig()
    {
        if (config != null)
        {
            EnvironmentalCreepers.logInfo("Reloading the configs from file '{}'", config.getConfigFile().getAbsolutePath());
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
        reloadConfig();
    }

    private static void loadConfigs(Configuration conf)
    {
        conf.load();

        Property prop;

        prop = conf.get(CATEGORY_GENERIC, "copyConfigToWorld", true);
        prop.setComment("If true, then the global config file is copied to the world\n (in data/environmentalcreepers/environmentalcreepers.cfg), if one doesn't exist there yet.");
        copyConfigToWorld = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "creeperAltitudeDamageMaxY", 64);
        prop.setComment("The maximum y position where Creeper explosions will do block damage,\nif enableCreeperAltitudeCondition is enabled.");
        creeperAltitudeDamageMaxY = prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "creeperAltitudeDamageMinY", -64);
        prop.setComment("The minimum y position where Creeper explosions will do block damage,\nif enableCreeperAltitudeCondition is enabled.");
        creeperAltitudeDamageMinY = prop.getDouble();

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

        prop = conf.get(CATEGORY_GENERIC, "enableCreeperAltitudeCondition", false);
        prop.setComment("Enable setting a y range for Creepers to do block damage.\nSet the range in 'creeperAltitudeDamageMaxY' and 'creeperAltitudeDamageMinY'.");
        enableCreeperAltitudeCondition = prop.getBoolean();

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

        prop = conf.get(CATEGORY_LISTS, "entityClassListType", "blacklist");
        prop.setComment("The list type for the explosion class filtering.\nEither 'none' or 'blacklist' or 'whitelist'.\n" +
                        "Blacklisted (or non-whitelisted) entities will not be removed from the explosion damage list.\n" +
                        "This allows for example those entities to run their custom code when damaged by explosions.");
        explosionEntityListType = ListType.fromName(prop.getString());

        prop = conf.get(CATEGORY_LISTS, "entityTypeBlacklist", new String[] { "appeng.entity.EntitySingularity" });
        prop.setComment("A list of full class names of entities that should be ignored.\n" +
                        "This means that these entities will not get removed from the\n" +
                        "list of entities to be damaged by the explosion, allowing these\n" +
                        "entities to handle the explosion code themselves.\n" +
                        "Used if entityClassListType = blacklist");
        explosionEntityBlacklistClassNames = prop.getStringList();

        prop = conf.get(CATEGORY_LISTS, "entityTypeWhitelist", new String[0]);
        prop.setComment("A list of full class names of entities that are the only ones\n" +
                        "that should be acted on, see the comment on entityTypeBlacklist.\n" +
                        "Used if entityClassListType = whitelist");
        explosionEntityWhitelistClassNames = prop.getStringList();

        prop = conf.get(CATEGORY_LISTS, "explosionClassListType", "blacklist");
        prop.setComment("The list type for the explosion class filtering.\nEither 'none' or 'blacklist' or 'whitelist'.\n" +
                        "Blacklisted (or non-whitelisted) explosion types won't be handled by this mod.");
        explosionClassListType = ListType.fromName(prop.getString());

        prop = conf.get(CATEGORY_LISTS, "explosionTypeBlacklist", new String[] { "slimeknights.tconstruct.gadgets.entity.ExplosionEFLN" });
        prop.setComment("A list of full class names of explosions that should be ignored. Used if explosionClassListType = blacklist");
        explosionBlacklistClassNames = prop.getStringList();

        prop = conf.get(CATEGORY_LISTS, "explosionTypeWhitelist", new String[0]);
        prop.setComment("A list of full class names of explosions that are the only ones that should be acted on. Used if explosionClassListType = whitelist");
        explosionWhitelistClassNames = prop.getStringList();

        clearAndSetEntityClasses(EXPLOSION_ENTITY_BLACKLIST, explosionEntityBlacklistClassNames);
        clearAndSetEntityClasses(EXPLOSION_ENTITY_WHITELIST, explosionEntityWhitelistClassNames);

        clearAndSetExplosionClasses(EXPLOSION_CLASS_BLACKLIST, explosionBlacklistClassNames);
        clearAndSetExplosionClasses(EXPLOSION_CLASS_WHITELIST, explosionWhitelistClassNames);

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
    private static void clearAndSetEntityClasses(HashSet<Class<? extends Entity>> set, String[] classNames)
    {
        set.clear();

        for (String name : classNames)
        {
            try
            {
                Class<?> clazz = Class.forName(name);

                if (Entity.class.isAssignableFrom(clazz))
                {
                    set.add((Class<? extends Entity>) clazz);
                }
                else
                {
                    EnvironmentalCreepers.logger.warn("Invalid entity class name (not an Entity): '{}'", name);
                }
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.warn("Invalid entity class name (class not found): '{}'", name);
            }
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
                else
                {
                    EnvironmentalCreepers.logger.warn("Invalid explosion class name (not an explosion class): '{}'", name);
                }
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.warn("Invalid explosion class name (class not found): '{}'", name);
            }
        }
    }

    public static enum ListType
    {
        NONE        ("none"),
        BLACKLIST   ("blacklist"),
        WHITELIST   ("whitelist");

        private final String name;

        ListType(String name)
        {
            this.name = name;
        }

        public static ListType fromName(String name)
        {
            for (ListType val : values())
            {
                if (val.name.equalsIgnoreCase(name))
                {
                    return val;
                }
            }

            return ListType.NONE;
        }
    }
}
