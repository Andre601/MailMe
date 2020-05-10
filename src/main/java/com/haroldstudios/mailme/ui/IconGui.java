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
import com.haroldstudios.mailme.utility.Utils;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class IconGui extends PaginationGui {

    private final MailMe plugin;

    public IconGui(MailMe plugin, Player player, List<?> items, int page, MailBuilder builder) {
        super(plugin, player, items, page, builder);
        this.plugin = plugin;

        updateTitlePath(plugin.getLocale().getMessage(getPlayerData().getLang(), "gui.choose-icon-title"));
        applyNavBut(page);
    }

    @Override
    public void open() {
        ItemStack stack = Utils.getItemStack(plugin.getLocale().getConfigurationSection(getPlayerData().getLang(), "gui.icon-custom"));
        GuiItem gi = new GuiItem(stack, event -> {
            event.setCancelled(true);
            if (event.getCursor().getType().equals(Material.AIR)) return;
            getMailBuilder().setIcon(new org.bukkit.inventory.ItemStack(event.getCursor()));
            if (getMailBuilder().getRecipients().size() > 0) {
                plugin.getGuiHandler().getChoosePlayerGui(getPlayer(), getMailBuilder()).openInputGui(getMailBuilder());
                return;
            }
            plugin.getGuiHandler().getChoosePlayerGui(getPlayer(), getMailBuilder()).open();
            GuiHandler.playDingSound(getPlayer());
        });
        List<GuiItem> iconList = new ArrayList<>();

        getPage(getCurrentPage()).forEach(it -> {
            ItemStack item = (ItemStack) it;
            iconList.add(new GuiItem(item, event -> {
                event.setCancelled(true);
                getMailBuilder().setIcon(item);
                if (getMailBuilder().getRecipients().size() > 0) {
                    plugin.getGuiHandler().getChoosePlayerGui(getPlayer(), getMailBuilder()).openInputGui(getMailBuilder());
                    return;
                }
                plugin.getGuiHandler().getChoosePlayerGui(getPlayer(), getMailBuilder()).open();
                GuiHandler.playDingSound(getPlayer());
            }));
        });

        getGui().setItem(3,5,gi);
        addBetweenPoints(iconList);

        getGui().open(getPlayer());
    }

    @Override
    public PaginationGui newInstance(int page) {
        return new IconGui(getPlugin(), getPlayer(), getAllItems(), page, getMailBuilder());
    }
}
