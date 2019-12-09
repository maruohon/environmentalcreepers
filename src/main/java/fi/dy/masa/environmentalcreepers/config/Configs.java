package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.world.Explosion;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.Reference;
import fi.dy.masa.environmentalcreepers.event.CreeperEventHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber
public class Configs
{
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_CLASS_BLACKLIST = new HashSet<Class<? extends Explosion>>();
    public static final HashSet<Class<? extends Explosion>> EXPLOSION_CLASS_WHITELIST = new HashSet<Class<? extends Explosion>>();
    public static final HashSet<Class<? extends Entity>> EXPLOSION_ENTITY_BLACKLIST = new HashSet<Class<? extends Entity>>();
    public static final HashSet<Class<? extends Entity>> EXPLOSION_ENTITY_WHITELIST = new HashSet<Class<? extends Entity>>();

    public static final String CATEGORY_GENERIC = "Generic";
    public static final String CATEGORY_LISTS = "Lists";
    public static final String CATEGORY_TOGGLES = "Toggles";

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;

    public static class Generic
    {
        private static boolean copyConfigToWorld;
        private static boolean usePerWorldConfig;
        public static boolean verboseLogging;

        public static double creeperChainReactionChance;
        public static double creeperChainReactionMaxDistance;
        public static double creeperExplosionBlockDropChance;
        public static double creeperExplosionStrengthNormal;
        public static double creeperExplosionStrengthCharged;
        public static double otherExplosionBlockDropChance;
    }

    public static class Toggles
    {
        public static boolean disableCreeperExplosionBlockDamage;
        public static boolean disableCreeperExplosionCompletely;
        public static boolean disableCreeperExplosionItemDamage;
        public static boolean disableOtherExplosionBlockDamage;
        public static boolean disableOtherExplosionItemDamage;
        public static boolean enableCreeperExplosionChainReaction;
        public static boolean modifyCreeperExplosionDropChance;
        public static boolean modifyCreeperExplosionStrength;
        public static boolean modifyOtherExplosionDropChance;
    }

    public static class Lists
    {
        public static ListType explosionClassListType = ListType.NONE;
        public static ListType entityClassListType = ListType.NONE;

        private static List<String> entityBlacklistClassNames = ImmutableList.of();
        private static List<String> entityWhitelistClassNames = ImmutableList.of();
        private static List<String> explosionBlacklistClassNames = ImmutableList.of();
        private static List<String> explosionWhitelistClassNames = ImmutableList.of();
    }

    private static File configFileGlobal;
    private static Path lastLoadedConfig;

    static
    {
        setupConfigs();
    }

    private static void setupConfigs()
    {
        addCategoryGeneric();
        addCategoryToggles();
        addCategoryLists();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    private static void addCategoryGeneric()
    {
        COMMON_BUILDER.comment(" Generic configs").push(CATEGORY_GENERIC);

        COMMON_BUILDER.comment(" If true, then the global config file is copied to the world\n" +
                               " (in worldname/environmentalcreepers/environmentalcreepers.cfg), if one doesn't exist there yet.")
                      .define("copyConfigToWorld", false);

        COMMON_BUILDER.comment(" The chance of Creeper explosions to cause other Creepers to trigger\n" +
                               " within range. Set to 1.0 to always trigger.")
                      .defineInRange("creeperChainReactionChance", 1.0, 0.0, 1.0);

        COMMON_BUILDER.comment(" The maximum distance within which a Creeper exploding will cause a chain reaction.")
                      .defineInRange("creeperChainReactionMaxDistance", 16.0, 0.0, 160.0);

        COMMON_BUILDER.comment(" The chance of Creeper explosions to drop the blocks as items.\n" +
                               " Set to 1.0 to always drop.")
                      .defineInRange("creeperExplosionBlockDropChance", 1.0, 0.0, 1.0);

        COMMON_BUILDER.comment(" The strength of Creeper explosions. Default in vanilla in 3.0 for normal Creepers,\n" +
                               " and it is doubled ie. 6.0 for Charged Creepers.")
                      .defineInRange("creeperExplosionStrengthNormal", 3.0, 0.0, 1000.0);

        COMMON_BUILDER.comment(" The strength of Charged Creeper explosions.\n" +
                               " Default in vanilla: 6.0 (double of normal Creepers).")
                      .defineInRange("creeperExplosionStrengthCharged", 6.0, 0.0, 1000.0);

        COMMON_BUILDER.comment(" The chance of other explosions than Creepers to drop the blocks as items.\n" +
                               " Set to 1.0 to always drop.")
                      .defineInRange("otherExplosionBlockDropChance", 1.0, 0.0, 1.0);

        COMMON_BUILDER.comment(" If true, then configs are attempted to be read from a config inside\n" +
                               " the world (in worldname/environmentalcreepers/environmentalcreepers.cfg), if one exists there.")
                      .define("usePerWorldConfig", false);

        COMMON_BUILDER.comment(" Log some messages on each explosion, for debugging purposes.\n" +
                               " Leave disabled for normal use.")
                      .define("verboseLogging", false);

        COMMON_BUILDER.pop();
    }

    private static void addCategoryToggles()
    {
        COMMON_BUILDER.comment(" Toggle options to enable/disable features").push(CATEGORY_TOGGLES);

        COMMON_BUILDER.comment(" Completely disable Creeper explosion from damaging blocks")
                      .define("disableCreeperExplosionBlockDamage", false);

        COMMON_BUILDER.comment(" Completely disable Creepers from exploding")
                      .define("disableCreeperExplosionCompletely", false);

        COMMON_BUILDER.comment(" Disable Creeper explosions from damaging items on the ground")
                      .define("disableCreeperExplosionItemDamage", false);

        COMMON_BUILDER.comment(" Completely disable other explosions than Creepers from damaging blocks")
                      .define("disableOtherExplosionBlockDamage", false);

        COMMON_BUILDER.comment(" Disable other explosions than Creepers from damaging items on the ground")
                      .define("disableOtherExplosionItemDamage", false);

        COMMON_BUILDER.comment(" When enabled, a Creeper exploding has a chance to trigger other nearby Creepers.")
                      .define("enableCreeperExplosionChainReaction", false);

        COMMON_BUILDER.comment(" Modify the chance of Creeper explosions to drop the blocks as items.\n" +
                               " Set the chance in creeperExplosionBlockDropChance.")
                      .define("modifyCreeperExplosionDropChance", true);

        COMMON_BUILDER.comment(" Modify the strength of Creeper explosions.")
                      .define("modifyCreeperExplosionStrength", false);

        COMMON_BUILDER.comment(" Modify the chance of other explosions than Creepers to drop the blocks\n" +
                               " as items. Set the chance in otherExplosionBlockDropChance.")
                      .define("modifyOtherExplosionDropChance", false);

        COMMON_BUILDER.pop();
    }

    private static void addCategoryLists()
    {
        COMMON_BUILDER.comment(" Explosion type, entity type etc. black- and white lists").push(CATEGORY_LISTS);

        COMMON_BUILDER.comment(" The list type for the entity class filtering.\n" +
                               " Either 'none' or 'blacklist' or 'whitelist'.\n" + 
                               " Blacklisted (or non-whitelisted) entities will not be removed from the explosion damage list.\n" + 
                               " This allows for example those entities to run their custom code when damaged by explosions.")
                      .define("entityClassListType", "blacklist");

        COMMON_BUILDER.comment(" The list type for the explosion class filtering.\n" +
                               " Either 'none' or 'blacklist' or 'whitelist'.\n" + 
                               " Blacklisted (or non-whitelisted) explosion types won't be handled by this mod.")
                      .define("explosionClassListType", "blacklist");

        COMMON_BUILDER.comment(" A list of full class names of entities that should be ignored.\n" +
                               " This means that these entities will not get removed from the\n" +
                               " list of entities to be damaged by the explosion, allowing these\n" +
                               " entities to handle the explosion code themselves.\n" +
                               " Used if entityClassListType = blacklist")
                      .defineList("entityBlacklistClassNames", ImmutableList.of("appeng.entity.EntitySingularity"), (val) -> true);

        COMMON_BUILDER.comment(" A list of full class names of entities that are the only ones\n" +
                               " that should be acted on, see the comment on entityTypeBlacklist.\n" +
                               " Used if entityClassListType = whitelist")
                      .defineList("entityWhitelistClassNames", ImmutableList.of(), (val) -> true);

        COMMON_BUILDER.comment(" A list of full class names of explosions that should be ignored.\n" +
                               " Used if explosionClassListType = blacklist")
                      .defineList("explosionBlacklistClassNames", ImmutableList.of("slimeknights.tconstruct.gadgets.entity.ExplosionEFLN"), (val) -> true);

        COMMON_BUILDER.comment(" A list of full class names of explosions that are the only ones that should be acted on.\n" +
                               " Used if explosionClassListType = whitelist")
                      .defineList("explosionWhitelistClassNames", ImmutableList.of(), (val) -> true);

        COMMON_BUILDER.pop();
    }

    private static void setConfigValues(ForgeConfigSpec spec)
    {
        setValuesInClass(Generic.class, spec);
        setValuesInClass(Toggles.class, spec);
        setValuesInClass(Lists.class, spec);

        setListType(Lists.class, spec, "entityClassListType");
        setListType(Lists.class, spec, "explosionClassListType");

        clearAndSetEntityClasses(EXPLOSION_ENTITY_BLACKLIST, Lists.entityBlacklistClassNames);
        clearAndSetEntityClasses(EXPLOSION_ENTITY_WHITELIST, Lists.entityWhitelistClassNames);

        clearAndSetExplosionClasses(EXPLOSION_CLASS_BLACKLIST, Lists.explosionBlacklistClassNames);
        clearAndSetExplosionClasses(EXPLOSION_CLASS_WHITELIST, Lists.explosionWhitelistClassNames);

        if (Toggles.disableCreeperExplosionCompletely)
        {
            CreeperEventHandler.getInstance().register();
        }
        else
        {
            CreeperEventHandler.getInstance().unregister();
        }
    }

    private static void setValuesInClass(Class<?> clazz, ForgeConfigSpec spec)
    {
        for (Field field : clazz.getDeclaredFields())
        {
            String category = clazz.getSimpleName();
            String name = field.getName();

            try
            {
                Class<?> type = field.getType();
                field.setAccessible(true);

                if (type == boolean.class)
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.BooleanValue>get(category + "." + name).get().booleanValue());
                }
                else if (type == double.class)
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.DoubleValue>get(category + "." + name).get().doubleValue());
                }
                else if (type == String.class)
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.ConfigValue<String>>get(category + "." + name).get());
                }
                else if (List.class.isAssignableFrom(type))
                {
                    field.set(null, spec.getValues().<ForgeConfigSpec.ConfigValue<List<String>>>get(category + "." + name).get());
                }
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.error("Failed to set config value for config '{}.{}'", category, name);
            }
        }
    }

    private static void setListType(Class<?> clazz, ForgeConfigSpec spec, String name)
    {
        String category = clazz.getSimpleName();

        try
        {
            String strVal = spec.getValues().<ForgeConfigSpec.ConfigValue<String>>get(category + "." + name).get();
            ListType type = ListType.NONE;

            if (ListType.BLACKLIST.name().equalsIgnoreCase(strVal))
            {
                type = ListType.BLACKLIST;
            }
            else if (ListType.WHITELIST.name().equalsIgnoreCase(strVal))
            {
                type = ListType.WHITELIST;
            }

            Field field = clazz.getDeclaredField(name);
            field.set(null, type);
        }
        catch (Exception e)
        {
            EnvironmentalCreepers.logger.error("Failed to set config value for config '{}.{}'", category, name);
        }
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfig.Loading event)
    {
        //System.out.printf("*** ModConfig.Loading\n");
        setConfigValues(COMMON_CONFIG);
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfig.ConfigReloading event)
    {
        //System.out.printf("*** ModConfig.ConfigReloading\n");
        setConfigValues(COMMON_CONFIG);
    }

    public static void loadConfig(Path path)
    {
        EnvironmentalCreepers.logInfo("Reloading the configs from file '{}'", path.toAbsolutePath().toString());

        ForgeConfigSpec spec = COMMON_CONFIG;
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);

        setConfigValues(spec);

        lastLoadedConfig = path;
    }

    public static void setGlobalConfigDirAndLoadConfigs(File configDirCommon)
    {
        File configFile = new File(configDirCommon, Reference.MOD_ID + ".toml");
        configFileGlobal = configFile;

        loadConfigsFromGlobalConfigFile();
    }

    public static void loadConfigsFromPerWorldConfigIfExists(@Nullable File configDir)
    {
        if (configDir != null)
        {
            File configFile = new File(configDir, Reference.MOD_ID + ".toml");

            if (Generic.copyConfigToWorld)
            {
                ConfigFileUtils.createDirIfNotExists(configDir);
                ConfigFileUtils.tryCopyConfigIfMissing(configFile, configFileGlobal);
            }

            if (Generic.usePerWorldConfig && configFile.exists() && configFile.isFile() && configFile.canRead())
            {
                loadConfig(configFile.toPath());
                return;
            }
        }

        // Fall-back
        loadConfigsFromGlobalConfigFile();
    }

    public static void loadConfigsFromGlobalConfigFile()
    {
        loadConfig(configFileGlobal.toPath());
    }

    public static boolean reloadConfig()
    {
        if (lastLoadedConfig != null)
        {
            loadConfig(lastLoadedConfig);
            return true;
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
