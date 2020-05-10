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

package com.haroldstudios.mailme.conversations;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.datastore.PlayerData;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.ui.GuiHandler;
import com.haroldstudios.mailme.ui.MailGui;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SearchInput extends StringPrompt {
    @Override
    public String getPromptText(ConversationContext context) {
        PlayerData data = MailMe.getInstance().getDataStoreHandler().getPlayerData((Player) context.getForWhom());
        return MailMe.getInstance().getLocale().getMessage(data.getLang(), "gui.search");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String s) {
        if (s.equals("cancel")) {
            return Prompt.END_OF_CONVERSATION;
        }

        PlayerData data = MailMe.getInstance().getDataStoreHandler().getPlayerData((Player) context.getForWhom());

        Player player = (Player) context.getForWhom();
        OfflinePlayer search = Bukkit.getOfflinePlayer(s);

        if (search == null) {
            Gui gui = new MailGui(MailMe.getInstance(), player, Collections.emptyList(), 0).getGui();
            gui.setItem(2, 5, new GuiItem(GuiHandler.getSearchIcon(MailMe.getInstance().getLocale(), data.getLang())));
            gui.open(player);
            return Prompt.END_OF_CONVERSATION;
        }

        List<Mail> mailList = data.getMail().stream().filter(mail -> mail.getSender().equals(search.getUniqueId())).filter(mail -> !mail.isAnonymous()).collect(Collectors.toList());

        new MailGui(MailMe.getInstance(), player, mailList, 0).open();

        return Prompt.END_OF_CONVERSATION;
    }
}
