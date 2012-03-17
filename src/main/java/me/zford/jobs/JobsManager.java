package me.zford.jobs;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobsPlayer;

public class JobsManager {
    private Jobs plugin;
    private HashMap<String, JobsPlayer> players = new HashMap<String, JobsPlayer>();
    public JobsManager(Jobs plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Add a player to the plugin to me managed.
     * @param playername
     */
    public void addPlayer(String playername) {
        players.put(playername, new JobsPlayer(plugin, playername, plugin.getJobsConfiguration().getJobsDAO()));
    }
    
    /**
     * Remove a player from the plugin.
     * @param playername
     */
    public void removePlayer(String playername) {
        if (players.containsKey(playername)) {
            JobsPlayer player = players.remove(playername);
            player.save();
        }
    }
    
    /**
     * Save all the information of all of the players in the game
     */
    public void saveAll() {
        for (JobsPlayer player : players.values()) {
            player.save();
        }
    }
    
    /**
     * Get the player job info for specific player
     * @param player - the player who's job you're getting
     * @return the player job info of the player
     */
    public JobsPlayer getJobsPlayer(String playername) {
        JobsPlayer player = players.get(playername);
        if(player != null)
            return player;
        return new JobsPlayer(plugin, playername, plugin.getJobsConfiguration().getJobsDAO());
    }
    
    /**
     * Causes jPlayer to leave their job
     * @param jPlayer
     * @param job
     */
    public void joinJob(JobsPlayer jPlayer, Job job) {
        if (jPlayer.isInJob(job))
            return;
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        // let the user join the job
        jPlayer.joinJob(job);
        plugin.getJobConfig().takeSlot(job);
        if (player != null) {
            String message = plugin.getMessageConfig().getMessage("join-job-success");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            for(String line: message.split("\n")) {
                player.sendMessage(line);
            }
        }
    }
    
    public void leaveJob(JobsPlayer jPlayer, Job job) {
        if (!jPlayer.isInJob(job))
            return;
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        // let the user leave the job
        jPlayer.leaveJob(job);
        plugin.getJobConfig().leaveSlot(job);
        if(player != null) {
            String message = plugin.getMessageConfig().getMessage("leave-job-success");
            message = message.replace("%jobcolour%", job.getChatColour().toString());
            message = message.replace("%jobname%", job.getName());
            for(String line: message.split("\n")){
                player.sendMessage(line);
            }
        }
    }
}
