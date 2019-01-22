package net.darkhax.supporters;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.authlib.GameProfile;

import net.darkhax.bookshelf.BookshelfRegistry;
import net.darkhax.bookshelf.lib.Constants;
import net.darkhax.bookshelf.util.SkullUtils;
import net.darkhax.supporters.command.CommandSupportersTree;
import net.darkhax.supporters.data.ProfileManager;
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
    private List<UUID> allSupporters;

    @EventHandler
    public void onPreInit (FMLPreInitializationEvent event) {

        if (!this.modDir.exists()) {

            this.modDir.mkdirs();
        }

        this.knownSupporters = new ConcurrentHashMap<>();
        this.skulls = new ConcurrentHashMap<>();
        this.allSupporters = Collections.synchronizedList(new ArrayList<>());
        this.lookupManager = new ProfileManager(new File(this.modDir, "supporters-cache.json"));
        this.config = new Configs(new File(this.modDir, "supporters.cfg"));
        this.loadSupporters(false);

        BookshelfRegistry.addCommand(new CommandSupportersTree());
    }

    /**
     * Provides access to the profile lookup manager.
     *
     * @return The internal lookup manager.
     */
    public ProfileManager getLookupManager () {

        return this.lookupManager;
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
     * Gets a supporter by their UUID. If not supporter is found this will be null.
     *
     * @param id The unique ID of a player to lookup.
     * @return The profile data associated with the passed id.
     */
    public GameProfile getSupporter (UUID id) {

        return this.knownSupporters.get(id);
    }

    /**
     * Gets a supporter's skull by their UUID.
     *
     * @param id The unique ID of a player to lookup.
     * @return The skull itemstack associated with the passed id.
     */
    public ItemStack getSupporterHead (UUID id) {

        return this.skulls.get(id);
    }

    /**
     * Gets the UUID of a random supporter.
     *
     * @return A random supporter's uuid.
     */
    public UUID getRandomSupporter () {

        return this.allSupporters.isEmpty() ? null : this.allSupporters.get(Constants.RANDOM.nextInt(this.allSupporters.size()));
    }

    /**
     * Triggers a reload of all the supporter data. This will spin off multiple threads, so
     * calls to this command should be used sparingly.
     */
    public void loadSupporters (boolean refresh) {

        // Clear the list of known supporters.
        this.knownSupporters.clear();
        this.skulls.clear();
        this.allSupporters.clear();

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
                    this.allSupporters.add(profile.getId());
                }
            }

            // Save the data to a file.
            this.lookupManager.save();

            LOG.info("Successfully loaded {} supporters. Took {}ms on a separate thread.", this.knownSupporters.size(), System.currentTimeMillis() - startTime);
        }).start();
    }
}