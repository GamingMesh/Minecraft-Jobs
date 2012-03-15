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

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class JobsBlockPaymentListener implements Listener {
    private Jobs plugin;
    
    public JobsBlockPaymentListener(Jobs plugin){
        this.plugin = plugin;
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        
        Player player = event.getPlayer();
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        // restricted area multiplier
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(event.getPlayer());
        
        if(plugin.hasWorldPermission(player, player.getWorld())) {
            plugin.getJobsManager().getJobsPlayer(player.getName()).broke(event.getBlock(), multiplier);            
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        
        // check to make sure you can build
        if(!event.canBuild()) return;
        
        Player player = event.getPlayer();
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        // restricted area multiplier
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(event.getPlayer());
        
        if(plugin.hasWorldPermission(player, player.getWorld())) {
            plugin.getJobsManager().getJobsPlayer(player.getName()).placed(event.getBlock(), multiplier);
        }
    }
}
