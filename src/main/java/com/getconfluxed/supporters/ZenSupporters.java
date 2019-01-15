package com.getconfluxed.supporters;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.getconfluxed.supporters.data.ProfileManager;
import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "supporters", name = "Supporters", version = "@VERSION@", dependencies = "", certificateFingerprint = "@FINGERPRINT@")
public class ZenSupporters {

    public static final Logger LOG = LogManager.getLogger("Supporters");
    
    @Instance("supporters")
    public static ZenSupporters instance;

    private final int CORE_COUNT = getCoreCount();
    private final File DIR = new File("config/supporters");
    private ProfileManager lookupManager;
    private Configs config;
    private Map<UUID, GameProfile> knownSupporters;

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) {

        if (!DIR.exists()) {
            
            DIR.mkdirs();
        }
        
        this.knownSupporters = new HashMap<>();
        this.lookupManager = new ProfileManager(new File(DIR, "supporters-cache.json"));
        this.config = new Configs(new File(DIR, "supporters.cfg"));
        this.loadSupporters(false);
        LOG.info("Detected {} cores. Allocating that many threads to the thread pool.", this.CORE_COUNT);
    }

    public boolean isSupporter (UUID id) {

        return this.knownSupporters.containsKey(id);
    }

    /**
     * Triggers a reload of all the supporter data. This will spin off multiple threads, so
     * calls to this command should be used sparingly.
     */
    public void loadSupporters (boolean refresh) {

        this.knownSupporters.clear();

        new Thread( () -> {

            this.lookupManager.load(refresh);
            // Create a thread pool with the number of cores.
            final ExecutorService executor = Executors.newFixedThreadPool(this.CORE_COUNT);

            for (final UUID hardcodedId : this.config.supporterUUIDs) {

                executor.submit( () -> {

                    final GameProfile profile = this.lookupManager.getProfileByUUID(hardcodedId);

                    if (!profile.isComplete()) {

                        this.knownSupporters.put(profile.getId(), profile);
                    }
                });
            }

            executor.shutdown();

            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch (final InterruptedException e) {
            }
            this.lookupManager.save();
        }).start();
    }

    private static int getCoreCount () {

        try {

            return Runtime.getRuntime().availableProcessors();
        }

        catch (final Exception e) {

            return 4;
        }
    }
}