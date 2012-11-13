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

import java.util.concurrent.LinkedBlockingQueue;


public class PlayerLoginManager extends Thread {
    
    private JobsPlugin plugin;
    
    private LinkedBlockingQueue<JobsLogin> queue = new LinkedBlockingQueue<JobsLogin>();
    
    private volatile boolean running = true;
    
    public PlayerLoginManager(JobsPlugin plugin) {
        super("Jobs-PlayerLoginTask");
        this.plugin = plugin;
    }
    
    public void addPlayer(String playerName) {
        queue.add(new JobsLogin(playerName, LoginType.LOGIN));
    }
    
    public void removePlayer(String playerName) {
        queue.add(new JobsLogin(playerName, LoginType.LOGOUT));
    }
    
    @Override
    public void run() {
        plugin.getLogger().info("Login manager started");
        while (running) {
            JobsLogin login = null;
            try {
                login = queue.take();
            } catch (InterruptedException e) {}
            
            if (login == null)
                continue;
            
            try {
                if (login.getType().equals(LoginType.LOGIN)) {
                    plugin.getPlayerManager().addPlayer(login.getPlayer());
                } else if (login.getType().equals(LoginType.LOGOUT)) {
                    plugin.getPlayerManager().removePlayer(login.getPlayer());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        plugin.getLogger().info("Login manager shutdown");
    }
    
    public void shutdown() {
        this.running = false;
        interrupt();
    }
    
    public enum LoginType {
        LOGIN,
        LOGOUT,
    }
    
    public class JobsLogin {
        private String playerName;
        private LoginType type;
        public JobsLogin(String playerName, LoginType type) {
            this.playerName = playerName;
            this.type = type;
        }
        
        public String getPlayer() {
            return playerName;
        }
        
        public LoginType getType() {
            return type;
        }
    }
}
