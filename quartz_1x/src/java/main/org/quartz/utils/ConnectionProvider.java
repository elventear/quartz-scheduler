/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementations of this interface used by <code>DBConnectionManager</code>
 * to provide connections from various sources.
 * 
 * @see DBConnectionManager
 * @see PoolingConnectionProvider
 * @see JNDIConnectionProvider
 * @see org.quartz.utils.weblogic.WeblogicConnectionProvider
 * 
 * @author Mohammad Rezaei
 */
public interface ConnectionProvider {
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * @return connection managed by this provider
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException;
    
    
    public void shutdown() throws SQLException;
}
