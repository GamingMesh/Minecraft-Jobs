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

package me.zford.jobs.spout.commands;

import org.spout.api.chat.ChatArguments;
import org.spout.api.command.CommandSource;

import me.zford.jobs.commands.CommandSender;

public class SpoutConsole implements CommandSender {
    private CommandSource source;
    public SpoutConsole(CommandSource source) {
        this.source = source;
    }
    
    @Override
    public void sendMessage(String message) {
        source.sendMessage(ChatArguments.fromString(message));
    }
    
    @Override
    public void sendMessage(String[] messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }
    
    @Override
    public boolean hasPermission(String name) {
        return source.hasPermission(name);
    }
}
