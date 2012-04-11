/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
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
 * 
 */

package me.zford.jobs.config.container;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.zford.jobs.Jobs;
import me.zford.jobs.dao.JobsDAO;
import me.zford.jobs.dao.container.JobsDAOData;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

public class JobsPlayer {
    // jobs plugin
    private Jobs plugin;
    // the player the object belongs to
    private String playername;
    // progression of the player in each job
    private LinkedHashMap<Job, JobProgression> progression = new LinkedHashMap<Job, JobProgression>();
    // display honorific
    private String honorific;
    // permission attachment
    private PermissionAttachment attachment;
    // dao
    JobsDAO dao;
        
    /**
     * Constructor.
     * Reads data storage and configures itself.
     * @param plugin - the jobs plugin
     * @param playername - the player this represents
     * @param dao - the data access object
     */
    public JobsPlayer(Jobs plugin, String playername, JobsDAO dao) {
        this.plugin = plugin;
        this.playername = playername;
        this.dao = dao;
        // for all jobs players have
        List<JobsDAOData> list = dao.getAllJobs(this);
        if (list != null) {
            for (JobsDAOData jobdata: list) {
                if (plugin.getJobConfig().getJob(jobdata.getJobName()) != null) {
                    // add the job
                    Job job = plugin.getJobConfig().getJob(jobdata.getJobName());
                    if (job != null) {
                        // create the progression object
                        JobProgression jobProgression = new JobProgression(plugin, job, jobdata.getExperience(), jobdata.getLevel(), this);
                        // calculate the max level
                        
                        // add the progression level.
                        progression.put(job, jobProgression);
                    }
                }
            }
        }
        reloadMaxExperience();
        reloadHonorific();
        recalculatePermissions();
    }
    
    /**
     * Broke a block.
     * 
     * Give correct experience and income
     * 
     * @param block - the block broken
     * @param multiplier - the payment/xp multiplier
     */
    public void broke(Block block, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double)progression.size());
        for (Map.Entry<Job, JobProgression> entry: progression.entrySet()) {
            // add the current level to the parameter list
            param.put("joblevel", (double)entry.getValue().getLevel());
            // get the income and give it
            Double income = entry.getKey().getBreakIncome(block, param);
            if(income != null) {
                Double exp = entry.getKey().getBreakExp(block, param);
                // give income
                plugin.getEconomy().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();
            }
            param.remove("joblevel");
        }
        // no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getBreakIncome(block, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
    }
    
    /**
     * Placed a block.
     * 
     * Give correct experience and income
     * 
     * @param block - the block placed
     * @param multiplier - the payment/xp multiplier
     */
    public void placed(Block block, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double)progression.size());
        for (Map.Entry<Job, JobProgression> entry: progression.entrySet()) {
            // add the current level to the parameter list
            param.put("joblevel", (double)entry.getValue().getLevel());
            // get the income and give it
            Double income = entry.getKey().getPlaceIncome(block, param);
            if(income != null) {
                Double exp = entry.getKey().getPlaceExp(block, param);
                // give income
                plugin.getEconomy().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();
            }
            param.remove("joblevel");
        }
        // no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getPlaceIncome(block, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
    }
    
    /**
     * Killed a living entity or owned wolf killed living entity.
     * 
     * Give correct experience and income
     * 
     * @param victim - the mob killed
     * @param multiplier - the payment/xp multiplier
     */
    public void killed(String victim, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double)progression.size());
        for (Map.Entry<Job, JobProgression> entry: progression.entrySet()) {
            // add the current level to the parameter list
            param.put("joblevel", (double)entry.getValue().getLevel());
            // get the income and give it
            Double income = entry.getKey().getKillIncome(victim, param);
            if(income != null) {
                Double exp = entry.getKey().getKillExp(victim, param);
                // give income
                plugin.getEconomy().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();    
            }
            param.remove("joblevel");
        }
        // no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getKillIncome(victim, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
    }
    
    /**
     * Fished an item
     * 
     * Give correct experience and income
     * 
     * @param item - the item fished
     * @param multiplier - the payment/xp multiplier
     */
    public void fished(Item item, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        param.put("numjobs", (double)progression.size());
        for (Map.Entry<Job, JobProgression> entry: progression.entrySet()) {
            // add the current level to the parameter list
            param.put("joblevel", (double)entry.getValue().getLevel());
            // get the income and give it
            Double income = entry.getKey().getFishIncome(item, param);
            if(income != null) {
                Double exp = entry.getKey().getFishExp(item, param);
                // give income
                plugin.getEconomy().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();
            }
            param.remove("joblevel");
        }
        // no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getFishIncome(item, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
    }
    
    /**
     * crafted an item.
     * 
     * Give correct experience and income
     * 
     * @param items - the items crafted
     * @param multipler - the payment/xp multiplier
     */
    public void crafted(ItemStack items, double multiplier) {
        HashMap<String, Double> param = new HashMap<String, Double>();
        // add the number of jobs to the parameter list
        param.put("numjobs", (double)progression.size());
        for (Map.Entry<Job, JobProgression> entry: progression.entrySet()) {
            // add the current level to the parameter list
            param.put("joblevel", (double)entry.getValue().getLevel());
            // get the income and give it
            Double income = entry.getKey().getCraftIncome(items, param);
            if(income != null) {
                Double exp = entry.getKey().getCraftExp(items, param);
                // give income
                plugin.getEconomy().pay(this, income*multiplier);
                entry.getValue().addExp(exp*multiplier);
                checkLevels();
            }
            param.remove("joblevel");
        }
        // no job
        if(this.progression.size() == 0) {
            Job jobNone = plugin.getJobConfig().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getCraftIncome(items, param);
                if(income != null) {
                    // give income
                    plugin.getEconomy().pay(this, income*multiplier);
                }
                param.remove("joblevel");
            }
        }
    }
    
    /**
     * Get the list of jobs
     * @return the list of jobs
     */
    public Set<Job> getJobs() {
        return Collections.unmodifiableSet(progression.keySet());
    }
    
    /**
     * Get the list of job progressions
     * @return the list of job progressions
     */
    public Collection<JobProgression> getJobsProgression() {
        return Collections.unmodifiableCollection(progression.values());
    }
    
    /**
     * Get the job progression with the certain job
     * @return the job progression
     */
    public JobProgression getJobsProgression(Job job) {
        return progression.get(job);
    }
    
    /**
     * get the player
     * @return the player
     */
    public String getName(){
        return playername;
    }
    
    /**
     * Function called to update the levels and make sure experience < maxExperience
     */
    private void checkLevels() {
        Player player = plugin.getServer().getPlayer(getName());
        boolean leveledUp = false;
        boolean levelChanged = false;
        do {
            levelChanged = false;
            for (JobProgression prog : progression.values()) {
                if (!prog.canLevelUp())
                    continue;
                Job job = prog.getJob();
                if (job.getMaxLevel() != null && prog.getLevel() >= job.getMaxLevel()) {
                    prog.setExperience(0.0);
                    String message = plugin.getMessageConfig().getMessage("at-max-level");
                    if (player != null) {
                        for(String line: message.split("\n")){
                            player.sendMessage(line);
                        }
                    }
                    continue;
                }
                leveledUp = true;
                levelChanged = true;
                // increase the level
                prog.setLevel(prog.getLevel()+1);
                // decrease the current exp
                prog.setExperience(prog.getExperience()-prog.getMaxExperience());
                // recalculate the maxexp 
                HashMap<String, Double> param = new HashMap<String, Double>();
                param.put("numjobs", (double) getJobs().size());
                param.put("joblevel", (double) prog.getLevel());
                prog.setMaxExperience((int) job.getMaxExp(param));
                
                String message;
                if(plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                    message = plugin.getMessageConfig().getMessage("level-up-broadcast");
                } else {
                    message = plugin.getMessageConfig().getMessage("level-up-no-broadcast");
                }
                message = message.replace("%jobname%", job.getName());
                message = message.replace("%jobcolour%", job.getChatColour().toString());
                if(prog.getTitle() != null){
                    message = message.replace("%titlename%", prog.getTitle().getName());
                    message = message.replace("%titlecolour%", prog.getTitle().getChatColor().toString());
                }
                message = message.replace("%playername%", getName());
                if (player == null) {
                    message = message.replace("%playerdisplayname%", getName());
                } else {
                    message = message.replace("%playerdisplayname%", player.getDisplayName());
                }
                message = message.replace("%joblevel%", ""+prog.getLevel());
                for (String line: message.split("\n")) {
                    if (plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                        plugin.getServer().broadcastMessage(line);
                    } else if (player != null) {
                        player.sendMessage(line);
                    }
                }
                
                Title levelTitle = plugin.getJobsConfiguration().getTitleForLevel(prog.getLevel());
                if (levelTitle == null || levelTitle.equals(prog.getTitle()))
                    continue;
                
                // user would skill up
                if (plugin.getJobsConfiguration().isBroadcastingSkillups()) {
                    message = plugin.getMessageConfig().getMessage("skill-up-broadcast");
                } else {
                    message = plugin.getMessageConfig().getMessage("skill-up-no-broadcast");
                }
                message = message.replace("%playername%", getName());
                message = message.replace("%titlecolour%", levelTitle.getChatColor().toString());
                message = message.replace("%titlename%", levelTitle.getName());
                message = message.replace("%jobcolour%", job.getChatColour().toString());
                message = message.replace("%jobname%", job.getName());
                for (String line: message.split("\n")) {
                    if (plugin.getJobsConfiguration().isBroadcastingLevelups()) {
                        plugin.getServer().broadcastMessage(line);
                    } else if (player != null) {
                        player.sendMessage(line);
                    }
                }
                prog.setTitle(levelTitle);
            }
        } while (levelChanged);
        
        if (leveledUp) {
            reloadHonorific();
            recalculatePermissions();
        }
    }
    
    public String getDisplayHonorific() {
        return honorific;
    }
    
    /**
     * Player joins a job
     * @param job - the job joined
     */
    public void joinJob(Job job) {
        if (!progression.containsKey(job)) {
            progression.put(job, new JobProgression(plugin, job, 0.0, 1, this));
            dao.joinJob(this, job);
            reloadMaxExperience();
            reloadHonorific();
            recalculatePermissions();
        }
    }
    
    /**
     * Player leaves a job
     * @param job - the job left
     */
    public void leaveJob(Job job) {
        JobProgression prog = progression.remove(job);
        if (prog != null) {
            dao.quitJob(this, job);
            reloadMaxExperience();
            reloadHonorific();
            recalculatePermissions();
        }
    }
    
    /**
     * Promotes player in job
     * @param job - the job being promoted
     * @param levels - number of levels to promote
     */
    public void promoteJob(Job job, int levels) {
        JobProgression prog = progression.get(job);
        if (prog == null)
            return;
        if (levels <= 0)
            return;
        int newLevel = prog.getLevel() + levels;
        Integer maxLevel = job.getMaxLevel();
        if (maxLevel != null && newLevel > maxLevel) {
            newLevel = maxLevel.intValue();
        }
        setLevel(job, newLevel);
    }
    
    /**
     * Demotes player in job
     * @param job - the job being deomoted
     * @param levels - number of levels to demote
     */
    public void demoteJob(Job job, int levels) {
        JobProgression prog = progression.get(job);
        if (prog == null)
            return;
        if (levels <= 0)
            return;
        int newLevel = prog.getLevel() - levels;
        if (newLevel < 1) {
            newLevel = 1;
        }
        setLevel(job, newLevel);
    }
    
    /**
     * Sets player to a specific level
     * @param job - the job
     * @param level - the level
     */
    private void setLevel(Job job, int level) {
        JobProgression prog = progression.get(job);
        if (prog == null)
            return;

        if (level != prog.getLevel()) {
            prog.setLevel(level);
            reloadMaxExperience();
            reloadHonorific();
            recalculatePermissions();
        }
    }
    
    /**
     * Sets player to a specific level
     * @param job - the job
     * @param experience - the experience
     */
    public void addExperience(Job job, double experience) {
        JobProgression prog = progression.get(job);
        if (prog == null)
            return;
        prog.addExp(experience);
        checkLevels();
    }
    
    /**
     * Player leaves a job
     * @param job - the job left
     */
    public void transferJob(Job oldjob, Job newjob) {
        if (!progression.containsKey(newjob)) {
            JobProgression prog = progression.remove(oldjob);
            if (prog != null) {
                prog.setJob(newjob);
                progression.put(newjob, prog);
                dao.quitJob(this, oldjob);
                dao.joinJob(this, newjob);
                if (newjob.getMaxLevel() != null && prog.getLevel() > newjob.getMaxLevel()) {
                    prog.setLevel(newjob.getMaxLevel());
                }
                reloadMaxExperience();
                reloadHonorific();
                checkLevels();
                recalculatePermissions();
                save();
            }
        }
    }
    
    /**
     * Checks if the player is in this job.
     * @param job - the job
     * @return true - they are in the job
     * @return false - they are not in the job
     */
    public boolean isInJob(Job job) {
        return progression.containsKey(job);
    }
    
    /**
     * Function that reloads your honorific
     */
    private void reloadHonorific() {
        StringBuilder builder = new StringBuilder();
        int numJobs = progression.size();
        boolean gotTitle = false;
        for (JobProgression prog: progression.values()) {
            DisplayMethod method = prog.getJob().getDisplayMethod();
            
            if (method.equals(DisplayMethod.NONE))
                continue;
            
            if (gotTitle) {
                builder.append(" ");
                gotTitle = false;
            }
            
            if (numJobs == 1) {
                if (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.TITLE)) {
                    if (prog.getTitle() != null) {
                        builder.append(prog.getTitle().getChatColor() + prog.getTitle().getName() + ChatColor.WHITE);
                        gotTitle = true;
                    }
                }
                
                if(method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.JOB)) {
                    if (gotTitle) {
                        builder.append(" ");
                    }
                    builder.append(prog.getJob().getChatColour() + prog.getJob().getName() + ChatColor.WHITE);
                    gotTitle = true;
                }
            }
            
            if (numJobs > 1 && (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.TITLE)) ||
                    method.equals(DisplayMethod.SHORT_FULL) ||
                    method.equals(DisplayMethod.SHORT_TITLE)) {
                // add title to honorific
                if (prog.getTitle() != null) {
                    builder.append(prog.getTitle().getChatColor() + prog.getTitle().getShortName() + ChatColor.WHITE);
                    gotTitle = true;
                }
            }
            
            if (numJobs > 1 && (method.equals(DisplayMethod.FULL) || method.equals(DisplayMethod.JOB)) ||
                    method.equals(DisplayMethod.SHORT_FULL) || 
                    method.equals(DisplayMethod.SHORT_JOB)) {
                builder.append(prog.getJob().getChatColour() + prog.getJob().getShortName() + ChatColor.WHITE);
                gotTitle = true;
            }
        }
        
        honorific = builder.toString().trim();
    }
    
    /**
     * Function to reload all of the maximum experiences
     */
    private void reloadMaxExperience() {
        HashMap<String, Double> param = new HashMap<String, Double>();
        param.put("numjobs", (double) progression.size());
        for (JobProgression prog: progression.values()) {
            param.put("joblevel", (double) prog.getLevel());
            prog.setMaxExperience((int)prog.getJob().getMaxExp(param));
            param.remove("joblevel");
        }
    }
    
    /**
     * Function to recalculate permissions
     */
    private void recalculatePermissions() {
        Player player = plugin.getServer().getPlayer(playername);
        if (player == null)
            return;
        
        boolean changed = false;
        if (this.attachment != null) {
            player.removeAttachment(attachment);
            this.attachment = null;
            changed = true;
        }
        
        if (progression.size() == 0) {
            Job job = plugin.getJobConfig().getJob("None");
            if (job != null) {
                for (JobPermission perm : job.getPermissions()) {
                    if (perm.getLevelRequirement() <= 0) {
                        if (this.attachment == null) {
                            this.attachment = player.addAttachment(plugin);
                            changed = true;
                        }
                        attachment.setPermission(perm.getNode(), perm.getValue());
                    }
                }
            }
        } else {
            for (JobProgression prog : progression.values()) {
                for (JobPermission perm : prog.getJob().getPermissions()) {
                    if (prog.getLevel() >= perm.getLevelRequirement()) {
                        if (this.attachment == null) {
                            this.attachment = player.addAttachment(plugin);
                            changed = true;
                        }
                        attachment.setPermission(perm.getNode(), perm.getValue());
                    }
                }
            }
        }
        if (changed)
            player.recalculatePermissions();
    }
    
    /**
     * Function to remove all permissions
     */
    public void removePermissions() {
        Player player = plugin.getServer().getPlayer(playername);
        if (player == null)
            return;
        
        if (this.attachment != null) {
            player.removeAttachment(attachment);
            player.recalculatePermissions();
            this.attachment = null;
        }
    }
    
    /**
     * Saves data
     */
    public void save() {
        dao.save(this);
    }
}
