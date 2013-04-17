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

package me.zford.jobs.bukkit.commands;

import org.bukkit.command.ConsoleCommandSender;

import me.zford.jobs.commands.CommandSender;

public class BukkitConsole implements CommandSender {
    private ConsoleCommandSender console;
    public BukkitConsole(ConsoleCommandSender sender) {
        this.console = sender;
    }
    @Override
    public void sendMessage(String message) {
        console.sendMessage(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        console.sendMessage(messages);
    }
    
    @Override
    public boolean hasPermission(String name) {
        return console.hasPermission(name);
    }

}
