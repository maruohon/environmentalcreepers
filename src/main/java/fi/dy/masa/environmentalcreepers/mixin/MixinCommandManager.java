package fi.dy.masa.environmentalcreepers.mixin;

import com.mojang.brigadier.CommandDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import fi.dy.masa.environmentalcreepers.commands.CommandReloadConfig;

@Mixin(CommandManager.class)
public class MixinCommandManager
{
    @Inject(method = "<init>", remap = false,
            at = @At(value = "INVOKE",
                     target = "Lcom/mojang/brigadier/CommandDispatcher;setConsumer(Lcom/mojang/brigadier/ResultConsumer;)V"))
    private void onInit(CommandManager.RegistrationEnvironment environment,
                        CommandRegistryAccess commandRegistryAccess,
                        CallbackInfo ci)
    {
        CommandDispatcher<ServerCommandSource> dispatcher = ((CommandManager) (Object) this).getDispatcher();

        CommandReloadConfig.register(dispatcher);
    }
}
