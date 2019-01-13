package com.getconfluxed.zensupporters;

import com.mojang.authlib.GameProfile;

public class Supporter {

    private final GameProfile profile;

    public Supporter (GameProfile profile) {

        this.profile = profile;
        System.out.println(profile.getName());
    }
}
