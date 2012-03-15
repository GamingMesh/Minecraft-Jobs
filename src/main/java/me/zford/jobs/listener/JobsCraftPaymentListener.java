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

package me.zford.jobs.listener;

import java.util.List;

import me.zford.jobs.Jobs;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class JobsCraftPaymentListener implements Listener{
	
	private Jobs plugin;
	
	public JobsCraftPaymentListener(Jobs plugin){
		this.plugin = plugin;
	}

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onInventoryCraft(InventoryClickEvent event) {
		// make sure plugin is enabled
	    if(!plugin.isEnabled()) return;
        Inventory inv = event.getInventory();
        
        if (!(inv instanceof CraftingInventory) || !event.getSlotType().equals(SlotType.RESULT))
            return;
        
        Recipe recipe = ((CraftingInventory) inv).getRecipe();

        System.out.println("CRAFT");
	    
        // restricted area multiplier
        List<HumanEntity> viewers = event.getViewers();
        if (viewers.size() == 0)
            return;
        Player player = null;
        for (HumanEntity viewer : event.getViewers()) {
            if (viewer instanceof Player) {
                player = (Player) viewer;
                break;
            }
        }
        
        if (player == null)
            return;
        System.out.println("CRAFT");
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        if (!plugin.hasWorldPermission(player, player.getWorld()))
            return;
        
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(player);
        
        ItemStack stack = recipe.getResult();
		plugin.getJobsManager().getJobsPlayer(player.getName()).crafted(stack, multiplier);
	}
}
