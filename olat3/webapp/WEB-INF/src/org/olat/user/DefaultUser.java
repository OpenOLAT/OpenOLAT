/**
 * 
 */
package org.olat.user;

/**
 * 
 * @author guido
 * 
 */
public class DefaultUser {
	
	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String language;
	private boolean isGuest;
	private boolean isAuthor;
	private boolean isAdmin;
	private boolean isUserManager;
	private boolean isGroupManager;
	private String userName;
	
	/**
	 * creates the system default users
	 * 
	 * [only used by spring]
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param password
	 * @param language
	 * @param isGuest
	 * @param isAuthor
	 * @param isAdmin
	 */
	private DefaultUser(String userName) {
		this.userName = userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isGuest() {
		return isGuest;
	}

	public boolean isAuthor() {
		return isAuthor;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public boolean isUserManager() {
		return isUserManager;
	}

	public boolean isGroupManager() {
		return isGroupManager;
	}

	public String getUserName() {
		return userName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}

	public void setAuthor(boolean isAuthor) {
		this.isAuthor = isAuthor;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public void setUserManager(boolean isUserManager) {
		this.isUserManager = isUserManager;
	}

	public void setGroupManager(boolean isGroupManager) {
		this.isGroupManager = isGroupManager;
	}
	

}
