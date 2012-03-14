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

package me.zford.jobs.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;


import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.RestrictedArea;
import me.zford.jobs.config.container.Title;
import me.zford.jobs.dao.JobsDAO;
import me.zford.jobs.dao.JobsDAOH2;
import me.zford.jobs.dao.JobsDAOMySQL;
import me.zford.jobs.util.ClassPathHack;
import me.zford.jobs.util.FileDownloader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


/**
 * Configuration class.
 * 
 * Holds all the configuration information for the jobs plugin
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
 *
 */
public class JobsConfiguration {
    private YamlConfiguration generalConfig;
	// all of the possible titles
	private List<Title> titles = new ArrayList<Title>();
	// data access object being used.
	private JobsDAO dao;
	
	private ArrayList<RestrictedArea> restrictedAreas = new ArrayList<RestrictedArea>();
	
	private Jobs plugin;
	
	public JobsConfiguration(Jobs plugin) {
	    this.plugin = plugin;
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
        File f = new File(plugin.getDataFolder(), "generalConfig.yml");
        
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        generalConfig = new YamlConfiguration();
        CommentedYamlConfiguration writer = new CommentedYamlConfiguration();
        StringBuilder header = new StringBuilder();
        header.append("General configuration.");
        header.append(System.getProperty("line.separator"));
        header.append("  The general configuration for the jobs plugin mostly includes how often the plugin");
        header.append(System.getProperty("line.separator"));
        header.append("saves user data (when the user is in the game), the storage method, whether");
        header.append(System.getProperty("line.separator"));
        header.append("to broadcast a message to the server when a user goes up a skill level.");
        header.append(System.getProperty("line.separator"));
        header.append("  It also allows admins to set the maximum number of jobs a player can have at");
        header.append(System.getProperty("line.separator"));
        header.append("any one time.");
        header.append(System.getProperty("line.separator"));
        
        generalConfig.options().copyDefaults(true);
        
        writer.options().header(header.toString());

        writer.addComment("storage-method", "storage method, can be MySQL, h2");
        generalConfig.addDefault("storage-method", "h2");
        
        writer.addComment("mysql-username", "Requires Mysql.");
        generalConfig.addDefault("mysql-username", "root");        
        generalConfig.addDefault("mysql-password", "");
        generalConfig.addDefault("mysql-url", "jdbc:mysql://localhost:3306/");
        generalConfig.addDefault("mysql-table-prefix", "");
        
        writer.addComment("save-period", 
                "How often in minutes you want it to save, 0 disables periodic saving and",
                "the system will only save on logout"
        );
        generalConfig.addDefault("save-period", 10);
        
        writer.addComment("broadcast-on-skill-up", "Do all players get a message when somone goes up a skill level?");
        generalConfig.addDefault("broadcast-on-skill-up", false);
        
        writer.addComment("broadcast-on-level-up", "Do all players get a message when somone goes up a level?");
        generalConfig.addDefault("broadcast-on-level-up", false);
        
        writer.addComment("max-jobs",
                "Maximum number of jobs a player can join.",
                "Use 0 for no maximum"
        );
        generalConfig.addDefault("max-jobs", 3);
        
        writer.addComment("enable-pay-near-spawner", "option to allow payment to be made when killing mobs from a spawner");
        generalConfig.addDefault("enable-pay-near-spawner", false);
        
        try {
            generalConfig.load(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        
        String storageMethod = generalConfig.getString("storage-method");
        if(storageMethod.equalsIgnoreCase("mysql")) {
            String username = generalConfig.getString("mysql-username");
            if(username == null) {
                plugin.getLogger().severe("[Jobs] - mysql-username property invalid or missing");
                plugin.disablePlugin();
            }
            String password = generalConfig.getString("mysql-password");
            String dbName = generalConfig.getString("mysql-database");
            String url = generalConfig.getString("mysql-url");
            String prefix = generalConfig.getString("mysql-table-prefix");
            if (plugin.isEnabled())
                this.dao = new JobsDAOMySQL(plugin, url, dbName, username, password, prefix);
        } else if(storageMethod.equalsIgnoreCase("h2")) {
            File h2jar = new File(plugin.getDataFolder(), "h2.jar");
            if (!h2jar.exists()) {
                plugin.getLogger().info("[Jobs] H2 library not found, downloading...");
                try {
                    FileDownloader.downloadFile(new URL("http://repo2.maven.org/maven2/com/h2database/h2/1.3.164/h2-1.3.164.jar"), h2jar);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    plugin.getLogger().severe("[Jobs] Could not download database library.  Disabling jobs!");
                    plugin.disablePlugin();
                }
            }
            if (plugin.isEnabled()) {
                try {
                    ClassPathHack.addFile(h2jar);
                } catch (IOException e) {
                    plugin.getLogger().severe("[Jobs] Could not load database library.  Disabling jobs!");
                    plugin.disablePlugin();
                }
                if (plugin.isEnabled())
                    this.dao = new JobsDAOH2(plugin);
            }
        } else {
			plugin.getLogger().severe("[Jobs] - Invalid storage method!  Disabling jobs!");
            plugin.disablePlugin();
		}
        
        // save-period
        if(generalConfig.getInt("save-period") <= 0) {
            plugin.getLogger().info("[Jobs] - Invalid save-period property! Defaulting to 10!");
            generalConfig.set("save-period", 10);
        }
        
        // Make sure we're only copying settings we care about
        copySetting(generalConfig, writer, "storage-method");
        copySetting(generalConfig, writer, "mysql-username");
        copySetting(generalConfig, writer, "mysql-password");
        copySetting(generalConfig, writer, "mysql-url");
        copySetting(generalConfig, writer, "mysql-table-prefix");
        copySetting(generalConfig, writer, "save-period");
        copySetting(generalConfig, writer, "broadcast-on-skill-up");
        copySetting(generalConfig, writer, "broadcast-on-level-up");
        copySetting(generalConfig, writer, "max-jobs");
        copySetting(generalConfig, writer, "enable-pay-near-spawner");
        
        // Write back config
        try {
            writer.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void copySetting(Configuration reader, Configuration writer, String path) {
	    writer.set(path, reader.get(path));
	}
	
	/**
	 * Method to load the title configuration
	 * 
	 * loads from Jobs/titleConfig.yml
	 */
	private void loadTitleSettings(){
	    this.titles.clear();
	    File f = new File(plugin.getDataFolder(), "titleConfig.yml");
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration conf = new YamlConfiguration();
        StringBuilder header = new StringBuilder()
            .append("Title configuration")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Stores the titles people gain at certain levels.")
            .append(System.getProperty("line.separator"))
            .append("Each title requres to have a name, short name (used when the player has more than")
            .append(System.getProperty("line.separator"))
            .append("1 job) the colour of the title and the level requrirement to attain the title.")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("It is recommended but not required to have a title at level 0.")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Titles are completely optional.")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Titles:").append(System.getProperty("line.separator"))
            .append("  Apprentice:").append(System.getProperty("line.separator"))
            .append("    Name: Apprentice").append(System.getProperty("line.separator"))
            .append("    ShortName: A").append(System.getProperty("line.separator"))
            .append("    ChatColour: WHITE").append(System.getProperty("line.separator"))
            .append("    levelReq: 0").append(System.getProperty("line.separator"))
            .append("  Novice:").append(System.getProperty("line.separator"))
            .append("    Name: Novice").append(System.getProperty("line.separator"))
            .append("    ShortName: N").append(System.getProperty("line.separator"))
            .append("    ChatColour: GRAY").append(System.getProperty("line.separator"))
            .append("    levelReq: 30").append(System.getProperty("line.separator"))
            .append("  Journeyman:").append(System.getProperty("line.separator"))
            .append("    Name: Journeyman").append(System.getProperty("line.separator"))
            .append("    ShortName: J").append(System.getProperty("line.separator"))
            .append("    ChatColour: GOLD").append(System.getProperty("line.separator"))
            .append("    levelReq: 60").append(System.getProperty("line.separator"))
            .append("  Master:").append(System.getProperty("line.separator"))
            .append("    Name: Master").append(System.getProperty("line.separator"))
            .append("    ShortName: M").append(System.getProperty("line.separator"))
            .append("    ChatColour: BLACK").append(System.getProperty("line.separator"))
            .append("    levelReq: 90").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"));
        conf.options().header(header.toString());
        conf.options().copyDefaults(true);
        conf.options().indent(2);
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
        if (titleSection == null) {
            titleSection = conf.createSection("Titles");
        }
        for (String titleKey : titleSection.getKeys(false)) {
            String titleName = conf.getString("Titles."+titleKey+".Name");
            String titleShortName = conf.getString("Titles."+titleKey+".ShortName");
            ChatColor colour = null;
            try {
                colour = ChatColor.valueOf(conf.getString("Titles."+titleKey+".ChatColour", "").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("[Jobs] - Title " + titleKey + "has an invalid ChatColour property. Skipping!");
                continue;
            }
            int levelReq = conf.getInt("Titles."+titleKey+".levelReq", -1);
            
            if (titleName == null) {
                plugin.getLogger().severe("[Jobs] - Title " + titleKey + " has an invalid Name property. Skipping!");
                continue;
            }
            if (titleShortName == null) {
                plugin.getLogger().severe("[Jobs] - Title " + titleKey + " has an invalid ShortName property. Skipping!");
                continue;
            }
            if (colour == null) {
                plugin.getLogger().severe("[Jobs] - Title " + titleKey + " has an invalid ChatColour property. Skipping!");
                continue;
            }
            if (levelReq <= -1) {
                plugin.getLogger().severe("[Jobs] - Title " + titleKey + " has an invalid levelReq property. Skipping!");
                continue;
            }
            
            this.titles.add(new Title(titleName, titleShortName, colour, levelReq));
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	

    /**
     * Method to load the restricted areas configuration
     * 
     * loads from Jobs/restrictedAreas.yml
     */
    private void loadRestrictedAreaSettings(){
        this.restrictedAreas.clear();
        File f = new File(plugin.getDataFolder(), "restrictedAreas.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration conf = new YamlConfiguration();
        conf.options().indent(2);
        conf.options().copyDefaults(true);
        StringBuilder header = new StringBuilder();
        
        header.append("Restricted area configuration")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Configures restricted areas where you cannot get experience or money")
            .append(System.getProperty("line.separator"))
            .append("when performing a job.")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("The multiplier changes the experience/money gains in an area.")
            .append(System.getProperty("line.separator"))
            .append("A multiplier of 0.0 means no money or xp, while 0.5 means you will get half the normal money/exp")
            .append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("restrictedareas:")
            .append(System.getProperty("line.separator"))
            .append("  area1:")
            .append(System.getProperty("line.separator"))
            .append("    world: 'world'")
            .append(System.getProperty("line.separator"))
            .append("    multiplier: 0.0")
            .append(System.getProperty("line.separator"))
            .append("    point1:")
            .append(System.getProperty("line.separator"))
            .append("      x: 125")
            .append(System.getProperty("line.separator"))
            .append("      y: 0")
            .append(System.getProperty("line.separator"))
            .append("      z: 125")
            .append(System.getProperty("line.separator"))
            .append("    point2:")
            .append(System.getProperty("line.separator"))
            .append("      x: 150")
            .append(System.getProperty("line.separator"))
            .append("      y: 100")
            .append(System.getProperty("line.separator"))
            .append("      z: 150")
            .append(System.getProperty("line.separator"))
            .append("  area2:")
            .append(System.getProperty("line.separator"))
            .append("    world: 'world_nether'")
            .append(System.getProperty("line.separator"))
            .append("    multiplier: 0.0")
            .append(System.getProperty("line.separator"))
            .append("    point1:")
            .append(System.getProperty("line.separator"))
            .append("      x: -100")
            .append(System.getProperty("line.separator"))
            .append("      y: 0")
            .append(System.getProperty("line.separator"))
            .append("      z: -100")
            .append(System.getProperty("line.separator"))
            .append("    point2:")
            .append(System.getProperty("line.separator"))
            .append("      x: -150")
            .append(System.getProperty("line.separator"))
            .append("      y: 100")
            .append(System.getProperty("line.separator"))
            .append("      z: -150");
        conf.options().header(header.toString());
        try {
            conf.load(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        List<World> worlds = Bukkit.getServer().getWorlds();
        ConfigurationSection areaSection = conf.getConfigurationSection("restrictedareas");
        if (areaSection != null) {
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
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Get how often in minutes to save job information
	 * @return how often in minutes to save job information
	 */
	public int getSavePeriod(){
		return generalConfig.getInt("save-period");
	}

	/**
	 * Get the Data Access Object for the plugin
	 * @return the DAO of the plugin
	 */
	public JobsDAO getJobsDAO(){
		return dao;
	}
	
	/**
	 * Function that tells if the system is set to broadcast on skill up
	 * @return true - broadcast on skill up
	 * @return false - do not broadcast on skill up
	 */
	public boolean isBroadcastingSkillups(){
		return generalConfig.getBoolean("broadcast-on-skill-up");
	}
	
	/**
     * Function that tells if the system is set to broadcast on level up
     * @return true - broadcast on level up
     * @return false - do not broadcast on level up
     */
    public boolean isBroadcastingLevelups(){
        return generalConfig.getBoolean("broadcast-on-level-up");
    }
	
	/**
	 * Function to return the title for a given level
	 * @return the correct title
	 * @return null if no title matches
	 */
	public Title getTitleForLevel(int level) {
		Title title = null;
		for (Title t: titles) {
			if (title == null) {
				if (t.getLevelReq() <= level) {
					title = t;
				}
			} else {
				if (t.getLevelReq() <= level && t.getLevelReq() > title.getLevelReq()) {
					title = t;
				}
			}
		}
		return title;
	}
	
	/**
	 * Function to return the maximum number of jobs a player can join
	 * @return
	 */
	public int getMaxJobs() {
	    return generalConfig.getInt("max-jobs");
	}
	
	/**
	 * Function to check if you get paid near a spawner is enabled
	 * @return true - you get paid
	 * @return false - you don't get paid
	 */
	public boolean payNearSpawner() {
	    return generalConfig.getBoolean("enable-pay-near-spawner");
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
