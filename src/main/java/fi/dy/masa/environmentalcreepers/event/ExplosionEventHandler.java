package fi.dy.masa.environmentalcreepers.event;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;
import fi.dy.masa.environmentalcreepers.config.Configs.ListType;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ExplosionEventHandler
{
    private final Field fieldExplosionSize;
    private final Field fieldExplosionMode;
    private final Field fieldCausesFire;

    public ExplosionEventHandler()
    {
        this.fieldExplosionSize = ObfuscationReflectionHelper.findField(Explosion.class, "field_77280_f"); // size
        this.fieldExplosionMode = ObfuscationReflectionHelper.findField(Explosion.class, "field_222260_b"); // mode
        this.fieldCausesFire    = ObfuscationReflectionHelper.findField(Explosion.class, "field_77286_a"); // causesFire
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

        if (explosion.getExplosivePlacedBy() instanceof CreeperEntity)
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

        if (explosion.getExplosivePlacedBy() instanceof CreeperEntity)
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
                explosion.clearAffectedBlockPositions();
            }

            if (Configs.Toggles.enableCreeperExplosionChainReaction)
            {
                this.causeCreeperChainReaction(event.getWorld(), explosion.getPosition());
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
                explosion.clearAffectedBlockPositions();
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
        World world = event.getWorld();
        Explosion explosion = event.getExplosion();

        if (Configs.Generic.verboseLogging)
        {
            EnvironmentalCreepers.logInfo("Replacing the explosion for type '{}' (class: {})",
                    isCreeper ? "Creeper" : "Other", explosion.getClass().getName());
        }

        try
        {
            boolean causesFire = this.fieldCausesFire.getBoolean(explosion);
            Explosion.Mode mode = (Explosion.Mode) this.fieldExplosionMode.get(explosion);
            float explosionSize;

            if (isCreeper && Configs.Toggles.modifyCreeperExplosionStrength)
            {
                if (((CreeperEntity) explosion.getExplosivePlacedBy()).getPowered())
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

            explosion.doExplosionA();

            if (world instanceof ServerWorld)
            {
                ServerWorld serverWorld = (ServerWorld) world;
                Vec3d pos = explosion.getPosition();

                if (isCreeper && Configs.Toggles.enableCreeperAltitudeCondition &&
                        (pos.y < Configs.Generic.creeperAltitudeDamageMinY ||
                         pos.y > Configs.Generic.creeperAltitudeDamageMaxY))
                {
                    mode = Explosion.Mode.NONE;
                }

                if (mode == Explosion.Mode.NONE)
                {
                    explosion.clearAffectedBlockPositions();
                }

                this.doExplosionB(world, explosion, mode, false, causesFire, explosionSize, isCreeper);

                for (PlayerEntity player : serverWorld.getPlayers())
                {
                    if (player.getDistanceSq(pos.x, pos.y, pos.z) < 4096.0D)
                    {
                        ((ServerPlayerEntity) player).connection.sendPacket(
                                new SExplosionPacket(pos.x, pos.y, pos.z, explosionSize,
                                        explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(player)));
                    }
                }
            }
            else
            {
                this.doExplosionB(world, explosion, mode, true, causesFire, explosionSize, isCreeper);
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

    private void doExplosionB(World world, Explosion explosion, Explosion.Mode mode, boolean spawnParticles, boolean causesFire, float explosionSize, boolean isCreeper)
    {
        Vec3d posVec = explosion.getPosition();
        Random rand = world.rand;
        boolean breaksBlock = mode != Explosion.Mode.NONE &&
                (isCreeper ? Configs.Toggles.disableCreeperExplosionBlockDamage == false :
                             Configs.Toggles.disableOtherExplosionBlockDamage == false);

        world.playSound(null, posVec.x, posVec.y, posVec.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);

        if (explosionSize >= 2.0F && breaksBlock)
        {
            world.addParticle(ParticleTypes.EXPLOSION_EMITTER, posVec.x, posVec.y, posVec.z, 1.0D, 0.0D, 0.0D);
        }
        else
        {
            world.addParticle(ParticleTypes.EXPLOSION, posVec.x, posVec.y, posVec.z, 1.0D, 0.0D, 0.0D);
        }

        float dropChance = (float) (isCreeper ? Configs.Generic.creeperExplosionBlockDropChance : Configs.Generic.otherExplosionBlockDropChance);
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.doExplosionB() - Type: '{}', drop chance: {}", isCreeper ? "Creeper" : "Other", dropChance);

        if (breaksBlock)
        {
            for (BlockPos pos : explosion.getAffectedBlockPositions())
            {
                BlockState state = world.getBlockState(pos);

                if (spawnParticles)
                {
                    double d0 = pos.getX() + rand.nextFloat();
                    double d1 = pos.getY() + rand.nextFloat();
                    double d2 = pos.getZ() + rand.nextFloat();
                    double d3 = d0 - posVec.x;
                    double d4 = d1 - posVec.y;
                    double d5 = d2 - posVec.z;
                    double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / explosionSize + 0.1D);
                    d7 = d7 * (double)(rand.nextFloat() * rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    world.addParticle(ParticleTypes.POOF, (d0 + posVec.x) / 2.0D, (d1 + posVec.y) / 2.0D, (d2 + posVec.z) / 2.0D, d3, d4, d5);
                    world.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
                }

                if (state.isAir(world, pos) == false)
                {
                    if ((world instanceof ServerWorld) && state.canDropFromExplosion(world, pos, explosion))
                    {
                        // The corresponding modify explosion chance config option is going
                        // to be true when this method is called in the first place
                        if (dropChance > 0)
                        {
                            ServerWorld serverWorld = (ServerWorld) world;
                            TileEntity te = state.hasTileEntity() ? world.getTileEntity(pos) : null;
                            LootContext.Builder builder = (new LootContext.Builder(serverWorld))
                                    .withRandom(rand).withParameter(LootParameters.POSITION, pos)
                                    .withParameter(LootParameters.TOOL, ItemStack.EMPTY)
                                    .withNullableParameter(LootParameters.BLOCK_ENTITY, te);

                            if (dropChance < 1.0f)
                            {
                                // See SurvivesExplosion loot condition
                                float size = 1.0f / dropChance;
                                builder.withParameter(LootParameters.EXPLOSION_RADIUS, size);
                            }

                            Block.spawnDrops(state, builder);
                        }
                    }

                    state.onBlockExploded(world, pos, explosion);
                }
            }
        }

        if (causesFire)
        {
            for (BlockPos pos : explosion.getAffectedBlockPositions())
            {
                if (world.getBlockState(pos).isAir(world, pos) &&
                    world.getBlockState(pos.down()).isOpaqueCube(world, pos.down()) &&
                    rand.nextInt(3) == 0)
                {
                    world.setBlockState(pos, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }

    private void causeCreeperChainReaction(World world, Vec3d explosionPos)
    {
        EnvironmentalCreepers.logInfo("ExplosionEventHandler.causeCreeperChainReaction() - Explosion Position: '{}'", explosionPos);

        double r = Configs.Generic.creeperChainReactionMaxDistance;
        double rSq = r * r;
        AxisAlignedBB bb = new AxisAlignedBB(
                explosionPos.x - r, explosionPos.y - r, explosionPos.z - r,
                explosionPos.x + r, explosionPos.y + r, explosionPos.z + r);
        List<CreeperEntity> list = world.getEntitiesWithinAABB(CreeperEntity.class, bb, (ent) -> ((ent instanceof LivingEntity) && ((LivingEntity) ent).getHealth() > 0));

        for (CreeperEntity creeper : list)
        {
            if (creeper.hasIgnited() == false && world.rand.nextFloat() < Configs.Generic.creeperChainReactionChance &&
                creeper.getDistanceSq(explosionPos.x, explosionPos.y, explosionPos.z) <= rSq)
            {
                EnvironmentalCreepers.logInfo("ExplosionEventHandler.causeCreeperChainReaction() - Igniting Creeper: '{}'", creeper.toString());
                creeper.ignite();
            }
        }
    }
}
