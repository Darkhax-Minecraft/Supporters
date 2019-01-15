package com.getconfluxed.supporters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.getconfluxed.supporters.data.ProfileManager;
import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "supporters", name = "Supporters", version = "@VERSION@", dependencies = "", certificateFingerprint = "@FINGERPRINT@")
public class Supporters {

    public static final Logger LOG = LogManager.getLogger("Supporters");

    @Instance("supporters")
    public static Supporters instance;

    private final File modDir = new File("config/supporters");
    private ProfileManager lookupManager;
    private Configs config;
    private Map<UUID, GameProfile> knownSupporters;

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) {

        if (!this.modDir.exists()) {

            this.modDir.mkdirs();
        }

        this.knownSupporters = new ConcurrentHashMap<>();
        this.lookupManager = new ProfileManager(new File(this.modDir, "supporters-cache.json"));
        this.config = new Configs(new File(this.modDir, "supporters.cfg"));
        this.loadSupporters(false);
    }

    public boolean isSupporter (UUID id) {

        return this.knownSupporters.containsKey(id);
    }

    /**
     * Triggers a reload of all the supporter data. This will spin off multiple threads, so
     * calls to this command should be used sparingly.
     */
    public void loadSupporters (boolean refresh) {

        // Clear the list of known supporters.
        this.knownSupporters.clear();

        // Start a new thread to update player info.
        new Thread( () -> {

            final long startTime = System.currentTimeMillis();
            
            // Loaded data into the cache file.
            this.lookupManager.load(refresh);

            // Iterate all the data from the config, and load it into the cache.
            for (final UUID configSpecifiedId : this.config.supporterUUIDs) {

                // Retrieve the profile.
                final GameProfile profile = this.lookupManager.getProfileByUUID(configSpecifiedId);

                // If the profile has a uuid and a name, load it into the map.
                if (profile.isComplete()) {

                    this.knownSupporters.put(profile.getId(), profile);
                }
            }

            // Save the data to a file.
            this.lookupManager.save();
            
            LOG.info("Successfully loaded {} supporters. Took {}ms on a separate thread.", knownSupporters.size(), System.currentTimeMillis() - startTime);
        }).start();
    }
}