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

package com.haroldstudios.mailme.utility;

import com.google.gson.reflect.TypeToken;
import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.datastore.PlayerData;
import com.haroldstudios.mailme.mail.Mail;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class Utils {

    public static String colour(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colourList(List<String> string) {
        return string.stream().map(Utils::colour).collect(Collectors.toList());
    }

    public static PlayerData readJson(File file) {
        try {

            Type token = new TypeToken<PlayerData>() {}.getType();
            FileReader fr = new FileReader(file);
            PlayerData data = MailMe.GSON.fromJson(fr, token);
            fr.close();

            return data;
        } catch (IOException io) {
            io.printStackTrace();
        }
        return null;
    }


    public static void writeJson(File file, PlayerData data) {
        try {
            FileWriter writer = new FileWriter(file);
            MailMe.GSON.toJson(data, writer);
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static ItemStack getItemStack(ConfigurationSection section) {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(section.getString("material")))
                .setName(colour(section.getString("title")))
                .setLore(colourList(section.getStringList("lore")).toArray(new String[]{}))
                .setAmount(section.getInt("amount"))
                .glow(section.getBoolean("glow"));

        if (Material.valueOf(section.getString("material")).equals(Material.PLAYER_HEAD))
            builder.setSkullTexture(section.getString("skull-texture"));

        return builder.build();
    }

    public static String applyPlaceHolders(Mail mail, String string, UUID player) {

        Locale.LANG lang = MailMe.getInstance().getDataStoreHandler().getPlayerData(player).getLang();
        Locale locale = MailMe.getInstance().getLocale();

        string = string.replaceAll("@time", mail.getDate().toString());

        string = string.replaceAll("@sender", mail.isAnonymous() ? locale.getMessage(lang, "mail.anonymous") : mail.isServer() ? locale.getMessage(lang, "mail.server") : mail.getSender() == null ? locale.getMessage(lang, "mail.unknown") : Bukkit.getOfflinePlayer(mail.getSender()).getName());
        string = string.replaceAll("@type", mail.getMailType().toString());
        string = string.replaceAll("@read", String.valueOf(mail.isRead()));
        return string;
    }

    public static List<String> applyPlaceHolders(Mail mail, List<String> string, UUID player) {
        return string.stream().map(str -> applyPlaceHolders(mail, str, player)).collect(Collectors.toList());
    }

    public static void playNoteEffect(Player player, Location location) {
        double note = 6 / 24D; // 6 is the value of the red note
        player.spawnParticle(Particle.NOTE, location, 0, note, 0, 0, 1);
    }

    public static boolean isValidPresetKey(final MailMe plugin, final String preset) {
        ConfigurationSection presets = plugin.getConfig().getConfigurationSection("presets");
        return (presets.getKeys(false).contains(preset));
    }
}
