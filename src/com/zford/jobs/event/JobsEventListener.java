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

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public abstract class JobsEventListener extends CustomEventListener{
	
	@Override
	public void onCustomEvent(Event event) {
		if(event instanceof JobsEvent){
			JobsEvent jobEvent = (JobsEvent)event;
			switch(jobEvent.getJobsEventType()){
			case LevelUp:
				// leveling up
				onJobLevelUp((JobsLevelUpEvent)event);
				break;
			case Join:
				// joining a job
				onJobJoin((JobsJoinEvent)event);
				break;
			case Leave:
				// leaving a job
				onJobLeave((JobsLeaveEvent)event);
				break;
			case SkillUp:
				onJobSkillUp((JobsSkillUpEvent)event);
				break;
			default:
				// do nothing
			}
		}
		super.onCustomEvent(event);
	}
	
	/**
	 * Called when the level up event is called
	 * @param event - the level up event
	 */
	public void onJobLevelUp(JobsLevelUpEvent event){
		
	}
	
	/**
	 * Called when the skill up event is called
	 * @param event - the skill up event
	 */
	public void onJobSkillUp(JobsSkillUpEvent event){
		
	}
	
	/**
	 * Called when the leave job event is called
	 * @param event - the leave job event
	 */
	public void onJobLeave(JobsLeaveEvent event){
		
	}
	
	/**
	 * Called when the join job event is called
	 * @param event - the join job event
	 */
	public void onJobJoin(JobsJoinEvent event){
		
	}
}
