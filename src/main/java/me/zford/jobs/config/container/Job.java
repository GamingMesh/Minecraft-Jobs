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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.zford.jobs.resources.jfep.Parser;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Job {
    // payment for breaking a block
    private List<JobInfo> jobBreakInfo;
    // payment for crafting
    private List<JobInfo> jobCraftInfo;
    // payment for placing a block
    private List<JobInfo> jobPlaceInfo;
    // payment for killing a living entity
    private List<JobInfo> jobKillInfo;
    // payment for fishing
    private List<JobInfo> jobFishInfo;
    // payment for smelting
    private List<JobInfo> jobSmeltInfo;
    // permissions
    private List<JobPermission> jobPermissions;
    // job name
    private String jobName;
    // job short name (for use in multiple jobs)
    private String jobShortName;
    // job chat colour
    private ChatColor jobColour;
    // job leveling equation
    private Parser maxExpEquation;
    // job income equation
    private Parser incomeEquation;
    // exp per block equation
    private Parser expEquation;
    // display method
    private DisplayMethod displayMethod;
    // max level
    private int maxLevel;
    // max number of people allowed with this job on the server.
    private Integer maxSlots;
    // is hidden job
    private boolean isHidden;

    /**
     * Constructor
     * @param jobBreakInfo - information about base rewards for breaking a block
     * @param jobPlaceInfo - information about base rewards for placing a block
     * @param jobKillInfo - information about base rewards for killing a LivingEntity
     * @param jobCraftInfo - information about base rewards for crafting an item
     * @param jobKillCustomInfo - information about base rewards for killing a custom jobs class
     * @param jobName - the name of the job
     * @param jobShortName - the shortened version of the name of the job.
     * @param jobColour - the colour of the job title as displayed in chat.
     * @param maxExpEquation - the equation by which the exp needed to level up is calculated
     * @param incomeEquation - the equation by which the income given for a level is calculated
     * @param expEquation - the equation by which the exp given for a level is calculated
     * @param displayMethod - the display method for this job.
     * @param maxLevel - the maximum level allowed (null for no max level)
     * @param maxSlots - the maximum number of people allowed to have this job at one time (null for no limits)
     */
    public Job(List<JobInfo> jobBreakInfo, 
            List<JobInfo> jobPlaceInfo, 
            List<JobInfo> jobKillInfo,
            List<JobInfo> jobFishInfo,
            List<JobInfo> jobCraftInfo,
            List<JobInfo> jobSmeltInfo,
            List<JobPermission> jobPermissions,
            String jobName,
            String jobShortName,
            ChatColor jobColour,
            Parser maxExpEquation,
            Parser incomeEquation,
            Parser expEquation,
            DisplayMethod displayMethod,
            int maxLevel,
            Integer maxSlots,
            boolean isHidden) {
        this.jobBreakInfo = jobBreakInfo;
        this.jobPlaceInfo = jobPlaceInfo;
        this.jobCraftInfo = jobCraftInfo;
        this.jobSmeltInfo = jobSmeltInfo;
        this.jobKillInfo = jobKillInfo;
        this.jobFishInfo = jobFishInfo;
        this.jobPermissions = jobPermissions;
        this.jobName = jobName;
        this.jobShortName = jobShortName;
        this.jobColour = jobColour;
        this.maxExpEquation = maxExpEquation;
        this.incomeEquation = incomeEquation;
        this.expEquation = expEquation;
        this.displayMethod = displayMethod;
        this.maxLevel = maxLevel;
        this.maxSlots = maxSlots;
        this.isHidden = isHidden;
    }
    
    /**
     * Function to get the income for killing a LivingEntity
     * @param mob - the creature
     * @param level - players job level
     * @param numjobs - number of jobs of the player
     * @return the income received for killing the LivingEntity
     */
    public Double getKillIncome(EntityType type, int level, int numjobs) {
        for (JobInfo info : jobKillInfo) {
            if (type.toString().equals(info.getName()))
                return info.getIncome(level, numjobs);
        }
        return null;
    }
    
    /**
     * Function to get the exp for killing a LivingEntity
     * @param mob - the creature
     * @param param - parameters for the customisable equation
     * @return the exp received for killing the LivingEntity
     */
    public Double getKillExp(EntityType type, int level, int numjobs) {
        for (JobInfo info : jobKillInfo) {
            if (type.toString().equals(info.getName()))
                return info.getExperience(level, numjobs);
        }
        return null;
    }
    
    /**
     * Function to get the income for placing a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the income received for placing the block
     * @return null if job has no payment for this type of block
     */
    public Double getPlaceIncome(Block block, int level, int numjobs) {
        return this.getBlockActionIncome(block, level, numjobs, this.jobPlaceInfo);
    }
    
    /**
     * Function to get the exp for placing a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the exp received for placing the block
     * @return null if job has no payment for this type of block
     */
    public Double getPlaceExp(Block block, int level, int numjobs) {
        return this.getBlockActionExp(block, level, numjobs, this.jobPlaceInfo);
    }
    
    /**
     * Function to get the income for breaking a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the income received for breaking the block
     * @return null if job has no payment for this type of block
     */
    public Double getBreakIncome(Block block, int level, int numjobs) {
        return this.getBlockActionIncome(block, level, numjobs, this.jobBreakInfo);
    }
    
    /**
     * Function to get the exp for breaking a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the exp received for breaking the block
     * @return null if job has no payment for this type of block
     */
    public Double getBreakExp(Block block, int level, int numjobs) {
        return this.getBlockActionExp(block, level, numjobs, this.jobBreakInfo);
    }
    
    /**
     * Function to get the income for crafting an item
     * @param items - the items
     * @param param - parameters for the customisable equation
     * @return the income received for crafting the item
     * @return null if job has no payment for this type of block
     */
    public Double getCraftIncome(ItemStack items, int level, int numjobs) {
        return this.getItemActionIncome(items, level, numjobs, this.jobCraftInfo);
    }
    
    /**
     * Function to get the exp for crafting an item
     * @param items - the items
     * @param param - parameters for the customisable equation
     * @return the income received for crafting the item
     * @return null if job has no payment for this type of block
     */
    public Double getCraftExp(ItemStack items, int level, int numjobs) {
        return this.getItemActionExp(items, level, numjobs, this.jobCraftInfo);
    }
    
    /**
     * Function to get the income for smelting an item
     * @param items - the items
     * @param param - parameters for the customisable equation
     * @return the income received for crafting the item
     * @return null if job has no payment for this type of block
     */
    public Double getSmeltIncome(ItemStack items, int level, int numjobs) {
        return this.getItemActionIncome(items, level, numjobs, this.jobSmeltInfo);
    }
    
    /**
     * Function to get the exp for smelting an item
     * @param items - the items
     * @param param - parameters for the customisable equation
     * @return the income received for crafting the item
     * @return null if job has no payment for this type of block
     */
    public Double getSmeltExp(ItemStack items, int level, int numjobs) {
        return this.getItemActionExp(items, level, numjobs, this.jobSmeltInfo);
    }
    
    /**
     * Function to get the income for fishing an item
     * @param item - the item
     * @param param - parameters for the customisable equation
     * @return the income received for fishing the item
     * @return null if job has no payment for this type of action
     */
    public Double getFishIncome(ItemStack items, int level, int numjobs) {
        return this.getItemActionIncome(items, level, numjobs, this.jobFishInfo);
    }
    
    /**
     * Function to get the income for fishing an item
     * @param item - the item
     * @param param - parameters for the customisable equation
     * @return the income received for fishing the item
     * @return null if job has no payment for this type of action
     */
    public Double getFishExp(ItemStack items, int level, int numjobs) {
        return this.getItemActionExp(items, level, numjobs, this.jobFishInfo);
    }
    
    /**
     * Function to get the income for performing the action
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @param info - info for performing the action
     * @return the income received for performing the action
     * @return null if job has no payment for this type of action
     */
    private Double getBlockActionIncome(Block block, int level, int numjobs, List<JobInfo> jobInfo) {
        String blockKey = block.getType().toString();        
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if(block.getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            blockKey = Material.REDSTONE_ORE.toString();
        }
        for (JobInfo info : jobInfo) {
            if (blockKey.equals(info.getName()) || info.getName().equals(blockKey+":"+block.getData()))
                return info.getIncome(level, numjobs);
        }
        return null;
    }

    /**
     * Function to get the exp for performing the action
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @param info - info for performing the action
     * @return the exp received for performing the action
     * @return null if job has no payment for this type of action
     */
    private Double getBlockActionExp(Block block, int level, int numjobs, List<JobInfo> jobInfo) {
        String blockKey = block.getType().toString();        
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if(block.getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            blockKey = Material.REDSTONE_ORE.toString();
        }
        for (JobInfo info : jobInfo) {
            if (blockKey.equals(info.getName()) || info.getName().equals(blockKey+":"+block.getData()))
                return info.getExperience(level, numjobs);
        }
        return null;
    }
    
    /**
     * Function to get the income for performing the action
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @param info - info for performing the action
     * @return the income received for performing the action
     * @return null if job has no payment for this type of action
     */
    private Double getItemActionIncome(ItemStack item, int level, int numjobs, List<JobInfo> jobInfo) {
        String itemKey = item.getType().toString();
        for (JobInfo info : jobInfo) {
            if (itemKey.equals(info.getName()) || info.getName().equals(itemKey+":"+item.getData()))
                return info.getIncome(level, numjobs);
        }
        return null;
    }

    /**
     * Function to get the exp for performing the action
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @param info - info for performing the action
     * @return the exp received for performing the action
     * @return null if job has no payment for this type of action
     */
    private Double getItemActionExp(ItemStack item, int level, int numjobs, List<JobInfo> jobInfo) {
        String itemKey = item.getType().toString();
        for (JobInfo info : jobInfo) {
            if (itemKey.equals(info.getName()) || info.getName().equals(itemKey+":"+item.getData()))
                return info.getExperience(level, numjobs);
        }
        return null;
    }
    
    /**
     * Get the job name
     * @return the job name
     */
    public String getName(){
        return jobName;
    }
    
    /**
     * Get the shortened version of the jobName
     * @return the shortened version of the jobName
     */
    public String getShortName(){
        return jobShortName;
    }
    
    /**
     * Get the Color of the job for chat
     * @return the Color of the job for chat
     */
    public ChatColor getChatColour(){
        return jobColour;
    }
    
    /**
     * Get the MaxExpEquation of the job
     * @return the MaxExpEquation of the job
     */
    public Parser getMaxExpEquation(){
        return maxExpEquation;
    }
    
    /**
     * Get the IncomeEquation of the job
     * @return the IncomeEquation of the job
     */
    public Parser getIncomeEquation(){
        return incomeEquation;
    }
    
    /**
     * Get the ExpEquation of the job
     * @return the ExpEquation of the job
     */
    public Parser getExpEquation(){
        return expEquation;
    }
    
    /**
     * Function to return the appropriate max exp for this level
     * @param level - current level
     * @return the correct max exp for this level
     */
    public double getMaxExp(Map<String, Double> param) {
        for (Map.Entry<String, Double> temp: param.entrySet()) {
            maxExpEquation.setVariable(temp.getKey(), temp.getValue());
        }
        return maxExpEquation.getValue();        
    }

    /**
     * Function to get the display method
     * @return the display method
     */
    public DisplayMethod getDisplayMethod() {
        return displayMethod;
    }
    
    /**
     * Function to return the maximum level
     * @return the max level
     * @return null - no max level
     */
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Function to return the maximum slots
     * @return the max slots
     * @return null - no max slots
     */
    public Integer getMaxSlots(){
        return maxSlots;
    }
    
    /**
     * Get the payout information about breaking blocks
     * @return the map of breaking blocks and its payment
     */
    public List<JobInfo> getBreakInfo() {
        return Collections.unmodifiableList(jobBreakInfo);
    }
    
    /**
     * Get the payout information about placing blocks
     * @return the map of placing blocks and its payment
     */
    public List<JobInfo> getPlaceInfo() {
        return Collections.unmodifiableList(jobPlaceInfo);
    }
    
    /**
     * Get the payout information about killing entities
     * @return the map of killing entities and its payment
     */
    public List<JobInfo> getKillInfo() {
        return Collections.unmodifiableList(jobKillInfo);
    }
    
    /**
     * Get the payout information for fishing
     * @return the map of fishing and its payment
     */
    public List<JobInfo> getFishInfo() {
        return Collections.unmodifiableList(jobFishInfo);
    }
    
    /**
     * Get the payout information for crafting
     * @return the map of crafting and its payment
     */
    public List<JobInfo> getCraftInfo() {
        return Collections.unmodifiableList(jobCraftInfo);
    }
    
    /**
     * Get the payout information for smelting
     * @return the map of smelting and its payment
     */
    public List<JobInfo> getSmeltInfo() {
        return Collections.unmodifiableList(jobSmeltInfo);
    }
    
    /**
     * Get the permission nodes for this job
     * @return Permissions for this job
     */
    public List<JobPermission> getPermissions() {
        return Collections.unmodifiableList(jobPermissions);
    }
    
    public boolean isHidden() {
        return isHidden;
    }
}
