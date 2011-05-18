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
		
		if(isEnabled()){
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
				                    System.err.println("[Jobs] Successfully linked with iConomy 4.");
								}
								else{
									JobsConfiguration.getInstance().setEconomyLink(new JobsiConomyLink((iConomy)getServer().getPluginManager().getPlugin("iConomy")));
				                    System.err.println("[Jobs] Successfully linked with iConomy 5+.");
								}
							}
							else {
								JobsConfiguration.getInstance().setEconomyLink(new JobsBOSEconomyLink((BOSEconomy)getServer().getPluginManager().getPlugin("BOSEconomy")));
			                    System.err.println("[Jobs] Successfully linked with BOSEconomy.");
							}
						}
					}
					
					// stats
					if(JobsConfiguration.getInstance().getStats() == null){
						if(getServer().getPluginManager().getPlugin("Stats") != null){
							JobsConfiguration.getInstance().setStats((Stats)getServer().getPluginManager().getPlugin("Stats"));
		                    System.err.println("[Jobs] Successfully linked with Stats.");
						}
					}
					
					// permissions
					if(JobsConfiguration.getInstance().getPermissions() == null){
						if(getServer().getPluginManager().getPlugin("Permissions") != null){
							JobsConfiguration.getInstance().setPermissions((Permissions)getServer().getPluginManager().getPlugin("Permissions"));
		                    System.err.println("[Jobs] Successfully linked with Permissions.");
						}
					}
				}
				
				@Override
				public void onPluginDisable(PluginDisableEvent event) {
					if(event.getPlugin().getDescription().getName().equalsIgnoreCase("iConomy") || 
							event.getPlugin().getDescription().getName().equalsIgnoreCase("BOSEconomy")){
						JobsConfiguration.getInstance().setEconomyLink(null);
	                    System.err.println("[Jobs] Economy system successfully unlinked.");
					}
					
					// stats
					if(event.getPlugin().getDescription().getName().equalsIgnoreCase("Stats")){
						JobsConfiguration.getInstance().setStats(null);
	                    System.err.println("[Jobs] Successfully unlinked with Stats.");
					}
					
					// permissions
					if(event.getPlugin().getDescription().getName().equalsIgnoreCase("Permissions")){
						JobsConfiguration.getInstance().setPermissions(null);
	                    System.err.println("[Jobs] Successfully unlinked with Permissions.");
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
						if((JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.join."+jobName))
								||
								((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled()))){
							getServer().getPluginManager().callEvent(new JobsJoinEvent(
									(Player)sender, JobsConfiguration.getInstance().getJob(jobName)));
						}
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
						sender.sendMessage(temp.getJob().getChatColour() + temp.getJob().getName() + ":");
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
					
					for(JobProgression temp: getJob((Player)sender).getJobsProgression()){
						DecimalFormat format = new DecimalFormat("#.##");
						sender.sendMessage(temp.getJob().getChatColour() + temp.getJob().getName() + ":");
						sender.sendMessage("    Level: " + temp.getLevel());
						sender.sendMessage("    Experience: " + format.format(temp.getExperience()) + " / " + format.format(temp.getMaxExperience()));
					}
				}
				// experience
				// browse
				else if(args.length == 1 && args[0].equalsIgnoreCase("browse")){
					ArrayList<String> jobs = new ArrayList<String>();
					for(Job temp: JobsConfiguration.getInstance().getJobs()){
						if((JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.join."+temp.getName()))
								||
								((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled()))){
							if(temp.getMaxLevel() == null){
								jobs.add(temp.getChatColour() + temp.getName());
							}
							else{
								jobs.add(temp.getChatColour() + temp.getName() + ChatColor.WHITE + " - max level: " + temp.getMaxLevel());
							}
						}
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
			
			// admin commands
			if(args.length == 3){
				if(args[0].equalsIgnoreCase("fire")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.fire"))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);
						Job job = JobsConfiguration.getInstance().getJob(args[2]);
						if(target != null && job != null){
							try{
								getServer().getPluginManager().callEvent(new JobsLeaveEvent(target, job));
								target.sendMessage("You have been fired from " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("employ")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.employ."+args[2]))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);
						Job job = JobsConfiguration.getInstance().getJob(args[2]);
						if(target != null && job != null){
							try{
								getServer().getPluginManager().callEvent(new JobsJoinEvent(target, job));
								target.sendMessage("You have been employed in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
					}
				}
			}
			else if(args.length == 4){
				if(args[0].equalsIgnoreCase("promote")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.promote"))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);
						Job job = JobsConfiguration.getInstance().getJob(args[2]);
						if(target != null && job != null){
							try{
								Integer levelsGained = Integer.parseInt(args[3]);
								if (players.get(target).getJobsProgression(job).getJob().getMaxLevel() != null &&
										levelsGained + players.get(target).getJobsProgression(job).getLevel() > players.get(target).getJobsProgression(job).getJob().getMaxLevel()){
									levelsGained = players.get(target).getJobsProgression(job).getJob().getMaxLevel() - players.get(target).getJobsProgression(job).getLevel();
								}
								players.get(target).getJobsProgression(job).setLevel(players.get(target).getJobsProgression(job).getLevel() + levelsGained);
								players.get(target).checkLevels();
								target.sendMessage("You have been promoted " + levelsGained + " levels in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("demote")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.demote"))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);	
						Job job = JobsConfiguration.getInstance().getJob(args[2]);
						if(target != null && job != null){
							try{
								Integer levelsLost = Integer.parseInt(args[3]);
								if (players.get(target).getJobsProgression(job).getLevel() - levelsLost < 1){
									levelsLost = players.get(target).getJobsProgression(job).getLevel() - 1;
								}
								players.get(target).getJobsProgression(job).setLevel(players.get(target).getJobsProgression(job).getLevel() - levelsLost);
								players.get(target).reloadMaxExperience();
								players.get(target).checkLevels();
								target.sendMessage("You have been demoted " + levelsLost + " levels in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("grantxp")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.grantxp"))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);
						Job job = JobsConfiguration.getInstance().getJob(args[2]);
						if(target != null && job != null){
							try{
								players.get(target).getJobsProgression(job).setExperience(players.get(target).getJobsProgression(job).getExperience() + Double.parseDouble(args[3]));
								players.get(target).reloadMaxExperience();
								players.get(target).checkLevels();
								target.sendMessage("You have been granted " + args[3] + " experience in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (ClassCastException ex){
								players.get(target).getJobsProgression(job).setExperience(players.get(target).getJobsProgression(job).getExperience() + (double)Integer.parseInt(args[3]));
								players.get(target).reloadMaxExperience();
								players.get(target).checkLevels();
								target.sendMessage("You have been granted " + args[3] + " experience in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("removexp")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.removexp"))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);
						Job job = JobsConfiguration.getInstance().getJob(args[2]);
						if(target != null && job != null){
							try{
								players.get(target).getJobsProgression(job).setExperience(players.get(target).getJobsProgression(job).getExperience() - Double.parseDouble(args[3]));
								players.get(target).checkLevels();
								target.sendMessage("You have been removec " + args[3] + " experience in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (ClassCastException ex){
								players.get(target).getJobsProgression(job).setExperience(players.get(target).getJobsProgression(job).getExperience() - (double)Integer.parseInt(args[3]));
								players.get(target).checkLevels();
								target.sendMessage("You have been removed " + args[3] + " experience in " + job.getName());
								sender.sendMessage("Your command has been performed.");
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
					}
				}
				else if(args[0].equalsIgnoreCase("transfer")){
					if((JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.transfer"))
							||
							(((JobsConfiguration.getInstance().getPermissions()!= null) || (JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
						Player target = getServer().getPlayer(args[1]);
						Job oldjob = JobsConfiguration.getInstance().getJob(args[2]);
						Job newjob = JobsConfiguration.getInstance().getJob(args[3]);
						if(target != null && oldjob != null & newjob != null){
							try{
								PlayerJobInfo info = players.get(target);
								if(info.isInJob(oldjob) && !info.isInJob(newjob)){
									info.transferJob(oldjob, newjob);
									if(newjob.getMaxLevel() != null && info.getJobsProgression(newjob).getLevel() > newjob.getMaxLevel()){
										info.getJobsProgression(newjob).setLevel(newjob.getMaxLevel());
									}
									players.get(target).reloadMaxExperience();
									players.get(target).reloadHonorific();
									players.get(target).checkLevels();
									save(target);
									target.sendMessage("You have been transferred from " + oldjob.getName() + " to " + newjob.getName());
									sender.sendMessage("Your command has been performed.");
									// stats plugin integration
									if(JobsConfiguration.getInstance().getStats() != null &&
											JobsConfiguration.getInstance().getStats().isEnabled()){
										Stats stats = JobsConfiguration.getInstance().getStats();
										if(info.getJobsProgression(newjob).getLevel() > stats.get(target.getName(), "job", newjob.getName())){
											stats.setStat(target.getName(), "job", newjob.getName(), info.getJobsProgression(newjob).getLevel());
											stats.saveAll();
										}
									}
								}
							}
							catch (Exception e){
								sender.sendMessage(ChatColor.RED + "There was an error in the command");
							}
						}
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
