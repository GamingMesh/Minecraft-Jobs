package me.alex.jobs.event;

import me.alex.jobs.config.container.JobProgression;
import me.alex.jobs.config.container.Title;

import org.bukkit.entity.Player;

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
