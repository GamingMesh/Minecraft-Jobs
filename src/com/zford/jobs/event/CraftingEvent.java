package com.zford.jobs.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;


public class CraftingEvent extends Event{

	protected CraftingEvent(Player player, ItemStack result) {
		
		super("CraftingEvent");
		// TODO Auto-generated constructor stub
	}

}
