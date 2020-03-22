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

package me.harry0198.mailme.mail.types;

import me.harry0198.mailme.mail.Mail;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unused"})
public final class MailLocation extends Mail {

    private final String type = "MailLocation"; // For deserializer
    private double x,y,z;
    private String world;

    /**
     *
     * @param icon Icon ItemStack
     * @param message Message mail
     * @param recipients List of OfflinePlayers
     */
    public MailLocation(ItemStack icon, List<OfflinePlayer> recipients, Location message) {
        this(icon, recipients, message, null);
    }

    /**
     *
     * @param icon Icon ItemStack
     * @param recipients List of OfflinePlayers
     * @param loc Message mail
     * @param sender Sender of Mail
     */
    public MailLocation(ItemStack icon,  List<OfflinePlayer> recipients, Location loc, UUID sender) {
        this(icon, new Date(), recipients, loc, sender);
    }

    /**
     *
     * @param icon Icon ItemStack
     * @param date Date to stamp onto mail
     * @param recipients List of OfflinePlayers
     * @param loc Message mail
     */
    public MailLocation(ItemStack icon, Date date, List<OfflinePlayer> recipients, Location loc) {
        this(icon, date, recipients, loc,null);
    }

    /**
     *
     * @param icon Icon ItemStack
     * @param date Date to stamp onto mail
     * @param recipients List of OfflinePlayers
     * @param loc Message mail
     * @param sender Sender of Mail
     */
    public MailLocation(ItemStack icon, Date date, List<OfflinePlayer> recipients, Location loc, UUID sender) {
        super(icon, date);
        super.setSender(sender);
        super.addRecipients(recipients);
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.world = loc.getWorld().getName();
    }
    
    @Override
    public MailType getMailType() {
        return MailType.MAIL_LOCATION;
    }

    @Override
    public void onClick(Player player) {
        player.spigot().sendMessage(getContentsAsText());
    }

    @Override
    public BaseComponent[] getContentsAsText() {
        return new ComponentBuilder("§bWorld: §f" + world + "\n" + "§bX: §f" + x + "\n" + "§bY: §f" + y + "\n" + "§bZ: §f" + z).create();
    }
}
