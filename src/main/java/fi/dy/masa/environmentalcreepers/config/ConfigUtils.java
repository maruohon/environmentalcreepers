package fi.dy.masa.environmentalcreepers.config;

import java.util.List;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.environmentalcreepers.config.options.IConfigBase;
import fi.dy.masa.environmentalcreepers.util.JsonUtils;

public class ConfigUtils
{
    public static void readConfigBase(JsonObject root, String category, List<? extends IConfigBase> options)
    {
        JsonObject obj = JsonUtils.getNestedObject(root, category, false);

        if (obj != null)
        {
            for (IConfigBase config : options)
            {
                String name = config.getName();

                if (obj.has(name))
                {
                    config.setValueFromJsonElement(obj.get(name), name);
                }
            }
        }
    }

    public static void writeConfigBase(JsonObject root, String category, List<? extends IConfigBase> options)
    {
        JsonObject obj = JsonUtils.getNestedObject(root, category, true);

        for (IConfigBase option : options)
        {
            obj.add("_comment_" + option.getName(), new JsonPrimitive(option.getComment()));
            obj.add(option.getName(), option.getAsJsonElement());
        }
    }
}
