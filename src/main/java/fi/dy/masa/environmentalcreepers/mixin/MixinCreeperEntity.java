package fi.dy.masa.environmentalcreepers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.mob.CreeperEntity;
import fi.dy.masa.environmentalcreepers.config.Configs;

@Mixin(CreeperEntity.class)
public class MixinCreeperEntity
{
    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void disableExplosion(CallbackInfo ci)
    {
        if (Configs.Toggles.DISABLE_CREEPER_EXPLOSION_ENTIRELY.getValue())
        {
            ci.cancel();
        }
    }
}
