package com.getconfluxed.zensupporters.data;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;

/**
 * Mojang has a stupid API that requires this weird callback thing when you make a request.
 * They don't thread the request, and the callback isn't really used in any beneficial way.
 * This implementation tries to make it less stupid, but it's still pretty bad.
 */
public class StupidCallback implements ProfileLookupCallback {

    /**
     * The result of the callback.
     */
    private GameProfile profile;

    @Override
    public void onProfileLookupSucceeded (GameProfile profile) {

        this.profile = profile;
    }

    @Override
    public void onProfileLookupFailed (GameProfile profile, Exception exception) {

        this.profile = null;
    }

    /**
     * Gets the result of the callback. This can be null.
     *
     * @return The result from the callback thing.
     */
    public GameProfile getProfile () {

        return this.profile;
    }
}