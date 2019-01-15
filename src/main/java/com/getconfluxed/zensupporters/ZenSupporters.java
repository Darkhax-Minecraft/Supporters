package com.getconfluxed.zensupporters;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.getconfluxed.zensupporters.data.ProfileManager;
import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "zensupporters", name = "ZenSupporters", version = "@VERSION@", dependencies = "required-after:crafttweaker", certificateFingerprint = "@FINGERPRINT@")
public class ZenSupporters {

    public static final Logger LOG = LogManager.getLogger("ZenSupporters");

    private final int CORE_COUNT = getCoreCount();
    private ProfileManager lookupManager;
    private Configs config;

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) {

        this.lookupManager = new ProfileManager(new File("zencache.json"));
        this.config = new Configs(event.getSuggestedConfigurationFile());
        this.loadSupporters(false);
        LOG.info("Detected {} cores. Allocating that many threads to the thread pool.", this.CORE_COUNT);
    }

    /**
     * Triggers a reload of all the supporter data. This will spin off multiple threads, so
     * calls to this command should be used sparingly.
     */
    public void loadSupporters (boolean refresh) {

        new Thread( () -> {

            this.lookupManager.load(refresh);
            // Create a thread pool with the number of cores.
            final ExecutorService executor = Executors.newFixedThreadPool(this.CORE_COUNT);

            for (final UUID hardcodedId : this.config.supporterUUIDs) {

                executor.submit( () -> {

                    final GameProfile profile = this.lookupManager.getProfileByUUID(hardcodedId);
                });
            }

            executor.shutdown();

            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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