package com.zford.jobs.listener;


import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.zford.jobs.Jobs;

public class JobsPlayerListener extends PlayerListener{
	// hook to the main plugin
	private Jobs plugin;
	
	public JobsPlayerListener(Jobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.addPlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.removePlayer(event.getPlayer());
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			// player has right clicked a block
			if(event.getClickedBlock().getTypeId() == 61 ||
					event.getClickedBlock().getTypeId() == 62){
				// furnace or burning furnace
			}
		}
	}
	
}
