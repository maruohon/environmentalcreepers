package fi.dy.masa.environmentalcreepers.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class ConfigString extends ConfigBase
{
    protected final String defaultValue;
    protected String value;

    public ConfigString(String name, String defaultValue, String comment)
    {
        super(name, comment);

        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
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
                this.value = element.getAsString();
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

    public JsonElement getAsJsonElement()
    {
        return new JsonPrimitive(this.getValue());
    }
}
