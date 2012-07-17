package me.zford.jobs.bukkit.tasks;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.RegisteredServiceProvider;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.bukkit.economy.BufferedEconomy;

public class BufferedPaymentThread extends Thread {
    
    private JobsPlugin plugin;
    private BufferedEconomy bufferedEconomy;
    
    private volatile boolean running = true;
    private int sleep;
    
    public BufferedPaymentThread(JobsPlugin plugin, BufferedEconomy bufferedEconomy, int duration) {
        super("Jobs-BufferedPaymentThread");
        this.plugin = plugin;
        this.bufferedEconomy = bufferedEconomy;
        this.sleep = duration * 1000;
    }

    @Override
    public void run() {
        plugin.getLogger().info("Started buffered payment thread");
        while (running) {
            try {
                sleep(sleep);
            } catch (InterruptedException e) {
                this.running = false;
                continue;
            }
            try {
                Future<Economy> economyFuture = plugin.getServer().getScheduler().callSyncMethod(plugin, new EconomyRegistrationCallable());
                Economy economy = economyFuture.get();
                if (economy != null)
                    bufferedEconomy.payAll(economy);
            } catch (Throwable t) {
                t.printStackTrace();
                plugin.getLogger().severe("Exception in BufferedPaymentThread, stopping economy payments!");
                running = false;
            }
        }
        plugin.getLogger().info("Buffered payment thread shutdown");   
    }
    
    public void shutdown() {
        this.running = false;
        interrupt();
    }
    
    public class EconomyRegistrationCallable implements Callable<Economy> {
        @Override
        public Economy call() throws Exception {
            RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (provider == null)
                return null;
            
            Economy economy = provider.getProvider();
            
            if (economy == null || !economy.isEnabled())
                return null;
            
            return economy;
        }
    }
}
