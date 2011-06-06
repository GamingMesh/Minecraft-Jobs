package me.alex.jobs.economy;

import org.bukkit.entity.Player;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;

public class JobsEssentialsLink implements JobsEconomyLink{
	private Economy economy;
	
	public JobsEssentialsLink(Economy economy){
		this.economy = economy;
	}
	
	@Override
	public void pay(Player player, double ammount) {
		// TODO
		try {
			economy.add(player.getName(), ammount);
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
