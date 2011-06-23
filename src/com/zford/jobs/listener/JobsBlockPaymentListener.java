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
