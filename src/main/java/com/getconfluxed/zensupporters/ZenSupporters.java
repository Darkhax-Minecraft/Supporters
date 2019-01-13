package com.getconfluxed.zensupporters;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.getconfluxed.zensupporters.data.ProfileManager;
import com.google.common.base.Charsets;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "zensupporters", name = "ZenSupporters", version = "@VERSION@", dependencies = "required-after:crafttweaker", certificateFingerprint = "@FINGERPRINT@")
public class ZenSupporters {

    public static final Logger LOG = LogManager.getLogger("ZenSupporters");
    
    private final ProfileManager lookupManager = new ProfileManager(new File("zencache.json"));
    private final Map<String, Supporter> map = new ConcurrentHashMap<>();

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) {

        this.loadSupporters();
    }

    /**
     * Triggers a reload of all the supporter data. This will spin off multiple threads, so
     * calls to this command should be used sparingly.
     */
    public void loadSupporters () {

        // TODO Change the amount of threads
        final ExecutorService executor = Executors.newFixedThreadPool(16);

        try {

            final long currentTime = new Date().getTime();

            // TODO add more sources for supporters
            // TODO add support for UUIDs instead of just usernames
            for (final String line : FileUtils.readLines(new File("config/entry.txt"), Charsets.UTF_8)) {

                executor.submit( () -> this.map.put(line, new Supporter(this.lookupManager.getProfileByUsername(line.toLowerCase(Locale.ROOT), currentTime))));
            }
        }

        catch (final IOException e) {

            LOG.catching(e);
        }

        executor.shutdown();
        this.lookupManager.save();

    }
}