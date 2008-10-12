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
 * 
 */
package org.quartz.impl.jdbcjobstore;

import java.sql.Connection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for database based lock handlers for providing thread/resource locking 
 * in order to protect resources from being altered by multiple threads at the 
 * same time.
 */
public abstract class DBSemaphore implements Semaphore, Constants,
    StdJDBCConstants, TablePrefixAware {

    private final Log log = LogFactory.getLog(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    ThreadLocal lockOwners = new ThreadLocal();

    private String sql;

    private String tablePrefix;
    
    private String expandedSQL;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public DBSemaphore(String tablePrefix, String sql, String defaultSQL) {
        this.sql = defaultSQL;
        this.tablePrefix = tablePrefix;
        setSQL(sql);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected Log getLog() {
        return log;
    }

    private HashSet getThreadLocks() {
        HashSet threadLocks = (HashSet) lockOwners.get();
        if (threadLocks == null) {
            threadLocks = new HashSet();
            lockOwners.set(threadLocks);
        }
        return threadLocks;
    }

    /**
     * Execute the SQL that will lock the proper database row.
     */
    protected abstract void executeSQL(
        Connection conn, String lockName, String expandedSQL) throws LockException;
    
    /**
     * Grants a lock on the identified resource to the calling thread (blocking
     * until it is available).
     * 
     * @return true if the lock was obtained.
     */
    public boolean obtainLock(Connection conn, String lockName)
        throws LockException {

        lockName = lockName.intern();

        Log log = getLog();
        
        if(log.isDebugEnabled()) {
            log.debug(
                "Lock '" + lockName + "' is desired by: "
                        + Thread.currentThread().getName());
        }
        if (!isLockOwner(conn, lockName)) {

            executeSQL(conn, lockName, expandedSQL);
            
            if(log.isDebugEnabled()) {
                log.debug(
                    "Lock '" + lockName + "' given to: "
                            + Thread.currentThread().getName());
            }
            getThreadLocks().add(lockName);
            //getThreadLocksObtainer().put(lockName, new
            // Exception("Obtainer..."));
        } else if(log.isDebugEnabled()) {
            log.debug(
                "Lock '" + lockName + "' Is already owned by: "
                        + Thread.currentThread().getName());
        }

        return true;
    }

       
    /**
     * Release the lock on the identified resource if it is held by the calling
     * thread.
     */
    public void releaseLock(Connection conn, String lockName) {

        lockName = lockName.intern();

        if (isLockOwner(conn, lockName)) {
            if(getLog().isDebugEnabled()) {
                getLog().debug(
                    "Lock '" + lockName + "' returned by: "
                            + Thread.currentThread().getName());
            }
            getThreadLocks().remove(lockName);
            //getThreadLocksObtainer().remove(lockName);
        } else if (getLog().isDebugEnabled()) {
            getLog().warn(
                "Lock '" + lockName + "' attempt to return by: "
                        + Thread.currentThread().getName()
                        + " -- but not owner!",
                new Exception("stack-trace of wrongful returner"));
        }
    }

    /**
     * Determine whether the calling thread owns a lock on the identified
     * resource.
     */
    public boolean isLockOwner(Connection conn, String lockName) {
        lockName = lockName.intern();

        return getThreadLocks().contains(lockName);
    }

    /**
     * This Semaphore implementation does use the database.
     */
    public boolean requiresConnection() {
        return true;
    }

    protected String getSQL() {
        return sql;
    }

    protected void setSQL(String sql) {
        if ((sql != null) && (sql.trim().length() != 0)) {
            this.sql = sql;
        }
        
        setExpandedSQL();
    }

    private void setExpandedSQL() {
        if (getTablePrefix() != null) {
            expandedSQL = Util.rtp(this.sql, getTablePrefix());
        }
    }
    
    protected String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        
        setExpandedSQL();
    }
}
