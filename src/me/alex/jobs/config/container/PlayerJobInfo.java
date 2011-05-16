package me.alex.jobs.config.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import me.alex.jobs.Jobs;
import me.alex.jobs.config.JobsConfiguration;
import me.alex.jobs.dao.JobsDAO;
import me.alex.jobs.dao.container.JobsDAOData;
import me.alex.jobs.event.JobsLevelUpEvent;
import me.alex.jobs.event.JobsSkillUpEvent;
import me.alex.jobs.util.DisplayMethod;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerJobInfo {
	// the player the object belongs to
	private Player player;
	// list of all jobs that the player does
	private List<Job> jobs;
	// progression of the player in each job
	private HashMap<Job, JobProgression> progression;
	// display honorific
	private String honorific;
		
	/**
	 * Constructor.
	 * Reads data storage and configures itself.
	 * @param player - the player that has just logged in
	 * @param dao - the data access object
	 */
	public PlayerJobInfo(Player player, JobsDAO dao){
		// set player link
		this.player = player;
		this.jobs = new ArrayList<Job>();
		this.progression = new HashMap<Job, JobProgression>();
		// for all jobs players have
		List<JobsDAOData> list = dao.getAllJobs(player);
		if(list != null){
			for(JobsDAOData job: list){
				if(JobsConfiguration.getInstance().getJob(job.getJobName()) != null){
					// add the job
					jobs.add(JobsConfiguration.getInstance().getJob(job.getJobName()));
					
					// create the progression object
					JobProgression jobProgression = 
						new JobProgression(JobsConfiguration.getInstance().getJob(job.getJobName()), job.getExperience(),job.getLevel(), this);
					// calculate the max level
					
					// add the progression level.
					progression.put(jobProgression.getJob(), jobProgression);
				}
			}
		}
		reloadMaxExperience();
		honorific = getDisplayHonorific();
		if(!honorific.equals(" ")){
			player.setDisplayName(honorific + player.getDisplayName());
		}
	}
	
	/**
	 * Broke a block.
	 * 
	 * Give correct experience and income
	 * 
	 * @param block - the block broken
	 */
	public void broke(Block block){
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> temp: progression.entrySet()){
			// add the current level to the parameter list
			param.put("joblevel", (double)temp.getValue().getLevel());
			// get the income and give it
			Double income = temp.getKey().getBreakIncome(block, param);
			if(income != null){
				JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
				temp.getValue().addExp(temp.getKey().getBreakExp(block, param));
				checkLevels();
			}
			param.remove("joblevel");
		}
	}
	
	/**
	 * Placed a block.
	 * 
	 * Give correct experience and income
	 * 
	 * @param block - the block placed
	 */
	public void placed(Block block){
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> temp: progression.entrySet()){
			// add the current level to the parameter list
			param.put("joblevel", (double)temp.getValue().getLevel());
			// get the income and give it
			Double income = temp.getKey().getPlaceIncome(block, param);
			if(income != null){
				// give income
				JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
				temp.getValue().addExp(temp.getKey().getPlaceExp(block, param));
				checkLevels();
			}
		}
	}
	
	/**
	 * Killed a living entity or owned wolf killed living entity.
	 * 
	 * Give correct experience and income
	 * 
	 * @param mob - the mob killed
	 */
	public void killed(LivingEntity mob){
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> temp: progression.entrySet()){
			// add the current level to the parameter list
			param.put("joblevel", (double)temp.getValue().getLevel());
			// get the income and give it
			Double income = temp.getKey().getKillIncome(mob, param);
			if(income != null){
				// give income
				JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
				temp.getValue().addExp(temp.getKey().getKillExp(mob, param));
				checkLevels();	
			}
		}
	}	
	
	/**
	 * Get the list of jobs
	 * @return the list of jobs
	 */
	public List<Job> getJobs(){
		return jobs;
	}
	
	/**
	 * Get the list of job progressions
	 * @return the list of job progressions
	 */
	public Collection<JobProgression> getJobsProgression(){
		return progression.values();
	}
	
	/**
	 * get the player
	 * @return the player
	 */
	public Player getPlayer(){
		return player;
	}
	
	/**
	 * Function called to update the levels and make sure experience < maxExperience
	 */
	public void checkLevels(){
		for(JobProgression temp: progression.values()){
			if(temp.canLevelUp()){
				// user would level up, call the joblevelupevent
				if(Jobs.getJobsServer() != null){
					JobsLevelUpEvent event = new JobsLevelUpEvent(player, temp, this);
					Jobs.getJobsServer().getPluginManager().callEvent(event);
				}
			}
			if(!JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()).equals(temp.getTitle())){
				// user would skill up
				if(Jobs.getJobsServer() != null){
					JobsSkillUpEvent event = new JobsSkillUpEvent(player, temp, JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()));
					Jobs.getJobsServer().getPluginManager().callEvent(event);
				}
			}
		}
	}
	
	public String getDisplayHonorific(){	
		String honorific = "";		
		
		
		if(jobs.size() > 1){
			// has more than 1 job - using shortname mode
			for(JobProgression temp: progression.values()){
				if(temp.getJob().getDisplayMethod().equals(DisplayMethod.FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.TITLE)){
					// add title to honorific
					if(temp.getTitle() != null){
						honorific += temp.getTitle().getChatColor() + temp.getTitle().getShortName() + ChatColor.WHITE;
					}
				}
				
				if(temp.getJob().getDisplayMethod().equals(DisplayMethod.FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.JOB)){
					honorific += temp.getJob().getJobChatColour() + temp.getJob().getJobShortName() + ChatColor.WHITE;
				}
				
				if(!temp.getJob().getDisplayMethod().equals(DisplayMethod.NONE)){
					honorific+=" ";
				}
			}
		}
		else if (jobs.size() > 0){
			// has only 1 job, using longname mode
			if(jobs.get(0).getDisplayMethod().equals(DisplayMethod.FULL) || jobs.get(0).getDisplayMethod().equals(DisplayMethod.TITLE)){
				// add title to honorific
				if(progression.get(jobs.get(0)).getTitle() != null){
					honorific += progression.get(jobs.get(0)).getTitle().getChatColor() + progression.get(jobs.get(0)).getTitle().getName() + ChatColor.WHITE;
				}
				if(jobs.get(0).getDisplayMethod().equals(DisplayMethod.FULL)){
					honorific += " ";
				}
			}
			
			if(jobs.get(0).getDisplayMethod().equals(DisplayMethod.FULL) || jobs.get(0).getDisplayMethod().equals(DisplayMethod.JOB)){
				honorific += jobs.get(0).getJobChatColour() + jobs.get(0).getJobName() + ChatColor.WHITE;
			}
		}
		return honorific.trim() + " ";
	}
	
	/**
	 * Player joins a job
	 * @param job - the job joined
	 */
	public void joinJob(Job job){
		jobs.add(job);
		progression.put(job, new JobProgression(job, 0.0, 1, this));
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void leaveJob(Job job){
		jobs.remove(job);
		progression.remove(job);
	}
	
	/**
	 * Checks if the player is in this job.
	 * @param job - the job
	 * @return true - they are in the job
	 * @return false - they are not in the job
	 */
	public boolean isInJob(Job job){
		return jobs.contains(job);
	}
	
	/**
	 * Function that reloads your honorific
	 */
	public void reloadHonorific(){
		String newHonorific = getDisplayHonorific();
		if(honorific.equals(" ")){
			player.setDisplayName(newHonorific + player.getDisplayName());
		}
		else{
			if(newHonorific.equals(" ")){
				player.setDisplayName(player.getDisplayName().replace(honorific, ""));
			}
			else{
				player.setDisplayName(player.getDisplayName().replace(honorific, newHonorific));
			}
		}
		honorific = newHonorific;
	}
	
	/**
	 * Function to reload all of the maximum experiences
	 */
	public void reloadMaxExperience(){
		HashMap<String, Double> param = new HashMap<String, Double>();
		param.put("numjobs", (double) progression.size());
		for(JobProgression temp: progression.values()){
			param.put("level", (double) temp.getLevel());
			temp.setMaxExperience(temp.getJob().getMaxExp(param));
			param.remove("level");
		}
	}
}
