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

public class SpoutPlayer extends Player {
    private org.spout.api.entity.Player player;
    public SpoutPlayer(org.spout.api.entity.Player player) {
        this.player = player;
    }
    
    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }
    
    @Override
    public void sendMessage(String[] messages) {
        player.sendMessage((Object[]) messages);
    }
    
    @Override
    public boolean hasPermission(String name) {
        return player.hasPermission(name);
    }
    
    @Override
    public String getName() {
        return player.getName();
    }
    
    @Override
    public Location getLocation() {
        return SpoutUtil.wrapLocation(player.getScene().getPosition(), player.getScene().getRotation());
    }
    
    @Override
    public void giveExp(int amount) {
        throw new UnsupportedOperationException();
    }
}
