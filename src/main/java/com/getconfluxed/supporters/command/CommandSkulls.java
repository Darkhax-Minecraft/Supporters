package com.getconfluxed.supporters.command;

import com.getconfluxed.supporters.Supporters;

import net.darkhax.bookshelf.command.Command;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class CommandSkulls extends Command {

    @Override
    public String getName () {

        return "skulls";
    }

    @Override
    public String getUsage (ICommandSender sender) {

        return "/supporters skulls";
    }

    @Override
    public void execute (MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (sender instanceof EntityPlayer) {

            final EntityPlayer player = (EntityPlayer) sender;

            for (final ItemStack stack : Supporters.instance.getSupporterSkulls()) {

                player.addItemStackToInventory(stack);
            }
        }
    }
}