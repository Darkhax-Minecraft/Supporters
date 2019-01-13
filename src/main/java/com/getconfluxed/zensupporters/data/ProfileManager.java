package com.getconfluxed.zensupporters.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Proxy;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.getconfluxed.zensupporters.ZenSupporters;
import com.google.gson.Gson;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

/**
 * This class serves as a combination of profile cache and client for looking up profiles from
 * Mojang's services. This custom cache is used instead of the vanilla one because the vanilla
 * cache is really clunky and doesn't work well in a threaded context.
 */
public class ProfileManager {

    /**
     * A client for the Minecraft session service.
     */
    private final MinecraftSessionService sessionService;

    /**
     * A client for the Minecraft profile repo.
     */
    private final GameProfileRepository profileRepo;

    /**
     * An instance of gson, to handle cache file serialization.
     */
    private final Gson gson;

    /**
     * The handle for the cache file.
     */
    private final File usercacheFile;

    private final Map<String, ProfileEntry> nameCache = new ConcurrentHashMap<>();
    private final Map<UUID, ProfileEntry> uuidCache = new ConcurrentHashMap<>();
    private final Map<UUID, GameProfile> filledProfiles = new ConcurrentHashMap<>();

    public ProfileManager (File usercacheFileIn) {

        // Generate a new auth client
        final YggdrasilAuthenticationService auth = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());

        this.sessionService = auth.createMinecraftSessionService();
        this.profileRepo = auth.createProfileRepository();

        this.usercacheFile = usercacheFileIn;
        this.gson = new Gson();
        this.load();
    }

    /**
     * Requests profile information from Mojang's player profile repo.
     *
     * @param name The username to search for.
     * @return The profile data that was found.
     */
    private GameProfile lookupProfile (String name) {

        // Requests profile info from Mojang
        final StupidCallback callback = new StupidCallback();
        this.profileRepo.findProfilesByNames(new String[] { name }, Agent.MINECRAFT, callback);
        return callback.getProfile();
    }

    /**
     * Adds a new entry to the the cache.
     *
     * @param entry The entry to cache.
     */
    private void cacheEntry (ProfileEntry entry) {

        this.nameCache.put(entry.getName().toLowerCase(Locale.ROOT), entry);
        this.uuidCache.put(entry.getId(), entry);
    }

    /**
     * Gets a profile for a username. This will look it up if it does not already exist.
     *
     * @param username The name of the user.
     * @return The profile that was found.
     */
    public GameProfile getProfileByUsername (String username) {

        return this.getProfileByUsername(username, new Date().getTime());
    }

    /**
     * Gets a profile for a username. This will look it up if it does not already exist. This
     * version of the method accepts an additional start time parameter which is intended for
     * bulk lookups like when the file is loaded.
     *
     * @param username The name of the user.
     * @param startTime The base time to use for new profiles. This is used to set the
     *        expiration date.
     * @return
     */
    public GameProfile getProfileByUsername (String username, long startTime) {

        // Grab the profile from the cache
        ProfileEntry profileEntry = this.nameCache.get(username);

        // If the cache entry exists, but has expired, reset it.
        if (profileEntry != null && startTime >= profileEntry.getExpiresOn()) {

            this.uuidCache.remove(profileEntry.getGameProfile().getId());
            this.nameCache.remove(username);
            profileEntry = null;
        }

        // If the profile doesn't exist, attempt to get it from the auth servers.
        if (profileEntry == null || profileEntry.getGameProfile() == null) {

            // Request profile information from Mojang.
            final GameProfile lookedUpProfile = this.lookupProfile(username);

            // If mojang gave us info
            if (lookedUpProfile != null) {

                // Cache the response from mojang. The time addition is based on 2 months of
                // time.
                this.cacheEntry(new ProfileEntry(lookedUpProfile, startTime + 2678400000L));
                return lookedUpProfile;
            }
        }

        return null;
    }

    /**
     * Creates the cache file if it doesn't already exist.
     */
    private void createCache () {

        if (!this.usercacheFile.exists()) {

            try {

                if (this.usercacheFile.createNewFile()) {

                    ZenSupporters.LOG.info("Successfully created cache file at {}.", this.usercacheFile.getAbsolutePath());
                }
            }

            catch (final IOException e) {

                ZenSupporters.LOG.error("Failed to create cache file. {}", this.usercacheFile.getAbsolutePath());
                ZenSupporters.LOG.catching(e);
            }
        }
    }

    /**
     * Loads the cache data into memory. This will reset all the existing cache data.
     */
    public void load () {

        // Clear existing caches
        this.nameCache.clear();
        this.uuidCache.clear();

        // Create the cache file if it did not exist.
        this.createCache();

        // Attempt to read the file.
        try (FileReader reader = new FileReader(this.usercacheFile)) {

            // Deserialize the json to an array of profiles.
            final ProfileEntry[] cacheEntries = this.gson.fromJson(reader, ProfileEntry[].class);

            if (cacheEntries != null) {

                // Iterate existing entries and add them to the cache.
                for (final ProfileEntry entry : cacheEntries) {

                    if (entry != null && entry.getGameProfile() != null) {

                        this.cacheEntry(entry);
                    }
                }
            }
        }
        catch (final IOException e) {

            ZenSupporters.LOG.catching(e);
        }
    }

    /**
     * Attempt to save the cache to a file.
     */
    public void save () {

        // Try to open a file writer to the cache file.
        try (FileWriter writer = new FileWriter(this.usercacheFile)) {

            // Serialize the cache file and save it to the file.
            writer.write(this.gson.toJson(this.nameCache.values()));
        }

        catch (final IOException e) {

            ZenSupporters.LOG.catching(e);
        }
    }

    /**
     * Attempts to get the filled properties for a profile. This includes things like texture
     * data.
     *
     * @param profile The profile to fill.
     * @return The filled profile.
     */
    public GameProfile getFilledProfile (GameProfile profile) {

        // If a filled profile already exists, add it to the cache.
        if (this.filledProfiles.containsKey(profile.getId())) {

            return this.filledProfiles.get(profile.getId());
        }

        // Request a filled profile from Mojang.
        final GameProfile filledProfile = this.sessionService.fillProfileProperties(profile, false);

        // If Mojang gave us a good profile, add it to the cache.
        if (filledProfile.getId() != null) {

            this.filledProfiles.put(filledProfile.getId(), filledProfile);
        }

        return filledProfile;
    }
}