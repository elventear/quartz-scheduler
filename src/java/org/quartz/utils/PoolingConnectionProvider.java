/* 
 * Copyright 2004-2005 OpenSymphony 
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
package org.quartz.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * <p>
 * A <code>ConnectionProvider</code> implementation that creates its own
 * pool of connections.
 * </p>
 * 
 * <p>
 * This class uses <a href="http://jakarta.apache.org/commons/dbcp/">DBCP</a>, 
 * an Apache-Jakarta-Commons product.
 * </p>
 * 
 * @see DBConnectionManager
 * @see ConnectionProvider
 * 
 * @author Sharada Jambula
 * @author James House
 * @author Mohammad Rezaei
 */
public class PoolingConnectionProvider implements ConnectionProvider {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constants.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /** The JDBC database driver. */
    public static final String DB_DRIVER = "driver";

    /** The JDBC database URL. */
    public static final String DB_URL = "URL";

    /** The database user name. */
    public static final String DB_USER = "user";

    /** The database user password. */
    public static final String DB_PASSWORD = "password";

    /** The maximum number of database connections to have in the pool. */
    public static final String DB_MAX_CONNECTIONS = "maxConnections";

    /** 
     * The database sql query to execute everytime a connection is retrieved 
     * from the pool to ensure that it is still valid. 
     */
    public static final String DB_VALIDATION_QUERY = "validationQuery";
    
    /** Default maximum number of database connections in the pool. */
    public static final int DEFAULT_DB_MAX_CONNECTIONS = 10; 

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private BasicDataSource datasource;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public PoolingConnectionProvider(String dbDriver, String dbURL,
            String dbUser, String dbPassword, int maxConnections,
            String dbValidationQuery) throws SQLException {
        initialize(
            dbDriver, dbURL, dbUser, dbPassword, 
            maxConnections, dbValidationQuery);
    }

    /**
     * Create a connection pool using the given properties.
     * 
     * <p>
     * The properties passed should contain:
     * <UL>
     * <LI>{@link #DB_DRIVER}- The database driver class name
     * <LI>{@link #DB_URL}- The database URL
     * <LI>{@link #DB_USER}- The database user
     * <LI>{@link #DB_PASSWORD}- The database password
     * <LI>{@link #DB_MAX_CONNECTIONS}- The maximum # connections in the pool,
     * optional
     * <LI>{@link #DB_VALIDATION_QUERY}- The sql validation query, optional
     * </UL>
     * </p>
     * 
     * @param config
     *            configuration properties
     */
    public PoolingConnectionProvider(Properties config) throws SQLException {
        PropertiesParser cfg = new PropertiesParser(config);
        initialize(
            cfg.getStringProperty(DB_DRIVER), 
            cfg.getStringProperty(DB_URL), 
            cfg.getStringProperty(DB_USER, ""), 
            cfg.getStringProperty(DB_PASSWORD, ""), 
            cfg.getIntProperty(DB_MAX_CONNECTIONS, DEFAULT_DB_MAX_CONNECTIONS), 
            cfg.getStringProperty(DB_VALIDATION_QUERY));
    }
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */
    
    /**
     * Create the underlying DBCP BasicDataSource with the 
     * default supported properties.
     */
    private void initialize(
        String dbDriver, 
        String dbURL, 
        String dbUser,
        String dbPassword, 
        int maxConnections, 
        String dbValidationQuery) throws SQLException {
        if (dbURL == null) {
            throw new SQLException(
                "DBPool could not be created: DB URL cannot be null");
        }
        
        if (dbDriver == null) {
            throw new SQLException(
                "DBPool '" + dbURL + "' could not be created: " +
                "DB driver class name cannot be null!");
        }
        
        if (maxConnections < 0) {
            throw new SQLException(
                "DBPool '" + dbURL + "' could not be created: " + 
                "Max connections must be greater than zero!");
        }

        datasource = new BasicDataSource();
        datasource.setDriverClassName(dbDriver);
        datasource.setUrl(dbURL);
        datasource.setUsername(dbUser);
        datasource.setPassword(dbPassword);
        datasource.setMaxActive(maxConnections);
        if (dbValidationQuery != null) {
            datasource.setValidationQuery(dbValidationQuery);
        }
    }
    
    /**
     * Get the DBCP BasicDataSource created during initialization.
     * 
     * <p>
     * This can be used to set additional data source properties in a 
     * subclass's constructor.
     * </p>
     */
    protected BasicDataSource getDataSource() {
        return datasource;
    }

    public Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }
    
    public void shutdown() throws SQLException {
        datasource.close();
    }    
}
