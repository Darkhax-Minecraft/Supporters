# Supporters [![](https://cf.way2muchnoise.eu/311440.svg)](https://minecraft.curseforge.com/projects/311440) [![](https://cf.way2muchnoise.eu/versions/311440.svg)](https://minecraft.curseforge.com/projects/311440) [![](https://cf.way2muchnoise.eu/packs/311440.svg)](https://minecraft.curseforge.com/projects/311440)

This mod provides modpacks and servers with a way to incorporate their supporters into the game experience. Supporters can be loaded from a variety of sources which are defined in the config file. The supporter data is loaded into the mod and then made available for other mods to use. The supporter data can also be accessed from CraftTweaker scripts if you want to use it in that way. 

This mod supports the following sources
- Directly added to the config file.
- Online links to lists of UUIDs. [example](https://gist.githubusercontent.com/darkhax/bb02babc2e702473b8d01367e3e0effe/raw/eb318af1b4b53052f47fe3637ac7737cf5d14977/web-test.txt)
- Twitch Subscribers. (Not Yet Implemented)
- Patreon Supporters. (Not Yet Implemented)

**NOTICE**: Twitch and Patreon support are a work in progress and are not publicly available yet. These features will make use of [Dries007's MCLink](https://mclink.dries007.net/) service. Traditionally this service has been used to simplify the whitelist process for supporter-only servers. In the future this mod will be able to pull from that data as well.

**Note:** Please do not use this mod to violate Mojang's EULA or Commercial Usage Guidelines. This mod should never be used to give players an unfair advantage in game. 

## CraftTweaker Support

### IPlayer Expansion
Wherever you have access to an IPlayer in your script file, you can call the `isKnownSupporter` method to check if that player is in the supporters list. This method returns a boolean. 

### Supporter Info
The `mods.supporters.Supporters` will be available in your scripts. You can call the `getRandomSupporter` method to get info about a random supporter, and `getAllSupporters` to get a list of all the supporter information. 

A supporter info object uses the type `mods.supporters.SupporterInfo`. This object has three methods that can be used. `getUserId` which returns a string containg the UUID of the supporter. `getName` which returns the username of the player. `getSkull` which returns an IItemStack of a player head for that supporter.