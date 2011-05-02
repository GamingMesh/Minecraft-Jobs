package me.alex.jobs;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class JobsPlayerListener extends PlayerListener{
	public Jobs plugin;
	
	public JobsPlayerListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event){
		plugin.addPlayer(event.getPlayer());
	}
	
	public void onPlayerQuit(PlayerQuitEvent event){
		plugin.removePlayer(event.getPlayer());
	}
}
