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
import me.zford.jobs.bukkit.commands.BukkitConsole;
import me.zford.jobs.commands.CommandSender;

public class BukkitUtil {
    public static Player wrapPlayer(org.bukkit.entity.Player player) {
        return new BukkitPlayer(player);
    }
    
    public static CommandSender wrapCommandSender(org.bukkit.command.CommandSender sender) {
        if (sender instanceof org.bukkit.entity.Player) {
            return wrapPlayer((org.bukkit.entity.Player) sender);
        } else if (sender instanceof org.bukkit.command.ConsoleCommandSender) {
            return new BukkitConsole((org.bukkit.command.ConsoleCommandSender) sender);
        }
        
        return null;
    }
    
    public static Location wrapLocation(org.bukkit.Location loc) {
        return new Location(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
}
