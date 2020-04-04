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

package com.haroldstudios.mailme.mail.types;

import com.haroldstudios.mailme.mail.Mail;
import net.md_5.bungee.api.chat.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused"})
public final class MailSound extends Mail {

    private Sound sound;

    public MailSound(ItemStack icon, List<OfflinePlayer> recipients, Sound sound) {
        this(icon, recipients, sound, "", null);
    }

    public MailSound(final ItemStack icon, final List<OfflinePlayer> recipients, final Sound sound, final String msg, final UUID sender) {
            super(icon, new Date());
            super.addRecipients(recipients);
            this.sound = sound;
            super.setSender(sender);
    }


    @Override
    public MailType getMailType() {
        return MailType.MAIL_SOUND;
    }

    @Override
    public void onClick(Player player) {
        player.playSound(player.getLocation(), sound, 1F, 1F);
    }

    @Override
    public BaseComponent[] getContentsAsText() {
        ComponentBuilder builder = new ComponentBuilder("");
        TextComponent txt = new TextComponent(sound.toString() + "\n");
        builder.append(txt);
        return builder.create();
    }
}
