package com.getconfluxed.supporters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;

import net.minecraftforge.common.config.Configuration;

public class Configs {

    /**
     * Forges configuration object.
     */
    private final Configuration config;

    /**
     * A list of all the UUIDs that have been loaded from all the sources.
     */
    public List<UUID> supporterUUIDs;

    public Configs (File file) {

        this.config = new Configuration(file);
        this.supporterUUIDs = new ArrayList<>();
        this.syncConfigData();
    }

    /**
     * Syncs the data in the config into ram. This will reload the file, and will also reload
     * any remote sources that have been configured.
     */
    private void syncConfigData () {

        // Clear out previous UUIDs.
        this.supporterUUIDs.clear();

        // Collect the UUIDs again from the configured sources.
        this.supporterUUIDs.addAll(this.getPredefinedSupporters());
        this.supporterUUIDs.addAll(this.getRemotesSupporters());

        // If the config file isn't the same, save it to the file.
        if (this.config.hasChanged()) {

            this.config.save();
        }
    }

    /**
     * Gets UUIDs from remote sources. This expects the page connected to, to return a bunch of
     * uuid strings separated by new line.
     *
     * @return The list of all UUIDs loaded from remote sources.
     */
    private List<UUID> getRemotesSupporters () {

        final String[] urls = this.config.getStringList("remotePlayers", "remote", new String[] { "https://gist.githubusercontent.com/darkhax/bb02babc2e702473b8d01367e3e0effe/raw/eb318af1b4b53052f47fe3637ac7737cf5d14977/web-test.txt" }, "This list is used to provide player sources from remote locations. This means you can update the list without pushing updates to the packs and servers directly.");
        final List<UUID> remoteIds = new ArrayList<>();

        // Iterate all the specified URLs and try to load them.
        for (final String url : urls) {

            try {

                // Connect to the URL and read the response as a string.
                final String response = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);

                // TODO This will eventually support reading other formats like json as well.

                // Split the response on every new line, and try to load it as a UUID.
                for (final String line : response.split("\\R")) {

                    remoteIds.add(UUID.fromString(line));
                }
            }

            catch (final IOException e) {

                ZenSupporters.LOG.error("Unable to read player data from {}.", url);
            }
        }

        return remoteIds;
    }

    /**
     * Get a list of predefined player Ids.
     *
     * @return The list of ids predefined in the configuration file.
     */
    private List<UUID> getPredefinedSupporters () {

        final List<UUID> predefinedUUIDs = new ArrayList<>();
        final String[] predefinedIds = this.config.getStringList("predefinedPlayers", "predefined", new String[] { "d183e5a2-a087-462a-963e-c3d7295f9ec5", "3bf32666-f9ba-4060-af02-53bdb0df38fc" }, "A list of predefined users to include. This list is included as part of the pack, and is not updated remotely.");

        for (final String hardcodedId : predefinedIds) {

            predefinedUUIDs.add(UUID.fromString(hardcodedId));
        }

        return predefinedUUIDs;
    }
}