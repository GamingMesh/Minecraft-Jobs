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

package me.zford.jobs.config.container;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


import me.zford.jobs.Jobs;
import me.zford.jobs.dao.JobsDAO;
import me.zford.jobs.dao.container.JobsDAOData;
import me.zford.jobs.event.JobsLevelUpEvent;
import me.zford.jobs.event.JobsSkillUpEvent;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;


public class JobsPlayer {
    // jobs plugin
    private Jobs plugin;
	// the player the object belongs to
	private String playername;
	// progression of the player in each job
	private LinkedHashMap<Job, JobProgression> progression = new LinkedHashMap<Job, JobProgression>();
	// display honorific
	private String honorific;
		
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
		// for all jobs players have
		List<JobsDAOData> list = dao.getAllJobs(this);
		if (list != null) {
			for (JobsDAOData jobdata: list) {
				if (plugin.getJobConfig().getJob(jobdata.getJobName()) != null) {
					// add the job
				    Job job = plugin.getJobConfig().getJob(jobdata.getJobName());
					if (job != null) {
    					// create the progression object
    					JobProgression jobProgression = new JobProgression(plugin, job, jobdata.getExperience(), jobdata.getLevel(), this);
    					// calculate the max level
    					
    					// add the progression level.
    					progression.put(job, jobProgression);
					}
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
                plugin.getEconomy().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getBreakIncome(block, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
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
                plugin.getEconomy().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getPlaceIncome(block, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
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
                plugin.getEconomy().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();	
			}
			param.remove("joblevel");
		}
		// no job
		if(this.progression.size() == 0) {
		    Job jobNone = plugin.getJobConfig().getJob("None");
		    if(jobNone != null) {
    		    param.put("joblevel", 1.0);
    		    Double income = jobNone.getKillIncome(victim, param);
    		    if(income != null) {
    		        // give income
    		        plugin.getEconomy().pay(this, income*multiplier);
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
                plugin.getEconomy().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();
            }
            param.remove("joblevel");
        }
	    // no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getFishIncome(item, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
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
                plugin.getEconomy().pay(this, income*multiplier);
				entry.getValue().addExp(exp*multiplier);
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getCraftIncome(items, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
	}
	
	/**
	 * Get the list of jobs
	 * @return the list of jobs
	 */
	public Set<Job> getJobs() {
		return Collections.unmodifiableSet(progression.keySet());
	}
	
	/**
	 * Get the list of job progressions
	 * @return the list of job progressions
	 */
	public Collection<JobProgression> getJobsProgression() {
		return Collections.unmodifiableCollection(progression.values());
	}
	
	/**
	 * Get the job progression with the certain job
	 * @return the job progression
	 */
	public JobProgression getJobsProgression(Job job) {
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
			
			if(plugin.getJobsConfiguration().getTitleForLevel(temp.getLevel()) != null && !plugin.getJobsConfiguration().getTitleForLevel(temp.getLevel()).equals(temp.getTitle())){
				// user would skill up
				JobsSkillUpEvent event = new JobsSkillUpEvent(this, temp, plugin.getJobsConfiguration().getTitleForLevel(temp.getLevel()));
				plugin.getServer().getPluginManager().callEvent(event);
			}
		}
	}
	
	public String getDisplayHonorific() {
	    return honorific;
	}
	
	/**
	 * Player joins a job
	 * @param job - the job joined
	 */
	public void joinJob(Job job) {
		progression.put(job, new JobProgression(plugin, job, 0.0, 1, this));
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void leaveJob(Job job) {
		progression.remove(job);
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void transferJob(Job oldjob, Job newjob) {
		JobProgression prog = progression.remove(oldjob);
		prog.setJob(newjob);
		progression.put(newjob, prog);
	}
	
	/**
	 * Checks if the player is in this job.
	 * @param job - the job
	 * @return true - they are in the job
	 * @return false - they are not in the job
	 */
	public boolean isInJob(Job job) {
		return progression.containsKey(job);
	}
	
	/**
	 * Function that reloads your honorific
	 */
	public void reloadHonorific() {
        StringBuilder builder = new StringBuilder();
        int numJobs = progression.size();
        boolean gotTitle = false;
        for (JobProgression prog: progression.values()) {
            DisplayMethod method = prog.getJob().getDisplayMethod();
            
            if (method.equals(DisplayMethod.NONE))
                continue;
            
            if (gotTitle) {
                builder.append(" ");
                gotTitle = false;
            }
            
            if (numJobs == 1) {
                if (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.TITLE)) {
                    if (prog.getTitle() != null) {
                        builder.append(prog.getTitle().getChatColor() + prog.getTitle().getName() + ChatColor.WHITE);
                        gotTitle = true;
                    }
                }
                
                if(method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.JOB)) {
                    if (gotTitle) {
                        builder.append(" ");
                    }
                    builder.append(prog.getJob().getChatColour() + prog.getJob().getName() + ChatColor.WHITE);
                    gotTitle = true;
                }
            }
            
            if (numJobs > 1 && (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.TITLE)) ||
                    method.equals(DisplayMethod.SHORT_FULL) ||
                    method.equals(DisplayMethod.SHORT_TITLE)) {
                // add title to honorific
                if (prog.getTitle() != null) {
                    builder.append(prog.getTitle().getChatColor() + prog.getTitle().getShortName() + ChatColor.WHITE);
                    gotTitle = true;
                }
            }
            
            if (numJobs > 1 && (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.JOB)) ||
                    method.equals(DisplayMethod.SHORT_FULL) || 
                    method.equals(DisplayMethod.SHORT_JOB)) {
                builder.append(prog.getJob().getChatColour() + prog.getJob().getShortName() + ChatColor.WHITE);
                gotTitle = true;
            }
        }
        
        honorific = builder.toString().trim();
	}
	
	/**
	 * Function to reload all of the maximum experiences
	 */
	public void reloadMaxExperience() {
		HashMap<String, Double> param = new HashMap<String, Double>();
		param.put("numjobs", (double) progression.size());
		for (JobProgression prog: progression.values()) {
			param.put("joblevel", (double) prog.getLevel());
			prog.setMaxExperience((int)prog.getJob().getMaxExp(param));
			param.remove("joblevel");
		}
	}
}
