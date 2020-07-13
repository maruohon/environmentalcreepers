package fi.dy.masa.environmentalcreepers.config.options;

import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class ConfigStringList extends ConfigBase
{
    private final ImmutableList<String> defaultValue;
    private ImmutableList<String> value;

    public ConfigStringList(String name, ImmutableList<String> defaultValue, String comment)
    {
        super(name, comment);

        this.defaultValue = defaultValue;
        this.value = ImmutableList.copyOf(defaultValue);
    }

    public ImmutableList<String> getValue()
    {
        return this.value;
    }

    public void setValue(List<String> newStrings)
    {
        this.value = ImmutableList.copyOf(newStrings);
    }

    @Override
    public void resetToDefault()
    {
        this.value = this.defaultValue;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element, String configName)
    {
        try
        {
            if (element.isJsonArray())
            {
                ImmutableList.Builder<String> builder = ImmutableList.builder();
                JsonArray arr = element.getAsJsonArray();
                final int count = arr.size();

                for (int i = 0; i < count; ++i)
                {
                    builder.add(arr.get(i).getAsString());
                }

                this.value = builder.build();
            }
            else
            {
                // Make sure to clear the old value in any case
                this.value = ImmutableList.of();
                EnvironmentalCreepers.logger.warn("Failed to set config value for '{}' from the JSON element '{}'", configName, element);
            }
        }
        catch (Exception e)
        {
            // Make sure to clear the old value in any case
            this.value = ImmutableList.of();
            EnvironmentalCreepers.logger.warn("Failed to set config value for '{}' from the JSON element '{}'", configName, element, e);
        }
    }

    @Override
    public JsonElement getAsJsonElement()
    {
        JsonArray arr = new JsonArray();

        for (String str : this.value)
        {
            arr.add(new JsonPrimitive(str));
        }

        return arr;
    }
}
