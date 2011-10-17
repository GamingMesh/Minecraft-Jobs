package com.zford.jobs.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JobsConnection {
    private Connection conn;
    private JobsConnectionPool pool;
    public JobsConnection(Connection conn, JobsConnectionPool pool) {
        this.conn = conn;
        this.pool = pool;
    }
    
    public synchronized boolean isClosed() {
        try {
            return conn.isClosed();
        } catch(SQLException e) {
            // Assume it's closed
            return true;
        }
    }
    
    public synchronized void close() {
        pool.returnToPool(this);
    }
    
    public synchronized void closeConnection() throws SQLException {
        conn.close();
    }
    
    public synchronized Statement createStatement() throws SQLException {
        return conn.createStatement();
    }
    
    public synchronized PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
}
