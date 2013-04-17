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

import java.sql.SQLException;
import java.sql.Statement;

import me.zford.jobs.Jobs;

public class JobsDAOMySQL extends JobsDAO {
    
    public JobsDAOMySQL(String url, String username, String password, String prefix) {
        super("com.mysql.jdbc.Driver", url, username, password, prefix);
        setUp();
    }
    
    public synchronized void setUp(){
        try {
            JobsConnection conn = getConnection();
            if (conn == null) {
                Jobs.getPluginLogger().severe("Could not initialize database!  Could not connect to MySQL!");
                return;
            }
            Statement st = conn.createStatement();
            String table = "CREATE TABLE IF NOT EXISTS `" + getPrefix() + "jobs` (username varchar(20), experience integer, level integer, job varchar(20));";
            st.executeUpdate(table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
