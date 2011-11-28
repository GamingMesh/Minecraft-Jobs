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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nidefawl.Stats.Stats;
import com.nijikokun.bukkit.Permissions.Permissions;
import com.zford.jobs.config.JobConfig;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.MessageConfig;
import com.zford.jobs.config.container.JobsPlayer;
import com.zford.jobs.economy.JobsBOSEconomyLink;
import com.zford.jobs.economy.JobsEssentialsLink;
import com.zford.jobs.economy.JobsiConomy5Link;
import com.zford.jobs.economy.JobsiConomy6Link;
import com.zford.jobs.listener.JobsBlockPaymentListener;
import com.zford.jobs.listener.JobsCraftPaymentListener;
import com.zford.jobs.listener.JobsFishPaymentListener;
import com.zford.jobs.listener.JobsJobListener;
import com.zford.jobs.listener.JobsKillPaymentListener;
import com.zford.jobs.listener.JobsPlayerListener;

import cosine.boseconomy.BOSEconomy;

import com.earth2me.essentials.Essentials;

/**
 * Jobs main class
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
 */
public class Jobs extends JavaPlugin{
	
	private HashMap<String, JobsPlayer> players = null;
	
	private static Jobs plugin = null;
	
    private JobsBlockPaymentListener blockListener;
    private JobsJobListener jobListener;
    private JobsKillPaymentListener killListener;
    private JobsPlayerListener playerListener;
    private JobsFishPaymentListener fishListener;
    private JobsCraftPaymentListener craftListener;
    private MessageConfig messageConfig;
	
    public Jobs() {
        blockListener = new JobsBlockPaymentListener(this);
        jobListener = new JobsJobListener(this);
        killListener = new JobsKillPaymentListener(this);
        playerListener = new JobsPlayerListener(this);
        fishListener = new JobsFishPaymentListener(this);
        plugin = this;
    }

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
		
		getServer().getLogger().info("[Jobs v" + getDescription().getVersion() + "] has been disabled succesfully.");
		// wipe the hashMap
		players.clear();
	}

	/**
	 * Method called when the plugin is enabled
	 */
	public void onEnable() {
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
		
		// enable the link for economy plugins
		getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, new ServerListener() {
		    
			@Override
			public void onPluginEnable(PluginEnableEvent event) {
                JobsConfiguration jc = JobsConfiguration.getInstance();
                PluginManager pm = getServer().getPluginManager();
                
				// economy plugins
				if(jc.getEconomyLink() == null){
					if(pm.getPlugin("iConomy") != null || 
							pm.getPlugin("BOSEconomy") != null ||
							pm.getPlugin("Essentials") != null) {
						if(pm.getPlugin("iConomy") != null && 
						        (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("iconomy")) &&
						        pm.getPlugin("iConomy").getDescription().getVersion().startsWith("5")) {
							jc.setEconomyLink(new JobsiConomy5Link((com.iConomy.iConomy)pm.getPlugin("iConomy")));
                            System.out.println("[Jobs] Successfully linked with iConomy 5.");
						} else if(pm.getPlugin("iConomy") != null && 
                                (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("iconomy")) &&
                                pm.getPlugin("iConomy").getDescription().getVersion().startsWith("6")) {
                            jc.setEconomyLink(new JobsiConomy6Link((com.iCo6.iConomy)pm.getPlugin("iConomy")));
                            System.out.println("[Jobs] Successfully linked with iConomy 6.");
                        } else if(pm.getPlugin("BOSEconomy") != null &&
						        (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("boseconomy"))) {
							jc.setEconomyLink(new JobsBOSEconomyLink((BOSEconomy)pm.getPlugin("BOSEconomy")));
							System.out.println("[Jobs] Successfully linked with BOSEconomy.");
						} else if(pm.getPlugin("Essentials") != null &&
                                (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("essentials"))) {
                            jc.setEconomyLink(new JobsEssentialsLink((Essentials)pm.getPlugin("Essentials")));
                            System.out.println("[Jobs] Successfully linked with Essentials.");
                        }
					}
				}
				
				// stats
				if(jc.getStats() == null && jc.isStatsEnabled()){
					if(pm.getPlugin("Stats") != null){
						jc.setStats((Stats)pm.getPlugin("Stats"));
	                    System.out.println("[Jobs] Successfully linked with Stats.");
					}
				}
				
				// spout
				if(craftListener == null){
				    if(getServer().getPluginManager().getPlugin("Spout") != null){
				        craftListener = new JobsCraftPaymentListener(plugin);
			            getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, craftListener, Event.Priority.Monitor, plugin);
                        System.out.println("[Jobs] Successfully linked with Spout.");
				    }
				}
			}
			
			@Override
			public void onPluginDisable(PluginDisableEvent event) {
                JobsConfiguration jc = JobsConfiguration.getInstance();
			    if(jc.getEconomyLink() instanceof JobsiConomy5Link && event.getPlugin().getDescription().getName().equalsIgnoreCase("iConomy") ||
                        jc.getEconomyLink() instanceof JobsBOSEconomyLink && event.getPlugin().getDescription().getName().equalsIgnoreCase("BOSEconomy") ||
                        jc.getEconomyLink() instanceof JobsEssentialsLink && event.getPlugin().getDescription().getName().equalsIgnoreCase("Essentials")
			            ) {
					jc.setEconomyLink(null);
                    System.out.println("[Jobs] Economy system successfully unlinked.");
				}
				
				// stats
				if(event.getPlugin().getDescription().getName().equalsIgnoreCase("Stats")){
					jc.setStats(null);
                    System.out.println("[Jobs] Successfully unlinked with Stats.");
				}
			}
		}, Event.Priority.Monitor, this);
		
		// register the listeners
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, jobListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, killListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.CREATURE_SPAWN, killListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_FISH, fishListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
		
		// add all online players
		for(Player online: getServer().getOnlinePlayers()){
			addPlayer(online.getName());
		}
		
		// all loaded properly.
		getServer().getLogger().info("[Jobs v" + getDescription().getVersion() + "] has been enabled succesfully.");
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
		save(playername);
		players.remove(playername);
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
	    Plugin plugin = getServer().getPluginManager().getPlugin("Permissions");
	    if (plugin == null || !plugin.isEnabled()) {
	        if (node.toLowerCase().startsWith("jobs.admin"))
	            return player.isOp();
	        else
	            return true;
	    }
	    Permissions permissions = (Permissions) plugin;
	    return permissions.getHandler().has(player, node);
	}
}
