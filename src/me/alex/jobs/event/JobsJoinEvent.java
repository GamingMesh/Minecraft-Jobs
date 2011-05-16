package me.alex.jobs.event;

import me.alex.jobs.config.container.Job;

import org.bukkit.entity.Player;

/**
 * Event for joining a job
 * @author Alex
 *
 */
public class JobsJoinEvent extends JobsEvent{
	private Player player;
	private Job job;

	/**
	 * Constructor
	 * @param player - the player joining the job
	 * @param job - job they are joining
	 */
	public JobsJoinEvent(Player player, Job job){
		super(JobsEventType.Join);
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
	 * Get the job the player is joining
	 * @return the job the player is joining
	 */
	public Job getNewJob(){
		return job;
	}

}
