package me.alex.jobs;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import com.iConomy.*;
import com.iConomy.system.Holdings;

public class JobsEntityListener extends EntityListener{
	private Jobs plugin;
	
	public JobsEntityListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onEntityDamage(EntityDamageEvent event){
		if(event instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent event2 = (EntityDamageByEntityEvent) event;
			if(!event.isCancelled()){
				if(event2.getDamager() instanceof Player && event2.getEntity() instanceof LivingEntity){
					Player damager = (Player)event2.getDamager();
					LivingEntity victim = (LivingEntity)event2.getEntity();
					// check if the victim is already dead.
					if(!victim.isDead() && victim.getHealth() > 0){
						// check if the damage would be dealt (noDamageTicks)
						if(victim.getNoDamageTicks() < (victim.getMaximumNoDamageTicks()/2.0) ||
								((victim.getNoDamageTicks() > (victim.getMaximumNoDamageTicks() / 2.0)) && victim.getLastDamage() < event.getDamage())){
							int damage = event.getDamage();
							if((victim.getNoDamageTicks() < (victim.getMaximumNoDamageTicks() / 2.0)) && victim.getLastDamage() < event.getDamage()){
								damage -= victim.getLastDamage();
							}
							
							if(victim.getHealth() - damage < 0){
								// entity has been killed by a player
								// check if near a mob spawner
								List<Entity> damagerSurround = damager.getNearbyEntities(5, 5, 5);
								List<Entity> victimSurround = victim.getNearbyEntities(5, 5, 5);
								for(Entity temp: damagerSurround){
									if (temp instanceof Block){
										if(((Block)temp).getType() == Material.MOB_SPAWNER){
											return;
										}
									}
								}
								for(Entity temp: victimSurround){
									if (temp instanceof Block){
										if(((Block)temp).getType() == Material.MOB_SPAWNER){
											return;
										}
									}
								}
								
								if(plugin.getJob(damager) != null){
									double income = plugin.getJob(damager).getKillIncome(victim.getClass());
									if(plugin.getiConomy() != null){
										Holdings account = iConomy.getAccount(damager.getName()).getHoldings();
										account.add(income);
									}
									else if(plugin.getBOSEconomy() != null){
										plugin.getBOSEconomy().addPlayerMoney(damager.getName(), (int)income, false);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
