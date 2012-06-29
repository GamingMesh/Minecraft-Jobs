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

package me.zford.jobs.bukkit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.DisplayMethod;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobInfo;
import me.zford.jobs.container.JobPermission;
import me.zford.jobs.resources.jfep.Parser;
import me.zford.jobs.util.ChatColor;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class JobConfig {
    private JobsPlugin plugin;
    public JobConfig(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void reload() {
        // job settings
        loadJobSettings();
    }
    
    /**
     * Method to load the jobs configuration
     * 
     * loads from Jobs/jobConfig.yml
     */
    private void loadJobSettings(){
        File f = new File(plugin.getJobsCore().getDataFolder(), "jobConfig.yml");
        ArrayList<Job> jobs = new ArrayList<Job>();
        plugin.getJobsCore().setJobs(jobs);
        plugin.getJobsCore().setNoneJob(null);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                plugin.getJobsCore().getPluginLogger().severe("Unable to create jobConfig.yml!  No jobs were loaded!");
                return;
            }
        }
        YamlConfiguration conf = new YamlConfiguration();
        conf.options().pathSeparator('/');
        try {
            conf.load(f);
        } catch (Exception e) {
            plugin.getJobsCore().getServerLogger().severe("==================== Jobs ====================");
            plugin.getJobsCore().getServerLogger().severe("Unable to load jobConfig.yml!");
            plugin.getJobsCore().getServerLogger().severe("Check your config for formatting issues!");
            plugin.getJobsCore().getServerLogger().severe("No jobs were loaded!");
            plugin.getJobsCore().getServerLogger().severe("Error: "+e.getMessage());
            plugin.getJobsCore().getServerLogger().severe("==============================================");
            return;
        }
        conf.options().header(new StringBuilder()
            .append("Jobs configuration.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Stores information about each job.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("For example configurations, visit http://dev.bukkit.org/server-mods/jobs/.").append(System.getProperty("line.separator"))
            .toString());
        ConfigurationSection jobsSection = conf.getConfigurationSection("Jobs");
        if (jobsSection == null) {
            jobsSection = conf.createSection("Jobs");
        }
        for (String jobKey : jobsSection.getKeys(false)) {
            ConfigurationSection jobSection = jobsSection.getConfigurationSection(jobKey);
            String jobName = jobSection.getString("fullname");
            if (jobName == null) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid fullname property. Skipping job!");
                continue;
            }
            
            int maxLevel = jobSection.getInt("max-level", 0);
            if (maxLevel < 0)
                maxLevel = 0;

            Integer maxSlots = jobSection.getInt("slots", 0);
            if (maxSlots.intValue() <= 0) {
                maxSlots = null;
            }

            String jobShortName = jobSection.getString("shortname");
            if (jobShortName == null) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " is missing the shortname property.  Skipping job!");
                continue;
            }

            ChatColor color = ChatColor.matchColor(jobSection.getString("ChatColour", ""));
            if (color == null) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid ChatColour property.  Skipping job!");
                continue;
            }
            DisplayMethod displayMethod = DisplayMethod.matchMethod(jobSection.getString("chat-display", ""));
            if (displayMethod == null) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid chat-display property. Defaulting to None!");
                displayMethod = DisplayMethod.NONE;
            }
            
            Parser maxExpEquation;
            String maxExpEquationInput = jobSection.getString("leveling-progression-equation");
            try {
                maxExpEquation = new Parser(maxExpEquationInput);
                // test equation
                maxExpEquation.setVariable("numjobs", 1);
                maxExpEquation.setVariable("joblevel", 1);
                maxExpEquation.getValue();
            } catch(Exception e) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid leveling-progression-equation property. Skipping job!");
                continue;
            }
            
            Parser incomeEquation;
            String incomeEquationInput = jobSection.getString("income-progression-equation");
            try {
                incomeEquation = new Parser(incomeEquationInput);
                // test equation
                incomeEquation.setVariable("numjobs", 1);
                incomeEquation.setVariable("joblevel", 1);
                incomeEquation.setVariable("baseincome", 1);
                incomeEquation.getValue();
            } catch(Exception e) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid income-progression-equation property. Skipping job!");
                continue;
            }
            
            Parser expEquation;
            String expEquationInput = jobSection.getString("experience-progression-equation");
            try {
                expEquation = new Parser(expEquationInput);
                // test equation
                expEquation.setVariable("numjobs", 1);
                expEquation.setVariable("joblevel", 1);
                expEquation.setVariable("baseexperience", 1);
                expEquation.getValue();
            } catch(Exception e) {
                plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid experience-progression-equation property. Skipping job!");
                continue;
            }
            
            // Permissions
            ArrayList<JobPermission> jobPermissions = new ArrayList<JobPermission>();
            ConfigurationSection permissionsSection = jobSection.getConfigurationSection("permissions");
            if(permissionsSection != null) {
                for(String permissionKey : permissionsSection.getKeys(false)) {
                    ConfigurationSection permissionSection = permissionsSection.getConfigurationSection(permissionKey);
                    
                    String node = permissionKey.toLowerCase();
                    if (permissionSection == null) {
                        plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid permission key" + permissionKey + "!");
                        continue;
                    }
                    boolean value = permissionSection.getBoolean("value", true);
                    int levelRequirement = permissionSection.getInt("level", 0);
                    jobPermissions.add(new JobPermission(node, value, levelRequirement));
                }
            }
            
            Job job = new Job(jobPermissions, jobName, jobShortName, color, maxExpEquation, displayMethod, maxLevel, maxSlots);
            
            for (ActionType actionType : ActionType.values()) {
                ConfigurationSection typeSection = jobSection.getConfigurationSection(actionType.getName());
                ArrayList<JobInfo> jobInfo = new ArrayList<JobInfo>();
                if (typeSection != null) {
                    for (String key : typeSection.getKeys(false)) {
                        ConfigurationSection section = typeSection.getConfigurationSection(key);
                        String myKey = key.toUpperCase();
                        String type = null;
                        String subType = "";
                        
                        if (myKey.contains("-")) {
                            // uses subType
                            subType = ":" + myKey.split("-")[1];
                            myKey = myKey.split("-")[0];
                        }
                        Material material = Material.matchMaterial(myKey);
                        if (material == null) {
                            // try integer method
                            Integer matId = null;
                            try {
                                matId = Integer.decode(myKey);
                            } catch (NumberFormatException e) {}
                            if (matId != null) {
                                material = Material.getMaterial(matId);
                            }
                        }
                        
                        if (material != null) {
                            type = material.toString();
                        } else {
                            // check entities
                            EntityType entity = EntityType.fromName(key);
                            if (entity == null) {
                                try {
                                    entity = EntityType.valueOf(key.toUpperCase());
                                } catch (IllegalArgumentException e) {}
                            }
                            
                            if (entity != null && entity.isAlive())
                                type = entity.toString();
                        }
                        
                        if (type == null) {
                            plugin.getJobsCore().getPluginLogger().warning("Job " + jobKey + " has an invalid " + key + " " + actionType.getName() + " type property. Skipping!");
                            continue;
                        }
                        
                        double income = section.getDouble("income", 0.0);
                        double experience = section.getDouble("experience", 0.0);
                        
                        jobInfo.add(new JobInfo(type+subType, income, incomeEquation, experience, expEquation));
                    }
                }
                job.setJobInfo(actionType, jobInfo);
            }
            
            if (jobKey.equalsIgnoreCase("none")) {
                plugin.getJobsCore().setNoneJob(job);
            } else {
                jobs.add(job);
            }
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
