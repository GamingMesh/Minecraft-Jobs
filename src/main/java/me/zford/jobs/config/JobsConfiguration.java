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

package me.zford.jobs.config;

import java.util.Locale;

import me.zford.jobs.Player;
import me.zford.jobs.container.Title;

public interface JobsConfiguration {
    public void reload();
    
    /**
     * Get how often in minutes to save job information
     * @return how often in minutes to save job information
     */
    public int getSavePeriod();
    
    /**
     * Function that tells if the system is set to broadcast on skill up
     * @return true - broadcast on skill up
     * @return false - do not broadcast on skill up
     */
    public boolean isBroadcastingSkillups();
    
    /**
     * Function that tells if the system is set to broadcast on level up
     * @return true - broadcast on level up
     * @return false - do not broadcast on level up
     */
    public boolean isBroadcastingLevelups();
    
    /**
     * Function that tells if the player should be paid while in creative
     * @return true - pay in creative
     * @return false - do not pay in creative
     */
    public boolean payInCreative();
    
    /**
     * Function to return the title for a given level
     * @return the correct title
     * @return null if no title matches
     */
    public Title getTitleForLevel(int level);
    
    public boolean addXpPlayer();
    
    /**
     * Function to check if jobs should be hidden to players that lack permission to join the job
     * @return
     */
    public boolean getHideJobsWithoutPermission();
    
    /**
     * Function to return the maximum number of jobs a player can join
     * @return
     */
    public int getMaxJobs();
    
    /**
     * Function to check if you get paid near a spawner is enabled
     * @return true - you get paid
     * @return false - you don't get paid
     */
    public boolean payNearSpawner();
    
    /**
     * Gets the area multiplier for the player
     * @param player
     * @return - the multiplier
     */
    public double getRestrictedMultiplier(Player player);
    
    public boolean getModifyChat();
    
    public int getEconomyBatchDelay();
    
    public boolean saveOnDisconnect();
    
    public Locale getLocale();
}
