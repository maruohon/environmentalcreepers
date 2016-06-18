package fi.dy.masa.environmentalcreepers.event;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class ExplosionEventHandler
{
    private final Field fieldExplosionSize;
    private final Field fieldIsFlaming;
    private final Field fieldIsSmoking;

    public ExplosionEventHandler()
    {
        this.fieldExplosionSize =   ReflectionHelper.findField(Explosion.class, "field_77280_f", "explosionSize");
        this.fieldIsSmoking =       ReflectionHelper.findField(Explosion.class, "field_82755_b", "isSmoking");
        this.fieldIsFlaming =       ReflectionHelper.findField(Explosion.class, "field_77286_a", "isFlaming");
    }

    @SubscribeEvent
    public void onExplosionStart(ExplosionEvent.Start event)
    {
        Explosion explosion = event.getExplosion();

        if (explosion.getExplosivePlacedBy() instanceof EntityCreeper)
        {
            if (Configs.modifyCreeperExplosionDropChance && Configs.disableCreeperExplosionBlockDamage == false)
            {
                this.replaceExplosion(event, true);
            }
        }
        else
        {
            if (Configs.modifyOtherExplosionDropChance && Configs.disableOtherExplosionBlockDamage == false)
            {
                this.replaceExplosion(event, false);
            }
        }
    }

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event)
    {
        if (event.getExplosion().getExplosivePlacedBy() instanceof EntityCreeper)
        {
            if (Configs.disableCreeperExplosionItemDamage)
            {
                this.removeItemEntities(event.getAffectedEntities());
            }

            if (Configs.disableCreeperExplosionBlockDamage)
            {
                event.getExplosion().clearAffectedBlockPositions();
            }
        }
        else
        {
            if (Configs.disableOtherExplosionItemDamage)
            {
                this.removeItemEntities(event.getAffectedEntities());
            }

            if (Configs.disableOtherExplosionBlockDamage)
            {
                event.getExplosion().clearAffectedBlockPositions();
            }
        }
    }

    private void removeItemEntities(List<Entity> list)
    {
        Iterator<Entity> iter = list.iterator();

        while (iter.hasNext())
        {
            Entity entity = iter.next();

            if (entity instanceof EntityItem)
            {
                iter.remove();
            }
        }
    }

    private void replaceExplosion(ExplosionEvent.Start event, boolean isCreeper)
    {
        World world = event.getWorld();
        Explosion explosion = event.getExplosion();

        try
        {
            boolean isSmoking = this.fieldIsSmoking.getBoolean(explosion);
            boolean isFlaming = this.fieldIsFlaming.getBoolean(explosion);
            float explosionSize;

            if (isCreeper && Configs.modifyCreeperExplosionStrength)
            {
                if (((EntityCreeper) explosion.getExplosivePlacedBy()).getPowered())
                {
                    explosionSize = (float) Configs.creeperExplosionStrengthCharged;
                }
                else
                {
                    explosionSize = (float) Configs.creeperExplosionStrengthNormal;
                }

                this.fieldExplosionSize.setFloat(explosion, explosionSize);
            }
            else
            {
                explosionSize = this.fieldExplosionSize.getFloat(explosion);
            }

            explosion.doExplosionA();

            if (world instanceof WorldServer)
            {
                this.doExplosionB(world, explosion, false, isCreeper, isSmoking, isFlaming, explosionSize);

                if (isSmoking == false)
                {
                    explosion.clearAffectedBlockPositions();
                }

                Vec3d pos = explosion.getPosition();

                for (EntityPlayer player : world.playerEntities)
                {
                    if (player.getDistanceSq(pos.xCoord, pos.yCoord, pos.zCoord) < 4096.0D)
                    {
                        ((EntityPlayerMP) player).connection.sendPacket(
                                new SPacketExplosion(pos.xCoord, pos.yCoord, pos.zCoord, explosionSize,
                                        explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(player)));
                    }
                }
            }
            else
            {
                this.doExplosionB(world, explosion, true, isCreeper, isSmoking, isFlaming, explosionSize);
            }
        }
        catch (IllegalAccessException e)
        {
            EnvironmentalCreepers.logger.error("IllegalAccessException while reflecting explosion fields...");
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            EnvironmentalCreepers.logger.error("IllegalArgumentException while reflecting explosion fields...");
            e.printStackTrace();
        }

        event.setCanceled(true);
    }

    private void doExplosionB(World world, Explosion explosion, boolean spawnParticles, boolean isCreeper, boolean isSmoking, boolean isFlaming, float explosionSize)
    {
        Vec3d pos = explosion.getPosition();
        Random rand = world.rand;

        world.playSound((EntityPlayer)null, pos.xCoord, pos.yCoord, pos.zCoord, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.2F) * 0.7F);

        if (explosionSize >= 2.0F && isSmoking)
        {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, pos.xCoord, pos.yCoord, pos.zCoord, 1.0D, 0.0D, 0.0D, new int[0]);
        }
        else
        {
            world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, pos.xCoord, pos.yCoord, pos.zCoord, 1.0D, 0.0D, 0.0D, new int[0]);
        }

        float dropChance = (float)(isCreeper ? Configs.creeperExplosionBlockDropChance : Configs.otherExplosionBlockDropChance);

        if (isSmoking)
        {
            for (BlockPos blockpos : explosion.getAffectedBlockPositions())
            {
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (spawnParticles)
                {
                    double d0 = blockpos.getX() + rand.nextFloat();
                    double d1 = blockpos.getY() + rand.nextFloat();
                    double d2 = blockpos.getZ() + rand.nextFloat();
                    double d3 = d0 - pos.xCoord;
                    double d4 = d1 - pos.yCoord;
                    double d5 = d2 - pos.zCoord;
                    double d6 = MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
                    d3 = d3 / d6;
                    d4 = d4 / d6;
                    d5 = d5 / d6;
                    double d7 = 0.5D / (d6 / explosionSize + 0.1D);
                    d7 = d7 * (double)(rand.nextFloat() * rand.nextFloat() + 0.3F);
                    d3 = d3 * d7;
                    d4 = d4 * d7;
                    d5 = d5 * d7;
                    world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + pos.xCoord) / 2.0D, (d1 + pos.yCoord) / 2.0D, (d2 + pos.zCoord) / 2.0D, d3, d4, d5, new int[0]);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
                }

                if (iblockstate.getMaterial() != Material.AIR)
                {
                    if (block.canDropFromExplosion(explosion))
                    {
                        block.dropBlockAsItemWithChance(world, blockpos, iblockstate, dropChance, 0);
                    }

                    block.onBlockExploded(world, blockpos, explosion);
                }
            }
        }

        if (isFlaming)
        {
            for (BlockPos blockpos : explosion.getAffectedBlockPositions())
            {
                if (rand.nextInt(3) == 0 &&
                    world.getBlockState(blockpos).getMaterial() == Material.AIR &&
                    world.getBlockState(blockpos.down()).isFullBlock())
                {
                    world.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }
}
