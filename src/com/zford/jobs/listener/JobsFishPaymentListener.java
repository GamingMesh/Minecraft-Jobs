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

import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerListener;

import com.zford.jobs.Jobs;

/**
 * Listener for fishing classes
 * 
 * @author Zak Ford <zak.j.ford@gmail.com>
 */

public class JobsFishPaymentListener extends PlayerListener {
    // hook to the main plugin
    private Jobs plugin;
    
    public JobsFishPaymentListener(Jobs plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerFish(PlayerFishEvent event) {
        // make sure plugin is enabled
        if(!plugin.isEnabled()) return;
        if(event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH) && event.getCaught() instanceof Item) {
            plugin.getJobsPlayer(event.getPlayer().getName()).fished((Item)event.getCaught());
        }
    }
}
