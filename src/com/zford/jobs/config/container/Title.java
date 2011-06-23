package com.zford.jobs.config.container;
import org.bukkit.ChatColor;

/**
 * Container class for titles
 * @author Alex
 *
 */
public class Title {
	private String name = null;
	private String shortName = null;
	private ChatColor color = null;
	private int levelReq = 0;
	
	/**
	 * Constructor
	 * @param name - The long name of the title
	 * @param shortName - the short name of the title
	 * @param color - the ChatColor of the title
	 * @param levelReq -  the level requirement of the title
	 */
	public Title(String name, String shortName, ChatColor color, int levelReq){
		this.name = name;
		this.color = color;
		this.levelReq = levelReq;
		this.shortName = shortName;
	}
	
	/**
	 * Function to return the long name of the title
	 * @return the long name of the title
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Function to get the ChatColor of the title
	 * @return the chat colour o the title
	 */
	public ChatColor getChatColor(){
		return color;
	}
	
	/**
	 * Function to get the levelRequirement of the title
	 * @return the level requirement for the title
	 */
	public int getLevelReq(){
		return levelReq;
	}
	
	/**
	 * Function to get the short name of the title
	 * @return the short name of the title
	 */
	public String getShortName(){
		return shortName;
	}
}
