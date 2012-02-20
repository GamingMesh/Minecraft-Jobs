/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.zford.jobs.economy.link;

import com.iConomy.iConomy;
import com.iConomy.system.BankAccount;
import com.nidefawl.Stats.Stats;
import com.zford.jobs.Jobs;

/**
 * Class that interfaces with iConomy and does the payment
 * @author Alex
 *
 */
public class iConomy5Link implements EconomyLink{
	// iConomy link
	private iConomy economy;
	private Jobs plugin;
	
	/**
	 * Constructor for creating the link
	 * @param economy - the iConomy object
	 */
	public iConomy5Link(Jobs plugin, iConomy economy){
		this.economy = economy;
	}
	
	@SuppressWarnings("static-access")
	@Override
    public void pay(String playername, double amount) {
		economy.getAccount(playername).getHoldings().add(amount);
	}

	@SuppressWarnings("static-access")
	@Override
	public void updateStats(String playername) {
		// stats plugin integration
		if(plugin.getJobsConfiguration().getStats() != null &&
		        plugin.getJobsConfiguration().getStats().isEnabled()){
			Stats stats = plugin.getJobsConfiguration().getStats();
			double balance = economy.getAccount(playername).getHoldings().balance();
			if(economy.getAccount(playername) != null &&
					economy.getAccount(playername).getBankAccounts() != null){
				for(BankAccount temp: economy.getAccount(playername).getBankAccounts()){
					balance += temp.getHoldings().balance();
				}
			}
			if(balance > stats.get(playername, "job", "money")){
				stats.setStat(playername, "job", "money", (int) balance);
				stats.saveAll();
			}
		}
	}
	
}
