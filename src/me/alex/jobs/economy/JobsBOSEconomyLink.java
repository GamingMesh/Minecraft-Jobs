package me.alex.jobs.economy;

import me.alex.jobs.config.JobsConfiguration;

import org.bukkit.entity.Player;

import com.nidefawl.Stats.Stats;

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

	@Override
	public void updateStats(Player player) {
		// TODO Auto-generated method stub
		// stats plugin integration
		if(JobsConfiguration.getInstance().getStats() != null &&
				JobsConfiguration.getInstance().getStats().isEnabled()){
			Stats stats = JobsConfiguration.getInstance().getStats();
			double balance = economy.getPlayerMoney(player.getName()) + economy.getBankMoney(player.getName());
			if(balance > stats.get(player.getName(), "job", "money")){
				stats.setStat(player.getName(), "job", "money", (int) balance);
				stats.saveAll();
			}
		}
	}

}
