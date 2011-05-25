package me.alex.jobs.listener;

import java.util.HashMap;

import me.alex.jobs.Jobs;
import me.alex.jobs.config.JobsConfiguration;
import me.alex.jobs.config.container.JobProgression;
import me.alex.jobs.config.container.PlayerJobInfo;
import me.alex.jobs.event.JobsEventListener;
import me.alex.jobs.event.JobsJoinEvent;
import me.alex.jobs.event.JobsLeaveEvent;
import me.alex.jobs.event.JobsLevelUpEvent;
import me.alex.jobs.event.JobsSkillUpEvent;
import me.alex.jobs.fake.JobsPlayer;

import org.bukkit.ChatColor;

import com.nidefawl.Stats.Stats;

/**
 * Job listener for doing the default job listening things.
 * @author Alex
 *
 */
public class JobsJobListener extends JobsEventListener{
	Jobs plugin;
	
	public JobsJobListener(Jobs plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onJobLevelUp(JobsLevelUpEvent event) {
		if(!event.isCancelled()){
			
			if(event.getJobProgression().getJob().getMaxLevel() == null ||
					event.getJobProgression().getLevel() < event.getJobProgression().getJob().getMaxLevel()){
				JobProgression progression = event.getJobProgression();
				// increase the level
				progression.setLevel(progression.getLevel()+1);
				// decrease the current exp
				progression.setExperience(progression.getExperience()-progression.getMaxExperience());
				// recalculate the maxexp 
				HashMap<String, Double> param = new HashMap<String, Double>();
				param.put("numjobs", (double)event.getNumJobs());
				param.put("joblevel", (double)progression.getLevel());
				
				progression.setMaxExperience(progression.getJob().getMaxExp(param));
				
				// TODO customizable message
				event.getPlayer().sendMessage(ChatColor.YELLOW + "-- Job Level Up --");
				
				// stats plugin integration
				if(JobsConfiguration.getInstance().getStats() != null &&
						JobsConfiguration.getInstance().getStats().isEnabled()){
					Stats stats = JobsConfiguration.getInstance().getStats();
					if(progression.getLevel() > stats.get(event.getPlayer().getName(), "job", progression.getJob().getName())){
						stats.setStat(event.getPlayer().getName(), "job", progression.getJob().getName(), progression.getLevel());
						stats.saveAll();
					}
				}
				plugin.getPlayerJobInfo(event.getPlayer()).checkLevels();
			}
			else{
				event.getJobProgression().setExperience(0.0);
				event.getPlayer().sendMessage(ChatColor.YELLOW + "-- You have reached the maximum level --");
			}
		}
	}
	
	@Override
	public void onJobSkillUp(JobsSkillUpEvent event) {
		if(!event.isCancelled()){
			// set new title
			
			event.getJobProgression().setTitle(event.getNewTitle());
			
			//broadcast
			if(JobsConfiguration.getInstance().isBroadcasting()){
				Jobs.getJobsServer().broadcastMessage(event.getPlayer().getName() + " has been promoted to a " +
						event.getNewTitle().getChatColor() + event.getNewTitle().getName() + ChatColor.WHITE + " " + 
						event.getJobProgression().getJob().getChatColour() + event.getJobProgression().getJob().getName() + ChatColor.WHITE);
			}
			else{
				event.getPlayer().sendMessage("Congratulations, you have been promoted to a " + 
						event.getNewTitle().getChatColor() + event.getNewTitle().getName() + ChatColor.WHITE + " " + 
						event.getJobProgression().getJob().getChatColour() + event.getJobProgression().getJob().getName() + ChatColor.WHITE);
			}
			event.getJobProgression().setTitle(event.getNewTitle());
			plugin.getJob(event.getPlayer()).reloadHonorific();
		}
	}
	
	@Override
	public void onJobJoin(JobsJoinEvent event) {
		if(!event.isCancelled()){
			// check if the user has already joined the job
			PlayerJobInfo info = plugin.getPlayerJobInfo(event.getPlayer());
			// offline
			if(info == null){
				info = new PlayerJobInfo(event.getPlayer(), JobsConfiguration.getInstance().getJobsDAO());
			}
			if(!info.isInJob(event.getNewJob())){
				// let the user join the job
				info.joinJob(event.getNewJob());
				JobsConfiguration.getInstance().getJobsDAO().joinJob(event.getPlayer(), event.getNewJob());
				event.getPlayer().sendMessage("You have joined the job " + event.getNewJob().getChatColour() + event.getNewJob().getName());
				if(!(event.getPlayer() instanceof JobsPlayer)){
					// if it's a real player
					plugin.getJob(event.getPlayer()).reloadHonorific();
					plugin.getJob(event.getPlayer()).reloadMaxExperience();
				}
				
				// stats plugin integration
				if(JobsConfiguration.getInstance().getStats() != null &&
						JobsConfiguration.getInstance().getStats().isEnabled()){
					Stats stats = JobsConfiguration.getInstance().getStats();
					if(1 > stats.get(event.getPlayer().getName(), "job", event.getNewJob().getName())){
						stats.setStat(event.getPlayer().getName(), "job", event.getNewJob().getName(), 1);
						stats.saveAll();
					}
				}
			}
			else {
				if(info.isInJob(event.getNewJob())){
					// already in job message
				}
				else{
					// you are already in too many jobs
				}
			}
		}
	}
	
	@Override
	public void onJobLeave(JobsLeaveEvent event) {
		if(!event.isCancelled()){
			// check if the user has already joined the job
			PlayerJobInfo info = plugin.getPlayerJobInfo(event.getPlayer());
			// offline
			if(info == null){
				info = new PlayerJobInfo(event.getPlayer(), JobsConfiguration.getInstance().getJobsDAO());
			}
			if(info.isInJob(event.getOldJob())){
				// let the user join the job
				info.leaveJob(event.getOldJob());
				JobsConfiguration.getInstance().getJobsDAO().quitJob(event.getPlayer(), event.getOldJob());
				event.getPlayer().sendMessage("You have left the job " + event.getOldJob().getChatColour() + event.getOldJob().getName());
				if(!(event.getPlayer() instanceof JobsPlayer)){
					plugin.getJob(event.getPlayer()).reloadHonorific();
					plugin.getJob(event.getPlayer()).reloadMaxExperience();
					plugin.getJob(event.getPlayer()).checkLevels();
				}
			}
		}
	}
}
