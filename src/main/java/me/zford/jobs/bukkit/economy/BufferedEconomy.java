package me.zford.jobs.bukkit.economy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.bukkit.tasks.BufferedPaymentTask;
import me.zford.jobs.container.JobsPlayer;
import me.zford.jobs.economy.BufferedPayment;

public class BufferedEconomy {
    
    private HashMap<String, Double> payments = new HashMap<String, Double>();
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
                int batchSize = plugin.getJobsConfiguration().getEconomyBatchSize();
                ArrayList<BufferedPayment> buffered = new ArrayList<BufferedPayment>(batchSize);
                for (Map.Entry<String, Double> entry : payments.entrySet()) {
                    if (buffered.size() >= batchSize) {
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BufferedPaymentTask(economy, buffered));
                        buffered = new ArrayList<BufferedPayment>(batchSize);
                    }
                    buffered.add(new BufferedPayment(entry.getKey(), entry.getValue()));
                }
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BufferedPaymentTask(economy, buffered));
            }
        }
        payments.clear();
    }
}
