package fi.dy.masa.environmentalcreepers;

import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.environmentalcreepers.config.Configs;
import net.fabricmc.api.ModInitializer;

public class EnvironmentalCreepers implements ModInitializer
{
    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    @Override
    public void onInitialize()
    {
    }

    public static void logInfo(String message, Object ... params)
    {
        if (Configs.Generic.VERBOSE_LOGGING.getValue())
        {
            logger.info(message, params);
        }
    }

    public static void logInfo(Supplier<String> supplier)
    {
        if (Configs.Generic.VERBOSE_LOGGING.getValue())
        {
            logger.info(supplier.get());
        }
    }
}
