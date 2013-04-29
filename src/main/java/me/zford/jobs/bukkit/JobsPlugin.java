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

package me.zford.jobs.bukkit;

import me.zford.jobs.Jobs;
import me.zford.jobs.bukkit.commands.BukkitJobsCommands;
import me.zford.jobs.bukkit.config.BukkitJobConfig;
import me.zford.jobs.bukkit.config.BukkitJobsConfiguration;
import me.zford.jobs.bukkit.economy.VaultEconomy;
import me.zford.jobs.bukkit.listeners.JobsListener;
import me.zford.jobs.bukkit.listeners.JobsPaymentListener;
import me.zford.jobs.config.ConfigManager;
import me.zford.jobs.economy.BlackholeEconomy;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class JobsPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Jobs.setPermissionHandler(new BukkitPermissionHandler(this));
        Jobs.setServer(new BukkitServer());
        Jobs.setScheduler(new BukkitTaskScheduler(this));
        
        Jobs.setPluginLogger(getLogger());
        
        Jobs.setDataFolder(getDataFolder());
        
        ConfigManager.registerJobsConfiguration(new BukkitJobsConfiguration(this));
        ConfigManager.registerJobConfig(new BukkitJobConfig(this));
        
        getCommand("jobs").setExecutor(new BukkitJobsCommands());
        
        Jobs.startup();
        
        // register the listeners
        getServer().getPluginManager().registerEvents(new JobsListener(this), this);
        getServer().getPluginManager().registerEvents(new JobsPaymentListener(this), this);
        
        // register economy
        Jobs.getScheduler().scheduleTask(new Runnable() {
            public void run() {
                Plugin test = getServer().getPluginManager().getPlugin("Vault");
                if (test != null) {
                    RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
                    if (provider != null) {
                        Economy economy = provider.getProvider();
                        
                        if (economy != null && economy.isEnabled()) {
                            Jobs.setEconomy(new VaultEconomy(economy));
                            Jobs.getPluginLogger().info("["+getDescription().getName()+"] Successfully linked with Vault.");
                            return;
                        }
                    }
                }
                
                // no Vault found
                Jobs.setEconomy(new BlackholeEconomy());
                Jobs.getServer().getLogger().severe("==================== Jobs ====================");
                Jobs.getServer().getLogger().severe("Vault is required by this plugin for economy support!");
                Jobs.getServer().getLogger().severe("Please install Vault first!");
                Jobs.getServer().getLogger().severe("You can find the latest version here:");
                Jobs.getServer().getLogger().severe("http://dev.bukkit.org/server-mods/vault/");
                Jobs.getServer().getLogger().severe("==============================================");
            }
        });
        
        // all loaded properly.
        Jobs.getPluginLogger().info("Plugin has been enabled succesfully.");
    }
    
    @Override
    public void onDisable() {
        Jobs.shutdown();
        Jobs.getPluginLogger().info("Plugin has been disabled succesfully.");
    }
}
