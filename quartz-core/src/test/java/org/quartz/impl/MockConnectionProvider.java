package org.quartz.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.quartz.utils.ConnectionProvider;

/**
 * Mock implementation of a ConnectionProvider
 * that keeps track of the order of it methods calls
 * 
 * @author adahanne
 */
public class MockConnectionProvider implements ConnectionProvider {

	private String customProperty; 
	public static List<String> methodsCalled = new ArrayList<String>();
	
	public Connection getConnection() throws SQLException {
		methodsCalled.add("getConnection");
		throw new MockSQLException("getConnection correctly called on MockConnectionProvider");
	}

	public void shutdown() throws SQLException {
	}

	public void initialize() throws SQLException {
		methodsCalled.add("initialize");

	}

	public void setCustomProperty(String customProperty) {
		methodsCalled.add("setCustomProperty("+customProperty+")");
	}
	
}

class MockSQLException extends SQLException{
	public MockSQLException(String string) {
		super(string);
	}
	
}
