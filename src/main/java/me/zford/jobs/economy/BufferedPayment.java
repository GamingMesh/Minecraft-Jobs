package me.zford.jobs.economy;

import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.JobsPlayer;

public class BufferedPayment {
    
    private HashMap<String, Double> payments = new HashMap<String, Double>();
    private Jobs plugin;
    public BufferedPayment(Jobs plugin) {
        this.plugin = plugin;
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
        RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            Economy economy = provider.getProvider();
            if (economy.isEnabled()) {
                for (Map.Entry<String, Double> entry : payments.entrySet()) {
                    String playername = entry.getKey();
                    double total = entry.getValue();
                    economy.depositPlayer(playername, total);
                }
            }
        }
        payments.clear();
    }
}
