package me.alex.jobs.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import me.alex.jobs.Jobs;
import me.alex.jobs.config.container.Job;
import me.alex.jobs.config.container.JobsBlockInfo;
import me.alex.jobs.config.container.JobsLivingEntityInfo;
import me.alex.jobs.config.container.Title;
import me.alex.jobs.dao.JobsDAO;
import me.alex.jobs.dao.JobsDAOFlatfile;
import me.alex.jobs.dao.JobsDAOMySQL;
import me.alex.jobs.economy.JobsEconomyLink;
import me.alex.jobs.util.DisplayMethod;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.mbertoli.jfep.Parser;
import org.yaml.snakeyaml.Yaml;

import com.nidefawl.Stats.Stats;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * Configuration class.
 * 
 * Holds all the configuration information for the jobs plugin
 * @author Alex
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
	// messages
	private HashMap<String, String> messages = null;
	// do i broadcast skillups?
	private boolean broadcast;
	// maximum number of jobs a player can join
	private Integer maxJobs;
	// used slots for each job
	private HashMap<Job, Integer> usedSlots;
	
	/**
	 * Private constructor.
	 * 
	 * Can only be called from within the class.
	 * Made to observe the singleton pattern.
	 */
	private JobsConfiguration(){
		// general settings
		loadGeneralSettings();
		// job settings
		loadJobSettings();
		// title settings
		loadTitleSettings();
		// messages settings
		loadMessageSettings();
		// get slots
		loadSlots();
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
	@SuppressWarnings("unchecked")
	private void loadGeneralSettings(){
		try {
			Yaml yaml = new Yaml();
			Object obj = yaml.load(new FileInputStream("plugins/Jobs/generalConfig.yml"));
			Map<String, Object> map = (Map<String, Object>)obj;
			
			// create dao.
			if(map.containsKey("storage-method")&& ((String)map.get("storage-method")).equals("MySQL")){
				String username = null;
				String url = null;
				String dbName = null;
				String password = null;
				String prefix = "";
				if(map.containsKey("mysql-username") && !((String)map.get("mysql-username")).equals("")){
					username = (String)map.get("mysql-username");
				}
				else{
					System.err.println("[Jobs] - mysql-username property invalid or missing");
					Jobs.disablePlugin();
					return;
				}
				if(map.containsKey("mysql-password")){
					password = (String)map.get("mysql-password");
				}
				else{
					System.err.println("[Jobs] - mysql-password property missing");
					Jobs.disablePlugin();
					return;
				}
				if(map.containsKey("mysql-database") && !((String)map.get("mysql-database")).equals("")){
					dbName = (String)map.get("mysql-database");
				}
				else{
					System.err.println("[Jobs] - mysql-database property invalid or missing");
					Jobs.disablePlugin();
					return;
				}
				if(map.containsKey("mysql-url") && !((String)map.get("mysql-url")).equals("")){
					url = (String)map.get("mysql-url");
				}
				else{
					System.err.println("[Jobs] - mysql-url property invalid or missing");
					Jobs.disablePlugin();
					return;
				}
				if(map.containsKey("mysql-table-prefix") && map.get("mysql-table-prefix") != null){
					prefix = (String)map.get("mysql-table-prefix");
				}
				else{
					System.err.println("[Jobs] - mysql-table-prefix property invalid or missing. Defaulting to no prefix.");
					prefix = "";
				}
				dao = new JobsDAOMySQL(url, dbName, username, password, prefix);
				// set up database
				((JobsDAOMySQL)dao).setUp();
			}
			else if(map.containsKey("storage-method") && ((String)map.get("storage-method")).equals("flatfile")){
				// create flatfile dao
				dao = new JobsDAOFlatfile();
			}
			else{
				// invalid selection
				System.err.println("[Jobs] - Storage method invalid or missing");
				Jobs.disablePlugin();
			}
			
			// save-period
			if(map.containsKey("save-period")){
				try{
					savePeriod = (Integer)map.get("save-period");
				}
				catch (ClassCastException e){
					// someone wrote it as a double
					try{
						double savePer = (Double)map.get("save-period");
						savePeriod = (int)savePer;
					}
					catch (Exception ex){
						// something went really wrong
						System.err.println("[Jobs] - error with save-period property");
						Jobs.disablePlugin();
					}
				}
			}
			else{
				System.out.println("[Jobs] - save-period property not found. Defaulting to 10!");
				savePeriod = 10;
			}
			
			// broadcasting
			if(map.containsKey("broadcast-on-skill-up")){
				try{
					broadcast = (Boolean)map.get("broadcast-on-skill-up");
				}
				catch (Exception e) {
					System.out.println("[Jobs] - broadcast-on-skill-up property does is invalid. Defaulting to false");
					broadcast = false;
				}
			}
			else{
				System.out.println("[Jobs] - broadcast-on-skill-up property does not exist. Defaulting to false");
				broadcast = false;
			}
			
			// save-period
			if(map.containsKey("max-jobs")){
				try{
					maxJobs = (Integer)map.get("max-jobs");
				}
				catch (ClassCastException e){
					// someone wrote it as a double
					try{
						double maxJob = (Double)map.get("max-jobs");
						maxJobs = (int)maxJob;
					}
					catch (Exception ex){
						// something went really wrong
						System.err.println("[Jobs] - error with max-jobs property. ");
						Jobs.disablePlugin();
					}
				}
			}
			else{
				System.out.println("[Jobs] - max-jobs property not found. Defaulting to unlimited!");
				maxJobs = null;
			}

		} catch (FileNotFoundException e) {
			System.err.println("[Jobs] - configuration file generalConfig.yml does not exist");
			// disable plugin
			Jobs.disablePlugin();
		}
	}
	
	/**
	 * Method to load the jobs configuration
	 * 
	 * loads from Jobs/jobConfig.yml
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadJobSettings(){
		try {
			Yaml yaml = new Yaml();
			Object obj = yaml.load(new FileInputStream("plugins/Jobs/jobConfig.yml"));
			Map<String, Object> map = (Map<String, Object>)obj;
			if(map.containsKey("Jobs")){
				Map<String, Object> jobsMap = (Map<String, Object>)map.get("Jobs");
				if(jobsMap.size() == 0){
					// no jobs
					System.err.println("[Jobs] - No jobs detected. Disabling Jobs!");
					Jobs.disablePlugin();
					return;
				}
				else{
					// some jobs
					jobs = new HashMap<String, Job>();
					for(Entry<String, Object> jobMap: jobsMap.entrySet()){
						Map<String, Object> jobInfoMap = (Map<String, Object>) jobMap.getValue();
						// fullname
						String jobName;
						if(jobInfoMap.containsKey("fullname")){
							try{
								jobName = (String) jobInfoMap.get("fullname");
								jobName = jobName.trim();
							}
							catch(Exception e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid fullname property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the fullname property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// max-level
						Integer maxLevel = null;
						if(jobInfoMap.containsKey("max-level")){
							try{
								maxLevel = (Integer) jobInfoMap.get("max-level");
							}
							catch(ClassCastException e){
								try{
									double temp = (Double) jobInfoMap.get("max-level");
									maxLevel = (int) temp;
								}
								catch (Exception ex){
									System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid max-level property. Disabling jobs!");
									Jobs.disablePlugin();
									return;
								}
							}
						}
						else{
							System.out.println("[Jobs] - Job " + jobMap.getKey() + " is missing the max-level property. defaulting to no limits !");
						}
						
						// max-slots
						Integer maxSlots = null;
						if(jobInfoMap.containsKey("slots")){
							try{
								maxSlots = (Integer) jobInfoMap.get("slots");
							}
							catch(ClassCastException e){
								try{
									double temp = (Double) jobInfoMap.get("slots");
									maxSlots = (int) temp;
								}
								catch (Exception ex){
									System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid slots property. Disabling jobs!");
									Jobs.disablePlugin();
									return;
								}
							}
						}
						else{
							System.out.println("[Jobs] - Job " + jobMap.getKey() + " is missing the slots property. defaulting to no limits !");
						}
						
						// shortname
						String jobShortName;
						if(jobInfoMap.containsKey("shortname")){
							try{
								jobShortName = (String) jobInfoMap.get("shortname");
								jobShortName = jobShortName.trim();
							}
							catch(Exception e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid shortname property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the shortname property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// Chatcolour
						ChatColor jobColour;
						if(jobInfoMap.containsKey("ChatColour")){
							try{
								jobColour = ChatColor.valueOf((String) jobInfoMap.get("ChatColour"));
							}
							catch(IllegalArgumentException e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid ChatColour property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the ChatColour property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// chat-display
						DisplayMethod displayMethod;
						if(jobInfoMap.containsKey("chat-display")){
							try{
								String disp = (String) jobInfoMap.get("chat-display");
								if(disp.equalsIgnoreCase("full")){
									// full
									displayMethod = DisplayMethod.FULL;
								}
								else if(disp.equalsIgnoreCase("job")){
									// job only
									displayMethod = DisplayMethod.JOB;
								}
								else if(disp.equalsIgnoreCase("title")){
									// title only
									displayMethod = DisplayMethod.TITLE;
								}
								else if(disp.equalsIgnoreCase("none")){
									// none
									displayMethod = DisplayMethod.NONE;
								}
								else {
									// error
									System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid broadcast-skill-up property. Disabling jobs !");
									Jobs.disablePlugin();
									return;
								}
							}
							catch(Exception e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid broadcast-skill-up property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the broadcast-skill-up property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// leveling progression equation
						Parser maxExpEquation;
						if(jobInfoMap.containsKey("leveling-progression-equation")){
							try{
								String parserInput = (String) jobInfoMap.get("leveling-progression-equation");
								maxExpEquation = new Parser(parserInput);
							}
							catch(Exception e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid leveling-progression-equation property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the leveling-progression-equation property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// income progression equation
						Parser incomeEquation;
						if(jobInfoMap.containsKey("income-progression-equation")){
							try{
								String parserInput = (String) jobInfoMap.get("income-progression-equation");
								incomeEquation = new Parser(parserInput);
							}
							catch(Exception e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid income-progression-equation property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the income-progression-equation property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// experience progression equation
						Parser expEquation;
						if(jobInfoMap.containsKey("experience-progression-equation")){
							try{
								String parserInput = (String) jobInfoMap.get("experience-progression-equation");
								expEquation = new Parser(parserInput);
							}
							catch(Exception e){
								System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid experience-progression-equation property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing the experience-progression-equation property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						
						// items
						
						// break
						HashMap<String, JobsBlockInfo> jobBreakInfo = null;
						if(jobInfoMap.containsKey("Break")){
							// break tag exists
							Map<String, Object> jobBreakMap = (Map<String, Object>) jobInfoMap.get("Break");
							if(jobBreakMap.size() == 0){
								// no break blocks detected
								jobBreakInfo = null;
							}
							else{
								// has some break blocks
								for(Entry<String, Object> jobBreakBlock: jobBreakMap.entrySet()){
									String blockType = String.valueOf(jobBreakBlock.getKey()).toUpperCase();
									String subtype = "";
									if(((String)blockType).contains("-")){
										// uses subtype
										subtype = ":"+((String)blockType).split("-")[1];
										blockType = ((String)blockType).split("-")[0];
									}
									Double income;
									Double experience;
									if (jobBreakInfo == null){
										jobBreakInfo = new HashMap<String, JobsBlockInfo>();
									}
									Map<String, Object> blockData = (Map<String, Object>) jobBreakBlock.getValue();
									// income
									if(blockData.containsKey("income")){
										try{
											income = (Double) blockData.get("income");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("income");
												income = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobBreakBlock.getKey() + " Break income property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobBreakBlock.getKey() + " Break income property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									// experience
									if(blockData.containsKey("experience")){
										try{
											experience = (Double) blockData.get("experience");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("experience");
												experience = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobBreakBlock.getKey() + " Break experience property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobBreakBlock.getKey() + " Break experience property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									MaterialData materData;
									Material mater;
									try{
										mater = Material.matchMaterial(blockType);
									}
									catch (IllegalArgumentException ex){
										mater = null;
									}
									if(mater != null){
										materData = new MaterialData(mater);
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobBreakBlock.getKey() + " Break block type property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									jobBreakInfo.put(mater.toString()+subtype, new JobsBlockInfo(materData, experience, income));
								}
							}
						}
						else{
							// no break tag
							jobBreakInfo = null;
						}
						
						// place
						HashMap<String, JobsBlockInfo> jobPlaceInfo = null;
						if(jobInfoMap.containsKey("Place")){
							// place tag exists
							Map<String, Object> jobPlaceMap = (Map<String, Object>) jobInfoMap.get("Place");
							if(jobPlaceMap.size() == 0){
								// no place blocks detected
								jobPlaceInfo = null;
							}
							else{
								// has some break blocks
								for(Entry<String, Object> jobPlaceBlock: jobPlaceMap.entrySet()){
									String blockType = String.valueOf(jobPlaceBlock.getKey()).toUpperCase();
									String subtype = "";
									if(blockType.contains("-")){
										// uses subtype
										subtype = ":"+blockType.split("-")[1];
										blockType = blockType.split("-")[0];
									}
									Double income;
									Double experience;
									if (jobPlaceInfo == null){
										jobPlaceInfo = new HashMap<String, JobsBlockInfo>();
									}
									Map<String, Object> blockData = (Map<String, Object>) jobPlaceBlock.getValue();
									// income
									if(blockData.containsKey("income")){
										try{
											income = (Double) blockData.get("income");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("income");
												income = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobPlaceBlock.getKey() + " Place income property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobPlaceBlock.getKey() + " Place income property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									// experience
									if(blockData.containsKey("experience")){
										try{
											experience = (Double) blockData.get("experience");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("experience");
												experience = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobPlaceBlock.getKey() + " Place experience property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobPlaceBlock.getKey() + " Place experience property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									MaterialData materData;
									Material mater;
									try{
										mater = Material.matchMaterial(blockType);
									}
									catch (IllegalArgumentException ex){
										mater = null;
									}
									if(mater != null){
										materData = new MaterialData(mater);
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobPlaceBlock.getKey() + " Place block type property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									jobPlaceInfo.put(mater.toString()+subtype, new JobsBlockInfo(materData, experience, income));
								}
							}
						}
						else{
							// no place tag
							jobPlaceInfo = null;
						}
						
						// kill
						HashMap<String, JobsLivingEntityInfo> jobKillInfo = null;
						if(jobInfoMap.containsKey("Kill")){
							// kill tag exists
							Map<String, Object> jobKillMap = (Map<String, Object>) jobInfoMap.get("Kill");
							if(jobKillMap.size() == 0){
								// no kill entities detected
								jobKillInfo = null;
							}
							else{
								// has some kill entities
								for(Entry<String, Object> jobKillEntity: jobKillMap.entrySet()){
									String entityType = jobKillEntity.getKey();
									// puts it in the correct case
									if(entityType.equalsIgnoreCase("pigzombie")){
										entityType = "PigZombie";
									}
									else{
										entityType = entityType.substring(0,1).toUpperCase() + entityType.substring(1).toLowerCase();
									}
									Double income;
									Double experience;
									if (jobKillInfo == null){
										jobKillInfo = new HashMap<String, JobsLivingEntityInfo>();
									}
									Map<String, Object> blockData = (Map<String, Object>) jobKillEntity.getValue();
									// income
									if(blockData.containsKey("income")){
										try{
											income = (Double) blockData.get("income");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("income");
												income = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobKillEntity.getKey() + " Kill income property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobKillEntity.getKey() + " Kill income property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									// experience
									if(blockData.containsKey("experience")){
										try{
											experience = (Double) blockData.get("experience");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("experience");
												experience = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobKillEntity.getKey() + " Kill experience property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobKillEntity.getKey() + " Kill experience property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									Class victim;
									try {
										victim = Class.forName("org.bukkit.craftbukkit.entity.Craft"+entityType);
									} catch (ClassNotFoundException e) {
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobKillEntity.getKey() + " Kill entity type property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									jobKillInfo.put(("org.bukkit.craftbukkit.entity.Craft"+entityType).trim(), new JobsLivingEntityInfo(victim, experience, income));
								}
							}
						}
						else{
							// no break tag
							jobKillInfo = null;
						}
						
						// custom-kill  
						if(jobInfoMap.containsKey("custom-kill")){
							// kill tag exists
							Map<String, Object> jobKillMap = (Map<String, Object>) jobInfoMap.get("custom-kill");
							if(jobKillMap.size() == 0){
								// no kill entities detected
								jobKillInfo = null;
							}
							else{
								// has some kill entities
								for(Entry<String, Object> jobKillEntity: jobKillMap.entrySet()){
									String entityType = jobKillEntity.getKey();
									// puts it in the correct case
									Double income;
									Double experience;
									if (jobKillInfo == null){
										jobKillInfo = new HashMap<String, JobsLivingEntityInfo>();
									}
									Map<String, Object> blockData = (Map<String, Object>) jobKillEntity.getValue();
									// income
									if(blockData.containsKey("income")){
										try{
											income = (Double) blockData.get("income");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("income");
												income = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobKillEntity.getKey() + " custom-kill income property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobKillEntity.getKey() + " custom-kill income property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									// experience
									if(blockData.containsKey("experience")){
										try{
											experience = (Double) blockData.get("experience");
										}
										catch(ClassCastException e){
											try{
												int temp = (Integer) blockData.get("experience");
												experience = (double) temp;
											}
											catch (Exception ex){
												System.err.println("[Jobs] - Job " + jobMap.getKey() + " has an invalid " + jobKillEntity.getKey() + " custom-kill experience property. Disabling jobs!");
												Jobs.disablePlugin();
												return;
											}
										}
									}
									else{
										// error
										System.err.println("[Jobs] - Job " + jobMap.getKey() + " is missing " + jobKillEntity.getKey() + " custom-kill experience property. Disabling jobs!");
										Jobs.disablePlugin();
										return;
									}
									try {
										jobKillInfo.put(("org.bukkit.craftbukkit.entity.CraftPlayer:"+entityType).trim(), new JobsLivingEntityInfo(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer"), experience, income));
									} catch (ClassNotFoundException e) {
										// won't enter
										e.printStackTrace();
									}
								}
							}
						}
						jobs.put(jobName, new Job(jobBreakInfo, jobPlaceInfo, jobKillInfo, jobName, jobShortName, jobColour, maxExpEquation, incomeEquation, expEquation, displayMethod, maxLevel, maxSlots));
					}
				}
			}
			else{
				// property doesn't exist
				System.err.println("[Jobs] - Jobs property missing or invalid. Disabling Jobs!");
				Jobs.disablePlugin();
				return;
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("[Jobs] - configuration file jobConfig.yml does not exist");
			e.printStackTrace();
			// disable plugin
			Jobs.disablePlugin();
		}
	}
	
	/**
	 * Method to load the title configuration
	 * 
	 * loads from Jobs/titleConfig.yml
	 */
	@SuppressWarnings({"unchecked" })
	private void loadTitleSettings(){
		try {
			Yaml yaml = new Yaml();
			Object obj = yaml.load(new FileInputStream("plugins/Jobs/titleConfig.yml"));
			Map<String, Object> map = (Map<String, Object>)obj;
			if(map.containsKey("Titles")){
				Map<String, Object> titlesMap = (Map<String, Object>)map.get("Titles");
				if(titlesMap.size() == 0){
					// no titles found
					System.err.println("[Jobs] - No titles found. Disabling titles");
					titles = null;
				}
				else {
					titles = new TreeMap<Integer, Title>();
					// titles are found :)
					for(Entry<String, Object> individualTitle: titlesMap.entrySet()){
						String titleName;
						String titleShortName;
						ChatColor colour;
						Integer levelReq;
						Map<String, Object> individualTitleMap = (Map<String, Object>)individualTitle.getValue();
						// long name
						if(individualTitleMap.containsKey("Name")){
							try{
								titleName = (String) individualTitleMap.get("Name");
							}
							catch(Exception e){
								System.err.println("[Jobs] - Title " + individualTitle.getKey() + " has an invalid Name property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Title " + individualTitle.getKey() + " is missing the Name property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// short name
						if(individualTitleMap.containsKey("ShortName")){
							try{
								titleShortName = (String) individualTitleMap.get("ShortName");
							}
							catch(Exception e){
								System.err.println("[Jobs] - Title " + individualTitle.getKey() + " has an invalid ShortName property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Title " + individualTitle.getKey() + " is missing the ShortName property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						// chat colour
						if(individualTitleMap.containsKey("ChatColour")){
							try{
								colour = ChatColor.valueOf((String) individualTitleMap.get("ChatColour"));
							}
							catch(IllegalArgumentException e){
								System.err.println("[Jobs] - Title " + individualTitle.getKey() + " has an invalid ChatColour property. Disabling jobs !");
								Jobs.disablePlugin();
								return;
							}
						}
						else{
							System.err.println("[Jobs] - Title " + individualTitle.getKey() + " is missing the ChatColour property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						
						// level requirement
						if(individualTitleMap.containsKey("levelReq")){
							try{
								levelReq = (Integer) individualTitleMap.get("levelReq");
							}
							catch(ClassCastException e){
								try{
									double temp = (Double) individualTitleMap.get("levelReq");
									levelReq = (int) temp;
								}
								catch (Exception ex){
									System.err.println("[Jobs] - Title " + individualTitle.getKey() + " has an invalid levelReq property. Disabling jobs !");
									Jobs.disablePlugin();
									return;
								}
							}
						}
						else{
							System.err.println("[Jobs] - Title " + individualTitle.getKey() + " is missing the levelReq property. Disabling jobs !");
							Jobs.disablePlugin();
							return;
						}
						
						// all loaded nicely
						titles.put(levelReq, new Title(titleName, titleShortName, colour, levelReq));
					}
				}
			}
			else{
				// missing
				System.err.println("[Jobs] - Titles property missing or invalid. Disabling titles");
				titles = null;
			}
		} catch (FileNotFoundException e) {
			// no titles detected
			titles = null;
			System.err.println("[Jobs] - configuration file titleConfig.yml does not exist, disabling titles");
		}
	}
	
	/**
	 * Method to load the message configuration
	 * 
	 * loads from Jobs/messageConfig.yml
	 */
	@SuppressWarnings("unchecked")
	private void loadMessageSettings(){
		try {
			Yaml yaml = new Yaml();
			Object obj = yaml.load(new FileInputStream("plugins/Jobs/messageConfig.yml"));
			messages = new HashMap<String, String>();
			for(Entry<String, String> temp: ((Map<String, String>) obj).entrySet()){
				String tempMessage = temp.getValue();
				tempMessage = tempMessage.replace("ChatColor.AQUA", ChatColor.AQUA.toString());
				tempMessage = tempMessage.replace("ChatColor.BLACK", ChatColor.BLACK.toString());
				tempMessage = tempMessage.replace("ChatColor.BLUE", ChatColor.BLUE.toString());
				tempMessage = tempMessage.replace("ChatColor.DARK_AQUA", ChatColor.DARK_AQUA.toString());
				tempMessage = tempMessage.replace("ChatColor.DARK_BLUE", ChatColor.DARK_BLUE.toString());
				tempMessage = tempMessage.replace("ChatColor.DARK_GRAY", ChatColor.DARK_GRAY.toString());
				tempMessage = tempMessage.replace("ChatColor.DARK_GREEN", ChatColor.DARK_GREEN.toString());
				tempMessage = tempMessage.replace("ChatColor.DARK_PURPLE", ChatColor.DARK_PURPLE.toString());
				tempMessage = tempMessage.replace("ChatColor.DARK_RED", ChatColor.DARK_RED.toString());
				tempMessage = tempMessage.replace("ChatColor.GOLD", ChatColor.GOLD.toString());
				tempMessage = tempMessage.replace("ChatColor.GRAY", ChatColor.GRAY.toString());
				tempMessage = tempMessage.replace("ChatColor.GREEN", ChatColor.GREEN.toString());
				tempMessage = tempMessage.replace("ChatColor.LIGHT_PURPLE", ChatColor.LIGHT_PURPLE.toString());
				tempMessage = tempMessage.replace("ChatColor.RED", ChatColor.RED.toString());
				tempMessage = tempMessage.replace("ChatColor.WHITE", ChatColor.WHITE.toString());
				tempMessage = tempMessage.replace("ChatColor.YELLOW", ChatColor.YELLOW.toString());
				messages.put(temp.getKey(), tempMessage);
			}
		} catch (FileNotFoundException e) {
			System.err.println("[Jobs] - configuration file messageConfig.yml does not exist");
			e.printStackTrace();
			// disable plugin
			Jobs.disablePlugin();
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
		return jobs.get(jobName);
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
	public String getMessage(String key){
		return messages.get(key);
	}
	
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
}
