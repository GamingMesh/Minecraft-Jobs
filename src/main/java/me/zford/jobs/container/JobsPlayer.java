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

package me.zford.jobs.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.dao.JobsDAOData;
import me.zford.jobs.util.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class JobsPlayer {
    // jobs plugin
    private JobsPlugin plugin;
    // the player the object belongs to
    private String playername;
    // progression of the player in each job
    private ArrayList<JobProgression> progression = new ArrayList<JobProgression>();
    // display honorific
    private String honorific;
    // permission attachment
    private PermissionAttachment attachment;
        
    /**
     * Constructor.
     * Reads data storage and configures itself.
     * @param plugin - the jobs plugin
     * @param playername - the player this represents
     * @param dao - the data access object
     */
    public JobsPlayer(JobsPlugin plugin, String playername) {
        this.plugin = plugin;
        this.playername = playername;
    }
    
    public void loadDAOData(List<JobsDAOData> list) {
        progression.clear();
        for (JobsDAOData jobdata: list) {
            if (plugin.getJobsCore().getJob(jobdata.getJobName()) != null) {
                // add the job
                Job job = plugin.getJobsCore().getJob(jobdata.getJobName());
                if (job != null) {
                    // create the progression object
                    JobProgression jobProgression = new JobProgression(job, this, jobdata.getLevel(), jobdata.getExperience(), plugin.getJobsConfiguration().getTitleForLevel(jobdata.getLevel()));
                    // calculate the max level
                    
                    // add the progression level.
                    progression.add(jobProgression);
                }
            }
        }
        reloadHonorific();
        recalculatePermissions();
    }
    
    /**
     * Get the list of job progressions
     * @return the list of job progressions
     */
    public List<JobProgression> getJobProgression() {
        return Collections.unmodifiableList(progression);
    }
    
    /**
     * Get the job progression with the certain job
     * @return the job progression
     */
    public JobProgression getJobProgression(Job job) {
        for (JobProgression prog : progression) {
            if (prog.getJob().equals(job))
                return prog;
        }
        return null;
    }
    
    /**
     * get the player
     * @return the player
     */
    public String getName(){
        return playername;
    }
    
    public String getDisplayHonorific() {
        return honorific;
    }
    
    /**
     * Player joins a job
     * @param job - the job joined
     */
    public boolean joinJob(Job job) {
        if (!isInJob(job)) {
            progression.add(new JobProgression(job, this, 1, 0.0, plugin.getJobsConfiguration().getTitleForLevel(1)));
            reloadHonorific();
            recalculatePermissions();
            return true;
        }
        return false;
    }
    
    /**
     * Player leaves a job
     * @param job - the job left
     */
    public boolean leaveJob(Job job) {
        JobProgression prog = getJobProgression(job);
        progression.remove(prog);
        if (prog != null) {
            reloadHonorific();
            recalculatePermissions();
            return true;
        }
        return false;
    }
    
    /**
     * Promotes player in job
     * @param job - the job being promoted
     * @param levels - number of levels to promote
     */
    public void promoteJob(Job job, int levels) {
        JobProgression prog = getJobProgression(job);
        if (prog == null)
            return;
        if (levels <= 0)
            return;
        int newLevel = prog.getLevel() + levels;
        Integer maxLevel = job.getMaxLevel();
        if (maxLevel != null && newLevel > maxLevel) {
            newLevel = maxLevel.intValue();
        }
        setLevel(job, newLevel);
    }
    
    /**
     * Demotes player in job
     * @param job - the job being deomoted
     * @param levels - number of levels to demote
     */
    public void demoteJob(Job job, int levels) {
        JobProgression prog = getJobProgression(job);
        if (prog == null)
            return;
        if (levels <= 0)
            return;
        int newLevel = prog.getLevel() - levels;
        if (newLevel < 1) {
            newLevel = 1;
        }
        setLevel(job, newLevel);
    }
    
    /**
     * Sets player to a specific level
     * @param job - the job
     * @param level - the level
     */
    private void setLevel(Job job, int level) {
        JobProgression prog = getJobProgression(job);
        if (prog == null)
            return;

        if (level != prog.getLevel()) {
            prog.setLevel(level);
            reloadHonorific();
            recalculatePermissions();
        }
    }
    
    /**
     * Player leaves a job
     * @param oldjob - the old job
     * @param newjob - the new job
     */
    public boolean transferJob(Job oldjob, Job newjob) {
        if (!isInJob(newjob)) {
            for (JobProgression prog : progression) {
                if (!prog.getJob().equals(oldjob))
                    continue;
                
                prog.setJob(newjob);
                if (newjob.getMaxLevel() > 0 && prog.getLevel() > newjob.getMaxLevel()) {
                    prog.setLevel(newjob.getMaxLevel());
                }
                reloadHonorific();
                recalculatePermissions();
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if the player is in this job.
     * @param job - the job
     * @return true - they are in the job
     * @return false - they are not in the job
     */
    public boolean isInJob(Job job) {
        for (JobProgression prog : progression) {
            if (prog.getJob().equals(job))
                return true;
        }
        return false;
    }
    
    /**
     * Function that reloads your honorific
     */
    public void reloadHonorific() {
        StringBuilder builder = new StringBuilder();
        int numJobs = progression.size();
        boolean gotTitle = false;
        for (JobProgression prog: progression) {
            DisplayMethod method = prog.getJob().getDisplayMethod();
            
            if (method.equals(DisplayMethod.NONE))
                continue;
            
            if (gotTitle) {
                builder.append(" ");
                gotTitle = false;
            }
            
            if (numJobs == 1) {
                if (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.TITLE)) {
                    if (prog.getTitle() != null) {
                        builder.append(prog.getTitle().getChatColor() + prog.getTitle().getName() + ChatColor.WHITE);
                        gotTitle = true;
                    }
                }
                
                if(method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.JOB)) {
                    if (gotTitle) {
                        builder.append(" ");
                    }
                    builder.append(prog.getJob().getChatColour() + prog.getJob().getName() + ChatColor.WHITE);
                    gotTitle = true;
                }
            }
            
            if (numJobs > 1 && (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.TITLE)) ||
                    method.equals(DisplayMethod.SHORT_FULL) ||
                    method.equals(DisplayMethod.SHORT_TITLE)) {
                // add title to honorific
                if (prog.getTitle() != null) {
                    builder.append(prog.getTitle().getChatColor() + prog.getTitle().getShortName() + ChatColor.WHITE);
                    gotTitle = true;
                }
            }
            
            if (numJobs > 1 && (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.JOB)) ||
                    method.equals(DisplayMethod.SHORT_FULL) || 
                    method.equals(DisplayMethod.SHORT_JOB)) {
                builder.append(prog.getJob().getChatColour() + prog.getJob().getShortName() + ChatColor.WHITE);
                gotTitle = true;
            }
        }
        
        honorific = builder.toString().trim();
    }
    
    /**
     * Function to recalculate permissions
     */
    public void recalculatePermissions() {
        Player player = plugin.getServer().getPlayer(playername);
        if (player == null)
            return;
        
        boolean changed = false;
        if (this.attachment != null) {
            player.removeAttachment(attachment);
            this.attachment = null;
            changed = true;
        }
        
        if (progression.size() == 0) {
            Job job = plugin.getJobsCore().getNoneJob();
            if (job != null) {
                for (JobPermission perm : job.getPermissions()) {
                    if (perm.getLevelRequirement() <= 0) {
                        if (this.attachment == null) {
                            this.attachment = player.addAttachment(plugin);
                            changed = true;
                        }
                        attachment.setPermission(perm.getNode(), perm.getValue());
                    }
                }
            }
        } else {
            for (JobProgression prog : progression) {
                for (JobPermission perm : prog.getJob().getPermissions()) {
                    if (prog.getLevel() >= perm.getLevelRequirement()) {
                        if (this.attachment == null) {
                            this.attachment = player.addAttachment(plugin);
                            changed = true;
                        }
                        attachment.setPermission(perm.getNode(), perm.getValue());
                    }
                }
            }
        }
        if (changed)
            player.recalculatePermissions();
    }
    
    /**
     * Function to remove all permissions
     */
    public void removePermissions() {
        Player player = plugin.getServer().getPlayer(playername);
        if (player == null)
            return;
        
        if (this.attachment != null) {
            player.removeAttachment(attachment);
            player.recalculatePermissions();
            this.attachment = null;
        }
    }
}
