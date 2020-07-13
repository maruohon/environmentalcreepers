package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.world.explosion.Explosion;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.options.ConfigBoolean;
import fi.dy.masa.environmentalcreepers.config.options.ConfigDouble;
import fi.dy.masa.environmentalcreepers.config.options.ConfigString;
import fi.dy.masa.environmentalcreepers.config.options.ConfigStringList;
import fi.dy.masa.environmentalcreepers.config.options.IConfigBase;
import fi.dy.masa.environmentalcreepers.util.JsonUtils;

public class Configs
{
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_CLASS_BLACKLIST = new HashSet<>();
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_CLASS_WHITELIST = new HashSet<>();
    public static final HashSet<Class<? extends Entity>> EXPLOSION_ENTITY_BLACKLIST = new HashSet<>();
    public static final HashSet<Class<? extends Entity>> EXPLOSION_ENTITY_WHITELIST = new HashSet<>();

    public static final String CATEGORY_GENERIC = "Generic";
    public static final String CATEGORY_LISTS = "Lists";
    public static final String CATEGORY_TOGGLES = "Toggles";

    public static class Generic
    {
        public static final ConfigBoolean COPY_CONFIG_TO_WORLD                  = new ConfigBoolean("copyConfigToWorld", false, "If true, then the global config file is copied to the world (in worldname/environmentalcreepers/environmentalcreepers.json), if one doesn't exist there yet.");
        public static final ConfigDouble CREEPER_ALTITUDE_DAMAGE_MAX_Y          = new ConfigDouble("creeperAltitudeDamageMaxY", 64.0, -30000000.0, 30000000.0, "The maximum y position where Creeper explosions will do block damage, if enableCreeperAltitudeCondition is enabled.");
        public static final ConfigDouble CREEPER_ALTITUDE_DAMAGE_MIN_Y          = new ConfigDouble("creeperAltitudeDamageMinY", -64.0, -30000000.0, 30000000.0, "The minimum y position where Creeper explosions will do block damage, if enableCreeperAltitudeCondition is enabled.");
        public static final ConfigDouble CREEPER_CHAIN_REACTION_CHANCE          = new ConfigDouble("creeperChainReactionChance", 1.0, 0.0, 1.0, "The chance of Creeper explosions to cause other Creepers to trigger within range. Set to 1.0 to always trigger.");
        public static final ConfigDouble CREEPER_CHAIN_REACTION_MAX_DISTANCE    = new ConfigDouble("creeperChainReactionMaxDistance", 16.0, 0.0, 160.0, "The maximum distance within which a Creeper exploding will cause a chain reaction.");
        public static final ConfigDouble CREEPER_EXPLOSION_BLOCK_DROP_CHANCE    = new ConfigDouble("creeperExplosionBlockDropChance", 1.0, 0.0, 1.0, "The chance of Creeper explosions to drop the blocks as items. Set to 1.0 to always drop.");
        public static final ConfigDouble CREEPER_EXPLOSION_STRENGTH_CHARGED     = new ConfigDouble("creeperExplosionStrengthCharged", 6.0, 0.0, 1000.0, "The strength of Charged Creeper explosions. Default in vanilla: 6.0 (double of normal Creepers).");
        public static final ConfigDouble CREEPER_EXPLOSION_STRENGTH_NORMAL      = new ConfigDouble("creeperExplosionStrengthNormal", 3.0, 0.0, 1000.0, "The strength of Creeper explosions. Default in vanilla in 3.0 for normal Creepers, and it is doubled ie. 6.0 for Charged Creepers.");
        public static final ConfigDouble OTHER_EXPLOSION_BLOCK_DROP_CHANCE      = new ConfigDouble("otherExplosionBlockDropChance", 1.0, 0.0, 1.0, "The chance of other explosions than Creepers to drop the blocks as items. Set to 1.0 to always drop.");
        public static final ConfigBoolean USE_PER_WORLD_CONFIG                  = new ConfigBoolean("usePerWorldConfig", false, "If true, then configs are attempted to be read from a config inside the world (in worldname/environmentalcreepers/environmentalcreepers.json), if one exists there.");
        public static final ConfigBoolean VERBOSE_LOGGING                       = new ConfigBoolean("verboseLogging", false, "Log some messages on each explosion, for debugging purposes. Leave disabled for normal use.");

        private static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                COPY_CONFIG_TO_WORLD,
                CREEPER_ALTITUDE_DAMAGE_MAX_Y,
                CREEPER_ALTITUDE_DAMAGE_MIN_Y,
                CREEPER_CHAIN_REACTION_CHANCE,
                CREEPER_CHAIN_REACTION_MAX_DISTANCE,
                CREEPER_EXPLOSION_BLOCK_DROP_CHANCE,
                CREEPER_EXPLOSION_STRENGTH_CHARGED,
                CREEPER_EXPLOSION_STRENGTH_NORMAL,
                OTHER_EXPLOSION_BLOCK_DROP_CHANCE,
                USE_PER_WORLD_CONFIG,
                VERBOSE_LOGGING
        );
    }

    public static class Toggles
    {
        public static final ConfigBoolean DISABLE_ALL_EXPLOSIONS                    = new ConfigBoolean("disableAllExplosions", false, "Completely disables all explosions");
        public static final ConfigBoolean DISABLE_CREEPER_EXPLOSION_BLOCK_DAMAGE    = new ConfigBoolean("disableCreeperExplosionBlockDamage", false, "Completely disable Creeper explosion from damaging blocks");
        public static final ConfigBoolean DISABLE_CREEPER_EXPLOSION_ENTIRELY        = new ConfigBoolean("disableCreeperExplosionCompletely", false, "Completely disable Creepers from exploding");
        public static final ConfigBoolean DISABLE_CREEPER_EXPLOSION_ENTITY_DAMAGE   = new ConfigBoolean("disableCreeperExplosionEntityDamage", false, "Disable Creeper explosions from damaging any entities (including items)");
        public static final ConfigBoolean DISABLE_CREEPER_EXPLOSION_ITEM_DAMAGE     = new ConfigBoolean("disableCreeperExplosionItemDamage", false, "Disable Creeper explosions from damaging items on the ground");
        public static final ConfigBoolean DISABLE_OTHER_EXPLOSION_BLOCK_DAMAGE      = new ConfigBoolean("disableOtherExplosionBlockDamage", false, "Completely disable other explosions than Creepers from damaging blocks");
        public static final ConfigBoolean DISABLE_OTHER_EXPLOSION_ENTITY_DAMAGE     = new ConfigBoolean("disableOtherExplosionEntityDamage", false, "Disable other explosions than Creepers from damaging any entities (including items)");
        public static final ConfigBoolean DISABLE_OTHER_EXPLOSION_ITEM_DAMAGE       = new ConfigBoolean("disableOtherExplosionItemDamage", false, "Disable other explosions than Creepers from damaging items on the ground");
        public static final ConfigBoolean CREEPER_ALTITUDE_CONDITION                = new ConfigBoolean("creeperAltitudeCondition", false, "Enable setting a y range for Creepers to do block damage. Set the range in Generic -> 'creeperAltitudeDamageMaxY' and 'creeperAltitudeDamageMinY'.");
        public static final ConfigBoolean CREEPER_EXPLOSION_CHAIN_REACTION          = new ConfigBoolean("enableCreeperExplosionChainReaction", false, "When enabled, a Creeper exploding has a chance to trigger other nearby Creepers.");
        public static final ConfigBoolean MODIFY_CREEPER_EXPLOSION_DROP_CHANCE      = new ConfigBoolean("modifyCreeperExplosionDropChance", true, "Modify the chance of Creeper explosions to drop the blocks as items. Set the chance in Generic -> creeperExplosionBlockDropChance.");
        public static final ConfigBoolean MODIFY_CREEPER_EXPLOSION_STRENGTH         = new ConfigBoolean("modifyCreeperExplosionStrength", false, "Modify the strength of Creeper explosions.");
        public static final ConfigBoolean MODIFY_OTHER_EXPLOSION_DROP_CHANCE        = new ConfigBoolean("modifyOtherExplosionDropChance", false, "Modify the chance of other explosions than Creepers to drop the blocks as items. Set the chance in Generic -> otherExplosionBlockDropChance.");

        private static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                DISABLE_ALL_EXPLOSIONS,
                DISABLE_CREEPER_EXPLOSION_BLOCK_DAMAGE,
                DISABLE_CREEPER_EXPLOSION_ENTIRELY,
                DISABLE_CREEPER_EXPLOSION_ENTITY_DAMAGE,
                DISABLE_CREEPER_EXPLOSION_ITEM_DAMAGE,
                DISABLE_OTHER_EXPLOSION_BLOCK_DAMAGE,
                DISABLE_OTHER_EXPLOSION_ENTITY_DAMAGE,
                DISABLE_OTHER_EXPLOSION_ITEM_DAMAGE,
                CREEPER_ALTITUDE_CONDITION,
                CREEPER_EXPLOSION_CHAIN_REACTION,
                MODIFY_CREEPER_EXPLOSION_DROP_CHANCE,
                MODIFY_CREEPER_EXPLOSION_STRENGTH,
                MODIFY_OTHER_EXPLOSION_DROP_CHANCE
        );
    }

    public static class Lists
    {
        private static final ConfigString     ENTITY_CLASS_LIST_TYPE            = new ConfigString("entityClassListType", "blacklist", "The list type for the entity class filtering. Either 'none' or 'blacklist' or 'whitelist'. Blacklisted (or non-whitelisted) entities will not be removed from the explosion damage list. This allows for example those entities to run their custom code when damaged by explosions.");
        private static final ConfigStringList ENTITY_BLACKLIST_CLASS_NAMES      = new ConfigStringList("entityBlacklistClassNames", ImmutableList.of(), "A list of full class names of entities that should be ignored. This means that these entities will not get removed from the list of entities to be damaged by the explosion, allowing these entities to handle the explosion code themselves. Used if entityClassListType = blacklist");
        private static final ConfigStringList ENTITY_WHITELIST_CLASS_NAMES      = new ConfigStringList("entityWhitelistClassNames", ImmutableList.of(), "A list of full class names of entities that are the only ones\n that should be acted on, see the comment on entityTypeBlacklist. Used if entityClassListType = whitelist");
        private static final ConfigString     EXPLOSION_CLASS_LIST_TYPE         = new ConfigString("explosionClassListType", "blacklist", "The list type for the explosion class filtering. Either 'none' or 'blacklist' or 'whitelist'. Blacklisted (or non-whitelisted) explosion types won't be handled by this mod.");
        private static final ConfigStringList EXPLOSION_BLACKLIST_CLASS_NAMES   = new ConfigStringList("explosionBlacklistClassNames", ImmutableList.of(), "A list of full class names of explosions that should be ignored. Used if explosionClassListType = blacklist");
        private static final ConfigStringList EXPLOSION_WHITELIST_CLASS_NAMES   = new ConfigStringList("explosionWhitelistClassNames", ImmutableList.of(), "A list of full class names of explosions that are the only ones that should be acted on. Used if explosionClassListType = whitelist");

        private static final List<? extends IConfigBase> OPTIONS = ImmutableList.of(
                ENTITY_CLASS_LIST_TYPE,
                ENTITY_BLACKLIST_CLASS_NAMES,
                ENTITY_WHITELIST_CLASS_NAMES
                /*
                EXPLOSION_CLASS_LIST_TYPE,
                EXPLOSION_BLACKLIST_CLASS_NAMES,
                EXPLOSION_WHITELIST_CLASS_NAMES
                */
        );

        public static ListType explosionClassListType = ListType.NONE;
        public static ListType entityClassListType = ListType.NONE;
    }

    private static File globalConfigDirectory = new File("config");
    private static File worldConfigDirectory;
    private static File lastLoadedConfigDirectory;

    private static Map<String, List<? extends IConfigBase>> getConfigsPerCategories()
    {
        return ImmutableMap.of(CATEGORY_GENERIC, Generic.OPTIONS, CATEGORY_TOGGLES, Toggles.OPTIONS, CATEGORY_LISTS, Lists.OPTIONS);
    }

    private static void clearOldValues()
    {
        Lists.entityClassListType    = ListType.NONE;
        Lists.explosionClassListType = ListType.NONE;

        EXPLOSION_ENTITY_BLACKLIST.clear();
        EXPLOSION_ENTITY_WHITELIST.clear();

        EXPLOSION_CLASS_BLACKLIST.clear();
        EXPLOSION_CLASS_WHITELIST.clear();

        for (Map.Entry<String, List<? extends IConfigBase>> entry : getConfigsPerCategories().entrySet())
        {
            entry.getValue().forEach(IConfigBase::resetToDefault);
        }
    }

    private static void onPostLoad()
    {
        Lists.entityClassListType    = getListType(Lists.ENTITY_CLASS_LIST_TYPE);
        Lists.explosionClassListType = getListType(Lists.EXPLOSION_CLASS_LIST_TYPE);

        clearAndSetEntityClasses(EXPLOSION_ENTITY_BLACKLIST, Lists.ENTITY_BLACKLIST_CLASS_NAMES.getValue());
        clearAndSetEntityClasses(EXPLOSION_ENTITY_WHITELIST, Lists.ENTITY_WHITELIST_CLASS_NAMES.getValue());

        clearAndSetExplosionClasses(EXPLOSION_CLASS_BLACKLIST, Lists.EXPLOSION_BLACKLIST_CLASS_NAMES.getValue());
        clearAndSetExplosionClasses(EXPLOSION_CLASS_WHITELIST, Lists.EXPLOSION_WHITELIST_CLASS_NAMES.getValue());
    }

    private static ListType getListType(ConfigString config)
    {
        String configValue = config.getValue();

        if (ListType.BLACKLIST.name().equalsIgnoreCase(configValue))
        {
            return ListType.BLACKLIST;
        }
        else if (ListType.WHITELIST.name().equalsIgnoreCase(configValue))
        {
            return ListType.WHITELIST;
        }
        else if (ListType.NONE.name().equalsIgnoreCase(configValue))
        {
            return ListType.NONE;
        }
        else
        {
            EnvironmentalCreepers.logger.error("Invalid list type '{}' for config '{}'", configValue, config.getName());
            return ListType.NONE;
        }
    }

    public static void setGlobalConfigDir(File dir)
    {
        if (dir.exists() == false && dir.mkdirs() == false)
        {
            EnvironmentalCreepers.logger.error("Failed to create config directory '{}'", dir.getName());
            return;
        }

        globalConfigDirectory = dir;
    }

    public static void setWorldConfigDir(File dir)
    {
        if (Generic.COPY_CONFIG_TO_WORLD.getValue() || Generic.USE_PER_WORLD_CONFIG.getValue())
        {
            if (dir.exists() == false && dir.mkdirs() == false)
            {
                EnvironmentalCreepers.logger.error("Failed to create config directory '{}'", dir.getName());
                return;
            }

            worldConfigDirectory = dir;
        }
    }

    public static void loadConfigsFromGlobalConfigFile()
    {
        loadConfigsFromDirectory(globalConfigDirectory);
    }

    public static void loadConfigsFromPerWorldConfigIfApplicable()
    {
        @Nullable File dir = worldConfigDirectory;

        if (dir != null)
        {
            if (Generic.COPY_CONFIG_TO_WORLD.getValue())
            {
                File globalConfigFile = new File(globalConfigDirectory, getConfigFileName());
                File configFile = new File(dir, getConfigFileName());
                ConfigFileUtils.createDirIfMissing(dir);
                ConfigFileUtils.copyFileIfMissing(globalConfigFile, configFile);
            }

            if (Generic.USE_PER_WORLD_CONFIG.getValue() && dir.exists() && dir.isDirectory())
            {
                loadConfigsFromDirectory(dir);
                return;
            }
        }

        // Fall-back
        loadConfigsFromGlobalConfigFile();
    }

    public static boolean reloadConfig()
    {
        if (lastLoadedConfigDirectory != null)
        {
            return loadConfigsFromDirectory(lastLoadedConfigDirectory);
        }

        return false;
    }

    private static boolean loadConfigsFromDirectory(@Nullable File dir)
    {
        if (dir != null)
        {
            lastLoadedConfigDirectory = dir;
            File file = new File(dir, getConfigFileName());

            EnvironmentalCreepers.logInfo("Reloading the configs from file '{}'", file.getAbsolutePath());

            return loadConfigsFromFile(file);
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private static void clearAndSetEntityClasses(HashSet<Class<? extends Entity>> set, List<String> classNames)
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
    private static void clearAndSetExplosionClasses(HashSet<Class<? extends Explosion>> set, List<String> classNames)
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

    public static boolean loadConfigsFromFile(File configFile)
    {
        clearOldValues();

        boolean success = false;

        if (configFile.exists() && configFile.isFile() && configFile.canRead())
        {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();

                for (Map.Entry<String, List<? extends IConfigBase>> entry : getConfigsPerCategories().entrySet())
                {
                    ConfigUtils.readConfigBase(root, entry.getKey(), entry.getValue());
                }

                success = true;
            }
        }
        else
        {
            // Create the config file when it doesn't exist yet
            saveConfigs();
        }

        onPostLoad();

        return success;
    }

    public static void saveConfigs()
    {
        File dir = lastLoadedConfigDirectory;

        if (dir == null)
        {
            EnvironmentalCreepers.logger.error("No valid config directory set");
            return;
        }

        if (dir.exists() == false && dir.mkdirs() == false)
        {
            EnvironmentalCreepers.logger.error("Failed to create config directory '{}'", dir.getName());
        }

        if (dir.exists() && dir.isDirectory())
        {
            JsonObject root = new JsonObject();

            for (Map.Entry<String, List<? extends IConfigBase>> entry : getConfigsPerCategories().entrySet())
            {
                ConfigUtils.writeConfigBase(root, entry.getKey(), entry.getValue());
            }

            JsonUtils.writeJsonToFile(root, new File(dir, getConfigFileName()));
        }
    }

    public static String getConfigFileName()
    {
        return "environmentalcreepers.json";
    }

    public enum ListType
    {
        NONE        ("none"),
        BLACKLIST   ("blacklist"),
        WHITELIST   ("whitelist");

        private final String name;

        ListType(String name)
        {
            this.name = name;
        }

        /*
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
        */
    }
}
