/**
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011 Zak Ford <zak.j.ford@gmail.com>
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
 */

package me.zford.jobs.bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.entity.Player;

import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.container.Title;
import me.zford.jobs.dao.JobsDAO;

public class PlayerManager {
    private JobsPlugin plugin;
    private Map<String, JobsPlayer> players = Collections.synchronizedMap(new HashMap<String, JobsPlayer>());
    public PlayerManager(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles join of new player
     * @param playername
     */
    public void playerJoin(String playername) {
        synchronized (players) {
            JobsPlayer jPlayer = players.get(playername);
            if (jPlayer == null) {
                jPlayer = new JobsPlayer(plugin, playername);
                jPlayer.loadDAOData(plugin.getJobsCore().getJobsDAO().getAllJobs(jPlayer));
                players.put(playername, jPlayer);
            }
            jPlayer.setOnline(true);
            jPlayer.reloadHonorific();
            jPlayer.recalculatePermissions();
        }
    }
    
    /**
     * Handles player quit
     * @param playername
     */
    public void playerQuit(String playername) {
        synchronized (players) {
            JobsPlayer jPlayer = players.get(playername);
            if (jPlayer != null) {
                jPlayer.setOnline(false);
            }
        }
    }
    
    /**
     * Save all the information of all of the players in the game
     */
    public void saveAll() {
        JobsDAO dao = plugin.getJobsCore().getJobsDAO();
        
        /*
         * Saving is a three step process to minimize synchronization locks when called asynchronously.
         * 
         * 1) Safely copy list for saving.
         * 2) Perform save on all players on copied list.
         * 3) Garbage collect the real list to remove any offline players with saved data
         */
        ArrayList<JobsPlayer> list = null;
        synchronized (players) {
            list = new ArrayList<JobsPlayer>(players.values());
        }
        
        for (JobsPlayer jPlayer : list) {
            jPlayer.save(dao);
        }
        
        synchronized (players) {
            Iterator<JobsPlayer> iter = players.values().iterator();
            while (iter.hasNext()) {
                JobsPlayer jPlayer = iter.next();
                synchronized (jPlayer.saveLock) {
                    if (!jPlayer.isOnline() && jPlayer.isSaved()) {
                        iter.remove();
                    }
                }
            }
        }
    }
    
    /**
     * Get the player job info for specific player
     * @param player - the player who's job you're getting
     * @return the player job info of the player
     */
    public JobsPlayer getJobsPlayer(String playername) {
        JobsPlayer jPlayer = players.get(playername);
        if (jPlayer == null) {
            jPlayer = new JobsPlayer(plugin, playername);
            jPlayer.loadDAOData(plugin.getJobsCore().getJobsDAO().getAllJobs(jPlayer));
        }
        return jPlayer;
    }
    
    /**
     * Causes player to join their job
     * @param jPlayer
     * @param job
     */
    public void joinJob(JobsPlayer jPlayer, Job job) {
        synchronized (jPlayer.saveLock) {
            if (jPlayer.isInJob(job))
                return;
            // let the user join the job
            if (!jPlayer.joinJob(job))
                return;
            
            plugin.getJobsCore().getJobsDAO().joinJob(jPlayer, job);
            plugin.getJobsCore().takeSlot(job);
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("join-job-success");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    /**
     * Causes player to leave their job
     * @param jPlayer
     * @param job
     */
    public void leaveJob(JobsPlayer jPlayer, Job job) {
        synchronized (jPlayer.saveLock) {
            if (!jPlayer.isInJob(job))
                return;
            // let the user leave the job
            if (!jPlayer.leaveJob(job))
                return;
            
            plugin.getJobsCore().getJobsDAO().quitJob(jPlayer, job);
            plugin.getJobsCore().leaveSlot(job);
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if(player != null) {
            String message = plugin.getMessageConfig().getMessage("leave-job-success");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    /**
     * Transfers player job
     * @param jPlayer
     * @param oldjob - the old job
     * @param newjob - the new job
     */
    public void transferJob(JobsPlayer jPlayer, Job oldjob, Job newjob) {
        synchronized (jPlayer.saveLock) {
            if (!jPlayer.transferJob(oldjob,  newjob))
                return;
            
            JobsDAO dao = plugin.getJobsCore().getJobsDAO();
            dao.quitJob(jPlayer, oldjob);
            dao.joinJob(jPlayer, newjob);
            jPlayer.save(dao);
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("transfer-target");
            message = message.replace("%oldjobcolour%", oldjob.getChatColour().toString());
            message = message.replace("%oldjobname%", oldjob.getName());
            message = message.replace("%newjobcolour%", newjob.getChatColour().toString());
            message = message.replace("%newjobname%", newjob.getName());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    /**
     * Promotes player in their job
     * @param jPlayer
     * @param job - the job
     * @param levels - number of levels to promote
     */
    public void promoteJob(JobsPlayer jPlayer, Job job, int levels) {
        synchronized (jPlayer.saveLock) {
            jPlayer.promoteJob(job, levels);
            jPlayer.save(plugin.getJobsCore().getJobsDAO());
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("promote-target");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            message = message.replace("%levelsgained%", Integer.valueOf(levels).toString());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    /**
     * Demote player in their job
     * @param jPlayer
     * @param job - the job
     * @param levels - number of levels to demote
     */
    public void demoteJob(JobsPlayer jPlayer, Job job, int levels) {
        synchronized (jPlayer.saveLock) {
            jPlayer.demoteJob(job, levels);
            jPlayer.save(plugin.getJobsCore().getJobsDAO());
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("demote-target");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            message = message.replace("%levelslost%", Integer.valueOf(levels).toString());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    /**
     * Adds experience to the player
     * @param jPlayer
     * @param job - the job
     * @param experience - experience gained
     */
    public void addExperience(JobsPlayer jPlayer, Job job, double experience) {
        synchronized (jPlayer.saveLock) {
            JobProgression prog = jPlayer.getJobProgression(job);
            if (prog == null)
                return;
            if (prog.addExperience(experience))
                performLevelUp(jPlayer, job);
    
            jPlayer.save(plugin.getJobsCore().getJobsDAO());
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("grantxp-target");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            message = message.replace("%expgained%", Double.valueOf(experience).toString());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    /**
     * Removes experience to the player
     * @param jPlayer
     * @param job - the job
     * @param experience - experience gained
     */
    public void removeExperience(JobsPlayer jPlayer, Job job, double experience) {
        synchronized (jPlayer.saveLock) {
            JobProgression prog = jPlayer.getJobProgression(job);
            if (prog == null)
                return;
            prog.addExperience(-experience);
            
            jPlayer.save(plugin.getJobsCore().getJobsDAO());
        }
        
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("removexp-target");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            message = message.replace("%expgained%", Double.valueOf(experience).toString());
            for (String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    
    /**
     * Broadcasts level up about a player
     * @param jPlayer
     * @param job
     */
    public void performLevelUp(JobsPlayer jPlayer, Job job) {
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        JobProgression prog = jPlayer.getJobProgression(job);
        if (prog == null)
            return;

        String message;
        if (plugin.getJobsConfiguration().isBroadcastingLevelups()) {
            message = plugin.getMessageConfig().getMessage("level-up-broadcast");
        } else {
            message = plugin.getMessageConfig().getMessage("level-up-no-broadcast");
        }
        message = message.replace("%jobname%", job.getName());
        message = message.replace("%jobcolour%", job.getChatColour().toString());
        if (prog.getTitle() != null) {
            message = message.replace("%titlename%", prog.getTitle().getName());
            message = message.replace("%titlecolour%", prog.getTitle().getChatColor().toString());
        }
        message = message.replace("%playername%", jPlayer.getName());
        if (player == null) {
            message = message.replace("%playerdisplayname%", jPlayer.getName());
        } else {
            message = message.replace("%playerdisplayname%", player.getDisplayName());
        }
        message = message.replace("%joblevel%", ""+prog.getLevel());
        for (String line: message.split("\n")) {
            if (plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                plugin.getServer().broadcastMessage(line);
            } else if (player != null) {
                player.sendMessage(line);
            }
        }
        
        Title levelTitle = plugin.getJobsConfiguration().getTitleForLevel(prog.getLevel());
        if (levelTitle != null && !levelTitle.equals(prog.getTitle())) {        
            // user would skill up
            if (plugin.getJobsConfiguration().isBroadcastingSkillups()) {
                message = plugin.getMessageConfig().getMessage("skill-up-broadcast");
            } else {
                message = plugin.getMessageConfig().getMessage("skill-up-no-broadcast");
            }
            message = message.replace("%playername%", jPlayer.getName());
            message = message.replace("%titlecolour%", levelTitle.getChatColor().toString());
            message = message.replace("%titlename%", levelTitle.getName());
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            for (String line: message.split("\n")) {
                if (plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                    plugin.getServer().broadcastMessage(line);
                } else if (player != null) {
                    player.sendMessage(line);
                }
            }
        }
        prog.setTitle(levelTitle);
        jPlayer.reloadHonorific();
        jPlayer.recalculatePermissions();
    }
}
