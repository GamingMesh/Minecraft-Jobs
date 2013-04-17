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

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import me.zford.jobs.Player;
import me.zford.jobs.container.RestrictedArea;
import me.zford.jobs.container.Title;

public abstract class JobsConfiguration {
    // all of the possible titles
    protected List<Title> titles = new ArrayList<Title>();
    protected ArrayList<RestrictedArea> restrictedAreas = new ArrayList<RestrictedArea>();
    protected Locale locale;
    protected int savePeriod;
    protected boolean isBroadcastingSkillups;
    protected boolean isBroadcastingLevelups;
    protected boolean payInCreative;
    protected boolean addXpPlayer;
    protected boolean hideJobsWithoutPermission;
    protected int maxJobs;
    protected boolean payNearSpawner;
    protected boolean modifyChat;
    protected int economyBatchDelay;
    protected boolean saveOnDisconnect;
    
    public abstract void reload();
    
    /**
     * Get how often in minutes to save job information
     * @return how often in minutes to save job information
     */
    public synchronized int getSavePeriod() {
        return savePeriod;
    }
    
    /**
     * Function that tells if the system is set to broadcast on skill up
     * @return true - broadcast on skill up
     * @return false - do not broadcast on skill up
     */
    public synchronized boolean isBroadcastingSkillups() {
        return isBroadcastingSkillups;
    }
    
    /**
     * Function that tells if the system is set to broadcast on level up
     * @return true - broadcast on level up
     * @return false - do not broadcast on level up
     */
    public synchronized boolean isBroadcastingLevelups() {
        return isBroadcastingLevelups;
    }
    
    /**
     * Function that tells if the player should be paid while in creative
     * @return true - pay in creative
     * @return false - do not pay in creative
     */
    public synchronized boolean payInCreative() {
        return payInCreative;
    }
    
    /**
     * Function to return the title for a given level
     * @return the correct title
     * @return null if no title matches
     */
    public Title getTitleForLevel(int level) {
        Title title = null;
        for (Title t: titles) {
            if (title == null) {
                if (t.getLevelReq() <= level) {
                    title = t;
                }
            } else {
                if (t.getLevelReq() <= level && t.getLevelReq() > title.getLevelReq()) {
                    title = t;
                }
            }
        }
        return title;
    }
    
    public synchronized boolean addXpPlayer() {
        return addXpPlayer;
    }
    
    /**
     * Function to check if jobs should be hidden to players that lack permission to join the job
     * @return
     */
    public synchronized boolean getHideJobsWithoutPermission() {
        return hideJobsWithoutPermission;
    }
    
    /**
     * Function to return the maximum number of jobs a player can join
     * @return
     */
    public synchronized int getMaxJobs() {
        return maxJobs;
    }
    
    /**
     * Function to check if you get paid near a spawner is enabled
     * @return true - you get paid
     * @return false - you don't get paid
     */
    public synchronized boolean payNearSpawner() {
        return payNearSpawner;
    }
    
    /**
     * Gets the area multiplier for the player
     * @param player
     * @return - the multiplier
     */
    public synchronized double getRestrictedMultiplier(Player player) {
        for(RestrictedArea area : restrictedAreas) {
            if (area.inRestrictedArea(player))
                return area.getMultiplier();
        }
        return 1.0;
    }
    
    public synchronized boolean getModifyChat() {
        return modifyChat;
    }
    
    public synchronized int getEconomyBatchDelay() {
        return economyBatchDelay;
    }
    
    public synchronized boolean saveOnDisconnect() {
        return saveOnDisconnect;
    }
    
    public synchronized Locale getLocale() {
        return locale;
    }
}
