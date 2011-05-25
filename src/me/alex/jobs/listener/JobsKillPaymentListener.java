package me.alex.jobs.listener;

import java.util.List;

import me.alex.jobs.Jobs;
import me.alex.jobs.config.JobsConfiguration;
import me.alex.jobs.config.container.Job;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

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
	 * Function that gets called whenever an entity gets damaged
	 * 
	 * Must make sure that the entity is getting damaged by another entity first
	 * and that this damager is either a player or a player's wolf.
	 * 
	 * Then it checks to see if the entity is actually dead and if it is to pay the killer
	 */
	public void onEntityDamage(EntityDamageEvent event){
		if(!event.isCancelled()){
			if(event instanceof EntityDamageByEntityEvent){
				EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent)event;
				// check if the damager is a wolf or a player
				if((damageEvent.getDamager() instanceof Player || damageEvent.getDamager() instanceof Wolf) && 
						damageEvent.getEntity() instanceof LivingEntity){
					// if it's a wolf, make sure it's tamed
					if(damageEvent.getDamager() instanceof Wolf){
						if(!((Wolf)damageEvent.getDamager()).isTamed()){
							return;
						}
					}
					// figure out who to pay (if a wolf is killing or a player is)
					Player payee;
					if(damageEvent.getDamager() instanceof Wolf){
						payee = (Player)((Wolf)damageEvent.getDamager()).getOwner();
					}
					else{
						payee = (Player)damageEvent.getDamager();
					}
					if(payee == null){
						return;
					}
					if((JobsConfiguration.getInstance().getPermissions() == null || !JobsConfiguration.getInstance().getPermissions().isEnabled())
							|| JobsConfiguration.getInstance().getPermissions().getHandler().has(payee, "jobs.world." + payee.getWorld().getName())){
						// does the user have permision?
						LivingEntity victim = (LivingEntity)damageEvent.getEntity();
						// check if the victim is dead already
						if(!victim.isDead() && victim.getHealth() > 0){
							if(victim.getNoDamageTicks() < (victim.getMaximumNoDamageTicks()/2.0) ||
									((victim.getNoDamageTicks() > (victim.getMaximumNoDamageTicks() / 2.0)) && victim.getLastDamage() < event.getDamage())){
								int damage = event.getDamage();
								if((victim.getNoDamageTicks() < (victim.getMaximumNoDamageTicks() / 2.0)) && victim.getLastDamage() < event.getDamage()){
									damage -= victim.getLastDamage();
								}
								// has it been dealt lethal damage?
								if(victim.getHealth() - damage <= 0){
									// yes
									// is anyone close to a mob spawner?
									List<Entity> damagerSurround = damageEvent.getDamager().getNearbyEntities(5, 5, 5);
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
									
									// not close to a mob spawner
	
									// pay
									plugin.getPlayerJobInfo(payee).killed(victim.getClass().toString().replace("class ", "").trim());
									// pay for jobs
									if(victim instanceof Player){
										for(Job temp: plugin.getPlayerJobInfo((Player)victim).getJobs()){
											plugin.getPlayerJobInfo(payee).killed((victim.getClass().toString().replace("class ", "")+":"+temp.getName()).trim());
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
}
