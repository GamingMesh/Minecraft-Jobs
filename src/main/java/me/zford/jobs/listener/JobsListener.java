/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
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
 * 
 */

package me.zford.jobs.listener;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.JobsPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JobsListener implements Listener {
    // hook to the main plugin
    private Jobs plugin;
    
    public JobsListener(Jobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        plugin.getJobsManager().addPlayer(event.getPlayer().getName());
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        plugin.getJobsManager().removePlayer(event.getPlayer().getName());
    }
    
    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!plugin.isEnabled()) return;
        
        Player player = event.getPlayer();
        JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(player.getName());
        
        if (jPlayer == null) return;
        
        String format = event.getFormat();
        String honorific = jPlayer.getDisplayHonorific();
        if (!honorific.isEmpty()) {
            format = format.replace("%1$s", honorific+ " %1$s");
            event.setFormat(format);
        }
    }
}
