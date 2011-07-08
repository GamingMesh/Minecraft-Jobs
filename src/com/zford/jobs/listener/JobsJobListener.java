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

package com.zford.jobs.listener;

import java.util.HashMap;

import com.nidefawl.Stats.Stats;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.JobsMessages;
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
				
				progression.setMaxExperience((int)progression.getJob().getMaxExp(param));
				
				String message = JobsMessages.getInstance().getMessage("level-up");
				message = message.replace("%jobname%", ""+progression.getJob().getName());
				message = message.replace("%jobcolour%", ""+progression.getJob().getChatColour());
				if(progression.getTitle() != null){
					message = message.replace("%titlename%", ""+progression.getTitle().getName());
					message = message.replace("%titlecolour%", ""+progression.getTitle().getChatColor());
				}
				message = message.replace("%playername%", ""+event.getPlayer().getName());
				message = message.replace("%playerdisplayname%", ""+event.getPlayer().getDisplayName());
				message = message.replace("%joblevel%", ""+progression.getLevel());
				for(String line: message.split("\n")){
					event.getPlayer().sendMessage(line);
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
    			String message = JobsMessages.getInstance().getMessage("at-max-level");
			
				for(String line: message.split("\n")){
					event.getPlayer().sendMessage(line);
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
				String message = JobsMessages.getInstance().getMessage("skill-up-broadcast");
				message = message.replace("%playername%", event.getPlayer().getName());
				if(event.getNewTitle() != null){
					message = message.replace("%titlecolour%", event.getNewTitle().getChatColor().toString());
					message = message.replace("%titlename%", event.getNewTitle().getName());
				}
				message = message.replace("%jobcolour%", event.getJobProgression().getJob().getChatColour().toString());
				message = message.replace("%jobname%", event.getJobProgression().getJob().getName());
				for(String line: message.split("\n")){
					event.getPlayer().sendMessage(line);
				}
			}
			else{
				String message = JobsMessages.getInstance().getMessage("skill-up-no-broadcast");
				if(event.getNewTitle() != null){
					message = message.replace("%titlecolour%", event.getNewTitle().getChatColor().toString());
					message = message.replace("%titlename%", event.getNewTitle().getName());
				}
				message = message.replace("%jobcolour%", event.getJobProgression().getJob().getChatColour().toString());
				message = message.replace("%jobname%", event.getJobProgression().getJob().getName());
				for(String line: message.split("\n")){
					event.getPlayer().sendMessage(line);
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
				String message = JobsMessages.getInstance().getMessage("join-job-success");
				message = message.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
				message = message.replace("%jobname%", event.getNewJob().getName());
				for(String line: message.split("\n")){
					event.getPlayer().sendMessage(line);
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
					String message = JobsMessages.getInstance().getMessage("join-job-failed-already-in");
					message = message.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
					message = message.replace("%jobname%", event.getNewJob().getName());
					for(String line: message.split("\n")){
						event.getPlayer().sendMessage(line);
					}
				}
				else{
					// you are already in too many jobs
					String message = JobsMessages.getInstance().getMessage("leave-job-failed-too-many");
					for(String line: message.split("\n")){
						event.getPlayer().sendMessage(line);
					}
				}
			}
		}
		else if (JobsConfiguration.getInstance().getUsedSlots(event.getNewJob()) >= event.getNewJob().getMaxSlots()){
			// already in job message
			String message = JobsMessages.getInstance().getMessage("join-job-failed-no-slots");
			message = message.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
			message = message.replace("%jobname%", event.getNewJob().getName());
			for(String line: message.split("\n")){
				event.getPlayer().sendMessage(line);
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
				String message = JobsMessages.getInstance().getMessage("leave-job-success");
				message = message.replace("%jobcolour%", event.getOldJob().getChatColour().toString());
				message = message.replace("%jobname%", event.getOldJob().getName());
				for(String line: message.split("\n")){
					event.getPlayer().sendMessage(line);
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
