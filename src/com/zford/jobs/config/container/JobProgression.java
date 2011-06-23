package com.zford.jobs.config.container;

import java.util.HashMap;

import com.zford.jobs.config.JobsConfiguration;


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
	private double maxExperience;
	private int level;
	
	public JobProgression(Job job, double experience, int level, PlayerJobInfo info){
		this.job = job;
		this.experience = experience;
		this.level = level;
		HashMap<String, Double> param = new HashMap<String, Double>();
		param.put("joblevel", (double) level);
		param.put("numjobs", (double) info.getJobs().size());
		maxExperience = job.getMaxExp(param);
		title = JobsConfiguration.getInstance().getTitleForLevel(level);
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
	public double getMaxExperience() {
		return maxExperience;
	}

	/**
	 * Set the experience needed to level up (Does not trigger levelup events)
	 * @param maxExperience - the new experience needed to level up
	 */
	public void setMaxExperience(double maxExperience) {
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
