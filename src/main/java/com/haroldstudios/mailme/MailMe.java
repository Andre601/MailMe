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

package com.haroldstudios.mailme;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haroldstudios.mailme.components.hooks.VaultHook;
import com.haroldstudios.mailme.conversations.*;
import com.haroldstudios.mailme.events.EntityEvents;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailBuilder;
import com.haroldstudios.mailme.command.MailCmd;
import com.haroldstudios.mailme.components.hooks.PlaceholderAPIExpansion;
import com.haroldstudios.mailme.datastore.DataStoreHandler;
import com.haroldstudios.mailme.datastore.PlayerData;
import com.haroldstudios.mailme.mail.MailSerializer;
import com.haroldstudios.mailme.ui.GuiHandler;
import com.haroldstudios.mailme.utility.Locale;

import com.haroldstudios.mailme.utility.TypeAdapterFactory;
import com.haroldstudios.mailme.utility.Utils;
import me.mattstudios.mf.base.CommandManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class MailMe extends JavaPlugin {

    public static Gson GSON = new GsonBuilder()
            .setDateFormat("dd-MM-yyyy-hh:mm:ss.SSS")
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .registerTypeAdapter(Mail.class, new MailSerializer<Mail>())
            .registerTypeAdapterFactory(new TypeAdapterFactory())
            .create();

    /* Handlers */
    private Locale locale;
    private GuiHandler uiHandler;
    private DataStoreHandler dataStoreHandler;
    private MailCmd mailCmds;

    /* Conversation Factory */
    private ConversationFactory searchFactory;

    /* Hooks */
    private VaultHook vaultHook;

    public static final List<OfflinePlayer> playerList = new ArrayList<>();

    private void loadConversations() {
        this.searchFactory = new ConversationFactory(this).withModality(true)
                .withFirstPrompt(new SearchInput())
                .withEscapeSequence("cancel").withTimeout(60)
                .thatExcludesNonPlayersWithMessage("Console is not supported by this command");
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        @SuppressWarnings("unused")
        Metrics metrics = new Metrics(this);

        loadConversations();
        debug("Creating playerdata folder");
        new File(getDataFolder() + "/playerdata").mkdir();
        debug("Loading locale");
        this.locale = new Locale();
        debug("Loading GuiHandler");
        this.uiHandler = new GuiHandler(this);
        debug("Registering events");
        getServer().getPluginManager().registerEvents(new EntityEvents(this), this);
        debug("Loading playerdata handler");
        this.dataStoreHandler = new DataStoreHandler(this);
        debug("Loading commands");
        CommandManager commandManager = new CommandManager(this);
        commandManager.getCompletionHandler().register("#locale", val -> Arrays.stream(Locale.LANG.values()).map(Enum::toString).collect(Collectors.toList()));
        commandManager.getCompletionHandler().register("#boolean", val -> Arrays.asList("true", "false"));
        commandManager.getCompletionHandler().register("#mailbox", val -> Arrays.asList("remove", "set"));
        commandManager.getCompletionHandler().register("#admin-options", val -> Arrays.asList("send", "preset"));
        commandManager.getCompletionHandler().register("#admin-who", val -> Arrays.asList("<player>", "all"));
        commandManager.getCompletionHandler().register("#admin-id", val -> Collections.singletonList("[name-id]"));
        commandManager.getCompletionHandler().register("#send-type", val -> Collections.singletonList("MAIL_MESSAGES"));
        commandManager.getCompletionHandler().register("#contents", val -> Collections.singletonList("[content]]"));
        this.mailCmds = new MailCmd(this);
        commandManager.register(mailCmds);
        commandManager.hideTabComplete(true);

        runMailBoxTask();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlaceholderAPIExpansion(this).register();
        }
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            this.vaultHook = new VaultHook(this);
        }

    }

    /**
     * Runs the ping task for mailboxes
     *
     * @return BukkitTask
     */
    public BukkitTask runMailBoxTask() {

        return Bukkit.getScheduler().runTaskTimer(this, () -> {
            // Iterate through players with mailbox
            for (OfflinePlayer player : playerList) {
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    // Checks if player is still online
                    if (!player.isOnline()) return;

                    // Checks if player has mailbox
                    PlayerData data = getDataStoreHandler().getPlayerData(player);
                    if (data.getMailBox() == null) return;

                    Location loc = data.getMailBox();

                    // Ensures mailbox still exists
                    if (loc.getWorld() == null || loc.getWorld().getBlockAt(loc) == null || loc.getWorld().getBlockAt(loc).getType() == null) return;
                    if (loc.getWorld().getBlockAt(loc).getType() == Material.AIR) {
                        getDataStoreHandler().getPlayerData(player).setMailBox(null);
                        return;
                    }
                    loc.add(0.5, 1.2, 0.5);

                    // Checks if player has unread mail
                    List<Mail> unread = data.getMail().stream().filter(mail -> !mail.isRead()).collect(Collectors.toList());
                    if (unread.size() > 0)
                        Utils.playNoteEffect((Player) player, loc);
                });
            }
        },0L,80L);
    }

    @Override
    public void onDisable() {
    }

    /**
     * Sends debug messages to console if exists
     *
     * @param msg Message to send
     */
    public void debug(String msg) {
        if (getConfig().getBoolean("debug"))
            getServer().getLogger().log(Level.INFO, msg);
    }


    public Locale getLocale() {
        return locale;
    }

    public GuiHandler getGuiHandler() {
        return uiHandler;
    }

    public DataStoreHandler getDataStoreHandler() { return dataStoreHandler; }

    public ConversationFactory getInputFactory(MailBuilder builder) {
        return new ConversationFactory(this).withModality(true)
                .withFirstPrompt(new InputPrompt(builder))
                .withEscapeSequence("cancel").withTimeout(300)
                .addConversationAbandonedListener(new ConversationAbandonedListener())
                .thatExcludesNonPlayersWithMessage("Console is not supported by this command");
    }

    public ConversationFactory getSearchFactory() {
        return searchFactory;
    }

    public ConversationFactory getSearchPlayerFactory(MailBuilder builder) { return new ConversationFactory(this).withModality(true)
            .withFirstPrompt(new PlayerSearch(builder))
            .withEscapeSequence("cancel").withTimeout(300)
            .thatExcludesNonPlayersWithMessage("Console is not supported by this command"); }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public MailCmd getCmds() {
        return mailCmds;
    }

    public static MailMe getInstance() {
        return getPlugin(MailMe.class);
    }
}
