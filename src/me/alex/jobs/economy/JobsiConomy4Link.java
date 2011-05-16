package me.alex.jobs.economy;

import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;

/**
 * Class that interfaces with iConomy 4.6 and does the payment
 * @author Alex
 *
 */
public class JobsiConomy4Link implements JobsEconomyLink{
	// iConomy link
	private iConomy economy = null;
	
	/**
	 * Constructor for creating the link
	 * @param economy - the iConomy object
	 */
	public JobsiConomy4Link(iConomy economy){
		this.economy = economy;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void pay(Player player, double amount) {
		// TODO Auto-generated method stub
		economy.getBank().getAccount(player.getName()).add(amount);
	}
	
}
