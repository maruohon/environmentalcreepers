package fi.dy.masa.environmentalcreepers.config.options;

import com.google.gson.JsonElement;

public interface IConfigBase
{
    /**
     * Returns the config name to display in the config GUIs
     * @return the name of this config
     */
    String getName();

    /**
     * Returns the comment displayed when hovering over the config name in the config GUI.
     * Newlines can be added with "\n". Can be null if there is no comment for this config.
     * @return the comment, or null if no comment has been set
     */
    String getComment();

    /**
     * Returns the "pretty name" for this config.
     * This is used in the possible toggle messages.
     * @return
     */
    default String getPrettyName()
    {
        return this.getName();
    }

    /**
     * Resets the config value back to the default value
     */
    void resetToDefault();

    /**
     * 
     * Set the value of this config option from a JSON element (is possible)
     * @param element
     * @param configName
     */
    void setValueFromJsonElement(JsonElement element, String configName);

    /**
     * Return the value of this config option as a JSON element, for saving into a config file.
     * @return
     */
    JsonElement getAsJsonElement();
}
