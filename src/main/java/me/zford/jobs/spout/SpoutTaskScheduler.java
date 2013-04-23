package me.zford.jobs.spout;

import org.spout.api.Spout;
import org.spout.api.scheduler.TaskPriority;

import me.zford.jobs.TaskScheduler;

public class SpoutTaskScheduler implements TaskScheduler {
    private JobsPlugin plugin;
    public SpoutTaskScheduler(JobsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void scheduleTask(Runnable task) {
        Spout.getScheduler().scheduleSyncDelayedTask(plugin, task);
    }

    @Override
    public void scheduleTask(Runnable task, long delayTicks) {
        // nothing we do with the scheduler is critical
        // change this if this becomes an issue down the road
        Spout.getScheduler().scheduleSyncDelayedTask(plugin, task, delayTicks * 50, TaskPriority.LOWEST);
    }

    @Override
    public void scheduleTask(Runnable task, long delayTicks, long periodTicks) {
        // nothing we do with the scheduler is critical
        // change this if this becomes an issue down the road
        Spout.getScheduler().scheduleSyncRepeatingTask(plugin, task, delayTicks * 50, periodTicks * 50, TaskPriority.LOWEST);
    }

}
