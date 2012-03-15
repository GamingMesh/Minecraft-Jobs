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

package me.zford.jobs.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import me.zford.jobs.Jobs;
import me.zford.jobs.config.container.Job;
import me.zford.jobs.config.container.JobProgression;
import me.zford.jobs.config.container.JobsPlayer;
import me.zford.jobs.dao.container.JobsDAOData;


public class JobsDAOMySQL extends JobsDAO {
    private String prefix = "";
    
    public JobsDAOMySQL(Jobs plugin, String url, String dbName, String username, String password, String prefix) {
        super(plugin, "com.mysql.jdbc.Driver", url+dbName, username, password);
        this.prefix = prefix;
        setUp();
    }
    
    public void setUp(){
        try{
            JobsConnection conn = getConnection();
            if(conn != null){
                Statement st = conn.createStatement();
                String table = "CREATE TABLE IF NOT EXISTS " + prefix + "jobs (username varchar(20), experience integer, level integer, job varchar(20));";
                st.executeUpdate(table);
                conn.close();
            }
            else{
                System.err.println("[Jobs] - MySQL connection problem");
                plugin.disablePlugin();
            }
        }
        catch (SQLException e){
            e.printStackTrace();
            plugin.disablePlugin();
        }
    }
    
    @Override
    public List<JobsDAOData> getAllJobs(JobsPlayer player) {
        ArrayList<JobsDAOData> jobs = null;
        try{
            JobsConnection conn = getConnection();
            String sql = "SELECT `experience`, `level`, `job` FROM `" + prefix + "jobs` WHERE `username` = ?;";
            PreparedStatement prest = conn.prepareStatement(sql);
            prest.setString(1, player.getName());
            ResultSet res = prest.executeQuery();
            while(res.next()){
                if(jobs == null){
                    jobs = new ArrayList<JobsDAOData>();
                }
                jobs.add(new JobsDAOData(res.getString(3), res.getInt(1), res.getInt(2)));
            }
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
            plugin.disablePlugin();
        }
        return jobs;
    }

    @Override
    public void quitJob(JobsPlayer player, Job job) {
        try{
            JobsConnection conn = getConnection();
            String sql1 = "DELETE FROM `" + prefix + "jobs` WHERE `username` = ? AND `job` = ?;";
            PreparedStatement prest = conn.prepareStatement(sql1);
            prest.setString(1, player.getName());
            prest.setString(2, job.getName());
            prest.executeUpdate();
            prest.close();
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
            plugin.disablePlugin();
        }       
    }

    @Override
    public void save(JobsPlayer player) {
        String sql = "UPDATE `" + prefix + "jobs` SET `experience` = ?, `level` = ? WHERE `username` = ? AND `job` = ?;";
        try {
            JobsConnection conn = getConnection();
            PreparedStatement prest = conn.prepareStatement(sql);
            for(JobProgression temp: player.getJobsProgression()){
                prest.setInt(1, (int)temp.getExperience());
                prest.setInt(2, temp.getLevel());
                prest.setString(3, player.getName());
                prest.setString(4, temp.getJob().getName());
                prest.executeUpdate();
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.disablePlugin();
        }
    }

    @Override
    public void joinJob(JobsPlayer player, Job job) {
        String sql = "INSERT INTO `" + prefix + "jobs` (`username`, `experience`, `level`, `job`) VALUES (?, ?, ?, ?);";
        try {
            JobsConnection conn = getConnection();
            PreparedStatement prest = conn.prepareStatement(sql);
            prest.setString(1, player.getName());
            prest.setInt(2, 0);
            prest.setInt(3, 1);
            prest.setString(4, job.getName());
            prest.executeUpdate();
            prest.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.disablePlugin();
        }
    }

    @Override
    public Integer getSlotsTaken(Job job) {
        Integer slot = 0;
        try{
            JobsConnection conn = getConnection();
            String sql = "SELECT COUNT(*) FROM `" + prefix + "jobs` WHERE `job` = ?;";
            PreparedStatement prest = conn.prepareStatement(sql);
            prest.setString(1, job.getName());
            ResultSet res = prest.executeQuery();
            if(res.next()){
                slot = res.getInt(1);
            }
            conn.close();
        }
        catch(SQLException e){
            e.printStackTrace();
            plugin.disablePlugin();
        }
        return slot;
    }
}
