package me.zford.jobs;

import java.util.HashMap;
import java.util.List;

import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobProgression;
import me.zford.jobs.config.container.JobsPlayer;

import org.bukkit.block.Block;
import org.bukkit.entity.Item;
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
        HashMap<String, Double> param = new HashMap<String, Double>();
        List<JobProgression> progression = jPlayer.getJobProgression();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double) progression.size());
        // no job
        if (progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getBreakIncome(block, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                }
                param.remove("joblevel");
            }
        } else {
            for (JobProgression prog : progression) {
                // add the current level to the parameter list
                param.put("joblevel", (double) prog.getLevel());
                // get the income and give it
                Double income = prog.getJob().getBreakIncome(block, param);
                if(income != null) {
                    Double exp = prog.getJob().getBreakExp(block, param);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
                param.remove("joblevel");
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
        HashMap<String, Double> param = new HashMap<String, Double>();
        List<JobProgression> progression = jPlayer.getJobProgression();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double) progression.size());
        // no job
        if (progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getPlaceIncome(block, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                }
                param.remove("joblevel");
            }
        } else {
            for (JobProgression prog : progression) {
                // add the current level to the parameter list
                param.put("joblevel", (double) prog.getLevel());
                // get the income and give it
                Double income = prog.getJob().getPlaceIncome(block, param);
                if(income != null) {
                    Double exp = prog.getJob().getPlaceExp(block, param);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
                param.remove("joblevel");
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
    public void killed(JobsPlayer jPlayer, String victim, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        List<JobProgression> progression = jPlayer.getJobProgression();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double) progression.size());
        // no job
        if (progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getKillIncome(victim, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                }
                param.remove("joblevel");
            }
        } else {
            for (JobProgression prog : progression) {
                // add the current level to the parameter list
                param.put("joblevel", (double) prog.getLevel());
                // get the income and give it
                Double income = prog.getJob().getKillIncome(victim, param);
                if(income != null) {
                    Double exp = prog.getJob().getKillExp(victim, param);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
                param.remove("joblevel");
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
    public void fished(JobsPlayer jPlayer, Item item, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        List<JobProgression> progression = jPlayer.getJobProgression();
        param.put("numjobs", (double) progression.size());
        // no job
        if (progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getFishIncome(item, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                }
                param.remove("joblevel");
            }
        } else {
            for (JobProgression prog : progression) {
                // add the current level to the parameter list
                param.put("joblevel", (double) prog.getLevel());
                // get the income and give it
                Double income = prog.getJob().getFishIncome(item, param);
                if(income != null) {
                    Double exp = prog.getJob().getFishExp(item, param);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
                param.remove("joblevel");
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
        HashMap<String, Double> param = new HashMap<String, Double>();
        List<JobProgression> progression = jPlayer.getJobProgression();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double) progression.size());
        // no job
        if (progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getCraftIncome(items, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                }
                param.remove("joblevel");
            }
        } else {
            for (JobProgression prog : progression) {
                // add the current level to the parameter list
                param.put("joblevel", (double) prog.getLevel());
                // get the income and give it
                Double income = prog.getJob().getCraftIncome(items, param);
                if(income != null) {
                    Double exp = prog.getJob().getCraftExp(items, param);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
                param.remove("joblevel");
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
        HashMap<String, Double> param = new HashMap<String, Double>();
        List<JobProgression> progression = jPlayer.getJobProgression();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double) progression.size());
        // no job
        if (progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getCraftIncome(items, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                }
                param.remove("joblevel");
            }
        } else {
            for (JobProgression prog : progression) {
                // add the current level to the parameter list
                param.put("joblevel", (double) prog.getLevel());
                // get the income and give it
                Double income = prog.getJob().getSmeltIncome(items, param);
                if(income != null) {
                    Double exp = prog.getJob().getSmeltExp(items, param);
                    // give income
                    plugin.getEconomy().pay(jPlayer, income*multiplier);
                    if (prog.addExperience(exp*multiplier))
                        plugin.getPlayerManager().performLevelUp(jPlayer, prog.getJob());
                }
                param.remove("joblevel");
            }
        }
    }
}
