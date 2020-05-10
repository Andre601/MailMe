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
import com.haroldstudios.mailme.components.IncompleteBuilderException;
import com.haroldstudios.mailme.datastore.PlayerData;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.mail.MailBuilder;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public final class InputPrompt extends StringPrompt {

    private final MailBuilder mail;

    public InputPrompt(MailBuilder mail) {
        this.mail = mail;
    }

    public String getPromptText(ConversationContext context) {
        PlayerData data = MailMe.getInstance().getDataStoreHandler().getPlayerData((Player) context.getForWhom());
        return MailMe.getInstance().getLocale().getMessage(data.getLang(), "mail.message");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String s) {

        if (s.equals("cancel") || mail.getMailType() == null) {
            return Prompt.END_OF_CONVERSATION;
        }

        if (mail.getMailType().equals(Mail.MailType.MAIL_SOUND)) {

            Sound sound;
            try {
                sound = Sound.valueOf(s.toUpperCase());
            } catch (IllegalArgumentException e) {
                return new InputPrompt(mail);
            }
            try {
                mail.setSound(sound).build();
                context.setSessionData("mail", mail);
            } catch (IncompleteBuilderException ignored) { }
            return Prompt.END_OF_CONVERSATION;
        }

        if (mail.getMailType().equals(Mail.MailType.MAIL_MESSAGE)) {
            mail.setMessage(s);
            context.setSessionData("mail", mail);
            return Prompt.END_OF_CONVERSATION;
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
