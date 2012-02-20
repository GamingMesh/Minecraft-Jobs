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

package com.zford.jobs;

import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.zford.jobs.config.JobConfig;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.MessageConfig;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.JobsPlayer;
import com.zford.jobs.listener.JobsBlockPaymentListener;
import com.zford.jobs.listener.JobsFishPaymentListener;
import com.zford.jobs.listener.JobsJobListener;
import com.zford.jobs.listener.JobsKillPaymentListener;
import com.zford.jobs.listener.JobsPlayerListener;
import com.zford.jobs.listener.JobsPluginListener;

/**
 * Jobs main class
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
 */
public class Jobs extends JavaPlugin{
	
	private HashMap<String, JobsPlayer> players = null;
	
	private static Jobs plugin = null;
	
    private MessageConfig messageConfig;

	/**
	 * Method called when you disable the plugin
	 */
	public void onDisable() {
		// kill all scheduled tasks associated to this.
		getServer().getScheduler().cancelTasks(this);

        
        for(JobsPlayer player: players.values()){
            // wipe the honorific
            player.removeHonorific();
        }
        
		// save all
		if(JobsConfiguration.getInstance().getJobsDAO() != null){
			saveAll();
			JobsConfiguration.getInstance().getJobsDAO().closeConnections();
		}
		
		getServer().getLogger().info("[" + getDescription().getFullName() + "] has been disabled succesfully.");
		// wipe the hashMap
		players.clear();
	}

	/**
	 * Method called when the plugin is enabled
	 */
	public void onEnable() {
        plugin = this;
		// load the jobConfogiration
		players = new HashMap<String, JobsPlayer>();
		JobsCommands commands = new JobsCommands(this);
		this.getCommand("jobs").setExecutor(commands);
		
		messageConfig = new MessageConfig(this);
		
		reloadConfigurations();
		
		if(!this.isEnabled())
		    return;
		
		// set the system to auto save
		if(JobsConfiguration.getInstance().getSavePeriod() > 0) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
				public void run(){
					saveAll();
				}
			}, 20*60*JobsConfiguration.getInstance().getSavePeriod(), 20*60*JobsConfiguration.getInstance().getSavePeriod());
		}
		
		// schedule payouts to buffered payments
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
		    public void run() {
		        JobsConfiguration.getInstance().getBufferedPayment().payAll();
		    }
		}, 100, 100);
		
		// enable the link for economy plugins
		getServer().getPluginManager().registerEvents(new JobsPluginListener(this), this);
		
		// register the listeners
		getServer().getPluginManager().registerEvents(new JobsBlockPaymentListener(this), this);
		getServer().getPluginManager().registerEvents(new JobsJobListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsKillPaymentListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsFishPaymentListener(this), this);
		getServer().getPluginManager().registerEvents(new JobsPlayerListener(this), this);
		
		// add all online players
		for(Player online: getServer().getOnlinePlayers()){
			addPlayer(online.getName());
		}
		
		// all loaded properly.
		getServer().getLogger().info("[" + getDescription().getFullName() + "] has been enabled succesfully.");
	}
	
	/**
	 * Add a player to the plugin to me managed.
	 * @param playername
	 */
	public void addPlayer(String playername) {
		players.put(playername, new JobsPlayer(this, playername, JobsConfiguration.getInstance().getJobsDAO()));
	}
	
	/**
	 * Remove a player from the plugin.
	 * @param playername
	 */
	public void removePlayer(String playername){
	    if(players.containsKey(playername)) {
    		save(playername);
    		players.remove(playername);
	    }
	}
	
	/**
	 * Save all the information of all of the players in the game
	 */
	public void saveAll() {
		for(String playername : players.keySet()){
			save(playername);
		}
	}
	
	/**
	 * Save the information for the specific player
	 * @param player - the player who's data is getting saved
	 */
	private void save(String playername) {
	    JobsPlayer player = players.get(playername);
		JobsConfiguration.getInstance().getJobsDAO().save(player);
	}
	
	/**
	 * Get the player job info for specific player
	 * @param player - the player who's job you're getting
	 * @return the player job info of the player
	 */
	public JobsPlayer getJobsPlayer(String playername) {
		JobsPlayer player = players.get(playername);
		if(player != null)
		    return player;
		return new JobsPlayer(this, playername, JobsConfiguration.getInstance().getJobsDAO());
	}
	
	/**
	 * Disable the plugin
	 */
	public static void disablePlugin(){
		if(plugin != null){
		    plugin.setEnabled(false);
		}
	}
	
	/**
	 * Get the message configuration data
	 * @return - the message configuration
	 */
	public MessageConfig getMessageConfig() {
	    return messageConfig;
	}
	
	/**
	 * Reloads all configuration files
	 */
	public void reloadConfigurations() {
	    getMessageConfig().reload();
	    JobsConfiguration.getInstance().reload();
	    JobConfig.getInstance().reload();
	}
	
    /**
     * Check permissions
     */
    public boolean hasPermission(Player player, String node) {
        return player.hasPermission(node);
    }
    
    /**
     * Check World permissions
     */
    public boolean hasWorldPermission(Player player, World world) {
        if (player.hasPermission("jobs.world.*")) {
            return true;
        } else {
            return player.hasPermission("jobs.world."+world.getName().toLowerCase());
        }
    }
    
    /**
     * Check Job joining permission
     */
    public boolean hasJobPermission(Player player, Job job) {
        if (player.hasPermission("jobs.join.*")) {
            return true;
        } else {
            return player.hasPermission("jobs.join."+job.getName().toLowerCase());
        }
    }
}
