package me.alex.jobs.listener;

import me.alex.jobs.Jobs;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class JobsBlockPaymentListener extends BlockListener{
	private Jobs plugin;
	
	public JobsBlockPaymentListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		// make sure event is not cancelled
		if(!event.isCancelled()){
			plugin.getJob(event.getPlayer()).broke(event.getBlock());			
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event){
		// make sure event is not cancelled
		if(event.canBuild() && !event.isCancelled()){
			plugin.getJob(event.getPlayer()).placed(event.getBlock());
		}
	}
}
