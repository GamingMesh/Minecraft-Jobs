package com.zford.jobs.event;


import org.bukkit.entity.Player;

import com.zford.jobs.config.container.JobProgression;
import com.zford.jobs.config.container.Title;

@SuppressWarnings("serial")
public class JobsSkillUpEvent extends JobsEvent{
	private Player player;
	private JobProgression jobProgression;
	private Title newTitle;

	public JobsSkillUpEvent(Player player, JobProgression jobProgression, Title newTitle) {
		super(JobsEventType.SkillUp);
		this.player = player;
		this.jobProgression = jobProgression;
		this.newTitle = newTitle;
		// TODO Auto-generated constructor stub
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public JobProgression getJobProgression(){
		return jobProgression;
	}
	
	public Title getNewTitle(){
		return newTitle;
	}
}
