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

package com.haroldstudios.mailme.mail;

import com.google.gson.reflect.TypeToken;
import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.components.IncompleteBuilderException;
import com.haroldstudios.mailme.mail.types.MailItems;
import com.haroldstudios.mailme.mail.types.MailLocation;
import com.haroldstudios.mailme.mail.types.MailMessages;
import com.haroldstudios.mailme.mail.types.MailSound;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class MailBuilder {

    @Nullable private Mail.MailType mailType;
    @Nullable private UUID sender;
    private String message;
    private ItemStack icon;
    private final List<UUID> recipients;
    private final List<ItemStack> items;
    private Sound sound;
    private Location location;
    private boolean preset;
    private String presetName;
    private boolean anonymous = false;
    private boolean serverSender = false;

    /**
     * MailMailBuilder Class Constructor
     * Create using new MailMailBuilder#MailBuilder()
     */
    public MailBuilder() {
        this.recipients = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    /**
     * Sets a preset mail for sending
     *
     * @param preset Preset
     * @return The MailBuilder
     */
    public MailBuilder setPreset(boolean preset) {
        this.preset = preset;
        return this;
    }

    /**
     * Sets a preset mail for sending
     *
     * @param name Preset name
     * @return The MailBuilder
     */
    public MailBuilder setPresetName(String name) {
        this.presetName = name;
        return this;
    }

    public MailBuilder setServerSender(boolean bool) {
        this.serverSender = bool;
        return this;
    }

    /**
     * Sets the mail type
     *
     * @param type MailType
     * @return The MailBuilder
     */
    public MailBuilder setMailType(Mail.MailType type) {
        this.mailType = type;
        return this;
    }

    /**
     * Adds a single recipient to the mail
     *
     * @param player OfflinePlayer to send to
     * @return The MailBuilder
     */
    public MailBuilder addRecipient(OfflinePlayer player) {
        recipients.add(player.getUniqueId());
        return this;
    }
    public MailBuilder addRecipient(UUID player) {
        recipients.add(player);
        return this;
    }

    /**
     * Adds a list of recipients to the mail object
     *
     * @param players List of OfflinePlayers
     * @return The MailBuilder
     */
    public MailBuilder addRecipients(List<UUID> players) {
        recipients.addAll(players);
        return this;
    }

    /**
     * Adds an array of recipients to the mail object
     *
     * @param players Array of OfflinePlayers
     * @return The MailBuilder
     */
    public MailBuilder addRecipients(UUID... players) {
        recipients.addAll(Arrays.asList(players));
        return this;
    }

    /**
     * Removes a single recipient from the mail
     *
     * @param player OfflinePlayer to remove from mail
     * @return The MailBuilder
     */
    public MailBuilder removeRecipient(OfflinePlayer player) {
        recipients.remove(player);
        return this;
    }

    public MailBuilder setAnonymous(boolean bool) {
        this.anonymous = bool;
        return this;
    }

    public MailBuilder clearRecipients() {
        this.recipients.clear();
        return this;
    }

    public boolean isAnonymous() {
        return this.anonymous;
    }

    /**
     * Sets a message for the MailMessages type enum (Message sending)
     *
     * @param message String to send
     * @return The MailBuilder
     */
    public MailBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Allows for updating the sender of the mail
     *
     * @param sender Player to set - null = server
     * @return The MailBuilder
     */
    public MailBuilder setSender(UUID sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Sets the icon of the mail
     *
     * @param stack ItemStack icon
     * @return The MailBuilder
     */
    public MailBuilder setIcon(ItemStack stack) {
        this.icon = stack;
        return this;
    }

    /**
     * Adds items for the MailItems type enum (Item sending)
     *
     * @param items List of items to send
     * @return The MailBuilder
     */
    public MailBuilder addItems(List<ItemStack> items) {
        this.items.addAll(items);
        return this;
    }

    /**
     * Adds a single item for the MailItems type enum (Item sending)
     *
     * @param item ItemStack to send
     * @return The MailBuilder
     */
    public MailBuilder addItem(ItemStack item) {
        this.items.add(item);
        return this;
    }

    /**
     * Sets the sound for the MailSound type enum (Sound sending)
     *
     * @param sound Sound to send
     * @return The MailBuilder
     */
    public MailBuilder setSound(Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Sets the location for the MailLocation type enum (Location sending)
     *
     * @param location Location to send
     * @return The MailBuilder
     */
    public MailBuilder setLocation(Location location) {
        this.location = location;
        return this;
    }

    /**
     * Getter for the MailType
     *
     * @return {@link Mail.MailType}
     */
    public Mail.MailType getMailType() {
        return mailType;
    }

    /**
     * Getter for the recipients of mail
     *
     * @return List of OfflinePlayers for recipients
     */
    public List<UUID> getRecipients() {
        return recipients;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public boolean isPreset() {
        return preset;
    }

    public UUID getSender() {
        return sender;
    }

    public boolean isServerSender() {
        return serverSender;
    }

    /**
     * Creates the mail object dependent on the MailType enum used
     *
     * @return The Mail Object
     * @throws IncompleteBuilderException if there are missing values
     */
    public Mail build() throws IncompleteBuilderException {

        if (mailType == null) throw new IncompleteBuilderException("Mail Type has not been set!");
        if (icon == null) throw new IncompleteBuilderException("Icon has not been set!");
        if (recipients.isEmpty()) throw new IncompleteBuilderException("You must add recipients!");

        Mail mail = null;

        switch (mailType) {
            case MAIL_ITEM:
                if (items.isEmpty()) throw new IncompleteBuilderException("Items can't be empty!");
                mail = new MailItems(icon, recipients, items, sender, anonymous, isServerSender());
                break;
            case MAIL_MESSAGE:
                if (message == null) throw new IncompleteBuilderException("You must set a message!");
                mail = new MailMessages(icon, new Date(), recipients, message, sender, anonymous, isServerSender());
                break;
            case MAIL_SOUND:
                if (sound == null) throw new IncompleteBuilderException("Sound is null for Sound Mail!");
                mail = new MailSound(icon, recipients, sound, message, sender, anonymous, isServerSender());
                break;
            case MAIL_LOCATION:
                if (location == null) throw new IncompleteBuilderException("Location is null for Location Mail!");
                mail = new MailLocation(icon, new Date(), recipients, location, sender, anonymous, isServerSender());
                break;


        }

        if (isPreset()) {
            // Sends creator the mail back.
            mail.sendAsServer();
            mail.clearRecipients();
            Type token = new TypeToken<Mail>() {
            }.getType();
            MailMe.getInstance().getConfig().set("presets." + presetName, MailMe.GSON.toJson(mail, token));
            MailMe.getInstance().saveConfig();
        }

        return mail;
    }
}
