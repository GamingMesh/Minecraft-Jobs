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
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class Job {
    // payment for breaking a block
    private Map<String, JobsMaterialInfo> jobBreakInfo;
    // payment for crafting
    private Map<String, JobsMaterialInfo> jobCraftInfo;
    // payment for placing a block
    private Map<String, JobsMaterialInfo> jobPlaceInfo;
    // payment for killing a living entity
    private Map<String, JobsLivingEntityInfo> jobKillInfo;
    // payment for killing a living entity
    private Map<String, JobsMaterialInfo> jobFishInfo;
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
    private Integer maxLevel;
    // max number of people allowed with this job on the server.
    private Integer maxSlots;
    // is hidden job
    private boolean isHidden;

    /**
     * Constructor
     * @param jobBreakInfo - information about base rewards for breaking a block
     * @param jobPlaceInfo - information about base rewards for placing a block
     * @param jobKillInfo - information about base rewards for killing a LivingEntity
     * @param jobCreateInfo - information about base rewards for creating an item
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
    public Job(Map<String, JobsMaterialInfo> jobBreakInfo, 
            Map<String, JobsMaterialInfo> jobPlaceInfo, 
            Map<String, JobsLivingEntityInfo> jobKillInfo,
            Map<String, JobsMaterialInfo> jobFishInfo,
            Map<String, JobsMaterialInfo> jobCreateInfo,
            List<JobPermission> jobPermissions,
            String jobName,
            String jobShortName,
            ChatColor jobColour,
            Parser maxExpEquation,
            Parser incomeEquation,
            Parser expEquation,
            DisplayMethod displayMethod,
            Integer maxLevel,
            Integer maxSlots,
            boolean isHidden) {
        this.jobBreakInfo = jobBreakInfo;
        this.jobPlaceInfo = jobPlaceInfo;
        this.jobCraftInfo = jobCreateInfo;
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
     * @param param - parameters for the customisable equation
     * @return the income received for killing the LivingEntity
     */
    public Double getKillIncome(String mob, Map<String, Double> param) {
        if(jobKillInfo.containsKey(mob)){
            return jobKillInfo.get(mob).getMoneyFromKill(incomeEquation, mob, param);
        }
        return null;
    }
    
    /**
     * Function to get the exp for killing a LivingEntity
     * @param mob - the creature
     * @param param - parameters for the customisable equation
     * @return the exp received for killing the LivingEntity
     */
    public Double getKillExp(String mob, Map<String, Double> param) {
        if(jobKillInfo.containsKey(mob)){
            return jobKillInfo.get(mob).getXPFromKill(expEquation, mob, param);
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
    public Double getPlaceIncome(Block block, Map<String, Double> param) {
        return this.getBlockActionIncome(block, param, this.jobPlaceInfo);
    }
    
    /**
     * Function to get the exp for placing a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the exp received for placing the block
     * @return null if job has no payment for this type of block
     */
    public Double getPlaceExp(Block block, Map<String, Double> param) {
        return this.getBlockActionExp(block, param, this.jobPlaceInfo);
    }
    
    /**
     * Function to get the income for breaking a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the income received for breaking the block
     * @return null if job has no payment for this type of block
     */
    public Double getBreakIncome(Block block, Map<String, Double> param){
        return this.getBlockActionIncome(block, param, this.jobBreakInfo);
    }
    
    /**
     * Function to get the exp for breaking a block
     * @param block - the block
     * @param param - parameters for the customisable equation
     * @return the exp received for breaking the block
     * @return null if job has no payment for this type of block
     */
    public Double getBreakExp(Block block, Map<String, Double> param){
        return this.getBlockActionExp(block, param, this.jobBreakInfo);
    }
    
    /**
     * Function to get the income for crafting an item
     * @param items - the items
     * @param param - parameters for the customisable equation
     * @return the income received for crafting the item
     * @return null if job has no payment for this type of block
     */
    public Double getCraftIncome(ItemStack items, Map<String, Double> param){
        return this.getItemActionIncome(items, param, this.jobCraftInfo);
    }
    
    /**
     * Function to get the exp for crafting an item
     * @param items - the items
     * @param param - parameters for the customisable equation
     * @return the income received for crafting the item
     * @return null if job has no payment for this type of block
     */
    public Double getCraftExp(ItemStack items, Map<String, Double> param){
        return this.getItemActionExp(items, param, this.jobCraftInfo);
    }
    
    /**
     * Function to get the income for fishing an item
     * @param item - the item
     * @param param - parameters for the customisable equation
     * @return the income received for fishing the item
     * @return null if job has no payment for this type of action
     */
    public Double getFishIncome(Item item, Map<String, Double> param) {
        String materialKey = item.getItemStack().getType().toString();
        // try simple
        if(this.jobFishInfo.containsKey(materialKey)){
            return this.jobFishInfo.get(materialKey).getMoneyFromMaterial(incomeEquation, param);
        }
        else{
            // try with sub-class
            if(this.jobFishInfo.containsKey(materialKey+":"+item.getItemStack().getData())){
                return this.jobFishInfo.get(materialKey+":"+item.getItemStack().getData()).getMoneyFromMaterial(incomeEquation, param);
            }
        }
        return null;
    }
    
    /**
     * Function to get the income for fishing an item
     * @param item - the item
     * @param param - parameters for the customisable equation
     * @return the income received for fishing the item
     * @return null if job has no payment for this type of action
     */
    public Double getFishExp(Item item, Map<String, Double> param) {
        String materialKey = item.getItemStack().getType().toString();
        // try simple
        if(this.jobFishInfo.containsKey(materialKey)){
            return this.jobFishInfo.get(materialKey).getXPFromMaterial(expEquation, param);
        }
        else{
            // try with sub-class
            if(this.jobFishInfo.containsKey(materialKey+":"+item.getItemStack().getData())){
                return this.jobFishInfo.get(materialKey+":"+item.getItemStack().getData()).getXPFromMaterial(expEquation, param);
            }
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
    private Double getBlockActionIncome(Block block, Map<String, Double> param, Map<String, JobsMaterialInfo> info) {
        String blockKey = block.getType().toString();
        
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if(block.getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            blockKey = Material.REDSTONE_ORE.toString();
        }
        // try simple
        if(info.containsKey(blockKey)){
            return info.get(blockKey).getMoneyFromMaterial(incomeEquation, param);
        }
        else{
            // try with sub-class
            if(info.containsKey(blockKey+":"+block.getData())){
                return info.get(blockKey+":"+block.getData()).getMoneyFromMaterial(incomeEquation, param);
            }
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
    private Double getBlockActionExp(Block block, Map<String, Double> param, Map<String, JobsMaterialInfo> info) {
        String blockKey = block.getType().toString();
        
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if(block.getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            blockKey = Material.REDSTONE_ORE.toString();
        }
        // try simple
        if(info.containsKey(blockKey)){
            return info.get(blockKey).getXPFromMaterial(expEquation, param);
        }
        else{
            // try with sub-class
            if(info.containsKey(blockKey+":"+block.getData())){
                return info.get(blockKey+":"+block.getData()).getXPFromMaterial(expEquation, param);
            }
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
    private Double getItemActionIncome(ItemStack item, Map<String, Double> param, Map<String, JobsMaterialInfo> info) {
        String blockKey = item.getType().toString();
        if(blockKey == null)
            return null;
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if(item.getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            blockKey = Material.REDSTONE_ORE.toString();
        }
        // try simple
        if(info.containsKey(blockKey)){                
            return item.getAmount() * info.get(blockKey).getMoneyFromMaterial(incomeEquation, param);
        }else if(item.getData()!=null){
            // try with sub-class
            if(info.containsKey(blockKey+":"+item.getData().getData())){
                return item.getAmount() * info.get(blockKey+":"+item.getData().getData()).getMoneyFromMaterial(incomeEquation, param);
            }
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
    private Double getItemActionExp(ItemStack item, Map<String, Double> param, Map<String, JobsMaterialInfo> info) {
        String blockKey = item.getType().toString();
        if(blockKey == null)
            return null;
        
        // Normalize GLOWING_REDSTONE_ORE to REDSTONE_ORE
        if(item.getType().equals(Material.GLOWING_REDSTONE_ORE)) {
            blockKey = Material.REDSTONE_ORE.toString();
        }
        // try simple
        if(info.containsKey(blockKey)){
            return item.getAmount() * info.get(blockKey).getXPFromMaterial(expEquation, param);
        }else if(item.getData()!=null){
            // try with sub-class
            if(info.containsKey(blockKey+":"+item.getData().getData())){
                return item.getAmount() * info.get(blockKey+":"+item.getData()).getXPFromMaterial(expEquation, param);
            }
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
    public Integer getMaxLevel(){
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
    public Map<String, JobsMaterialInfo> getBreakInfo() {
        return Collections.unmodifiableMap(jobBreakInfo);
    }
    
    /**
     * Get the payout information about placing blocks
     * @return the map of placing blocks and its payment
     */
    public Map<String, JobsMaterialInfo> getPlaceInfo() {
        return Collections.unmodifiableMap(jobPlaceInfo);
    }
    
    /**
     * Get the payout information about killing entities
     * @return the map of killing entities and its payment
     */
    public Map<String, JobsLivingEntityInfo> getKillInfo() {
        return Collections.unmodifiableMap(jobKillInfo);
    }
    
    /**
     * Get the payout information for fishing
     * @return the map of fishing and its payment
     */
    public Map<String, JobsMaterialInfo> getFishInfo() {
        return Collections.unmodifiableMap(jobFishInfo);
    }
    
    /**
     * Get the payout information for crafting
     * @return the map of fishing and its payment
     */
    public Map<String, JobsMaterialInfo> getCraftInfo() {
        return Collections.unmodifiableMap(jobCraftInfo);
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
