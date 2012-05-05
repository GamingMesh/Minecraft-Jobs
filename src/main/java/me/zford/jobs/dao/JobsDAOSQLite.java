package me.zford.jobs.dao;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

import me.zford.jobs.bukkit.JobsPlugin;

public class JobsDAOSQLite extends JobsDAO {
    public JobsDAOSQLite(JobsPlugin plugin) {
        super(plugin, "org.sqlite.JDBC", "jdbc:sqlite:"+new File(plugin.getDataFolder(), "jobs.sqlite.db").getPath(), null, null, "");
        setUp();
    }
    
    public void setUp(){
        try{
            JobsConnection conn = getConnection();
            if(conn != null) {
                Statement st = conn.createStatement();
                String table = "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "jobs` (username varchar(20), experience INT, level INT, job varchar(20));";
                st.executeUpdate(table);
                conn.close();
            } else {
                System.err.println("[Jobs] - SQLite connection problem");
                plugin.disablePlugin();
            }
        }
        catch (SQLException e){
            e.printStackTrace();
            plugin.disablePlugin();
        }
    }
}
