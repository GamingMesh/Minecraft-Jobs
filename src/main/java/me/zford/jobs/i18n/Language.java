/**
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011 Zak Ford <zak.j.ford@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.zford.jobs.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

public class Language {
    private static Properties bundle;

    static {
        bundle = new Properties();
        try {
            bundle.load(new InputStreamReader(Language.class.getResourceAsStream("/i18n/messages.properties"), "UTF-8"));
        } catch (IOException e) {
        }
    }
    
    private Language() { }
    
    /**
     * Reloads the config
     */
    public static void reload(Locale locale) {
        InputStream stream = null;
        try {
            stream = Language.class.getResourceAsStream("/i18n/messages_" + locale.toLanguageTag() + ".properties");
        } catch (Exception e) { }
        if (stream == null) {
            try {
                stream = Language.class.getResourceAsStream("/i18n/messages_" + locale.getLanguage() + ".properties");
            } catch (Exception e) { }
        }
        if (stream != null) {
            try {
                bundle.load(new InputStreamReader(stream, "UTF-8"));
            } catch (Exception e) { }
        }
    }
    
    /**
     * Get the message with the correct key
     * @param key - the key of the message
     * @return the message
     */
    public static String getMessage(String key) {
        return bundle.get(key).toString();
    }
    
    public static boolean containsKey(String key) {
        return bundle.containsKey(key);
    }
}
