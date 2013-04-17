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

import org.bukkit.Bukkit;

import me.zford.jobs.TaskScheduler;

public class BukkitTaskScheduler implements TaskScheduler {
    private JobsPlugin plugin;
    public BukkitTaskScheduler(JobsPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public void scheduleTask(Runnable task) {
        Bukkit.getServer().getScheduler().runTask(plugin, task);
    }
    @Override
    public void scheduleTask(Runnable task, long delayTicks) {
        Bukkit.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        
    }
    @Override
    public void scheduleTask(Runnable task, long delayTicks, long periodTicks) {
        Bukkit.getServer().getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
    }
}
