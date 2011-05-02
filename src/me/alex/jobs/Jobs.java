package me.alex.jobs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TreeMap;
import java.util.logging.Logger;

import me.alex.jobs.dao.FlatFileJobsDAO;
import me.alex.jobs.dao.JobsDAO;
import me.alex.jobs.dao.MySQLJobsDAO;
import me.alex.jobs.dao.SqliteJobsDAO;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import com.nidefawl.Stats.Stats;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;

import cosine.boseconomy.BOSEconomy;

public class Jobs extends JavaPlugin{
	private static final Logger log = Logger.getLogger("Minecraft");
	
	private final JobsPlayerListener playerListener = new JobsPlayerListener(this);
	private final JobsBlockListener blockListener =  new JobsBlockListener(this);
	private final JobsEntityListener entityListener = new JobsEntityListener(this);
	private final HashMap<Player, Job> players = new HashMap<Player, Job>();
	private JobsDAO dao = null;
	
	// possible jobs and their configurations. <Job, <Material, base cost>>
	private HashMap<String, HashMap<Material, Double>> jobConfigurationsBreak = 
		new HashMap<String, HashMap<Material, Double>>();
	private HashMap<String, HashMap<Material, Double>> jobConfigurationsPlace = 
		new HashMap<String, HashMap<Material, Double>>();
	private HashMap<String, HashMap<Class, Double>> jobConfigurationsKill = 
		new HashMap<String, HashMap<Class, Double>>();
	private HashMap<String, ChatColor> jobColours =  new HashMap<String, ChatColor>();
	private HashMap<String, Double> levelingProgressionRate =
		new HashMap<String, Double>();
	private HashMap<String, Double> incomeProgressionRate =
		new HashMap<String, Double>();
	
	private TreeMap<Integer, Title> titles = new TreeMap<Integer, Title>();
	
	private int displayLevel = 0;
	private boolean broadcast = false;
	private boolean debugEnabled = false;
	private String debugLog = "plugins/Jobs/jobs.log";
	private PrintWriter jobsLog = null;
	private double flatRate = 1;
	
	// for iConomy
	private PluginListener pluginListener = null;
    private Server server = null;
    
    private Stats stats = null;
    
    private BOSEconomy boseconomy = null;
    private iConomy iConomy = null;

    private PermissionHandler permissions = null;
    
    private int baseXp = 100;
    private double xpMultiplyer = 1.0;
    
    // autosave
    private Timer saveTimer = null;
	
	@SuppressWarnings("unchecked")
	public void onEnable(){
		try{
			Yaml yaml = new Yaml();
			Object obj = null;
			try {
				obj = yaml.load(new FileInputStream("plugins/Jobs/configuration.yml"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, Object> map = (Map<String, Object>)obj;
			
			if(debugEnabled){
				try {
					jobsLog = new PrintWriter(new FileOutputStream(debugLog, true));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(((String)map.get("storage-method")).equals("MySQL")){
				dao = new MySQLJobsDAO(this, 
						(String)map.get("mysql-database"), 
						(String)map.get("mysql-url"), 
						(String)map.get("mysql-username"), 
						(String)map.get("mysql-password"));
			}
			else if (((String)map.get("storage-method")).equals("sqlite")){
				dao = new SqliteJobsDAO(this, (String)map.get("sqlite-database"));
			}
			else if(((String)map.get("storage-method")).equals("flatfile")){
				dao = new FlatFileJobsDAO("plugins/Jobs/jobs.data", this);
			}
			
			if (((String)map.get("chat-display")).equals("full")){
				displayLevel = 3;
			}
			else if(((String)map.get("chat-display")).equals("job")){
				displayLevel = 2;
			}
			else if (((String)map.get("chat-display")).equals("skill")){
				displayLevel = 1;
			}
			else if(((String)map.get("chat-display")).equals("none")){
				displayLevel = 0;
			}
			
			broadcast = (Boolean)map.get("broadcast-skill-up");
			flatRate = (Double)map.get("flat-rate-payout");
			baseXp = (Integer)map.get("base-exp");
			xpMultiplyer = (Double)map.get("xp-multiplyer");
							
			// load up possible jobs
			Map<String, Object> jobMap = (Map<String, Object>)map.get("Jobs");
					
			// for each job detected
			for(String jobType: jobMap.keySet()){
				HashMap<Material, Double> MapBreak = new HashMap<Material, Double>();
				HashMap<Material, Double> MapPlace = new HashMap<Material, Double>();
				HashMap<Class, Double> MapKill = new HashMap<Class, Double>();
				
				// chat colour
				jobColours.put(jobType, ChatColor.valueOf((String)((Map<String, Object>)jobMap.get(jobType)).get("ChatColour")));
				// leveling progression rate
				levelingProgressionRate.put(jobType, (Double)((Map<String, Object>)jobMap.get(jobType)).get("levelingProgressionRate"));
				// income progression rate
				incomeProgressionRate.put(jobType, (Double)((Map<String, Object>)jobMap.get(jobType)).get("incomeProgressionRate"));
	
				
				// break blocks
				if((boolean)((Map<String, Object>)jobMap.get(jobType)).containsKey("Break")){
					Map<String, Double> breakMap = (Map<String, Double>)((Map<String, Object>)jobMap.get(jobType)).get("Break");
					if(breakMap != null){
						for(String name: breakMap.keySet()){
							MapBreak.put(Material.getMaterial(name), breakMap.get(name));
						}
					}
				}
				
				// place blocks
				if((boolean)((Map<String, Object>)jobMap.get(jobType)).containsKey("Place")){
					Map<String, Double> placeMap = (Map<String, Double>)((Map<String, Object>)jobMap.get(jobType)).get("Place");
					if(placeMap != null){
						for(String name: placeMap.keySet()){
							MapPlace.put(Material.getMaterial(name), placeMap.get(name));
						}
					}
				}
				
				// kills
				if((boolean)((Map<String, Object>)jobMap.get(jobType)).containsKey("Kill")){
					Map<String, Double> killMap = (Map<String, Double>)((Map<String, Object>)jobMap.get(jobType)).get("Kill");
					if(killMap != null){
						for(String name: killMap.keySet()){
							MapKill.put(Class.forName("org.bukkit.craftbukkit.entity.Craft"+name), killMap.get(name));
						}
					}
				}
				
				
				jobConfigurationsBreak.put(jobType, MapBreak);
				jobConfigurationsPlace.put(jobType, MapPlace);
				jobConfigurationsKill.put(jobType, MapKill);
			}
			
			// load up possible titles
			Map<String, Object> titlesMap = (Map<String, Object>)map.get("Titles");
					
			// for each title detected
			for(String title: titlesMap.keySet()){
				Map<String, Object> titleInfoMap = (Map<String, Object>)titlesMap.get(title);
				titles.put((Integer)titleInfoMap.get("levelReq"), 
						new Title(title, 
								ChatColor.valueOf((String)titleInfoMap.get("ChatColour")),
								(Integer)titleInfoMap.get("levelReq")));
			}
			
			if((Integer)map.get("save-period")>0){
				saveTimer = new Timer();
				saveTimer.scheduleAtFixedRate(new SaveScheduler(this), 
						60000*(Integer)map.get("save-period"), 
						60000*(Integer)map.get("save-period"));
			}
			
			PluginManager pm = getServer().getPluginManager();
			pluginListener = new PluginListener(this);
			server = getServer();
			pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.Monitor, this);
			pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
			
			for(Player online: getServer().getOnlinePlayers()){
				addPlayer(online);
			}
			
			log.info("Jobs started");		
		}
		catch (Exception e){
			e.printStackTrace();
			log.info("Jobs not started, there's an error with the configuration");
		}
	}
	
	public void onDisable(){
		log.info("Jobs stopped");
		for(Player player: players.keySet()){
			dao.saveJob(player, players.get(player));
			players.get(player).stripTitle();
		}
		players.clear();
		if(jobsLog != null){
			jobsLog.close();
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(commandLabel.equalsIgnoreCase("jobs")){
			Player player = (Player)sender;
			// join path
			if(args.length >=1){
				if(args[0].equalsIgnoreCase("stats")){
					if(players.get(player)!= null){
						players.get(player).showStats(player);
					}
					else {
						player.sendMessage(ChatColor.RED + "You have not yet chosen a job.");
					}
					return true;
				}
				else if(args[0].equalsIgnoreCase("browse")){
					player.sendMessage("The following jobs are available:");
					for(String job: jobConfigurationsBreak.keySet()){
						if(permissions != null){
							if(permissions.has(player, "jobs.job." + job)){
								player.sendMessage(job);
							}
						}
						else {
							player.sendMessage(job);
						}
					}
					player.sendMessage("For more information on each job type /jobs info [Job]");
					return true;
				}
			}
			if(args.length >= 2){
				String jobName = "";
				for(int i=1; i<args.length; ++i){
					jobName += args[i] + " ";
				}
				jobName = jobName.trim();
				if(args[0].equalsIgnoreCase("join")){
					// if job selected is in possible jobs, add them to it.
					if(jobConfigurationsBreak.containsKey(jobName)){
						if(permissions != null){
							// if they are allowed to join, add them to it.
							if(permissions.has(player, "jobs.job." + jobName)){
								if(players.containsKey(player)){
									removePlayer(player);
								}
								dao.changeJob(player, jobName);
								addPlayer(player);
								player.sendMessage("You are now a " + jobName);
								return true;
							}
							else{
								player.sendMessage(ChatColor.RED + "You do not have permission to join this job");
								return true;
							}
						}
						else {
							if(players.containsKey(player)){
								removePlayer(player);
							}
							dao.changeJob(player, jobName);
							addPlayer(player);
							player.sendMessage("You are now a " + jobName);
							return true;
						}
					}
				}
				else if (args[0].equalsIgnoreCase("info")){
					if(jobConfigurationsBreak.containsKey(args[1])){
						player.sendMessage(args[1]);
						player.sendMessage("-----------------------");
						player.sendMessage("Base Salaries:");
						if(!jobConfigurationsBreak.get(args[1]).isEmpty()){
							player.sendMessage("  Harvest:");
							for(Entry<Material, Double> entry: jobConfigurationsBreak.get(args[1]).entrySet()){
								String item = entry.getKey().name().toLowerCase();
								item = item.replace('_', ' ');
								player.sendMessage("    " + item + " : " + entry.getValue());
							}
						}
						if(!jobConfigurationsPlace.get(args[1]).isEmpty()){
							player.sendMessage("  Place:");
							for(Entry<Material, Double> entry: jobConfigurationsPlace.get(args[1]).entrySet()){
								String item = entry.getKey().name().toLowerCase();
								item = item.replace('_', ' ');
								player.sendMessage("    " + item + " : " + entry.getValue());
							}
						}
						if(!jobConfigurationsKill.get(args[1]).isEmpty()){
							player.sendMessage("  Kill:");
							for(Entry<Class, Double> entry: jobConfigurationsKill.get(args[1]).entrySet()){
								Class item = entry.getKey();
								player.sendMessage("    " + item.getSimpleName().replace("Craft", "") + " : " + entry.getValue());
							}
						}
						return true;
					}
				}
			}
			player.sendMessage("Welcome to the Jobs plugin");
			player.sendMessage("----------------------------------------------------");
			player.sendMessage("/jobs stats      - view your current job stats;");
			player.sendMessage("/jobs browse     - shows all of the jobs available");
			player.sendMessage("/jobs join [Job] - join the job");
			player.sendMessage("/jobs info [Job] - view job information");
		}
		return true;
	}
	
	public void addPlayer(Player player){
		Job playerJob = dao.findPlayer(player);
		if(playerJob != null){
			players.put(player, playerJob);
		}
	}
	

	public void removePlayer(Player player){
		if(players.containsKey(player)){
			players.get(player).stripTitle();
			dao.saveJob(player, players.get(player));
			players.remove(player);
		}
	}
	
	public Server getBukkitServer(){
		return server;
	}
	
	public iConomy getiConomy(){
		return iConomy;
	}
	
	public boolean setiConomy(iConomy plugin){
		if(iConomy == null){
			iConomy = plugin;
		}
		else {
			return false;
		}
		return true;
	}
	
	public BOSEconomy getBOSEconomy(){
		return boseconomy;
	}
	
	public boolean setBOSEconomy(BOSEconomy plugin){
		if(boseconomy == null){
			boseconomy = plugin;
		}
		else {
			return false;
		}
		return true;
	}
	
	public PermissionHandler getPermissions(){
		return permissions;
	}
	
	public boolean setPermissions(PermissionHandler plugin){
		if(permissions == null){
			permissions = plugin;
		}
		else {
			return false;
		}
		return true;
	}
	
	public Stats getStats(){
		return stats;
	}
	
	public boolean setStats(Stats plugin){
		if(stats == null){
			stats = plugin;
		}
		else {
			return false;
		}
		return true;
	}
	
	public Job getJob(Player player){
		return players.get(player);
	}
	
	public HashMap<Material, Double> getBreakList(String job){
		return jobConfigurationsBreak.get(job);
	}
	
	public HashMap<Material, Double> getPlaceList(String job){
		return jobConfigurationsPlace.get(job);
	}
	
	public HashMap<Class, Double> getKillList(String job){
		return jobConfigurationsKill.get(job);
	}
	
	public void saveAll(){
		for(Entry<Player, Job> entry: players.entrySet()){
			dao.saveJob(entry.getKey(), entry.getValue());
		}
	}
	
	public double getLevelingRate(String job){
		return levelingProgressionRate.get(job);
	}
	
	public ChatColor getChatColour(String job){
		return jobColours.get(job);
	}
	
	public double getIncomeRate(String job){
		return incomeProgressionRate.get(job);
	}
	
	public int getDisplayLevel(){
		return displayLevel;
	}
	
	public boolean isBroadcasting(){
		return broadcast;
	}
	
	public void writeToLog(String output){
		if(debugEnabled){
			jobsLog.println(output);
		}
	}
	
	public double getFlateRatePayout(){
		return flatRate;
	}
	
	public Title getTitle(int level){
		Title correctTitle = null;
		for(Title title: titles.values()){
			if(correctTitle == null){
				if(title.getLevelReq() <= level){
					correctTitle = title;
				}
			}
			else {
				if(title.getLevelReq() <= level && 
						correctTitle.getLevelReq() < title.getLevelReq()){
					correctTitle = title;
				}
			}
		}
		return correctTitle;
	}
	
	public int getBaseXp(){
		return baseXp;
	}
	
	public double getXpMultiplier(){
		return xpMultiplyer;
	}
}
