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

package me.zford.jobs.bukkit.config;

import java.util.ResourceBundle;

import me.zford.jobs.bukkit.JobsPlugin;

public class MessageConfig {
    private JobsPlugin plugin;
    private ResourceBundle bundle;
    
    public MessageConfig(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Reloads the config
     */
    public void reload() {
        bundle = ResourceBundle.getBundle("i18n/messages", plugin.getJobsConfiguration().getLocale());
    }
    
    /**
     * Get the message with the correct key
     * @param key - the key of the message
     * @return the message
     */
    public String getMessage(String key) {
        return bundle.getString(key);
    }
    
    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }
}
