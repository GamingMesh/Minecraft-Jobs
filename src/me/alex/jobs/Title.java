package me.alex.jobs;
import org.bukkit.ChatColor;


public class Title {
	private String name = null;
	private ChatColor color = null;
	private int levelReq = 0;
	public Title(String name, ChatColor color, int levelReq){
		this.name = name;
		this.color = color;
		this.levelReq = levelReq;
	}
	
	public String getName(){
		return name;
	}
	
	public ChatColor getChatColor(){
		return color;
	}
	
	public int getLevelReq(){
		return levelReq;
	}
}
