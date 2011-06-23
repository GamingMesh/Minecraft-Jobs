package com.zford.jobs.economy;

import org.bukkit.entity.Player;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class JobsEssentialsLink implements JobsEconomyLink{
	
	public JobsEssentialsLink(){
	}
	
	@Override
	public void pay(Player player, double amount) {
		// TODO
		try {
			Economy.add(player.getName(), amount);
		} catch (UserDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoLoanPermittedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateStats(Player player) {
		// TODO Auto-generated method stub
		
	}

}
