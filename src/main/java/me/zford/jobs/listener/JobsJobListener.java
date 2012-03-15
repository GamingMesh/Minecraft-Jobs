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

package me.zford.jobs.listener;

import java.util.HashMap;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.JobProgression;
import me.zford.jobs.event.JobsJoinEvent;
import me.zford.jobs.event.JobsLeaveEvent;
import me.zford.jobs.event.JobsLevelUpEvent;
import me.zford.jobs.event.JobsSkillUpEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;


/**
 * Job listener for doing the default job listening things.
 * @author Alex
 *
 */
public class JobsJobListener implements Listener {
    Jobs plugin;
    
    public JobsJobListener(Jobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJobLevelUp(JobsLevelUpEvent event) {
        if(event.isCancelled())
            return;

        Player player = plugin.getServer().getPlayer(event.getPlayer().getName());
        
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
            
            String message;
            if(plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                message = plugin.getMessageConfig().getMessage("level-up-broadcast");
            } else {
                message = plugin.getMessageConfig().getMessage("level-up-no-broadcast");
            }
            message = message.replace("%jobname%", ""+progression.getJob().getName());
            message = message.replace("%jobcolour%", ""+progression.getJob().getChatColour());
            if(progression.getTitle() != null){
                message = message.replace("%titlename%", ""+progression.getTitle().getName());
                message = message.replace("%titlecolour%", ""+progression.getTitle().getChatColor());
            }
            message = message.replace("%playername%", ""+event.getPlayer().getName());
            if(player == null) {
                message = message.replace("%playerdisplayname%", ""+event.getPlayer().getName());
            } else {
                message = message.replace("%playerdisplayname%", ""+player.getDisplayName());
            }
            message = message.replace("%joblevel%", ""+progression.getLevel());
            if(plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                for(String line: message.split("\n")){
                    plugin.getServer().broadcastMessage(line);
                }
            } else if(player != null) {
                for(String line: message.split("\n")){
                    player.sendMessage(line);
                }
            }
            event.getPlayer().checkLevels();
        } else if(player != null) {
            event.getJobProgression().setExperience(0.0);
            String message = plugin.getMessageConfig().getMessage("at-max-level");
        
            for(String line: message.split("\n")){
                player.sendMessage(line);
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJobSkillUp(JobsSkillUpEvent event) {
        if(event.isCancelled())
            return;
        
        Player player = plugin.getServer().getPlayer(event.getPlayer().getName());
        
        // set new title
        event.getJobProgression().setTitle(event.getNewTitle());
        
        //broadcast
        if(plugin.getJobsConfiguration().isBroadcastingSkillups()){
            String message = plugin.getMessageConfig().getMessage("skill-up-broadcast");
            message = message.replace("%playername%", event.getPlayer().getName());
            if(event.getNewTitle() != null){
                message = message.replace("%titlecolour%", event.getNewTitle().getChatColor().toString());
                message = message.replace("%titlename%", event.getNewTitle().getName());
            }
            message = message.replace("%jobcolour%", event.getJobProgression().getJob().getChatColour().toString());
            message = message.replace("%jobname%", event.getJobProgression().getJob().getName());
            for(String line: message.split("\n")){
                plugin.getServer().broadcastMessage(line);
            }
        } else if(player != null) {
            String message = plugin.getMessageConfig().getMessage("skill-up-no-broadcast");
            if(event.getNewTitle() != null){
                message = message.replace("%titlecolour%", event.getNewTitle().getChatColor().toString());
                message = message.replace("%titlename%", event.getNewTitle().getName());
            }
            message = message.replace("%jobcolour%", event.getJobProgression().getJob().getChatColour().toString());
            message = message.replace("%jobname%", event.getJobProgression().getJob().getName());
            for(String line: message.split("\n")){
                player.sendMessage(line);
            }
        }
        event.getJobProgression().setTitle(event.getNewTitle());
        event.getPlayer().reloadHonorific();
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJobJoin(JobsJoinEvent event) {
        if(event.isCancelled())
            return;
        
        Player player = plugin.getServer().getPlayer(event.getPlayer().getName());
        
        if(event.getNewJob().getMaxSlots() == null || plugin.getJobConfig().getUsedSlots(event.getNewJob()) < event.getNewJob().getMaxSlots()) {
            if(!event.getPlayer().isInJob(event.getNewJob())){
                // let the user join the job
                event.getPlayer().joinJob(event.getNewJob());
                plugin.getJobsConfiguration().getJobsDAO().joinJob(event.getPlayer(), event.getNewJob());
                plugin.getJobConfig().takeSlot(event.getNewJob());
                String message = plugin.getMessageConfig().getMessage("join-job-success");
                message = message.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
                message = message.replace("%jobname%", event.getNewJob().getName());
                if(player != null) {
                    for(String line: message.split("\n")) {
                        player.sendMessage(line);
                    }
                }
                event.getPlayer().reloadHonorific();
                event.getPlayer().reloadMaxExperience();
            } else if(player != null) {
                if(event.getPlayer().isInJob(event.getNewJob())){
                    // already in job message
                    String message = plugin.getMessageConfig().getMessage("join-job-failed-already-in");
                    message = message.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
                    message = message.replace("%jobname%", event.getNewJob().getName());
                    for(String line: message.split("\n")){
                        player.sendMessage(line);
                    }
                }
                else{
                    // you are already in too many jobs
                    String message = plugin.getMessageConfig().getMessage("leave-job-failed-too-many");
                    for(String line: message.split("\n")){
                        player.sendMessage(line);
                    }
                }
            }
        } else if (player != null && plugin.getJobConfig().getUsedSlots(event.getNewJob()) >= event.getNewJob().getMaxSlots()) {
            // already in job message
            String message = plugin.getMessageConfig().getMessage("join-job-failed-no-slots");
            message = message.replace("%jobcolour%", event.getNewJob().getChatColour().toString());
            message = message.replace("%jobname%", event.getNewJob().getName());
            for(String line: message.split("\n")){
                player.sendMessage(line);
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onJobLeave(JobsLeaveEvent event) {
        if(event.isCancelled())
            return;
        
        Player player = plugin.getServer().getPlayer(event.getPlayer().getName());
        
        if(event.getPlayer().isInJob(event.getOldJob())){
            // let the user join the job
            event.getPlayer().leaveJob(event.getOldJob());
            plugin.getJobsConfiguration().getJobsDAO().quitJob(event.getPlayer(), event.getOldJob());
            plugin.getJobConfig().leaveSlot(event.getOldJob());
            if(player != null) {
                String message = plugin.getMessageConfig().getMessage("leave-job-success");
                message = message.replace("%jobcolour%", event.getOldJob().getChatColour().toString());
                message = message.replace("%jobname%", event.getOldJob().getName());
                for(String line: message.split("\n")){
                    player.sendMessage(line);
                }
            }
            event.getPlayer().reloadHonorific();
            event.getPlayer().reloadMaxExperience();
            event.getPlayer().checkLevels();
        }
    }
}
