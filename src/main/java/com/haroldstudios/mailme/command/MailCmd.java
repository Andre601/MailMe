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

package com.haroldstudios.mailme.command;

import com.google.gson.reflect.TypeToken;
import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.components.IncompleteBuilderException;
import com.haroldstudios.mailme.datastore.DataStoreHandler;
import com.haroldstudios.mailme.datastore.PlayerData;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailBuilder;
import com.haroldstudios.mailme.ui.MailGui;
import com.haroldstudios.mailme.utility.Locale;
import com.haroldstudios.mailme.utility.Pagination;
import com.haroldstudios.mailme.utility.Utils;
import me.mattstudios.mf.annotations.*;
import me.mattstudios.mf.annotations.Optional;
import me.mattstudios.mf.base.CommandBase;
import me.mattstudios.mf.exceptions.SyntaxError;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command("mail")
@Alias("mailme")
@SuppressWarnings({"unused"})
public final class MailCmd extends CommandBase {

    private final static String BASE_PERM = "mailme.base.";
    private final static String ADMIN_PERM = "mailme.admin";

    private final MailMe plugin;

    public MailCmd(MailMe plugin) {
        this.plugin = plugin;
    }

    @Default
    public void execute(Player player) {
        player.sendMessage(plugin.getLocale().getLore(plugin.getDataStoreHandler().getPlayerData(player).getLang(), "help"));
    }

    @Permission(BASE_PERM + "lang")
    @SubCommand("lang")
    @Completion("#locale")
    public void setLanguage(Player player, String lang) {
        lang = lang.toUpperCase();
        try {
            plugin.getDataStoreHandler().getPlayerData(player.getUniqueId()).setLang(Locale.LANG.valueOf(lang));
        } catch (IllegalArgumentException e) {
            throw new SyntaxError();
        }
        player.sendMessage(plugin.getLocale().getMessage(Locale.LANG.valueOf(lang), "cmd.lang-change"));
    }

    @Permission(BASE_PERM + "notify")
    @SubCommand("notify")
    @Completion("#boolean")
    public void setNotifySetting(Player player, Boolean bool) {
        PlayerData data = plugin.getDataStoreHandler().getPlayerData(player);
        data.setNotifySettings(bool);
        player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "cmd.notify-change"));
    }

    @Permission(BASE_PERM + "read")
    @SubCommand("read")
    public void read(Player player) {
        new MailGui(plugin, player, plugin.getDataStoreHandler().getPlayerData(player.getUniqueId()).getMail(), 0).open();
    }

    @Permission(BASE_PERM + "send")
    @SubCommand("reply")
    public void reply(Player player, String pl) {

        PlayerData data = plugin.getDataStoreHandler().getPlayerData(player);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            OfflinePlayer target = Bukkit.getOfflinePlayer(pl);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (target == null || !target.hasPlayedBefore()) {
                    plugin.getLocale().getMessage(data.getLang(), "cmd.unknown-player");
                    return;
                }


                plugin.getGuiHandler().getChooseTypeGui().open(player, new MailBuilder().addRecipient(target));
            });
        });
    }


    @Permission(BASE_PERM + "send")
    @SubCommand("send")
    // /mailme send [playername] [type] [contents]
    public void send(CommandSender sender, String[] args) {
        // If just sending via gui
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("No console! :(");
                return;
            }
            plugin.getGuiHandler().getChooseTypeGui().open((Player) sender, new MailBuilder());
            return;
        }

        // If sending via commands
        if (!(args.length >= 4)) {
            throw new SyntaxError();
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Mail.MailType type;
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
            String contents = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            
            boolean server = !(sender instanceof Player);

            try {
                type = Mail.MailType.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cSyntax Error!");
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {

                Mail mail;
                try {
                    if (server) {
                        // Sets sender to receiver.
                        mail = new MailBuilder().setServerSender(true).setMailType(Mail.MailType.MAIL_MESSAGE).setIcon(new ItemStack(Material.RED_BANNER)).addRecipient(offlinePlayer).setMessage(contents).build();
                    } else {
                        mail = new MailBuilder().setSender(((Player) sender).getUniqueId()).setMailType(Mail.MailType.MAIL_MESSAGE).setAnonymous(false).setIcon(new ItemStack(Material.RED_BANNER)).addRecipient(offlinePlayer).setMessage(contents).build();
                    }
                } catch (IncompleteBuilderException e) {
                    e.printStackTrace();
                    return;
                }

                mail.sendMail();

            });
        });




    }

    @Permission(BASE_PERM + "text")
    @SubCommand("text")
    public void readAsText(Player player, @Optional Integer page) {

        PlayerData data = plugin.getDataStoreHandler().getPlayerData(player.getUniqueId());
        Locale.LANG lang = data.getLang();

        if (page == null) page = 0;
        if (page > 0) {
            TextComponent message = new TextComponent(MailMe.getInstance().getLocale().getMessage(lang, "text.prev-page"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MailMe.getInstance().getLocale().getMessage(lang, "text.prev-page-hover")).create()));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mailme text " + (page - 1)));
            player.spigot().sendMessage(message);
        }

        Pagination<Mail> pagination = new Pagination<>(2, plugin.getDataStoreHandler().getPlayerData(player).getMail());

        if (page < pagination.totalPages()) {
            pagination.getPage(page).forEach(m -> player.spigot().sendMessage(m.getMailAsText()));

            TextComponent m = new TextComponent(MailMe.getInstance().getLocale().getMessage(lang, "text.next-page"));
            m.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MailMe.getInstance().getLocale().getMessage(lang, "text.next-page-hover")).create()));
            m.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mailme text " + (page + 1)));
            player.spigot().sendMessage(m);
        }
    }

    @Permission(BASE_PERM + "mailbox")
    @SubCommand("mailbox")
    @Completion("#mailbox")
    public void mailBox(Player player, String string) {

        PlayerData data = plugin.getDataStoreHandler().getPlayerData(player);

        if (string.equalsIgnoreCase("set")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Block chest = player.getTargetBlock(null, 10);
                if (chest == null || chest.getType() == null) {
                    System.out.println("failed 1");
                    player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "mailbox.not-chest"));
                    return;
                }

                if (plugin.getDataStoreHandler().getValidMailBoxMaterials() != null) {
                    System.out.println(plugin.getDataStoreHandler().getValidMailBoxMaterials());
                    if (!plugin.getDataStoreHandler().getValidMailBoxMaterials().contains(chest.getType())) {
                        System.out.println("failed 2");
                        System.out.println(chest.getType());
                        System.out.println(plugin.getDataStoreHandler().getValidMailBoxMaterials().contains(chest.getType()));
                        player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "mailbox.not-chest"));
                        return;
                    }
                }

                Location loc = chest.getLocation();

                Bukkit.getScheduler().runTask(plugin, () -> plugin.getDataStoreHandler().getPlayerData(player).setMailBox(loc));
                player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "mailbox.mailbox-set"));
            });
        } else if (string.equalsIgnoreCase("remove")) {
            data.setMailBox(null);
            player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "mailbox.mailbox-removed"));
        }
    }

    @Permission(ADMIN_PERM)
    @SubCommand("defaultMB")
    public void setDefaultMailBox(Player player) {

        PlayerData data = plugin.getDataStoreHandler().getPlayerData(player);
        ConfigurationSection mailboxConfig = plugin.getConfig().getConfigurationSection("default-mailbox");
        DataStoreHandler store = plugin.getDataStoreHandler();
        Location oldLocation = store.defaultLocation;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Block chest = player.getTargetBlock(null, 10);
            if (!chest.getType().equals(Material.CHEST)) {
                player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "mailbox.not-chest"));
                return;
            }
            Location loc = chest.getLocation();
            Bukkit.getScheduler().runTask(MailMe.getInstance(), () -> {
                mailboxConfig.set("world", loc.getWorld().getName());
                mailboxConfig.set("x", loc.getBlockX());
                mailboxConfig.set("y", loc.getBlockY());
                mailboxConfig.set("z", loc.getBlockZ());
                plugin.saveConfig();
            });

            plugin.getDataStoreHandler().defaultLocation = loc;

            player.sendMessage(plugin.getLocale().getMessage(data.getLang(), "mailbox.mailbox-set"));

            Bukkit.getScheduler().runTask(plugin, () -> {

                try (Stream<Path> walk = Files.walk(Paths.get(MailMe.getInstance().getDataFolder() + "/playerdata"))) {
                    List<String> result = walk.map(Path::toString)
                            .filter(f -> f.endsWith(".json")).collect(Collectors.toList());

                    result.forEach(dataFile -> {
                        dataFile = new File(dataFile).getName();
                        UUID uuid = UUID.fromString(dataFile.substring(0, dataFile.lastIndexOf('.')));
                        // Check if player's data is in our cache at the moment.
                        boolean exists = store.playerDataExists(uuid);

                        PlayerData playerData = store.getPlayerData(uuid);

                        // If player does not have a mailbox, give them the default one
                        if (playerData.getMailBox() == null) {
                            playerData.setMailBox(loc);
                        } else {
                            // If the player's old mailbox was a default mailbox, move it to the new location.
                            if (playerData.getMailBox().equals(oldLocation)) {
                                playerData.setMailBox(loc);
                            }
                        }
                        // If the data was not in our cache
                        if (!exists) {
                            playerData.update();
                            store.removePlayerData(uuid);
                        }

                    });

                } catch (IOException exception) {
                    MailMe.getInstance().debug("Failed to send as admin: " + exception.getMessage());
                }
            });

        });
    }

    @Permission(ADMIN_PERM)
    @SubCommand("output-debug")
    public void outputDebug(CommandSender sender) {
        StringBuilder sb = new StringBuilder()
                .append("[MailMe Debug Output]")
                .append("\nBukkit version: ")
                .append(Bukkit.getServer().getBukkitVersion())
                .append("\nPlugin Version: ")
                .append(plugin.getDescription().getVersion())
                .append("\nJava Version: ")
                .append(System.getProperty("java.runtime.version"))
                .append("\nLoaded Language files: ")
                .append(plugin.getLocale().getLoadedLanguages());

        sender.sendMessage(sb.toString());
    }

    @Permission(ADMIN_PERM)
    @Completion({"#admin-options"})
    @SubCommand("admin")
    public void adminCmd(CommandSender sender, String[] args) {
        if (args[1].contains("send")) {
            adminSendCmd(sender, args);
        } else if (args[1].contains("preset")) {
            presetCmd(sender, args);
        }
    }

    // Usage /mailme admin send <player/all/new> [preset-ID]
    public void adminSendCmd(CommandSender sender, String[] args) {
        if (args.length < 3) throw new SyntaxError(); // If missing <player/all/new>

        if (args.length == 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("No console! :(");
                return;
            }
            Player player = (Player) sender;
            MailBuilder builder = new MailBuilder();

            if (args[2].equalsIgnoreCase("all")) {
                builder.addRecipients(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getUniqueId).collect(Collectors.toList()));
            } else {
                builder.addRecipient(Bukkit.getOfflinePlayer(args[2]));
            }

            plugin.getGuiHandler().getChooseTypeGui().open(player, builder);
            return;
        }
        // if args length is greater than 2 (has id)
        if (!Utils.isValidPresetKey(plugin, args[3])) {
            sender.sendMessage(plugin.getLocale().getMessage("cmd.invalid-preset-key"));
            return;
        }
        ConfigurationSection presets = plugin.getConfig().getConfigurationSection("presets");

        String preset = presets.getString(args[3]);
        Type token = new TypeToken<Mail>() {}.getType();
        MailMe.getInstance();
        Mail mail = MailMe.GSON.fromJson(preset, token);

        List<UUID> recipients = new ArrayList<>();

        // Send to all players
        if (args[2].equalsIgnoreCase("all")) {

            try (Stream<Path> walk = Files.walk(Paths.get(MailMe.getInstance().getDataFolder() + "/playerdata"))) {
                List<String> result = walk.map(Path::toString)
                        .filter(f -> f.endsWith(".json")).collect(Collectors.toList());

                result.forEach(file -> recipients.add(MailMe.getInstance().getDataStoreHandler().getTmpFromFile(new File(file)).getUuid()));

            } catch (IOException exception) {
                MailMe.getInstance().debug("Failed to send as admin: " + exception.getMessage());
            }

        // Send to specific player
        } else {
            recipients.add(Bukkit.getOfflinePlayer(args[2]).getUniqueId());
        }

        mail.addRecipientsUUID(recipients);
        // Send as Server
        mail.sendAsServer();
    }

    // Usage /mailme admin preset <name>
    public void presetCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) throw new SyntaxError();

        Player player = (Player) sender;
        if (args.length < 3) throw new SyntaxError();

        MailBuilder builder = new MailBuilder();
        builder
                .setServerSender(true)
                .setPreset(true)
                .setAnonymous(false)
                .setPresetName(args[2])
                .addRecipient(player);


        plugin.getGuiHandler().getChooseTypeGui().open(player, builder);
    }



}
