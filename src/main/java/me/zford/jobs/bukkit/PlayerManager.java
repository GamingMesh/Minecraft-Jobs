package me.zford.jobs.bukkit;

import java.util.HashMap;

import org.bukkit.entity.Player;

import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.container.Title;
import me.zford.jobs.dao.JobsDAO;

public class PlayerManager {
    private JobsPlugin plugin;
    private HashMap<String, JobsPlayer> players = new HashMap<String, JobsPlayer>();
    public PlayerManager(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Add a player to the plugin to me managed.
     * @param playername
     */
    public void addPlayer(String playername) {
        JobsPlayer jPlayer = new JobsPlayer(plugin, playername);
        jPlayer.loadDAOData(plugin.getJobsCore().getJobsDAO().getAllJobs(jPlayer));
        players.put(playername, jPlayer);
    }
    
    /**
     * Remove a player from the plugin.
     * @param playername
     */
    public void removePlayer(String playername) {
        JobsDAO dao = plugin.getJobsCore().getJobsDAO();
        if (players.containsKey(playername)) {
            JobsPlayer player = players.remove(playername);
            dao.save(player);
        }
    }
    
    /**
     * Save all the information of all of the players in the game
     */
    public void saveAll() {
        JobsDAO dao = plugin.getJobsCore().getJobsDAO();
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
        if (jPlayer.isInJob(job))
            return;
        Player player = plugin.getServer().getPlayer(jPlayer.getName());
        // let the user join the job
        if (!jPlayer.joinJob(job))
            return;
        
        plugin.getJobsCore().getJobsDAO().joinJob(jPlayer, job);
        plugin.getJobsCore().takeSlot(job);
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
        
        plugin.getJobsCore().getJobsDAO().quitJob(jPlayer, job);
        plugin.getJobsCore().leaveSlot(job);
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
        
        JobsDAO dao = plugin.getJobsCore().getJobsDAO();
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
        plugin.getJobsCore().getJobsDAO().save(jPlayer);
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
        plugin.getJobsCore().getJobsDAO().save(jPlayer);
    }
    
    /**
     * Adds experience to the player
     * @param jPlayer
     * @param job - the job
     * @param experience - experience gained
     */
    public void addExperience(JobsPlayer jPlayer, Job job, double experience) {
        JobProgression prog = jPlayer.getJobProgression(job);
        if (prog == null)
            return;
        if (prog.addExperience(experience))
            performLevelUp(jPlayer, job);
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
        plugin.getJobsCore().getJobsDAO().save(jPlayer);
    }
    
    /**
     * Removes experience to the player
     * @param jPlayer
     * @param job - the job
     * @param experience - experience gained
     */
    public void removeExperience(JobsPlayer jPlayer, Job job, double experience) {
        JobProgression prog = jPlayer.getJobProgression(job);
        if (prog == null)
            return;
        prog.addExperience(-experience);
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
        plugin.getJobsCore().getJobsDAO().save(jPlayer);
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
