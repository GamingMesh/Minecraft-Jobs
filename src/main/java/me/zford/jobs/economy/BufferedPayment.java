package me.zford.jobs.economy;

import java.util.HashMap;
import java.util.Map.Entry;

import me.zford.jobs.config.container.JobsPlayer;
import me.zford.jobs.economy.link.EconomyLink;


public class BufferedPayment {
    
    private HashMap<String, Double> payments;
    private EconomyLink economy;
    public BufferedPayment(EconomyLink economy) {
        this.economy = economy;
        payments = new HashMap<String, Double>();
    }

    /**
     * Add payment to player's payment buffer
     * @param player - player to be paid
     * @param amount - amount to be paid
     */
    public void pay(JobsPlayer player, double amount) {
        double total = 0;
        String playername = player.getName();
        if (payments.containsKey(playername))
            total = payments.get(playername);
        
        total += amount;
        payments.put(player.getName(), total);
    }
    
    /**
     * Payout all players the amount they are going to be paid
     */
    public void payAll() {
        if (payments.isEmpty())
            return;
        if (economy != null) {
            for (Entry<String, Double> entry : payments.entrySet()) {
                String playername = entry.getKey();
                double total = entry.getValue();
                economy.pay(playername, total);
            }
        }
        payments.clear();
    }
}
