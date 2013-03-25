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
import java.util.LinkedHashSet;
import java.util.List;

import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobInfo;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.i18n.Language;
import me.zford.jobs.util.ChatColor;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobsCommands implements CommandExecutor {
    
    private JobsPlugin plugin;
    private static final String label = "jobs";
    private LinkedHashSet<String> commands = new LinkedHashSet<String>();
    
    public JobsCommands(JobsPlugin plugin) {
        this.plugin = plugin;
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
                sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.permission"));
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
        String key = "command."+cmd+".help.args";
        if (Language.containsKey(key)) {
            builder.append(' ');
            builder.append(Language.getMessage(key));
        }
        return builder.toString();
    }
    
    public void sendUsage(CommandSender sender, String cmd) {
        String message = ChatColor.YELLOW + Language.getMessage("command.help.output.usage");
        message = message.replace("%usage%", getUsage(cmd));
        sender.sendMessage(message);
        sender.sendMessage(ChatColor.YELLOW+"* "+Language.getMessage("command."+cmd+".help"));
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
        sender.sendMessage(ChatColor.YELLOW + Language.getMessage("command.help.output"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        
        if (!plugin.hasJobPermission(pSender, job)) {
            // you do not have permission to join the job
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.permission"));
            return true;
        }
        
        if (jPlayer.isInJob(job)) {
            // already in job message
            String message = ChatColor.RED + Language.getMessage("command.join.error.alreadyin");
            message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.RED);
            sender.sendMessage(message);
            return true;
        }
        
        if (job.getMaxSlots() != null && plugin.getJobsCore().getUsedSlots(job) >= job.getMaxSlots()) {
            String message = ChatColor.RED + Language.getMessage("command.join.error.fullslots");
            message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.RED);
            sender.sendMessage(message);
            return true;
        }
        
        int confMaxJobs = plugin.getJobsConfiguration().getMaxJobs();
        if (confMaxJobs > 0 && jPlayer.getJobProgression().size() >= confMaxJobs) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.join.error.maxjobs"));
            return true;
        }
        
        plugin.getPlayerManager().joinJob(jPlayer, job);
        
        String message = Language.getMessage("command.join.success");
        message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
        sender.sendMessage(message);
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        
        plugin.getPlayerManager().leaveJob(jPlayer, job);
        String message = Language.getMessage("command.leave.success");
        message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
        sender.sendMessage(message);
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        String type = "";
        if (args.length >= 2) {
            type = args[1];
        }
        sender.sendMessage(jobInfoMessage(jPlayer, job, type).split("\n"));
        return true;
    }
    
    public boolean stats(CommandSender sender, String[] args) {
        if (!(sender instanceof CommandSender))
            return false;

        JobsPlayer jPlayer = null;
        if (args.length >= 1) {
            if (!sender.hasPermission("jobs.command.admin.stats")) {
                sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.permission"));
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
            sender.sendMessage(Language.getMessage("command.stats.error.job"));
            return true;
        }
        
        for (JobProgression jobProg: jPlayer.getJobProgression()){
            sender.sendMessage(jobStatsMessage(jobProg).split("\n"));
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
                jobs.add(job.getChatColor() + job.getName() + ChatColor.WHITE + " - max lvl: " + job.getMaxLevel());
            } else {
                jobs.add(job.getChatColor() + job.getName());
            }
        }
        
        if (jobs.size() == 0) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.browse.error.nojobs"));
            return true;   
        }
        sender.sendMessage(Language.getMessage("command.browse.output.header"));
        for(String job : jobs) {
            sender.sendMessage("    "+job);
        }
        sender.sendMessage(Language.getMessage("command.browse.output.footer"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        String type = "";
        if (args.length >= 3) {
            type = args[2];
        }
        sender.sendMessage(jobInfoMessage(jPlayer, job, type).split("\n"));
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
            sender.sendMessage(Language.getMessage("command.admin.success"));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        if (!jPlayer.isInJob(job)) {
            String message = ChatColor.RED + Language.getMessage("command.fire.error.nojob");
            message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.RED);
            sender.sendMessage(message);
            return true;
        }
        try {
            plugin.getPlayerManager().leaveJob(jPlayer, job);
            if (player != null) {
                String message = Language.getMessage("command.fire.output.target");
                message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
                sender.sendMessage(message);
            }
            
            sender.sendMessage(Language.getMessage("command.admin.success"));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        if (jPlayer.isInJob(job)) {
            // already in job message
            String message = ChatColor.RED + Language.getMessage("command.employ.error.alreadyin");
            message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.RED);
            sender.sendMessage(message);
            return true;
        }
        try {
            // check if player already has the job
            plugin.getPlayerManager().joinJob(jPlayer, job);
            if(player != null) {
                String message = Language.getMessage("command.employ.output.target");
                message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
                sender.sendMessage(message);
            }
            sender.sendMessage(Language.getMessage("command.admin.success"));
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        try {
            // check if player already has the job
            if (jPlayer.isInJob(job)) {
                Integer levelsGained = Integer.parseInt(args[2]);
                plugin.getPlayerManager().promoteJob(jPlayer, job, levelsGained);
                
                if (player != null) {
                    String message = Language.getMessage("command.promote.output.target");
                    message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
                    message = message.replace("%levelsgained%", Integer.valueOf(levelsGained).toString());
                    player.sendMessage(message);
                }
                
                sender.sendMessage(Language.getMessage("command.admin.success"));
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        try {
            // check if player already has the job
            if (jPlayer.isInJob(job)) {
                Integer levelsLost = Integer.parseInt(args[2]);
                plugin.getPlayerManager().demoteJob(jPlayer, job, levelsLost);
                
                if (player != null) {
                    String message = Language.getMessage("command.demote.output.target");
                    message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
                    message = message.replace("%levelslost%", Integer.valueOf(levelsLost).toString());
                    player.sendMessage(message);
                }
                
                sender.sendMessage(Language.getMessage("command.admin.success"));
            }
        }
        catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        double xpGained;
        try {
            xpGained = Double.parseDouble(args[2]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
            return true;
        }
        if (xpGained <= 0) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
            return true;
        }
        // check if player already has the job
        if (jPlayer.isInJob(job)) {
            plugin.getPlayerManager().addExperience(jPlayer, job, xpGained);
            
            if (player != null) {
                String message = Language.getMessage("command.grantxp.output.target");
                message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
                message = message.replace("%xpgained%", Double.valueOf(xpGained).toString());
            }
            
            sender.sendMessage(Language.getMessage("command.admin.success"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        double xpLost;
        try {
            xpLost = Double.parseDouble(args[2]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
            return true;
        }
        if (xpLost <= 0) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
            return true;
        }
        // check if player already has the job
        if (jPlayer.isInJob(job)) {
            plugin.getPlayerManager().removeExperience(jPlayer, job, xpLost);
            
            if (player != null) {
                String message = Language.getMessage("command.removexp.output.target");
                message = message.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
                message = message.replace("%xplost%", Double.valueOf(xpLost).toString());
                player.sendMessage(message);
            }
            
            sender.sendMessage(Language.getMessage("command.admin.success"));
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
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        if (newjob == null) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.error.job"));
            return true;
        }
        try {
            if(jPlayer.isInJob(oldjob) && !jPlayer.isInJob(newjob)) {
                plugin.getPlayerManager().transferJob(jPlayer, oldjob, newjob);
                
                if (player != null) {
                    String message = Language.getMessage("command.transfer.output.target");
                    message = message.replace("%oldjobname%", oldjob.getChatColor() + oldjob.getName() + ChatColor.WHITE);
                    message = message.replace("%newjobname%", newjob.getChatColor() + newjob.getName() + ChatColor.WHITE);
                    player.sendMessage(message);
                }
                sender.sendMessage(Language.getMessage("command.admin.success"));
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + Language.getMessage("command.admin.error"));
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
            return ChatColor.RED + Language.getMessage("command.error.job");
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
                    String myMessage = Language.getMessage("command.info.output." + actionType.getName().toLowerCase() + ".none");
                    myMessage = myMessage.replace("%jobname%", job.getChatColor() + job.getName() + ChatColor.WHITE);
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
        message.append(Language.getMessage("command.info.output." + type.getName().toLowerCase()));
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
        String message = Language.getMessage("command.stats.output");
        message = message.replace("%joblevel%", Integer.valueOf(jobProg.getLevel()).toString());
        message = message.replace("%jobname%", jobProg.getJob().getChatColor() + jobProg.getJob().getName() + ChatColor.WHITE);
        message = message.replace("%jobxp%", Integer.toString((int)jobProg.getExperience()));
        message = message.replace("%jobmaxxp%", Integer.toString(jobProg.getMaxExperience()));
        return message;
    }
}
