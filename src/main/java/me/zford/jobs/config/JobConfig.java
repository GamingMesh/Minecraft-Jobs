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

package me.zford.jobs.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import me.zford.jobs.Jobs;
import me.zford.jobs.actions.ActionType;
import me.zford.jobs.config.container.DisplayMethod;
import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobInfo;
import me.zford.jobs.config.container.JobPermission;
import me.zford.jobs.resources.jfep.Parser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public class JobConfig {
    // all of the possible jobs
    private ArrayList<Job> jobs = new ArrayList<Job>();
    // the "none" job
    private Job noneJob = null;
    // used slots for each job
    private WeakHashMap<Job, Integer> usedSlots = new WeakHashMap<Job, Integer>();
    
    
    private Jobs plugin;
    public JobConfig(Jobs plugin) {
        this.plugin = plugin;
    }
    
    public void reload() {
        // job settings
        loadJobSettings();
        // get slots
        loadSlots();
    }
    
    /**
     * Method to load the jobs configuration
     * 
     * loads from Jobs/jobConfig.yml
     */
    private void loadJobSettings(){
        File f = new File(plugin.getDataFolder(), "jobConfig.yml");
        this.jobs.clear();
        this.noneJob = null;
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to create jobConfig.yml!  No jobs were loaded!");
                return;
            }
        }
        YamlConfiguration conf = new YamlConfiguration();
        conf.options().pathSeparator('/');
        conf.options().header(new StringBuilder()
            .append("Jobs configuration.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Stores information about each job.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("For example configurations, visit http://dev.bukkit.org/server-mods/jobs/.").append(System.getProperty("line.separator"))
            .toString());
        try {
            conf.load(f);
        } catch (Exception e) {
            plugin.getServer().getLogger().severe("==================== Jobs ====================");
            plugin.getServer().getLogger().severe("Unable to load jobConfig.yml!");
            plugin.getServer().getLogger().severe("Check your config for formatting issues!");
            plugin.getServer().getLogger().severe("No jobs were loaded!");
            plugin.getServer().getLogger().severe("Error: "+e.getMessage());
            plugin.getServer().getLogger().severe("==============================================");
            return;
        }
        ConfigurationSection jobsSection = conf.getConfigurationSection("Jobs");
        if (jobsSection == null) {
            jobsSection = conf.createSection("Jobs");
        }
        for (String jobKey : jobsSection.getKeys(false)) {
            ConfigurationSection jobSection = jobsSection.getConfigurationSection(jobKey);
            String jobName = jobSection.getString("fullname");
            if (jobName == null) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid fullname property. Skipping job!");
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
                plugin.getLogger().severe("Job " + jobKey + " is missing the shortname property.  Skipping job!");
                continue;
            }

            ChatColor jobColour;
            try {
                jobColour = ChatColor.valueOf(jobSection.getString("ChatColour", "").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid ChatColour property.  Skipping job!");
                continue;
            }
            DisplayMethod displayMethod = DisplayMethod.matchMethod(jobSection.getString("chat-display", ""));
            if (displayMethod == null) {
                plugin.getLogger().warning("Job " + jobKey + " has an invalid chat-display property. Defaulting to None!");
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
                plugin.getLogger().severe("Job " + jobKey + " has an invalid leveling-progression-equation property. Skipping job!");
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
                plugin.getLogger().severe("Job " + jobKey + " has an invalid income-progression-equation property. Skipping job!");
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
                plugin.getLogger().severe("Job " + jobKey + " has an invalid experience-progression-equation property. Skipping job!");
                continue;
            }
            
            // items
            
            // break
            ConfigurationSection breakSection = jobSection.getConfigurationSection("Break");
            ArrayList<JobInfo> jobBreakInfo = new ArrayList<JobInfo>();
            if (breakSection != null) {
                for (String breakKey : breakSection.getKeys(false)) {
                    ConfigurationSection breakItem = breakSection.getConfigurationSection(breakKey);
                    String materialType = breakKey.toUpperCase();
                    String subType = "";
                    
                    if (materialType.contains("-")) {
                        // uses subType
                        subType = ":" + materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    Material material = Material.matchMaterial(materialType);
                    if (material == null) {
                        // try integer method
                        Integer matId = null;
                        try {
                            matId = Integer.decode(materialType);
                        } catch (NumberFormatException e) {}
                        if (matId != null) {
                            material = Material.getMaterial(matId);
                        }
                    }
                    
                    if (material == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + breakKey + " Break material type property. Skipping!");
                        continue;
                    }
                    
                    double income = breakItem.getDouble("income", 0.0);
                    double experience = breakItem.getDouble("experience", 0.0);
                    
                    jobBreakInfo.add(new JobInfo(material.toString()+subType, income, incomeEquation, experience, expEquation));
                }
            }
            
            // place
            ConfigurationSection placeSection = jobSection.getConfigurationSection("Place");
            ArrayList<JobInfo> jobPlaceInfo = new ArrayList<JobInfo>();
            if (placeSection != null) {
                for (String placeKey : placeSection.getKeys(false)) {
                    ConfigurationSection placeItem = placeSection.getConfigurationSection(placeKey);
                    String materialType = placeKey.toUpperCase();
                    String subType = "";
                    
                    if (materialType.contains("-")) {
                        // uses subType
                        subType = ":" + materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    Material material = Material.matchMaterial(materialType);
                    if (material == null) {
                        // try integer method
                        Integer matId = null;
                        try {
                            matId = Integer.decode(materialType);
                        } catch (NumberFormatException e) {}
                        if (matId != null) {
                            material = Material.getMaterial(matId);
                        }
                    }
                    
                    if(material == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + placeKey + " Place material type property. Skipping!");
                        continue;
                    }
                    
                    double income = placeItem.getDouble("income", 0.0);
                    double experience = placeItem.getDouble("experience", 0.0);
                    
                    jobPlaceInfo.add(new JobInfo(material.toString()+subType, income, incomeEquation, experience, expEquation));
                }
            }
            
            // craft
            ConfigurationSection craftSection = jobSection.getConfigurationSection("Craft");
            ArrayList<JobInfo> jobCraftInfo = new ArrayList<JobInfo>();
            if (craftSection != null) {
                for (String craftKey : craftSection.getKeys(false)) {
                    ConfigurationSection craftItem = craftSection.getConfigurationSection(craftKey);
                    String materialType = craftKey.toUpperCase();
                    String subType = "";
                    
                    if (materialType.contains("-")) {
                        // uses subType
                        subType = ":" + materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    Material material = Material.matchMaterial(materialType);
                    if (material == null) {
                        // try integer method
                        Integer matId = null;
                        try {
                            matId = Integer.decode(materialType);
                        } catch (NumberFormatException e) {}
                        if (matId != null) {
                            material = Material.getMaterial(matId);
                        }
                    }
                    
                    if(material == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + craftKey + " Craft material type property. Skipping!");
                        continue;
                    }
                    
                    double income = craftItem.getDouble("income", 0.0);
                    double experience = craftItem.getDouble("experience", 0.0);

                    jobCraftInfo.add(new JobInfo(material.toString()+subType, income, incomeEquation, experience, expEquation));
                }
            }
            
            // smelt
            ConfigurationSection smeltSection = jobSection.getConfigurationSection("Smelt");
            ArrayList<JobInfo> jobSmeltInfo = new ArrayList<JobInfo>();
            if (smeltSection != null) {
                for (String smeltKey : smeltSection.getKeys(false)) {
                    ConfigurationSection smeltItem = smeltSection.getConfigurationSection(smeltKey);
                    String materialType = smeltKey.toUpperCase();
                    String subType = "";
                    
                    if (materialType.contains("-")) {
                        // uses subType
                        subType = ":" + materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    Material material = Material.matchMaterial(materialType);
                    if (material == null) {
                        // try integer method
                        Integer matId = null;
                        try {
                            matId = Integer.decode(materialType);
                        } catch (NumberFormatException e) {}
                        if (matId != null) {
                            material = Material.getMaterial(matId);
                        }
                    }
                    
                    if(material == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + smeltKey + " Smelt material type property. Skipping!");
                        continue;
                    }
                    
                    double income = smeltItem.getDouble("income", 0.0);
                    double experience = smeltItem.getDouble("experience", 0.0);
                    
                    jobSmeltInfo.add(new JobInfo(material.toString()+subType, income, incomeEquation, experience, expEquation));
                }
            }
            
            // kill
            ConfigurationSection killSection = jobSection.getConfigurationSection("Kill");
            ArrayList<JobInfo> jobKillInfo = new ArrayList<JobInfo>();
            if (killSection != null) {
                for (String killKey : killSection.getKeys(false)) {
                    ConfigurationSection killItem = killSection.getConfigurationSection(killKey);
                    EntityType type = EntityType.fromName(killKey.toUpperCase());
                    if (type == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + killKey + " Kill entity type property. Skipping!");
                        continue;
                    }
                    
                    double income = killItem.getDouble("income", 0.0);
                    double experience = killItem.getDouble("experience", 0.0);
                    
                    jobKillInfo.add(new JobInfo(type.toString(), income, incomeEquation, experience, expEquation));
                }
            }
            
            // fish
            ConfigurationSection fishSection = jobSection.getConfigurationSection("Fish");
            ArrayList<JobInfo> jobFishInfo = new ArrayList<JobInfo>();
            if (fishSection != null) {
                for (String fishKey : fishSection.getKeys(false)) {
                    ConfigurationSection fishItem = fishSection.getConfigurationSection(fishKey);
                    String materialType = fishKey.toUpperCase();
                    String subType = "";
                    
                    if (materialType.contains("-")) {
                        // uses subType
                        subType = ":" + materialType.split("-")[1];
                        materialType = materialType.split("-")[0];
                    }
                    Material material = Material.matchMaterial(materialType);
                    if (material == null) {
                        // try integer method
                        Integer matId = null;
                        try {
                            matId = Integer.decode(materialType);
                        } catch (NumberFormatException e) {}
                        if (matId != null) {
                            material = Material.getMaterial(matId);
                        }
                    }
                    
                    if(material == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + fishKey + " Fish material type property. Skipping!");
                        continue;
                    }
                    
                    double income = fishItem.getDouble("income", 0.0);
                    double experience = fishItem.getDouble("experience", 0.0);
                    
                    jobKillInfo.add(new JobInfo(material.toString()+subType, income, incomeEquation, experience, expEquation));
                }
            }
            
            // Permissions
            ArrayList<JobPermission> jobPermissions = new ArrayList<JobPermission>();
            ConfigurationSection permissionsSection = jobSection.getConfigurationSection("permissions");
            if(permissionsSection != null) {
                for(String permissionKey : permissionsSection.getKeys(false)) {
                    ConfigurationSection permissionSection = permissionsSection.getConfigurationSection(permissionKey);
                    
                    String node = permissionKey.toLowerCase();
                    if (permissionSection == null) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid permission key" + permissionKey + "!");
                        continue;
                    }
                    boolean value = permissionSection.getBoolean("value", true);
                    int levelRequirement = permissionSection.getInt("level", 0);
                    jobPermissions.add(new JobPermission(node, value, levelRequirement));
                }
            }
            
            Job job = new Job(jobPermissions, jobName, jobShortName, jobColour, maxExpEquation, displayMethod, maxLevel, maxSlots);
            
            job.setJobInfo(ActionType.BREAK, jobBreakInfo);
            job.setJobInfo(ActionType.PLACE, jobPlaceInfo);
            job.setJobInfo(ActionType.KILL, jobKillInfo);
            job.setJobInfo(ActionType.FISH, jobFishInfo);
            job.setJobInfo(ActionType.CRAFT, jobCraftInfo);
            job.setJobInfo(ActionType.SMELT, jobSmeltInfo);
            
            if (jobKey.equalsIgnoreCase("none")) {
                this.noneJob = job;
            } else {
                this.jobs.add(job);
            }
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Load the slots available
     */
    private void loadSlots() {
        usedSlots.clear();
        for (Job job: jobs) {
            usedSlots.put(job, plugin.getJobsConfiguration().getJobsDAO().getSlotsTaken(job));
        }
    }
    
    /**
     * Function to return the job information that matches the jobName given
     * @param jobName - the ame of the job given
     * @return the job that matches the name
     */
    public Job getJob(String jobName) {
        for (Job job : jobs) {
            if (job.getName().equalsIgnoreCase(jobName))
                return job;
        }
        return null;
    }
    
    public Job getNoneJob() {
        return noneJob;
    }
    
    /**
     * Get all the jobs loaded in the plugin
     * @return a collection of the jobs
     */
    public List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }
    
    /**
     * Function to get the number of slots used on the server for this job
     * @param job - the job
     * @return the number of slots
     */
    public int getUsedSlots(Job job){
        return usedSlots.get(job);
    }
    
    /**
     * Function to increase the number of used slots for a job
     * @param job - the job someone is taking
     */
    public void takeSlot(Job job){
        usedSlots.put(job, usedSlots.get(job)+1);
    }
    
    /**
     * Function to decrease the number of used slots for a job
     * @param job - the job someone is leaving
     */
    public void leaveSlot(Job job){
        usedSlots.put(job, usedSlots.get(job)-1);
    }
}
