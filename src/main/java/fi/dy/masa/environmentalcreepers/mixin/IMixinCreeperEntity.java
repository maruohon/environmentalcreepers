package fi.dy.masa.environmentalcreepers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;

@Mixin(CreeperEntity.class)
public interface IMixinCreeperEntity
{
    @Accessor("CHARGED")
    static TrackedData<Boolean> envc_getCharged() { return null; }
}
