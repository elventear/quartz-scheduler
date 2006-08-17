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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Provide thread/resource locking in order to protect
 * resources from being altered by multiple threads at the same time using
 * a db row update.
 * 
 * <p>
 * <b>Note:</b> This Semaphore implementation is useful for databases that do
 * not support row locking via "SELECT FOR UPDATE" type syntax, for example
 * Microsoft SQLServer (MSSQL).
 * </p> 
 */
public class UpdateLockRowSemaphore extends DBSemaphore {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constants.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static final String UPDATE_FOR_LOCK = 
        "UPDATE " + TABLE_PREFIX_SUBST + TABLE_LOCKS + 
        " SET " + COL_LOCK_NAME + " = " + COL_LOCK_NAME +
        " WHERE " + COL_LOCK_NAME + " = ? ";


    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constructors.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public UpdateLockRowSemaphore() {
        super(DEFAULT_TABLE_PREFIX, null, UPDATE_FOR_LOCK);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Interface.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * Execute the SQL select for update that will lock the proper database row.
     */
    protected void executeSQL(Connection conn, String lockName, String expandedSQL) throws LockException {
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(expandedSQL);
            ps.setString(1, lockName);

            if (getLog().isDebugEnabled()) {
                getLog().debug(
                    "Lock '" + lockName + "' is being obtained: " + 
                    Thread.currentThread().getName());
            }
            
            int numUpdate = ps.executeUpdate();
            
            if (numUpdate < 1) {
                throw new SQLException(Util.rtp(
                    "No row exists in table " + TABLE_PREFIX_SUBST + TABLE_LOCKS + 
                    " for lock named: " + lockName, getTablePrefix()));
            }
        } catch (SQLException sqle) {
            //Exception src =
            // (Exception)getThreadLocksObtainer().get(lockName);
            //if(src != null)
            //  src.printStackTrace();
            //else
            //  System.err.println("--- ***************** NO OBTAINER!");

            if(getLog().isDebugEnabled()) {
                getLog().debug(
                    "Lock '" + lockName + "' was not obtained by: " + 
                    Thread.currentThread().getName());
            }
            
            throw new LockException(
                "Failure obtaining db row lock: " + sqle.getMessage(), sqle);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception ignore) {
                }
            }
        }
    }
    
    protected String getUpdateLockRowSQL() {
        return getSQL();
    }

    public void setUpdateLockRowSQL(String updateLockRowSQL) {
        setSQL(updateLockRowSQL);
    }
}
