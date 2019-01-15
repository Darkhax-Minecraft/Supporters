package com.getconfluxed.supporters.crt;

import com.getconfluxed.supporters.ZenSupporters;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import net.minecraft.entity.player.EntityPlayer;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * This class expands CraftTweaker's player class with methods to interact with this mod. This
 * class should not be directly referenced by any mod code directly. Seriously, stay away! lol
 */
@ZenRegister
@ZenExpansion("crafttweaker.player.IPlayer")
public class PlayerSupporterExpansion {

    /**
     * Checks if a player is in the known supporters list.
     *
     * @param player The player to check.
     * @return Whether or not the player is a known supporter.
     */
    @ZenMethod
    public static boolean isKnownSupporter (IPlayer player) {

        final EntityPlayer actualPlayer = CraftTweakerMC.getPlayer(player);
        return actualPlayer != null && ZenSupporters.instance.isSupporter(actualPlayer.getUniqueID());
    }
}
