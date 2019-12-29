package fi.dy.masa.environmentalcreepers.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import fi.dy.masa.environmentalcreepers.config.Configs;

public class CommandReloadConfigs extends CommandBase
{
    @Override
    public String getName()
    {
        return "environmentalcreepers-reloadconfig";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return  "/" + this.getName();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Configs.reloadConfig();
        CommandBase.notifyCommandListener(sender, this, "Config reloaded");
    }
}
