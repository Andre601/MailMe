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

package com.haroldstudios.mailme.components.hooks;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.mail.Mail;
import com.haroldstudios.mailme.utility.Locale;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Level;

public final class VaultHook {

    private static Economy econ = null;
    private final MailMe plugin;

    public VaultHook(final MailMe plugin) {
        this.plugin = plugin;
        if (!plugin.getConfig().getBoolean("enable-vault")) return;

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().log(Level.WARNING, "No Vault Economy Plugin found!");
            return;
        }
        econ = rsp.getProvider();
    }

    /**
     * Attempts to use economy for sending mail
     * @param player Player to attempt with
     * @param amount Amount to try and remove from balance
     * @return If transaction was success
     */
    public boolean attemptTransaction(Player player, double amount) {
        if (econ == null) return true;
        Locale.LANG lang = plugin.getDataStoreHandler().getPlayerData(player).getLang();
        EconomyResponse r = econ.withdrawPlayer(player, amount);
        if(!r.transactionSuccess()) {
            player.sendMessage(plugin.getLocale().getMessage(lang, "cmd.vault-insufficient-funds"));
            return false;
        } else {
            String msg = plugin.getLocale().getMessage(lang, "vault");
            msg = msg.replaceAll("@amount", String.valueOf(r.amount));
            player.sendMessage(msg);
        }
        return true;
    }

    public boolean attemptTransaction(Player player, Mail.MailType type) {
        if (econ == null) return true;
        Locale.LANG lang = plugin.getDataStoreHandler().getPlayerData(player).getLang();

        EconomyResponse r;

        switch (type) {
            case MAIL_LOCATION:
                r = econ.withdrawPlayer(player, MailMe.getInstance().getConfig().getDouble("cost.location"));
                break;
            case MAIL_SOUND:
                r = econ.withdrawPlayer(player, MailMe.getInstance().getConfig().getDouble("cost.sound"));
                break;
            case MAIL_ITEM:
                r = econ.withdrawPlayer(player, MailMe.getInstance().getConfig().getDouble("cost.item"));
                break;
            default:
                r = econ.withdrawPlayer(player, MailMe.getInstance().getConfig().getDouble("cost.message"));
                break;
        }

        if(!r.transactionSuccess()) {
            player.sendMessage(plugin.getLocale().getMessage(lang, "cmd.vault-insufficient-funds"));
            return false;
        } else {
            String msg = plugin.getLocale().getMessage(lang, "vault");
            msg = msg.replaceAll("@amount", String.valueOf(r.amount));
            player.sendMessage(msg);
        }
        return true;
    }
}
