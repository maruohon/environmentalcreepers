package fi.dy.masa.environmentalcreepers.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class JsonUtils
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Nullable
    public static JsonObject getNestedObject(JsonObject parent, String key, boolean create)
    {
        if (parent.has(key) == false || parent.get(key).isJsonObject() == false)
        {
            if (create == false)
            {
                return null;
            }

            JsonObject obj = new JsonObject();
            parent.add(key, obj);
            return obj;
        }
        else
        {
            return parent.get(key).getAsJsonObject();
        }
    }

    @Nullable
    public static JsonElement parseJsonFile(File file)
    {
        if (file != null && file.exists() && file.isFile() && file.canRead())
        {
            String fileName = file.getAbsolutePath();

            try
            {
                JsonParser parser = new JsonParser();
                FileReader reader = new FileReader(file);

                JsonElement element = parser.parse(reader);
                reader.close();

                return element;
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.error("Failed to parse the JSON file '{}'", fileName, e);
            }
        }

        return null;
    }

    public static boolean writeJsonToFile(JsonElement root, File file)
    {
        return writeJsonToFile(GSON, root, file);
    }

    public static boolean writeJsonToFile(Gson gson, JsonElement root, File file)
    {
        FileWriter writer = null;

        try
        {
            writer = new FileWriter(file);
            writer.write(gson.toJson(root));
            writer.close();

            return true;
        }
        catch (IOException e)
        {
            EnvironmentalCreepers.logger.warn("Failed to write JSON data to file '{}'", file.getAbsolutePath(), e);
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            }
            catch (Exception e)
            {
                EnvironmentalCreepers.logger.warn("Failed to close JSON file", e);
            }
        }

        return false;
    }
}
