package fi.dy.masa.environmentalcreepers.event;

import java.lang.reflect.Field;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class CreeperEventHandler
{
    private static final Field field_Creeper_timeSinceIgnited = ObfuscationReflectionHelper.findField(Creeper.class, "f_32270_"); // swell (mcp: timeSinceIgnited)
    private static final Field field_Creeper_fuseTime = ObfuscationReflectionHelper.findField(Creeper.class, "f_32271_"); // maxSwell (mcp: fuseTime)

    public static final CreeperEventHandler INSTANCE = new CreeperEventHandler();

    private boolean registered;

    public void register()
    {
        if (this.registered == false)
        {
            EnvironmentalCreepers.logInfo("Registering CreeperEventHandler");
            MinecraftForge.EVENT_BUS.register(this);
            this.registered = true;
        }
    }

    public void unregister()
    {
        if (this.registered)
        {
            EnvironmentalCreepers.logInfo("Unregistering CreeperEventHandler");
            MinecraftForge.EVENT_BUS.unregister(this);
            this.registered = false;
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent event)
    {
        if (Configs.Toggles.disableCreeperExplosionCompletely &&
            event.getEntity() instanceof Creeper creeper)
        {
            int state = creeper.getSwellDir();

            if (state > 0)
            {
                try
                {
                    int timeSinceIgnited = (int) field_Creeper_timeSinceIgnited.get(creeper);
                    int fuseTime = (int) field_Creeper_fuseTime.get(creeper);

                    if (timeSinceIgnited >= (fuseTime - state - 1))
                    {
                        field_Creeper_timeSinceIgnited.set(creeper, fuseTime - state - 1);
                    }
                }
                catch (Exception e)
                {
                    EnvironmentalCreepers.logger.warn("CreeperEventHandler.onLivingUpdate(): Exception while trying to reflect Creeper fields");
                }
            }
        }
    }
}
