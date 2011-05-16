package me.alex.jobs.economy;

import org.bukkit.entity.Player;

import com.iConomy.iConomy;

/**
 * Class that interfaces with iConomy and does the payment
 * @author Alex
 *
 */
public class JobsiConomyLink implements JobsEconomyLink{
	// iConomy link
	private iConomy economy = null;
	
	/**
	 * Constructor for creating the link
	 * @param economy - the iConomy object
	 */
	public JobsiConomyLink(iConomy economy){
		this.economy = economy;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void pay(Player player, double amount) {
		// TODO Auto-generated method stub
		economy.getAccount(player.getName()).getHoldings().add(amount);
	}
	
}
