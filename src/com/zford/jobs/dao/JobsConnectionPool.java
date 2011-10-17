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
