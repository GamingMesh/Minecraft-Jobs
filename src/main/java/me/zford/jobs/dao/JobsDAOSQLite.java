package me.zford.jobs.dao;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

import me.zford.jobs.Jobs;

public class JobsDAOSQLite extends JobsDAO {
    public JobsDAOSQLite(Jobs core) {
        super(core, "org.sqlite.JDBC", "jdbc:sqlite:"+new File(core.getDataFolder(), "jobs.sqlite.db").getPath(), null, null, "");
        setUp();
    }
    
    public synchronized void setUp(){
        try {
            JobsConnection conn = getConnection();
            if (conn == null) {
                core.getPluginLogger().severe("Could not initialize database!  Could not connect to SQLite!");
                return;
            }
            Statement st = conn.createStatement();
            String table = "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "jobs` (username varchar(20), experience INT, level INT, job varchar(20));";
            st.executeUpdate(table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
