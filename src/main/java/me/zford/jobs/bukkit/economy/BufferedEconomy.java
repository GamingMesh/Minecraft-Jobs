/**
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011 Zak Ford <zak.j.ford@gmail.com>
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
 */

package me.zford.jobs.bukkit.economy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.bukkit.tasks.BufferedPaymentTask;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.economy.BufferedPayment;

public class BufferedEconomy {
    
    private Map<String, Double> payments = Collections.synchronizedMap(new LinkedHashMap<String, Double>());
    private JobsPlugin plugin;
    public BufferedEconomy(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Add payment to player's payment buffer
     * @param player - player to be paid
     * @param amount - amount to be paid
     */
    public void pay(JobsPlayer player, double amount) {
        if (amount == 0)
            return;
        double total = 0;
        String playername = player.getName();
        
        synchronized (payments) {
            if (payments.containsKey(playername))
                total = payments.get(playername);
            
            total += amount;
            payments.put(player.getName(), total);
        }
    }
    
    /**
     * Payout all players the amount they are going to be paid
     */
    public void payAll(Economy economy) {
        if (payments.isEmpty())
            return;
        
        int batchSize = plugin.getJobsConfiguration().getEconomyBatchSize();
        ArrayList<BufferedPayment> buffered = new ArrayList<BufferedPayment>(batchSize);
        synchronized (payments) {
            int i = 0;
            for (Map.Entry<String, Double> entry : payments.entrySet()) {
                if (buffered.size() >= batchSize) {
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BufferedPaymentTask(economy, buffered), i);
                    buffered = new ArrayList<BufferedPayment>(batchSize);
                    i++;
                }
                String playername = entry.getKey();
                double payment = entry.getValue().doubleValue();
                if (payment != 0)
                    buffered.add(new BufferedPayment(playername, payment));
            }
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BufferedPaymentTask(economy, buffered), i);
            
            payments.clear();
        }
    }
}
