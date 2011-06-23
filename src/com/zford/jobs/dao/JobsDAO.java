/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.zford.jobs.dao;

import java.util.List;


import org.bukkit.entity.Player;

import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.PlayerJobInfo;
import com.zford.jobs.dao.container.JobsDAOData;

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
