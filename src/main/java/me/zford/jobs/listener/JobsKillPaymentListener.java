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
import java.util.Set;
import java.util.WeakHashMap;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobsPlayer;

import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;


/**
 * Plugin that monitors when things get killed by you and then pays you
 * 
 * @author Alex
 *
 */
public class JobsKillPaymentListener implements Listener {
	private Jobs plugin;
	private Set<LivingEntity> mobSpawnerCreatures;
	
	public JobsKillPaymentListener(Jobs plugin) {
		this.plugin = plugin;
		this.mobSpawnerCreatures = Collections.newSetFromMap(new WeakHashMap<LivingEntity, Boolean>());
	}
	
	/**
	 * Function that gets called whenever an entity gets killed
	 * 
	 * Must make sure that the entity is getting damaged by another entity first
	 * and that this damager is either a player or a player's wolf.
	 * 
	 * Then it pays the killer
	 */
    @EventHandler(priority=EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event)
	{
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
            } else if(e.getDamager() instanceof Wolf && ((Wolf)e.getDamager()).isTamed() == true && ((Wolf)e.getDamager()).getOwner() instanceof Player) {
                pDamager = (Player)((Wolf)e.getDamager()).getOwner();
            }
            if(pDamager != null) {
                // check if in creative
                if (pDamager.getGameMode().equals(GameMode.CREATIVE) && !plugin.getJobsConfiguration().payInCreative())
                    return;
                // restricted area multiplier
                double multiplier = plugin.getJobsConfiguration().getRestrictedMultiplier(pDamager);
                // pay
                JobsPlayer jDamager = plugin.getJobsPlayer(pDamager.getName());
                jDamager.killed(lVictim.getClass().toString().replace("class ", "").trim(), multiplier);
                // pay for jobs
                if(lVictim instanceof Player){
                    JobsPlayer jVictim = plugin.getJobsPlayer(((Player)lVictim).getName());
                    if(jVictim!=null && jVictim.getJobs()!= null){
                        for(Job temp: jVictim.getJobs()){
                            jDamager.killed((lVictim.getClass().toString().replace("class ", "")+":"+temp.getName()).trim(), multiplier);
                        }
                    }
                }
            }
        }
    }
	
	/**
	 * Track creatures that are spawned from a mob spawner
	 * 
	 * These creatures shouldn't payout if the configuration is set
	 */
    @EventHandler(priority=EventPriority.MONITOR)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
	    if(event.isCancelled())
	        return;
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
