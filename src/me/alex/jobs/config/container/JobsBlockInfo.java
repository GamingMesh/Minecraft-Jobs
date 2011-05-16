package me.alex.jobs.config.container;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;
import org.mbertoli.jfep.Parser;

/**
 * Class for holding payment and experience information about a block
 * @author Alex
 *
 */
public class JobsBlockInfo{
	private MaterialData blockMaterial;
	private double xpGiven;
	private double moneyGiven;
	
	/**
	 * Constructor
	 * @param blockMaterial - material that the class represents
	 * @param xpGiven - base xp given for the block
	 * @param moneyGiven - base money given for the block
	 */
	public JobsBlockInfo(MaterialData blockMaterial, double xpGiven, double moneyGiven){
		this.blockMaterial = blockMaterial;
		this.xpGiven = xpGiven;
		this.moneyGiven = moneyGiven;
	}
	
	/**
	 * Function to return the material the block represents
	 * @return The material the block represents.
	 */
	public MaterialData getMaterial(){
		return blockMaterial;
	}
	
	/**
	 * Function to get the base xp given for this block
	 * @return the xp given for this block
	 */
	public double getXpGiven(){
		return xpGiven;
	}
	
	/**
	 * Function to get the base money given for this block
	 * @return the money given for this block
	 */
	public double getMoneyGiven(){
		return moneyGiven;
	}

	/**
	 * Function to get the money that should be paid out for this block
	 * 
	 * @param equation - equation to calculate the payout
	 * @param block - block in question
	 * @param parameters - equation parameters
	 * @return the money given
	 * @return null if it isn't the block
	 */
	public Double getMoneyFromBlock(Parser equation, Block block,
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
	 * @param block - block in question
	 * @param parameters - equation parameters
	 * @return the experience given
	 * @return null if it isn't the block
	 */
	public Double getXPFromBlock(Parser equation, Block block,
			HashMap<String, Double> parameters) {
		for(Entry<String, Double> temp: parameters.entrySet()){
			equation.setVariable(temp.getKey(), temp.getValue());
		}
		equation.setVariable("baseexperience", xpGiven);
		return equation.getValue();
	}
}
