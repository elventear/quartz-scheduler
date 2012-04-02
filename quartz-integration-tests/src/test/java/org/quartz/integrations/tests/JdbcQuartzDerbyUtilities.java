package org.quartz.integrations.tests;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.quartz.utils.ConnectionProvider;

public final class JdbcQuartzDerbyUtilities {

    private static final String DATABASE_DRIVER_CLASS = "org.apache.derby.jdbc.ClientDriver";
    private static final String DATABASE_CONNECTION_PREFIX = "jdbc:derby://localhost:1527//tmp/bug.db;create=true";
    private static final List<String> DATABASE_SETUP_STATEMENTS;
    private static final List<String> DATABASE_TEARDOWN_STATEMENTS;
    
    private final static Properties PROPS = new Properties();
    static {
    	
    	PROPS.setProperty("user","quartz");
    	PROPS.setProperty("password","quartz");
    	
    	
        try {
            Class.forName(DATABASE_DRIVER_CLASS).newInstance();
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }

        List<String> setup = new ArrayList<String>();
        String setupScript;
        try {
            InputStream setupStream = PostgresConnectionProvider.class
                    .getClassLoader().getResourceAsStream("tables_derby.sql");
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(setupStream, "US-ASCII"));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    } else if (!line.startsWith("--")) {
                        sb.append(line).append("\n");
                    }
                }
                setupScript = sb.toString();
            } finally {
                setupStream.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        for (String command : setupScript.split(";")) {
            if (!command.matches("\\s*")) {
                setup.add(command);
            }
        }
        DATABASE_SETUP_STATEMENTS = setup;
        
        
        List<String> tearDown = new ArrayList<String>();
        String tearDownScript;
        try {
            InputStream tearDownStream = PostgresConnectionProvider.class
                    .getClassLoader().getResourceAsStream("tables_derby_drop.sql");
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(tearDownStream, "US-ASCII"));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    } else if (!line.startsWith("--")) {
                        sb.append(line).append("\n");
                    }
                }
                tearDownScript = sb.toString();
            } finally {
                tearDownStream.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        for (String command : tearDownScript.split(";")) {
            if (!command.matches("\\s*")) {
            	tearDown.add(command);
            }
        }
        DATABASE_TEARDOWN_STATEMENTS = tearDown;
        
        
    }

    public static void createDatabase() throws SQLException {
//        DBConnectionManager.getInstance().addConnectionProvider(name,
//                new PostgresConnectionProvider(name));
    	Connection conn = DriverManager.getConnection(DATABASE_CONNECTION_PREFIX ,PROPS);
        try {
            Statement statement = conn.createStatement();
            for (String command : DATABASE_SETUP_STATEMENTS) {
                statement.addBatch(command);
            }
            statement.executeBatch();
        }
        finally {
            conn.close();
        }
    }

    
	public static int triggersInAcquiredState() throws SQLException {
		int triggersInAcquiredState = 0;
		Connection conn = DriverManager.getConnection(DATABASE_CONNECTION_PREFIX, PROPS);
		try {
			Statement statement = conn.createStatement();
			ResultSet result = statement.executeQuery("SELECT count( * ) FROM QRTZ_TRIGGERS WHERE TRIGGER_STATE = 'ACQUIRED' ");
			while (result.next()) { 
				triggersInAcquiredState = result.getInt(1);
			}
		} finally {
			conn.close();
		}
		return triggersInAcquiredState;
	}
    
    public static void destroyDatabase() throws SQLException {
    	Connection conn = DriverManager.getConnection(DATABASE_CONNECTION_PREFIX ,PROPS);
        try {
            Statement statement = conn.createStatement();
            for (String command : DATABASE_TEARDOWN_STATEMENTS) {
                statement.addBatch(command);
            }
            statement.executeBatch();
        }
        finally {
            conn.close();
        }
    }

    static class PostgresConnectionProvider implements ConnectionProvider {



        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DATABASE_CONNECTION_PREFIX , PROPS);
        }

        public void shutdown() throws SQLException {
            // nothing to do
        }
    }

    private JdbcQuartzDerbyUtilities() {
        // not instantiable
    }
}
