package net.darkhax.supporters.command;

import net.darkhax.bookshelf.command.CommandTree;
import net.minecraft.command.ICommandSender;

public class CommandSupportersTree extends CommandTree {

    public CommandSupportersTree () {

        this.addSubcommand(new CommandReload());
        this.addSubcommand(new CommandInfo());
        this.addSubcommand(new CommandSkulls());
    }

    @Override
    public int getRequiredPermissionLevel () {

        return 0;
    }

    @Override
    public String getName () {

        return "supporters";
    }

    @Override
    public String getUsage (ICommandSender sender) {

        return "/supporters";
    }
}