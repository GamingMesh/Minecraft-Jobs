package com.zford.jobs.economy;

import org.bukkit.entity.Player;

/**
 * Interface that to be used when creating a class that talks with the 
 * economy systems (e.g. iConomy, BOSEconomy ...)
 * @author Alex
 *
 */
public interface JobsEconomyLink {
	/**
	 * Pay the player
	 * @param player - player to be paid
	 * @param amount - amount to be paid
	 */
	public void pay(Player player, double amount);
	
	/**
	 * Update Stats plugin money stats
	 * @param player - the player
	 */
	public void updateStats(Player player);
}
