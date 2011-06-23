package com.zford.jobs.config.container;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class similar to materialData but for kills
 * @author Alex
 *
 */
public class KillData {
	private Class victimClass;
	private List<String> jobs;
	
	/**
	 * Constructor for when it's not a player
	 * @param victimClass - the victim class
	 */
	public KillData(Class victimClass) {
		this.victimClass = victimClass;
	}
	
	/**
	 * Constructor for when it is a player with a job
	 * @param victimClass - the victim class (usually a player)
	 * @param job - the job as a string
	 */
	public KillData(Class victimClass, String job){
		this.victimClass = victimClass;
		this.jobs = new ArrayList<String>();
		jobs.add(job);
	}
	
	/**
	 * Constructor for when they have multiple jobs
	 * @param victimClass - the victim class (usually a player)
	 * @param job - the jobs as a list of strings
	 */
	public KillData(Class victimClass, List<String> jobs){
		this.victimClass = victimClass;
		this.jobs = jobs;
	}
	
	/**
	 * Comparator.
	 * @param obj - comparing class
	 * @return true - they are the same
	 * @return false - they are not the same
	 */
	public boolean equals(KillData obj) {
		return ((jobs.containsAll(obj.getJobs()) || obj.getJobs().containsAll(jobs) || 
				jobs.size() == 0) && victimClass.equals(obj.getVictimClass()));
	}
	
	/**
	 * Getter function for the list of jobs
	 * @return the list of job names
	 */
	public List<String> getJobs(){
		return jobs;
	}
	
	/**
	 * Getter function for the class
	 * @return the class
	 */
	public Class getVictimClass(){
		return victimClass;
	}
}
