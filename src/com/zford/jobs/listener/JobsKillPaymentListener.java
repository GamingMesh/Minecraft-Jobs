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

package com.zford.jobs.listener;


import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.RestrictedArea;

/**
 * Plugin that monitors when things get killed by you and then pays you
 * 
 * @author Alex
 *
 */
public class JobsKillPaymentListener extends EntityListener{
	private Jobs plugin;
	
	public JobsKillPaymentListener(Jobs plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Function that gets called whenever an entity gets killed
	 * 
	 * Must make sure that the entity is getting damaged by another entity first
	 * and that this damager is either a player or a player's wolf.
	 * 
	 * Then it pays the killer
	 */
	public void onEntityDeath(EntityDeathEvent event)
	{
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
	    if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent 
	    		|| event.getEntity().getLastDamageCause() instanceof EntityDamageByProjectileEvent){
	        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event.getEntity().getLastDamageCause();
	        if((e.getDamager() instanceof Wolf && ((Wolf)e.getDamager()).isTamed() == true && ((Wolf)e.getDamager()).getOwner() != null) || e.getDamager() instanceof Player)
	        {
	        	Player damager;
	        	LivingEntity victim = (LivingEntity)e.getEntity();
	        	if(e.getDamager() instanceof Wolf && ((Wolf)e.getDamager()).isTamed() == true){
	        		damager = (Player)((Wolf)e.getDamager()).getOwner();
	        	}
	        	else {
	        		damager = (Player)e.getDamager();
	        	}
                // if spawned from a mob spawner
	        	if (plugin.mobSpawned.contains(victim)) return;

                // inside restricted area, no payment or experience
	        	if (RestrictedArea.isRestricted(damager) || RestrictedArea.isRestricted(victim)) return;
	        	
	        	// pay
				plugin.getPlayerJobInfo(damager).killed(victim.getClass().toString().replace("class ", "").trim());
				// pay for jobs
				if(victim instanceof Player){
					if(plugin.getPlayerJobInfo((Player)victim)!=null && plugin.getPlayerJobInfo((Player)victim).getJobs()!= null){
						for(Job temp: plugin.getPlayerJobInfo((Player)victim).getJobs()){
							plugin.getPlayerJobInfo(damager).killed((victim.getClass().toString().replace("class ", "")+":"+temp.getName()).trim());
						}
					}
				}
	        }
	    }
	    if(plugin.mobSpawned.contains(event.getEntity()))
	    	plugin.mobSpawned.remove(event.getEntity());
	}
	
	//Keep track of what spawned from mob spawners
	public void onCreatureSpawn(CreatureSpawnEvent event) 
	{
		if(event.getSpawnReason() == SpawnReason.SPAWNER)
			plugin.mobSpawned.add(event.getEntity());
	}
}
