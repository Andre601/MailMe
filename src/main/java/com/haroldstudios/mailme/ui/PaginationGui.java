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
import com.haroldstudios.mailme.mail.MailBuilder;
import com.haroldstudios.mailme.utility.Locale;
import com.haroldstudios.mailme.utility.Pagination;
import com.haroldstudios.mailme.utility.Utils;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

abstract class PaginationGui {

    private final MailMe plugin;
    private final MailBuilder mailBuilder;
    private final List<Object> allItems = new ArrayList<>();
    private final PlayerData data;
    private final Player player;
    private final Pagination<?> pages;
    private final int rows = 5;
    private final Gui gui;
    private final int currentPage;

    PaginationGui(MailMe plugin, Player player, List<?> items, int currentPage, MailBuilder mailBuilder) {
        this.mailBuilder = mailBuilder;
        this.plugin = plugin;
        this.player = player;
        this.data = plugin.getDataStoreHandler().getPlayerData(player);
        this.pages = new Pagination<>(7, items);
        this.gui = new Gui(plugin, rows, "MailMe");
        this.allItems.addAll(items);
        this.currentPage = currentPage;

        gui.getFiller().fill(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), e -> e.setCancelled(true)));
        gui.setItem(5,5,new GuiItem(Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.exit-button")), e -> {
            e.setCancelled(true);
            gui.close(player);
            GuiHandler.playCloseSound(player);
        }));
    }

    protected void applyNavBut(int page) {
        if ((page - 1) >= 0)
            gui.setItem(2,1, new GuiItem(GuiHandler.getPrevious(plugin.getLocale(), data.getLang()), event -> {
                event.setCancelled(true);
                newInstance(page - 1).open();
            }));

        if ((page + 1) < pages.totalPages())
            gui.setItem(2,9,new GuiItem(GuiHandler.getNext(plugin.getLocale(), data.getLang()), event -> {
                event.setCancelled(true);
                newInstance(page + 1).open();

            }));
    }


    protected void setNoResults() {
        gui.setItem(2,5, new GuiItem(GuiHandler.getNoResults(getPlugin().getLocale(), data.getLang()), e -> e.setCancelled(true)));
    }

    protected MailBuilder getMailBuilder() {
        return mailBuilder;
    }

    protected List<?> getPage(int page) {
        return pages.getPage(page);
    }

    protected MailMe getPlugin() {
        return plugin;
    }

    protected PlayerData getPlayerData() {
        return data;
    }

    protected List<?> getAllItems() {
        return allItems;
    }

    protected Player getPlayer() {
        return player;
    }

    protected ItemStack removeFilter() {
        return Utils.getItemStack(plugin.getLocale().getConfigurationSection(data.getLang(), "gui.remove-filter"));
    }

    protected void addBetweenPoints(List<GuiItem> guiItems) {
        int marker = 0;

        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= 9; col++) {
                int slot = getSlotFromRowCol(row, col);
                if (!((row == 2) && (col >= 2 && col <= 8)))
                    continue;
                if (marker >= guiItems.size())
                    break;
                gui.setItem(slot, guiItems.get(marker));
                marker++;
            }
        }
    }

    protected ItemStack areYouSure(Locale.LANG lang) {
        return Utils.getItemStack(plugin.getLocale().getConfigurationSection(lang, "gui.are-you-sure"));
    }

    protected void updateTitlePath(String addition) {
        gui.updateTitle("MailMe // " + addition);
    }

    public Gui getGui() {
        return gui;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    private int getSlotFromRowCol(final int row, final int col) {
        return (col + (row - 1) * 9) - 1;
    }


    public abstract void open();
    public abstract PaginationGui newInstance(int page);
}
