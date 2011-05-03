package me.alex.jobs;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.iConomy.iConomy;
import com.nidefawl.Stats.Stats;
import com.nijikokun.bukkit.Permissions.Permissions;

import cosine.boseconomy.BOSEconomy;

public class PluginListener extends ServerListener {
	Jobs plugin = null;
    public PluginListener(Jobs plugin) { 
    	this.plugin = plugin;
    }

    public void onPluginEnable(PluginEnableEvent event) {
        if(plugin.getiConomy() == null) {
            Plugin iConomy = plugin.getBukkitServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if(iConomy.isEnabled()) {
                	plugin.setiConomy((iConomy)iConomy);
                    System.out.println("[Jobs] Successfully linked with iConomy.");
                }
            }
        }
        if(plugin.getStats() == null) {
            Plugin stats = plugin.getBukkitServer().getPluginManager().getPlugin("Stats");

            if (stats != null) {
                if(stats.isEnabled()) {
                	plugin.setStats((Stats)stats);
                    System.out.println("[Jobs] Successfully linked with Stats.");
                }
            }
        }
        if(plugin.getBOSEconomy() == null) {
            Plugin boseconomy = plugin.getBukkitServer().getPluginManager().getPlugin("BOSEconomy");

            if (boseconomy != null) {
                if(boseconomy.isEnabled()) {
                	plugin.setBOSEconomy((BOSEconomy) boseconomy);
                    System.out.println("[Jobs] Successfully linked with BOSEconomy.");
                }
            }
        }
        if(plugin.getPermissions() == null) {
            Plugin permissions = plugin.getBukkitServer().getPluginManager().getPlugin("Permissions");

            if (permissions != null) {
                if(permissions.isEnabled()) {
                	plugin.setPermissions(((Permissions) permissions).getHandler());
                    System.out.println("[Jobs] Successfully linked with Permissions.");
                }
            }
        }
    }
}
