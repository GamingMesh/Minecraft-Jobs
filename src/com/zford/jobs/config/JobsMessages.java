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

package com.zford.jobs.config;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.util.config.Configuration;

/**
 * Message configuration class.
 * 
 * Message configuration data for jobs plugin
 * @author Zak Ford <zak.j.ford@gmail.com>
 *
 */

public class JobsMessages {
    
    private enum JobsMessageEnum {
        ADMIN_COMMAND_FAILED,
        ADMIN_COMMAND_SUCCESS,
        AT_MAX_LEVEL,
        BREAK_HEADER,
        BREAK_INFO_NO_SUB,
        BREAK_INFO_SUB,
        BREAK_NONE,
        BROWSE_JOBS_FOOTER,
        BROWSE_JOBS_HEADER,
        BROWSE_NO_JOBS,
        DEMOTE_TARGET,
        EMPLOY_TARGET,
        ERROR_NO_JOB,
        ERROR_NO_PERMISSION,
        FIRE_TARGET,
        FIRE_TARGET_NO_JOB,
        FISH_HEADER,
        FISH_INFO_NO_SUB,
        FISH_INFO_SUB,
        FISH_NONE,
        GRANTXP_TARGET,
        JOBS_ADMIN_DEMOTE,
        JOBS_ADMIN_EMPLOY,
        JOBS_ADMIN_FIRE,
        JOBS_ADMIN_GRANTXP,
        JOBS_ADMIN_PROMOTE,
        JOBS_ADMIN_REMOVEXP,
        JOBS_ADMIN_TRANSFER,
        JOBS_BROWSE,
        JOBS_INFO,
        JOBS_JOIN,
        JOBS_LEAVE,
        JOBS_STATS,
        JOIN_JOB_FAILED_ALREADY_IN,
        JOIN_JOB_FAILED_NO_SLOTS,
        JOIN_JOB_SUCCESS,
        JOIN_TOO_MANY_JOB,
        KILL_HEADER,
        KILL_INFO_NO_SUB,
        KILL_INFO_SUB,
        KILL_NONE,
        LEAVE_JOB_FAILED_TOO_MANY,
        LEAVE_JOB_SUCCESS,
        LEVEL_UP,
        PLACE_HEADER,
        PLACE_INFO_NO_SUB,
        PLACE_INFO_SUB,
        PLACE_NONE,
        PROMOTE_TARGET,
        REMOVEXP_TARGET,
        SKILL_UP_BROADCAST,
        SKILL_UP_NO_BROADCAST,
        STATS_JOB,
        STATS_NO_JOB,
        TRANSFER_TARGET;
        
        public static JobsMessageEnum fromString(String text) {
            if(text != null) {
                text = text.replace('-', '_').toUpperCase();
                for(JobsMessageEnum m : JobsMessageEnum.values()) {
                    if(text.equals(m.toString())) {
                        return m;
                    }
                }
            }
            return null;
        }
    }
    
    private static JobsMessages instance = null;

    private HashMap<String, String> messages;
    
    /**
     * Private function to create singleton pattern
     */
    private JobsMessages() {
    }
    
    /**
     * Retrieve instance
     * @return - an instance of JobsMessageConfig
     */
    public static JobsMessages getInstance() {
        if(instance == null) {
            instance = new JobsMessages();
        }
        return instance;
    }
    
    /**
     * Reloads the config
     */
    public void reloadConfig(){
        this.messages = new HashMap<String, String>();
        File f = new File("plugins/Jobs/messageConfig.yml");
        Configuration conf;
        if(!f.exists()) {
            System.err.println("[Jobs] - configuration file messageConfig.yml does not exist, using default messages.");
            return;
        }
        conf = new Configuration(f);
        conf.load();
        List<String> configKeys = conf.getKeys(null);
        if (configKeys == null) {
            return;
        }
        for(String key : configKeys) {
            this.messages.put(key, this.parseColors(conf.getString(key)));
        }
    }
    
    /**
     * Parse ChatColors from YML configuration file
     * @param value - configuration string
     * @return string with replaced colors
     */
    private String parseColors(String value) {
        return value.replace("ChatColor.AQUA", ChatColor.AQUA.toString())
            .replace("ChatColor.BLACK", ChatColor.BLACK.toString())
            .replace("ChatColor.BLUE", ChatColor.BLUE.toString())
            .replace("ChatColor.DARK_AQUA", ChatColor.DARK_AQUA.toString())
            .replace("ChatColor.DARK_BLUE", ChatColor.DARK_BLUE.toString())
            .replace("ChatColor.DARK_GRAY", ChatColor.DARK_GRAY.toString())
            .replace("ChatColor.DARK_GREEN", ChatColor.DARK_GREEN.toString())
            .replace("ChatColor.DARK_PURPLE", ChatColor.DARK_PURPLE.toString())
            .replace("ChatColor.DARK_RED", ChatColor.DARK_RED.toString())
            .replace("ChatColor.GOLD", ChatColor.GOLD.toString())
            .replace("ChatColor.GRAY", ChatColor.GRAY.toString())
            .replace("ChatColor.GREEN", ChatColor.GREEN.toString())
            .replace("ChatColor.LIGHT_PURPLE", ChatColor.LIGHT_PURPLE.toString())
            .replace("ChatColor.RED", ChatColor.RED.toString())
            .replace("ChatColor.WHITE", ChatColor.WHITE.toString())
            .replace("ChatColor.YELLOW", ChatColor.YELLOW.toString());
    }
    
    /**
     * Get the message with the correct key
     * @param key - the key of the message
     * @return the message
     */
    public String getMessage(String key){
        String message = this.messages.get(key);
        if(message != null) {
            return message;
        }
        // We can't find the message in the YML file, use default
        JobsMessageEnum enumKey = JobsMessageEnum.fromString(key);
        switch(enumKey) {
        case ADMIN_COMMAND_FAILED:
            return ChatColor.RED + "There was an error in the command";
        case ADMIN_COMMAND_SUCCESS:
            return "Your command has been performed.";
        case AT_MAX_LEVEL:
            return ChatColor.YELLOW + "-- You have reached the maximum level --";
        case BREAK_HEADER:
            return "Break:";
        case BREAK_INFO_NO_SUB:
            return "    %item% - %income%" + ChatColor.GREEN + " income" + ChatColor.WHITE + ", %experience%" + 
            ChatColor.YELLOW + " exp";
        case BREAK_INFO_SUB:
            return "    %item%:%subitem% - %income%" + ChatColor.GREEN + " income" + ChatColor.WHITE + ", %experience%" + 
                ChatColor.YELLOW + " exp";
        case BREAK_NONE:
            return "%jobcolour%%jobname%" +ChatColor.WHITE+ " does not get money from breaking anything.";
        case BROWSE_JOBS_FOOTER:
            return "For more information type in /jobs info [JobName]";
        case BROWSE_JOBS_HEADER:
            return "You are allowed to join the following jobs:";
        case BROWSE_NO_JOBS:
            return "There are no jobs you can join";
        case DEMOTE_TARGET:
            return "You have been demoted %levelslost% levels in %jobcolour%%jobname%";
        case EMPLOY_TARGET:
            return "You have been employed in %jobcolour%%jobname%";
        case ERROR_NO_JOB:
            return ChatColor.RED + "The job you have selected does not exist";
        case ERROR_NO_PERMISSION:
            return ChatColor.RED + "You do not have permission to do that";
        case FIRE_TARGET:
            return "You have been fired from %jobcolour%%jobname%";
        case FIRE_TARGET_NO_JOB:
            return "Player does not have the job %jobcolour%%jobname%";
        case FISH_HEADER:
            return "Fish:";
        case FISH_INFO_NO_SUB:
            return "    %item% - %income%" + ChatColor.GREEN + " income" + ChatColor.WHITE + ", %experience%" + 
            ChatColor.YELLOW + " exp";
        case FISH_INFO_SUB:
            return "    %item%:%subitem% - %income%" + ChatColor.GREEN + " income" + ChatColor.WHITE + ", %experience%" + 
                ChatColor.YELLOW + " exp";
        case FISH_NONE:
            return "%jobcolour%%jobname%" +ChatColor.WHITE+ " does not get money for fish.";
        case GRANTXP_TARGET:
            return "You have been granted %expgained% experience in %jobcolour%%jobname%";
        case JOBS_ADMIN_DEMOTE:
            return "/jobs demote <playername> <job> <levels> - demote the player X levels in a job";
        case JOBS_ADMIN_EMPLOY:
            return "/jobs employ <playername> <job> - employ the player to the job";
        case JOBS_ADMIN_FIRE:
            return "/jobs fire <playername> <job> - fire the player from the job";
        case JOBS_ADMIN_GRANTXP:
            return "/jobs grantxp <playername> <job> <experience> - grant the player X experience in a job";
        case JOBS_ADMIN_PROMOTE:
            return "/jobs promote <playername> <job> <levels> - promote the player X levels in a job";
        case JOBS_ADMIN_REMOVEXP:
            return "/jobs removexp <playername> <job> <experience> - remove X experience from the player in a job";
        case JOBS_ADMIN_TRANSFER:
            return "/jobs transfer <playername> <oldjob> <newjob> - transfer a player's job from an old job to a new job";
        case JOBS_BROWSE:
            return "/jobs browse - list the jobs available to you";
        case JOBS_INFO:
            return "/jobs info <jobname> <break, place, kill, fish> - show how much each job is getting paid and for what";
        case JOBS_JOIN:
            return "/jobs join <jobname> - join the selected job";
        case JOBS_LEAVE:
            return "/jobs leave <jobname> - leave the selected job";
        case JOBS_STATS:
            return "/jobs stats - show the level you are in each job you are part of";
        case JOIN_JOB_FAILED_ALREADY_IN:
            return "You are already in the job %jobcolour%%jobname%"+ChatColor.WHITE+".";
        case JOIN_JOB_FAILED_NO_SLOTS:
            return "You cannot join the job %jobcolour%%jobname%"+ChatColor.WHITE+", there are no slots available.";
        case JOIN_JOB_SUCCESS:
            return "You have joined the job %jobcolour%%jobname%"+ChatColor.WHITE+".";
        case JOIN_TOO_MANY_JOB:
            return ChatColor.RED + "You have already joined too many jobs.";
        case KILL_HEADER:
            return "Kill:";
        case KILL_INFO_NO_SUB:
            return "    %item% - %income%" + ChatColor.WHITE + ", %experience%" +
                ChatColor.YELLOW + " exp";
        case KILL_INFO_SUB:
            return "    %item%:%subitem% - %income%" + ChatColor.WHITE + ", %experience%" +
                ChatColor.YELLOW + " exp";
        case KILL_NONE:
            return "%jobcolour%%jobname%" +ChatColor.WHITE+ " does not get money from killing anything.";
        case LEAVE_JOB_FAILED_TOO_MANY:
            return "You have already joined too many jobs.";
        case LEAVE_JOB_SUCCESS:
            return "You have left the job %jobcolour%%jobname%"+ChatColor.WHITE+".";
        case LEVEL_UP:
            return ChatColor.YELLOW + "-- Job Level Up --";
        case PLACE_HEADER:
            return "Place:";
        case PLACE_INFO_NO_SUB:
            return "    %item% - %income%" + ChatColor.GREEN + " income" + ChatColor.WHITE + ", %experience%" + 
                ChatColor.YELLOW + " exp";
        case PLACE_INFO_SUB:
            return "    %item%:%subitem% - %income%" + ChatColor.GREEN + " income" + ChatColor.WHITE + ", %experience%" + 
                ChatColor.YELLOW + " exp";
        case PLACE_NONE:
            return "%jobcolour%%jobname%" +ChatColor.WHITE+ " does not get money from placing anything.";
        case PROMOTE_TARGET:
            return "You have been promoted %levelsgained% levels in %jobcolour%%jobname%";
        case REMOVEXP_TARGET:
            return "You have lost %explost% experience in jobcolour%%jobname%";
        case SKILL_UP_BROADCAST:
            return "%playername% has been promoted to a %titlecolour%%titlename% %jobcolour%%jobname%"+ChatColor.WHITE+".";
        case SKILL_UP_NO_BROADCAST:
            return "Congratulations, you have been promoted to a %titlecolour%%titlename% %jobcolour%%jobname%"+ChatColor.WHITE+".";
        case STATS_JOB:
            return "lvl%joblevel% %jobcolour%%jobname%:\n    Experience: %jobexp% / %jobmaxexp%";
        case STATS_NO_JOB:
            return ChatColor.RED + "Please join a job first";
        case TRANSFER_TARGET:
            return "You have been transferred from %oldjobcolour%%oldjobname% to %newjobcolour%%newjobname%";
        default:
            System.err.println("[Jobs] Message "+key+" does not exist!");
            return null;
        }
    }
}
