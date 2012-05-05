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
            economy.depositPlayer(payment.getPlayerName(), payment.getAmount());
        }
    }
}
