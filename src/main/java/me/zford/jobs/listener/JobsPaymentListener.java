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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobsPlayer;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class JobsPaymentListener implements Listener {
    private Jobs plugin;
    private Set<LivingEntity> mobSpawnerCreatures = Collections.newSetFromMap(new WeakHashMap<LivingEntity, Boolean>());
    
    public JobsPaymentListener(Jobs plugin){
        this.plugin = plugin;
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        
        Player player = event.getPlayer();
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        // restricted area multiplier
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(event.getPlayer());
        
        if(plugin.hasWorldPermission(player, player.getWorld())) {
            plugin.getJobsManager().getJobsPlayer(player.getName()).broke(event.getBlock(), multiplier);            
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        
        // check to make sure you can build
        if(!event.canBuild()) return;
        
        Player player = event.getPlayer();
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        // restricted area multiplier
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(event.getPlayer());
        
        if(plugin.hasWorldPermission(player, player.getWorld())) {
            plugin.getJobsManager().getJobsPlayer(player.getName()).placed(event.getBlock(), multiplier);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerFish(PlayerFishEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        
        Player player = event.getPlayer();
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        if (!plugin.hasWorldPermission(player, player.getWorld())) return;
        
        // restricted area multiplier
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(player);
        
        if(event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) && event.getCaught() instanceof Item) {
            plugin.getJobsManager().getJobsPlayer(player.getName()).fished((Item)event.getCaught(), multiplier);
        }
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onInventoryCraft(InventoryClickEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        Inventory inv = event.getInventory();
        
        if (!(inv instanceof CraftingInventory) || !event.getSlotType().equals(SlotType.RESULT))
            return;
        
        Recipe recipe = ((CraftingInventory) inv).getRecipe();
        
        if (recipe == null)
            return;
        
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
        
        // check if in creative
        if (player.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
            return;
        
        if (!plugin.hasWorldPermission(player, player.getWorld()))
            return;
        
        double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(player);
        
        ItemStack stack = recipe.getResult();
        plugin.getJobsManager().getJobsPlayer(player.getName()).crafted(stack, multiplier);
    }
    
    @EventHandler(priority=EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        // Entity that died must be living
        if(!(event.getEntity() instanceof LivingEntity))
            return;
        LivingEntity lVictim = (LivingEntity)event.getEntity();
        
        // mob spawner, no payment or experience
        if(mobSpawnerCreatures.remove(lVictim))
            return;
        
        // make sure plugin is enabled
        if(!plugin.isEnabled())
            return;
        
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event.getEntity().getLastDamageCause();
            Player pDamager = null;
            if(e.getDamager() instanceof Player) {
                pDamager = (Player)e.getDamager();
            } else if(e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player) {
                pDamager = (Player)((Projectile)e.getDamager()).getShooter();
            } else if(e.getDamager() instanceof Tameable) {
                Tameable t = (Tameable) e.getDamager();
                if (t.isTamed() && t.getOwner() instanceof Player) {
                    pDamager = (Player) t.getOwner();
                }
            }
            if(pDamager != null) {
                // check if in creative
                if (pDamager.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
                    return;
                // restricted area multiplier
                double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(pDamager);
                // pay
                JobsPlayer jDamager = plugin.getJobsManager().getJobsPlayer(pDamager.getName());
                jDamager.killed(lVictim.getClass().toString().replace("class ", "").trim(), multiplier);
                // pay for jobs
                if(lVictim instanceof Player){
                    JobsPlayer jVictim = plugin.getJobsManager().getJobsPlayer(((Player)lVictim).getName());
                    if(jVictim!=null && jVictim.getJobs()!= null){
                        for(Job temp: jVictim.getJobs()){
                            jDamager.killed((lVictim.getClass().toString().replace("class ", "")+":"+temp.getName()).trim(), multiplier);
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(!(event.getEntity() instanceof LivingEntity))
            return;
        if(!event.getSpawnReason().equals(SpawnReason.SPAWNER))
            return;
        if(plugin.getJobsConfiguration().payNearSpawner())
            return;
        LivingEntity creature = (LivingEntity)event.getEntity();
        mobSpawnerCreatures.add(creature);
    }
}
