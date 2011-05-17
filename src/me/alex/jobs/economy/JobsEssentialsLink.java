package me.alex.jobs.economy;

import org.bukkit.entity.Player;

import com.earth2me.essentials.Essentials;

public class JobsEssentialsLink implements JobsEconomyLink{
	private Essentials economy;
	
	public JobsEssentialsLink(Essentials economy){
		this.economy = economy;
	}
	
	@Override
	public void pay(Player player, double ammount) {
		// TODO
	}

	@Override
	public void updateStats(Player player) {
		// TODO Auto-generated method stub
		
	}

}
