/*
 *   Copyright [2020] [Harry0198]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.haroldstudios.mailme.datastore;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.utility.Locale;
import com.haroldstudios.mailme.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DataStoreHandler {

    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final MailMe plugin;
    /* Location of default mailbox location */
    public Location defaultLocation;
    @Nullable public List<Material> mailboxMaterials = new ArrayList<>();
    private Map<Location, UUID> playerMailBoxes = new HashMap<>();

    public DataStoreHandler(MailMe plugin) {
        this.plugin = plugin;
        String world = plugin.getConfig().getString("default-mailbox.world");
        int x = plugin.getConfig().getInt("default-mailbox.x");
        int y = plugin.getConfig().getInt("default-mailbox.y");
        int z = plugin.getConfig().getInt("default-mailbox.z");
        defaultLocation = new Location(Bukkit.getWorld(world),x,y,z);

        if (plugin.getConfig().getBoolean("restricted-mailboxes")) {
            for (String each : plugin.getConfig().getStringList("valid-mailboxes")) {
                Material material;
                try {
                    material = Material.valueOf(each);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "Invalid MailBox Type Material: " + each);
                    return;
                }
                mailboxMaterials.add(material);
            }
        } else {
            // Removes list
            mailboxMaterials = null;
        }

        try (Stream<Path> walk = Files.walk(Paths.get(MailMe.getInstance().getDataFolder() + "/playerdata"))) {
            List<String> result = walk.map(Path::toString)
                    .filter(f -> f.endsWith(".json")).collect(Collectors.toList());

            result.forEach(file -> {
                PlayerData data = getTmpFromFile(new File(file));
                Location mailbox = data.getMailBox();
                if (mailbox == null) return;

                playerMailBoxes.put(mailbox, data.getUuid());
            });

        } catch (IOException exception) {
            MailMe.getInstance().debug("Failed to send as admin: " + exception.getMessage());
        }
    }

    public PlayerData getPlayerData(OfflinePlayer player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (!playerDataExists(uuid))
            makeData(uuid);
        return playerData.get(uuid);
    }

    public void forceSetPlayerData(PlayerData data) {
        if (playerData.containsKey(data.getUuid()))
            playerData.replace(data.getUuid(), data);
    }

    public List<Material> getValidMailBoxMaterials() {
        return mailboxMaterials;
    }

    public PlayerData getTmpFromFile(File file) {
        return Utils.readJson(file);
    }

    public void removePlayerData(UUID uuid) {
        if (playerDataExists(uuid))
            playerData.remove(uuid);
    }

    public void makeData(UUID uuid) {

        final File jsonData = new File(plugin.getDataFolder() + "/playerdata/" + uuid.toString() + ".json");
        try {
            jsonData.createNewFile();
        } catch (IOException io) {
            io.printStackTrace();
        }
        PlayerData data = Utils.readJson(jsonData);

        // If PlayerData exists in file
        if (data != null) {
            playerData.put(uuid, data);
            return;
        }

        PlayerData newData = new PlayerData(uuid, Locale.LANG.valueOf(plugin.getConfig().getString("lang")));
        Utils.writeJson(new File(MailMe.getInstance().getDataFolder() + "/playerdata/" + uuid.toString() + ".json"), newData);
        this.playerData.put(uuid, newData);
    }

    /**
     * Getter for the default mailbox location
     *
     * @return Location Object of mailbox
     */
    private Location getDefaultMailBoxLocation() {
        return defaultLocation;
    }

    public Map<Location, UUID> getPlayerMailBoxes() {
        return playerMailBoxes;
    }

    public boolean playerDataExists(UUID uuid) {
        return playerData.containsKey(uuid);
    }

    public boolean playerDataFileExists(UUID uuid) {
        return new File(plugin.getDataFolder() + "/playerdata/" + uuid.toString() + ".json").exists();
    }

}
