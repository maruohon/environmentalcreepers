package fi.dy.masa.environmentalcreepers.proxy;

import net.minecraftforge.common.MinecraftForge;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class ClientProxy extends ServerProxy
{
    @Override
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(new Configs());
    }
}
