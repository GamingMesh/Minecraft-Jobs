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

package me.zford.jobs.bukkit.tasks;

import java.util.List;

import me.zford.jobs.economy.BufferedPayment;
import net.milkbowl.vault.economy.Economy;

public class BufferedPaymentTask implements Runnable {
    private Economy economy;
    private List<BufferedPayment> payments;
    public BufferedPaymentTask(Economy economy, List<BufferedPayment> payments) {
        this.economy = economy;
        this.payments = payments;
    }
    @Override
    public void run() {
        for (BufferedPayment payment : payments) {
            if (payment.getAmount() > 0) {
                economy.depositPlayer(payment.getPlayerName(), payment.getAmount());
            } else {
                economy.withdrawPlayer(payment.getPlayerName(), -payment.getAmount());
            }
        }
    }
}
