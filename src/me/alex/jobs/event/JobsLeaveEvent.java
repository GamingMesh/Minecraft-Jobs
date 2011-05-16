package me.alex.jobs.event;

import me.alex.jobs.config.container.Job;

import org.bukkit.entity.Player;

public class JobsLeaveEvent extends JobsEvent{
	private Player player;
	private Job job;

	/**
	 * Constructor
	 * @param player - the player leaving the job
	 * @param job - job they are leaving
	 */
	public JobsLeaveEvent(Player player, Job job){
		super(JobsEventType.Leave);
		this.player = player;
		this.job = job;
	}
	
	/**
	 * Get the player involved in this event
	 * @return the player involved in this event
	 */
	public Player getPlayer(){
		return player;
	}
	
	/**
	 * Get the job the player is leaving
	 * @return the job the player is leaving
	 */
	public Job getOldJob(){
		return job;
	}

}
