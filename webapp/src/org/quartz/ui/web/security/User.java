package org.quartz.ui.web.security;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthew Payne
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class User {
	
	String userName;
	String password;
	
	String email;
	String lastName;
	String firstName;

	Map roles;

	public User() {
		roles = new HashMap();
	}
	
	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * @return Returns the roles.
	 */
	public Map getRoles() {
		return roles;
	}
	/**
	 * @param roles The roles to set.
	 */
	public void setRoles(Map roles) {
		this.roles = roles;
	}
}
