package me.zford.jobs.tasks;

import me.zford.jobs.bukkit.PlayerManager;

public class DatabaseSaveTask implements Runnable {
    
    private PlayerManager manager;
    
    public DatabaseSaveTask(PlayerManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        manager.saveAll();
    }
}
