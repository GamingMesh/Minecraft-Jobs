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

import me.zford.jobs.Jobs;
import me.zford.jobs.spout.commands.SpoutJobsCommands;
import me.zford.jobs.spout.listeners.JobsListener;

import org.spout.api.permissions.DefaultPermissions;
import org.spout.api.plugin.CommonPlugin;

public class JobsPlugin extends CommonPlugin {

    @Override
    public void onDisable() {
        Jobs.shutdown();

        Jobs.getPluginLogger().info("Plugin has been disabled succesfully.");
    }

    @Override
    public void onEnable() {
        Jobs.setPermissionHandler(new SpoutPermissionHandler());
        Jobs.setServer(new SpoutServer());
        Jobs.setScheduler(new SpoutTaskScheduler(this));
        
        Jobs.setPluginLogger(getLogger());
        
        Jobs.setDataFolder(getDataFolder());
        
        Jobs.startup();
        
        getEngine().getEventManager().registerEvents(new JobsListener(), this);
        getEngine().getRootCommand().addSubCommand(this, "jobs").setExecutor(new SpoutJobsCommands());
        
        registerPermissionDefaults();
        
        Jobs.getPluginLogger().info("Plugin has been enabled succesfully.");
    }
    
    private void registerPermissionDefaults() {
        DefaultPermissions perms = getEngine().getDefaultPermissions();
        perms.addDefaultPermission("jobs.use");
        perms.addDefaultPermission("jobs.command.browse");
        perms.addDefaultPermission("jobs.command.stats");
        perms.addDefaultPermission("jobs.command.join");
        perms.addDefaultPermission("jobs.command.leave");
        perms.addDefaultPermission("jobs.command.leaveall");
        perms.addDefaultPermission("jobs.command.info");
    }
}
