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


import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.container.RestrictedArea;

public class JobsBlockPaymentListener extends BlockListener{
	private Jobs plugin;
	
	public JobsBlockPaymentListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
        if (RestrictedArea.isRestricted(event.getPlayer())) {
            // inside restricted area, no payment or experience
            return;
        }
		// make sure event is not cancelled
		if(!event.isCancelled() && 
				((JobsConfiguration.getInstance().getPermissions() == null || !JobsConfiguration.getInstance().getPermissions().isEnabled())
						|| JobsConfiguration.getInstance().getPermissions().getHandler().has(event.getPlayer(), "jobs.world." + event.getPlayer().getWorld().getName()))){
			plugin.getJob(event.getPlayer()).broke(event.getBlock());			
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event){
        if (RestrictedArea.isRestricted(event.getPlayer())) {
            // inside restricted area, no payment or experience
            return;
        }
		// make sure event is not cancelled
		if(event.canBuild() && !event.isCancelled() && 
				((JobsConfiguration.getInstance().getPermissions() == null || !JobsConfiguration.getInstance().getPermissions().isEnabled())
						|| JobsConfiguration.getInstance().getPermissions().getHandler().has(event.getPlayer(), "jobs.world." + event.getPlayer().getWorld().getName()))){
			plugin.getJob(event.getPlayer()).placed(event.getBlock());
		}
	}
}
