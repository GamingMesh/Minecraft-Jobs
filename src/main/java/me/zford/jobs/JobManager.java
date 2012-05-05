package me.zford.jobs;

import java.util.List;

import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobProgression;
import me.zford.jobs.config.container.JobsPlayer;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class JobManager {
    
    private Jobs plugin;
    
    public JobManager(Jobs plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Broke a block.
     * 
     * Give correct experience and income
     * @param jPlayer - the player
     * @param block - the block broken
     * @param multiplier - the payment/xp multiplier
     */
    public void broke(JobsPlayer jPlayer, Block block, double multiplier) {
        List<JobProgression> progression = jPlayer.getJobProgression();
        int numjobs = progression.size();
        // no job
        if (numjobs == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if (jobNone != null) {
                Double income = jobNone.getBreakIncome(block, 1, numjobs);
                if (income != null)
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
            }
        } else {
            for (JobProgression prog : progression) {
                int level = prog.getLevel();
                Double income = prog.getJob().getBreakIncome(block, level, numjobs);
                if (income != null) {
                    Double exp = prog.getJob().getBreakExp(block, level, numjobs);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
            }
        }
    }
    
    /**
     * Placed a block.
     * 
     * Give correct experience and income
     * @param jPlayer - the player
     * @param block - the block placed
     * @param multiplier - the payment/xp multiplier
     */
    public void placed(JobsPlayer jPlayer, Block block, double multiplier) {
        List<JobProgression> progression = jPlayer.getJobProgression();
        int numjobs = progression.size();
        // no job
        if (numjobs == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if (jobNone != null) {
                Double income = jobNone.getPlaceIncome(block, 1, numjobs);
                if (income != null)
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
            }
        } else {
            for (JobProgression prog : progression) {
                int level = prog.getLevel();
                Double income = prog.getJob().getPlaceIncome(block, level, numjobs);
                if (income != null) {
                    Double exp = prog.getJob().getPlaceExp(block, level, numjobs);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
            }
        }
    }
    
    /**
     * Killed a living entity or owned wolf killed living entity.
     * 
     * Give correct experience and income
     * @param jPlayer - the player
     * @param victim - the mob killed
     * @param multiplier - the payment/xp multiplier
     */
    public void killed(JobsPlayer jPlayer, EntityType type, double multiplier) {
        List<JobProgression> progression = jPlayer.getJobProgression();
        int numjobs = progression.size();
        // no job
        if (numjobs == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if (jobNone != null) {
                Double income = jobNone.getKillIncome(type, 1, numjobs);
                if (income != null)
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
            }
        } else {
            for (JobProgression prog : progression) {
                int level = prog.getLevel();
                Double income = prog.getJob().getKillIncome(type, level, numjobs);
                if (income != null) {
                    Double exp = prog.getJob().getKillExp(type, level, numjobs);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
            }
        }
    }
    
    /**
     * Fished an item
     * 
     * Give correct experience and income
     * @param jPlayer - the player
     * @param item - the item fished
     * @param multiplier - the payment/xp multiplier
     */
    public void fished(JobsPlayer jPlayer, ItemStack items, double multiplier) {
        List<JobProgression> progression = jPlayer.getJobProgression();
        int numjobs = progression.size();
        // no job
        if (numjobs == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if (jobNone != null) {
                Double income = jobNone.getFishIncome(items, 1, numjobs);
                if (income != null)
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
            }
        } else {
            for (JobProgression prog : progression) {
                int level = prog.getLevel();
                Double income = prog.getJob().getFishIncome(items, level, numjobs);
                if (income != null) {
                    Double exp = prog.getJob().getFishExp(items, level, numjobs);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
            }
        }
    }
    
    /**
     * crafted an item.
     * 
     * Give correct experience and income
     * @param jPlayer - the player
     * @param items - the items crafted
     * @param multipler - the payment/xp multiplier
     */
    public void crafted(JobsPlayer jPlayer, ItemStack items, double multiplier) {
        List<JobProgression> progression = jPlayer.getJobProgression();
        int numjobs = progression.size();
        // no job
        if (numjobs == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if (jobNone != null) {
                Double income = jobNone.getCraftIncome(items, 1, numjobs);
                if (income != null)
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
            }
        } else {
            for (JobProgression prog : progression) {
                int level = prog.getLevel();
                Double income = prog.getJob().getCraftIncome(items, level, numjobs);
                if (income != null) {
                    Double exp = prog.getJob().getCraftExp(items, level, numjobs);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
            }
        }
    }
    
    /**
     * smelted an item.
     * 
     * Give correct experience and income
     * @param jPlayer - the player
     * @param items - the items smelted
     * @param multipler - the payment/xp multiplier
     */
    public void smelted(JobsPlayer jPlayer, ItemStack items, double multiplier) {
        List<JobProgression> progression = jPlayer.getJobProgression();
        int numjobs = progression.size();
        // no job
        if (numjobs == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                Double income = jobNone.getCraftIncome(items, 1, numjobs);
                if (income != null)
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
            }
        } else {
            for (JobProgression prog : progression) {
                int level = prog.getLevel();
                Double income = prog.getJob().getSmeltIncome(items, level, numjobs);
                if (income != null) {
                    Double exp = prog.getJob().getSmeltExp(items, level, numjobs);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
            }
        }
    }
}
