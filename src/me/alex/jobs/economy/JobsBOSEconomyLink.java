package me.alex.jobs.economy;

import org.bukkit.entity.Player;

import cosine.boseconomy.BOSEconomy;

/**
 * Class that interfaces with BOSEconomt and does the payment
 * @author Alex
 *
 */
public class JobsBOSEconomyLink implements JobsEconomyLink{
	private BOSEconomy economy;
	
	/**
	 * Constructor for creating the link
	 * @param economy - the BOSEconomy object
	 */
	public JobsBOSEconomyLink(BOSEconomy economy) {
		this.economy = economy;
	}
	
	@Override
	public void pay(Player player, double amount) {
		economy.addPlayerMoney(player.getName(), (int)amount, true);
	}

}
