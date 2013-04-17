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

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;

import me.zford.jobs.Jobs;

public class JobsDAOH2 extends JobsDAO {
    public JobsDAOH2() {
        super("org.h2.Driver", "jdbc:h2:"+new File(Jobs.getDataFolder(), "jobs").getPath(), "sa", "sa", "");
        File dir = Jobs.getDataFolder();
        if (!dir.exists())
            dir.mkdirs();
        setUp();
    }
    
    public synchronized void setUp(){
        try {
            JobsConnection conn = getConnection();
            if (conn == null) {
                Jobs.getPluginLogger().severe("Could not initialize database!  Could not connect to H2!");
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
