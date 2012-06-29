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
            while (!queue.isEmpty()) {
                JobsLogin login = queue.remove();
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
            try {
                sleep(10);
            } catch (InterruptedException e) {
                this.running = false;
                continue;
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
