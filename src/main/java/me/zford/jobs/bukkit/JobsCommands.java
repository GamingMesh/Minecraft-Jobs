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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobInfo;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.util.ChatColor;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobsCommands implements CommandExecutor {
    
    private JobsPlugin plugin;
    private static final String label = "jobs";
    private HashMap<String, String> commandUsage = new HashMap<String, String>();
    private HashMap<String, String> commandHelp = new HashMap<String, String>();
    private LinkedHashSet<String> commands = new LinkedHashSet<String>();
    
    public JobsCommands(JobsPlugin plugin) {
        this.plugin = plugin;
        commandUsage.put("join", "[jobname]");
        commandUsage.put("leave", "[jobname]");
        commandUsage.put("info", "[jobname] [action]");
        commandUsage.put("stats", "[playername]");
        commandUsage.put("playerinfo", "[playername] [jobname] [action]");
        commandUsage.put("fire", "[playername] [jobname]");
        commandUsage.put("employ", "[playername] [jobname]");
        commandUsage.put("promote", "[playername] [jobname] [levels]");
        commandUsage.put("demote", "[playername] [jobname] [levels]");
        commandUsage.put("grantxp", "[playername] [jobname] [xp]");
        commandUsage.put("removexp", "[playername] [jobname] [xp]");
        commandUsage.put("transfer", "[playername] [oldjob] [newjob]");

        commandHelp.put("browse", "List the jobs available to you.");
        commandHelp.put("stats", "Show the level you are in each job you are part of.");
        commandHelp.put("join", "Join the selected job.");
        commandHelp.put("leave", "Leave the selected job.");
        commandHelp.put("info", "Show how much each job is getting paid and for what.");
        commandHelp.put("playerinfo", "Show how much each job is getting paid and for what on another player.");
        commandHelp.put("fire", "Fire the player from the job.");
        commandHelp.put("employ", "Employ the player to the job.");
        commandHelp.put("promote", "Promote the player X levels in a job.");
        commandHelp.put("demote", "Demote the player X levels in a job.");
        commandHelp.put("grantxp", "Grant the player X experience in a job.");
        commandHelp.put("removexp", "Remove X experience from the player in a job.");
        commandHelp.put("transfer", "Transfer a player's job from an old job to a new job.");
        commandHelp.put("reload", "Reload configurations.");

        commands.add("browse");
        commands.add("stats");
        commands.add("join");
        commands.add("leave");
        commands.add("info");
        commands.add("playerinfo");
        commands.add("fire");
        commands.add("employ");
        commands.add("promote");
        commands.add("demote");
        commands.add("grantxp");
        commands.add("removexp");
        commands.add("transfer");
        commands.add("reload");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            return help(sender);
        
        String cmd = args[0].toLowerCase();
        
        if (!commands.contains(cmd))
            return help(sender);
        
        String[] myArgs = reduceArgs(args);
        
        if (myArgs.length > 0) {
            if (myArgs[myArgs.length - 1].equals("?")) {
                sendUsage(sender, cmd);
                return true;
            }
        }
        
        try {
            Method m = getClass().getMethod(cmd, CommandSender.class, String[].class);
            if (!hasCommandPermission(sender, cmd)) {
                sender.sendMessage(plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            return (Boolean) m.invoke(this, sender, myArgs);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            return help(sender);
        }
        
        return false;
    }
    
    private static String[] reduceArgs(String[] args) {
        if (args.length <= 1)
            return new String[0];
        
        return Arrays.copyOfRange(args, 1, args.length);
    }
    
    private static boolean hasCommandPermission(CommandSender sender, String cmd) {
        return sender.hasPermission("jobs.command."+cmd);
    }
    
    private String getUsage(String cmd) {
        StringBuilder builder = new StringBuilder();
        builder.append(ChatColor.GREEN.toString());
        builder.append('/').append(label).append(' ');
        builder.append(cmd);
        builder.append(ChatColor.YELLOW);
        if (commandUsage.containsKey(cmd)) {
            builder.append(' ');
            builder.append(commandUsage.get(cmd));
        }
        return builder.toString();
    }
    
    public void sendUsage(CommandSender sender, String cmd) {
        sender.sendMessage(ChatColor.YELLOW+"Usage: "+getUsage(cmd));
        if (commandHelp.containsKey(cmd)) {
            sender.sendMessage(ChatColor.YELLOW+"* "+commandHelp.get(cmd));
        }
    }
    
    public void sendValidActions(CommandSender sender) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (ActionType action : ActionType.values()) {
            if (!first)
                builder.append(',');
            
            builder.append(action.getName());
            first = false;
            
        }
        sender.sendMessage(ChatColor.YELLOW+"Valid actions are: "+ChatColor.WHITE+ builder.toString());
    }
    
    private boolean help(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN+"*** "+ChatColor.YELLOW+"Jobs"+ChatColor.GREEN+" ***");
        for (String cmd : commands) {
            if (!hasCommandPermission(sender, cmd))
                continue;
            sender.sendMessage(getUsage(cmd));
        }
        sender.sendMessage(ChatColor.YELLOW+"Type /jobs [cmd] ? for more information about a command.");
        return true;
    }
    
    public boolean join(CommandSender sender, String[] args) {
        if (!(sender instanceof CommandSender))
            return false;
        
        if (args.length < 1) {
            sendUsage(sender, "join");
            return true;
        }
        
        Player pSender = (Player) sender;
        JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(pSender.getName());
        
        String jobName = args[0];
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
    
    public boolean leave(CommandSender sender, String[] args) {
        if (!(sender instanceof CommandSender))
            return false;
        
        if (args.length < 1) {
            sendUsage(sender, "leave");
            return true;
        }
        
        Player pSender = (Player) sender;
        JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(pSender.getName());
        
        String jobName = args[0];
        Job job = plugin.getJobsCore().getJob(jobName);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        
        plugin.getPlayerManager().leaveJob(jPlayer, job);
        return true;
    }
    
    public boolean info(CommandSender sender, String[] args) {
        if (!(sender instanceof CommandSender))
            return false;
        
        if (args.length < 1) {
            sendUsage(sender, "info");
            sendValidActions(sender);
            return true;
        }
        
        Player pSender = (Player) sender;
        JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(pSender.getName());
        
        String jobName = args[0];
        Job job = plugin.getJobsCore().getJob(jobName);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        String type = "";
        if (args.length >= 2) {
            type = args[1];
        }
        sendMessageByLine(sender, jobInfoMessage(jPlayer, job, type));
        return true;
    }
    
    public boolean stats(CommandSender sender, String[] args) {
        if (!(sender instanceof CommandSender))
            return false;

        JobsPlayer jPlayer = null;
        if (args.length >= 1) {
            if (!sender.hasPermission("jobs.command.admin.stats")) {
                sender.sendMessage(plugin.getMessageConfig().getMessage("error-no-permission"));
                return true;
            }
            Player player = plugin.getServer().getPlayer(args[0]);
            if (player == null) {
                jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
            } else {
                jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
            }
        } else if (sender instanceof Player) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(((Player) sender).getName());
        }
        
        if(jPlayer == null) {
            sendUsage(sender, "stats");
            return true;
        }
        
        if (jPlayer.getJobProgression().size() == 0){
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("stats-no-job"));
            return true;
        }
        
        for (JobProgression jobProg: jPlayer.getJobProgression()){
            sendMessageByLine(sender, jobStatsMessage(jobProg));
        }
        return true;
    }
    
    public boolean browse(CommandSender sender, String[] args) {
        ArrayList<String> jobs = new ArrayList<String>();
        for (Job job: plugin.getJobsCore().getJobs()) {
            if (plugin.getJobsConfiguration().getHideJobsWithoutPermission()) {
                if (!plugin.hasJobPermission(sender, job))
                    continue;
            }
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
    
    public boolean playerinfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender, "playerinfo");
            sendValidActions(sender);
            return true;
        }
        
        String playerName = args[0];
        JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(playerName);
        
        String jobName = args[1];
        Job job = plugin.getJobsCore().getJob(jobName);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        String type = "";
        if (args.length >= 3) {
            type = args[2];
        }
        sendMessageByLine(sender, jobInfoMessage(jPlayer, job, type));
        return true;
    }
    
    public boolean reload(CommandSender sender, String[] args) {
        try {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
                jPlayer.removePermissions();
            }
            plugin.reloadConfigurations();
            for(Player player : plugin.getServer().getOnlinePlayers()) {
                JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
                jPlayer.reloadHonorific();
                jPlayer.recalculatePermissions();
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
    
    public boolean fire(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender, "fire");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job job = plugin.getJobsCore().getJob(args[1]);
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
    
    public boolean employ(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendUsage(sender, "employ");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job job = plugin.getJobsCore().getJob(args[1]);
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
    
    public boolean promote(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "promote");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job job = plugin.getJobsCore().getJob(args[1]);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        try {
            // check if player already has the job
            if (jPlayer.isInJob(job)) {
                Integer levelsGained = Integer.parseInt(args[2]);
                plugin.getPlayerManager().promoteJob(jPlayer, job, levelsGained);
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            }
        } catch (Exception e) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
        }
        return true;
    }
    
    public boolean demote(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "demote");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job job = plugin.getJobsCore().getJob(args[1]);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        try {
            // check if player already has the job
            if (jPlayer.isInJob(job)) {
                Integer levelsLost = Integer.parseInt(args[2]);
                plugin.getPlayerManager().demoteJob(jPlayer, job, levelsLost);
                sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-success"));
            }
        }
        catch (Exception e) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("admin-command-failed"));
        }
        return true;
    }
    
    public boolean grantxp(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "grantxp");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job job = plugin.getJobsCore().getJob(args[1]);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        double expGained;
        try {
            expGained = Double.parseDouble(args[2]);
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
    
    public boolean removexp(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "removexp");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job job = plugin.getJobsCore().getJob(args[1]);
        if (job == null) {
            sendMessageByLine(sender, plugin.getMessageConfig().getMessage("error-no-job"));
            return true;
        }
        double expLost;
        try {
            expLost = Double.parseDouble(args[2]);
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
    
    public boolean transfer(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendUsage(sender, "transfer");
            return true;
        }
        JobsPlayer jPlayer = null;
        Player player = plugin.getServer().getPlayer(args[0]);
        if (player == null) {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(args[0]);
        } else {
            jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        }
        Job oldjob = plugin.getJobsCore().getJob(args[1]);
        Job newjob = plugin.getJobsCore().getJob(args[2]);
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
            if (type.startsWith(actionType.getName().toLowerCase())) {
                showAllTypes = 0;
                break;
            }
        }
        
        for (ActionType actionType : ActionType.values()) {
            if (showAllTypes == 1 || type.startsWith(actionType.getName().toLowerCase())) {
                List<JobInfo> info = job.getJobInfo(actionType);
                if (info != null && !info.isEmpty()) {
                    message.append(jobInfoMessage(player, job, actionType));
                } else if (showAllTypes == 0) {
                    String myMessage = plugin.getMessageConfig().getMessage(actionType.getName().toLowerCase()+"-none");
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
        message.append(type.getName());
        message.append(":\n");
        
        int level = 1;
        
        JobProgression prog = player.getJobProgression(job);
        if (prog != null)
            level = prog.getLevel();
        int numjobs = player.getJobProgression().size();
        List<JobInfo> jobInfo = job.getJobInfo(type);
        Economy economy = plugin.getEconomy();
        for (JobInfo info: jobInfo) {
            String materialName = info.getName().toLowerCase().replace('_', ' ');
            
            double income = info.getIncome(level, numjobs);
            ChatColor incomeColor = income >= 0 ? ChatColor.GREEN : ChatColor.DARK_RED;
            String incomeString = economy != null ? economy.format(income) : String.format("$%.2f", income);
            
            double xp = info.getExperience(level, numjobs);
            ChatColor xpColor = xp >= 0 ? ChatColor.YELLOW : ChatColor.GRAY;
            String xpString = String.format("%.2f xp", xp);
            
            message.append("  ");
            
            message.append(materialName);
            message.append(' ');
            
            message.append(xpColor.toString());
            message.append(xpString);
            message.append(' ');
            
            message.append(incomeColor.toString());
            message.append(incomeString);
            
            message.append('\n');
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
