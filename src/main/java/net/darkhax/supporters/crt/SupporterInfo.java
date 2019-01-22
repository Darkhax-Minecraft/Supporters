package net.darkhax.supporters.crt;

import java.util.UUID;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;

@ZenRegister
@ZenClass("mods.supporters.SupporterInfo")
public class SupporterInfo {

    private final String userID;
    private final String name;
    private final IItemStack skull;

    public SupporterInfo (UUID id, String name, ItemStack skull) {

        this.userID = id.toString();
        this.name = name;
        this.skull = CraftTweakerMC.getIItemStack(skull);
    }
    
    @ZenGetter
    public String getUserId() {
        
        return this.userID;
    }
    
    @ZenGetter
    public String getName() {
        
        return this.name;
    }
    
    public IItemStack getSkull () {
        
        return this.skull;
    }
}