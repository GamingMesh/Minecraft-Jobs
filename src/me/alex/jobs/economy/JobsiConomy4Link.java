package me.alex.jobs.economy;

import me.alex.jobs.config.JobsConfiguration;

import org.bukkit.entity.Player;

import com.nidefawl.Stats.Stats;
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

	@Override
	public void updateStats(Player player) {
		// TODO Auto-generated method stub
		// stats plugin integration
		if(JobsConfiguration.getInstance().getStats() != null &&
				JobsConfiguration.getInstance().getStats().isEnabled()){
			Stats stats = JobsConfiguration.getInstance().getStats();
			double balance = economy.getBank().getAccount(player.getName()).getBalance();
			if(balance > stats.get(player.getName(), "job", "money")){
				stats.setStat(player.getName(), "job", "money", (int) balance);
				stats.saveAll();
			}
		}
	}
	
}
