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

package me.zford.jobs;

import java.util.HashMap;

import me.zford.jobs.config.JobConfig;
import me.zford.jobs.config.JobsConfiguration;
import me.zford.jobs.config.MessageConfig;
import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobsPlayer;
import me.zford.jobs.economy.BufferedPayment;
import me.zford.jobs.economy.link.VaultLink;
import me.zford.jobs.listener.JobsBlockPaymentListener;
import me.zford.jobs.listener.JobsCraftPaymentListener;
import me.zford.jobs.listener.JobsFishPaymentListener;
import me.zford.jobs.listener.JobsJobListener;
import me.zford.jobs.listener.JobsKillPaymentListener;
import me.zford.jobs.listener.JobsPlayerListener;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Jobs main class
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
 */
public class Jobs extends JavaPlugin{
	
	private HashMap<String, JobsPlayer> players = null;
	
    private MessageConfig messageConfig;
    
    private JobsConfiguration jobsConfiguration;
    private JobConfig jobConfig;
    private BufferedPayment economy;

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
		if(getJobsConfiguration().getJobsDAO() != null){
			saveAll();
			getJobsConfiguration().getJobsDAO().closeConnections();
		}
		
		getServer().getLogger().info("["+getDescription().getName()+"] has been disabled succesfully.");
		// wipe the hashMap
		players.clear();
	}

	/**
	 * Method called when the plugin is enabled
	 */
	public void onEnable() {
	    jobsConfiguration = new JobsConfiguration(this);
	    jobConfig = new JobConfig(this);
	    
		players = new HashMap<String, JobsPlayer>();
		JobsCommands commands = new JobsCommands(this);
		this.getCommand("jobs").setExecutor(commands);
		
		messageConfig = new MessageConfig(this);
		
		reloadConfigurations();
		
		if(!this.isEnabled())
		    return;
		
		if (!loadVault()) {
		    getLogger().severe("["+getDescription().getName()+"] Could not load Vault!");
		    setEnabled(false);
		    return;
		}
		
		// set the system to auto save
		if(getJobsConfiguration().getSavePeriod() > 0) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
				public void run(){
					saveAll();
				}
			}, 20*60*getJobsConfiguration().getSavePeriod(), 20*60*getJobsConfiguration().getSavePeriod());
		}
		
		// schedule payouts to buffered payments
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
		    public void run() {
		        economy.payAll();
		    }
		}, 100, 100);
		
		// register the listeners
		getServer().getPluginManager().registerEvents(new JobsBlockPaymentListener(this), this);
		getServer().getPluginManager().registerEvents(new JobsJobListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsKillPaymentListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsFishPaymentListener(this), this);
		getServer().getPluginManager().registerEvents(new JobsPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsCraftPaymentListener(this), this);

		
		// add all online players
		for(Player online: getServer().getOnlinePlayers()){
			addPlayer(online.getName());
		}
		
		// register permissions
		reRegisterPermissions();
		
		// all loaded properly.
		getServer().getLogger().info("["+getDescription().getName()+"] has been enabled succesfully.");
	}
	
	/**
	 * Loads vault and sets as default economy
	 */
	private boolean loadVault() {
	    Plugin test = getServer().getPluginManager().getPlugin("Vault");
	    if (test == null)
	        return false;
	    
	    VaultLink link = new VaultLink(this);
	    economy = new BufferedPayment(link);
        
        getLogger().info("["+getDescription().getName()+"] Successfully linked with Vault.");
	    return true;
	}
	/**
	 * Retrieves the economy hook
	 * @return - buffered payment hook
	 */
	public BufferedPayment getEconomy() {
	    return economy;
	}
	
	/**
	 * Add a player to the plugin to me managed.
	 * @param playername
	 */
	public void addPlayer(String playername) {
		players.put(playername, new JobsPlayer(this, playername, getJobsConfiguration().getJobsDAO()));
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
	    getJobsConfiguration().getJobsDAO().save(player);
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
		return new JobsPlayer(this, playername, getJobsConfiguration().getJobsDAO());
	}
	
	/**
	 * Disable the plugin
	 */
	public void disablePlugin(){
	    setEnabled(false);
	}
    
    /**
     * Returns the jobs configuration
     */
    public JobsConfiguration getJobsConfiguration() {
        return jobsConfiguration;
    }
    
    /**
     * Returns the job config
     */
    public JobConfig getJobConfig() {
        return jobConfig;
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
	    jobsConfiguration.reload();
	    getJobConfig().reload();
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
        if (!player.hasPermission("jobs.use")) {
            return false;
        } else {
            return player.hasPermission("jobs.world."+world.getName().toLowerCase());
        }
    }
    
    /**
     * Check Job joining permission
     */
    public boolean hasJobPermission(Player player, Job job) {
        if (!player.hasPermission("jobs.use")) {
            return false;
        } else {
            return player.hasPermission("jobs.join."+job.getName().toLowerCase());
        }
    }
    
    public void reRegisterPermissions() {
        PluginManager pm = getServer().getPluginManager();
        for (World world : getServer().getWorlds()) {
            if (pm.getPermission("jobs.world."+world.getName().toLowerCase()) == null)
                pm.addPermission(new Permission("jobs.world."+world.getName().toLowerCase(), PermissionDefault.TRUE));
        }
        for (Job job : getJobConfig().getJobs()) {
            if (pm.getPermission("jobs.join."+job.getName().toLowerCase()) == null)
                pm.addPermission(new Permission("jobs.join."+job.getName().toLowerCase(), PermissionDefault.TRUE));
        }
    }
}
