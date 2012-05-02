package me.zford.jobs.tasks;

import me.zford.jobs.economy.BufferedPayment;

public class BufferedPaymentTask implements Runnable {
    
    private BufferedPayment economy;
    
    public BufferedPaymentTask(BufferedPayment economy) {
        this.economy = economy;
    }

    @Override
    public void run() {
        economy.payAll();
    }
}
