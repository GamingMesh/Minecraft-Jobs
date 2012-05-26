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

package me.zford.jobs.bukkit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobInfo;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.util.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobsCommands implements CommandExecutor {
    
    private JobsPlugin plugin;
    
    public JobsCommands(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player pSender = (Player)sender;
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(pSender.getName());
            // player only commands
            // join
            if (args.length >= 2 && args[0].equalsIgnoreCase("join")) {
                String jobName = args[1].trim();
                Job job = plugin.getJobsCore().getJob(jobName);
                if (job == null) {
                    // job does not exist
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                    return true;
                }
                
                if (!plugin.hasJobPermission(pSender, job)) {
                    // you do not have permission to join the job
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                    return true;
                }
                
                if (jPlayer.isInJob(job)) {
                    // already in job message
                    String message = plugin.getMessageConfig().getMessage("join-job-failed-already-in");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    sendMessageByLine(sender, message);
                    return true;
                }
                
                if (job.getMaxSlots() != null && plugin.getJobsCore().getUsedSlots(job) >= job.getMaxSlots()) {
                    String message = plugin.getMessageConfig().getMessage("join-job-failed-no-slots");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    sendMessageByLine(sender, message);
                    return true;
                }
                
                int confMaxJobs = plugin.getJobsConfiguration().getMaxJobs();
                if (confMaxJobs > 0 && jPlayer.getJobProgression().size() >= confMaxJobs) {
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("join-too-many-job"));
                    return true;
                }
                
                plugin.getPlayerManager().joinJob(jPlayer, job);
                return true;
            }
            // leave
            else if (args.length >= 2 && args[0].equalsIgnoreCase("leave")) {
                String jobName = args[1].trim();
                Job job = plugin.getJobsCore().getJob(jobName);
                if (job == null) {
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                    return true;
                }
                
                plugin.getPlayerManager().leaveJob(jPlayer, job);
                return true;
            }
            // jobs info <jobname> <break, place, kill>
            else if (args.length >= 2 && args[0].equalsIgnoreCase("info")) {
                Job job = plugin.getJobsCore().getJob(args[1]);
                String type = "";
                if(args.length >= 3) {
                    type = args[2];
                }
                sendMessageByLine(sender, jobInfoMessage(jPlayer, job, type));
                return true;
            }
        }
        // stats
        if (args.length >= 1 && args[0].equalsIgnoreCase("stats")) {
            JobsPlayer jPlayer = null;
            if (args.length >= 2) {
                if (!sender.hasPermission("jobs.admin.stats")) {
                    sender.sendMessage(ChatColor.RED + "There was an error in your command");
                    return true;
                }
                jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            } else if (sender instanceof Player) {
                jPlayer = plugin.getPlayerManager().getJobsPlayer(((Player)sender).getName());
            }
            
            if(jPlayer == null) {
                sender.sendMessage(ChatColor.RED + "There was an error in your command");
                return true;
            }
            
            if(jPlayer.getJobProgression().size() == 0){
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("stats-no-job"));
                return true;
            }
            
            for(JobProgression jobProg: jPlayer.getJobProgression()){
                sendMessageByLine(sender, jobStatsMessage(jobProg));
            }
            return true;
        }
        // browse
        else if (args.length >= 1 && args[0].equalsIgnoreCase("browse")) {
            ArrayList<String> jobs = new ArrayList<String>();
            for (Job job: plugin.getJobsCore().getJobs()) {
                if (!plugin.hasJobPermission(sender, job))
                    continue;
                
                if (job.getMaxLevel() > 0) {
                    jobs.add(job.getChatColour() + job.getName() + ChatColor.WHITE + " - max lvl: " + job.getMaxLevel());
                } else {
                    jobs.add(job.getChatColour() + job.getName());
                }
            }
            
            if (jobs.size() == 0) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("browse-no-jobs"));
                return true;   
            }
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("browse-jobs-header"));
            for(String job : jobs) {
                sender.sendMessage("    "+job);
            }
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("browse-jobs-footer"));
            return true;
        }
        // admin commands
        else if (args.length >= 3 && args[0].equalsIgnoreCase("admininfo")) {
            if (!sender.hasPermission("jobs.admin.info")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            String type = "";
            if (args.length >= 4) {
                type = args[3];
            }
            sendMessageByLine(sender, jobInfoMessage(jPlayer, job, type));
            return true;
        }
        else if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("jobs.admin.reload")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            try {
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
                    jPlayer.removePermissions();
                    plugin.getPlayerManager().removePlayer(player.getName());
                }
                plugin.reloadConfigurations();
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.getPlayerManager().addPlayer(player.getName());
                }
                plugin.reRegisterPermissions();
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
                plugin.getLogger().severe("There was an error when performing a reload: ");
                e.printStackTrace();
            }
            return true;
        }
        else if (args.length >= 3 && args[0].equalsIgnoreCase("fire")) {
            if (!sender.hasPermission("jobs.admin.fire")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            if (!jPlayer.isInJob(job)) {
                String message = plugin.getMessageConfig().getMessage("fire-target-no-job");
                message = message.replace("%jobcolour%", job.getChatColour().toString());
                message = message.replace("%jobname%", job.getName());
                sendMessageByLine(sender, message);
                return true;
            }
            try {
                plugin.getPlayerManager().leaveJob(jPlayer, job);
                if (player != null) {
                    String message = plugin.getMessageConfig().getMessage("fire-target");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    sendMessageByLine(player, message);
                }
                
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
            }
            return true;
        }
        else if (args.length >= 3 && args[0].equalsIgnoreCase("employ")) {
            if (!sender.hasPermission("jobs.admin.employ."+args[2])) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            if (jPlayer.isInJob(job)) {
                // already in job message
                String message = plugin.getMessageConfig().getMessage("join-job-failed-already-in");
                message = message.replace("%jobcolour%", job.getChatColour().toString());
                message = message.replace("%jobname%", job.getName());
                sendMessageByLine(sender, message);
                return true;
            }
            try {
                // check if player already has the job
                plugin.getPlayerManager().joinJob(jPlayer, job);
                if(player != null) {
                    String message = plugin.getMessageConfig().getMessage("employ-target");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    sendMessageByLine(player, message);
                }
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
            }
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("promote")) {
            if (!sender.hasPermission("jobs.admin.promote")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            try {
                // check if player already has the job
                if (jPlayer.isInJob(job)) {
                    Integer levelsGained = Integer.parseInt(args[3]);
                    plugin.getPlayerManager().promoteJob(jPlayer, job, levelsGained);
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
                }
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
            }
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("demote")) {
            if (!sender.hasPermission("jobs.admin.demote")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            try {
                // check if player already has the job
                if (jPlayer.isInJob(job)) {
                    Integer levelsLost = Integer.parseInt(args[3]);
                    plugin.getPlayerManager().demoteJob(jPlayer, job, levelsLost);
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
                }
            }
            catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
            }
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("grantxp")) {
            if (!sender.hasPermission("jobs.admin.grantxp")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            double expGained;
            try {
                expGained = Double.parseDouble(args[3]);
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
                return true;
            }
            if (expGained <= 0) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
                return true;
            }
            // check if player already has the job
            if (jPlayer.isInJob(job)) {
                plugin.getPlayerManager().addExperience(jPlayer, job, expGained);
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            }
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("removexp")) {
            if (!sender.hasPermission("jobs.admin.removexp")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Job job = plugin.getJobsCore().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            double expLost;
            try {
                expLost = Double.parseDouble(args[3]);
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
                return true;
            }
            if (expLost <= 0) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
                return true;
            }
            // check if player already has the job
            if (jPlayer.isInJob(job)) {
                plugin.getPlayerManager().removeExperience(jPlayer, job, expLost);
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            }
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("transfer")) {
            if (!sender.hasPermission("jobs.admin.transfer")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(args[1]);
            Job oldjob = plugin.getJobsCore().getJob(args[2]);
            Job newjob = plugin.getJobsCore().getJob(args[3]);
            if (oldjob == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            if (newjob == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            try {
                if(jPlayer.isInJob(oldjob) && !jPlayer.isInJob(newjob)) {
                    plugin.getPlayerManager().transferJob(jPlayer, oldjob, newjob);
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
                }
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
            }
            return true;
        }
        // jobs-browse
        sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-browse"));
        
        if (sender instanceof Player) {
            // jobs-join
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-join"));
            //jobs-leave
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-leave"));
            //jobs-stats
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-stats"));
            //jobs-info
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-info"));
        }
        //jobs-admin-info
        if (sender.hasPermission("jobs.admin.info")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-info"));
        }
        //jobs-admin-fire
        if (sender.hasPermission("jobs.admin.fire")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-fire"));
        }
        //jobs-admin-employ
        if (sender.hasPermission("jobs.admin.employ")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-employ"));
        }
        //jobs-admin-promote
        if (sender.hasPermission("jobs.admin.promote")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-promote"));
        }
        //jobs-admin-demote
        if (sender.hasPermission("jobs.admin.demote")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-demote"));
        }
        //jobs-admin-grantxp
        if (sender.hasPermission("jobs.admin.grantxp")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-grantxp"));
        }
        //jobs-admin-removexp
        if (sender.hasPermission("jobs.admin.removexp")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-removexp"));
        }
        //jobs-admin-transfer
        if (sender.hasPermission("jobs.admin.transfer")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-transfer"));
        }
        if (sender.hasPermission("jobs.admin.reload")) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("jobs-admin-reload"));
        }
        return true;
    }

    
    /**
     * Displays info about a job
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param type - type of info
     * @return the message
     */
    private String jobInfoMessage(JobsPlayer player, Job job, String type) {
        if(job == null){
            // job doesn't exist
            return plugin.getMessageConfig().getMessage("error-no-job");
        }
        
        if (type == null) {
            type = "";
        } else {
            type = type.toLowerCase();
        }
        
        StringBuilder message = new StringBuilder();
        
        int showAllTypes = 1;
        for (ActionType actionType : ActionType.values()) {
            if (type.startsWith(actionType.getName())) {
                showAllTypes = 0;
                break;
            }
        }
        
        for (ActionType actionType : ActionType.values()) {
            if (showAllTypes == 1 || type.startsWith(actionType.getName())) {
                List<JobInfo> info = job.getJobInfo(actionType);
                if (info != null && !info.isEmpty()) {
                    message.append(jobInfoMessage(player, job, actionType));
                } else if (showAllTypes == 0) {
                    String myMessage = plugin.getMessageConfig().getMessage(actionType.getName()+"-none");
                    myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                    myMessage = myMessage.replace("%jobname%", job.getName());
                    message.append(myMessage);
                }
            }
        }
        return message.toString();
    }
    
    /**
     * Displays info about a particular action
     * @param player - the player of the job
     * @param prog - the job we are displaying info about
     * @param type - the type of action
     * @return the message
     */
    private String jobInfoMessage(JobsPlayer player, Job job, ActionType type) {
        StringBuilder message = new StringBuilder();
        message.append(plugin.getMessageConfig().getMessage(type.getName()+"-header")).append("\n");
        
        DecimalFormat format = new DecimalFormat("#.##");
        int level = 1;
        
        JobProgression prog = player.getJobProgression(job);
        if (prog != null)
            level = prog.getLevel();
        int numjobs = player.getJobProgression().size();
        List<JobInfo> jobInfo = job.getJobInfo(type);
        for (JobInfo info: jobInfo) {
            String myMessage;
            if (info.getName().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage(type.getName()+"-info-sub");
                myMessage = myMessage.replace("%item%", info.getName().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", info.getName().split(":")[1]);
            } else {
                myMessage = plugin.getMessageConfig().getMessage(type.getName()+"-info-no-sub");
                myMessage = myMessage.replace("%item%", info.getName().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(info.getIncome(level, numjobs)));
            myMessage = myMessage.replace("%experience%", format.format(info.getExperience(level, numjobs)));
            message.append(myMessage).append("\n");
        }
        return message.toString();
    }
    
    /**
     * Displays job stats about a particular player's job
     * @param jobProg - the job progress of the players job
     * @return the message
     */
    private String jobStatsMessage(JobProgression jobProg) {
        String message = plugin.getMessageConfig().getMessage("stats-job");
        message = message.replace("%joblevel%", Integer.valueOf(jobProg.getLevel()).toString());
        message = message.replace("%jobcolour%", jobProg.getJob().getChatColour().toString());
        message = message.replace("%jobname%", jobProg.getJob().getName());
        message = message.replace("%jobexp%", Integer.toString((int)jobProg.getExperience()));
        message = message.replace("%jobmaxexp%", Integer.toString(jobProg.getMaxExperience()));
        return message;
    }
    
    
    /**
     * Sends a message to line by line
     * @param sender - who receives info
     * @param message - message which needs to be sent
     */
    private void sendMessageByLine(CommandSender sender, String message) {
        for(String line : message.split("\n")) {
            sender.sendMessage(line);
        }
    }
}
