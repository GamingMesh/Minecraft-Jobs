/**
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011 Zak Ford <zak.j.ford@gmail.com>
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
 */

package me.zford.jobs.bukkit;

import me.zford.jobs.Location;
import me.zford.jobs.Player;

public class BukkitPlayer implements Player {
    private org.bukkit.entity.Player player;
    public BukkitPlayer(org.bukkit.entity.Player player) {
        this.player = player;
    }
    
    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }
    
    @Override
    public void sendMessage(String[] messages) {
        player.sendMessage(messages);
    }
    
    @Override
    public String getName() {
        return player.getName();
    }
    
    @Override
    public String getDisplayName() {
        return player.getDisplayName();
    }
    
    @Override
    public boolean hasPermission(String name) {
        return player.hasPermission(name);
    }

    @Override
    public Location getLocation() {
        return BukkitUtil.wrapLocation(player.getLocation());
    }

    @Override
    public void giveExp(int amount) {
        player.giveExp(amount);
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass())
            return false;
        
        BukkitPlayer other = (BukkitPlayer) obj;
        return this.player.equals(other.player);
    }
    
    @Override
    public int hashCode() {
        return player.hashCode();
    }
}
