package me.alex.jobs.listener;

import me.alex.jobs.Jobs;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

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
	
}
