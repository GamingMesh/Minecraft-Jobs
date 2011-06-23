package com.zford.jobs.listener;

import java.util.HashMap;


import org.bukkit.ChatColor;

import com.nidefawl.Stats.Stats;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.container.JobProgression;
import com.zford.jobs.config.container.PlayerJobInfo;
import com.zford.jobs.event.JobsEventListener;
import com.zford.jobs.event.JobsJoinEvent;
import com.zford.jobs.event.JobsLeaveEvent;
import com.zford.jobs.event.JobsLevelUpEvent;
import com.zford.jobs.event.JobsSkillUpEvent;
import com.zford.jobs.fake.JobsPlayer;

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
				String tempMessage = JobsConfiguration.getInstance().getMessage("level-up");
				if(tempMessage == null){
					event.getPlayer().sendMessage(ChatColor.YELLOW + "-- Job Level Up --");
				}
				else {
					tempMessage = tempMessage.replace("%jobname%", ""+progression.getJob().getName());
					tempMessage = tempMessage.replace("%jobcolour%", ""+progression.getJob().getChatColour());
					if(progression.getTitle() != null){
						tempMessage = tempMessage.replace("%titlename%", ""+progression.getTitle().getName());
						tempMessage = tempMessage.replace("%titlecolour%", ""+progression.getTitle().getChatColor());
					}
					tempMessage = tempMessage.replace("%playername%", ""+event.getPlayer().getName());
					tempMessage = tempMessage.replace("%playerdisplayname%", ""+event.getPlayer().getDisplayName());
					tempMessage = tempMessage.replace("%joblevel%", ""+progression.getLevel());
					for(String temp: tempMessage.split("\n")){
						event.getPlayer().sendMessage(temp);
					}
				}
				
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
				String tempMessage = JobsConfiguration.getInstance().getMessage("at-max-level");
				if(tempMessage == null){
					event.getPlayer().sendMessage(ChatColor.YELLOW + "-- You have reached the maximum level --");
				}
				else {
					for(String temp: tempMessage.split("\n")){
						event.getPlayer().sendMessage(temp);
					}
				}
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
				String tempMessage = JobsConfiguration.getInstance().getMessage("skill-up-broadcast");
				if(tempMessage == null){
					Jobs.getJobsServer().broadcastMessage(event.getPlayer().getName() + " has been promoted to a " +
							event.getNewTitle().getChatColor() + event.getNewTitle().getName() + ChatColor.WHITE + " " + 
							event.getJobProgression().getJob().getChatColour() + event.getJobProgression().getJob().getName() + ChatColor.WHITE);				}
				else {
					tempMessage = tempMessage.replace("%playername%", event.getPlayer().getName());
					if(event.getNewTitle() != null){
						tempMessage = tempMessage.replace("%titlecolour%", event.getNewTitle().getChatColor().toString());
						tempMessage = tempMessage.replace("%titlename%", event.getNewTitle().getName());
					}
					tempMessage = tempMessage.replace("%jobcolour%", event.getJobProgression().getJob().getChatColour().toString());
					tempMessage = tempMessage.replace("%jobname%", event.getJobProgression().getJob().getName());
					for(String temp: tempMessage.split("\n")){
						event.getPlayer().sendMessage(temp);
					}
				}
			}
			else{
				String tempMessage = JobsConfiguration.getInstance().getMessage("skill-up-no-broadcast");
				if(tempMessage == null){
					event.getPlayer().sendMessage("Congratulations, you have been promoted to a " + 
							event.getNewTitle().getChatColor() + event.getNewTitle().getName() + ChatColor.WHITE + " " + 
							event.getJobProgression().getJob().getChatColour() + event.getJobProgression().getJob().getName() + ChatColor.WHITE);
				}
				else {
					if(event.getNewTitle() != null){
						tempMessage = tempMessage.replace("%titlecolour%", event.getNewTitle().getChatColor().toString());
						tempMessage = tempMessage.replace("%titlename%", event.getNewTitle().getName());
					}
					tempMessage = tempMessage.replace("%jobcolour%", event.getJobProgression().getJob().getChatColour().toString());
					tempMessage = tempMessage.replace("%jobname%", event.getJobProgression().getJob().getName());
					for(String temp: tempMessage.split("\n")){
						event.getPlayer().sendMessage(temp);
					}
				}
			}
			event.getJobProgression().setTitle(event.getNewTitle());
			plugin.getJob(event.getPlayer()).reloadHonorific();
		}
	}
	
	@Override
	public void onJobJoin(JobsJoinEvent event) {
		if(!event.isCancelled() && 
				(event.getNewJob().getMaxSlots() == null ||  (JobsConfiguration.getInstance().getUsedSlots(event.getNewJob()) < event.getNewJob().getMaxSlots()))){
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
				JobsConfiguration.getInstance().takeSlot(event.getNewJob());
				String tempMessage = JobsConfiguration.getInstance().getMessage("join-job-success");
				if(tempMessage == null){
					event.getPlayer().sendMessage("You have joined the job " + event.getNewJob().getChatColour() + event.getNewJob().getName());
				}
				else {
					tempMessage = tempMessage.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
					tempMessage = tempMessage.replace("%jobname%", event.getNewJob().getName());
					for(String temp: tempMessage.split("\n")){
						event.getPlayer().sendMessage(temp);
					}
				}
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
					String tempMessage = JobsConfiguration.getInstance().getMessage("join-job-failed-already-in");
					if(tempMessage == null){
						event.getPlayer().sendMessage("You are already in the job " + event.getNewJob().getChatColour() + event.getNewJob().getName());

					}
					else {
						tempMessage = tempMessage.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
						tempMessage = tempMessage.replace("%jobname%", event.getNewJob().getName());
						for(String temp: tempMessage.split("\n")){
							event.getPlayer().sendMessage(temp);
						}
					}
				}
				else{
					// you are already in too many jobs
					String tempMessage = JobsConfiguration.getInstance().getMessage("leave-job-failed-too-many");
					if(tempMessage == null){
						event.getPlayer().sendMessage("You have already joined too many jobs.");

					}
					else {
						for(String temp: tempMessage.split("\n")){
							event.getPlayer().sendMessage(temp);
						}
					}
				}
			}
		}
		else if (JobsConfiguration.getInstance().getUsedSlots(event.getNewJob()) >= event.getNewJob().getMaxSlots()){
			// already in job message
			String tempMessage = JobsConfiguration.getInstance().getMessage("join-job-failed-no-slots");
			if(tempMessage == null){
				event.getPlayer().sendMessage("You cannot join the job " + event.getNewJob().getChatColour() + event.getNewJob().getName() + ChatColor.WHITE + ", there are no slots available.");

			}
			else {
				tempMessage = tempMessage.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
				tempMessage = tempMessage.replace("%jobname%", event.getNewJob().getName());
				for(String temp: tempMessage.split("\n")){
					event.getPlayer().sendMessage(temp);
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
				JobsConfiguration.getInstance().leaveSlot(event.getOldJob());
				String tempMessage = JobsConfiguration.getInstance().getMessage("leave-job-success");
				if(tempMessage == null){
					event.getPlayer().sendMessage("You have left the job " + event.getOldJob().getChatColour() + event.getOldJob().getName());

				}
				else {
					tempMessage = tempMessage.replace("%jobcolour%", event.getOldJob().getChatColour().toString());
					tempMessage = tempMessage.replace("%jobname%", event.getOldJob().getName());
					for(String temp: tempMessage.split("\n")){
						event.getPlayer().sendMessage(temp);
					}
				}
				if(!(event.getPlayer() instanceof JobsPlayer)){
					plugin.getJob(event.getPlayer()).reloadHonorific();
					plugin.getJob(event.getPlayer()).reloadMaxExperience();
					plugin.getJob(event.getPlayer()).checkLevels();
				}
			}
		}
	}
}
