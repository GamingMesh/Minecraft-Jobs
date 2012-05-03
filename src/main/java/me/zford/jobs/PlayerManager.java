package me.zford.jobs;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobsPlayer;
import me.zford.jobs.dao.JobsDAO;

public class PlayerManager {
    private Jobs plugin;
    private HashMap<String, JobsPlayer> players = new HashMap<String, JobsPlayer>();
    public PlayerManager(Jobs plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Add a player to the plugin to me managed.
     * @param playername
     */
    public void addPlayer(String playername) {
        JobsPlayer jPlayer = new JobsPlayer(plugin, playername);
        jPlayer.loadDAOData(plugin.getJobsConfiguration().getJobsDAO().getAllJobs(jPlayer));
        players.put(playername, jPlayer);
    }
    
    /**
     * Remove a player from the plugin.
     * @param playername
     */
    public void removePlayer(String playername) {
        JobsDAO dao = plugin.getJobsConfiguration().getJobsDAO();
        if (players.containsKey(playername)) {
            JobsPlayer player = players.remove(playername);
            dao.save(player);
        }
    }
    
    /**
     * Save all the information of all of the players in the game
     */
    public void saveAll() {
        JobsDAO dao = plugin.getJobsConfiguration().getJobsDAO();
        for (JobsPlayer player : players.values()) {
            dao.save(player);
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
            jPlayer.loadDAOData(plugin.getJobsConfiguration().getJobsDAO().getAllJobs(jPlayer));
        }
        return jPlayer;
    }
    
    /**
     * Causes player to join their job
     * @param jPlayer
     * @param job
     */
    public void joinJob(JobsPlayer jPlayer, Job job) {
        if (jPlayer.isInJob(job))
            return;
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        // let the user join the job
        if (!jPlayer.joinJob(job))
            return;
        
        plugin.getJobsConfiguration().getJobsDAO().joinJob(jPlayer, job);
        plugin.getJobConfig().takeSlot(job);
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
        if (!jPlayer.isInJob(job))
            return;
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        // let the user leave the job
        if (!jPlayer.leaveJob(job))
            return;
        
        plugin.getJobsConfiguration().getJobsDAO().quitJob(jPlayer, job);
        plugin.getJobConfig().leaveSlot(job);
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
        if (!jPlayer.transferJob(oldjob,  newjob))
            return;
        
        JobsDAO dao = plugin.getJobsConfiguration().getJobsDAO();
        dao.quitJob(jPlayer, oldjob);
        dao.joinJob(jPlayer, newjob);
        dao.save(jPlayer);
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
        jPlayer.promoteJob(job, levels);
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
        plugin.getJobsConfiguration().getJobsDAO().save(jPlayer);
    }
    
    /**
     * Demote player in their job
     * @param jPlayer
     * @param job - the job
     * @param levels - number of levels to demote
     */
    public void demoteJob(JobsPlayer jPlayer, Job job, int levels) {
        jPlayer.demoteJob(job, levels);
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
        plugin.getJobsConfiguration().getJobsDAO().save(jPlayer);
    }
    
    /**
     * Adds experience to the player
     * @param jPlayer
     * @param job - the job
     * @param experience - experience gained
     */
    public void addExperience(JobsPlayer jPlayer, Job job, double experience) {
        jPlayer.addExperience(job, experience);
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
        plugin.getJobsConfiguration().getJobsDAO().save(jPlayer);
    }
    
    /**
     * Removes experience to the player
     * @param jPlayer
     * @param job - the job
     * @param experience - experience gained
     */
    public void removeExperience(JobsPlayer jPlayer, Job job, double experience) {
        jPlayer.addExperience(job, -experience);
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
        plugin.getJobsConfiguration().getJobsDAO().save(jPlayer);
    }
}
