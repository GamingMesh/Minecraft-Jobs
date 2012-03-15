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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.DisplayMethod;
import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobsLivingEntityInfo;
import me.zford.jobs.config.container.JobsMaterialInfo;
import me.zford.jobs.resources.jfep.Parser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.material.MaterialData;

public class JobConfig {
    // all of the possible jobs
    private LinkedHashMap<String, Job> jobs = new LinkedHashMap<String, Job>();
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
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration conf = new YamlConfiguration();
        conf.options().header(new StringBuilder()
            .append("Jobs configuration.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Stores information about each job.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("For example configurations, visit http://dev.bukkit.org/server-mods/jobs/.").append(System.getProperty("line.separator"))
            .toString());
        try {
            conf.load(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        ConfigurationSection jobSection = conf.getConfigurationSection("Jobs");
        if (jobSection == null) {
            jobSection = conf.createSection("Jobs");
        }
        for(String jobKey : jobSection.getKeys(false)) {
            String jobName = conf.getString("Jobs."+jobKey+".fullname");
            if (jobName == null) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid fullname property. Skipping job!");
                continue;
            }
            
            Integer maxLevel = conf.getInt("Jobs."+jobKey+".max-level", 0);
            if (maxLevel.intValue() <= 0) {
                maxLevel = null;
            }

            Integer maxSlots = conf.getInt("Jobs."+jobKey+".slots", 0);
            if (maxSlots.intValue() <= 0) {
                maxSlots = null;
            }

            String jobShortName = conf.getString("Jobs."+jobKey+".shortname");
            if (jobShortName == null) {
                plugin.getLogger().severe("Job " + jobKey + " is missing the shortname property.  Skipping job!");
                continue;
            }

            ChatColor jobColour;
            try {
                jobColour = ChatColor.valueOf(conf.getString("Jobs."+jobKey+".ChatColour", "").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid ChatColour property.  Skipping job!");
                continue;
            }
            String disp = conf.getString("Jobs."+jobKey+".chat-display", "");
            DisplayMethod displayMethod;
            if (disp.equalsIgnoreCase("full")) {
                displayMethod = DisplayMethod.FULL;
            } else if(disp.equalsIgnoreCase("job")) {
                displayMethod = DisplayMethod.JOB;
            } else if(disp.equalsIgnoreCase("title")) {
                displayMethod = DisplayMethod.TITLE;
            } else if(disp.equalsIgnoreCase("none")) {
                displayMethod = DisplayMethod.NONE;
            } else if(disp.equalsIgnoreCase("shortfull")) {
                displayMethod = DisplayMethod.SHORT_FULL;
            } else if(disp.equalsIgnoreCase("shortjob")) {
                displayMethod = DisplayMethod.SHORT_JOB;
            } else if(disp.equalsIgnoreCase("shorttitle")) {
                displayMethod = DisplayMethod.SHORT_TITLE;
            } else {
                plugin.getLogger().warning("Job " + jobKey + " has an invalid chat-display property. Defaulting to None!");
                displayMethod = DisplayMethod.NONE;
            }
            
            Parser maxExpEquation;
            String maxExpEquationInput = conf.getString("Jobs."+jobKey+".leveling-progression-equation");
            try {
                maxExpEquation = new Parser(maxExpEquationInput);
            } catch(Exception e) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid leveling-progression-equation property. Skipping job!");
                continue;
            }
            
            Parser incomeEquation;
            String incomeEquationInput = conf.getString("Jobs."+jobKey+".income-progression-equation");
            try {
                incomeEquation = new Parser(incomeEquationInput);
            } catch(Exception e) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid income-progression-equation property. Skipping job!");
                continue;
            }
            
            Parser expEquation;
            String expEquationInput = conf.getString("Jobs."+jobKey+".experience-progression-equation");
            try {
                expEquation = new Parser(expEquationInput);
            } catch(Exception e) {
                plugin.getLogger().severe("Job " + jobKey + " has an invalid experience-progression-equation property. Skipping job!");
                continue;
            }
            
            // items
            
            // break
            ConfigurationSection breakSection = conf.getConfigurationSection("Jobs."+jobKey+".Break");
            HashMap<String, JobsMaterialInfo> jobBreakInfo = new HashMap<String, JobsMaterialInfo>();
            if(breakSection != null) {
                for (String breakKey : breakSection.getKeys(false)) {
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
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Break."+breakKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Break."+breakKey+".experience", 0.0);
                    
                    jobBreakInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // place
            ConfigurationSection placeSection = conf.getConfigurationSection("Jobs."+jobKey+".Place");
            HashMap<String, JobsMaterialInfo> jobPlaceInfo = new HashMap<String, JobsMaterialInfo>();
            if(placeSection != null) {
                for(String placeKey : placeSection.getKeys(false)) {
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
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Place."+placeKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Place."+placeKey+".experience", 0.0);
                    
                    jobPlaceInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // craft
            ConfigurationSection craftSection = conf.getConfigurationSection("Jobs."+jobKey+".Craft");
            HashMap<String, JobsMaterialInfo> jobCraftInfo = new HashMap<String, JobsMaterialInfo>();
            if(craftSection != null) {
                for(String craftKey : craftSection.getKeys(false)) {
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
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Craft."+craftKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Craft."+craftKey+".experience", 0.0);
                    
                    jobCraftInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // kill
            ConfigurationSection killSection = conf.getConfigurationSection("Jobs."+jobKey+".Kill");
            HashMap<String, JobsLivingEntityInfo> jobKillInfo = new HashMap<String, JobsLivingEntityInfo>();
            if(killSection != null) {
                for(String killKey : killSection.getKeys(false)) {
                    @SuppressWarnings("rawtypes")
                    Class victim;
                    try {
                        victim = Class.forName("org.bukkit.craftbukkit.entity.Craft"+killKey);
                    } catch (ClassNotFoundException e) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + killKey + " Kill entity type property. Skipping!");
                        continue;
                    }
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Kill."+killKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Kill."+killKey+".experience", 0.0);
                    
                    jobKillInfo.put(("org.bukkit.craftbukkit.entity.Craft"+killKey).trim(), new JobsLivingEntityInfo(victim, experience, income));
                }
            }
            
            // fish
            ConfigurationSection fishSection = conf.getConfigurationSection("Jobs."+jobKey+".Fish");
            HashMap<String, JobsMaterialInfo> jobFishInfo = new HashMap<String, JobsMaterialInfo>();
            if(fishSection != null) {
                for(String fishKey : fishSection.getKeys(false)) {
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
                    MaterialData materialData = new MaterialData(material);
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".Fish."+fishKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".Fish."+fishKey+".experience", 0.0);
                    
                    jobFishInfo.put(material.toString()+subType, new JobsMaterialInfo(materialData, experience, income));
                }
            }
            
            // custom-kill
            ConfigurationSection customKillSection = conf.getConfigurationSection("Jobs."+jobKey+".custom-kill");
            if(customKillSection != null) {
                for(String customKillKey : customKillSection.getKeys(false)) {
                    String entityType = customKillKey.toString();
                    
                    Double income = conf.getDouble("Jobs."+jobKey+".custom-kill."+customKillKey+".income", 0.0);
                    Double experience = conf.getDouble("Jobs."+jobKey+".custom-kill."+customKillKey+".experience", 0.0);
                    
                    try {
                        jobKillInfo.put(("org.bukkit.craftbukkit.entity.CraftPlayer:"+entityType).trim(), new JobsLivingEntityInfo(Class.forName("org.bukkit.craftbukkit.entity.CraftPlayer"), experience, income));
                    } catch (ClassNotFoundException e) {
                        plugin.getLogger().severe("Job " + jobKey + " has an invalid " + customKillKey + " custom-kill entity type property. Skipping!");
                        continue;
                    }
                }
            }
            
            boolean isHidden = false;
            if (jobKey.equalsIgnoreCase("none")) {
                isHidden = true;
            }
            
            this.jobs.put(jobName.toLowerCase(), new Job(jobBreakInfo, jobPlaceInfo, jobKillInfo, jobFishInfo, jobCraftInfo, jobName, jobShortName, jobColour, maxExpEquation, incomeEquation, expEquation, displayMethod, maxLevel, maxSlots, isHidden));
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
        for(Job temp: jobs.values()){
            usedSlots.put(temp, plugin.getJobsConfiguration().getJobsDAO().getSlotsTaken(temp));
        }
    }
    
    /**
     * Function to return the job information that matches the jobName given
     * @param jobName - the ame of the job given
     * @return the job that matches the name
     */
    public Job getJob(String jobName){
        return jobs.get(jobName.toLowerCase());
    }
    
    /**
     * Get all the jobs loaded in the plugin
     * @return a collection of the jobs
     */
    public Collection<Job> getJobs() {
        return Collections.unmodifiableCollection(jobs.values());
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
