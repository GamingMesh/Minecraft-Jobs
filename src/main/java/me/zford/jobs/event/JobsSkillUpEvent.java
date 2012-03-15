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

package me.zford.jobs.event;

import me.zford.jobs.config.container.JobProgression;
import me.zford.jobs.config.container.JobsPlayer;
import me.zford.jobs.config.container.Title;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class JobsSkillUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private JobsPlayer player;
    private JobProgression jobProgression;
    private Title newTitle;

    public JobsSkillUpEvent(JobsPlayer player, JobProgression jobProgression, Title newTitle) {
        this.player = player;
        this.jobProgression = jobProgression;
        this.newTitle = newTitle;
        // TODO Auto-generated constructor stub
    }
    
    public JobsPlayer getPlayer(){
        return player;
    }
    
    public JobProgression getJobProgression(){
        return jobProgression;
    }
    
    public Title getNewTitle(){
        return newTitle;
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
