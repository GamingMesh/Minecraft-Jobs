package me.alex.jobs.listener;

import me.alex.jobs.Jobs;
import me.alex.jobs.config.JobsConfiguration;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import de.diddiz.LogBlock.Consumer;

public class JobsBlockPaymentListener extends BlockListener{
	private Jobs plugin;
	
	public JobsBlockPaymentListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		// make sure event is not cancelled
		if(!event.isCancelled() && 
				((JobsConfiguration.getInstance().getPermissions() == null || !JobsConfiguration.getInstance().getPermissions().isEnabled())
						|| JobsConfiguration.getInstance().getPermissions().getHandler().has(event.getPlayer(), "jobs.world." + event.getPlayer().getWorld().getName()))){
			plugin.getJob(event.getPlayer()).broke(event.getBlock());			
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event){
		// make sure event is not cancelled
		if(event.canBuild() && !event.isCancelled() && 
				((JobsConfiguration.getInstance().getPermissions() == null || !JobsConfiguration.getInstance().getPermissions().isEnabled())
						|| JobsConfiguration.getInstance().getPermissions().getHandler().has(event.getPlayer(), "jobs.world." + event.getPlayer().getWorld().getName()))){
			plugin.getJob(event.getPlayer()).placed(event.getBlock());
		}
	}
}
