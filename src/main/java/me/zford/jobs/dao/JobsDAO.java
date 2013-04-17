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

package me.zford.jobs.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import me.zford.jobs.Jobs;
import me.zford.jobs.container.Job;
import me.zford.jobs.container.JobProgression;
import me.zford.jobs.container.JobsPlayer;


/**
 * Data Access Object interface for the Jobs plugin
 * 
 * Interface that holds all methods that a DAO needs to have
 * @author Alex
 *
 */
public abstract class JobsDAO {
    
    private JobsConnectionPool pool;
    private String prefix;
    
    public JobsDAO(String driverName, String url, String username, String password, String prefix) {
        this.prefix = prefix;
        try {
            pool = new JobsConnectionPool(driverName, url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the database prefix
     * @return the prefix
     */
    protected String getPrefix() {
        return prefix;
    }
    
    /**
     * Get all jobs the player is part of.
     * @param player - the player being searched for
     * @return list of all of the names of the jobs the players are part of.
     */
    public synchronized List<JobsDAOData> getAllJobs(JobsPlayer player) {
        ArrayList<JobsDAOData> jobs = new ArrayList<JobsDAOData>();
        JobsConnection conn = getConnection();
        if (conn == null)
            return jobs;
        try {
            String sql = "SELECT `experience`, `level`, `job` FROM `" + prefix + "jobs` WHERE `username` = ?;";
            PreparedStatement prest = conn.prepareStatement(sql);
            prest.setString(1, player.getName());
            ResultSet res = prest.executeQuery();
            while (res.next()) {
                jobs.add(new JobsDAOData(res.getString(3), res.getInt(1), res.getInt(2)));
            }
            prest.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }
    
    /**
     * Join a job (create player-job entry from storage)
     * @param player - player that wishes to join the job
     * @param job - job that the player wishes to join
     */
    public synchronized void joinJob(JobsPlayer player, Job job) {
        String sql = "INSERT INTO `" + prefix + "jobs` (`username`, `experience`, `level`, `job`) VALUES (?, ?, ?, ?);";
        JobsConnection conn = getConnection();
        if (conn == null)
            return;
        try {
            PreparedStatement prest = conn.prepareStatement(sql);
            prest.setString(1, player.getName());
            prest.setInt(2, 0);
            prest.setInt(3, 1);
            prest.setString(4, job.getName());
            prest.executeUpdate();
            prest.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Quit a job (delete player-job entry from storage)
     * @param player - player that wishes to quit the job
     * @param job - job that the player wishes to quit
     */
    public synchronized void quitJob(JobsPlayer player, Job job) {
        JobsConnection conn = getConnection();
        if (conn == null)
            return;
        try {
            String sql1 = "DELETE FROM `" + prefix + "jobs` WHERE `username` = ? AND `job` = ?;";
            PreparedStatement prest = conn.prepareStatement(sql1);
            prest.setString(1, player.getName());
            prest.setString(2, job.getName());
            prest.executeUpdate();
            prest.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }       
    }
    
    /**
     * Save player-job information
     * @param jobInfo - the information getting saved
     */
    public synchronized void save(JobsPlayer player) {
        String sql = "UPDATE `" + prefix + "jobs` SET `experience` = ?, `level` = ? WHERE `username` = ? AND `job` = ?;";
        JobsConnection conn = getConnection();
        if (conn == null)
            return;
        try {
            PreparedStatement prest = conn.prepareStatement(sql);
            for (JobProgression temp: player.getJobProgression()) {
                prest.setInt(1, (int)temp.getExperience());
                prest.setInt(2, temp.getLevel());
                prest.setString(3, player.getName());
                prest.setString(4, temp.getJob().getName());
                prest.executeUpdate();
            }
            prest.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get the number of players that have a particular job
     * @param job - the job
     * @return  the number of players that have a particular job
     */
    public synchronized int getSlotsTaken(Job job) {
        int slot = 0;
        JobsConnection conn = getConnection();
        if (conn == null)
            return slot;
        try {
            String sql = "SELECT COUNT(*) FROM `" + prefix + "jobs` WHERE `job` = ?;";
            PreparedStatement prest = conn.prepareStatement(sql);
            prest.setString(1, job.getName());
            ResultSet res = prest.executeQuery();
            if (res.next()) {
                slot = res.getInt(1);
            }
            prest.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return slot;
    }
    
    /**
     * Get a database connection
     * @return  JobsConnection object
     * @throws SQLException 
     */
    protected JobsConnection getConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            Jobs.getPluginLogger().severe("Unable to connect to the database: "+e.getMessage());
            return null;
        }
    }
    
    /**
     * Close all active database handles
     */
    public synchronized void closeConnections() {
        pool.closeConnection();
    }
}
