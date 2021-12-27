package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import com.google.common.io.Files;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class ConfigFileUtils
{
    public static void createDirIfNotExists(File dir)
    {
        if (dir.exists() == false)
        {
            try
            {
                if (dir.mkdirs() == false)
                {
                    EnvironmentalCreepers.logger.warn("Failed to create the directory '{}'", dir.getAbsolutePath());
                }
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.warn("Failed to create the directory '{}'", dir.getAbsolutePath(), e);
            }

        }
    }

    public static void tryCopyConfigIfMissing(File fileToReplace, File replacementFile)
    {
        if (replacementFile.exists() &&
            replacementFile.isFile() &&
            replacementFile.canRead() &&
            fileToReplace.exists() == false)
        {
            try
            {
                EnvironmentalCreepers.logger.info("Copying the file '{}' to the new location '{}'",
                        replacementFile.getAbsolutePath(), fileToReplace.getAbsolutePath());

                Files.copy(replacementFile, fileToReplace);
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.warn("Failed to copy the file '{}' to the new location '{}'",
                        replacementFile.getAbsolutePath(), fileToReplace.getAbsolutePath(), e);
            }
        }
    }
}
