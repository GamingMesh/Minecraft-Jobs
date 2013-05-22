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

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import me.zford.jobs.Player;
import me.zford.jobs.Server;

public class BukkitServer implements Server {
    @Override
    public Player getPlayer(String name) {
        org.bukkit.entity.Player player = Bukkit.getServer().getPlayer(name);
        if (player == null)
            return null;
        return BukkitUtil.wrapPlayer(player);
    }
    
    @Override
    public Player getPlayerExact(String name) {
        org.bukkit.entity.Player player = Bukkit.getServer().getPlayerExact(name);
        if (player == null)
            return null;
        return BukkitUtil.wrapPlayer(player);
    }
    
    @Override
    public Player[] getOnlinePlayers() {
        org.bukkit.entity.Player[] players = Bukkit.getServer().getOnlinePlayers();
        Player[] copy = new Player[players.length];
        for (int i=0; i < players.length; i++) {
            copy[i] = BukkitUtil.wrapPlayer(players[i]);
        }
        return copy;
    }

    @Override
    public Logger getLogger() {
        return Bukkit.getServer().getLogger();
    }
    
    @Override
    public void broadcastMessage(String message) {
        Bukkit.getServer().broadcastMessage(message);
    }
}
