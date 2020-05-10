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

package com.haroldstudios.mailme.ui;

import com.haroldstudios.mailme.MailMe;

import com.haroldstudios.mailme.mail.MailBuilder;
import com.haroldstudios.mailme.utility.Locale;
import com.haroldstudios.mailme.utility.Utils;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GuiHandler {

    private final MailMe plugin;
    private final ChooseTypeGui chooseTypeGui;
    private final List<ItemStack> icons = new ArrayList<>();

    public GuiHandler(MailMe plugin) {
        this.plugin = plugin;
        this.chooseTypeGui = new ChooseTypeGui(plugin);

        List<String> section = plugin.getConfig().getStringList("icons");
        section.forEach(icon -> icons.add(new ItemBuilder(new ItemStack(Material.valueOf(icon))).glow(true).build()));
    }

    public IconGui getIconGui(Player player, MailBuilder builder) {
        return new IconGui(plugin, player, icons, 0, builder);
    }

    public ChoosePlayerGui getChoosePlayerGui(Player player, MailBuilder builder) {
        return new ChoosePlayerGui(plugin, player, new ArrayList<>(Bukkit.getOnlinePlayers()), 0, builder);
    }

    public static ItemStack getPrevious(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.back-button"));
    }

    public static ItemStack getNext(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.forward-button"));
    }

    public static ItemStack getSearchIcon(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.search-button"));
    }

    public static ItemStack getNoResults(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.no-results"));
    }

    public static GuiItem getLoading(Locale locale, Locale.LANG lang) {
        return new GuiItem(Utils.getItemStack(locale.getConfigurationSection(lang, "gui.loading")), event -> event.setCancelled(true));
    }

    public static ItemStack getTextButton(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.text-button"));
    }

    public static ItemStack getContinue(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.continue"));
    }

    public static ItemStack getSend(Locale locale, Locale.LANG lang) {
        return Utils.getItemStack(locale.getConfigurationSection(lang, "gui.send"));
    }

    public static void playUISound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
    }

    public static void playDingSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F);
    }

    public static void playCloseSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1F, 1F);
    }


    public ChooseTypeGui getChooseTypeGui() {
        return chooseTypeGui;
    }

    public ItemInputGui getItemInputGui(Player player, MailBuilder builder) {
        return new ItemInputGui(plugin, player, builder);
    }
}
