/* 
 * Copyright 2004-2006 OpenSymphony 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */
package org.quartz.impl.jdbcjobstore;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobPersistenceException;

/**
 * <p>
 * Protects a <code>{@link java.sql.Connection}</code>'s attributes from being permanently modfied.
 * </p>
 * 
 * <p>
 * Wraps a provided <code>{@link java.sql.Connection}</code> such that its auto 
 * commit and transaction isolation attributes can be overwritten, but 
 * will automatically restored to their original values when the connection
 * is actually closed (and potentially returned to a pool for reuse).
 * </p>
 * 
 * @see org.quartz.impl.jdbcjobstore.JobStoreSupport#getConnection()
 * @see org.quartz.impl.jdbcjobstore.JobStoreCMT#getNonManagedTXConnection()
 */
public class AttributeRestoringConnectionWrapper implements Connection {
    private Connection conn;
    
    private boolean overwroteOriginalAutoCommitValue;
    private boolean overwroteOriginalTxIsolationValue;

    // Set if overwroteOriginalAutoCommitValue is true
    private boolean originalAutoCommitValue; 

    // Set if overwroteOriginalTxIsolationValue is true
    private int originalTxIsolationValue;
    
    public AttributeRestoringConnectionWrapper(
        Connection conn) throws JobPersistenceException {
        this.conn = conn;
    }

    Log getLog() {
        return LogFactory.getLog(getClass());
    }
    
    /**
     * Sets this connection's auto-commit mode to the given state, saving
     * the original mode.  The connection's original auto commit mode is restored
     * when the connection is closed.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        boolean currentAutoCommitValue = conn.getAutoCommit();
            
        if (autoCommit != currentAutoCommitValue) {
            if (overwroteOriginalAutoCommitValue == false) {
                overwroteOriginalAutoCommitValue = true;
                originalAutoCommitValue = currentAutoCommitValue;
            }
            
            conn.setAutoCommit(autoCommit);
        }
    }

    /**
     * Attempts to change the transaction isolation level to the given level, saving
     * the original level.  The connection's original transaction isolation level is 
     * restored when the connection is closed.
     */
    public void setTransactionIsolation(int level) throws SQLException {
        int currentLevel = conn.getTransactionIsolation();
        
        if (level != currentLevel) {
            if (overwroteOriginalTxIsolationValue == false) {
                overwroteOriginalTxIsolationValue = true;
                originalTxIsolationValue = currentLevel;
            }
            
            conn.setTransactionIsolation(level);
        }
    }
    
    /**
     * Gets the underlying connection to which all operations ultimately 
     * defer.  This is provided in case a user ever needs to punch through 
     * the wrapper to access vendor specific methods outside of the 
     * standard <code>java.sql.Connection</code> interface.
     * 
     * @return The underlying connection to which all operations
     * ultimately defer.
     */
    public Connection getWrappedConnection() {
        return conn;
    }

    /**
     * Attempts to restore the auto commit and transaction isolation connection
     * attributes of the wrapped connection to their original values (if they
     * were overwritten).
     */
    public void restoreOriginalAtributes() {
        try {
            if (overwroteOriginalAutoCommitValue) {
                conn.setAutoCommit(originalAutoCommitValue);
            }
        } catch (Throwable t) {
            getLog().warn("Failed restore connection's original auto commit setting.", t);
        }
        
        try {    
            if (overwroteOriginalTxIsolationValue) {
                conn.setTransactionIsolation(originalTxIsolationValue);
            }
        } catch (Throwable t) {
            getLog().warn("Failed restore connection's original transaction isolation setting.", t);
        }
    }
    
    /**
     * Attempts to restore the auto commit and transaction isolation connection
     * attributes of the wrapped connection to their original values (if they
     * were overwritten), before finally actually closing the wrapped connection.
     */
    public void close() throws SQLException {
        restoreOriginalAtributes();
        
        conn.close();
    }

    // The following methods are just passthrough to the wrapped connection
    
    public Statement createStatement() throws SQLException {
        return conn.createStatement();
    }
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return conn.prepareStatement(sql);
    }
    public CallableStatement prepareCall(String sql) throws SQLException {
        return conn.prepareCall(sql);
    }
    public String nativeSQL(String sql) throws SQLException {
        return conn.nativeSQL(sql);
    }
    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }
    public void commit() throws SQLException {
        conn.commit();
    }
    public void rollback() throws SQLException {
        conn.rollback();
    }
    public boolean isClosed() throws SQLException {
        return conn.isClosed();
    }
    public DatabaseMetaData getMetaData() throws SQLException {
        return conn.getMetaData();
    }
    public void setReadOnly(boolean readOnly) throws SQLException {
        conn.setReadOnly(readOnly);
    }
    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }
    public void setCatalog(String catalog) throws SQLException {
        conn.setCatalog(catalog);
    }
    public String getCatalog() throws SQLException {
        return conn.getCatalog();
    }
    public int getTransactionIsolation() throws SQLException {
        return conn.getTransactionIsolation();
    }
    public SQLWarning getWarnings() throws SQLException {
        return conn.getWarnings();
    }
    public void clearWarnings() throws SQLException {
        conn.clearWarnings();
    }
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency);
    }
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    public Map getTypeMap() throws SQLException {
        return conn.getTypeMap();
    }
    public void setTypeMap(Map map) throws SQLException {
        conn.setTypeMap(map);
    }
    public void setHoldability(int holdability) throws SQLException {
        conn.setHoldability(holdability);
    }
    public int getHoldability() throws SQLException {
        return conn.getHoldability();
    }
    public Savepoint setSavepoint() throws SQLException {
        return conn.setSavepoint();
    }
    public Savepoint setSavepoint(String name) throws SQLException {
        return conn.setSavepoint(name);
    }
    public void rollback(Savepoint savepoint) throws SQLException {
        conn.rollback(savepoint);
    }
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return conn.prepareStatement(sql, autoGeneratedKeys);
    }
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return conn.prepareStatement(sql, columnIndexes);
    }
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return conn.prepareStatement(sql, columnNames);
    }
}
