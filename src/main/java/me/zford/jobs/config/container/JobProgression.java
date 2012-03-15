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

package me.zford.jobs.config.container;

import java.util.HashMap;

import me.zford.jobs.Jobs;



/**
 * Container class for job progression.
 * 
 * Holds the experience, maxExperience and level.
 * @author Alex
 *
 */
public class JobProgression {
    private Job job;
    private Title title;
    private double experience;
    private int maxExperience;
    private int level;
    private Jobs plugin;
    
    public JobProgression(Jobs plugin, Job job, double experience, int level, JobsPlayer info){
        this.plugin = plugin;
        this.job = job;
        this.experience = experience;
        this.level = level;
        HashMap<String, Double> param = new HashMap<String, Double>();
        param.put("joblevel", (double) level);
        param.put("numjobs", (double) info.getJobs().size());
        maxExperience = (int)job.getMaxExp(param);
        title = this.plugin.getJobsConfiguration().getTitleForLevel(level);
    }
    
    /**
     * Add experience (does not cause a level up event
     * @param exp - experience to be added
     */
    public void addExp(double exp){
        experience += exp;
    }
    
    /**
     * Can the job level up?
     * @return true if the job can level up
     * @return false if the job cannot
     */
    public boolean canLevelUp(){
        if (experience >= maxExperience){
            return true;
        }
        return false;
    }
    
    /**
     * Return the job
     * @return the job
     */
    public Job getJob() {
        return job;
    }

    /**
     * Set the job
     * @param job - the new job to be set
     */
    public void setJob(Job job) {
        this.job = job;
    }

    /**
     * Get the experience in this job
     * @return the experiece in this job
     */
    public double getExperience() {
        return experience;
    }

    /**
     * Set the experience for this job
     * @param experience - the experience in this job
     */
    public void setExperience(double experience) {
        this.experience = experience;
    }

    /**
     * Get the maximum experience for this level
     * @return the experience needed to level up
     */
    public int getMaxExperience() {
        return maxExperience;
    }

    /**
     * Set the experience needed to level up (Does not trigger levelup events)
     * @param maxExperience - the new experience needed to level up
     */
    public void setMaxExperience(int maxExperience) {
        this.maxExperience = maxExperience;
    }

    /**
     * Get the current level of this job
     * @return the level of this job
     */
    public int getLevel() {
        return level;
    }

    /**
     * Set the level of this job
     * @param level - the new level for this job
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Set a current title
     * @param title - the new title
     */
    public void setTitle(Title title) {
        this.title = title;
    }

    /**
     * Get the title
     * @return the current title
     */
    public Title getTitle() {
        return title;
    }
}
