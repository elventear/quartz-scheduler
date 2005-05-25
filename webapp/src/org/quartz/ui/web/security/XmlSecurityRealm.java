package org.quartz.ui.web.security;

import org.securityfilter.realm.SimpleSecurityRealmBase;
//import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple Xml implementation of the SecurityRealmInterface.
 *
 * There is one user: username is 'username', password is 'password'
 * And this user is in one role: 'inthisrole'
 *
 * @author Matthew Payne (matthew.payne@sutternow.com)
 * @version $Revision$ $Date$
 */
public class XmlSecurityRealm extends SimpleSecurityRealmBase {

	 Users users;
	   
	
	public XmlSecurityRealm() {
		super();
		//users = (Users) ctx.getBean("users");
	}
	
	private String username="";
	private String password="";
	
/**
 * @return Returns the users.
 */
public Users getUsers() {
	return users;
}
/**
 * @param users The users to set.
 */
public void setUsers(Users users) {
	this.users = users;
}
 
   //ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext.xml");
   
   private String exampleProperty;

   /**
    * Authenticate a user.
    *
    * Implement this method in a subclass to avoid dealing with Principal objects.
    *
    * @param username a username
    * @param password a plain text password, as entered by the user
    *
    * @return null if the user cannot be authenticated, otherwise a Pricipal object is returned
    */
   public boolean booleanAuthenticate(String username, String password) {
    //System.out.println("checking user\'" + username+ " with pass\'" + password);

   	if (this.username.equals(username) && this.password.equals(password)) {
   		return true;
   	}
   	
    if (users == null) {
    	return false;
    }
   	
   	User user = users.getUser(username);
		if (user == null) {
			return false;
		} else {
			return user.getPassword().equals(password);
		}
   }

   /**
    * Test for role membership.
    *
    * Implement this method in a subclass to avoid dealing with Principal objects.
    *
    * @param username The name of the user
    * @param role name of a role to test for membership
    * @return true if the user is in the role, false otherwise
    */
   public boolean isUserInRole(String username, String role) {
    //System.out.println("checking user\'" + username+ " for\'" + role);
   	
   	// no roles have been provided
   	if (users==null) {
   		return true;
   		
   	}
   	
   	 User user = users.getUser(username);
      		
      		if (user == null) {
      			return false;
      		} else {
      			return user.getRoles().containsKey(role);
      		}
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
	 * @return Returns the username.
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}

// ----------------------------------------------------------------------------
// EOF
