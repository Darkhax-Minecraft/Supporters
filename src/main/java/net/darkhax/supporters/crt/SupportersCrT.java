package net.darkhax.supporters.crt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.darkhax.supporters.Supporters;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.supporters.Supporters")
@ZenRegister
public class SupportersCrT {

    @ZenMethod
    public static SupporterInfo getRandomSupporter() {

        final UUID randomId = Supporters.instance.getRandomSupporter();
        final GameProfile profile = Supporters.instance.getSupporter(randomId);
        final ItemStack skull = Supporters.instance.getSupporterHead(randomId);       
        return new SupporterInfo(profile.getId(), profile.getName(), skull);
    }

    @ZenMethod
    public static List<SupporterInfo> getAllSupporters() {
        
        final List<SupporterInfo> supporterInfo = new ArrayList<>();
        
        for (GameProfile profile : Supporters.instance.getSupporters()) {
            
            supporterInfo.add(new SupporterInfo(profile.getId(), profile.getName(), Supporters.instance.getSupporterHead(profile.getId())));
        }
        
        return supporterInfo;
    }
}