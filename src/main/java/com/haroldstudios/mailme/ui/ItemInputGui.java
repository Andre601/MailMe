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
import com.haroldstudios.mailme.components.IncompleteBuilderException;
import com.haroldstudios.mailme.mail.MailBuilder;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class ItemInputGui {

    private final Gui gui;
    private final Player player;
    private final List<ItemStack> items = new ArrayList<>();
    private boolean gracefulClose = false;

    public ItemInputGui(MailMe plugin, Player player, MailBuilder builder) {
        this.gui = new Gui(plugin, 2, "MailMe // " + plugin.getLocale().getMessage(plugin.getDataStoreHandler().getPlayerData(player).getLang(), "gui.item-input-title"));
        this.player = player;
        gui.getFiller().fillBottom(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), event -> event.setCancelled(true)));
        gui.setItem(2,9, new GuiItem(GuiHandler.getContinue(plugin.getLocale(), plugin.getDataStoreHandler().getPlayerData(player).getLang()), event -> {
            event.setCancelled(true);
            try {

                for (int i = 0; i < 9; i++) {
                    if (event.getInventory().getItem(i) == null)
                        continue;
                    org.bukkit.inventory.ItemStack stack = new ItemStack(event.getInventory().getItem(i)); // Convert from NMS ItemStack to Bukkit ItemStack
                    builder.addItem(stack);
                }
                GuiHandler.playDingSound(player);
                if (builder.getItems().size() < 1) return;
                builder.build().sendMail();
                gracefulClose = true;

            } catch (IncompleteBuilderException ibe) {
                ibe.printStackTrace();
            }

            gui.close(player);
        }));

        gui.setCloseGuiAction(event -> {

            if (gracefulClose) return;
            for (int i = 0; i < 9; i++) {
                ItemStack item = event.getInventory().getItem(i);
                if (item == null)
                    continue;
                items.add(item);
            }

            ItemStack[] stockArr = new ItemStack[items.size()];
            stockArr = items.toArray(stockArr);
            event.getPlayer().getInventory().addItem(stockArr);
        });
    }

    public void open() {
        gui.open(player);
    }

}
