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
import java.util.TreeMap;
import java.util.ArrayList;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.material.MaterialData;
import org.bukkit.util.config.Configuration;
import org.mbertoli.jfep.Parser;

import com.nidefawl.Stats.Stats;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.JobsMaterialInfo;
import com.zford.jobs.config.container.JobsLivingEntityInfo;
import com.zford.jobs.config.container.RestrictedArea;
import com.zford.jobs.config.container.Title;
import com.zford.jobs.dao.JobsDAO;
import com.zford.jobs.dao.JobsDAOH2;
import com.zford.jobs.dao.JobsDAOMySQL;
import com.zford.jobs.economy.JobsEconomyLink;
import com.zford.jobs.util.DisplayMethod;

/**
 * Configuration class.
 * 
 * Holds all the configuration information for the jobs plugin
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
 *
 */
public class JobsConfiguration {
	// enum of the chat display method
	private DisplayMethod dispMethod;
	// all of the possible jobs
	private HashMap<String, Job> jobs;
	// all of the possible titles
	private TreeMap<Integer, Title> titles;
	// how often to save the data in minutes
	private int savePeriod;
	// data access object being used.
	private JobsDAO dao;
	// JobsConfiguration object.
	private static JobsConfiguration jobsConfig = null;
	// economy plugin
	private JobsEconomyLink economy = null;
	// stats integration
	private Stats stats = null;
	// permissions integration
	private Permissions permissions = null;
	// do i broadcast skillups?
	private boolean broadcast;
	// maximum number of jobs a player can join
	private Integer maxJobs;
	// default economy plugin
	private String defaultEconomy;
	// used slots for each job
	private HashMap<Job, Integer> usedSlots;
	// is stats enabled
	private boolean statsEnabled;
	// can get money near spawner.
	private boolean payNearSpawner;
	
	private ArrayList<RestrictedArea> restrictedAreas;
	
	/**
	 * Private constructor.
	 * 
	 * Can only be called from within the class.
	 * Made to observe the singleton pattern.
	 */
	private JobsConfiguration(){
	    reload();
	}
	
	public void reload() {
        // general settings
        loadGeneralSettings();
        // job settings
        loadJobSettings();
        // title settings
        loadTitleSettings();
        // messages settings
        JobsMessages.getInstance().reloadConfig();
        // get slots
        loadSlots();
        // restricted areas
        loadRestrictedAreaSettings();
	}
	
	/**
	 * Load the slots available
	 */
	private void loadSlots() {
		usedSlots = new HashMap<Job, Integer>();
		for(Job temp: jobs.values()){
			usedSlots.put(temp, dao.getSlotsTaken(temp));
		}
	}

	/**
	 * Method to load the general configuration
	 * 
	 * loads from Jobs/generalConfig.yml
	 */
	private void loadGeneralSettings(){
        File f = new File("plugins/Jobs/generalConfig.yml");
        Configuration conf;
        if(!f.exists()) {
            // disable plugin
            System.err.println("[Jobs] - configuration file generalConfig.yml does not exist.  Disabling jobs !");
            Jobs.disablePlugin();
            return;
        }
        conf = new Configuration(f);
        conf.load();
        String storageMethod = conf.getString("storage-method", "");
        if(storageMethod.equalsIgnoreCase("mysql")) {
            String username = conf.getString("mysql-username");
            if(username == null) {
                System.err.println("[Jobs] - mysql-username property invalid or missing");
                Jobs.disablePlugin();
                return;
            }
            String password = conf.getString("mysql-password", "");
            String dbName = conf.getString("mysql-database");
            if(dbName == null) {
                System.err.println("[Jobs] - mysql-database property invalid or missing");
                Jobs.disablePlugin();
                return;
            }
            String url = conf.getString("mysql-url");
            if(url == null) {
                System.err.println("[Jobs] - mysql-url property invalid or missing");
                Jobs.disablePlugin();
                return;
            }
            String prefix = conf.getString("mysql-table-prefix", "");
            this.dao = new JobsDAOMySQL(url, dbName, username, password, prefix);
        }
        else if(storageMethod.equalsIgnoreCase("h2")) {
            this.dao = new JobsDAOH2();
        }
        else {
			// invalid selection
			System.err.println("[Jobs] - Storage method invalid or missing");
			Jobs.disablePlugin();
		}
        
        // save-period
        this.savePeriod = conf.getInt("save-period", -1);
        if(this.savePeriod <= 0) {
            System.out.println("[Jobs] - save-period property not found. Defaulting to 10!");
            savePeriod = 10;
        }
			
		    // broadcasting
        this.broadcast = conf.getBoolean("broadcast-on-skill-up", false);
        
        // enable stats
        this.statsEnabled = conf.getBoolean("enable-stats", false);
			
		    // enable pay near spawner
        this.payNearSpawner = conf.getBoolean("enable-pay-near-spawner", false);
        
        // max-jobs
        this.maxJobs = conf.getInt("max-jobs", -1);
        if(this.maxJobs == -1) {
            System.out.println("[Jobs] - max-jobs property not found. Defaulting to unlimited!");
            maxJobs = null;
        }
        
        // default economy plugin to use
        this.defaultEconomy = conf.getString("economy", "");
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
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + breakKey + " Break material type property. Disabling jobs!");
                        Jobs.disablePlugin();
                        return;
                    }
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Break."+breakKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Break."+breakKey+".experience", 0.0);
                    
                    jobBreakInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            } else {
                jobBreakInfo = null;
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
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + placeKey + " Place material type property. Disabling jobs!");
                        Jobs.disablePlugin();
                        return;
                    }
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Place."+placeKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Place."+placeKey+".experience", 0.0);
                    
                    jobPlaceInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            } else {
                jobPlaceInfo = null;
            }
            
            // kill
            List<String> killKeys = conf.getKeys("Jobs."+jobKey+".Kill");
            HashMap<String, JobsLivingEntityInfo> jobKillInfo = new HashMap<String, JobsLivingEntityInfo>();
            if(killKeys != null) {
                for(String killKey : killKeys) {
                    String entityType;
                    // puts it in the correct case
                    if(killKey.equalsIgnoreCase("pigzombie")){
                        entityType = "PigZombie";
                    }
                    else{
                        entityType = killKey.substring(0,1).toUpperCase() + killKey.substring(1).toLowerCase();
                    }
                    @SuppressWarnings("rawtypes")
                    Class victim;
                    try {
                        victim = Class.forName("org.bukkit.craftbukkit.entity.Craft"+entityType);
                    } catch (ClassNotFoundException e) {
                        System.err.println("[Jobs] - Job " + jobKey + " has an invalid " + killKey + " Kill entity type property. Disabling jobs!");
                        Jobs.disablePlugin();
                        return;
                    }
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Kill."+killKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Kill."+killKey+".experience", 0.0);
                    
                    jobKillInfo.put(("org.bukkit.craftbukkit.entity.Craft"+entityType).trim(), new JobsLivingEntityInfo(victim, experience, income));
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
            } else {
                jobFishInfo = null;
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
            
            // set to null if there are no kills
            if(jobKillInfo.size() == 0) {
                jobKillInfo = null;
            }
            
            this.jobs.put(jobName.toLowerCase(), new Job(jobBreakInfo, jobPlaceInfo, jobKillInfo, jobFishInfo, jobName, jobShortName, jobColour, maxExpEquation, incomeEquation, expEquation, displayMethod, maxLevel, maxSlots));
        }
	}
	
	/**
	 * Method to load the title configuration
	 * 
	 * loads from Jobs/titleConfig.yml
	 */
	private void loadTitleSettings(){
	    File f = new File("plugins/Jobs/titleConfig.yml");
        Configuration conf;
        if(!f.exists()) {
            // no titles detected
            this.titles = null;
            System.err.println("[Jobs] - configuration file titleConfig.yml does not exist, disabling titles");
            return;
        }
        conf = new Configuration(f);
        conf.load();
        List<String> titleKeys = conf.getKeys("Titles");
        if(titleKeys == null) {
            // no titles found
            System.err.println("[Jobs] - No titles found. Disabling titles");
            titles = null;
            return;
        }
        this.titles = new TreeMap<Integer, Title>();
        for(String titleKey : titleKeys) {
            String titleName = conf.getString("Titles."+titleKey+".Name");
            String titleShortName = conf.getString("Titles."+titleKey+".ShortName");
            ChatColor colour = ChatColor.valueOf(conf.getString("Titles."+titleKey+".ChatColour", "").toUpperCase());
            int levelReq = conf.getInt("Titles."+titleKey+".levelReq", -1);
            
            if(titleName == null) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid Name property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            if(titleShortName == null) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid ShortName property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            if(colour == null) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid ChatColour property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            if(levelReq == -1) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid levelReq property. Disabling jobs !");
                Jobs.disablePlugin();
                return;
            }
            
            this.titles.put(levelReq, new Title(titleName, titleShortName, colour, levelReq));
        }
	}
	

    /**
     * Method to load the restricted areas configuration
     * 
     * loads from Jobs/restrictedAreas.yml
     */
    private void loadRestrictedAreaSettings(){
        this.restrictedAreas = new ArrayList<RestrictedArea>();
        File f = new File("plugins/Jobs/restrictedAreas.yml");
        Configuration conf;
        if(f.exists()) {
            conf = new Configuration(f);
            conf.load();
            List<String> areaKeys = conf.getKeys("restrictedareas");
            List<World> worlds = Bukkit.getServer().getWorlds();
            if ( areaKeys == null ) {
                return;
            }
            for (String areaKey : areaKeys) {
                String worldName = conf.getString("restrictedareas."+areaKey+".world");
                World pointWorld = null;
                for (World world : worlds) {
                    if (world.getName().equals(worldName)) {
                        pointWorld = world;
                        break;
                    }
                }
                Location point1 = new Location(pointWorld,
                        conf.getDouble("restrictedareas."+areaKey+".point1.x", 0.0),
                        conf.getDouble("restrictedareas."+areaKey+".point1.y", 0.0),
                        conf.getDouble("restrictedareas."+areaKey+".point1.z", 0.0));

                Location point2 = new Location(pointWorld,
                        conf.getDouble("restrictedareas."+areaKey+".point2.x", 0.0),
                        conf.getDouble("restrictedareas."+areaKey+".point2.y", 0.0),
                        conf.getDouble("restrictedareas."+areaKey+".point2.z", 0.0));
                this.restrictedAreas.add(new RestrictedArea(point1, point2));
            }
        } else {
            System.err.println("[Jobs] - configuration file restrictedAreas.yml does not exist");
        }
    }
	
	/**
	 * Method to get the configuration.
	 * Never store this. Always call the function and then do something.
	 * @return the job configuration object
	 */
	public static JobsConfiguration getInstance(){
		if(jobsConfig == null){
			jobsConfig = new JobsConfiguration();
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
	 * Get the display method
	 * @return the display method
	 */
	public DisplayMethod getDisplayMethod(){
		return dispMethod;
	}
	
	/**
	 * Get how often in minutes to save job information
	 * @return how often in minutes to save job information
	 */
	public int getSavePeriod(){
		return savePeriod;
	}
	
	/**
	 * Get which economy plugin should we use
	 * @return which economy plugin should we use
	 */
	public int getDefaultEconomy(){
		return defaultEconomy;
	}
	
	/**
	 * Get the Data Access Object for the plugin
	 * @return the DAO of the plugin
	 */
	public JobsDAO getJobsDAO(){
		return dao;
	}
	
	/**
	 * Gets the economy interface to the economy being used
	 * @return the interface to the economy being used
	 */
	public JobsEconomyLink getEconomyLink(){
		return economy;
	}
	
	/**
	 * Unhook the all plugins being used
	 */
	public void unhookAll(){
		economy = null;
		stats = null;
		permissions = null;
	}
	
	/**
	 * Set the economy link
	 * @param economy - the new economy link
	 */
	public void setEconomyLink(JobsEconomyLink economy){
		this.economy = economy;
	}
	
	/**
	 * Getter for the stats plugin
	 * @return the stats plugin
	 */
	public Stats getStats() {
		return stats;
	}

	/**
	 * Setter for the stats plugin
	 * @param stats - the stats plugin
	 */
	public void setStats(Stats stats) {
		this.stats = stats;
	}

	/**
	 * Getter for the permissions plugin
	 * @return the permissions plugin
	 */
	public Permissions getPermissions() {
		return permissions;
	}

	/**
	 * Setter for the permissions plugin
	 * @param permissions - the permissions plugin
	 */
	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}
	
	/**
	 * Get the message with the correct key
	 * @param key - the key of the message
	 * @return the message
	 */
	/*
	@Deprecated
	public String getMessage(String key){
	    return JobsMessages.getInstance().getMessage(key);
	}
	*/
	
	/**
	 * Function that tells if the system is set to broadcast on skill up
	 * @return true - broadcast on skill up
	 * @return false - do not broadcast on skill up
	 */
	public boolean isBroadcasting(){
		return broadcast;
	}
	
	/**
	 * Function to return the title for a given level
	 * @return the correct title
	 * @return null if no title matches
	 */
	public Title getTitleForLevel(int level){
		Title title = null;
		if(titles != null){
			for(Title temp: titles.values()){
				if(title == null){
					if(temp.getLevelReq() <= level){
						title = temp;
					}
				}
				else {
					if(temp.getLevelReq() <= level && temp.getLevelReq() > title.getLevelReq()){
						title = temp;
					}
				}
			}
		}
		return title;
	}
	
	/**
	 * Get all the jobs loaded in the plugin
	 * @return a collection of the jobs
	 */
	public Collection<Job> getJobs(){
		return jobs.values();
	}
	
	/**
	 * Function to return the maximum number of jobs a player can join
	 * @return
	 */
	public Integer getMaxJobs(){
		return maxJobs;
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
	
	/**
	 * Function to check if stats is enabled
	 * @return true - stats is enabled
	 * @return false - stats is disabled
	 */
	public boolean isStatsEnabled(){
		return statsEnabled;
	}
	
	/**
	 * Function to check if you get paid near a spawner is enabled
	 * @return true - you get paid
	 * @return false - you don't get paid
	 */
	public boolean payNearSpawner(){
		return payNearSpawner;
	}
	
   /**
     * Function to get the restricted areas on the server
     * @return restricted areas on the server
     */
	public List<RestrictedArea> getRestrictedAreas() {
	    return this.restrictedAreas;
	}
}
