/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.utils.weblogic;

import java.sql.Connection;
import java.sql.SQLException;

import org.quartz.utils.ConnectionProvider;

import weblogic.jdbc.jts.Driver;

/**
 * <p>
 * Provides connections via Weblogic's JTS driver.
 * </p>
 * 
 * @see org.quartz.utils.ConnectionProvider
 * @see org.quartz.utils.DBConnectionManager
 * 
 * @author Mohammad Rezaei
 * @author James House
 */
public class WeblogicConnectionProvider implements ConnectionProvider {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String poolName;

    private weblogic.jdbc.jts.Driver driver;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public WeblogicConnectionProvider(String poolName) {
        this.poolName = poolName;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public Connection getConnection() throws SQLException {
        try {
            if (driver == null)
                    driver = (Driver) weblogic.jdbc.jts.Driver.class
                            .newInstance();

            java.sql.Connection con = null;
            con = driver.connect("jdbc:weblogic:jts:" + poolName,
                    (java.util.Properties) null);

            return con;
        } catch (Exception e) {
            throw new SQLException(
                    "Could not get weblogic pool connection with name '"
                            + poolName + "': " + e.getClass().getName() + ": "
                            + e.getMessage());
        }
    }
    
    public void shutdown() throws SQLException {
        // do nothing
    }    
}
