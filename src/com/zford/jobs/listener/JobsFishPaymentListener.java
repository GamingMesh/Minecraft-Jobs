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

package com.zford.jobs.listener;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;

import com.zford.jobs.Jobs;

/**
 * Listener for fishing classes
 * 
 * @author Zak Ford <zak.j.ford@gmail.com>
 */

public class JobsFishPaymentListener extends PlayerListener {
    // hook to the main plugin
    private Jobs plugin;
    
    public JobsFishPaymentListener(Jobs plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if(!event.getAnimationType().equals(PlayerAnimationType.ARM_SWING)) return;
        
        // on player interact, if player is holding a fishing rod, set is fishing to true, otherwise set to false
        if(event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getType().equals(Material.FISHING_ROD)) {
            plugin.getPlayerJobInfo(event.getPlayer()).isFishing(true);
        } else {
            plugin.getPlayerJobInfo(event.getPlayer()).isFishing(false);
        }
    }
    
    @Override
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        // clear fishing if you change item in hand
        plugin.getPlayerJobInfo(event.getPlayer()).isFishing(false);
    }
    
    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if(event.isCancelled()) return;
        
        if(plugin.getPlayerJobInfo(event.getPlayer()).isFishing()) {
            plugin.getJob(event.getPlayer()).fished(event.getItem());
            // We got the item, clear fishing status
            plugin.getPlayerJobInfo(event.getPlayer()).isFishing(false);
        }
    }
    
    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(event.isCancelled()) return;
        
        // prevent drop item exploits
        plugin.getPlayerJobInfo(event.getPlayer()).isFishing(false);
    }
}
