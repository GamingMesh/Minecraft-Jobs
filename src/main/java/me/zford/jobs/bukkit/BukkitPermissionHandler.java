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

import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import me.zford.jobs.Jobs;
import me.zford.jobs.PermissionHandler;
import me.zford.jobs.Player;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobPermission;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;

public class BukkitPermissionHandler implements PermissionHandler {
    private JobsPlugin plugin;
    public BukkitPermissionHandler(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public void recalculatePermissions(JobsPlayer jPlayer) {
        org.bukkit.entity.Player player = plugin.getServer().getPlayer(jPlayer.getName());
        if (player == null)
            return;
        
        boolean changed = false;
        
        // remove old permissions
        String permName = "jobs.players."+player.getName();
        Permission permission = plugin.getServer().getPluginManager().getPermission(permName);
        if (permission != null) {
            plugin.getServer().getPluginManager().removePermission(permission);
            changed = true;
        }
        
        // Permissions should only apply if we have permission to use jobs in this world
        if (hasWorldPermission(BukkitUtil.wrapPlayer(player), player.getWorld().getName())) {
            List<JobProgression> progression = jPlayer.getJobProgression();
            // calculate new permissions
            HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();
            if (progression.size() == 0) {
                Job job = Jobs.getNoneJob();
                if (job != null) {
                    for (JobPermission perm : job.getPermissions()) {
                        if (perm.getLevelRequirement() <= 0) {
                            if (perm.getValue()) {
                                permissions.put(perm.getNode(), true);
                            } else {
                                /*
                                 * If the key exists, don't put a false node in
                                 * This is in case we already have a true node there
                                 */
                                if (!permissions.containsKey(perm.getNode())) {
                                    permissions.put(perm.getNode(), false);
                                }
                            }
                        }
                    }
                }
            } else {
                for (JobProgression prog : progression) {
                    for (JobPermission perm : prog.getJob().getPermissions()) {
                        if (prog.getLevel() >= perm.getLevelRequirement()) {
                            /*
                             * If the key exists, don't put a false node in
                             * This is in case we already have a true node there
                             */
                            if (perm.getValue()) {
                                permissions.put(perm.getNode(), true);
                            } else {
                                if (!permissions.containsKey(perm.getNode())) {
                                    permissions.put(perm.getNode(), false);
                                }
                            }
                        }
                    }
                }
            }
            
            // add new permissions (if applicable)
            if (permissions.size() > 0) {
                plugin.getServer().getPluginManager().addPermission(new Permission(permName, PermissionDefault.FALSE, permissions));
                changed = true;
            }
        }
        
        // If the permissions changed, recalculate them
        if (!changed)
            return;
        
        // find old attachment
        PermissionAttachment attachment = null;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (pai.getAttachment() != null && pai.getAttachment().getPlugin() instanceof JobsPlugin) {
                attachment = pai.getAttachment();
            }
        }
        
        // create if attachment doesn't exist
        if (attachment == null) {
            attachment = player.addAttachment(plugin);
            attachment.setPermission(permName, true);
        }
        
        // recalculate!
        player.recalculatePermissions();
    }
    
    @Override
    public void registerPermissions() {
        PluginManager pm = plugin.getServer().getPluginManager();
        for (World world : plugin.getServer().getWorlds()) {
            if (pm.getPermission("jobs.world."+world.getName().toLowerCase()) == null)
                pm.addPermission(new Permission("jobs.world."+world.getName().toLowerCase(), PermissionDefault.TRUE));
        }
        for (Job job : Jobs.getJobs()) {
            if (pm.getPermission("jobs.join."+job.getName().toLowerCase()) == null)
                pm.addPermission(new Permission("jobs.join."+job.getName().toLowerCase(), PermissionDefault.TRUE));
        }
    }
    
    /**
     * Check World permissions
     */
    @Override
    public boolean hasWorldPermission(Player player, String world) {
        if (!player.hasPermission("jobs.use")) {
            return false;
        } else {
            return player.hasPermission("jobs.world."+world.toLowerCase());
        }
    }

}
