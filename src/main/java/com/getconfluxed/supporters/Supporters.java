package com.getconfluxed.supporters;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.getconfluxed.supporters.command.CommandSupportersTree;
import com.getconfluxed.supporters.data.ProfileManager;
import com.mojang.authlib.GameProfile;

import net.darkhax.bookshelf.BookshelfRegistry;
import net.darkhax.bookshelf.util.SkullUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "supporters", name = "Supporters", version = "@VERSION@", dependencies = "required-after:bookshelf", certificateFingerprint = "@FINGERPRINT@")
public class Supporters {

    public static final Logger LOG = LogManager.getLogger("Supporters");

    @Instance("supporters")
    public static Supporters instance;

    private final File modDir = new File("config/supporters");
    private ProfileManager lookupManager;
    private Configs config;
    private Map<UUID, GameProfile> knownSupporters;
    private Map<UUID, ItemStack> skulls;

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) {

        if (!this.modDir.exists()) {

            this.modDir.mkdirs();
        }

        this.knownSupporters = new ConcurrentHashMap<>();
        this.skulls = new ConcurrentHashMap<>();
        this.lookupManager = new ProfileManager(new File(this.modDir, "supporters-cache.json"));
        this.config = new Configs(new File(this.modDir, "supporters.cfg"));
        this.loadSupporters(false);

        BookshelfRegistry.addCommand(new CommandSupportersTree());
    }

    /**
     * Checks if an ID belongs to a supporter.
     *
     * @param id The id to look for.
     * @return Whether or not the id belongs to a supporter.
     */
    public boolean isSupporter (UUID id) {

        return this.knownSupporters.containsKey(id);
    }

    /**
     * Gets a collection of items which are player heads of the supporters.
     *
     * @return An unmodifiable collection of supporters.
     */
    public Collection<ItemStack> getSupporterSkulls () {

        return Collections.unmodifiableCollection(this.skulls.values());
    }

    /**
     * Gets a collection of supporter profiles.
     *
     * @return An unmodifiable collection of supporters.
     */
    public Collection<GameProfile> getSupporters () {

        return Collections.unmodifiableCollection(this.knownSupporters.values());
    }

    /**
     * Triggers a reload of all the supporter data. This will spin off multiple threads, so
     * calls to this command should be used sparingly.
     */
    public void loadSupporters (boolean refresh) {

        // Clear the list of known supporters.
        this.knownSupporters.clear();
        this.skulls.clear();

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
                    this.skulls.put(profile.getId(), SkullUtils.createSkull(profile.getName()));
                }
            }

            // Save the data to a file.
            this.lookupManager.save();

            LOG.info("Successfully loaded {} supporters. Took {}ms on a separate thread.", this.knownSupporters.size(), System.currentTimeMillis() - startTime);
        }).start();
    }
}