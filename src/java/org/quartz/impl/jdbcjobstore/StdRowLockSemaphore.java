/* 
 * Copyright 2001-2009 James House 
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

/*
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.impl.jdbcjobstore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Internal database based lock handler for providing thread/resource locking 
 * in order to protect resources from being altered by multiple threads at the 
 * same time.
 * 
 * @author jhouse
 */
public class StdRowLockSemaphore extends DBSemaphore {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static final String SELECT_FOR_LOCK = "SELECT * FROM "
            + TABLE_PREFIX_SUBST + TABLE_LOCKS + " WHERE " + COL_LOCK_NAME
            + " = ? FOR UPDATE";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * This constructor is for using the <code>StdRowLockSemaphore</code> as
     * a bean.
     */
    public StdRowLockSemaphore() {
        super(DEFAULT_TABLE_PREFIX, null, SELECT_FOR_LOCK);
    }
    
    public StdRowLockSemaphore(String tablePrefix, String selectWithLockSQL) {
        super(tablePrefix, selectWithLockSQL, SELECT_FOR_LOCK);
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
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(expandedSQL);
            ps.setString(1, lockName);
            
            if (getLog().isDebugEnabled()) {
                getLog().debug(
                    "Lock '" + lockName + "' is being obtained: " + 
                    Thread.currentThread().getName());
            }
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException(Util.rtp(
                    "No row exists in table " + TABLE_PREFIX_SUBST + 
                    TABLE_LOCKS + " for lock named: " + lockName, getTablePrefix()));
            }
        } catch (SQLException sqle) {
            //Exception src =
            // (Exception)getThreadLocksObtainer().get(lockName);
            //if(src != null)
            //  src.printStackTrace();
            //else
            //  System.err.println("--- ***************** NO OBTAINER!");

            if (getLog().isDebugEnabled()) {
                getLog().debug(
                    "Lock '" + lockName + "' was not obtained by: " + 
                    Thread.currentThread().getName());
            }
            
            throw new LockException("Failure obtaining db row lock: "
                    + sqle.getMessage(), sqle);
        } finally {
            if (rs != null) { 
                try {
                    rs.close();
                } catch (Exception ignore) {
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    protected String getSelectWithLockSQL() {
        return getSQL();
    }

    public void setSelectWithLockSQL(String selectWithLockSQL) {
        setSQL(selectWithLockSQL);
    }
}
