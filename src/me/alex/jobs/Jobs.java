package me.alex.jobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import me.alex.jobs.config.JobsConfiguration;
import me.alex.jobs.config.container.Job;
import me.alex.jobs.config.container.JobProgression;
import me.alex.jobs.config.container.PlayerJobInfo;
import me.alex.jobs.economy.JobsBOSEconomyLink;
import me.alex.jobs.economy.JobsiConomy4Link;
import me.alex.jobs.economy.JobsiConomyLink;
import me.alex.jobs.event.JobsJoinEvent;
import me.alex.jobs.event.JobsLeaveEvent;
import me.alex.jobs.listener.JobsBlockPaymentListener;
import me.alex.jobs.listener.JobsJobListener;
import me.alex.jobs.listener.JobsKillPaymentListener;
import me.alex.jobs.listener.JobsPlayerListener;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.java.JavaPlugin;

import com.iConomy.iConomy;
import com.nidefawl.Stats.Stats;
import com.nijikokun.bukkit.Permissions.Permissions;

import cosine.boseconomy.BOSEconomy;

/**
 * Jobs main class
 * @author Alex
 *
 */
public class Jobs extends JavaPlugin{
	
	private HashMap<Player, PlayerJobInfo> players = null;
	
	private static Jobs plugin = null;

	/**
	 * Method called when you disable the plugin
	 */
	public void onDisable() {
		// kill all scheduled tasks associated to this.
		getServer().getScheduler().cancelTasks(this);
		// save all
		if(JobsConfiguration.getInstance().getJobsDAO() != null){
			saveAll();
		}
		getServer().getLogger().info("[Jobs v" + getDescription().getVersion() + "] has been disabled succesfully.");
	}

	/**
	 * Method called when the plugin is enabled
	 */
	public void onEnable() {
		// load the jobConfogiration
		plugin = this;
		players = new HashMap<Player, PlayerJobInfo>();
		JobsConfiguration.getInstance();
		
		JobsBlockPaymentListener blockListener = new JobsBlockPaymentListener(this);
		JobsJobListener jobListener = new JobsJobListener(this);
		JobsKillPaymentListener killListener = new JobsKillPaymentListener(this);
		JobsPlayerListener playerListener = new JobsPlayerListener(this);
		
		// set the system to auto save
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
			public void run(){
				saveAll();
			}
		}, 20*60*JobsConfiguration.getInstance().getSavePeriod(), 20*60*JobsConfiguration.getInstance().getSavePeriod());
		
		// enable the link for economy plugins
		getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, new ServerListener() {
			
			@Override
			public void onPluginEnable(PluginEnableEvent event) {
								
				// economy plugins
				if(JobsConfiguration.getInstance().getEconomyLink() == null){
					if(getServer().getPluginManager().getPlugin("iConomy") != null || 
							getServer().getPluginManager().getPlugin("BOSEconomy") != null){
						if(getServer().getPluginManager().getPlugin("iConomy") != null){
							if(getServer().getPluginManager().getPlugin("iConomy").getDescription().getVersion().startsWith("4")){
								JobsConfiguration.getInstance().setEconomyLink(
										new JobsiConomy4Link((com.nijiko.coelho.iConomy.iConomy)getServer().getPluginManager().getPlugin("iConomy")));
			                    System.out.println("[Jobs] Successfully linked with iConomy 4.");
							}
							else{
								JobsConfiguration.getInstance().setEconomyLink(new JobsiConomyLink((iConomy)getServer().getPluginManager().getPlugin("iConomy")));
			                    System.out.println("[Jobs] Successfully linked with iConomy 5+.");
							}
						}
						else {
							JobsConfiguration.getInstance().setEconomyLink(new JobsBOSEconomyLink((BOSEconomy)getServer().getPluginManager().getPlugin("BOSEconomy")));
		                    System.out.println("[Jobs] Successfully linked with BOSEconomy.");
						}
					}
				}
				
				// stats
				if(JobsConfiguration.getInstance().getStats() == null){
					if(getServer().getPluginManager().getPlugin("Stats") != null){
						JobsConfiguration.getInstance().setStats((Stats)getServer().getPluginManager().getPlugin("Stats"));
	                    System.out.println("[Jobs] Successfully linked with Stats.");
					}
				}
				
				// permissions
				if(JobsConfiguration.getInstance().getPermissions() == null){
					if(getServer().getPluginManager().getPlugin("Permissions") != null){
						JobsConfiguration.getInstance().setPermissions((Permissions)getServer().getPluginManager().getPlugin("Permissions"));
	                    System.out.println("[Jobs] Successfully linked with Permissions.");
					}
				}
			}
			
			@Override
			public void onPluginDisable(PluginDisableEvent event) {
				if(event.getPlugin().getDescription().getName().equalsIgnoreCase("iConomy") || 
						event.getPlugin().getDescription().getName().equalsIgnoreCase("BOSEconomy")){
					JobsConfiguration.getInstance().setEconomyLink(null);
                    System.out.println("[Jobs] Economy system successfully unlinked.");
				}
				
				// stats
				if(event.getPlugin().getDescription().getName().equalsIgnoreCase("Stats")){
					JobsConfiguration.getInstance().setStats(null);
                    System.out.println("[Jobs] Successfully unlinked with Stats.");
				}
				
				// permissions
				if(event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")){
					JobsConfiguration.getInstance().setPermissions(null);
                    System.out.println("[Jobs] Successfully unlinked with Permissions.");
				}
			}
		}, Event.Priority.Monitor, this);
		
		// register the listeners
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, jobListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, killListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
		
		// all loaded properly.
		getServer().getLogger().info("[Jobs v" + getDescription().getVersion() + "] has been enabled succesfully.");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(label.equalsIgnoreCase("jobs")){
			if(sender instanceof Player){
				// player only commands
				// join
				if(args.length >= 2 && args[0].equalsIgnoreCase("join")){
					String jobName = "";
					for(int i=1; i< args.length; ++i){
						jobName += args[i] + " ";
					}
					jobName = jobName.trim();
					if(JobsConfiguration.getInstance().getJob(jobName) != null){
						getServer().getPluginManager().callEvent(new JobsJoinEvent(
								(Player)sender, JobsConfiguration.getInstance().getJob(jobName)));
					}
				}
				// leave
				else if(args.length >= 2 && args[0].equalsIgnoreCase("leave")){
					String jobName = "";
					for(int i=1; i< args.length; ++i){
						jobName += args[i] + " ";
					}
					jobName = jobName.trim();
					if(JobsConfiguration.getInstance().getJob(jobName) != null){
						getServer().getPluginManager().callEvent(new JobsLeaveEvent(
								(Player)sender, JobsConfiguration.getInstance().getJob(jobName)));
					}
				}
				// stats
				else if(args.length == 1 && args[0].equalsIgnoreCase("stats")){
					if(getJob((Player)sender).getJobsProgression().size() == 0){
						sender.sendMessage(ChatColor.RED + "Please join a job first");
					}
					for(JobProgression temp: getJob((Player)sender).getJobsProgression()){
						DecimalFormat format = new DecimalFormat("#.##");
						sender.sendMessage(temp.getJob().getJobChatColour() + temp.getJob().getJobName() + ":");
						sender.sendMessage("    Level: " + temp.getLevel());
						sender.sendMessage("    Experience: " + format.format(temp.getExperience()) + " / " + format.format(temp.getMaxExperience()));
					}
				}
				// payment
				else if(args.length == 2 && args[0].equalsIgnoreCase("income")){
					if(getJob((Player)sender).getJobsProgression().size() == 0){
						sender.sendMessage(ChatColor.RED + "Please join a job first");
					}
					if(args[1].equalsIgnoreCase("break")){
						
					}
					else if(args[1].equalsIgnoreCase("place")){
						
					}
					else if(args[1].equalsIgnoreCase("kill")){
						
					}
					else if(args[1].equalsIgnoreCase("custom-kill")){
						
					}
					
					for(JobProgression temp: getJob((Player)sender).getJobsProgression()){
						DecimalFormat format = new DecimalFormat("#.##");
						sender.sendMessage(temp.getJob().getJobChatColour() + temp.getJob().getJobName() + ":");
						sender.sendMessage("    Level: " + temp.getLevel());
						sender.sendMessage("    Experience: " + format.format(temp.getExperience()) + " / " + format.format(temp.getMaxExperience()));
					}
				}
				// experience
				// browse
				else if(args.length == 1 && args[0].equalsIgnoreCase("browse")){
					ArrayList<String> jobs = new ArrayList<String>();
					for(Job temp: JobsConfiguration.getInstance().getJobs()){
						jobs.add(temp.getJobChatColour() + temp.getJobName());
					}
					if(jobs.size() == 0){
						sender.sendMessage("There are no jobs you can join");
					}
					else{
						sender.sendMessage("You are allowed to join the following jobs:");
						for(String temp: jobs){
							sender.sendMessage("    " + temp);
						}
						sender.sendMessage("For more information type in /jobs info [JobName]");
					}
				}
			}
			return true;
		}
		return true;
	}
	
	/**
	 * Add a player to the plugin to me managed.
	 * @param player
	 */
	public void addPlayer(Player player){
		players.put(player, new PlayerJobInfo(player, JobsConfiguration.getInstance().getJobsDAO()));
	}
	
	/**
	 * Remove a player from the plugin.
	 * @param player
	 */
	public void removePlayer(Player player){
		save(player);
		players.remove(player);
	}
	
	/**
	 * Get the playerJobInfo for the player
	 * @param player - the player you want the job info for
	 * @return the job info for the player
	 */
	public PlayerJobInfo getJob(Player player){
		return players.get(player);
	}
	
	/**
	 * Save all the information of all of the players in the game
	 */
	public void saveAll(){
		for(Player player: players.keySet()){
			save(player);
		}
	}
	
	/**
	 * Save the information for the specific player
	 * @param player - the player who's data is getting saved
	 */
	private void save(Player player){
		if(player != null){
			JobsConfiguration.getInstance().getJobsDAO().save(players.get(player));
		}
	}
	
	/**
	 * Get the player job info for specific player
	 * @param player - the player who's job you're getting
	 * @return the player job info of the player
	 */
	public PlayerJobInfo getPlayerJobInfo(Player player){
		return players.get(player);
	}
	
	/**
	 * Get the current plugin
	 * @return a refference to the plugin
	 */
	private static Jobs getPlugin(){
		return plugin;
	}
	
	/**
	 * Disable the plugin
	 */
	public static void disablePlugin(){
		if(Jobs.getPlugin() != null){
			Jobs.getPlugin().getServer().getPluginManager().disablePlugin(Jobs.getPlugin());
		}
	}
	
	/**
	 * Get the server
	 * @return the server
	 */
	public static Server getJobsServer(){
		if(plugin != null){
			return plugin.getServer();
		}
		return null;
	}

}
