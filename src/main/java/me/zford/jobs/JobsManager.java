package me.zford.jobs;

import java.util.HashMap;

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
}
