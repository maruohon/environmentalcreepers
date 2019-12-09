package fi.dy.masa.environmentalcreepers.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class CommandReloadConfig
{
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("Environmental Creepers: failed to reload the config!"));

    public static void register(CommandDispatcher<CommandSource> dispatcher)
    {
        dispatcher.register(
                Commands.literal("environmentalcreepers-reload")
                    .requires((src) -> src.hasPermissionLevel(4))
                    .executes((src) -> reloadConfig(src.getSource())));
     }

     private static int reloadConfig(CommandSource source) throws CommandSyntaxException
     {
         if (Configs.reloadConfig())
         {
             source.sendFeedback(new StringTextComponent("Environmental Creepers config reloaded"), true);
             return 0;
         }

         throw FAILED_EXCEPTION.create();
     }
}
