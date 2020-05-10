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

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.datastore.PlayerData;

import com.haroldstudios.mailme.mail.types.MailItems;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public abstract class Mail implements Cloneable {

    private ItemStack icon;
    private Date date;
    private final List<UUID> recipients = new ArrayList<>();
    private boolean read = false;
    private boolean reply = false;
    private UUID sender;
    private final int delay;
    private final boolean anonymous;
    private final boolean server;

    public Mail(ItemStack icon, Date date, boolean anonymous, boolean server) {
        this.icon = icon;
        this.anonymous = anonymous;
        this.server = server;
        this.date = date;
        delay = MailMe.getInstance().getConfig().getInt("delay");
    }

    /**
     * Gets Type of Mail
     *
     * @return MailType
     */
    public abstract MailType getMailType();

    /**
     * Gets Content as Text Form
     * @return BaseComponent
     */
    public abstract BaseComponent[] getContentsAsText();

    /**
     * Clone the Mail Object so it is separated from reference object
     * @return Cloned Mail Object
     */
    @Override
    public abstract Mail clone();

    /**
     * Click action in GUI
     *
     * @param player Player
     */
    public abstract void onClick(Player player);


    /* Getters */

    /**
     * Gets Date sent
     *
     * @return Date
     */
    public Date getDate() { return date; }

    /**
     * Gets Icon set
     *
     * @return ItemStack ICON
     */
    public ItemStack getIcon() {
        return icon;
    }

    /**
     * If this mail has been read before or not
     *
     * @return if read or not
     */
    public boolean isRead() {
        return read;
    }

    public boolean isServer() { return server; }

    /**
     * Has mail be replied to
     *
     * @return if been replied to
     */
    @Deprecated
    public boolean isReply() {
        return reply;
    }

    public boolean isAnonymous() { return anonymous; }

    /**
     * Gets the sender of the mail
     *
     * @return UUID of sender
     */
    public UUID getSender() { return sender; }

    /**
     * List of recipients
     *
     * @return List of UUIDs of recipients
     */
    public List<UUID> getRecipients() { return recipients; }

    /* Setters */

    /**
     * Sets icon of Mail
     *
     * @param icon ItemStack
     */
    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    /**
     * Sets the read status
     *
     * @param read status to set to
     * @param player Player to set
     */
    public void setRead(boolean read, Player player) {
        this.read = read;
        MailMe.getInstance().getDataStoreHandler().getPlayerData(player).update();
    }

    /**
     * Sets reply status
     *
     * @param reply Status to set to
     */
    @Deprecated
    public void setReply(boolean reply) {
        this.reply = reply;
    }

    /**
     * Sets sender of mail
     *
     * @param uuid UUID of sender
     */
    public void setSender(UUID uuid) { this.sender = uuid; }

    /**
     * Clears the recipients
     */
    public void clearRecipients() {
        recipients.clear();
    }

    /**
     * Adds recipients
     *
     * @param players List of Players
     */
    public void addRecipients(List<UUID> players) {
        recipients.addAll(players);
    }

    /**
     * Adds recipients
     *
     * @param players List of Players via UUID
     */
    public void addRecipientsUUID(List<UUID> players) {
        recipients.addAll(players);
    }

    /**
     * Recipient to remove from list
     *
     * @param player Player to remove
     */
    public void removeRecipient(OfflinePlayer player) {
        recipients.remove(player.getUniqueId());
    }

    /**
     * Sends the mail
     */
    public void sendMail() {

        List<UUID> recipients = getRecipients();
        List<UUID> newList = new ArrayList<>(recipients);

        if (getSender() == null) {
            sendAsServer();
            return;
        }

        PlayerData senderData = MailMe.getInstance().getDataStoreHandler().getPlayerData(sender);

        for (UUID player : recipients) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, -delay);
            Date timeTravel = cal.getTime();
            PlayerData data = MailMe.getInstance().getDataStoreHandler().getPlayerData(player);
            List<Mail> dataMail = data.getMail().stream().filter(r -> r.getDate().getTime() > timeTravel.getTime()).filter(r -> r.getSender().equals(sender)).collect(Collectors.toList());
            if (!dataMail.isEmpty()) {
                if (getMailType().equals(MailType.MAIL_ITEM)) {
                    MailItems mailItems = (MailItems) this;
                    mailItems.giveItems(Bukkit.getPlayer(sender));
                }
                Bukkit.getPlayer(sender).sendMessage(String.format(MailMe.getInstance().getLocale().getMessage(senderData.getLang(), "notify.could-not-send"), Bukkit.getOfflinePlayer(player).getName()));
                newList.remove(player);
            }
        }

        if (sender.toString().length() != 36) return;

        List<Object> newestList;
        newestList = newList.stream().map(pl -> Bukkit.getOfflinePlayer(pl).getName()).collect(Collectors.toList());

        // If there are more than x recipients, instead of listing all players, shorten list
        if (newestList.size() > 5) {
            newestList.clear();
            newestList.add(MailMe.getInstance().getLocale().getMessage(senderData.getLang(), "notify.sent-multiple"));
        }

        if (MailMe.getInstance().getVaultHook() != null && !MailMe.getInstance().getVaultHook().attemptTransaction(Bukkit.getPlayer(sender))) {
            if (getMailType().equals(MailType.MAIL_ITEM)) {
                MailItems mailItems = (MailItems) this;
                mailItems.giveItems(Bukkit.getPlayer(sender));
            }
            return;
        }

        try {
            if (Bukkit.getPlayer(sender) != null || sender.equals(Bukkit.getPlayer(sender).getUniqueId())) {
                Bukkit.getPlayer(sender).sendMessage(String.format(MailMe.getInstance().getLocale().getMessage(senderData.getLang(), "notify.sent"), newestList));
            }
        } catch (NullPointerException ignore) { }
        newList.forEach(player -> MailMe.getInstance().getDataStoreHandler().getPlayerData(player).addMail(this.clone()));

    }

    /**
     * Sends the mail as if came from server / admin
     * (Bypasses Checks and Removes Sender)
     */
    public void sendAsServer() {
        this.date = new Date();

        getRecipients().forEach(player -> {

            PlayerData data = MailMe.getInstance().getDataStoreHandler().getPlayerData(player);
            Mail mail = this.clone();
            data.addMail(mail);

            MailMe.getInstance().getDataStoreHandler().forceSetPlayerData(data);
        });

        // NEEDED? PlayerData senderData = MailMe.getInstance().getDataStoreHandler().getPlayerData(sender);
    }

    /**
     * Gets the mail in its text form
     * Send using player.spigot().sendMessage();
     *
     * @return BaseComponent
     */
    public BaseComponent[] getMailAsText() {
        List<String> msgs = MailMe.getInstance().getLocale().getMessages("text.format");
        String sender = getSender() != null ? Bukkit.getOfflinePlayer(getSender()).getName() : "???";
        ComponentBuilder builder = new ComponentBuilder("");

        builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mailme reply"));
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MailMe.getInstance().getLocale().getMessage("text.hover")).create()));

        OfflinePlayer sendr = Bukkit.getOfflinePlayer(getSender());

        for (String each : msgs) {
            String t = each + "\n";
            t = t.replaceAll("%time%", getDate().toString());
            if (isAnonymous()) {
                t = t.replaceAll("%sender%", "???");
            }
            t = t.replaceAll("%sender%", getSender() == null ? "???" : !sendr.hasPlayedBefore() ?  "???": sendr.getName() == null ? "???" : sendr.getName());
            if (!t.contains("%contents%")) {
                builder.append(new TextComponent(t));
                continue;
            }
            t = t.replaceAll("%contents%", "");
            builder.append(new TextComponent(t));

            builder.append(getContentsAsText());
        }

        return builder.create();
    }

    /**
     * Mail Types
     */
    public enum MailType {
        MAIL_ITEM, MAIL_MESSAGE, MAIL_SOUND, MAIL_LOCATION,
    }
}

