package me.zford.jobs.tasks;

import me.zford.jobs.bukkit.JobsPlugin;

public class DatabaseSaveTask extends Thread {
    
    private JobsPlugin plugin;
    
    private volatile boolean running = true;
    private int sleep;
    
    public DatabaseSaveTask(JobsPlugin plugin, int duration) {
        super("Jobs-DatabaseSaveTask");
        this.plugin = plugin;
        this.sleep = duration * 60000;
    }

    @Override
    public void run() {
        plugin.getLogger().info("Started database save task");
        while (running) {
            try {
                sleep(sleep);
            } catch (InterruptedException e) {
                this.running = false;
                continue;
            }
            try {
                plugin.getPlayerManager().saveAll();
            } catch (Throwable t) {
                t.printStackTrace();
                plugin.getLogger().severe("Exception in DatabaseSaveTask, stopping auto save!");
                running = false;
            }
        }
        plugin.getLogger().info("Database save task shutdown");
        
    }
    
    public void shutdown() {
        this.running = false;
        interrupt();
    }
}
