/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
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
	private boolean isSysAdmin;
	private String userName;
	
	/**
	 * creates the system default users
	 * 
	 * [only used by spring]
	 * 
	 * @param firstName
	 * @param lastName
	 * @param email
	 * @param cred
	 * @param language
	 * @param isGuest
	 * @param isAuthor
	 * @param isAdmin
	 */
	public DefaultUser(String userName) {
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

	public boolean isSysAdmin() {
		return isSysAdmin;
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

	public void setSysAdmin(boolean isSysAdmin) {
		this.isSysAdmin = isSysAdmin;
	}
}
