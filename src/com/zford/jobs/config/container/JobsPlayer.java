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

package com.zford.jobs.config.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobConfig;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.dao.JobsDAO;
import com.zford.jobs.dao.container.JobsDAOData;
import com.zford.jobs.event.JobsLevelUpEvent;
import com.zford.jobs.event.JobsSkillUpEvent;
import com.zford.jobs.util.DisplayMethod;

public class JobsPlayer {
    // jobs plugin
    private Jobs plugin;
	// the player the object belongs to
	private String playername;
	// list of all jobs that the player does
	private List<Job> jobs;
	// progression of the player in each job
	private HashMap<Job, JobProgression> progression;
	// display honorific
	private String honorific = null;
		
	/**
	 * Constructor.
	 * Reads data storage and configures itself.
	 * @param plugin - the jobs plugin
	 * @param playername - the player this represents
	 * @param dao - the data access object
	 */
	public JobsPlayer(Jobs plugin, String playername, JobsDAO dao) {
	    this.plugin = plugin;
		// set player link
		this.playername = playername;
		this.jobs = new ArrayList<Job>();
		this.progression = new HashMap<Job, JobProgression>();
		// for all jobs players have
		List<JobsDAOData> list = dao.getAllJobs(this);
		if(list != null){
			for(JobsDAOData job: list){
				if(JobConfig.getInstance().getJob(job.getJobName()) != null){
					// add the job
					jobs.add(JobConfig.getInstance().getJob(job.getJobName()));
					
					// create the progression object
					JobProgression jobProgression = 
						new JobProgression(JobConfig.getInstance().getJob(job.getJobName()), job.getExperience(),job.getLevel(), this);
					// calculate the max level
					
					// add the progression level.
					progression.put(jobProgression.getJob(), jobProgression);
				}
			}
		}
		reloadMaxExperience();
		reloadHonorific();
	}
	
	/**
	 * Broke a block.
	 * 
	 * Give correct experience and income
	 * 
	 * @param block - the block broken
     * @param multiplier - the payment/xp multiplier
	 */
	public void broke(Block block, double multiplier) {
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> entry: progression.entrySet()) {
			// add the current level to the parameter list
			param.put("joblevel", (double)entry.getValue().getLevel());
			// get the income and give it
			Double income = entry.getKey().getBreakIncome(block, param);
			if(income != null) {
                Double exp = entry.getKey().getBreakExp(block, param);
                // give income
				JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = JobConfig.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getBreakIncome(block, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
	}
	
	/**
	 * Placed a block.
	 * 
	 * Give correct experience and income
	 * 
	 * @param block - the block placed
     * @param multiplier - the payment/xp multiplier
	 */
	public void placed(Block block, double multiplier) {
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> entry: progression.entrySet()) {
			// add the current level to the parameter list
			param.put("joblevel", (double)entry.getValue().getLevel());
			// get the income and give it
			Double income = entry.getKey().getPlaceIncome(block, param);
			if(income != null) {
                Double exp = entry.getKey().getPlaceExp(block, param);
				// give income
				JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = JobConfig.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getPlaceIncome(block, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
	}
	
	/**
	 * Killed a living entity or owned wolf killed living entity.
	 * 
	 * Give correct experience and income
	 * 
	 * @param victim - the mob killed
	 * @param multiplier - the payment/xp multiplier
	 */
	public void killed(String victim, double multiplier) {
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> entry: progression.entrySet()) {
			// add the current level to the parameter list
			param.put("joblevel", (double)entry.getValue().getLevel());
			// get the income and give it
			Double income = entry.getKey().getKillIncome(victim, param);
			if(income != null) {
                Double exp = entry.getKey().getKillExp(victim, param);
				// give income
				JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();	
			}
			param.remove("joblevel");
		}
		// no job
		if(this.progression.size() == 0) {
		    Job jobNone = JobConfig.getInstance().getJob("None");
		    if(jobNone != null) {
    		    param.put("joblevel", 1.0);
    		    Double income = jobNone.getKillIncome(victim, param);
    		    if(income != null) {
    		        // give income
    		        JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
    		    }
    		    param.remove("joblevel");
		    }
		}
	}
	
	/**
	 * Fished an item
	 * 
	 * Give correct experience and income
	 * 
	 * @param item - the item fished
     * @param multiplier - the payment/xp multiplier
	 */
	public void fished(Item item, double multiplier) {
	    HashMap<String, Double> param = new HashMap<String, Double>();
	    param.put("numjobs", (double)progression.size());
	    for(Entry<Job, JobProgression> entry: progression.entrySet()) {
            // add the current level to the parameter list
            param.put("joblevel", (double)entry.getValue().getLevel());
            // get the income and give it
            Double income = entry.getKey().getFishIncome(item, param);
            if(income != null) {
                Double exp = entry.getKey().getFishExp(item, param);
                // give income
                JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();
            }
            param.remove("joblevel");
        }
	    // no job
        if(this.progression.size() == 0) {
            Job jobNone = JobConfig.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getFishIncome(item, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
	}
	
	/**
	 * crafted an item.
	 * 
	 * Give correct experience and income
	 * 
	 * @param items - the items crafted
     * @param multipler - the payment/xp multiplier
	 */
	public void crafted(ItemStack items, double multiplier) {
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> entry: progression.entrySet()) {
			// add the current level to the parameter list
			param.put("joblevel", (double)entry.getValue().getLevel());
			// get the income and give it
			Double income = entry.getKey().getCraftIncome(items, param);
			if(income != null) {
                Double exp = entry.getKey().getCraftExp(items, param);
                // give income
				JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = JobConfig.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getCraftIncome(items, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getBufferedPayment().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
	}
	
	/**
	 * Get the list of jobs
	 * @return the list of jobs
	 */
	public List<Job> getJobs(){
		return jobs;
	}
	
	/**
	 * Get the list of job progressions
	 * @return the list of job progressions
	 */
	public Collection<JobProgression> getJobsProgression(){
		return progression.values();
	}
	
	/**
	 * Get the job progression with the certain job
	 * @return the job progression
	 */
	public JobProgression getJobsProgression(Job job){
		return progression.get(job);
	}
	
	/**
	 * get the player
	 * @return the player
	 */
	public String getName(){
		return playername;
	}
	
	/**
	 * Function called to update the levels and make sure experience < maxExperience
	 */
	public void checkLevels(){
		for(JobProgression temp: progression.values()){
			if(temp.canLevelUp()){
				// user would level up, call the joblevelupevent
				JobsLevelUpEvent event = new JobsLevelUpEvent(this, temp);
                plugin.getServer().getPluginManager().callEvent(event);
			}
			
			if(JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()) != null && !JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()).equals(temp.getTitle())){
				// user would skill up
				JobsSkillUpEvent event = new JobsSkillUpEvent(this, temp, JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()));
				plugin.getServer().getPluginManager().callEvent(event);
			}
		}
	}
	
	public String getDisplayHonorific(){
		
		String honorific = "";		
		
		if(jobs.size() > 1){
			// has more than 1 job - using shortname mode
			for(JobProgression temp: progression.values()){
				if(temp.getJob().getDisplayMethod().equals(DisplayMethod.FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.TITLE) ||
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_TITLE)){
					// add title to honorific
					if(temp.getTitle() != null){
						honorific += temp.getTitle().getChatColor() + temp.getTitle().getShortName() + ChatColor.WHITE;
					}
				}
				
				if(temp.getJob().getDisplayMethod().equals(DisplayMethod.FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.JOB) ||
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_JOB)){
					honorific += temp.getJob().getChatColour() + temp.getJob().getShortName() + ChatColor.WHITE;
				}
				
				if(!temp.getJob().getDisplayMethod().equals(DisplayMethod.NONE)){
					honorific+=" ";
				}
			}
		}
		else{
		    Job job;
		    if(jobs.size() == 0) {
		        job = JobConfig.getInstance().getJob("None");
		    } else {
		        job = jobs.get(0);
		    }
		    
            if(job == null) {
                return null;
            }
            
		    JobProgression jobProgression = progression.get(job);
		    
			// using longname mode
			if(job.getDisplayMethod().equals(DisplayMethod.FULL) || job.getDisplayMethod().equals(DisplayMethod.TITLE)){
				// add title to honorific
				if(jobProgression != null && jobProgression.getTitle() != null){
					honorific += jobProgression.getTitle().getChatColor() + jobProgression.getTitle().getName() + ChatColor.WHITE;
				}
				if(job.getDisplayMethod().equals(DisplayMethod.FULL)){
					honorific += " ";
				}
			}
			if(job.getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || job.getDisplayMethod().equals(DisplayMethod.SHORT_TITLE)){
				// add title to honorific
				if(jobProgression != null && jobProgression.getTitle() != null){
					honorific += jobProgression.getTitle().getChatColor() + jobProgression.getTitle().getShortName() + ChatColor.WHITE;
				}
			}
			
			if(job.getDisplayMethod().equals(DisplayMethod.FULL) || job.getDisplayMethod().equals(DisplayMethod.JOB)){
				honorific += job.getChatColour() + job.getName() + ChatColor.WHITE;
			}
			if(job.getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || job.getDisplayMethod().equals(DisplayMethod.SHORT_JOB)){
				honorific += job.getChatColour() + job.getShortName() + ChatColor.WHITE;
			}
		}
		
		if(honorific.equals("")){
			return null;
		}else{
			return honorific.trim();
		}
	}
	
	/**
	 * Player joins a job
	 * @param job - the job joined
	 */
	public void joinJob(Job job){
		jobs.add(job);
		progression.put(job, new JobProgression(job, 0.0, 1, this));
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void leaveJob(Job job){
		jobs.remove(job);
		progression.remove(job);
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void transferJob(Job oldjob, Job newjob){
		JobProgression prog = progression.get(oldjob);
		jobs.remove(oldjob);
		jobs.add(newjob);
		progression.remove(oldjob);
		prog.setJob(newjob);
		progression.put(newjob, prog);
		
	}
	
	/**
	 * Checks if the player is in this job.
	 * @param job - the job
	 * @return true - they are in the job
	 * @return false - they are not in the job
	 */
	public boolean isInJob(Job job){
		return jobs.contains(job);
	}
	
	/**
	 * Function that reloads your honorific
	 */
	public void reloadHonorific(){
		String newHonorific = getDisplayHonorific();
        Player player = plugin.getServer().getPlayer(playername);
        if(player == null)
            return;
		
		if(newHonorific == null && honorific != null){
			// strip the current honorific.
		    player.setDisplayName(player.getDisplayName().trim().replaceFirst(honorific + " ", "").trim());
		}
		else if (newHonorific != null && honorific != null){
			// replace the honorific
		    player.setDisplayName(player.getDisplayName().trim().replaceFirst(honorific, newHonorific).trim());
		}
		else if(newHonorific != null && honorific == null){
			// new honorific
		    player.setDisplayName((newHonorific + " " + player.getDisplayName().trim()).trim());
		}
		// set the new honorific
		honorific = newHonorific;
	}
	
	/**
     * Function that removes your honorific
     */
    public void removeHonorific() {
        Player player = plugin.getServer().getPlayer(playername);
        if(player == null)
            return;
        if(honorific != null) {
            player.setDisplayName(player.getDisplayName().trim().replaceFirst(honorific + " ", "").trim());
        }
        honorific = null;
    }
	
	/**
	 * Function to reload all of the maximum experiences
	 */
	public void reloadMaxExperience(){
		HashMap<String, Double> param = new HashMap<String, Double>();
		param.put("numjobs", (double) progression.size());
		for(JobProgression temp: progression.values()){
			param.put("joblevel", (double) temp.getLevel());
			temp.setMaxExperience((int)temp.getJob().getMaxExp(param));
			param.remove("joblevel");
		}
	}
}
