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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.nidefawl.Stats.Stats;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.RestrictedArea;
import com.zford.jobs.config.container.Title;
import com.zford.jobs.dao.JobsDAO;
import com.zford.jobs.dao.JobsDAOH2;
import com.zford.jobs.dao.JobsDAOMySQL;
import com.zford.jobs.economy.BufferedPayment;
import com.zford.jobs.economy.link.EconomyLink;
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
	// all of the possible titles
	private TreeMap<Integer, Title> titles;
	// how often to save the data in minutes
	private int savePeriod;
	// data access object being used.
	private JobsDAO dao;
	// economy plugin
	private EconomyLink economy = null;
	// economy payment buffer
    private BufferedPayment bufferedPayment;
	// stats integration
	private Stats stats = null;
	// do i broadcast skillups?
    private boolean broadcastSkillups;
    // do i broadcast level ups?
    private boolean broadcastLevelups;
	// maximum number of jobs a player can join
	private Integer maxJobs;
	// is stats enabled
	private boolean statsEnabled;
	// can get money near spawner.
	private boolean payNearSpawner;
	// default economy plugin
	private String defaultEconomy = null;
	
	private ArrayList<RestrictedArea> restrictedAreas;
	
	private Jobs plugin;
	
	public JobsConfiguration(Jobs plugin) {
	    this.plugin = plugin;
	    this.bufferedPayment = new BufferedPayment(plugin);
	}
	
	public void reload() {
        // general settings
        loadGeneralSettings();
        // title settings
        loadTitleSettings();
        // restricted areas
        loadRestrictedAreaSettings();
	}

	/**
	 * Method to load the general configuration
	 * 
	 * loads from Jobs/generalConfig.yml
	 */
	private void loadGeneralSettings(){
        File f = new File("plugins/Jobs/generalConfig.yml");
        YamlConfiguration conf;
        if(!f.exists()) {
            // disable plugin
            System.err.println("[Jobs] - configuration file generalConfig.yml does not exist.  Disabling jobs !");
            plugin.disablePlugin();
            return;
        }
        conf = new YamlConfiguration();
        try {
            conf.load(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        String storageMethod = conf.getString("storage-method", "");
        if(storageMethod.equalsIgnoreCase("mysql")) {
            String username = conf.getString("mysql-username");
            if(username == null) {
                System.err.println("[Jobs] - mysql-username property invalid or missing");
                plugin.disablePlugin();
                return;
            }
            String password = conf.getString("mysql-password", "");
            String dbName = conf.getString("mysql-database");
            if(dbName == null) {
                System.err.println("[Jobs] - mysql-database property invalid or missing");
                plugin.disablePlugin();
                return;
            }
            String url = conf.getString("mysql-url");
            if(url == null) {
                System.err.println("[Jobs] - mysql-url property invalid or missing");
                plugin.disablePlugin();
                return;
            }
            String prefix = conf.getString("mysql-table-prefix", "");
            this.dao = new JobsDAOMySQL(plugin, url, dbName, username, password, prefix);
        }
        else if(storageMethod.equalsIgnoreCase("h2")) {
            this.dao = new JobsDAOH2(plugin);
        }
        else {
			// invalid selection
			System.err.println("[Jobs] - Storage method invalid or missing");
			plugin.disablePlugin();
		}
        
        // save-period
        this.savePeriod = conf.getInt("save-period", -1);
        if(this.savePeriod <= 0) {
            System.out.println("[Jobs] - save-period property not found. Defaulting to 10!");
            savePeriod = 10;
        }
			
		// broadcasting
        this.broadcastSkillups = conf.getBoolean("broadcast-on-skill-up", false);
        this.broadcastLevelups = conf.getBoolean("broadcast-on-level-up", false);
        
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
        this.defaultEconomy = conf.getString("economy");
	}
	
	/**
	 * Method to load the title configuration
	 * 
	 * loads from Jobs/titleConfig.yml
	 */
	private void loadTitleSettings(){
	    File f = new File("plugins/Jobs/titleConfig.yml");
        YamlConfiguration conf;
        if(!f.exists()) {
            // no titles detected
            this.titles = null;
            System.err.println("[Jobs] - configuration file titleConfig.yml does not exist, disabling titles");
            return;
        }
        conf = new YamlConfiguration();
        try {
            conf.load(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ConfigurationSection titleSection = conf.getConfigurationSection("Titles");
        if(titleSection == null) {
            // no titles found
            System.err.println("[Jobs] - No titles found. Disabling titles");
            titles = null;
            return;
        }
        this.titles = new TreeMap<Integer, Title>();
        for(String titleKey : titleSection.getKeys(false)) {
            String titleName = conf.getString("Titles."+titleKey+".Name");
            String titleShortName = conf.getString("Titles."+titleKey+".ShortName");
            ChatColor colour = ChatColor.valueOf(conf.getString("Titles."+titleKey+".ChatColour", "").toUpperCase());
            int levelReq = conf.getInt("Titles."+titleKey+".levelReq", -1);
            
            if(titleName == null) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid Name property. Disabling jobs !");
                plugin.disablePlugin();
                return;
            }
            if(titleShortName == null) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid ShortName property. Disabling jobs !");
                plugin.disablePlugin();
                return;
            }
            if(colour == null) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid ChatColour property. Disabling jobs !");
                plugin.disablePlugin();
                return;
            }
            if(levelReq == -1) {
                System.err.println("[Jobs] - Title " + titleKey + " has an invalid levelReq property. Disabling jobs !");
                plugin.disablePlugin();
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
        YamlConfiguration conf;
        if(!f.exists()) {
            System.err.println("[Jobs] - configuration file restrictedAreas.yml does not exist");
            return;
        }
        conf = new YamlConfiguration();
        try {
            conf.load(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ConfigurationSection areaSection = conf.getConfigurationSection("restrictedareas");
        List<World> worlds = Bukkit.getServer().getWorlds();
        if (areaSection == null)
            return;
        
        for (String areaKey : areaSection.getKeys(false)) {
            String worldName = conf.getString("restrictedareas."+areaKey+".world");
            double multiplier = conf.getDouble("restrictedareas."+areaKey+".multiplier", 0.0);
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
            this.restrictedAreas.add(new RestrictedArea(point1, point2, multiplier));
        }
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
	public String getDefaultEconomy(){
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
	public EconomyLink getEconomyLink(){
		return economy;
	}
	
	/**
	 * Set the economy link
	 * @param economy - the new economy link
	 */
	public void setEconomyLink(EconomyLink economy){
		this.economy = economy;
	}
    
    /**
     * Gets the economy buffered payment
     * @return the buffered payment class
     */
    public BufferedPayment getBufferedPayment() {
        return bufferedPayment;
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
	 * Function that tells if the system is set to broadcast on skill up
	 * @return true - broadcast on skill up
	 * @return false - do not broadcast on skill up
	 */
	public boolean isBroadcastingSkillups(){
		return broadcastSkillups;
	}
	
	/**
     * Function that tells if the system is set to broadcast on level up
     * @return true - broadcast on level up
     * @return false - do not broadcast on level up
     */
    public boolean isBroadcastingLevelups(){
        return broadcastLevelups;
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
	 * Function to return the maximum number of jobs a player can join
	 * @return
	 */
	public Integer getMaxJobs(){
		return maxJobs;
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
    
    /**
     * Gets the area multiplier for the player
     * @param player
     * @return - the multiplier
     */
    public double getRestrictedMultiplier(Player player) {
        for(RestrictedArea area : getRestrictedAreas()) {
            if (area.inRestrictedArea(player))
                return area.getMultiplier();
        }
        return 1.0;
    }
}
