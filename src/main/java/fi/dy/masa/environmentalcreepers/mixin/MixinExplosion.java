package fi.dy.masa.environmentalcreepers.mixin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.explosion.ExplosionBehavior;

import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.util.ExplosionUtils;

@Mixin(Explosion.class)
public abstract class MixinExplosion
{
    @Shadow @Final private World world;
    @Shadow @Final private Entity entity;
    @Shadow @Final private ObjectArrayList<BlockPos> affectedBlocks;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final @Mutable private float power;

    @Shadow @org.jetbrains.annotations.Nullable public abstract LivingEntity getCausingEntity();

    @Shadow @Final private DestructionType destructionType;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/particle/ParticleEffect;Lnet/minecraft/sound/SoundEvent;)V",
            at = @At("RETURN"))
    private void envc_modifyExplosionSize(World world, Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, ParticleEffect particle, ParticleEffect emitterParticle, SoundEvent soundEvent, CallbackInfo ci)
    {
        if (entity instanceof CreeperEntity && Configs.Toggles.MODIFY_CREEPER_EXPLOSION_STRENGTH.getValue())
        {
            if (entity.getDataTracker().get(IMixinCreeperEntity.envc_getCharged()))
            {
                this.power = Configs.Generic.CREEPER_EXPLOSION_STRENGTH_CHARGED.getFloatValue();
            }
            else
            {
                this.power = Configs.Generic.CREEPER_EXPLOSION_STRENGTH_NORMAL.getFloatValue();
            }
        }
    }

    @Inject(method = "affectWorld", at = @At("HEAD"), cancellable = true)
    private void envc_disableExplosionBlockDamageOrCompletely(CallbackInfo ci)
    {
        if (this.world.isClient == false)
        {
            EnvironmentalCreepers.logInfo(this::envc_printExplosionInfo);
        }

        if (Configs.Toggles.DISABLE_ALL_EXPLOSIONS.getValue())
        {
            EnvironmentalCreepers.logInfo("MixinExplosion.envc_disableExplosionBlockDamageOrCompletely(), type: '{}'", (this.entity instanceof CreeperEntity) ? "Creeper" : "Other");
            ci.cancel();
        }
        else if (this.entity instanceof CreeperEntity)
        {
            if (Configs.Toggles.DISABLE_CREEPER_EXPLOSION_BLOCK_DAMAGE.getValue() ||
                (Configs.Toggles.CREEPER_ALTITUDE_CONDITION.getValue() &&
                (this.y < Configs.Generic.CREEPER_ALTITUDE_DAMAGE_MIN_Y.getValue() ||
                 this.y > Configs.Generic.CREEPER_ALTITUDE_DAMAGE_MAX_Y.getValue())))
            {
                EnvironmentalCreepers.logInfo("MixinExplosion.envc_disableExplosionBlockDamageOrCompletely: clearAffectedBlockPositions(), type: 'Creeper'");
                this.affectedBlocks.clear();
            }

            if (Configs.Toggles.CREEPER_EXPLOSION_CHAIN_REACTION.getValue())
            {
                ExplosionUtils.causeCreeperChainReaction(this.world, new Vec3d(this.x, this.y, this.z));
            }
        }
        else if (Configs.Toggles.DISABLE_OTHER_EXPLOSION_BLOCK_DAMAGE.getValue() && (this.entity instanceof CreeperEntity) == false)
        {
            EnvironmentalCreepers.logInfo("MixinExplosion.envc_disableExplosionBlockDamageOrCompletely: clearAffectedBlockPositions(), type: 'Other'");
            this.affectedBlocks.clear();
        }
    }

    @Inject(method = "affectWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/Util;shuffle(Ljava/util/List;Lnet/minecraft/util/math/random/Random;)V"))
    private void envc_preventItemDrops(boolean particles, CallbackInfo ci)
    {
        if (this.entity instanceof CreeperEntity)
        {
            if (Configs.Toggles.MODIFY_CREEPER_EXPLOSION_DROP_CHANCE.getValue() &&
                Configs.Generic.CREEPER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue() == 0.0f)
            {
                this.envc_removeBlocks();
            }
        }
        else
        {
            if (Configs.Toggles.MODIFY_OTHER_EXPLOSION_DROP_CHANCE.getValue() &&
                Configs.Generic.OTHER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue() == 0.0f)
            {
                this.envc_removeBlocks();
            }
        }
    }

    @Inject(method = "getDestructionType", at = @At("HEAD"), cancellable = true)
    private void envc_overrideDestructionType(CallbackInfoReturnable<DestructionType> cir)
    {
        if (this.destructionType != DestructionType.DESTROY_WITH_DECAY)
        {
            return;
        }

        if (this.entity instanceof CreeperEntity)
        {
            if (Configs.Toggles.MODIFY_CREEPER_EXPLOSION_DROP_CHANCE.getValue() &&
                Configs.Generic.CREEPER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue() >= 1.0f)
            {
                cir.setReturnValue(DestructionType.DESTROY);
            }
        }
        else
        {
            if (Configs.Toggles.MODIFY_OTHER_EXPLOSION_DROP_CHANCE.getValue() &&
                Configs.Generic.OTHER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue() >= 1.0f)
            {
                cir.setReturnValue(DestructionType.DESTROY);
            }
        }
    }

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"), cancellable = true)
    private void envc_disableExplosionCompletely1(CallbackInfo ci)
    {
        if (Configs.Toggles.DISABLE_ALL_EXPLOSIONS.getValue())
        {
            EnvironmentalCreepers.logInfo("MixinExplosion.disableExplosionCompletely1(), type: '{}'", (this.entity instanceof CreeperEntity) ? "Creeper" : "Other");
            ci.cancel();
        }
    }

    @ModifyVariable(method = "collectBlocksAndDamageEntities", ordinal = 0,
                    slice = @Slice(from = @At(value = "INVOKE",
                                              target = "Lnet/minecraft/world/World;getOtherEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;)Ljava/util/List;"),
                                   to = @At(value = "INVOKE",
                                            target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion(Lnet/minecraft/world/explosion/Explosion;)Z")),
                    at = @At(value = "INVOKE",
                             target = "Lnet/minecraft/util/math/Vec3d;<init>(DDD)V"))
    private List<Entity> envc_disableExplosionEntityDamage(List<Entity> list)
    {
        Set<Entity> immune = new HashSet<>();

        for (Entity e : list)
        {
            if (this.envc_isImmuneToExplosion(e))
            {
                immune.add(e);
            }
        }

        if (immune.isEmpty() == false)
        {
            EnvironmentalCreepers.logInfo("MixinExplosion.disableExplosionEntityDamage(), type: '{}'", (this.entity instanceof CreeperEntity) ? "Creeper" : "Other");
            list.removeAll(immune);
        }

        return list;
    }

    private boolean envc_isImmuneToExplosion(Entity entity)
    {
        Configs.ListType type = Configs.Lists.entityClassListType;

        if (this.entity instanceof CreeperEntity)
        {
            if ((Configs.Toggles.DISABLE_CREEPER_EXPLOSION_ENTITY_DAMAGE.getValue() ||
                (Configs.Toggles.DISABLE_CREEPER_EXPLOSION_ITEM_DAMAGE.getValue() && entity instanceof ItemEntity)) &&
                (type == Configs.ListType.NONE ||
                 (type == Configs.ListType.WHITELIST && Configs.EXPLOSION_ENTITY_WHITELIST.contains(entity.getClass())) ||
                 (type == Configs.ListType.BLACKLIST && Configs.EXPLOSION_ENTITY_BLACKLIST.contains(entity.getClass()) == false)))
            {
                return true;
            }
        }
        else
        {
            if ((Configs.Toggles.DISABLE_OTHER_EXPLOSION_ENTITY_DAMAGE.getValue() ||
                (Configs.Toggles.DISABLE_OTHER_EXPLOSION_ITEM_DAMAGE.getValue() && entity instanceof ItemEntity)) &&
                (type == Configs.ListType.NONE ||
                 (type == Configs.ListType.WHITELIST && Configs.EXPLOSION_ENTITY_WHITELIST.contains(entity.getClass())) ||
                 (type == Configs.ListType.BLACKLIST && Configs.EXPLOSION_ENTITY_BLACKLIST.contains(entity.getClass()) == false)))
            {
                return true;
            }
        }

        return entity.isImmuneToExplosion((Explosion)(Object) this);
    }

    private void envc_removeBlocks()
    {
        BlockState air = Blocks.AIR.getDefaultState();

        this.world.getProfiler().push("explosion_blocks");

        for (BlockPos pos : this.affectedBlocks)
        {
            BlockState state = this.world.getBlockState(pos);
            this.world.setBlockState(pos, air, Block.NOTIFY_ALL);
            state.getBlock().onDestroyedByExplosion(this.world, pos, (Explosion) (Object) this);
        }

        this.world.getProfiler().pop();

        this.affectedBlocks.clear();
    }

    private String envc_printExplosionInfo()
    {
        return String.format("Explosion @ [%.5f, %.5f, %.5f], power: %.2f - type: '%s' - explosion class: '%s', placer: '%s'",
                             this.x, this.y, this.z, this.power,
                             (this.entity instanceof CreeperEntity) ? "Creeper" : "Other",
                             this.getClass().getName(),
                             this.getCausingEntity() != null ? this.getCausingEntity().getClass().getName() : "<null>");
    }
}
