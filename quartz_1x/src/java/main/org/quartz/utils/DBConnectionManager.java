/*
 * Copyright (c) 2004-2005 by OpenSymphony
 * All rights reserved.
 * 
 * Previously Copyright (c) 2001-2004 James House
 */
package org.quartz.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * <p>
 * Manages a collection of ConnectionProviders, and provides transparent access
 * to their connections.
 * </p>
 * 
 * @see ConnectionProvider
 * @see PoolingConnectionProvider
 * @see JNDIConnectionProvider
 * @see org.quartz.utils.weblogic.WeblogicConnectionProvider
 * 
 * @author James House
 * @author Sharada Jambula
 * @author Mohammad Rezaei
 */
public class DBConnectionManager {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public static final String DB_PROPS_PREFIX = "org.quartz.db.";

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private static DBConnectionManager instance = new DBConnectionManager();

    private HashMap providers = new HashMap();

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Private constructor
     * </p>
     *  
     */
    private DBConnectionManager() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public void addConnectionProvider(String dataSourceName,
            ConnectionProvider provider) {
        this.providers.put(dataSourceName, provider);
    }

    /**
     * Get a database connection from the DataSource with the given name.
     * 
     * @return a database connection
     * @exception SQLException
     *              if an error occurs, or there is no DataSource with the
     *              given name.
     */
    public Connection getConnection(String dsName) throws SQLException {
        ConnectionProvider provider = (ConnectionProvider) providers
                .get(dsName);
        if (provider == null)
                throw new SQLException("There is no DataSource named '"
                        + dsName + "'");

        return provider.getConnection();
    }

    /**
     * Get the class instance.
     * 
     * @return an instance of this class
     */
    public static DBConnectionManager getInstance() {
        // since the instance variable is initialized at class loading time,
        // it's not necessary to synchronize this method */
        return instance;
    }

    /**
     * Shuts down database connections from the DataSource with the given name,
     * if applicable for the underlying provider.
     *
     * @return a database connection
     * @exception SQLException
     *              if an error occurs, or there is no DataSource with the
     *              given name.
     */
    public void shutdown(String dsName) throws SQLException {

        ConnectionProvider provider = (ConnectionProvider) providers
        .get(dsName);
        if (provider == null)
            throw new SQLException("There is no DataSource named '"
                    + dsName + "'");

        provider.shutdown();

    }    
}
