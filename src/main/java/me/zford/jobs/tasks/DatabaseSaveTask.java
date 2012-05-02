package me.zford.jobs.tasks;

import me.zford.jobs.JobsManager;

public class DatabaseSaveTask implements Runnable {
    
    private JobsManager manager;
    
    public DatabaseSaveTask(JobsManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        manager.saveAll();
    }
}
