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

package com.zford.jobs.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.zford.jobs.config.container.JobProgression;
import com.zford.jobs.config.container.JobsPlayer;

@SuppressWarnings("serial")
public class JobsLevelUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private JobsPlayer player;
    private JobProgression jobProgression;

    public JobsLevelUpEvent(JobsPlayer player, JobProgression jobProgression) {
        this.player = player;
        this.jobProgression = jobProgression;
    }
    
    public JobsPlayer getPlayer(){
        return player;
    }
    
    public JobProgression getJobProgression(){
        return jobProgression;
    }
    
    public void checkLevels(){
        player.checkLevels();
    }
    
    public int getNumJobs(){
        return player.getJobs().size();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
