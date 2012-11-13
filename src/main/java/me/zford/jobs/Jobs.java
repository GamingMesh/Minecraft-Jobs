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

package me.zford.jobs;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.container.Job;
import me.zford.jobs.dao.JobsDAO;
import me.zford.jobs.util.JobsClassLoader;

public class Jobs {
    private Logger pLogger;
    private Logger sLogger;
    private File dataFolder;
    private JobsClassLoader classLoader = new JobsClassLoader(this);
    private JobsDAO dao = null;
    private List<Job> jobs = null;
    private Job noneJob = null;
    private WeakHashMap<Job, Integer> usedSlots = new WeakHashMap<Job, Integer>();
    
    public Jobs(JobsPlugin plugin) {
        this.pLogger = plugin.getLogger();
        this.sLogger = plugin.getServer().getLogger();
        this.dataFolder = plugin.getDataFolder();
    }
    
    /**
     * Retrieves the plugin logger
     * @return the plugin logger
     */
    public Logger getPluginLogger() {
        return pLogger;
    }
    
    /**
     * Retrieves the server logger
     * @return the server logger
     */
    public Logger getServerLogger() {
        return sLogger;
    }
    
    /**
     * Retrieves the data folder
     * @return data folder
     */
    public File getDataFolder() {
        return dataFolder;
    }
    
    /**
     * Sets the Data Access Object
     * @param dao - the DAO
     */
    public void setDAO(JobsDAO dao) {
        this.dao = dao;
    }
    
    /**
     * Get the Data Access Object
     * @return the DAO
     */
    public JobsDAO getJobsDAO() {
        return dao;
    }
    
    /**
     * Sets the list of jobs
     * @param jobs - list of jobs
     */
    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
    
    /**
     * Retrieves the list of active jobs
     * @return list of jobs
     */
    public List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }
    
    /**
     * Sets the none job
     * @param noneJob - the none job
     */
    public void setNoneJob(Job noneJob) {
        this.noneJob = noneJob;
    }
    
    /**
     * Retrieves the "none" job
     * @return the none job
     */
    public Job getNoneJob() {
        return noneJob;
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
    
    /**
     * Reloads all data
     */
    public void reload() {
        usedSlots.clear();
        for (Job job: jobs) {
            usedSlots.put(job, dao.getSlotsTaken(job));
        }
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
    public void takeSlot(Job job) {
        usedSlots.put(job, usedSlots.get(job)+1);
    }
    
    /**
     * Function to decrease the number of used slots for a job
     * @param job - the job someone is leaving
     */
    public void leaveSlot(Job job) {
        usedSlots.put(job, usedSlots.get(job)-1);
    }
    
    /**
     * Returns the jobs classloader
     */
    public JobsClassLoader getJobsClassloader() {
        return classLoader;
    }
}
