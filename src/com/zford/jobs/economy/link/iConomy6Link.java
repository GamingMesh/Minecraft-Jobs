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



import com.iCo6.iConomy;
import com.iCo6.system.Accounts;
import com.nidefawl.Stats.Stats;
import com.zford.jobs.config.JobsConfiguration;

/**
 * Class that interfaces with iConomy and does the payment
 * @author Alex
 *
 */
public class iConomy6Link implements EconomyLink{
	private Accounts accounts;
	
	/**
	 * Constructor for creating the link
	 * @param economy - the iConomy object
	 */
	public iConomy6Link(iConomy economy){
		this.accounts = new Accounts();
	}
	
	@Override
    public void pay(String playername, double amount) {
	    accounts.get(playername).getHoldings().add(amount);
	}

	@Override
	public void updateStats(String playername) {
		// stats plugin integration
		if(JobsConfiguration.getInstance().getStats() != null &&
				JobsConfiguration.getInstance().getStats().isEnabled()){
			Stats stats = JobsConfiguration.getInstance().getStats();
			double balance = accounts.get(playername).getHoldings().getBalance();
			if(balance > stats.get(playername, "job", "money")){
				stats.setStat(playername, "job", "money", (int) balance);
				stats.saveAll();
			}
		}
	}
	
}
