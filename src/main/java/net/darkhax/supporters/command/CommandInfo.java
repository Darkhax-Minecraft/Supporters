package net.darkhax.supporters.command;

import java.util.StringJoiner;

import com.mojang.authlib.GameProfile;

import net.darkhax.bookshelf.command.Command;
import net.darkhax.supporters.Supporters;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandInfo extends Command {

    @Override
    public String getName () {

        return "info";
    }

    @Override
    public String getUsage (ICommandSender sender) {

        return "/supporters info";
    }

    @Override
    public void execute (MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        final StringJoiner builder = new StringJoiner(", ");

        for (final GameProfile profile : Supporters.instance.getSupporters()) {

            builder.add(profile.getName());
        }

        sender.sendMessage(new TextComponentString(builder.toString()));
    }
}