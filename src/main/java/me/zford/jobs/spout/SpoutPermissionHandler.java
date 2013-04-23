package me.zford.jobs.spout;

import org.spout.api.Spout;
import org.spout.api.geo.World;
import org.spout.api.permissions.DefaultPermissions;

import me.zford.jobs.Jobs;
import me.zford.jobs.PermissionHandler;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobsPlayer;

public class SpoutPermissionHandler implements PermissionHandler {

    @Override
    public void recalculatePermissions(JobsPlayer jPlayer) {
        // not implemented
    }

    @Override
    public void registerPermissions() {
        DefaultPermissions perms = Spout.getEngine().getDefaultPermissions();
        for (World world : Spout.getEngine().getWorlds()) {
            perms.addDefaultPermission("jobs.world."+world.getName().toLowerCase());
        }
        for (Job job : Jobs.getJobs()) {
            perms.addDefaultPermission("jobs.join."+job.getName().toLowerCase());
        }
    }

}
