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

package me.zford.jobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobProgression;
import me.zford.jobs.config.container.JobsLivingEntityInfo;
import me.zford.jobs.config.container.JobsMaterialInfo;
import me.zford.jobs.config.container.JobsPlayer;
import me.zford.jobs.resources.jfep.Parser;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class JobsCommands implements CommandExecutor {
    
    private Jobs plugin;
    
    public JobsCommands(Jobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player pSender = (Player)sender;
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(pSender.getName());
            // player only commands
            // join
            if (args.length >= 2 && args[0].equalsIgnoreCase("join")) {
                String jobName = args[1].trim();
                Job job = plugin.getJobConfig().getJob(jobName);
                if (job == null || job.isHidden()) {
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
                
                if (job.getMaxSlots() != null && plugin.getJobConfig().getUsedSlots(job) >= job.getMaxSlots()) {
                    String message = plugin.getMessageConfig().getMessage("join-job-failed-no-slots");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    sendMessageByLine(sender, message);
                    return true;
                }
                
                int confMaxJobs = plugin.getJobsConfiguration().getMaxJobs();
                if (confMaxJobs > 0 && jPlayer.getJobs().size() >= confMaxJobs) {
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("join-too-many-job"));
                    return true;
                }
                
                plugin.getJobsManager().joinJob(jPlayer, job);
                return true;
            }
            // leave
            else if (args.length >= 2 && args[0].equalsIgnoreCase("leave")) {
                String jobName = args[1].trim();
                Job job = plugin.getJobConfig().getJob(jobName);
                if (job == null) {
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                    return true;
                }
                
                plugin.getJobsManager().leaveJob(jPlayer, job);
                return true;
            }
            // jobs info <jobname> <break, place, kill>
            else if (args.length >= 2 && args[0].equalsIgnoreCase("info")) {
                Job job = plugin.getJobConfig().getJob(args[1]);
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
                jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            } else if (sender instanceof Player) {
                jPlayer = plugin.getJobsManager().getJobsPlayer(((Player)sender).getName());
            }
            
            if(jPlayer == null) {
                sender.sendMessage(ChatColor.RED + "There was an error in your command");
                return true;
            }
            
            if(jPlayer.getJobsProgression().size() == 0){
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("stats-no-job"));
                return true;
            }
            
            for(JobProgression jobProg: jPlayer.getJobsProgression()){
                sendMessageByLine(sender, jobStatsMessage(jobProg));
            }
            return true;
        }
        // browse
        else if (args.length >= 1 && args[0].equalsIgnoreCase("browse")) {
            ArrayList<String> jobs = new ArrayList<String>();
            for (Job job: plugin.getJobConfig().getJobs()) {
                if (!plugin.hasJobPermission(sender, job))
                    continue;
                if (job.isHidden())
                    continue;
                
                if (job.getMaxLevel() != null) {
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
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
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
                    JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(player.getName());
                    jPlayer.removePermissions();
                    plugin.getJobsManager().removePlayer(player.getName());
                }
                plugin.reloadConfigurations();
                for(Player player : plugin.getServer().getOnlinePlayers()) {
                    plugin.getJobsManager().addPlayer(player.getName());
                }
                plugin.reRegisterPermissions();
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            } catch (Exception e) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
                plugin.getLogger().severe("There was an error when performing a reload: "+e.getStackTrace());
            }
            return true;
        }
        else if (args.length >= 3 && args[0].equalsIgnoreCase("fire")) {
            if (!sender.hasPermission("jobs.admin.fire")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
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
                plugin.getJobsManager().leaveJob(jPlayer, job);
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
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
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
                plugin.getJobsManager().joinJob(jPlayer, job);
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
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            try {
                // check if player already has the job
                if (jPlayer.isInJob(job)) {
                    Integer levelsGained = Integer.parseInt(args[3]);
                    jPlayer.promoteJob(job, levelsGained);
                    
                    if(player != null) {
                        String message = plugin.getMessageConfig().getMessage("promote-target");
                        message = message.replace("%jobcolour%", job.getChatColour().toString());
                        message = message.replace("%jobname%", job.getName());
                        message = message.replace("%levelsgained%", levelsGained.toString());
                        sendMessageByLine(player, message);
                    }
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
                }
                jPlayer.save();
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
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
            if (job == null) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
                return true;
            }
            try {
                // check if player already has the job
                if (jPlayer.isInJob(job)) {
                    Integer levelsLost = Integer.parseInt(args[3]);
                    jPlayer.demoteJob(job, levelsLost);
                    
                    if (player != null) {
                        String message = plugin.getMessageConfig().getMessage("demote-target");
                        message = message.replace("%jobcolour%", job.getChatColour().toString());
                        message = message.replace("%jobname%", job.getName());
                        message = message.replace("%levelslost%", levelsLost.toString());
                        sendMessageByLine(player, message);
                    }
                    sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
                }
                jPlayer.save();
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
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
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
            if(jPlayer.isInJob(job)) {
                jPlayer.addExperience(job, expGained);
                if (player != null) {
                    String message = plugin.getMessageConfig().getMessage("grantxp-target");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    message = message.replace("%expgained%", args[3]);
                    sendMessageByLine(player, message);
                }
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            }
            jPlayer.save();
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("removexp")) {
            if (!sender.hasPermission("jobs.admin.removexp")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job job = plugin.getJobConfig().getJob(args[2]);
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
                jPlayer.addExperience(job, -expLost);
                if(player != null) {
                    String message = plugin.getMessageConfig().getMessage("removexp-target");
                    message = message.replace("%jobcolour%", job.getChatColour().toString());
                    message = message.replace("%jobname%", job.getName());
                    message = message.replace("%explost%", args[3]);
                    sendMessageByLine(player, message);
                }
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            }
            jPlayer.save();
            return true;
        }
        else if (args.length >= 4 && args[0].equalsIgnoreCase("transfer")) {
            if (!sender.hasPermission("jobs.admin.transfer")) {
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            JobsPlayer jPlayer = plugin.getJobsManager().getJobsPlayer(args[1]);
            Player player = plugin.getServer().getPlayer(args[1]);
            Job oldjob = plugin.getJobConfig().getJob(args[2]);
            Job newjob = plugin.getJobConfig().getJob(args[3]);
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
                    jPlayer.transferJob(oldjob, newjob);
                    if (player != null) {
                        String message = plugin.getMessageConfig().getMessage("transfer-target");
                        message = message.replace("%oldjobcolour%", oldjob.getChatColour().toString());
                        message = message.replace("%oldjobname%", oldjob.getName());
                        message = message.replace("%newjobcolour%", newjob.getChatColour().toString());
                        message = message.replace("%newjobname%", newjob.getName());
                        sendMessageByLine(player, message);
                    }
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
        
        String message = "";
        
        String[] jobTypes = { "break", "place", "kill", "fish", "craft", "smelt" };
        
        int showAllTypes = 1;
        for (String jobType : jobTypes) {
            if (jobType.equalsIgnoreCase(type)) {
                showAllTypes = 0;
                break;
            }
        }
        
        if (type.equalsIgnoreCase("break") || showAllTypes == 1) {
            // break
            Map<String, JobsMaterialInfo> jobBreakInfo = job.getBreakInfo();
            if (jobBreakInfo != null && !jobBreakInfo.isEmpty()) {
                message += jobInfoBreakMessage(player, job, jobBreakInfo);
            } else if (showAllTypes == 0) {
                String myMessage = plugin.getMessageConfig().getMessage("break-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        if (type.equalsIgnoreCase("place") || showAllTypes == 1) {
            // place
            Map<String, JobsMaterialInfo> jobPlaceInfo = job.getPlaceInfo();
            
            if (jobPlaceInfo != null && !jobPlaceInfo.isEmpty()) {
                message += jobInfoPlaceMessage(player, job, jobPlaceInfo);
            } else if (showAllTypes == 0) {
                String myMessage = plugin.getMessageConfig().getMessage("place-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        if (type.equalsIgnoreCase("kill") || showAllTypes == 1) {
            // kill
            Map<String, JobsLivingEntityInfo> jobKillInfo = job.getKillInfo();
            
            if (jobKillInfo != null && !jobKillInfo.isEmpty()) {
                message += jobInfoKillMessage(player, job, jobKillInfo);
            } else if (showAllTypes == 0) {
                String myMessage = plugin.getMessageConfig().getMessage("kill-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        
        if (type.equalsIgnoreCase("fish") || showAllTypes == 1) {
            // fish
            Map<String, JobsMaterialInfo> jobFishInfo = job.getFishInfo();
            
            if (jobFishInfo != null && !jobFishInfo.isEmpty()) {
                message += jobInfoFishMessage(player, job, jobFishInfo);
            } else if (showAllTypes == 0) {
                String myMessage = plugin.getMessageConfig().getMessage("fish-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        
        if (type.equalsIgnoreCase("craft") || showAllTypes == 1) {
            // craft
            Map<String, JobsMaterialInfo> jobCraftInfo = job.getCraftInfo();
            
            if (jobCraftInfo != null && !jobCraftInfo.isEmpty()) {
                message += jobInfoCraftMessage(player, job, jobCraftInfo);
            } else if(showAllTypes == 0) {
                String myMessage = plugin.getMessageConfig().getMessage("craft-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        
        if (type.equalsIgnoreCase("smelt") || showAllTypes == 1) {
            // craft
            Map<String, JobsMaterialInfo> jobSmeltInfo = job.getSmeltInfo();
            
            if (jobSmeltInfo != null && !jobSmeltInfo.isEmpty()) {
                message += jobInfoSmeltMessage(player, job, jobSmeltInfo);
            } else if(showAllTypes == 0) {
                String myMessage = plugin.getMessageConfig().getMessage("smelt-none");
                myMessage = myMessage.replace("%jobcolour%", job.getChatColour().toString());
                myMessage = myMessage.replace("%jobname%", job.getName());
                message += myMessage;
            }
        }
        return message;
    }
    
    /**
     * Displays info about breaking blocks
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobBreakInfo - the information to display
     * @return the message
     */
    private String jobInfoBreakMessage(JobsPlayer player, Job job, Map<String, JobsMaterialInfo> jobBreakInfo) {
        
        String message = "";
        message += plugin.getMessageConfig().getMessage("break-header")+"\n";
        
        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = player.getJobsProgression(job);
        Parser expEquation = job.getExpEquation();
        Parser incomeEquation = job.getIncomeEquation();
        if(prog != null){
            expEquation.setVariable("joblevel", prog.getLevel());
            incomeEquation.setVariable("joblevel", prog.getLevel());
        }
        else {
            expEquation.setVariable("joblevel", 1);
            incomeEquation.setVariable("joblevel", 1);
        }
        expEquation.setVariable("numjobs", player.getJobs().size());
        incomeEquation.setVariable("numjobs", player.getJobs().size());
        for (Map.Entry<String, JobsMaterialInfo> temp: jobBreakInfo.entrySet()) {
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage("break-info-sub");
            }
            else {
                myMessage = plugin.getMessageConfig().getMessage("break-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage + "\n";
        }
        return message;
    }
    
    /**
     * Displays info about placing blocks
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobPlaceInfo - the information to display
     * @return the message
     */ 
    private String jobInfoPlaceMessage(JobsPlayer player, Job job, Map<String, JobsMaterialInfo> jobPlaceInfo) {
        
        String message = "";
        message += plugin.getMessageConfig().getMessage("place-header")+"\n";

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = player.getJobsProgression(job);
        Parser expEquation = job.getExpEquation();
        Parser incomeEquation = job.getIncomeEquation();
        if(prog != null){
            expEquation.setVariable("joblevel", prog.getLevel());
            incomeEquation.setVariable("joblevel", prog.getLevel());
        }
        else {
            expEquation.setVariable("joblevel", 1);
            incomeEquation.setVariable("joblevel", 1);
        }
        expEquation.setVariable("numjobs", player.getJobs().size());
        incomeEquation.setVariable("numjobs", player.getJobs().size());
        for (Map.Entry<String, JobsMaterialInfo> temp: jobPlaceInfo.entrySet()) {
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage("place-info-sub");
            }
            else {
                myMessage = plugin.getMessageConfig().getMessage("place-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage + "\n";
        }
        return message;
    }
    
    /**
     * Displays info about killing entities
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobKillInfo - the information to display
     * @return the message
     */
    private String jobInfoKillMessage(JobsPlayer player, Job job, Map<String, JobsLivingEntityInfo> jobKillInfo) {
        
        String message = "";
        message += plugin.getMessageConfig().getMessage("kill-header")+"\n";

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = player.getJobsProgression(job);
        Parser expEquation = job.getExpEquation();
        Parser incomeEquation = job.getIncomeEquation();
        if(prog != null){
            expEquation.setVariable("joblevel", prog.getLevel());
            incomeEquation.setVariable("joblevel", prog.getLevel());
        }
        else {
            expEquation.setVariable("joblevel", 1);
            incomeEquation.setVariable("joblevel", 1);
        }
        expEquation.setVariable("numjobs", player.getJobs().size());
        incomeEquation.setVariable("numjobs", player.getJobs().size());
        for (Map.Entry<String, JobsLivingEntityInfo> temp: jobKillInfo.entrySet()) {
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage("kill-info-sub");
            }
            else {
                myMessage = plugin.getMessageConfig().getMessage("kill-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("org.bukkit.craftbukkit.entity.Craft", ""));
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("org.bukkit.craftbukkit.entity.Craft", ""));
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage + "\n";
        }
        return message;
    }
    
    /**
     * Displays info about fishing
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobFishInfo - the information to display
     * @return the message
     */ 
    private String jobInfoFishMessage(JobsPlayer player, Job job, Map<String, JobsMaterialInfo> jobFishInfo) {
        
        String message = "";
        message += plugin.getMessageConfig().getMessage("fish-header")+"\n";

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = player.getJobsProgression(job);
        Parser expEquation = job.getExpEquation();
        Parser incomeEquation = job.getIncomeEquation();
        if(prog != null){
            expEquation.setVariable("joblevel", prog.getLevel());
            incomeEquation.setVariable("joblevel", prog.getLevel());
        }
        else {
            expEquation.setVariable("joblevel", 1);
            incomeEquation.setVariable("joblevel", 1);
        }
        expEquation.setVariable("numjobs", player.getJobs().size());
        incomeEquation.setVariable("numjobs", player.getJobs().size());
        for (Map.Entry<String, JobsMaterialInfo> temp: jobFishInfo.entrySet()) {
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage("fish-info-sub");
            }
            else {
                myMessage = plugin.getMessageConfig().getMessage("fish-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage + "\n";
        }
        return message;
    }
    
    /**
     * Displays info about fishing
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobCraftInfo - the information to display
     * @return the message
     */ 
    private String jobInfoCraftMessage(JobsPlayer player, Job job, Map<String, JobsMaterialInfo> jobCraftInfo) {
        
        String message = "";
        message += plugin.getMessageConfig().getMessage("craft-header")+"\n";

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = player.getJobsProgression(job);
        Parser expEquation = job.getExpEquation();
        Parser incomeEquation = job.getIncomeEquation();
        if(prog != null){
            expEquation.setVariable("joblevel", prog.getLevel());
            incomeEquation.setVariable("joblevel", prog.getLevel());
        }
        else {
            expEquation.setVariable("joblevel", 1);
            incomeEquation.setVariable("joblevel", 1);
        }
        expEquation.setVariable("numjobs", player.getJobs().size());
        incomeEquation.setVariable("numjobs", player.getJobs().size());
        for (Map.Entry<String, JobsMaterialInfo> temp: jobCraftInfo.entrySet()) {
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage("craft-info-sub");
            }
            else {
                myMessage = plugin.getMessageConfig().getMessage("craft-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage + "\n";
        }
        return message;
    }
    
    /**
     * Displays info about fishing
     * @param player - the player of the job
     * @param job - the job we are displaying info about
     * @param jobCraftInfo - the information to display
     * @return the message
     */ 
    private String jobInfoSmeltMessage(JobsPlayer player, Job job, Map<String, JobsMaterialInfo> jobSmeltInfo) {
        
        String message = "";
        message += plugin.getMessageConfig().getMessage("smelt-header")+"\n";

        DecimalFormat format = new DecimalFormat("#.##");
        JobProgression prog = player.getJobsProgression(job);
        Parser expEquation = job.getExpEquation();
        Parser incomeEquation = job.getIncomeEquation();
        if(prog != null){
            expEquation.setVariable("joblevel", prog.getLevel());
            incomeEquation.setVariable("joblevel", prog.getLevel());
        }
        else {
            expEquation.setVariable("joblevel", 1);
            incomeEquation.setVariable("joblevel", 1);
        }
        expEquation.setVariable("numjobs", player.getJobs().size());
        incomeEquation.setVariable("numjobs", player.getJobs().size());
        for (Map.Entry<String, JobsMaterialInfo> temp: jobSmeltInfo.entrySet()) {
            expEquation.setVariable("baseexperience", temp.getValue().getXpGiven());
            incomeEquation.setVariable("baseincome", temp.getValue().getMoneyGiven());
            String myMessage;
            if(temp.getKey().contains(":")){
                myMessage = plugin.getMessageConfig().getMessage("smelt-info-sub");
            }
            else {
                myMessage = plugin.getMessageConfig().getMessage("smelt-info-no-sub");
            }
            if(temp.getKey().contains(":")){
                myMessage = myMessage.replace("%item%", temp.getKey().split(":")[0].replace("_", " ").toLowerCase());
                myMessage = myMessage.replace("%subitem%", temp.getKey().split(":")[1]);
            }
            else{
                myMessage = myMessage.replace("%item%", temp.getKey().replace("_", " ").toLowerCase());
            }
            myMessage = myMessage.replace("%income%", format.format(incomeEquation.getValue()));
            myMessage = myMessage.replace("%experience%", format.format(expEquation.getValue()));
            message += myMessage + "\n";
        }
        return message;
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
