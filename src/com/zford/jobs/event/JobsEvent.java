package com.zford.jobs.event;

import org.bukkit.event.Event;

@SuppressWarnings("serial")
public abstract class JobsEvent extends Event{
	private JobsEventType type;
	private boolean cancelled;
	
	protected JobsEvent(JobsEventType type) {
		super("JobsEvent");
		this.type = type;
		cancelled = false;
		// TODO Auto-generated constructor stub
	}

	public JobsEventType getJobsEventType() {
		return type;
	}
	
	public boolean isCancelled(){
		return cancelled;
	}
	
	public void setCancelled(boolean cancelled){
		this.cancelled = cancelled;
	}
}
