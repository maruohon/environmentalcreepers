package fi.dy.masa.environmentalcreepers.mixin;


import java.util.function.BiConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import fi.dy.masa.environmentalcreepers.config.Configs;

@Mixin(AbstractBlock.class)
public class MixinAbstractBlock
{
    @Redirect(method = "onExploded", allow = 1,
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/explosion/Explosion$DestructionType;DESTROY_WITH_DECAY:Lnet/minecraft/world/explosion/Explosion$DestructionType;")),
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/loot/context/LootContextParameterSet$Builder;add(Lnet/minecraft/loot/context/LootContextParameter;Ljava/lang/Object;)Lnet/minecraft/loot/context/LootContextParameterSet$Builder;"))
    private <T> LootContextParameterSet.Builder envc_modifyDropChance(LootContextParameterSet.Builder builder, LootContextParameter<T> key, T value,
                                                                      BlockState state, World world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger)
    {
        if (explosion.getCausingEntity() instanceof CreeperEntity)
        {
            if (Configs.Toggles.MODIFY_CREEPER_EXPLOSION_DROP_CHANCE.getValue())
            {
                float dropChance = Configs.Generic.CREEPER_EXPLOSION_BLOCK_DROP_CHANCE.getFloatValue();

                if (dropChance > 0.0F && dropChance < 1.0f)
                {
                    // See SurvivesExplosion loot condition
                    float size = 1.0f / dropChance;
                    return builder.add(LootContextParameters.EXPLOSION_RADIUS, size);
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
                    return builder.add(LootContextParameters.EXPLOSION_RADIUS, size);
                }
            }
        }

        // The drop chance == 1.0 case is handled by changing the destruction type to just `DESTROY`,
        // so that the `EXPLOSION_RADIUS` parameter is not added at all, and thus this mixin will not run.
        // So by default run the normal unmodified add() call.

        return builder.add(key, value);
    }
}
