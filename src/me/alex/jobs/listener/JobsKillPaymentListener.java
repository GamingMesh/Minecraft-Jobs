package me.alex.jobs.listener;

import me.alex.jobs.Jobs;
import me.alex.jobs.config.JobsConfiguration;
import me.alex.jobs.config.container.Job;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
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
		// if event is not cancelled and entity isn't already dead.
		if(!event.isCancelled() && !event.getEntity().isDead()){
			Player damager;
			LivingEntity victim;
			if(event instanceof EntityDamageByEntityEvent){
				System.out.println(((EntityDamageByEntityEvent)event).getDamager().getClass());
			}
			if(event instanceof EntityDamageByProjectileEvent){
				EntityDamageByProjectileEvent damageEvent = (EntityDamageByProjectileEvent)event;
				if(damageEvent.getDamager() instanceof Player){
					damager = (Player) damageEvent.getDamager();
				}
				else{
					// hasn't been killed by a player shooting an arrow/snowball
					return;
				}
			}
			else if (event instanceof EntityDamageByEntityEvent){
				System.out.println("enity by entity event");
				EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
				System.out.println(damageEvent.getDamager().getClass());

				if(damageEvent.getDamager() instanceof Player){
					damager = (Player) damageEvent.getDamager();
				}
				else if (damageEvent.getDamager() instanceof Wolf){
					System.out.println(damageEvent.getDamager().getClass());
					// wolf has an owner
					if(((Wolf)damageEvent.getDamager()).getOwner() != null && 
							((Wolf)damageEvent.getDamager()).getOwner() instanceof Player){
						System.out.println("is wolf and owned by player");
						damager = (Player)((Wolf)damageEvent.getDamager()).getOwner();
						System.out.println(damager.getName());
					}
					else {
						// wild wolf, don't care
						System.out.println("wild");
						return;
					}
				}
				else {
					// don't care
					return;
				}
			}
			else {
				// don't care
				return;
			}
						
			if(event.getEntity() instanceof LivingEntity){
				victim = (LivingEntity)event.getEntity();
			}
			else{
				// not interested
				return;
			}
			
			if(victim.getHealth() <= 0){
				// for all intensive purposes dead.
				return;
			}
			
			// if they have permissions
			if((JobsConfiguration.getInstance().getPermissions() == null || !JobsConfiguration.getInstance().getPermissions().isEnabled())
					|| JobsConfiguration.getInstance().getPermissions().getHandler().has(damager, "jobs.world." + damager.getWorld().getName())){
				// if victim would die
				if(victim.getHealth() - event.getDamage() > 0){
					// not dealth lethal damage
					return;
				}
				// if near mob spawners.
				if (nearMobSpawner(damager) ||	nearMobSpawner(victim)){
					// near mob spawner, no payment or experience
					return;
				}
				// pay
				plugin.getPlayerJobInfo(damager).killed(victim.getClass().toString().replace("class ", "").trim());
				// pay for jobs
				if(victim instanceof Player){
					for(Job temp: plugin.getPlayerJobInfo((Player)victim).getJobs()){
						plugin.getPlayerJobInfo(damager).killed((victim.getClass().toString().replace("class ", "")+":"+temp.getName()).trim());
					}
				}
			}
		}
	}
	
	/**
	 * Function to check whether an entity is near a mob spawner
	 * @param entity - the entity to be checked
	 * @return true - near a mob spawner
	 * @return false - not near a mob spawner
	 */
	private boolean nearMobSpawner(LivingEntity entity){
		int x = entity.getLocation().getBlockX();
		int y = entity.getLocation().getBlockY();
		int z = entity.getLocation().getBlockZ();
		for(int a=0; a< 10; ++a){
			for(int b=0; b< 10; ++b){
				for(int c=0; c< 10; ++c){
					if((entity.getWorld().getBlockAt(x-a,y-b,z-c).getTypeId() == 52)||
							(entity.getWorld().getBlockAt(x+a,y+b,z+c).getTypeId() == 52)){
						return true;
					}
				}
			}
		}
		return false;
	}
}
