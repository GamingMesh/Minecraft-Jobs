package me.alex.jobs.event;

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
