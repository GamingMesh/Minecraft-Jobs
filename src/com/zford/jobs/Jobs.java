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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


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
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.JobsMessages;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.JobProgression;
import com.zford.jobs.config.container.JobsMaterialInfo;
import com.zford.jobs.config.container.JobsLivingEntityInfo;
import com.zford.jobs.config.container.PlayerJobInfo;
import com.zford.jobs.economy.JobsBOSEconomyLink;
import com.zford.jobs.economy.JobsiConomyLink;
import com.zford.jobs.event.JobsJoinEvent;
import com.zford.jobs.event.JobsLeaveEvent;
import com.zford.jobs.fake.JobsPlayer;
import com.zford.jobs.listener.JobsBlockPaymentListener;
import com.zford.jobs.listener.JobsFishPaymentListener;
import com.zford.jobs.listener.JobsJobListener;
import com.zford.jobs.listener.JobsKillPaymentListener;
import com.zford.jobs.listener.JobsPlayerListener;

import cosine.boseconomy.BOSEconomy;

/**
 * Jobs main class
 * @author Alex
 * @author Zak Ford <zak.j.ford@gmail.com>
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
		
		for(Entry<Player, PlayerJobInfo> online: players.entrySet()){
			// wipe the honorific
			online.getKey().setDisplayName(online.getKey().getDisplayName().replace(online.getValue().getDisplayHonorific()+" ", "").trim());
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
		plugin = this;
		players = new HashMap<Player, PlayerJobInfo>();
		JobsConfiguration.getInstance();
		
		if(isEnabled()){
			JobsBlockPaymentListener blockListener = new JobsBlockPaymentListener(this);
			JobsJobListener jobListener = new JobsJobListener(this);
			JobsKillPaymentListener killListener = new JobsKillPaymentListener(this);
            JobsPlayerListener playerListener = new JobsPlayerListener(this);
            JobsFishPaymentListener fishListener = new JobsFishPaymentListener(this);
			
			// set the system to auto save
			if(JobsConfiguration.getInstance().getSavePeriod() > 0){
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
									
					// economy plugins
					if(JobsConfiguration.getInstance().getEconomyLink() == null){
						if(getServer().getPluginManager().getPlugin("iConomy") != null || 
								getServer().getPluginManager().getPlugin("BOSEconomy") != null){
							if(getServer().getPluginManager().getPlugin("iConomy") != null){
								JobsConfiguration.getInstance().setEconomyLink(new JobsiConomyLink((iConomy)getServer().getPluginManager().getPlugin("iConomy")));
			                    System.out.println("[Jobs] Successfully linked with iConomy 5+.");
							}
							else if(getServer().getPluginManager().getPlugin("BOSEconomy") != null){
								JobsConfiguration.getInstance().setEconomyLink(new JobsBOSEconomyLink((BOSEconomy)getServer().getPluginManager().getPlugin("BOSEconomy")));
			                    System.out.println("[Jobs] Successfully linked with BOSEconomy.");
							}
						} else {
                            System.err.println("[Jobs] Cannot find valid economy plugin");
                            Jobs.disablePlugin();
                            return;
                        }
					}
					
					// stats
					if(JobsConfiguration.getInstance().getStats() == null && JobsConfiguration.getInstance().isStatsEnabled()){
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
			getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, killListener, Event.Priority.Monitor, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, fishListener, Event.Priority.Monitor, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM_HELD, fishListener, Event.Priority.Monitor, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_PICKUP_ITEM, fishListener, Event.Priority.Monitor, this);
            getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, fishListener, Event.Priority.Monitor, this);
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
		if(!label.equalsIgnoreCase("jobs")){
		    return true;
		}
		
		if(sender instanceof Player){
			// player only commands
			// join
			if(args.length == 2 && args[0].equalsIgnoreCase("join")){
				String jobName = args[1].trim();
				if(JobsConfiguration.getInstance().getJob(jobName) != null && !jobName.equalsIgnoreCase("None")){
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
                            Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("join-too-many-job"));
                            return true;
						}
					}
					else {
						// you do not have permission to join the job
					    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("error-no-permission"));
						return true;
					}
				}
				else{
					// job does not exist
					Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("error-no-job"));
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
					Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("error-no-job"));
				}
				return true;
			}
			// stats
			else if(args.length >= 1 && args[0].equalsIgnoreCase("stats")){
			    Player statsPlayer = (Player)sender;
			    if(args.length == 2) {
			        if(JobsConfiguration.getInstance().getPermissions()!= null &&
			                JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.stats")) {
			            statsPlayer = getServer().getPlayer(args[1]);
			        }
			        else {
			            sender.sendMessage(ChatColor.RED + "There was an error in your command");
			            return true;
			        }
			    }
			        
				if(getJob(statsPlayer).getJobsProgression().size() == 0){
                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("stats-no-job"));
					return true;
				}
				else{
					for(JobProgression jobProg: getJob(statsPlayer).getJobsProgression()){
					    sendMessageByLine(sender, jobStatsMessage(jobProg));
					}
					return true;
				}
			}
			// jobs info <jobname> <break, place, kill>
			else if(args.length >= 2 && args[0].equalsIgnoreCase("info")){
		        Job job = JobsConfiguration.getInstance().getJob(args[1]);
		        String type = "";
		        if(args.length >= 3) {
		            type = args[2];
		        }
		        sendMessageByLine(sender, jobInfoMessage((Player)sender, job, type));
		        return true;
			}
		}
		if(sender instanceof ConsoleCommandSender || sender instanceof Player){
			// browse
			if(args.length >= 1 && args[0].equalsIgnoreCase("browse")){
				ArrayList<String> jobs = new ArrayList<String>();
				for(Job temp: JobsConfiguration.getInstance().getJobs()){
					if(sender instanceof ConsoleCommandSender || 
							(JobsConfiguration.getInstance().getPermissions()!= null &&
							JobsConfiguration.getInstance().getPermissions().isEnabled() &&
							JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.join."+temp.getName()))
							||
							((JobsConfiguration.getInstance().getPermissions() == null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled()))){
					    if(!temp.getName().equalsIgnoreCase("None")) {
    						if(temp.getMaxLevel() == null){
    							jobs.add(temp.getChatColour() + temp.getName());
    						}
    						else{
    							jobs.add(temp.getChatColour() + temp.getName() + ChatColor.WHITE + " - max lvl: " + temp.getMaxLevel());
    						}
					    }
					}
				}
				if(jobs.size() == 0){
                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("browse-no-jobs"));
					
				}
				else{
                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("browse-jobs-header"));
				    
				    for(String job : jobs) {
				        sender.sendMessage("    "+job);
				    }
				    
				    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("browse-jobs-footer"));
				}
				return true;
			}
            
            // admin commands
			else if(args.length >= 2 && args[0].equalsIgnoreCase("admininfo")){
                if(sender instanceof ConsoleCommandSender || 
                        (JobsConfiguration.getInstance().getPermissions()!= null &&
                        JobsConfiguration.getInstance().getPermissions().isEnabled() &&
                        JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.info"))
                        ||
                        (((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
                    Player target = getServer().getPlayer(args[1]);
                    if(target == null){
                        target = new JobsPlayer(args[1]);
                    }
                    
                    String message = "";
                    message += "----------------\n";
                    for(JobProgression jobProg: getJob(target).getJobsProgression()){
                        Job job = jobProg.getJob();
                        message += jobStatsMessage(jobProg);
                        message += jobInfoMessage(target, job, "");
                        message += "----------------\n";
                    }
                    sendMessageByLine(sender, message);
                }
                return true;
            }
			
			if(args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			    if(sender instanceof ConsoleCommandSender || 
                        (JobsConfiguration.getInstance().getPermissions()!= null &&
                        JobsConfiguration.getInstance().getPermissions().isEnabled() &&
                        JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.reload"))
                        ||
                        (((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
			        try {
    			        if(isEnabled()) {
    			            JobsConfiguration.getInstance().reload();
        			        if(sender instanceof Player) {
        			            Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
        			        }
    			        }
			        } catch (Exception e) {
			            Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
			        }
                    return true;
			    }
			}
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
									String message = JobsMessages.getInstance().getMessage("fire-target");
								    message = message.replace("%jobcolour%", job.getChatColour().toString());
								    message = message.replace("%jobname%", job.getName());
                                    Jobs.sendMessageByLine(target, message);
                                    
                                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
								}
								else{
									String message = JobsMessages.getInstance().getMessage("fire-target-no-job");
									message = message.replace("%jobcolour%", job.getChatColour().toString());
									message = message.replace("%jobname%", job.getName());
                                    Jobs.sendMessageByLine(sender, message);
								}
							}
							catch (Exception e){
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
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
									String message = JobsMessages.getInstance().getMessage("employ-target");
								    message = message.replace("%jobcolour%", job.getChatColour().toString());
								    message = message.replace("%jobname%", job.getName());
                                    Jobs.sendMessageByLine(target, message);
                                    
                                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
								}
							}
							catch (Exception e){
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
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
									
									{
										String message = JobsMessages.getInstance().getMessage("promote-target");
									    message = message.replace("%jobcolour%", job.getChatColour().toString());
									    message = message.replace("%jobname%", job.getName());
									    message = message.replace("%levelsgained%", levelsGained.toString());
                                        Jobs.sendMessageByLine(target, message);
									}
                                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
								}
								if(target instanceof JobsPlayer){
									JobsConfiguration.getInstance().getJobsDAO().save(info);
								}
							}
							catch (Exception e){
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
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
									
									{
										String message = JobsMessages.getInstance().getMessage("demote-target");
										message = message.replace("%jobcolour%", job.getChatColour().toString());
										message = message.replace("%jobname%", job.getName());
										message = message.replace("%levelslost%", levelsLost.toString());
                                        Jobs.sendMessageByLine(target, message);
									}
                                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
								}
								if(target instanceof JobsPlayer){
									JobsConfiguration.getInstance().getJobsDAO().save(info);
								}
							}
							catch (Exception e){
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
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
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
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
								{
									String message = JobsMessages.getInstance().getMessage("grantxp-target");
									message = message.replace("%jobcolour%", job.getChatColour().toString());
									message = message.replace("%jobname%", job.getName());
									message = message.replace("%expgained%", args[3]);
                                    Jobs.sendMessageByLine(target, message);
								}
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
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
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
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
								
								{
									String message = JobsMessages.getInstance().getMessage("removexp-target");
								    message = message.replace("%jobcolour%", job.getChatColour().toString());
								    message = message.replace("%jobname%", job.getName());
								    message = message.replace("%explost%", args[3]);
                                    Jobs.sendMessageByLine(target, message);
								}
                                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
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
									{
										String message = JobsMessages.getInstance().getMessage("transfer-target");
									    message = message.replace("%oldjobcolour%", oldjob.getChatColour().toString());
									    message = message.replace("%oldjobname%", oldjob.getName());
									    message = message.replace("%newjobcolour%", newjob.getChatColour().toString());
										message = message.replace("%newjobname%", newjob.getName());
	                                    Jobs.sendMessageByLine(target, message);
									}
                                    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-success"));
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
							    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("admin-command-failed"));
							}
						}
					}
	                return true;
				}
			}
			if(args.length > 0){
				sender.sendMessage(ChatColor.RED + "There was an error in your command");
			}
			
			// jobs-browse
            Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-browse"));
			
			if(sender instanceof Player){
                // jobs-join
                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-join"));
                
                //jobs-leave
                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-leave"));
                
            	//jobs-stats
                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-stats"));
                
            	//jobs-info
                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-info"));
			}
			//jobs-admin-info
            if(sender instanceof ConsoleCommandSender || 
                    (JobsConfiguration.getInstance().getPermissions()!= null &&
                    JobsConfiguration.getInstance().getPermissions().isEnabled() &&
                    JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.info"))
                    ||
                    (((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-info"));
            }
			//jobs-admin-fire
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.fire"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
				Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-fire"));
			}
			//jobs-admin-employ
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.employ"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
				Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-employ"));
			}
			//jobs-admin-promote
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.promote"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
				Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-promote"));
			}
			//jobs-admin-demote
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.demote"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
			    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-demote"));
			}
			//jobs-admin-grantxp
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.grantxp"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
			    Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-grantxp"));
			}
			//jobs-admin-removexp
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.removexp"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
				Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-removexp"));
			}
			//jobs-admin-transfer
			if(sender instanceof ConsoleCommandSender || 
					(JobsConfiguration.getInstance().getPermissions()!= null &&
					JobsConfiguration.getInstance().getPermissions().isEnabled() &&
					JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.transfer"))
					||
					(((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
				Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-transfer"));
			}
			if(sender instanceof ConsoleCommandSender || 
                    (JobsConfiguration.getInstance().getPermissions()!= null &&
                    JobsConfiguration.getInstance().getPermissions().isEnabled() &&
                    JobsConfiguration.getInstance().getPermissions().getHandler().has((Player)sender, "jobs.admin.reload"))
                    ||
                    (((JobsConfiguration.getInstance().getPermissions()== null) || !(JobsConfiguration.getInstance().getPermissions().isEnabled())) && sender.isOp())){
                Jobs.sendMessageByLine(sender, JobsMessages.getInstance().getMessage("jobs-admin-reload"));
            }
		}
		return true;
	}
	
	/**
	 * Displays info about a job
	 * @param player - the player of the job
	 * @param job - the job we are displaying info about
	 * @param type - type of info
	 * @return the message
	 */
	private String jobInfoMessage(Player player, Job job, String type) {
        if(job == null){
            // job doesn't exist
            return JobsMessages.getInstance().getMessage("error-no-job");
        }
        
        String message = "";
        
        int showAllTypes = 1;
        if(type.equalsIgnoreCase("break") || type.equalsIgnoreCase("place") || type.equalsIgnoreCase("kill") || type.equalsIgnoreCase("fish")) {
            showAllTypes = 0;
        }
        
        if(type.equalsIgnoreCase("break") || showAllTypes == 1){
            // break
            HashMap<String, JobsMaterialInfo> jobBreakInfo = job.getBreakInfo();
            if(jobBreakInfo != null){
                message += jobInfoBreakMessage(player, job, jobBreakInfo);
            }
            else if(showAllTypes == 0) {
                String myMessage = JobsMessages.getInstance().getMessage("break-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        if(type.equalsIgnoreCase("place") || showAllTypes == 1){
            // place
            HashMap<String, JobsMaterialInfo> jobPlaceInfo = job.getPlaceInfo();
            
            if(jobPlaceInfo != null){
                message += jobInfoPlaceMessage(player, job, jobPlaceInfo);
            }
            else if(showAllTypes == 0) {
                String myMessage = JobsMessages.getInstance().getMessage("place-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        if(type.equalsIgnoreCase("kill") || showAllTypes == 1){
            // kill
            HashMap<String, JobsLivingEntityInfo> jobKillInfo = job.getKillInfo();
            
            if(jobKillInfo != null){
                message += jobInfoKillMessage(player, job, jobKillInfo);
            }
            else if(showAllTypes == 0) {
                String myMessage = JobsMessages.getInstance().getMessage("kill-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        
        if(type.equalsIgnoreCase("fish") || showAllTypes == 1){
            // fish
            HashMap<String, JobsMaterialInfo> jobFishInfo = job.getFishInfo();
            
            if(jobFishInfo != null){
                message += jobInfoFishMessage(player, job, jobFishInfo);
            }
            else if(showAllTypes == 0) {
                String myMessage = JobsMessages.getInstance().getMessage("fish-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        return message;
	}
	
	/**
     * Displays info about breaking blocks
     * @param player - the player of the job
	 * @param job - the job we are displaying info about
	 * @param jobBreakInfo - the information to display
	 * @return the message
     */
	private String jobInfoBreakMessage(Player player, Job job, HashMap<String, JobsMaterialInfo> jobBreakInfo) {
	    
	    String message = "";
	    message += JobsMessages.getInstance().getMessage("break-header");
        
        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = getJob(player).getJobsProgression(job);
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
        expEquation.setVariable("numjobs", getJob(player).getJobs().size());
        incomeEquation.setVariable("numjobs", getJob(player).getJobs().size());
        for(Entry<String, JobsMaterialInfo> temp: jobBreakInfo.entrySet()){
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = JobsMessages.getInstance().getMessage("break-info-sub");
            }
            else {
                myMessage = JobsMessages.getInstance().getMessage("break-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage;
        }
        return message;
	}
	
    /**
     * Displays info about placing blocks
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobPlaceInfo - the information to display
     * @return the message
     */	
	private String jobInfoPlaceMessage(Player player, Job job, HashMap<String, JobsMaterialInfo> jobPlaceInfo) {
	    
	    String message = "";
	    message += JobsMessages.getInstance().getMessage("place-header");

	    DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = getJob(player).getJobsProgression(job);
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
        expEquation.setVariable("numjobs", getJob(player).getJobs().size());
        incomeEquation.setVariable("numjobs", getJob(player).getJobs().size());
        for(Entry<String, JobsMaterialInfo> temp: jobPlaceInfo.entrySet()){
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = JobsMessages.getInstance().getMessage("place-info-sub");
            }
            else {
                myMessage = JobsMessages.getInstance().getMessage("place-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage;
        }
        return message;
	}
	
    /**
     * Displays info about killing entities
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobKillInfo - the information to display
     * @return the message
     */
    private String jobInfoKillMessage(Player player, Job job, HashMap<String, JobsLivingEntityInfo> jobKillInfo) {
        
        String message = "";
        message += JobsMessages.getInstance().getMessage("kill-header");

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = getJob(player).getJobsProgression(job);
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
        expEquation.setVariable("numjobs", getJob(player).getJobs().size());
        incomeEquation.setVariable("numjobs", getJob(player).getJobs().size());
        for(Entry<String, JobsLivingEntityInfo> temp: jobKillInfo.entrySet()){
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = JobsMessages.getInstance().getMessage("kill-info-sub");
            }
            else {
                myMessage = JobsMessages.getInstance().getMessage("kill-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("org.bukkit.craftbukkit.entity.Craft", ""));
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("org.bukkit.craftbukkit.entity.Craft", ""));
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage;
        }
        return message;
    }
    
    /**
     * Displays info about fishing
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobFishInfo - the information to display
     * @return the message
     */ 
    private String jobInfoFishMessage(Player player, Job job, HashMap<String, JobsMaterialInfo> jobFishInfo) {
        
        String message = "";
        message += JobsMessages.getInstance().getMessage("fish-header");

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = getJob(player).getJobsProgression(job);
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
        expEquation.setVariable("numjobs", getJob(player).getJobs().size());
        incomeEquation.setVariable("numjobs", getJob(player).getJobs().size());
        for(Entry<String, JobsMaterialInfo> temp: jobFishInfo.entrySet()){
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = JobsMessages.getInstance().getMessage("fish-info-sub");
            }
            else {
                myMessage = JobsMessages.getInstance().getMessage("fish-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage;
        }
        return message;
    }
    
    /**
     * Displays job stats about a particular player's job
     * @param jobProg - the job progress of the players job
     * @return the message
     */
    private String jobStatsMessage(JobProgression jobProg) {
        String message = JobsMessages.getInstance().getMessage("stats-job");
        message = message.replace("%joblevel%", Integer.valueOf(jobProg.getLevel()).toString());
        message = message.replace("%jobcolour%", jobProg.getJob().getChatColour().toString());
        message = message.replace("%jobname%", jobProg.getJob().getName());
        message = message.replace("%jobexp%", Integer.toString((int)jobProg.getExperience()));
        message = message.replace("%jobmaxexp%", Integer.toString(jobProg.getMaxExperience()));
        return message;
    }
    
    
    /**
     * Sends a message to line by line
     * @param sender - who receives info
     * @param message - message which needs to be sent
     */
    private static void sendMessageByLine(CommandSender sender, String message) {
        for(String line : message.split("\n")) {
            sender.sendMessage(line);
        }
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
	    if(player == null) {
	        return null;
	    }
		PlayerJobInfo info = players.get(player);
		if(info == null) {
		    info = new PlayerJobInfo(player, JobsConfiguration.getInstance().getJobsDAO());
		}
		return info;
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
		    Jobs.getPlugin().setEnabled(false);
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
