package me.alex.jobs.dao;

import java.util.List;

import me.alex.jobs.config.container.Job;
import me.alex.jobs.config.container.PlayerJobInfo;
import me.alex.jobs.dao.container.JobsDAOData;

import org.bukkit.entity.Player;

/**
 * Data Access Object interface for the Jobs plugin
 * 
 * Interface that holds all methods that a DAO needs to have
 * @author Alex
 *
 */
public interface JobsDAO {
	
	/**
	 * Get all jobs the player is part of.
	 * @param player - the player being searched for
	 * @return list of all of the names of the jobs the players are part of.
	 */
	public List<JobsDAOData> getAllJobs(Player player);
	
	/**
	 * Join a job (create player-job entry from storage)
	 * @param player - player that wishes to join the job
	 * @param job - job that the player wishes to join
	 */
	public void joinJob(Player player, Job job);

	/**
	 * Quit a job (delete player-job entry from storage)
	 * @param player - player that wishes to quit the job
	 * @param job - job that the player wishes to quit
	 */
	public void quitJob(Player player, Job job);
	
	/**
	 * Save player-job information
	 * @param jobInfo - the information getting saved
	 */
	public void save(PlayerJobInfo jobInfo);
	
	/**
	 * Get the number of players that have a particular job
	 * @param job - the job
	 * @return  the number of players that have a particular job
	 */
	public Integer getSlotsTaken(Job job);
	
}
