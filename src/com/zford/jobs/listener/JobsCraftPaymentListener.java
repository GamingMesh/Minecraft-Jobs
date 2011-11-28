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

import org.bukkit.entity.Player;
import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.RestrictedArea;

public class JobsCraftPaymentListener extends InventoryListener{
	
	private Jobs plugin;
	
	public JobsCraftPaymentListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onInventoryCraft(InventoryCraftEvent event){
		
		// make sure plugin is enabled
	    if(!plugin.isEnabled()) return;
        // make sure event is not canceled
        if(event.isCancelled()) return;
	    
        // restricted area multiplier
        double multiplier = RestrictedArea.getMultiplier(event.getPlayer());
        
        if(event.getResult() == null)
            return;
        
        Player player = event.getPlayer();
        
        if(plugin.hasPermission(player, "jobs.world." + player.getWorld().getName())) {
			plugin.getJobsPlayer(player.getName()).crafted(event.getResult(), multiplier);			
		}
	}
}
