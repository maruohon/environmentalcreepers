package fi.dy.masa.environmentalcreepers.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class ConfigBoolean extends ConfigBase
{
    private final boolean defaultValue;
    private boolean value;

    public ConfigBoolean(String name, boolean defaultValue, String comment)
    {
        this(name, defaultValue, comment, name);
    }

    public ConfigBoolean(String name, boolean defaultValue, String comment, String prettyName)
    {
        super(name, comment, prettyName);

        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean getValue()
    {
        return this.value;
    }

    public boolean getDefaultValue()
    {
        return this.defaultValue;
    }

    public void setValue(boolean value)
    {
        this.value = value;
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
            if (element.isJsonPrimitive())
            {
                this.value = element.getAsBoolean();
            }
            else
            {
                EnvironmentalCreepers.logger.warn("Failed to set config value for '{}' from the JSON element '{}'", configName, element);
            }
        }
        catch (Exception e)
        {
            EnvironmentalCreepers.logger.warn("Failed to set config value for '{}' from the JSON element '{}'", configName, element, e);
        }
    }

    @Override
    public JsonElement getAsJsonElement()
    {
        return new JsonPrimitive(this.value);
    }
}
