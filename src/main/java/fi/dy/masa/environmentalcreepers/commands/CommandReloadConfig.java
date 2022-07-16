package fi.dy.masa.environmentalcreepers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class CommandReloadConfig
{
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.literal("Environmental Creepers: failed to reload the config!"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher)
    {
        dispatcher.register(
                CommandManager.literal("environmentalcreepers-reload")
                    .requires((src) -> src.hasPermissionLevel(4))
                    .executes((src) -> reloadConfig(src.getSource())));
    }

    private static int reloadConfig(ServerCommandSource source) throws CommandSyntaxException
    {
        if (Configs.reloadConfig())
        {
            source.sendFeedback(Text.literal("Environmental Creepers config reloaded"), true);
            return 0;
        }

        throw FAILED_EXCEPTION.create();
    }
}
