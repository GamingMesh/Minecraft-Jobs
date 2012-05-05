package me.zford.jobs.tasks;

import me.zford.jobs.economy.BufferedEconomy;

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
