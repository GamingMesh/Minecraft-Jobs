package com.zford.jobs.listener;

import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobsConfiguration;
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
	    
        // inside restricted area, no payment or experience
        if (RestrictedArea.isRestricted(event.getPlayer())) return;
        
		if(event.getResult() != null && (JobsConfiguration.getInstance().getPermissions() == null || 
		        !JobsConfiguration.getInstance().getPermissions().isEnabled() ||
		        JobsConfiguration.getInstance().getPermissions().getHandler().has(event.getPlayer(), "jobs.world." + event.getPlayer().getWorld().getName()))){
			plugin.getJobsPlayer(event.getPlayer().getName()).crafted(event.getResult());			
		}
	}
}
