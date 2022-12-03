package fi.dy.masa.environmentalcreepers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import fi.dy.masa.environmentalcreepers.config.Configs;

public class CommandReloadConfig
{
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(Component.literal("Environmental Creepers: failed to reload the config!"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
                Commands.literal("environmentalcreepers-reload")
                    .requires((src) -> src.hasPermission(4))
                    .executes((src) -> reloadConfig(src.getSource())));
     }

     private static int reloadConfig(CommandSourceStack source) throws CommandSyntaxException
     {
         if (Configs.reloadConfig())
         {
             source.sendSuccess(Component.literal("Environmental Creepers config reloaded"), true);
             return 0;
         }

         throw FAILED_EXCEPTION.create();
     }
}
