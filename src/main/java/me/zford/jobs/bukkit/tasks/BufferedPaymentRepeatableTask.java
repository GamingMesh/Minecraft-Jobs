package me.zford.jobs.bukkit.tasks;

import me.zford.jobs.bukkit.economy.BufferedEconomy;

public class BufferedPaymentRepeatableTask implements Runnable {
    
    private BufferedEconomy economy;
    
    public BufferedPaymentRepeatableTask(BufferedEconomy economy) {
        this.economy = economy;
    }

    @Override
    public void run() {
        economy.payAll();
    }
}
