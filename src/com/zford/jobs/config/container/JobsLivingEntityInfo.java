package com.zford.jobs.config.container;

import java.util.HashMap;
import java.util.Map.Entry;

import org.mbertoli.jfep.Parser;


/**
 * Class for holding payment and experience information about a living entity
 * @author Alex
 *
 */
@SuppressWarnings("rawtypes")
public class JobsLivingEntityInfo {
	private Class livingEntityClass;
	private double xpGiven;
	private double moneyGiven;
	
	/**
	 * Constructor
	 * @param livingEntityClass - LivingEntity that the class represents
	 * @param xpGiven - base xp given for the LivingEntity
	 * @param moneyGiven - base money given for the LivingEntity
	 */
	public JobsLivingEntityInfo(Class livingEntityClass, double xpGiven, double moneyGiven){
		this.livingEntityClass = livingEntityClass;
		this.xpGiven = xpGiven;
		this.moneyGiven = moneyGiven;
	}
	

	/**
	 * Function to return the class the block represents
	 * @return The class the block represents.
	 */
	public Class getLicingEntityClass(){
		return livingEntityClass;
	}
	
	/**
	 * Function to get the base xp given for this LivingEntity
	 * @return the xp given for this LivingEntity
	 */
	public double getXpGiven(){
		return xpGiven;
	}
	
	/**
	 * Function to get the base money given for this LivingEntity
	 * @return the money given for this LivingEntity
	 */
	public double getMoneyGiven(){
		return moneyGiven;
	}
	
	/**
	 * Function to get the money that should be paid out for this block
	 * 
	 * @param equation - equation to calculate the payout
	 * @param mob - mob in question
	 * @param parameters - equation parameters
	 * @return the money given
	 * @return null if it isn't the mob
	 */
	public Double getMoneyFromKill(Parser equation, String mob,
			HashMap<String, Double> parameters) {
		for(Entry<String, Double> temp: parameters.entrySet()){
			equation.setVariable(temp.getKey(), temp.getValue());
		}
		equation.setVariable("baseincome", moneyGiven);
		return equation.getValue();
	}

	/**
	 * Function to get the experience that should be paid out for this block
	 * 
	 * @param equation - equation to calculate the experience
	 * @param mob - mob in question
	 * @param parameters - equation parameters
	 * @return the experience given
	 * @return null if it isn't the mob
	 */
	public Double getXPFromKill(Parser equation, String mob,
			HashMap<String, Double> parameters) {
		for(Entry<String, Double> temp: parameters.entrySet()){
			equation.setVariable(temp.getKey(), temp.getValue());
		}
		equation.setVariable("baseexperience", xpGiven);
		return equation.getValue();
	}
}
