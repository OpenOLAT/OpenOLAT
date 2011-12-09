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
package org.olat.test.util.selenium.olatapi.user;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.SeleniumHelper;

import com.thoughtworks.selenium.Selenium;

/**
 * UserManagement abstraction.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class UserManagement extends OLATSeleniumWrapper {

	public UserManagement(Selenium selenium) {
		super(selenium);
		try { //loads slower since 29.01.2010
			Thread.sleep(5000);
		} catch (Exception e) {
			// nothing to do
		}
		// Check that we're on the right place	
		if(!selenium.isElementPresent("ui=userManagement::menu_userSearch()")) {
			throw new IllegalStateException("This is not the - UserManagement - page");
		}
	}
	
	/**
	 * 
	 * @param username
	 * @return Returns false if none found, else true if at least one found.
	 */
	public boolean searchUser(String username) {
		selectUserSearch();
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		if(selenium.isTextPresent("No user was found with these attributes")) {
			return false;
		} else if (selenium.isElementPresent("ui=commons::usertable_userlist_selectAll()")) {
			return true;
		}
		throw new IllegalStateException("The searchUser could not decide whether it find or not any user!");
	}
	
	/**
	 * Search and selects user
	 * @param username
	 * @return
	 */
	public UserSettings selectUser(String username) {
		boolean userFound = searchUser(username);
		if(userFound) {
		  selenium.click("ui=commons::usertable_userlist_selectUserName(nameOfUser=" + username + ")");
      selenium.waitForPageToLoad("30000");
    
      return new UserSettings(selenium);
		}
		throw new IllegalStateException("selectUser could not find any user!");
	}
	
	/**
	 * Checks that one cannot edit admin users.
	 * @param username
	 * @return Returns true if cannotEditUser message shows up
	 */
	public boolean cannotEditUser(String username) throws Exception {
		boolean userFound = searchUser(username);
		if(userFound) {
			selenium.click("ui=commons::usertable_userlist_selectUserName(nameOfUser=" + username + ")");
			selenium.waitForPageToLoad("30000");   
			return SeleniumHelper.isTextPresent(selenium, "You do not have enough rights to edit this user.", 20);			
		}
		throw new IllegalStateException("cannotEditUser could not find any user!");
	}
	
	/**
	 * Select the search user menu item.
	 */
	private void selectUserSearch() {
		selenium.click("ui=userManagement::menu_userSearch()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Creates user without check whether already exists.
	 * Language: EN.
	 * Should this check if user exists?
	 * @param username
	 * @param firstname
	 * @param lastname
	 * @param email
	 * @param password
	 */
	public UserSettings createUser(String username, String firstname, String lastname, String email, String password) {		
		selenium.click("ui=userManagement::menu_createUser()");
		selenium.waitForPageToLoad("30000");
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);			
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=First name)", firstname);		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Last name)", lastname);			
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=E-mail)", email);		
		selenium.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=Password)", password);
		//select EN
		selenium.select("ui=userManagement::content_createUser_language()",	"value=en");		
		selenium.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=Verify password)", password);
		selenium.click("ui=userManagement::content_createUser_save()");
		selenium.waitForPageToLoad("30000");
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			// nothing to do
		}
		assertTrue(selenium.isTextPresent("Manage user settings"));
		return new UserSettings(selenium);
	}
	
	/**
	 * Imports users, the parameter is a excel like string, see the GUI example.
	 * Imports only the new users. If at least one imported (new) returns true, else false.
	 * 
	 * @param rowsFromExcelString
	 * @return Returns true if all users could be imported.
	 */
	public boolean importAllUsers(String rowsFromExcelString) {
		startImportUsersWizard(rowsFromExcelString);
		
		//if at least one new user, the finish button is active
		if(selenium.isElementPresent("ui=userManagement::content_importUsers_finish()") && !selenium.isElementPresent("ui=userManagement::content_importUsers_importWarnIcon()")) {
			selenium.click("ui=userManagement::content_importUsers_finish()");
			selenium.waitForPageToLoad("30000");
		  //TODO:LD: add assert if user created: "New users successfully created!" ??? here?
			//SeleniumHelper.waitUntilTextPresent(selenium, "New users successfully created!", 60);		  
			return true;
		} 
		return false;
	}
	
	private void startImportUsersWizard(String rowsFromExcelString) {
		selenium.click("ui=userManagement::menu_importUsers()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=userManagement::content_importUsers_startUserImport()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=userManagement::content_importUsers_fillTextArea()", rowsFromExcelString);
		selenium.click("ui=userManagement::content_importUsers_next()");
		selenium.waitForPageToLoad("30000");
		assertTrue(selenium.isTextPresent("Preview of user data"));
	}
	
	/**
	 * Imports only the new users from the rowsFromExcelString. 
	 * It checks that there are already existing users in the input data set.
	 * 
	 * @param rowsFromExcelString
	 * @return Returns true if at least one user could be imported.
	 */
	public boolean importOnlyNewUsers(String rowsFromExcelString) {
		startImportUsersWizard(rowsFromExcelString);
			
		if(selenium.isElementPresent("ui=userManagement::content_importUsers_finish()") && selenium.isElementPresent("ui=userManagement::content_importUsers_importWarnIcon()")) {
			selenium.click("ui=userManagement::content_importUsers_finish()");
			selenium.waitForPageToLoad("30000");
		  //TODO:LD: add assert if user created: "New users successfully created!" ??? here?			
			return true;
		}
		return false;
	}
	
	/**
	 * Tries to import but no new users found.
	 * @param rowsFromExcelString
	 * @return Returns true if no new user found for import.
	 */
	public boolean importUsersExpectingError(String rowsFromExcelString) {
      startImportUsersWizard(rowsFromExcelString);
		
      if(!selenium.isElementPresent("ui=userManagement::content_importUsers_finish()")) {
		assertTrue(selenium.isTextPresent("There are no new users in this table."));
		selenium.click("ui=userManagement::content_importUsers_cancel()");
		selenium.waitForPageToLoad("30000");
		return true;
	  }		
      return false;
	}

	public void deleteUser(String username) {
		selenium.click("ui=userManagement::menu_deleteUser()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_checkUsernameToRemove(nameOfUser=" + username + ")");		
		selenium.click("ui=commons::usertable_sendUserDeletionEmail()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_adduser_finish()");
	  selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Deletes user if the user deletion workflow is not locked and if a user is found.
	 * It assumes that a user identified by username exists, so call searchUser beforehand.
	 * @param username
	 * @return Returns true only if the user was deleted.
	 */
	public boolean deleteUserImmediately(String username) {
		selenium.click("ui=userManagement::menu_deleteUserImmediately()");
		selenium.waitForPageToLoad("30000");	
		if(selenium.isTextPresent("The workflow regarding the deletion of users is being edited by")) {
			//workflow locked, cannot delete user
			return false;
		} else if(selenium.isElementPresent("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)")) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", username);	
		  selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
	      selenium.waitForPageToLoad("30000");
	      try {
	        Thread.sleep(1000);  
	      } catch (Exception e) {
	        // nothing to do
	      } 
	      if(selenium.isTextPresent("No user was found with these attributes. Please try again.")) {
	        throw new IllegalStateException("deleteUserImmediately could not find any user!");
	      }
		  selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser=" + username + ")");
		  selenium.click("ui=commons::usertable_adduser_choose()");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=dialog::Okay()");
		  selenium.waitForPageToLoad("90000");// looks like sometimes user delete takes rather long. increasing timeout to 90sec from 30sec
		  return true;
		}
		throw new IllegalStateException("deleteUserImmediately found a strange state: delete workflow is not locked but it doesn't find the delete form either!");
	}
	
	public void setUsersReplayURL(String usersNamesInSeparateLines) {
		selenium.click("ui=userManagement::menu_usersReplayURL()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=userManagement::content_usersReplayURL_userReplayTextArea()", usersNamesInSeparateLines);
		selenium.click("ui=commons::saveInput()");
		selenium.waitForPageToLoad("30000");
	}
}
