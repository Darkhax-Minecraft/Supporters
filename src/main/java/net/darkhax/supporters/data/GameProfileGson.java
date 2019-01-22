package net.darkhax.supporters.data;

import java.util.UUID;

import com.google.common.collect.Iterables;
import com.google.gson.annotations.Expose;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

/**
 * This class is used as an intermediary type for Gson. It allows the mod to cache profiles,
 * and additional properties that are not traditionally cached.
 */
public class GameProfileGson {

    @Expose
    private final String name;

    @Expose
    private final UUID id;

    @Expose
    private String textureData;

    public GameProfileGson (GameProfile profile) {

        this.name = profile.getName();
        this.id = profile.getId();

        final Property property = (Property) Iterables.getFirst(profile.getProperties().get("textures"), (Object) null);

        if (property != null) {

            this.textureData = property.getValue();
        }
    }

    public GameProfile constructProfile () {

        final GameProfile profile = new GameProfile(this.id, this.name);
        profile.getProperties().put("textures", new Property("texutres", this.textureData));
        return profile;
    }
}