package fi.dy.masa.environmentalcreepers.event;

import java.lang.reflect.Field;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class CreeperEventHandler
{
    private static final Field field_Creeper_timeSinceIgnited = ReflectionHelper.findField(EntityCreeper.class, "field_70833_d", "timeSinceIgnited");
    private static final Field field_Creeper_fuseTime = ReflectionHelper.findField(EntityCreeper.class, "field_82225_f", "fuseTime");
    private static CreeperEventHandler instance = new CreeperEventHandler();
    private boolean registered;

    public static CreeperEventHandler getInstance()
    {
        return instance;
    }

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
        if (Configs.disableCreeperExplosionCompletely && event.getEntity() instanceof EntityCreeper)
        {
            EntityCreeper creeper = (EntityCreeper) event.getEntity();
            int state = creeper.getCreeperState();

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
