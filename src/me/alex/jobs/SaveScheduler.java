package me.alex.jobs;

import java.util.TimerTask;

public class SaveScheduler extends TimerTask{
	Jobs plugin = null;
	public SaveScheduler(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void run(){
		plugin.saveAll();
	}
}