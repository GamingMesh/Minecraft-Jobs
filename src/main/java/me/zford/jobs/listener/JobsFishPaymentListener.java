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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;


/**
 * Listener for fishing classes
 * 
 * @author Zak Ford <zak.j.ford@gmail.com>
 */

public class JobsFishPaymentListener implements Listener {
    // hook to the main plugin
    private Jobs plugin;
    
    public JobsFishPaymentListener(Jobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        
        Player player = event.getPlayer();
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        if (!plugin.hasWorldPermission(player, player.getWorld())) return;
        
        // restricted area multiplier
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(player);
        
        if(event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) && event.getCaught() instanceof Item) {
            plugin.getJobsPlayer(player.getName()).fished((Item)event.getCaught(), multiplier);
        }
    }
}
