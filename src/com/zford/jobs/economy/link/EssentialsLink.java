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


import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.nidefawl.Stats.Stats;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.config.container.JobsPlayer;

public class EssentialsLink implements EconomyLink{
	
	public EssentialsLink(Essentials essentials){
	}
	
	@Override
	public void pay(JobsPlayer player, double amount) {
		try {
			Economy.add(player.getName(), amount);
		} catch (UserDoesNotExistException e) {
			e.printStackTrace();
		} catch (NoLoanPermittedException e) {
			e.printStackTrace();
		}
	}

    @Override
	public void updateStats(JobsPlayer player) {
        // stats plugin integration
        if(JobsConfiguration.getInstance().getStats() != null &&
                JobsConfiguration.getInstance().getStats().isEnabled()){
            Stats stats = JobsConfiguration.getInstance().getStats();
            double balance;
            try {
                balance = Economy.getMoney(player.getName());
            } catch(UserDoesNotExistException e) {
                e.printStackTrace();
                return;
            }
            if(balance > stats.get(player.getName(), "job", "money")){
                stats.setStat(player.getName(), "job", "money", (int) balance);
                stats.saveAll();
            }
        }
    }

}
