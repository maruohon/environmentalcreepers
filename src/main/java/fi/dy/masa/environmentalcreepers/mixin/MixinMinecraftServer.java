package fi.dy.masa.environmentalcreepers.mixin;

import java.io.File;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import fi.dy.masa.environmentalcreepers.Reference;
import fi.dy.masa.environmentalcreepers.config.Configs;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer
{
    @Shadow @Final protected LevelStorage.Session session;

    @Inject(method = "runServer", at = @At("HEAD"))
    private void onServerStart(CallbackInfo ci)
    {
        File globalDir = new File(new File("."), "config");
        File worldDir = new File(this.session.getDirectory(WorldSavePath.ROOT).toFile(), Reference.MOD_ID);

        try
        {
            globalDir = globalDir.getCanonicalFile();
            worldDir = worldDir.getCanonicalFile();
        }
        catch (Exception ignore) {}

        Configs.setGlobalConfigDir(globalDir);
        Configs.setWorldConfigDir(worldDir);
        Configs.loadConfigsFromPerWorldConfigIfApplicable();
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void onShutdown(CallbackInfo ci)
    {
        Configs.loadConfigsFromGlobalConfigFile();
    }
}
