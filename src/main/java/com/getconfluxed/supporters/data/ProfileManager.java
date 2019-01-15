package com.getconfluxed.supporters.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.getconfluxed.supporters.ZenSupporters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
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
     * An instance of gson, to handle cache file serialization.
     */
    private final Gson gson;

    /**
     * The handle for the cache file.
     */
    private final File usercacheFile;

    /**
     * A map of all the loaded profiles. The map is accessed by UUID.
     */
    private final Map<UUID, GameProfile> uuidCache = new ConcurrentHashMap<>();

    public ProfileManager (File usercacheFileIn) {

        // Generate a new auth client
        final YggdrasilAuthenticationService auth = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
        this.sessionService = auth.createMinecraftSessionService();
        this.usercacheFile = usercacheFileIn;
        this.gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    }

    /**
     * Gets a profile by it's UUID. If no profile is found, it will be looked up and cached
     * using {@link #retrieveProfile(UUID)}.
     *
     * @param id The ID of the player to look up.
     * @return The profile that was found.
     */
    public GameProfile getProfileByUUID (UUID id) {

        return this.uuidCache.containsKey(id) ? this.uuidCache.get(id) : this.retrieveProfile(id);
    }

    /**
     * Creates the cache file if it doesn't already exist.
     */
    private void createCacheFile () {

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
     * Requests profile info from Mojang's servers. This will also cache it to
     * {@link #uuidCache} to make subsequent lookups faster.
     *
     * @param uuid The id of the player to lookup.
     * @return The profile that was received.
     */
    private GameProfile retrieveProfile (UUID uuid) {

        final GameProfile profile = this.sessionService.fillProfileProperties(new GameProfile(uuid, null), false);
        this.uuidCache.put(uuid, profile);
        return profile;
    }

    /**
     * Loads cache data from the cache file. This cache is used to improve the speed of future
     * lookups.
     *
     * @param refresh Whether or not the cache data should be repopulated with data from
     *        Mojang's servers or not.
     */
    public void load (boolean refresh) {

        // Clear existing caches
        this.uuidCache.clear();

        // Create the cache file if it did not exist.
        this.createCacheFile();

        // Attempt to read the file.
        try (FileReader reader = new FileReader(this.usercacheFile)) {

            // Deserialize the json to an array of profiles.
            final GameProfileGson[] profiles = this.gson.fromJson(reader, GameProfileGson[].class);

            if (profiles != null) {

                // If refresh is true, profiles are looked up again.
                if (refresh) {

                    for (final GameProfileGson profile : profiles) {

                        final GameProfile compiledProfile = profile.constructProfile();
                        this.retrieveProfile(compiledProfile.getId());
                    }
                }

                // Otherwise load from file and assume it's true.
                else {

                    for (final GameProfileGson profile : profiles) {

                        final GameProfile compiledProfile = profile.constructProfile();
                        this.uuidCache.put(compiledProfile.getId(), compiledProfile);
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

            // Compile all the profiles into the custom gson format.
            final List<GameProfileGson> profiles = this.uuidCache.values().stream().map(GameProfileGson::new).collect(Collectors.toList());
            // Serialize the cache file and save it to the file.
            writer.write(this.gson.toJson(profiles));
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

        return this.sessionService.fillProfileProperties(profile, true);
    }
}