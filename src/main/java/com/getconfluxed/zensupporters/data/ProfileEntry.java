package com.getconfluxed.zensupporters.data;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

/**
 * This class represents a profile entry from a cache file, and sometimes from a web server.
 */
public class ProfileEntry {

    /**
     * The display name of the profile.
     */
    private final String name;

    /**
     * The unique id of the profile.
     */
    private final UUID id;

    /**
     * A basic time stamp for when the profile expires and should be reset.
     */
    private final long expiresOn;

    public ProfileEntry (GameProfile profile, long expiresOn) {

        this(profile.getId(), profile.getName(), expiresOn);
    }

    public ProfileEntry (UUID id, String name, long expiresOn) {

        this.name = name;
        this.id = id;
        this.expiresOn = expiresOn;
    }

    public GameProfile getGameProfile () {

        return new GameProfile(this.getId(), this.getName());
    }

    public String getName () {

        return this.name;
    }

    public UUID getId () {

        return this.id;
    }

    public long getExpiresOn () {

        return this.expiresOn;
    }
}