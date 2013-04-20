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

package me.zford.jobs.spout;

import me.zford.jobs.Location;
import me.zford.jobs.Player;
import me.zford.jobs.commands.CommandSender;
import me.zford.jobs.spout.commands.SpoutConsole;

public class SpoutUtil {
    public static Player wrapPlayer(org.spout.api.entity.Player player) {
        return new SpoutPlayer(player);
    }
    
    public static CommandSender wrapCommandSource(org.spout.api.command.CommandSource source) {
        if (source instanceof org.spout.api.entity.Player) {
            return wrapPlayer((org.spout.api.entity.Player) source);
        }
        return new SpoutConsole(source);
    }
    
    public static Location wrapLocation(org.spout.api.geo.discrete.Point point, org.spout.api.math.Quaternion quat) {
        return new Location(point.getWorld().getName(), point.getX(), point.getY(), point.getZ(), quat.getYaw(), quat.getPitch());
    }
}
