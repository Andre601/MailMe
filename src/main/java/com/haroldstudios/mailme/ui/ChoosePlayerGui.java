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
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ChoosePlayerGui extends PaginationGui {

    public ChoosePlayerGui(MailMe plugin, Player player, List<?> items, int currentPage, MailBuilder builder) {
        super(plugin, player, items, currentPage, builder);

        applyNavBut(currentPage);
        getGui().setItem(2,2, GuiHandler.getLoading(getPlugin().getLocale(), getPlayerData().getLang()));
        updateTitlePath(getPlugin().getLocale().getMessage(getPlayerData().getLang(), "gui.choose-player-title"));

        getGui().setItem(3,9,new GuiItem(GuiHandler.getSearchIcon(getPlugin().getLocale(), getPlayerData().getLang()), event -> {
            event.setCancelled(true);
            getPlugin().getSearchPlayerFactory(getMailBuilder()).buildConversation(getPlayer()).begin();
            getGui().close(getPlayer());
            GuiHandler.playUISound(getPlayer());
        }));

        getGui().setItem(4,9, new GuiItem(removeFilter(), event -> {
            event.setCancelled(true);
            getPlugin().getGuiHandler().getChoosePlayerGui(getPlayer(), getMailBuilder()).open();
            GuiHandler.playUISound(getPlayer());
        }));

        getGui().setItem(5,9,new GuiItem(GuiHandler.getContinue(getPlugin().getLocale(), getPlayerData().getLang()), event -> {
            event.setCancelled(true);
            if (!getMailBuilder().getRecipients().isEmpty()) {
                // Opens next menu
                openInputGui(getMailBuilder());
                GuiHandler.playUISound(getPlayer());
            }
        }));
    }

    @Override
    public void open() {

        getGui().open(getPlayer());

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<GuiItem> heads = new ArrayList<>();
            int slot = 1;
            for (Object each : getPage(getCurrentPage())) {
                OfflinePlayer p = (OfflinePlayer) each;
                if (p.hasPlayedBefore() || p.isOnline()) {
                    ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD).setSkullOwner(p).setName(ChatColor.AQUA + p.getName());

                    if (getMailBuilder().getRecipients().contains(p)) {
                        getGui().setItem(slot, new GuiItem(new ItemBuilder(Material.EMERALD).glow(true).build(), e -> e.setCancelled(true)));
                    }
                    int finalSlot = slot;
                    heads.add(new GuiItem(builder.build(), event -> {
                        event.setCancelled(true);
                        if (getMailBuilder().getRecipients().contains(p)) {
                            getMailBuilder().removeRecipient(p);
                            getGui().setItem(finalSlot, new GuiItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).build(), e -> e.setCancelled(true)));
                        } else {
                            getMailBuilder().addRecipient(p);
                            getGui().setItem(finalSlot, new GuiItem(new ItemBuilder(Material.EMERALD).glow(true).build(), e -> e.setCancelled(true)));
                        }
                        GuiHandler.playDingSound(getPlayer());
                        getGui().update();


                    }));
                    slot++;
                } else
                    setNoResults();
            }

            if (getAllItems().size() == 0) {
                setNoResults();
                getGui().open(getPlayer());
                return;
            }

            addBetweenPoints(heads);
            Bukkit.getScheduler().runTask(getPlugin(), () -> getGui().open(getPlayer()));
        });
    }

    @Override
    public PaginationGui newInstance(int page) {
        return new ChoosePlayerGui(getPlugin(), getPlayer(), new ArrayList<>(Bukkit.getOnlinePlayers()), page, getMailBuilder());
    }

    public void openInputGui(MailBuilder builder) {
        try {
            // Asserted not null in previous menu
            switch (Objects.requireNonNull(builder.getMailType())) {

                case MAIL_MESSAGE:
                case MAIL_SOUND:
                    getPlayer().closeInventory();
                    getPlugin().getInputFactory(getMailBuilder()).buildConversation(getPlayer()).begin();
                    break;
                case MAIL_ITEM:
                    getPlugin().getGuiHandler().getItemInputGui(getPlayer(), builder).open();
                    break;
                case MAIL_LOCATION:
                    getPlayer().closeInventory();
                    builder.setLocation(getPlayer().getLocation());
                    Objects.requireNonNull(builder.build()).sendMail();
                    break;
            }
        } catch (IncompleteBuilderException ibe) { // Shouldn't EVER hit here
            ibe.printStackTrace();
        }
    }
}
