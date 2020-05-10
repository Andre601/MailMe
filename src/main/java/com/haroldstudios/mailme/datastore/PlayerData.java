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
import com.haroldstudios.mailme.mail.Mail;

import com.haroldstudios.mailme.utility.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public final class PlayerData {

    private final TreeMap<Date,Mail> mail = new TreeMap<>();
    private com.haroldstudios.mailme.utility.Locale.LANG lang;
    private final UUID uuid;
    private boolean notify = true;
    public int x,y,z;
    public String world;

    /**
     * Class constructor
     * This should NEVER be accessed directly. Always go via PlayerDataHandler
     *
     * @param uuid UUID of player
     * @param lang Player's preferred language
     */
    public PlayerData(final UUID uuid, final com.haroldstudios.mailme.utility.Locale.LANG lang) {
        this.uuid = uuid;
        this.lang = lang;
        Location defaultLoc = MailMe.getInstance().getDataStoreHandler().defaultLocation;
        if (defaultLoc != null) {
            this.x = defaultLoc.getBlockX();
            this.y = defaultLoc.getBlockY();
            this.z = defaultLoc.getBlockZ();
            Bukkit.getScheduler().runTask(MailMe.getInstance(), () ->
                this.world = defaultLoc.getWorld() == null ? "world" : defaultLoc.getWorld().getName());
        }
    }


    /* Getters */

    /**
     * Gets Mail Objects in order - newest to oldest
     *
     * @return Player's mail newest to oldest
     */
    public List<Mail> getMail() {
        return new ArrayList<>(mail.descendingMap().values());
    }

    /**
     * Gets amount of Mail player has
     *
     * @return Integer Amount of Mail player has
     */
    public Integer getMailCount() {
        return mail.descendingMap().values().size();
    }

    /**
     * Gets the language the player has set
     *
     * @return Locale Language player is using
     */
    public com.haroldstudios.mailme.utility.Locale.LANG getLang() { return this.lang; }

    /**
     * Gets the player's UUID
     *
     * @return Player's UUID
     */
    public UUID getUuid() { return uuid; }

    /**
     * Gets the current notification preference
     *
     * @return True if allowed notifications
     */
    public boolean getNotifySetting() { return notify; }

    /**
     * Gets the player's mailbox location
     *
     * @return MailBox location
     */
    public Location getMailBox() {
        if (world != null)
            return new Location(Bukkit.getWorld(world), x, y, z);
        return null;
    }

    /* Setters */

    /**
     * Sets the player's notification preference
     *
     * @param notify if wants to receive notifications
     */
    public void setNotifySettings(boolean notify) {
        this.notify = notify;
        update();
    }

    /**
     * Sets the player's mailbox location
     *
     * @param location of new mailbox
     */
    public void setMailBox(Location location) {

        // If could potentially be in our cache
        if (getMailBox() != null) {
            // Remove from mailboxes cache
            MailMe.getInstance().getDataStoreHandler().getPlayerMailBoxes().remove(getMailBox());
        }


        if (location == null) {

            this.world = null;
            update();

            return;
        }
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
        update();

        MailMe.getInstance().getDataStoreHandler().getPlayerMailBoxes().put(location, getUuid());

        tryAddToTask(Bukkit.getPlayer(uuid));
    }

    /**
     * Adds Mail to Player's Map
     *
     * @param mail Mail Object
     */
    public void addMail(Mail mail) {
        this.mail.put(mail.getDate(), mail);
        update();

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline() && notify) {
            Player p = (Player) player;
            Locale locale = MailMe.getInstance().getLocale();
            String sender = mail.isAnonymous() ? locale.getMessage(getLang(), "mail.anonymous") : mail.isServer() ? locale.getMessage(getLang(), "mail.server") : mail.getSender() == null ? locale.getMessage(getLang(), "mail.unknown") : Bukkit.getOfflinePlayer(mail.getSender()).getName();
            p.sendMessage(String.format(MailMe.getInstance().getLocale().getMessage(getLang(), "notify.received"), sender));
        }

        tryAddToTask(player);
    }

    /**
     * Returns if the player has any unread mail
     *
     * @return If player has unread mail
     */
    public boolean hasUnreadMail() {
        return getMail().stream().anyMatch(m -> !m.isRead());
    }

    /**
     * Attempts to add player to mailbox ping task
     * Player must be online
     *
     * @param player OfflinePlayer
     */
    private void tryAddToTask(OfflinePlayer player) {
        if (player.isOnline()) {
            if (!MailMe.playerList.contains(player)) {
                MailMe.playerList.add(player);
            }
        }
    }

    /**
     * Removes mail from the player's DataStore
     *
     * @param mail Mail object
     */
    public void deleteMail(Mail mail) {
        this.mail.remove(mail.getDate());
        update();
    }

    /**
     * Sets Player's preferred language
     *
     * @param lang Locale Language
     */
    public void setLang(Locale.LANG lang) {
        this.lang = lang;
        update();
    }


    /**
     * Writes data to file.
     */
    public void update() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () ->
                Utils.writeJson(new File(MailMe.getInstance().getDataFolder() + "/playerdata/" + uuid.toString() + ".json"), this));

    }
}
