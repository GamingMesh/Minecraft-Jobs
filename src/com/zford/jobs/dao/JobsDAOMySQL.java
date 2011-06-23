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

package com.zford.jobs.dao;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import org.bukkit.entity.Player;

import com.mysql.jdbc.Connection;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.container.Job;
import com.zford.jobs.config.container.JobProgression;
import com.zford.jobs.config.container.PlayerJobInfo;
import com.zford.jobs.dao.container.JobsDAOData;

public class JobsDAOMySQL implements JobsDAO {
	private String url = null;
	private String dbName = null;
	private String driver = "com.mysql.jdbc.Driver";
	private String username = null;
	private String password = null;
	private String prefix = "";
	
	public JobsDAOMySQL(String url, String dbName, String username, String password, String prefix) {
		this.url = url;
		this.dbName = dbName;
		this.username = username;
		this.password = password;
		this.prefix = prefix;
		setUp();
	}
	
	public void setUp(){
		try{
			Connection conn = getConnection();
			if(conn != null){
				Statement st = conn.createStatement();
				String table = "CREATE TABLE IF NOT EXISTS " + prefix + "jobs (username varchar(20), experience integer, level integer, job varchar(20));";
				st.executeUpdate(table);
				conn.close();
			}
			else{
				System.err.println("[Jobs] - MySQL connection problem");
				Jobs.disablePlugin();
			}
		}
		catch (SQLException s){
			s.printStackTrace();
			Jobs.disablePlugin();
		}
	}

	private Connection getConnection(){
		try{
			Class.forName(driver).newInstance();
			return (Connection) DriverManager.getConnection(url+dbName, username, password);
		}
		catch (Exception e){
			e.printStackTrace();
			System.err.println("[Jobs - database connection error. Disabling jobs!]");
			Jobs.disablePlugin();
			return null;
		}
	}
	
	@Override
	public List<JobsDAOData> getAllJobs(Player player) {
		ArrayList<JobsDAOData> jobs = null;
		Connection conn = getConnection();
		try{
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
		}
		return jobs;
	}

	@Override
	public void quitJob(Player player, Job job) {
		try{
			Connection conn = getConnection();
			String sql1 = "DELETE FROM `" + prefix + "jobs` WHERE `username` = ? AND `job` = ?;";
			PreparedStatement prest = conn.prepareStatement(sql1);
			prest.setString(1, player.getName());
			prest.setString(2, job.getName());
			prest.executeUpdate();
			prest.close();
			conn.close();
		}
		catch(SQLException e){
			
		}		
	}

	@Override
	public void save(PlayerJobInfo jobInfo) {
		String sql = "UPDATE `" + prefix + "jobs` SET `experience` = ?, `level` = ? WHERE `username` = ? AND `job` = ?;";
		try {
			Connection conn = getConnection();
			PreparedStatement prest = conn.prepareStatement(sql);
			for(JobProgression temp: jobInfo.getJobsProgression()){
				prest.setInt(1, (int)temp.getExperience());
				prest.setInt(2, temp.getLevel());
				prest.setString(3, jobInfo.getPlayer().getName());
				prest.setString(4, temp.getJob().getName());
				prest.executeUpdate();
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void joinJob(Player player, Job job) {
		String sql = "INSERT INTO `" + prefix + "jobs` (`username`, `experience`, `level`, `job`) VALUES (?, ?, ?, ?);";
		try {
			Connection conn = getConnection();
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

	@Override
	public Integer getSlotsTaken(Job job) {
		Integer slot = 0;
		Connection conn = getConnection();
		try{
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
		}
		return slot;
	}

}
