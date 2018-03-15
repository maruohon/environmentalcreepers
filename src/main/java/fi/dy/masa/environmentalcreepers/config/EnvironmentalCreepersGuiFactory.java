package fi.dy.masa.environmentalcreepers.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.DefaultGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import fi.dy.masa.environmentalcreepers.Reference;

public class EnvironmentalCreepersGuiFactory extends DefaultGuiFactory
{
    public EnvironmentalCreepersGuiFactory()
    {
        super(Reference.MOD_ID, GuiConfig.getAbridgedConfigPath(Configs.getConfig().getConfigFile().toString()));
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parent)
    {
        return new GuiConfig(parent, getConfigElements(), this.modid, false, false, GuiConfig.getAbridgedConfigPath(Configs.getConfig().getConfigFile().toString()));
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> configElements = new ArrayList<IConfigElement>();

        configElements.add(new ConfigElement(Configs.getConfig().getCategory(Configs.CATEGORY_GENERIC)));
        configElements.add(new ConfigElement(Configs.getConfig().getCategory(Configs.CATEGORY_LISTS)));

        return configElements;
    }
}
