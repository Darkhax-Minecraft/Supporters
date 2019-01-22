package net.darkhax.supporters.command;

import net.darkhax.bookshelf.command.Command;
import net.darkhax.supporters.Supporters;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandReload extends Command {

    @Override
    public String getName () {

        return "reload";
    }

    @Override
    public int getRequiredPermissionLevel () {

        return 2;
    }

    @Override
    public String getUsage (ICommandSender sender) {

        return "/supporters reload [refresh]";
    }

    @Override
    public void execute (MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        final boolean shouldRefresh = args.length > 0 && parseBoolean(args[0]);
        sender.sendMessage(new TextComponentTranslation("supporters.reload.start"));
        Supporters.instance.loadSupporters(shouldRefresh);
    }
}