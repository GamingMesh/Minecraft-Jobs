package com.zford.jobs.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

import com.earth2me.essentials.Essentials;
import com.nidefawl.Stats.Stats;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.economy.link.BOSEconomy6Link;
import com.zford.jobs.economy.link.BOSEconomy7Link;
import com.zford.jobs.economy.link.EssentialsLink;
import com.zford.jobs.economy.link.iConomy5Link;
import com.zford.jobs.economy.link.iConomy6Link;

import cosine.boseconomy.BOSEconomy;

public class JobsPluginListener implements Listener {
    private Jobs plugin;
    private boolean craftingRegistered = false;
    public JobsPluginListener(Jobs plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority=EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        JobsConfiguration jc = plugin.getJobsConfiguration();
        PluginManager pm = plugin.getServer().getPluginManager();
        
        // economy plugins
        if(jc.getEconomyLink() == null){
            if(pm.getPlugin("iConomy") != null || 
                    pm.getPlugin("BOSEconomy") != null ||
                    pm.getPlugin("Essentials") != null) {
                if(pm.getPlugin("iConomy") != null && 
                        (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("iconomy"))) {
                    if(pm.getPlugin("iConomy").getDescription().getVersion().startsWith("5")) {
                        jc.setEconomyLink(new iConomy5Link(plugin, (com.iConomy.iConomy)pm.getPlugin("iConomy")));
                        System.out.println("[Jobs] Successfully linked with iConomy 5.");
                    } else if(pm.getPlugin("iConomy").getDescription().getVersion().startsWith("6")) {
                        jc.setEconomyLink(new iConomy6Link(plugin, (com.iCo6.iConomy)pm.getPlugin("iConomy")));
                        System.out.println("[Jobs] Successfully linked with iConomy 6.");
                    }
                } else if(pm.getPlugin("BOSEconomy") != null &&
                        (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("boseconomy"))) {
                    if(pm.getPlugin("BOSEconomy").getDescription().getVersion().startsWith("0.6")) {
                        jc.setEconomyLink(new BOSEconomy6Link(plugin, (BOSEconomy)pm.getPlugin("BOSEconomy")));
                        System.out.println("[Jobs] Successfully linked with BOSEconomy 6.");
                    } else if(pm.getPlugin("BOSEconomy").getDescription().getVersion().startsWith("0.7")) {
                        jc.setEconomyLink(new BOSEconomy7Link(plugin, (BOSEconomy)pm.getPlugin("BOSEconomy")));
                        System.out.println("[Jobs] Successfully linked with BOSEconomy 7.");
                    }
                } else if(pm.getPlugin("Essentials") != null &&
                        (jc.getDefaultEconomy() == null || jc.getDefaultEconomy().equalsIgnoreCase("essentials"))) {
                    jc.setEconomyLink(new EssentialsLink(plugin, (Essentials)pm.getPlugin("Essentials")));
                    System.out.println("[Jobs] Successfully linked with Essentials.");
                }
            }
        }
        
        // stats
        if(jc.getStats() == null && jc.isStatsEnabled()){
            if(pm.getPlugin("Stats") != null){
                jc.setStats((Stats)pm.getPlugin("Stats"));
                System.out.println("[Jobs] Successfully linked with Stats.");
            }
        }
        
        // spout
        if(!craftingRegistered){
            if(plugin.getServer().getPluginManager().getPlugin("Spout") != null){
                plugin.getServer().getPluginManager().registerEvents(new JobsCraftPaymentListener(plugin), plugin);
                craftingRegistered = true;
                System.out.println("[Jobs] Successfully linked with Spout.");
            }
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        JobsConfiguration jc = plugin.getJobsConfiguration();
        if(jc.getEconomyLink() instanceof iConomy5Link && event.getPlugin().getDescription().getName().equalsIgnoreCase("iConomy") ||
                jc.getEconomyLink() instanceof BOSEconomy6Link && event.getPlugin().getDescription().getName().equalsIgnoreCase("BOSEconomy") ||
                jc.getEconomyLink() instanceof EssentialsLink && event.getPlugin().getDescription().getName().equalsIgnoreCase("Essentials")
                ) {
            jc.setEconomyLink(null);
            System.out.println("[Jobs] Economy system successfully unlinked.");
        }
        
        // stats
        if(event.getPlugin().getDescription().getName().equalsIgnoreCase("Stats")){
            jc.setStats(null);
            System.out.println("[Jobs] Successfully unlinked with Stats.");
        }
    }

}
