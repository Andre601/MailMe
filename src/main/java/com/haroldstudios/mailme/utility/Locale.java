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

package com.haroldstudios.mailme.utility;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Locale {

    private final Map<LANG, YamlConfiguration> yaml = new HashMap<>();
    private final LANG serverLang;

    public Locale() {
        serverLang = LANG.valueOf(MailMe.getInstance().getConfig().getString("lang"));
        for (LANG lang : LANG.values()) {
            if (!new File(MailMe.getInstance().getDataFolder() + "/languages", lang.toString() + ".yml").exists()) {
                MailMe.getInstance().saveResource("languages/" + lang.toString() + ".yml", false);
            }

            File langFile = new File(MailMe.getInstance().getDataFolder() + "/languages/" + lang.toString() + ".yml");
            YamlConfiguration externalYamlConfig = YamlConfiguration.loadConfiguration(langFile);

            InputStreamReader defConfigStream = new InputStreamReader(MailMe.getInstance().getResource(lang.toString() + ".yml"), StandardCharsets.UTF_8);
            YamlConfiguration internalLangConfig = YamlConfiguration.loadConfiguration(defConfigStream);

            for (String string : internalLangConfig.getKeys(true)) {
                if (!externalYamlConfig.contains(string)) {
                    externalYamlConfig.set(string, internalLangConfig.get(string));
                }
            }
            try {
               externalYamlConfig.save(langFile);
            } catch (IOException io) {
                io.printStackTrace();
            }

            this.yaml.put(lang, externalYamlConfig);
        }
    }

    public Set<LANG> getLoadedLanguages() {
        return yaml.keySet();
    }

    public String getMessage(String string) {
        return getMessage(serverLang, string);
    }

    public String getMessage(LANG lang, String string) {
        String msg = this.yaml.get(lang).getString(string);
        if (msg == null) {
            return "Unknown Message! Needs configurating in MailMe's config.yml!";
        }
        return Utils.colour(msg.replace("@prefix", this.yaml.get(lang).getString("prefix")));
    }

    public List<String> getMessages(String string) {
        return getMessages(serverLang, string);
    }

    public List<String> getMessages(LANG lang, String string) {
        List<String> msg = this.yaml.get(lang).getStringList(string);
        msg.forEach(e -> e.replace("@prefix", getMessage(lang,"prefix")));
        return Utils.colourList(msg);
    }

    public String[] getLore(LANG lang, String string) {
        List<String> msg = Utils.colourList(this.yaml.get(lang).getStringList(string));
        return msg.toArray(new String[0]);
    }

    public ConfigurationSection getConfigurationSection(LANG lang, String path) {
        return this.yaml.get(lang).getConfigurationSection(path);
    }

    public enum LANG {
        EN, FR, PT, DE
    }
}
