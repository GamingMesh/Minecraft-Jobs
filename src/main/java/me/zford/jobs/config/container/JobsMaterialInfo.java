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
import java.util.Map.Entry;

import me.zford.jobs.resources.jfep.Parser;

import org.bukkit.material.MaterialData;

/**
 * Class for holding payment and experience information about a block
 * @author Alex
 *
 */
public class JobsMaterialInfo{
	private MaterialData blockMaterial;
	private double xpGiven;
	private double moneyGiven;
	
	/**
	 * Constructor
	 * @param blockMaterial - material that the class represents
	 * @param xpGiven - base xp given for the block
	 * @param moneyGiven - base money given for the block
	 */
	public JobsMaterialInfo(MaterialData blockMaterial, double xpGiven, double moneyGiven){
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
	 * @param parameters - equation parameters
	 * @return the money given
	 * @return null if it isn't the block
	 */
	public Double getMoneyFromMaterial(Parser equation, HashMap<String, Double> parameters) {
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
	 * @param parameters - equation parameters
	 * @return the experience given
	 * @return null if it isn't the block
	 */
	public Double getXPFromMaterial(Parser equation, HashMap<String, Double> parameters) {
		for(Entry<String, Double> temp: parameters.entrySet()){
			equation.setVariable(temp.getKey(), temp.getValue());
		}
		equation.setVariable("baseexperience", xpGiven);
		return equation.getValue();
	}
}
