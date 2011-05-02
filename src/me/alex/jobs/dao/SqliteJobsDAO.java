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

public class SqliteJobsDAO implements JobsDAO{
	private Jobs plugin = null;
	private String driver = "org.sqlite.JDBC";
	private String url = null;
	private Connection conn;
	public SqliteJobsDAO(Jobs plugin, String url) {
		this.plugin = plugin;
		this.url = url;
	}
	
	@SuppressWarnings("unused")
	private SqliteJobsDAO(){}

	private void connect(){
		try{
			Class.forName(driver).newInstance();
			conn = DriverManager.getConnection(url);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private void disconnect(){
		try{
			conn.close();
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
			String sql = "SELECT experience, level, job FROM jobs WHERE username = ?;";
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
		connect();
		if(plugin.getJob(player)!=null){
			// existing job
			String sql = "UPDATE jobs SET experience = 0, level = 1, job = ? WHERE username = ?;";
			try {
				PreparedStatement prest = conn.prepareStatement(sql);
				prest.setString(1, newJob);
				prest.setString(2, player.getName());
				prest.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// new job
			String sql = "INSERT INTO jobs VALUES (?, ?, ?, ?);";
			try {
				PreparedStatement prest = conn.prepareStatement(sql);
				prest.setString(1, player.getName());
				prest.setInt(2, 0);
				prest.setInt(3, 1);
				prest.setString(4, newJob);
				prest.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		disconnect();
	}
	
	public void saveJob(Player player, Job job){
		connect();
		String sql = "UPDATE jobs SET experience = ?, level = ? WHERE username = ?;";
		try {
			PreparedStatement prest = conn.prepareStatement(sql);
			prest.setInt(1, job.getExperience());
			prest.setInt(2, job.getLevel());
			prest.setString(3, player.getName());
			prest.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		disconnect();
	}
}
