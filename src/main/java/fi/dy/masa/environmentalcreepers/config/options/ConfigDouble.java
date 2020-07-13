package fi.dy.masa.environmentalcreepers.config.options;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.util.math.MathHelper;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;

public class ConfigDouble extends ConfigBase
{
    private final double minValue;
    private final double maxValue;
    private final double defaultValue;
    private double value;

    public ConfigDouble(String name, double defaultValue, String comment)
    {
        this(name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE, comment);
    }

    public ConfigDouble(String name, double defaultValue, double minValue, double maxValue, String comment)
    {
        this(name, defaultValue, minValue, maxValue, false, comment);
    }

    public ConfigDouble(String name, double defaultValue, double minValue, double maxValue, boolean useSlider, String comment)
    {
        super(name, comment);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public double getValue()
    {
        return this.value;
    }

    public float getFloatValue()
    {
        return (float) this.value;
    }

    public double getDefaultValue()
    {
        return this.defaultValue;
    }

    public void setValue(double value)
    {
        this.value = this.getClampedValue(value);
    }

    @Override
    public void resetToDefault()
    {
        this.value = this.defaultValue;
    }

    protected double getClampedValue(double value)
    {
        return MathHelper.clamp(value, this.minValue, this.maxValue);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element, String configName)
    {
        try
        {
            if (element.isJsonPrimitive())
            {
                this.value = this.getClampedValue(element.getAsDouble());
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
