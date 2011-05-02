package me.alex.jobs.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import me.alex.jobs.Job;
import me.alex.jobs.Jobs;

import org.bukkit.entity.Player;

public class MySQLJobsDAO implements JobsDAO {
	private Connection conn = null;
	private String url = null;
	private String dbName = null;
	private String driver = "com.mysql.jdbc.Driver";
	private String username = null;
	private String password = null;
	
	private Jobs plugin = null;
	
	@SuppressWarnings("unused")
	private MySQLJobsDAO(){}
	
	public MySQLJobsDAO(Jobs plugin, String dbName, String url, String username, String password){
		this.plugin = plugin;
		this.dbName = dbName;
		this.url = url;
		this.username = username;
		this.password = password;
		setUp();
	}
	
	private void connect(){
		try{
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url+dbName, username, password);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void disconnect(){
		try{
			conn.close();
			conn = null;
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void setUp(){
		try{
			connect();
			Statement st = conn.createStatement();
			String table = "CREATE TABLE IF NOT EXISTS jobs (username varchar(20), experience integer, level integer, job varchar(20));";
			st.executeUpdate(table);
			disconnect();
		}
		catch (SQLException s){
			s.printStackTrace();
		}
	}
	
	public Job findPlayer(Player player){
		connect();
		Job job = null;
		try{
			String sql = "SELECT `experience`, `level`, `job` FROM `jobs` WHERE `username` = ?;";
			PreparedStatement prest = conn.prepareStatement(sql);
			prest.setString(1, player.getName());
			ResultSet res = prest.executeQuery();
			if(res.next()){
				job = new Job(res.getString(3), res.getInt(1), res.getInt(2), plugin, player);
			}
			else {
				return null;
			}
		}
		catch(SQLException e){
			
		}
		disconnect();
		return job;
	}
	
	public void changeJob(Player player, String newJob){
		cleanUp(player, newJob);
		Job job = findPlayer(player);
		connect();
		if(job!=null){
			job.stripTitle();
			// existing job
			String sql = "UPDATE `jobs` SET `experience` = 0, `level` = 1, `job` = ? WHERE `username` = ?;";
			try {
				PreparedStatement prest = conn.prepareStatement(sql);
				prest.setString(1, newJob);
				prest.setString(2, player.getName());
				prest.executeUpdate();
				prest.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// new job
			String sql = "INSERT INTO `jobs` (`username`, `experience`, `level`, `job`) VALUES (?, ?, ?, ?);";
			try {
				PreparedStatement prest = conn.prepareStatement(sql);
				prest.setString(1, player.getName());
				prest.setInt(2, 0);
				prest.setInt(3, 1);
				prest.setString(4, newJob);
				prest.executeUpdate();
				prest.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		disconnect();
	}
	
	public void saveJob(Player player, Job job){
		cleanUp(player, job.getJobName());
		connect();
		String sql = "UPDATE `jobs` SET `experience` = ?, `level` = ? WHERE `username` = ?;";
		try {
			PreparedStatement prest = conn.prepareStatement(sql);
			prest.setInt(1, job.getExperience());
			prest.setInt(2, job.getLevel());
			prest.setString(3, player.getName());
			prest.executeUpdate();
			prest.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		disconnect();
	}
	
	public void cleanUp(Player player, String job){
		connect();
		try{
			String sql1 = "DELETE FROM `jobs` WHERE `username` = ? AND NOT `job` = ?;";
			PreparedStatement prest = conn.prepareStatement(sql1);
			prest.setString(1, player.getName());
			prest.setString(2, job);
			prest.executeUpdate();
			prest.close();
			String sql = "SELECT `experience`, `level`, `job` FROM `jobs` WHERE `username` = ?;";
			prest = conn.prepareStatement(sql);
			prest.setString(1, player.getName());
			ResultSet res = prest.executeQuery();
			int count = 0;
			while(res.next()){
				++count;
			}
			if(count > 1){
				prest.close();
				String sql2 = "DELETE FROM `jobs` WHERE `username` = ? AND `job` = ? LIMIT " + (count-1) + ";";
				prest = conn.prepareStatement(sql2);
				prest.setString(1, player.getName());
				prest.setString(2, job);
				prest.executeUpdate();
				prest.close();
			}
		}
		catch(SQLException e){
			
		}
		disconnect();
	}
	
}