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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class JobsConnectionPool {
    private LinkedList<JobsConnection> pooledConnections;
    private String url;
    private String username;
    private String password;
    public JobsConnectionPool(String driver, String url, String username, String password) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.pooledConnections = new LinkedList<JobsConnection>();
        Class.forName(driver).newInstance();
        this.url = url;
        this.username = username;
        this.password = password;
    }
    
    public synchronized JobsConnection getConnection() throws SQLException {
        // Try to get a pooled connection
        while(!pooledConnections.isEmpty()) {
            JobsConnection conn = pooledConnections.remove();
            if(!conn.isClosed())
                return conn;
        }
        // create a new connection
        Connection conn = DriverManager.getConnection(url, username, password);
        return new JobsConnection(conn, this);
    }
    
    public synchronized void returnToPool(JobsConnection conn) {
        pooledConnections.add(conn);
    }
    
    public synchronized void closeConnections() {
        while(!pooledConnections.isEmpty()) {
            JobsConnection conn = pooledConnections.remove();
            try {
                conn.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
