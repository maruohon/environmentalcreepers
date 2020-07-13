package fi.dy.masa.environmentalcreepers.config;

import java.io.File;
import com.google.common.io.Files;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class ConfigFileUtils
{
    public static void createDirIfMissing(File dir)
    {
        if (dir.exists() == false)
        {
            try
            {
                dir.mkdirs();
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.warn("Failed to create the directory '{}'", dir.getAbsolutePath(), e);
            }
        }
    }

    public static void copyFileIfMissing(File replacementFile, File fileToReplace)
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
