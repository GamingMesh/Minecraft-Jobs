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

package me.zford.jobs.spout.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.spout.api.material.BlockMaterial;
import org.spout.api.material.Material;
import org.spout.api.util.config.ConfigurationNode;
import org.spout.api.util.config.yaml.YamlConfiguration;

import me.zford.jobs.Jobs;
import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.config.JobConfig;
import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.DisplayMethod;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobInfo;
import me.zford.jobs.container.JobPermission;
import me.zford.jobs.resources.jfep.Parser;
import me.zford.jobs.util.ChatColor;

public class SpoutJobConfig extends JobConfig {
    private JobsPlugin plugin;
    public SpoutJobConfig(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
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
        File f = new File(plugin.getDataFolder(), "jobConfig.yml");
        ArrayList<Job> jobs = new ArrayList<Job>();
        Jobs.setJobs(jobs);
        Jobs.setNoneJob(null);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                Jobs.getPluginLogger().severe("Unable to create jobConfig.yml!  No jobs were loaded!");
                return;
            }
        }
        YamlConfiguration conf = new YamlConfiguration(f);
        conf.setPathSeparator("/");
        try {
            conf.load();
        } catch (Exception e) {
            Jobs.getServer().getLogger().severe("==================== Jobs ====================");
            Jobs.getServer().getLogger().severe("Unable to load jobConfig.yml!");
            Jobs.getServer().getLogger().severe("Check your config for formatting issues!");
            Jobs.getServer().getLogger().severe("No jobs were loaded!");
            Jobs.getServer().getLogger().severe("Error: "+e.getMessage());
            Jobs.getServer().getLogger().severe("==============================================");
            return;
        }
        conf.setHeader(new StringBuilder()
            .append("Jobs configuration.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("Stores information about each job.").append(System.getProperty("line.separator"))
            .append(System.getProperty("line.separator"))
            .append("For example configurations, visit http://dev.bukkit.org/server-mods/jobs/.").append(System.getProperty("line.separator"))
            .toString());
        ConfigurationNode jobsSection = conf.getNode("Jobs");
        if (jobsSection.isAttached()) {
            for (String jobKey : jobsSection.getKeys(false)) {
                ConfigurationNode jobSection = jobsSection.getNode(jobKey);
                String jobName = getNodeString(jobSection, "fullname");
                if (jobName == null) {
                    Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid fullname property. Skipping job!");
                    continue;
                }
                
                int maxLevel = getNodeInt(jobSection, "max-level", 0);
                
                Integer maxSlots = getNodeInt(jobSection, "slots", 0);
                if (maxSlots.intValue() <= 0)
                    maxSlots = null;
                
                String jobShortName = getNodeString(jobSection, "shortname");
                if (jobShortName == null) {
                    Jobs.getPluginLogger().warning("Job " + jobKey + " is missing the shortname property.  Skipping job!");
                    continue;
                }
                
                ChatColor color = ChatColor.WHITE;
                String chatColorString = getNodeString(jobSection, "ChatColour");
                if (chatColorString != null) {
                    color = ChatColor.matchColor(chatColorString);
                    if (color == null) {
                        color = ChatColor.WHITE;
                        Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid ChatColour property.  Defaulting to WHITE!");
                    }
                }
                
                DisplayMethod displayMethod = DisplayMethod.NONE;
                String displayMethodString = getNodeString(jobSection, "chat-display");
                if (displayMethodString != null) {
                    displayMethod = DisplayMethod.matchMethod(displayMethodString);
                    if (displayMethod == null) {
                        Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid chat-display property. Defaulting to None!");
                        displayMethod = DisplayMethod.NONE;
                    }
                }
                
                Parser maxExpEquation = null;
                String maxExpEquationInput = getNodeString(jobSection, "leveling-progression-equation");
                try {
                    if (maxExpEquationInput != null) {
                        maxExpEquation = new Parser(maxExpEquationInput);
                        // test equation
                        maxExpEquation.setVariable("numjobs", 1);
                        maxExpEquation.setVariable("joblevel", 1);
                        maxExpEquation.getValue();
                    }
                } catch(Exception e) {
                    maxExpEquation = null;
                }
                
                if (maxExpEquation == null) {
                    Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid leveling-progression-equation property. Skipping job!");
                    continue;
                }
                
                Parser incomeEquation = null;
                String incomeEquationInput = getNodeString(jobSection, "income-progression-equation");
                try {
                    if (incomeEquationInput != null) {
                        incomeEquation = new Parser(incomeEquationInput);
                        // test equation
                        incomeEquation.setVariable("numjobs", 1);
                        incomeEquation.setVariable("joblevel", 1);
                        incomeEquation.setVariable("baseincome", 1);
                        incomeEquation.getValue();
                    }
                } catch(Exception e) {
                    incomeEquation = null;
                }
                
                if (incomeEquation == null) {
                    Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid income-progression-equation property. Skipping job!");
                    continue;
                }
                
                Parser expEquation = null;
                String expEquationInput = getNodeString(jobSection, "experience-progression-equation");
                try {
                    if (expEquationInput != null) {
                        expEquation = new Parser(expEquationInput);
                        // test equation
                        expEquation.setVariable("numjobs", 1);
                        expEquation.setVariable("joblevel", 1);
                        expEquation.setVariable("baseexperience", 1);
                        expEquation.getValue();
                    }
                } catch(Exception e) {
                    expEquation = null;
                }
                
                if (expEquation == null) {
                    Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid experience-progression-equation property. Skipping job!");
                    continue;
                }
                
                // Permissions
                ArrayList<JobPermission> jobPermissions = new ArrayList<JobPermission>();
                ConfigurationNode permissionsSection = jobSection.getNode("permissions");
                if (permissionsSection.isAttached()) {
                    for (String permissionKey : permissionsSection.getKeys(false)) {
                        ConfigurationNode permissionSection = permissionsSection.getNode(permissionKey);
                        
                        String node = permissionKey.toLowerCase();
                        if (!permissionSection.isAttached()) {
                            Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid permission key" + permissionKey + "!");
                            continue;
                        }
                        boolean value = getNodeBoolean(permissionSection, "value", true);
                        int levelRequirement = getNodeInt(permissionSection, "level", 0);
                        jobPermissions.add(new JobPermission(node, value, levelRequirement));
                    }
                }
                
                Job job = new Job(jobPermissions, jobName, jobShortName, color, maxExpEquation, displayMethod, maxLevel, maxSlots);
                
                for (ActionType actionType : ActionType.values()) {
                    ConfigurationNode typeSection = jobSection.getNode(actionType.getName());
                    ArrayList<JobInfo> jobInfo = new ArrayList<JobInfo>();
                    if (typeSection.isAttached()) {
                        for (String key : typeSection.getKeys(false)) {
                            ConfigurationNode section = typeSection.getNode(key);
                            String myKey = key.toUpperCase();
                            String type = null;
                            
                            Material material = Material.get(myKey);
                            if (material == null) {
                                // try integer method
                                Short matId = null;
                                try {
                                    matId = Short.decode(myKey);
                                } catch (NumberFormatException e) {}
                                if (matId != null) {
                                    material = Material.get(matId.shortValue());
                                }
                            }
                            
                            if (material != null) {
                                // Break and Place actions MUST be blocks
                                if (actionType == ActionType.BREAK || actionType == ActionType.PLACE) {
                                    if (!(material instanceof BlockMaterial)) {
                                        Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid " + actionType.getName() + " type property: " + key + "! Material must be a block!");
                                        continue;
                                    }
                                }
                                
                                type = material.getName();
                            }
                            
                            if (type == null) {
                                Jobs.getPluginLogger().warning("Job " + jobKey + " has an invalid " + actionType.getName() + " type property: " + key + "!");
                                continue;
                            }
                            
                            double income = getNodeDouble(section, "income", 0);
                            double experience = getNodeDouble(section, "experience", 0);
                            
                            jobInfo.add(new JobInfo(type, income, incomeEquation, experience, expEquation));
                        }
                    }
                    job.setJobInfo(actionType, jobInfo);
                }
                
                if (jobKey.equalsIgnoreCase("none")) {
                    Jobs.setNoneJob(job);
                } else {
                    jobs.add(job);
                }
            }
        }
        try {
            conf.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getNodeString(ConfigurationNode node, String key) {
        ConfigurationNode child = node.getNode(key);
        return child.isAttached() ? child.getString() : null; 
    }
    
    public boolean getNodeBoolean(ConfigurationNode node, String key, boolean def) {
        ConfigurationNode child = node.getNode(key);
        return child.isAttached() ? child.getBoolean(def) : def; 
    }
    
    public int getNodeInt(ConfigurationNode node, String key, int def) {
        ConfigurationNode child = node.getNode(key);
        return child.isAttached() ? child.getInt(def) : def; 
    }
    
    public double getNodeDouble(ConfigurationNode node, String key, double def) {
        ConfigurationNode child = node.getNode(key);
        return child.isAttached() ? child.getDouble(def) : def; 
    }
}
