package me.alex.jobs.dao;

import me.alex.jobs.Job;

import org.bukkit.entity.Player;

public interface JobsDAO {
	
	public Job findPlayer(Player player);
	
	public void changeJob(Player player, String newJob);
	
	public void saveJob(Player player, Job job);
}
