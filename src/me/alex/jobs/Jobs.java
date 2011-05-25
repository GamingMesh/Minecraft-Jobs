package me.alex.jobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.alex.jobs.config.JobsConfiguration;
import me.alex.jobs.config.container.Job;
import me.alex.jobs.config.container.JobProgression;
import me.alex.jobs.config.container.JobsBlockInfo;
import me.alex.jobs.config.container.JobsLivingEntityInfo;
import me.alex.jobs.config.container.PlayerJobInfo;
import me.alex.jobs.economy.JobsBOSEconomyLink;
import me.alex.jobs.economy.JobsiConomy4Link;
import me.alex.jobs.economy.JobsiConomyLink;
import me.alex.jobs.event.JobsJoinEvent;
import me.alex.jobs.event.JobsLeaveEvent;
import me.alex.jobs.fake.JobsPlayer;
import me.alex.jobs.listener.JobsBlockPaymentListener;
import me.alex.jobs.listener.JobsJobListener;
import me.alex.jobs.listener.JobsKillPaymentListener;
import me.alex.jobs.listener.JobsPlayerListener;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mbertoli.jfep.Parser;

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
		
		for(Entry<Player, PlayerJobInfo> online: players.entrySet()){
			// wipe the honorific
			online.getKey().setDisplayName(online.getKey().getDisplayName().replace(online.getValue().getDisplayHonorific(), ""));
		}
		// wipe the hashMap
		players = null;
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
			
			// add all online players
			for(Player online: getServer().getOnlinePlayers()){
				addPlayer(online);
			}
			
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
				if(args.length == 2 && args[0].equalsIgnoreCase("join")){
					String jobName = args[1].trim();
					if(JobsConfiguration.getInstance().getJob(jobName) != null){
						if((JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.join."+jobName))
								||
								((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled()))){
							if(JobsConfiguration.getInstance().getMaxJobs() == null || players.get((Player)sender).getJobs().size() < JobsConfiguration.getInstance().getMaxJobs()){
								getServer().getPluginManager().callEvent(new JobsJoinEvent(
										(Player)sender, JobsConfiguration.getInstance().getJob(jobName)));
								return true;
							}
							else{
								String tempMessage = JobsConfiguration.getInstance().getMessage("join-too-many-job");
								if(tempMessage == null){
									sender.sendMessage(ChatColor.RED + "You have already joined too many jobs.");
									return true;
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
									return true;
								}
							}
						}
						else {
							// you do not have permission to join the job
							noPermission(sender);
							return true;
						}
					}
					else{
						// job does not exist
						jobDoesNotExist(sender);
						return true;
					}
				}
				// leave
				else if(args.length >= 2 && args[0].equalsIgnoreCase("leave")){
					String jobName = args[1].trim();
					if(JobsConfiguration.getInstance().getJob(jobName) != null){
						getServer().getPluginManager().callEvent(new JobsLeaveEvent(
								(Player)sender, JobsConfiguration.getInstance().getJob(jobName)));
					}
					else{
						jobDoesNotExist(sender);
					}
					return true;
				}
				// stats
				else if(args.length == 1 && args[0].equalsIgnoreCase("stats")){
					if(getJob((Player)sender).getJobsProgression().size() == 0){
						String tempMessage = JobsConfiguration.getInstance().getMessage("stats-no-job");
						if(tempMessage == null){
							sender.sendMessage(ChatColor.RED + "Please join a job first");
						}
						else{
							for(String temp: tempMessage.split("\n")){
								sender.sendMessage(temp);
							}
						}
						return true;
					}
					else{
						for(JobProgression tempJobProg: getJob((Player)sender).getJobsProgression()){
							DecimalFormat format = new DecimalFormat("#.##");
							String tempMessage = JobsConfiguration.getInstance().getMessage("stats-job");
							if(tempMessage == null){
								sender.sendMessage("lvl" + tempJobProg.getLevel() + " " + tempJobProg.getJob().getChatColour() + tempJobProg.getJob().getName() + ":");
								sender.sendMessage("    Experience: " + format.format(tempJobProg.getExperience()) + " / " + format.format(tempJobProg.getMaxExperience()));
							}
							else {
								tempMessage = tempMessage.replace("%joblevel%", ""+tempJobProg.getLevel());
								tempMessage = tempMessage.replace("%jobcolour%", ""+tempJobProg.getJob().getChatColour());
								tempMessage = tempMessage.replace("%jobname%", ""+tempJobProg.getJob().getName());
								tempMessage = tempMessage.replace("%jobexp%", format.format(tempJobProg.getExperience()));
								tempMessage = tempMessage.replace("%jobmaxexp%", format.format(tempJobProg.getMaxExperience()));
								for(String temp: tempMessage.split("\n")){
									sender.sendMessage(temp);
								}
							}
						}
						return true;
					}
				}
				// jobs info <jobname> <break, place, kill>
				else if(args.length == 3 && args[0].equalsIgnoreCase("info")){
					Job job = JobsConfiguration.getInstance().getJob(args[1]);
					if(job == null){
						// job doesn't exist
						jobDoesNotExist(sender);
						return true;
					}
					else{
						// break
						if(args[2].equalsIgnoreCase("break")){
							HashMap<String, JobsBlockInfo> jobBreakInfo = job.getBreakInfo();
							
							if(jobBreakInfo != null){
								String tempMessage = JobsConfiguration.getInstance().getMessage("break-header");
								if(tempMessage == null){
									sender.sendMessage("Break:");
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
								}
								DecimalFormat format = new DecimalFormat("#.##");
								JobProgression prog = players.get((Player)sender).getJobsProgression(job);
								Parser expEquation = job.getExpEquation();
								Parser incomeEquation = job.getIncomeEquation();
								if(prog != null){
									expEquation.setVariable("joblevel", prog.getLevel());
									incomeEquation.setVariable("joblevel", prog.getLevel());
								}
								else {
									expEquation.setVariable("joblevel", 1);
									incomeEquation.setVariable("joblevel", 1);
								}
								expEquation.setVariable("numjobs", players.get((Player)sender).getJobs().size());
								incomeEquation.setVariable("numjobs", players.get((Player)sender).getJobs().size());
								for(Entry<String, JobsBlockInfo> temp: jobBreakInfo.entrySet()){
									expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
									incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
									if(temp.getKey().contains(":")){
										tempMessage = JobsConfiguration.getInstance().getMessage("break-info-sub");
									}
									else {
										tempMessage = JobsConfiguration.getInstance().getMessage("break-info-no-sub");
									}
									if(tempMessage == null){
										sender.sendMessage("    " + temp.getKey().replace("_", " ").toLowerCase() + " : " + 
												format.format(incomeEquation.getValue()) + ChatColor.GREEN + " income" + ChatColor.WHITE + ", " + 
												format.format(expEquation.getValue()) + ChatColor.YELLOW + " exp");
									}
									else{
										if(temp.getKey().contains(":")){
											tempMessage = tempMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
											tempMessage = tempMessage.replace("%subitem$", temp.getKey().split(":")[1]);
										}
										else{
											tempMessage = tempMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
										}
										tempMessage = tempMessage.replace("%income%", format.format(incomeEquation.getValue()));
										tempMessage = tempMessage.replace("%experience%", format.format(expEquation.getValue()));
										for(String tempMsg: tempMessage.split("\n")){
											sender.sendMessage(tempMsg);
										}
									}
								}
							}
							else {
								String tempMessage = JobsConfiguration.getInstance().getMessage("break-none");
								if(tempMessage == null){
									sender.sendMessage(job.getChatColour()+job.getName()+ChatColor.WHITE+ " does not get money from breaking anything.");
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("place")){
							// place
							HashMap<String, JobsBlockInfo> jobPlaceInfo = job.getPlaceInfo();
							
							if(jobPlaceInfo != null){
								String tempMessage = JobsConfiguration.getInstance().getMessage("place-header");
								if(tempMessage == null){
									sender.sendMessage("Place:");
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
								}
								DecimalFormat format = new DecimalFormat("#.##");
								JobProgression prog = players.get((Player)sender).getJobsProgression(job);
								Parser expEquation = job.getExpEquation();
								Parser incomeEquation = job.getIncomeEquation();
								if(prog != null){
									expEquation.setVariable("joblevel", prog.getLevel());
									incomeEquation.setVariable("joblevel", prog.getLevel());
								}
								else {
									expEquation.setVariable("joblevel", 1);
									incomeEquation.setVariable("joblevel", 1);
								}
								expEquation.setVariable("numjobs", players.get((Player)sender).getJobs().size());
								incomeEquation.setVariable("numjobs", players.get((Player)sender).getJobs().size());
								for(Entry<String, JobsBlockInfo> temp: jobPlaceInfo.entrySet()){
									expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
									incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
									if(temp.getKey().contains(":")){
										tempMessage = JobsConfiguration.getInstance().getMessage("place-info-sub");
									}
									else {
										tempMessage = JobsConfiguration.getInstance().getMessage("place-info-no-sub");
									}
									if(tempMessage == null){
										sender.sendMessage("    " + temp.getKey().replace("_", " ").toLowerCase() + " - " + 
												format.format(incomeEquation.getValue()) + ChatColor.GREEN + " income" + ChatColor.WHITE + ", " + 
												format.format(expEquation.getValue()) + ChatColor.YELLOW + " exp");
									}
									else{
										if(temp.getKey().contains(":")){
											tempMessage = tempMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
											tempMessage = tempMessage.replace("%subitem$", temp.getKey().split(":")[1]);
										}
										else{
											tempMessage = tempMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
										}
										tempMessage = tempMessage.replace("%income%", format.format(incomeEquation.getValue()));
										tempMessage = tempMessage.replace("%experience%", format.format(expEquation.getValue()));
										for(String tempMsg: tempMessage.split("\n")){
											sender.sendMessage(tempMsg);
										}
									}
								}
							}
							else {
								String tempMessage = JobsConfiguration.getInstance().getMessage("break-none");
								if(tempMessage == null){
									sender.sendMessage(job.getChatColour()+job.getName()+ChatColor.WHITE+ " does not get money from placing anything.");
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
								}
							}
							return true;
						}
						else if(args[2].equalsIgnoreCase("kill")){
							// kill
							HashMap<String, JobsLivingEntityInfo> jobKillInfo = job.getKillInfo();
							
							if(jobKillInfo != null){
								String tempMessage = JobsConfiguration.getInstance().getMessage("kill-header");
								if(tempMessage == null){
									sender.sendMessage("Kill:");
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
								}
								DecimalFormat format = new DecimalFormat("#.##");
								JobProgression prog = players.get((Player)sender).getJobsProgression(job);
								Parser expEquation = job.getExpEquation();
								Parser incomeEquation = job.getIncomeEquation();
								if(prog != null){
									expEquation.setVariable("joblevel", prog.getLevel());
									incomeEquation.setVariable("joblevel", prog.getLevel());
								}
								else {
									expEquation.setVariable("joblevel", 1);
									incomeEquation.setVariable("joblevel", 1);
								}
								expEquation.setVariable("numjobs", players.get((Player)sender).getJobs().size());
								incomeEquation.setVariable("numjobs", players.get((Player)sender).getJobs().size());
								for(Entry<String, JobsLivingEntityInfo> temp: jobKillInfo.entrySet()){
									expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
									incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
									if(temp.getKey().contains(":")){
										tempMessage = JobsConfiguration.getInstance().getMessage("kill-info-sub");
									}
									else {
										tempMessage = JobsConfiguration.getInstance().getMessage("kill-info-no-sub");
									}
									if(tempMessage == null){
										sender.sendMessage("    " + temp.getKey().replace("org.bukkit.craftbukkit.entity.Craft", "") + " - " + 
												format.format(incomeEquation.getValue()) + ChatColor.GREEN + " income" + ChatColor.WHITE + ", " + 
												format.format(expEquation.getValue()) + ChatColor.YELLOW + " exp");
									}
									else{
										if(temp.getKey().contains(":")){
											tempMessage = tempMessage.replace("%item%", temp.getKey().split(":")[0].replace("org.bukkit.craftbukkit.entity.Craft", ""));
											tempMessage = tempMessage.replace("%subitem$", temp.getKey().split(":")[1]);
										}
										else{
											tempMessage = tempMessage.replace("%item%", temp.getKey().replace("org.bukkit.craftbukkit.entity.Craft", ""));
										}
										tempMessage = tempMessage.replace("%income%", format.format(incomeEquation.getValue()));
										tempMessage = tempMessage.replace("%experience%", format.format(expEquation.getValue()));
										for(String tempMsg: tempMessage.split("\n")){
											sender.sendMessage(tempMsg);
										}
									}
								}
							}
							else {
								String tempMessage = JobsConfiguration.getInstance().getMessage("kill-none");
								if(tempMessage == null){
									sender.sendMessage(job.getChatColour()+job.getName()+ChatColor.WHITE+ " does not get money from killing anything.");
								}
								else{
									for(String temp: tempMessage.split("\n")){
										sender.sendMessage(temp);
									}
								}
							}
							return true;
						}
					}
				}
			}
			if(sender instanceof ConsoleCommandSender || sender instanceof Player){
				// browse
				if(args.length == 1 && args[0].equalsIgnoreCase("browse")){
					ArrayList<String> jobs = new ArrayList<String>();
					for(Job temp: JobsConfiguration.getInstance().getJobs()){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.join."+temp.getName()))
								||
								((JobsConfiguration.getInstance().getPermissions() == null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled()))){
							if(temp.getMaxLevel() == null){
								jobs.add(temp.getChatColour() + temp.getName());
							}
							else{
								jobs.add(temp.getChatColour() + temp.getName() + ChatColor.WHITE + " - max lvl: " + temp.getMaxLevel());
							}
						}
					}
					if(jobs.size() == 0){
						String tempMessage = JobsConfiguration.getInstance().getMessage("browse-no-jobs");
						if(tempMessage == null){
							sender.sendMessage("There are no jobs you can join");
						}
						else {
							for(String temp: tempMessage.split("\n")){
								sender.sendMessage(temp);
							}
						}
						
					}
					else{
						String tempMessage = JobsConfiguration.getInstance().getMessage("browse-jobs-header");
						if(tempMessage == null){
							sender.sendMessage("You are allowed to join the following jobs:");
						}
						else {
							for(String temp: tempMessage.split("\n")){
								sender.sendMessage(temp);
							}
						}
						for(String temp: jobs){
							sender.sendMessage("    " + temp);
						}
						tempMessage = JobsConfiguration.getInstance().getMessage("browse-jobs-footer");
						if(tempMessage == null){
							sender.sendMessage("For more information type in /jobs info [JobName]");
						}
						else {
							for(String temp: tempMessage.split("\n")){
								sender.sendMessage(temp);
							}
						}
					}
					return true;
				}
				
				// admin commands
				if(args.length == 3){
					if(args[0].equalsIgnoreCase("fire")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.fire"))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job job = JobsConfiguration.getInstance().getJob(args[2]);
							if(target != null && job != null){
								try{
									// check if player even has the job
									PlayerJobInfo info = players.get(target);
									if(info == null){
										// player isn't online
										info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
									}
									if(info.isInJob(job)){
										getServer().getPluginManager().callEvent(new JobsLeaveEvent(target, job));
										String tempMessage = JobsConfiguration.getInstance().getMessage("fire-target");
										if(tempMessage == null){
											target.sendMessage("You have been fired from " + job.getChatColour() + job.getName());
										}
										else {
											tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
											tempMessage = tempMessage.replace("%jobname%", job.getName());
											for(String temp: tempMessage.split("\n")){
												target.sendMessage(temp);
											}
										}
										tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
										if(tempMessage == null){
											sender.sendMessage("Your command has been performed.");
										}
										else {
											for(String temp: tempMessage.split("\n")){
												sender.sendMessage(temp);
											}
										}
									}
									else{
										String tempMessage = JobsConfiguration.getInstance().getMessage("fire-target-no-job");
										if(tempMessage == null){
											target.sendMessage("Player does not have the job " + job.getChatColour() + job.getName());
										}
										else {
											tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
											tempMessage = tempMessage.replace("%jobname%", job.getName());
											for(String temp: tempMessage.split("\n")){
												target.sendMessage(temp);
											}
										}
									}
								}
								catch (Exception e){
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
							}
						}
						return true;
					}
					else if(args[0].equalsIgnoreCase("employ")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.employ."+args[2]))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job job = JobsConfiguration.getInstance().getJob(args[2]);
							if(target != null && job != null){
								try{
									// check if player already has the job
									PlayerJobInfo info = players.get(target);
									if(info == null){
										// player isn't online
										info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
									}
									if(!info.isInJob(job)){
										getServer().getPluginManager().callEvent(new JobsJoinEvent(target, job));
										String tempMessage = JobsConfiguration.getInstance().getMessage("employ-target");
										if(tempMessage == null){
											target.sendMessage("You have been employed in " + job.getChatColour() + job.getName());
										}
										else {
											tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
											tempMessage = tempMessage.replace("%jobname%", job.getName());
											for(String temp: tempMessage.split("\n")){
												target.sendMessage(temp);
											}
										}
										tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
										if(tempMessage == null){
											sender.sendMessage("Your command has been performed.");
										}
										else {
											for(String temp: tempMessage.split("\n")){
												sender.sendMessage(temp);
											}
										}
									}
								}
								catch (Exception e){
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
							}
						}
					}
					return true;
				}
				else if(args.length == 4){
					if(args[0].equalsIgnoreCase("promote")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.promote"))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job job = JobsConfiguration.getInstance().getJob(args[2]);
							if(target != null && job != null){
								try{
									// check if player already has the job
									PlayerJobInfo info = players.get(target);
									if(info == null){
										// player isn't online
										info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
									}
									if(info.isInJob(job)){
										Integer levelsGained = Integer.parseInt(args[3]);
										if (info.getJobsProgression(job).getJob().getMaxLevel() != null &&
												levelsGained + info.getJobsProgression(job).getLevel() > info.getJobsProgression(job).getJob().getMaxLevel()){
											levelsGained = info.getJobsProgression(job).getJob().getMaxLevel() - info.getJobsProgression(job).getLevel();
										}
										info.getJobsProgression(job).setLevel(info.getJobsProgression(job).getLevel() + levelsGained);
										if(!(target instanceof JobsPlayer)){
											info.reloadMaxExperience();
											info.checkLevels();
										}
										String tempMessage = JobsConfiguration.getInstance().getMessage("promote-target");
										if(tempMessage == null){
											target.sendMessage("You have been promoted " + levelsGained + " levels in " + job.getChatColour() + job.getName());
										}
										else {
											tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
											tempMessage = tempMessage.replace("%jobname%", job.getName());
											tempMessage = tempMessage.replace("%levelsgained%", levelsGained.toString());
											for(String temp: tempMessage.split("\n")){
												target.sendMessage(temp);
											}
										}
										tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
										if(tempMessage == null){
											sender.sendMessage("Your command has been performed.");
										}
										else {
											for(String temp: tempMessage.split("\n")){
												sender.sendMessage(temp);
											}
										}
									}
									if(target instanceof JobsPlayer){
										JobsConfiguration.getInstance().getJobsDAO().save(info);
									}
								}
								catch (Exception e){
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
							}
						}
						return true;
					}
					else if(args[0].equalsIgnoreCase("demote")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.demote"))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);	
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job job = JobsConfiguration.getInstance().getJob(args[2]);
							if(target != null && job != null){
								try{
									// check if player already has the job
									PlayerJobInfo info = players.get(target);
									if(info == null){
										// player isn't online
										info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
									}
									if(info.isInJob(job)){
										Integer levelsLost = Integer.parseInt(args[3]);
										if (info.getJobsProgression(job).getLevel() - levelsLost < 1){
											levelsLost = info.getJobsProgression(job).getLevel() - 1;
										}
										info.getJobsProgression(job).setLevel(info.getJobsProgression(job).getLevel() - levelsLost);
										if(!(target instanceof JobsPlayer)){
											info.reloadMaxExperience();
											info.checkLevels();
										}
										String tempMessage = JobsConfiguration.getInstance().getMessage("demote-target");
										if(tempMessage == null){
											target.sendMessage("You have been demoted " + levelsLost + " levels in " + job.getChatColour() + job.getName());
										}
										else {
											tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
											tempMessage = tempMessage.replace("%jobname%", job.getName());
											tempMessage = tempMessage.replace("%levelslost%", levelsLost.toString());
											for(String temp: tempMessage.split("\n")){
												target.sendMessage(temp);
											}
										}
										tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
										if(tempMessage == null){
											sender.sendMessage("Your command has been performed.");
										}
										else {
											for(String temp: tempMessage.split("\n")){
												sender.sendMessage(temp);
											}
										}
									}
									if(target instanceof JobsPlayer){
										JobsConfiguration.getInstance().getJobsDAO().save(info);
									}
								}
								catch (Exception e){
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
							}
						}
						return true;
					}
					else if(args[0].equalsIgnoreCase("grantxp")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.grantxp"))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job job = JobsConfiguration.getInstance().getJob(args[2]);
							if(target != null && job != null){
								Double expGained;
								try{
									expGained = Double.parseDouble(args[3]);
								}
								catch (ClassCastException ex){
									expGained = (double) Integer.parseInt(args[3]);
								}
								catch(Exception e){
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
									return true;
								}
								// check if player already has the job
								PlayerJobInfo info = players.get(target);
								if(info == null){
									// player isn't online
									info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
								}
								if(info.isInJob(job)){
									info.getJobsProgression(job).setExperience(info.getJobsProgression(job).getExperience() + expGained);
									if(!(target instanceof JobsPlayer)){
										info.reloadMaxExperience();
										info.checkLevels();
									}
									String tempMessage = JobsConfiguration.getInstance().getMessage("grantxp-target");
									if(tempMessage == null){
										target.sendMessage("You have been granted " + expGained + " experience in " + job.getChatColour() + job.getName());
									}
									else {
										tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
										tempMessage = tempMessage.replace("%jobname%", job.getName());
										tempMessage = tempMessage.replace("%expgained%", args[3]);
										for(String temp: tempMessage.split("\n")){
											target.sendMessage(temp);
										}
									}
									tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
									if(tempMessage == null){
										sender.sendMessage("Your command has been performed.");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
								if(target instanceof JobsPlayer){
									JobsConfiguration.getInstance().getJobsDAO().save(info);
								}
							}
						}
						return true;
					}
					else if(args[0].equalsIgnoreCase("removexp")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.removexp"))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job job = JobsConfiguration.getInstance().getJob(args[2]);
							if(target != null && job != null){
								Double expLost;
								try{
									expLost = Double.parseDouble(args[3]);
								}
								catch (ClassCastException ex){
									expLost = (double) Integer.parseInt(args[3]);
								}
								catch(Exception e){
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
									return true;
								}
								// check if player already has the job
								PlayerJobInfo info = players.get(target);
								if(info == null){
									// player isn't online
									info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
								}
								if(info.isInJob(job)){
									info.getJobsProgression(job).setExperience(info.getJobsProgression(job).getExperience() - expLost);
									String tempMessage = JobsConfiguration.getInstance().getMessage("removexp-target");
									if(tempMessage == null){
										target.sendMessage("You have lost " + expLost + " experience in " + job.getChatColour() + job.getName());
									}
									else {
										tempMessage = tempMessage.replace("%jobcolour%", job.getChatColour().toString());
										tempMessage = tempMessage.replace("%jobname%", job.getName());
										tempMessage = tempMessage.replace("%explost%", args[3]);
										for(String temp: tempMessage.split("\n")){
											target.sendMessage(temp);
										}
									}
									tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
									if(tempMessage == null){
										sender.sendMessage("Your command has been performed.");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
								if(target instanceof JobsPlayer){
									JobsConfiguration.getInstance().getJobsDAO().save(info);
								}
							}
						}
						return true;
					}
					else if(args[0].equalsIgnoreCase("transfer")){
						if(sender instanceof ConsoleCommandSender || 
								(JobsConfiguration.getInstance().getPermissions()!= null &&
								JobsConfiguration.getInstance().getPermissions().isEnabled() &&
								JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.transfer"))
								||
								(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
							Player target = getServer().getPlayer(args[1]);
							if(target == null){
								target = new JobsPlayer(args[1]);
							}
							Job oldjob = JobsConfiguration.getInstance().getJob(args[2]);
							Job newjob = JobsConfiguration.getInstance().getJob(args[3]);
							if(target != null && oldjob != null & newjob != null){
								try{
									PlayerJobInfo info = players.get(target);
									if (info == null){
										info = new PlayerJobInfo(target, JobsConfiguration.getInstance().getJobsDAO());
									}
									if(info.isInJob(oldjob) && !info.isInJob(newjob)){
										info.transferJob(oldjob, newjob);
										if(newjob.getMaxLevel() != null && info.getJobsProgression(newjob).getLevel() > newjob.getMaxLevel()){
											info.getJobsProgression(newjob).setLevel(newjob.getMaxLevel());
										}
										if(!(target instanceof JobsPlayer)){
											info.reloadMaxExperience();
											info.reloadHonorific();
											info.checkLevels();
										}
										// quit old job
										JobsConfiguration.getInstance().getJobsDAO().quitJob(target, oldjob);
										// join new job
										JobsConfiguration.getInstance().getJobsDAO().joinJob(target, newjob);
										// save data
										JobsConfiguration.getInstance().getJobsDAO().save(info);
										String tempMessage = JobsConfiguration.getInstance().getMessage("removexp-target");
										if(tempMessage == null){
											target.sendMessage("You have been transferred from " + oldjob.getChatColour() + oldjob.getName() + " to " + newjob.getChatColour() +  newjob.getName());
										}
										else {
											tempMessage = tempMessage.replace("%oldjobcolour%", oldjob.getChatColour().toString());
											tempMessage = tempMessage.replace("%oldjobname%", oldjob.getName());
											tempMessage = tempMessage.replace("%newjobcolour%", newjob.getChatColour().toString());
											tempMessage = tempMessage.replace("%newjobname%", newjob.getName());
											for(String temp: tempMessage.split("\n")){
												target.sendMessage(temp);
											}
										}
										tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-success");
										if(tempMessage == null){
											sender.sendMessage("Your command has been performed.");
										}
										else {
											for(String temp: tempMessage.split("\n")){
												sender.sendMessage(temp);
											}
										}
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
									String tempMessage = JobsConfiguration.getInstance().getMessage("admin-command-failed");
									if(tempMessage == null){
										sender.sendMessage(ChatColor.RED + "There was an error in the command");
									}
									else {
										for(String temp: tempMessage.split("\n")){
											sender.sendMessage(temp);
										}
									}
								}
							}
						}
					}
					return true;
				}
				if(args.length > 0){
					sender.sendMessage(ChatColor.RED + "There was an error in your command");
				}
				// jobs-browse
				String tempMessage = JobsConfiguration.getInstance().getMessage("jobs-browse");
				if(tempMessage == null){
					sender.sendMessage("/jobs browse - list the jobs available to you");
				}
				else {
					for(String temp: tempMessage.split("\n")){
						sender.sendMessage(temp);
					}
				}
				// jobs-join
				if(sender instanceof Player){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-join");
					if(tempMessage == null){
						sender.sendMessage("/jobs join <jobname> - join the selected job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
					//jobs-leave
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-leave");
					if(tempMessage == null){
						sender.sendMessage("/jobs leave <jobname> - leave the selected job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
					//jobs-stats
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-stats");
					if(tempMessage == null){
						sender.sendMessage("/jobs stats - show the level you are in each job you are part of");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
					//jobs-info
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-info");
					if(tempMessage == null){
						sender.sendMessage("/jobs info <jobname> <break, place, kill> - show how much each job is getting paid and for what");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-fire
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.fire"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-fire");
					if(tempMessage == null){
						sender.sendMessage(" /jobs fire <playername> <job> - fire the player from the job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-employ
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.employ"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-employ");
					if(tempMessage == null){
						sender.sendMessage("/jobs employ <playername> <job> - employ the player to the job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-promote
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.promote"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-promote");
					if(tempMessage == null){
						sender.sendMessage("/jobs promote <playername> <job> <levels> - promote the player X levels in a job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-demote
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.demote"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-demote");
					if(tempMessage == null){
						sender.sendMessage("/jobs demote <playername> <job> <levels> - demote the player X levels in a job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-grantxp
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.grantxp"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-grantxp");
					if(tempMessage == null){
						sender.sendMessage("/jobs grantxp <playername> <job> <experience> - grant the player X experience in a job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-removexp
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.removexp"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-removexp");
					if(tempMessage == null){
						sender.sendMessage("/jobs removexp <playername> <job> <experience> - remove X experience from the player in a job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
				//jobs-admin-transfer
				if(sender instanceof ConsoleCommandSender || 
						(JobsConfiguration.getInstance().getPermissions()!= null &&
						JobsConfiguration.getInstance().getPermissions().isEnabled() &&
						JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.transfer"))
						||
						(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
					tempMessage = JobsConfiguration.getInstance().getMessage("jobs-admin-transfer");
					if(tempMessage == null){
						sender.sendMessage("/jobs transfer <playername> <oldjob> <newjob> - transfer a player's job from an old job to a new job");
					}
					else {
						for(String temp: tempMessage.split("\n")){
							sender.sendMessage(temp);
						}
					}
				}
			}
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
	 * Function to tell the player they do not have permission to do something
	 * @param sender
	 */
	private void noPermission(CommandSender sender){
		String tempMessage = JobsConfiguration.getInstance().getMessage("error-no-permission");
		if(tempMessage == null){
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
		}
		else {
			for(String temp: tempMessage.split("\n")){
				sender.sendMessage(temp);
			}
		}
	}
	
	/**
	 * Function to tell the player that the job does not exist
	 * @param sender
	 */
	private void jobDoesNotExist(CommandSender sender){
		String tempMessage = JobsConfiguration.getInstance().getMessage("error-no-job");
		if(tempMessage == null){
			sender.sendMessage(ChatColor.RED + "The job you have selected does not exist");
		}
		else {
			for(String temp: tempMessage.split("\n")){
				sender.sendMessage(temp);
			}
		}
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
