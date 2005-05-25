/*
 * Created on May 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.quartz.ui.web.security;

import java.util.Map;

/**
 * @author Matthew Payne
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Users {
	 private Map userMap; 	
	
	 public User getUser(String name) {
	 	
	 	return (User) userMap.get(name);
	 	
	 	
	 }
	 
	/**
	 * @return Returns the userMap.
	 */
	public Map getUserMap() {
		return userMap;
	}
	/**
	 * @param userMap The userMap to set.
	 */
	public void setUserMap(Map userMap) {
		this.userMap = userMap;
	}
}
