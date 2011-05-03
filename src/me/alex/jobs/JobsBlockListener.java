package me.alex.jobs;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.iConomy.iConomy;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

public class JobsBlockListener extends BlockListener{
	public Jobs plugin;
	public JobsBlockListener(Jobs plugin){
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		if(!event.isCancelled()){
			Block block = event.getBlock();
			Player player = event.getPlayer();
			if(plugin.getJob(player) != null){
				double income = plugin.getJob(player).getBreakIncome(block);	
				if(plugin.getiConomy() != null){
					Holdings account = iConomy.getAccount(player.getName()).getHoldings();
					account.add(income);
				}
				else if(plugin.getBOSEconomy() != null){
					plugin.getBOSEconomy().addPlayerMoney(player.getName(), (int)income, false);
				}
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event){
		if(event.canBuild() && !event.isCancelled()){
			Block block = event.getBlock();
			Player player = event.getPlayer();
			if(plugin.getJob(player) != null){
				double income = plugin.getJob(player).getPlaceIncome(block);
				if(plugin.getiConomy() != null){
					Holdings account = iConomy.getAccount(player.getName()).getHoldings();
					account.add(income);
				}
				else if(plugin.getBOSEconomy() != null){
					plugin.getBOSEconomy().addPlayerMoney(player.getName(), (int)income, false);
				}
			}
		}
	}
}
