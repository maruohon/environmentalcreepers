package fi.dy.masa.environmentalcreepers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import fi.dy.masa.environmentalcreepers.commands.CommandReloadConfig;

@Mixin(CommandManager.class)
public class MixinCommandManager
{
    @Inject(method = "<init>(Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;)V", at = @At("RETURN"))
    private void onInit(CommandManager.RegistrationEnvironment environment, CallbackInfo ci)
    {
        CommandDispatcher<ServerCommandSource> dispatcher = ((CommandManager) (Object) this).getDispatcher();

        CommandReloadConfig.register(dispatcher);
    }
}
