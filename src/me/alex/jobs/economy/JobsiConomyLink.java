package me.alex.jobs.economy;

import me.alex.jobs.config.JobsConfiguration;

import org.bukkit.entity.Player;

import com.iConomy.iConomy;
import com.iConomy.system.BankAccount;
import com.nidefawl.Stats.Stats;

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
		economy.getAccount(player.getName()).getHoldings().add(amount);
	}

	@SuppressWarnings("static-access")
	@Override
	public void updateStats(Player player) {
		// stats plugin integration
		if(JobsConfiguration.getInstance().getStats() != null &&
				JobsConfiguration.getInstance().getStats().isEnabled()){
			Stats stats = JobsConfiguration.getInstance().getStats();
			double balance = economy.getAccount(player.getName()).getHoldings().balance();
			for(BankAccount temp: economy.getAccount(player.getName()).getBankAccounts()){
				balance += temp.getHoldings().balance();
			}
			if(balance > stats.get(player.getName(), "job", "money")){
				stats.setStat(player.getName(), "job", "money", (int) balance);
				stats.saveAll();
			}
		}
	}
	
}
