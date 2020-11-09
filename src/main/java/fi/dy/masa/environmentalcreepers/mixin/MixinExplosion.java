package fi.dy.masa.environmentalcreepers.mixin;

import java.util.List;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.util.ExplosionUtils;

@Mixin(Explosion.class)
public class MixinExplosion
{
    @Shadow @Final private World world;
    @Shadow @Final private Entity entity;
    @Shadow @Final private List<BlockPos> affectedBlocks;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final @Mutable private float power;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)V",
            at = @At("RETURN"))
    private void modifyExplosionSize(World world, @Nullable Entity entity, @Nullable DamageSource damageSource,
                                     @Nullable ExplosionBehavior explosionBehavior, double x, double y, double z, float power, boolean fire,
                                     Explosion.DestructionType destructionType, CallbackInfo ci)
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

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"), cancellable = true)
    private void disableExplosionCompletely1(CallbackInfo ci)
    {
        if (Configs.Toggles.DISABLE_ALL_EXPLOSIONS.getValue())
        {
            EnvironmentalCreepers.logInfo("MixinExplosion.disableExplosionCompletely1(), type: '{}'", (this.entity instanceof CreeperEntity) ? "Creeper" : "Other");
            ci.cancel();
        }
    }

    @Inject(method = "affectWorld", at = @At("HEAD"), cancellable = true)
    private void disableExplosionCompletely2(boolean particles, CallbackInfo ci)
    {
        if (this.world.isClient == false)
        {
            EnvironmentalCreepers.logInfo(this::printExplosionPosition);
        }

        if (Configs.Toggles.DISABLE_ALL_EXPLOSIONS.getValue())
        {
            EnvironmentalCreepers.logInfo("MixinExplosion.disableExplosionCompletely2(), type: '{}'", (this.entity instanceof CreeperEntity) ? "Creeper" : "Other");
            ci.cancel();
        }
    }

    @Inject(method = "affectWorld", at = @At("HEAD"), cancellable = true)
    private void disableExplosionBlockDamage(CallbackInfo ci)
    {
        if (this.entity instanceof CreeperEntity)
        {
            if (Configs.Toggles.DISABLE_CREEPER_EXPLOSION_BLOCK_DAMAGE.getValue() ||
                (Configs.Toggles.CREEPER_ALTITUDE_CONDITION.getValue() &&
                (this.y < Configs.Generic.CREEPER_ALTITUDE_DAMAGE_MIN_Y.getValue() ||
                 this.y > Configs.Generic.CREEPER_ALTITUDE_DAMAGE_MAX_Y.getValue())))
            {
                EnvironmentalCreepers.logInfo("MixinExplosion: clearAffectedBlockPositions(), type: 'Creeper'");
                this.affectedBlocks.clear();
            }

            if (Configs.Toggles.CREEPER_EXPLOSION_CHAIN_REACTION.getValue())
            {
                ExplosionUtils.causeCreeperChainReaction(this.world, new Vec3d(this.x, this.y, this.z));
            }
        }
        else if (Configs.Toggles.DISABLE_OTHER_EXPLOSION_BLOCK_DAMAGE.getValue() && (this.entity instanceof CreeperEntity) == false)
        {
            EnvironmentalCreepers.logInfo("MixinExplosion: clearAffectedBlockPositions(), type: 'Other'");
            this.affectedBlocks.clear();
        }
    }

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion()Z"))
    private boolean disableExplosionEntityDamage(Entity entity)
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

        return entity.isImmuneToExplosion();
    }

    @Redirect(method = "affectWorld",
              slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/explosion/Explosion$DestructionType;DESTROY:Lnet/minecraft/world/explosion/Explosion$DestructionType;")),
              at = @At(value = "INVOKE",
              target = "Lnet/minecraft/loot/context/LootContext$Builder;parameter(Lnet/minecraft/loot/context/LootContextParameter;Ljava/lang/Object;)Lnet/minecraft/loot/context/LootContext$Builder;"))
    private <T> LootContext.Builder modifyDropChance(LootContext.Builder builder, LootContextParameter<T> key, T value)
    {
        if (this.entity instanceof CreeperEntity)
        {
            if (Configs.Toggles.MODIFY_CREEPER_EXPLOSION_DROP_CHANCE.getValue())
            {
                float dropChance = Configs.Generic.CREEPER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue();

                if (dropChance > 0.0F && dropChance < 1.0f)
                {
                    // See SurvivesExplosion loot condition
                    float size = 1.0f / dropChance;
                    return builder.parameter(LootContextParameters.EXPLOSION_RADIUS, size);
                }
            }
        }
        else
        {
            if (Configs.Toggles.MODIFY_OTHER_EXPLOSION_DROP_CHANCE.getValue())
            {
                float dropChance = Configs.Generic.OTHER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue();

                if (dropChance > 0.0F && dropChance < 1.0f)
                {
                    // See SurvivesExplosion loot condition
                    float size = 1.0f / dropChance;
                    return builder.parameter(LootContextParameters.EXPLOSION_RADIUS, size);
                }
            }
        }

        return builder;
    }

    @Inject(method = "affectWorld", at = @At(value = "INVOKE",
            target = "Ljava/util/Collections;shuffle(Ljava/util/List;Ljava/util/Random;)V"))
    private void preventItemDrops(boolean particles, CallbackInfo ci)
    {
        if (this.entity instanceof CreeperEntity)
        {
            if (Configs.Toggles.MODIFY_CREEPER_EXPLOSION_DROP_CHANCE.getValue() &&
                Configs.Generic.CREEPER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue() == 0.0f)
            {
                this.removeBlocks();
            }
        }
        else
        {
            if (Configs.Toggles.MODIFY_OTHER_EXPLOSION_DROP_CHANCE.getValue() &&
                Configs.Generic.OTHER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue() == 0.0f)
            {
                this.removeBlocks();
            }
        }
    }

    private void removeBlocks()
    {
        BlockState air = Blocks.AIR.getDefaultState();

        this.world.getProfiler().push("explosion_blocks");

        for (BlockPos pos : this.affectedBlocks)
        {
            BlockState state = this.world.getBlockState(pos);

            if (state.isAir() == false)
            {
                this.world.setBlockState(pos, air, 3);
                state.getBlock().onDestroyedByExplosion(this.world, pos, (Explosion) (Object) this);
            }
        }

        this.world.getProfiler().pop();

        this.affectedBlocks.clear();
    }

    private String printExplosionPosition()
    {
        return String.format("Explosion.affectWorld() @ [%.5f, %.5f, %.5f], power: %.2f - type: '%s'", this.x, this.y, this.z, this.power, (this.entity instanceof CreeperEntity) ? "Creeper" : "Other");
    }
}
