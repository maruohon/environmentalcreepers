package fi.dy.masa.environmentalcreepers.util;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import fi.dy.masa.environmentalcreepers.EnvironmentalCreepers;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class ExplosionUtils
{
    public static void causeCreeperChainReaction(World world, Vec3d explosionPos)
    {
        EnvironmentalCreepers.logInfo("ExplosionUtils.causeCreeperChainReaction() - Explosion Position: '{}'", explosionPos);

        double r = Configs.Generic.CREEPER_CHAIN_REACTION_MAX_DISTANCE.getValue();
        double rSq = r * r;
        Box bb = new Box(
                explosionPos.x - r, explosionPos.y - r, explosionPos.z - r,
                explosionPos.x + r, explosionPos.y + r, explosionPos.z + r);
        List<CreeperEntity> list = world.getEntitiesByType(EntityType.CREEPER, bb, (ent) -> ent.getHealth() > 0);

        for (CreeperEntity creeper : list)
        {
            if (creeper.isIgnited() == false && world.random.nextFloat() < Configs.Generic.CREEPER_CHAIN_REACTION_CHANCE.getValue() &&
                creeper.squaredDistanceTo(explosionPos.x, explosionPos.y, explosionPos.z) <= rSq)
            {
                EnvironmentalCreepers.logInfo("ExplosionUtils.causeCreeperChainReaction() - Igniting Creeper: '{}'", creeper.toString());
                creeper.ignite();
            }
        }
    }
}
