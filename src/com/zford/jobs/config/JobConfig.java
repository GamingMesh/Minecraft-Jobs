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

package com.zford.jobs.config;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.util.config.Configuration;
import org.mbertoli.jfep.Parser;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.JobsMaterialInfo;
import com.zford.jobs.config.container.JobsLivingEntityInfo;
import com.zford.jobs.util.DisplayMethod;

/**
 * Configuration class.
 * 
 * Holds all the configuration information for the jobs plugin
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
 *
 */
@SuppressWarnings("deprecation")
public class JobConfig {
	// all of the possible jobs
	private HashMap<String, Job> jobs;
	// JobConfig object.
	private static JobConfig jobsConfig = null;
	// used slots for each job
	private HashMap<Job, Integer> usedSlots;
	
	/**
	 * Private constructor.
	 * 
	 * Can only be called from within the class.
	 * Made to observe the singleton pattern.
	 */
	private JobConfig(){
	}
	
	public void reload() {
        // job settings
        loadJobSettings();
        // get slots
        loadSlots();
	}
	
	/**
	 * Method to load the jobs configuration
	 * 
	 * loads from Jobs/jobConfig.yml
	 */
	private void loadJobSettings(){
	    File f = new File("plugins/Jobs/jobConfig.yml");
        Configuration conf;
        this.jobs = new HashMap<String, Job>();
        if(!f.exists()) {
            // disable plugin
            System.err.println("[Jobs] - configuration file jobConfig.yml does not exist.  Disabling jobs !");
            Jobs.disablePlugin();
            return;
        }
        conf = new Configuration(f);
        conf.load();
        List<String> jobKeys = conf.getKeys("Jobs");
        if(jobKeys == null) {
            // no jobs
            System.err.println("[Jobs] - No jobs detected. Disabling Jobs!");
            Jobs.disablePlugin();
            return;
        }
        for(String jobKey : jobKeys) {
            String jobName = conf.getString("Jobs."+jobKey+".fullname");
            if(jobName == null) {
                System.err.println("[Jobs] - Job " + jobKey + " has an invalid fullname property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            
            Integer maxLevel = conf.getInt("Jobs."+jobKey+".max-level", -1);
            if(maxLevel.intValue() == -1) {
                maxLevel = null;
                System.out.println("[Jobs] - Job " + jobKey + " is missing the max-level property. defaulting to no limits !");
            }

            Integer maxSlots = conf.getInt("Jobs."+jobKey+".slots", -1);
            if(maxSlots.intValue() == -1) {
                maxSlots = null;
                System.out.println("[Jobs] - Job " + jobKey + " is missing the slots property. defaulting to no limits !");
            }

            String jobShortName = conf.getString("Jobs."+jobKey+".shortname");
            if(jobShortName == null) {
                System.err.println("[Jobs] - Job " + jobKey + " is missing the shortname property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }

            ChatColor jobColour = ChatColor.valueOf(conf.getString("Jobs."+jobKey+".ChatColour", "").toUpperCase());
            if(jobColour == null) {
                System.err.println("[Jobs] - Job " + jobKey + " is missing the ChatColour property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            String disp = conf.getString("Jobs."+jobKey+".chat-display", "").toLowerCase();
            DisplayMethod displayMethod;
            if(disp.equals("full")){
                // full
                displayMethod = DisplayMethod.FULL;
            }
            else if(disp.equals("job")){
                // job only
                displayMethod = DisplayMethod.JOB;
            }
            else if(disp.equals("title")){
                // title only
                displayMethod = DisplayMethod.TITLE;
            }
            else if(disp.equals("none")){
                // none
                displayMethod = DisplayMethod.NONE;
            }
            else if(disp.equals("shortfull")){
                // none
                displayMethod = DisplayMethod.SHORT_FULL;
            }
            else if(disp.equals("shortjob")){
                // none
                displayMethod = DisplayMethod.SHORT_JOB;
            }
            else if(disp.equals("shorttitle")){
                // none
                displayMethod = DisplayMethod.SHORT_TITLE;
            }
            else {
                // error
                System.err.println("[Jobs] - Job " + jobKey + " has an invalid chat-display property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            
            Parser maxExpEquation;
            String maxExpEquationInput = conf.getString("Jobs."+jobKey+".leveling-progression-equation");
            try {
                maxExpEquation = new Parser(maxExpEquationInput);
            }
            catch(Exception e){
                System.err.println("[Jobs] - Job " + jobKey + " has an invalid leveling-progression-equation property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            
            Parser incomeEquation;
            String incomeEquationInput = conf.getString("Jobs."+jobKey+".income-progression-equation");
            try {
                incomeEquation = new Parser(incomeEquationInput);
            }
            catch(Exception e){
                System.err.println("[Jobs] - Job " + jobKey + " has an invalid income-progression-equation property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            
            Parser expEquation;
            String expEquationInput = conf.getString("Jobs."+jobKey+".experience-progression-equation");
            try{
                expEquation = new Parser(expEquationInput);
            }
            catch(Exception e){
                System.err.println("[Jobs] - Job " + jobKey + " has an invalid experience-progression-equation property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            
            // items
            
            // break
            List<String> breakKeys = conf.getKeys("Jobs."+jobKey+".Break");
            HashMap<String, JobsMaterialInfo> jobBreakInfo = new HashMap<String, JobsMaterialInfo>();
            if(breakKeys != null) {
                for(String breakKey : breakKeys) {
                    String materialType = breakKey.toUpperCase();
                    String subType = "";
                    Material material;
                    if(materialType.contains("-")) {
                        // uses subType
                        subType = ":"+materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    try {
                        material = Material.matchMaterial(materialType);
                    }
                    catch(IllegalArgumentException e) {
                        material = null;
                    }
                    if(material == null) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + breakKey + " Break material type property. Skipping!");
                        continue;
                    }
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Break."+breakKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Break."+breakKey+".experience", 0.0);
                    
                    jobBreakInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // place
            List<String> placeKeys = conf.getKeys("Jobs."+jobKey+".Place");
            HashMap<String, JobsMaterialInfo> jobPlaceInfo = new HashMap<String, JobsMaterialInfo>();
            if(placeKeys != null) {
                for(String placeKey : placeKeys) {
                    String materialType = placeKey.toUpperCase();
                    String subType = "";
                    Material material;
                    if(materialType.contains("-")) {
                        // uses subType
                        subType = ":"+materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    try {
                        material = Material.matchMaterial(materialType);
                    }
                    catch(IllegalArgumentException e) {
                        material = null;
                    }
                    if(material == null) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + placeKey + " Place material type property. Skipping!");
                        continue;
                    }
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Place."+placeKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Place."+placeKey+".experience", 0.0);
                    
                    jobPlaceInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // craft
            List<String> craftKeys = conf.getKeys("Jobs."+jobKey+".Craft");
            HashMap<String, JobsMaterialInfo> jobCraftInfo = new HashMap<String, JobsMaterialInfo>();
            if(craftKeys != null) {
                for(String craftKey : craftKeys) {
                    String materialType = craftKey.toUpperCase();
                    String subType = "";
                    Material material;
                    if(materialType.contains("-")) {
                        // uses subType
                        subType = ":"+materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    try {
                        material = Material.matchMaterial(materialType);
                    }
                    catch(IllegalArgumentException e) {
                        material = null;
                    }
                    if(material == null) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + craftKey + " Craft material type property. Skipping!");
                        continue;
                    }
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Craft."+craftKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Craft."+craftKey+".experience", 0.0);
                    
                    jobCraftInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // kill
            List<String> killKeys = conf.getKeys("Jobs."+jobKey+".Kill");
            HashMap<String, JobsLivingEntityInfo> jobKillInfo = new HashMap<String, JobsLivingEntityInfo>();
            if(killKeys != null) {
                for(String killKey : killKeys) {
                    @SuppressWarnings("rawtypes")
                    Class victim;
                    try {
                        victim = Class.forName("org.bukkit.craftbukkit.entity.Craft"+killKey);
                    } catch (ClassNotFoundException e) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + killKey + " Kill entity type property. Skipping!");
                        continue;
                    }
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Kill."+killKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Kill."+killKey+".experience", 0.0);
                    
                    jobKillInfo.put(("org.bukkit.craftbukkit.entity.Craft"+killKey).trim(), new JobsLivingEntityInfo(victim, experience, income));
                }
            }
            
            // fish
            List<String> fishKeys = conf.getKeys("Jobs."+jobKey+".Fish");
            HashMap<String, JobsMaterialInfo> jobFishInfo = new HashMap<String, JobsMaterialInfo>();
            if(fishKeys != null) {
                for(String fishKey : fishKeys) {
                    String materialType = fishKey.toUpperCase();
                    String subType = "";
                    Material material;
                    if(materialType.contains("-")) {
                        // uses subType
                        subType = ":"+materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    try {
                        material = Material.matchMaterial(materialType);
                    }
                    catch(IllegalArgumentException e) {
                        material = null;
                    }
                    if(material == null) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + fishKey + " Fish material type property. Disabling jobs!");
                        Jobs.disablePlugin();
                        return;
                    }
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Fish."+fishKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Fish."+fishKey+".experience", 0.0);
                    
                    jobFishInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // custom-kill
            List<String> customKillKeys = conf.getKeys("Jobs."+jobKey+".custom-kill");
            if(customKillKeys != null) {
                for(String customKillKey : customKillKeys) {
                    String entityType = customKillKey.toString();
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".custom-kill."+customKillKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".custom-kill."+customKillKey+".experience", 0.0);
                    
                    try {
                        jobKillInfo.put(("org.bukkit.craftbukkit.entity.CraftPlayer:"+entityType).trim(), new JobsLivingEntityInfo(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer"), experience, income));
                    } catch (ClassNotFoundException e) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + customKillKey + " custom-kill entity type property. Disabling jobs!");
                        Jobs.disablePlugin();
                        return;
                    }
                }
            }
            
            this.jobs.put(jobName.toLowerCase(), new Job(jobBreakInfo, jobPlaceInfo, jobKillInfo, jobFishInfo, jobCraftInfo, jobName, jobShortName, jobColour, maxExpEquation, incomeEquation, expEquation, displayMethod, maxLevel, maxSlots));
        }
	}
    
    /**
     * Load the slots available
     */
    private void loadSlots() {
        usedSlots = new HashMap<Job, Integer>();
        for(Job temp: jobs.values()){
            usedSlots.put(temp, JobsConfiguration.getInstance().getJobsDAO().getSlotsTaken(temp));
        }
    }
	
	/**
	 * Method to get the configuration.
	 * Never store this. Always call the function and then do something.
	 * @return the job configuration object
	 */
	public static JobConfig getInstance(){
		if(jobsConfig == null){
			jobsConfig = new JobConfig();
		}
		return jobsConfig;
	}
	
	/**
	 * Function to return the job information that matches the jobName given
	 * @param jobName - the ame of the job given
	 * @return the job that matches the name
	 */
	public Job getJob(String jobName){
		return jobs.get(jobName.toLowerCase());
	}
	
	/**
	 * Get all the jobs loaded in the plugin
	 * @return a collection of the jobs
	 */
	public Collection<Job> getJobs(){
		return jobs.values();
	}
	
	/**
	 * Function to get the number of slots used on the server for this job
	 * @param job - the job
	 * @return the number of slots
	 */
	public Integer getUsedSlots(Job job){
		return usedSlots.get(job);
	}
	
	/**
	 * Function to increase the number of used slots for a job
	 * @param job - the job someone is taking
	 */
	public void takeSlot(Job job){
		usedSlots.put(job, usedSlots.get(job)+1);
	}
	
	/**
	 * Function to decrease the number of used slots for a job
	 * @param job - the job someone is leaving
	 */
	public void leaveSlot(Job job){
		usedSlots.put(job, usedSlots.get(job)-1);
	}
}
