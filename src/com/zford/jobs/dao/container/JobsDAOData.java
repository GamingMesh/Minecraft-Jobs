package com.zford.jobs.dao.container;

/**
 * Container class to hold information out of the database.
 * 
 * Holds job name
 * Experience in the job
 * Level in the job
 * @author Alex
 *
 */
public class JobsDAOData {
	private String job;
	private int experience;
	private int level;
	
	/**
	 * Constructor class for the DAO side of things.
	 * @param job - the name of the job
	 * @param experience - the experience of the job
	 * @param level - the level of the job
	 */
	public JobsDAOData(String job, int experience, int level){
		this.job = job;
		this.experience = experience;
		this.level = level;
	}
	
	/**
	 * Getter function for the job name
	 * @return the job name
	 */
	public String getJobName(){
		return job;
	}
	
	/**
	 * Getter function for the experience.
	 * @return the experience in the job
	 */
	public double getExperience(){
		return (double)experience;
	}
	
	/**
	 * Getter function for the level
	 * @return the level in the job
	 */
	public int getLevel(){
		return level;
	}
}
