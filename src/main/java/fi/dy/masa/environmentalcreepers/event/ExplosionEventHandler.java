package fi.dy.masa.environmentalcreepers.event;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.config.Configs.ListType;

public class ExplosionEventHandler
{
    private final Field fieldExplosionSize;
    private final Field fieldExplosionMode;
    private final Field fieldExploder;
    private final Field fieldCausesFire;

    public ExplosionEventHandler()
    {
        this.fieldExplosionSize = ObfuscationReflectionHelper.findField(Explosion.class, "f_46017_"); // radius (mcp: size)
        this.fieldExplosionMode = ObfuscationReflectionHelper.findField(Explosion.class, "f_46010_"); // blockInteraction (mcp: mode)
        this.fieldExploder      = ObfuscationReflectionHelper.findField(Explosion.class, "f_46016_"); // source (mcp: exploder)
        this.fieldCausesFire    = ObfuscationReflectionHelper.findField(Explosion.class, "f_46009_"); // fire (mcp: causesFire)
    }

    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event)
    {
        Explosion explosion = event.getExplosion();

        if ((Configs.Lists.explosionClassListType == ListType.WHITELIST && Configs.EXPLOSION_CLASS_WHITELIST.contains(explosion.getClass()) == false) ||
            (Configs.Lists.explosionClassListType == ListType.BLACKLIST && Configs.EXPLOSION_CLASS_BLACKLIST.contains(explosion.getClass())))
        {
            if (Configs.Generic.verboseLogging)
            {
                EnvironmentalCreepers.logInfo("Explosion (blocked by white- or blacklist): class: {}, position: {}",
                        explosion.getClass().getName(), explosion.getPosition());
            }

            return;
        }

        if (Configs.Generic.verboseLogging)
        {
            EnvironmentalCreepers.logInfo("Explosion: class: {}, position: {}", explosion.getClass().getName(), explosion.getPosition());
        }

        if (explosion.getSourceMob() instanceof Creeper)
        {
            if (Configs.Toggles.modifyCreeperExplosionDropChance && Configs.Toggles.disableCreeperExplosionBlockDamage == false)
            {
                this.replaceExplosion(event, true);
            }
        }
        else
        {
            if (Configs.Toggles.modifyOtherExplosionDropChance && Configs.Toggles.disableOtherExplosionBlockDamage == false)
            {
                this.replaceExplosion(event, false);
            }
        }
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event)
    {
        Explosion explosion = event.getExplosion();

        if ((Configs.Lists.explosionClassListType == ListType.WHITELIST && Configs.EXPLOSION_CLASS_WHITELIST.contains(explosion.getClass()) == false) ||
            (Configs.Lists.explosionClassListType == ListType.BLACKLIST && Configs.EXPLOSION_CLASS_BLACKLIST.contains(explosion.getClass())))
        {
            return;
        }

        if (explosion.getSourceMob() instanceof Creeper)
        {
            if (Configs.Toggles.disableCreeperExplosionItemDamage)
            {
                this.removeItemEntities(event.getAffectedEntities(), true);
            }

            if (Configs.Toggles.disableCreeperExplosionBlockDamage ||
                (Configs.Toggles.enableCreeperAltitudeCondition &&
                    (explosion.getPosition().y < Configs.Generic.creeperAltitudeDamageMinY ||
                     explosion.getPosition().y > Configs.Generic.creeperAltitudeDamageMaxY)))
            {
                EnvironmentalCreepers.logInfo("ExplosionEventHandler - clearAffectedBlockPositions() - Type: 'Creeper'");
                explosion.clearToBlow();
            }

            if (Configs.Toggles.enableCreeperExplosionChainReaction)
            {
                this.causeCreeperChainReaction(event.getLevel(), explosion.getPosition());
            }
        }
        else
        {
            if (Configs.Toggles.disableOtherExplosionItemDamage)
            {
                this.removeItemEntities(event.getAffectedEntities(), false);
            }

            if (Configs.Toggles.disableOtherExplosionBlockDamage)
            {
                EnvironmentalCreepers.logInfo("ExplosionEventHandler - clearAffectedBlockPositions() - Type: 'Other'");
                explosion.clearToBlow();
            }
        }
    }

    private void removeItemEntities(List<Entity> list, boolean isCreeper)
    {
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.removeItemEntities() - Type: '{}'", isCreeper ? "Creeper" : "Other");
        Iterator<Entity> iter = list.iterator();
        ListType type = Configs.Lists.entityClassListType;

        while (iter.hasNext())
        {
            Entity entity = iter.next();

            if (entity instanceof ItemEntity)
            {
                if ( type == ListType.NONE ||
                    (type == ListType.WHITELIST && Configs.EXPLOSION_ENTITY_WHITELIST.contains(entity.getClass())) ||
                    (type == ListType.BLACKLIST && Configs.EXPLOSION_ENTITY_BLACKLIST.contains(entity.getClass()) == false))
                {
                    iter.remove();
                }
            }
        }
    }

    private void replaceExplosion(ExplosionEvent.Start event, boolean isCreeper)
    {
        Level world = event.getLevel();
        Explosion explosion = event.getExplosion();

        if (Configs.Generic.verboseLogging)
        {
            EnvironmentalCreepers.logInfo("Replacing the explosion for type '{}' (class: {})",
                    isCreeper ? "Creeper" : "Other", explosion.getClass().getName());
        }

        try
        {
            boolean causesFire = this.fieldCausesFire.getBoolean(explosion);
            Explosion.BlockInteraction mode = (Explosion.BlockInteraction) this.fieldExplosionMode.get(explosion);
            float explosionSize;

            if (isCreeper && Configs.Toggles.modifyCreeperExplosionStrength)
            {
                if (((Creeper) explosion.getSourceMob()).isPowered())
                {
                    explosionSize = (float) Configs.Generic.creeperExplosionStrengthCharged;
                }
                else
                {
                    explosionSize = (float) Configs.Generic.creeperExplosionStrengthNormal;
                }

                this.fieldExplosionSize.setFloat(explosion, explosionSize);
            }
            else
            {
                explosionSize = this.fieldExplosionSize.getFloat(explosion);
            }

            explosion.explode();

            if (world instanceof ServerLevel serverWorld)
            {
                Vec3 pos = explosion.getPosition();

                if (isCreeper && Configs.Toggles.enableCreeperAltitudeCondition &&
                    (pos.y < Configs.Generic.creeperAltitudeDamageMinY ||
                     pos.y > Configs.Generic.creeperAltitudeDamageMaxY))
                {
                    mode = Explosion.BlockInteraction.NONE;
                }

                if (mode == Explosion.BlockInteraction.NONE)
                {
                    explosion.clearToBlow();
                }

                this.finalizeExplosion(world, explosion, mode, false, causesFire, explosionSize, isCreeper);

                for (ServerPlayer player : serverWorld.players())
                {
                    if (player.distanceToSqr(pos.x, pos.y, pos.z) < 4096.0D)
                    {
                        player.connection.send(new ClientboundExplodePacket(pos.x, pos.y, pos.z, explosionSize,
                                explosion.getToBlow(), explosion.getHitPlayers().get(player)));
                    }
                }
            }
            else
            {
                this.finalizeExplosion(world, explosion, mode, true, causesFire, explosionSize, isCreeper);
            }
        }
        catch (IllegalAccessException e)
        {
            EnvironmentalCreepers.logger.error("IllegalAccessException while reflecting explosion fields", e);
        }
        catch (IllegalArgumentException e)
        {
            EnvironmentalCreepers.logger.error("IllegalArgumentException while reflecting explosion fields", e);
        }

        event.setCanceled(true);
    }

    private void finalizeExplosion(Level world, Explosion explosion, Explosion.BlockInteraction mode, boolean spawnParticles, boolean causesFire, float explosionSize, boolean isCreeper)
    {
        Vec3 posVec = explosion.getPosition();
        RandomSource rand = world.random;
        boolean breaksBlock = mode != Explosion.BlockInteraction.NONE &&
                (isCreeper ? Configs.Toggles.disableCreeperExplosionBlockDamage == false :
                             Configs.Toggles.disableOtherExplosionBlockDamage == false);

        world.playSound(null, posVec.x, posVec.y, posVec.z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);

        if (spawnParticles)
        {
            if (explosionSize >= 2.0F && breaksBlock)
            {
                world.addParticle(ParticleTypes.EXPLOSION_EMITTER, posVec.x, posVec.y, posVec.z, 1.0D, 0.0D, 0.0D);
            }
            else
            {
                world.addParticle(ParticleTypes.EXPLOSION, posVec.x, posVec.y, posVec.z, 1.0D, 0.0D, 0.0D);
            }
        }

        float dropChance = (float) (isCreeper ? Configs.Generic.creeperExplosionBlockDropChance : Configs.Generic.otherExplosionBlockDropChance);
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.doExplosionB() - Type: '{}', drop chance: {}", isCreeper ? "Creeper" : "Other", dropChance);

        if (breaksBlock)
        {
            ObjectArrayList<Pair<ItemStack, BlockPos>> drops = new ObjectArrayList<>();
            shuffle(explosion.getToBlow(), world.random);

            for (BlockPos pos : explosion.getToBlow())
            {
                BlockState state = world.getBlockState(pos);

                if (state.isAir() == false)
                {
                    world.getProfiler().push("explosion_blocks");

                    if ((world instanceof ServerLevel) && state.canDropFromExplosion(world, pos, explosion))
                    {
                        // The corresponding modify explosion chance config option is going
                        // to be true when this method is called in the first place
                        if (dropChance > 0)
                        {
                            ServerLevel serverWorld = (ServerLevel) world;
                            BlockEntity te = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
                            Entity exploder = this.getExploder(explosion);

                            LootContext.Builder builder = (new LootContext.Builder(serverWorld))
                                    .withRandom(rand)
                                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                                    .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, te)
                                    .withOptionalParameter(LootContextParams.THIS_ENTITY, exploder);

                            if (dropChance < 1.0f)
                            {
                                // See SurvivesExplosion loot condition
                                float size = 1.0f / dropChance;
                                builder.withParameter(LootContextParams.EXPLOSION_RADIUS, size);
                            }

                            state.getDrops(builder).forEach((stack) -> mergeStackToPreviousDrops(drops, stack, pos));
                        }
                    }

                    state.onBlockExploded(world, pos, explosion);
                    world.getProfiler().pop();
                }
            }

            for (Pair<ItemStack, BlockPos> pair : drops)
            {
                Block.popResource(world, pair.getSecond(), pair.getFirst());
            }
        }

        if (causesFire)
        {
            BlockPos.MutableBlockPos posMutable = new BlockPos.MutableBlockPos();

            for (BlockPos pos : explosion.getToBlow())
            {
                if (rand.nextInt(3) == 0 && world.getBlockState(pos).isAir())
                {
                    posMutable.setWithOffset(pos, Direction.DOWN);

                    if (world.getBlockState(posMutable).isSolidRender(world, posMutable))
                    {
                        world.setBlockAndUpdate(pos, BaseFireBlock.getState(world, pos));
                    }
                }
            }
        }
    }

    @Nullable
    private Entity getExploder(Explosion explosion)
    {
        try
        {
            return (Entity) this.fieldExploder.get(explosion);
        }
        catch (Exception ignore) {}

        return null;
    }

    private static void mergeStackToPreviousDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> drops, ItemStack stack, BlockPos pos)
    {
        final int size = drops.size();

        for (int i = 0; i < size; ++i)
        {
            Pair<ItemStack, BlockPos> pair = drops.get(i);
            ItemStack stackTmp = pair.getFirst();

            if (ItemEntity.areMergable(stackTmp, stack))
            {
                ItemStack stackNew = ItemEntity.merge(stackTmp, stack, 16);
                drops.set(i, Pair.of(stackNew, pair.getSecond()));

                if (stack.isEmpty())
                {
                    return;
                }
           }
        }

        drops.add(Pair.of(stack, pos));
    }

    private void causeCreeperChainReaction(Level world, Vec3 explosionPos)
    {
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.causeCreeperChainReaction() - Explosion Position: '{}'", explosionPos);

        double r = Configs.Generic.creeperChainReactionMaxDistance;
        double rSq = r * r;
        AABB bb = new AABB(explosionPos.x - r, explosionPos.y - r, explosionPos.z - r,
                           explosionPos.x + r, explosionPos.y + r, explosionPos.z + r);
        List<Creeper> list = world.getEntitiesOfClass(Creeper.class, bb, (ent) -> ent.getHealth() > 0);

        for (Creeper creeper : list)
        {
            if (creeper.isIgnited() == false && world.random.nextFloat() < Configs.Generic.creeperChainReactionChance &&
                creeper.distanceToSqr(explosionPos.x, explosionPos.y, explosionPos.z) <= rSq)
            {
                EnvironmentalCreepers.logInfo("ExplosionEventHandler.causeCreeperChainReaction() - Igniting Creeper: '{}'", creeper.toString());
                creeper.ignite();
            }
        }
    }

    private static <T> void shuffle(List<T> list, RandomSource rand) {
        int i = list.size();

        for (int j = i; j > 1; --j)
        {
            int k = rand.nextInt(j);
            list.set(j - 1, list.set(k, list.get(j - 1)));
        }

    }
}
