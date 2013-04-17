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

package me.zford.jobs.economy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import me.zford.jobs.Jobs;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.tasks.BufferedPaymentTask;

public class BufferedEconomy {
    private Economy economy;
    private LinkedBlockingQueue<BufferedPayment> payments = new LinkedBlockingQueue<BufferedPayment>();
    private final Map<String, BufferedPayment> paymentCache = Collections.synchronizedMap(new HashMap<String, BufferedPayment>());
    
    public BufferedEconomy (Economy economy) {
        this.economy = economy;
    }
    /**
     * Add payment to player's payment buffer
     * @param player - player to be paid
     * @param amount - amount to be paid
     */
    public void pay(JobsPlayer player, double amount) {
        if (amount == 0)
            return;
        pay(new BufferedPayment(player.getName(), amount));
    }
    
    /**
     * Add payment to player's payment buffer
     * @param payment - payment to be paid
     */
    public void pay(BufferedPayment payment) {
        payments.add(payment);
    }
    
    public String format(double money) {
        return economy.format(money);
    }
    
    /**
     * Payout all players the amount they are going to be paid
     */
    public void payAll() {
        if (payments.isEmpty())
            return;
        
        synchronized (paymentCache) {
            // combine all payments using paymentCache
            while (!payments.isEmpty()) {
                BufferedPayment payment = payments.remove();
                if (paymentCache.containsKey(payment.getPlayerName())) {
                    BufferedPayment existing = paymentCache.get(payment.getPlayerName());
                    existing.setAmount(existing.getAmount() + payment.getAmount());
                } else {
                    paymentCache.put(payment.getPlayerName(), payment);
                }
            }
            // Schedule all payments
            int i = 0;
            for (BufferedPayment payment : paymentCache.values()) {
                i++;
                Jobs.getScheduler().scheduleTask(new BufferedPaymentTask(this, economy, payment), i);
            }
            // empty payment cache
            paymentCache.clear();
        }
    }
}
