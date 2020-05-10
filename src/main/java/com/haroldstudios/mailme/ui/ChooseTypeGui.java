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
import com.haroldstudios.mailme.datastore.PlayerData;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utility.Utils;
import com.haroldstudios.mailme.mail.MailBuilder;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ChooseTypeGui {

    private final MailMe plugin;
    private final Gui ui;

    public ChooseTypeGui(MailMe plugin) {
        this.plugin = plugin;
        this.ui = new Gui(plugin, 3, ChatColor.YELLOW + "MailMe");
        ui.getFiller().fill(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), e -> e.setCancelled(true)));
    }

    public void open(Player player, MailBuilder builder) {

        Gui gui = ui;

        PlayerData data = plugin.getDataStoreHandler().getPlayerData(player);
        builder.setSender(player.getUniqueId());

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        // Sets builder types dependent on gui choice & opens next menu
        gui.setItem(2, 3, new GuiItem(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.choose.message")), event -> {
            plugin.getGuiHandler().getIconGui(player, builder.setMailType(Mail.MailType.MAIL_MESSAGE)).open();
            GuiHandler.playUISound(player);
        }));
        gui.setItem(2, 4, new GuiItem(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.choose.item")), event -> {
            plugin.getGuiHandler().getIconGui(player, builder.setMailType(Mail.MailType.MAIL_ITEM)).open();
            GuiHandler.playUISound(player);
        }));
        gui.setItem(2, 6, new GuiItem(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.choose.sound")), event -> {
            plugin.getGuiHandler().getIconGui(player, builder.setMailType(Mail.MailType.MAIL_SOUND)).open();
            GuiHandler.playUISound(player);
        }));
        gui.setItem(2, 7, new GuiItem(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.choose.location")), event -> {
            plugin.getGuiHandler().getIconGui(player, builder.setMailType(Mail.MailType.MAIL_LOCATION)).open();
            GuiHandler.playUISound(player);
        }));
        gui.addSlotAction(3,5, event -> {
            GuiHandler.playUISound(player);

            builder.setAnonymous(!builder.isAnonymous());
            gui.updateItem(3,5, new GuiItem(new ItemBuilder(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.choose.send-anonymously"))).glow(builder.isAnonymous()).build()));
        });
        if (player.hasPermission( "mailme.base.send-anonymously")) {
            gui.setItem(3, 5, new GuiItem(new ItemBuilder(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.choose.send-anonymously"))).glow(builder.isAnonymous()).build()));
        }
        gui.open(player);

    }

}
