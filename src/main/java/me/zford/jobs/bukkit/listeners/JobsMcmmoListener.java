/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
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
 * 
 */

package me.zford.jobs.bukkit.listeners;

import me.zford.jobs.bukkit.JobsPlugin;
import me.zford.jobs.bukkit.actions.ItemActionInfo;
import me.zford.jobs.container.ActionType;
import me.zford.jobs.container.JobsPlayer;

import org.bukkit.GameMode;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.events.skills.McMMOPlayerRepairCheckEvent;

public class JobsMcmmoListener implements Listener {
	
    private JobsPlugin plugin;
    
    public JobsMcmmoListener(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onMcMMOPlayerRepairCheck(McMMOPlayerRepairCheckEvent event) {
    	
    	if (!plugin.isEnabled()) return;

    	Player player = event.getPlayer();
    	
    	if (player == null) return;
    	
    	if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
    	
    	ItemStack repairMaterial = event.getRepairMaterial();
    	
    	if (repairMaterial == null)
            return;

    	ItemStack materialsUsed = new ItemStack(repairMaterial.getType(), 1);
    	
    	double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(player);
        JobsPlayer jPlayer = plugin.getPlayerManager().getJobsPlayer(player.getName());
        plugin.action(jPlayer, new ItemActionInfo(materialsUsed, ActionType.REPAIR), multiplier);
    }
}
