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
* <p>
*/ 
package org.olat.test.util.selenium.olatapi.user;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * UserManagement/UserSettings abstraction.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class UserSettings extends OLATSeleniumWrapper {

	public UserSettings(Selenium selenium) {
		super(selenium);

		if(!selenium.isElementPresent("ui=userManagement::content_userdetail_roles()")) {
			throw new IllegalStateException("This is not the - UserSettings - page");
		}
	}

	/**
	 * Sets the first name and/or the visibility of it.
	 * @param firstname, could be null if the change is not desired.
	 * @param showOnVisitingCard, could be null if the change is not desired.
	 */
	public void setFirstName(String firstname, Boolean showOnVisitingCard) {	
		selectUserProfileTab();
		setTextInput("First name", firstname, showOnVisitingCard); 		
	}
	
	private void setTextInput(String inputLabel, String text, Boolean showOnVisitingCard) {
		if(text!=null) {
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=" + inputLabel + ")", text);
		}
		if(showOnVisitingCard!=null) {
			if(showOnVisitingCard) {
			  selenium.check("ui=commons::flexiForm_labeledCheckbox(formElementLabel=" + inputLabel + ")");
			} else {
				selenium.uncheck("ui=commons::flexiForm_labeledCheckbox(formElementLabel=" + inputLabel + ")");
			}
		}
		selenium.click("ui=commons::flexiForm_saveButton()");				
		selenium.waitForPageToLoad("30000");
	}
	
	public void setLastName(String lastname, Boolean showOnVisitingCard) {
		selectUserProfileTab();
		setTextInput("Last name", lastname, showOnVisitingCard); 		
	}
	
	public void setEmail(String email, Boolean showOnVisitingCard) {
		selectUserProfileTab();
		setTextInput("E-mail address", email, showOnVisitingCard); 
	}
	
	public String getEmail() {
	  selectUserProfileTab();	 
	  return selenium.getValue("ui=commons::flexiForm_labeledTextInput(formElementLabel=E-mail)");	  
	}
	
	public void setPassword(String newPassword) {
		selectChangePasswordTab();
		selenium.type("ui=userManagement::content_userdetail_changePasswordTab_newPassword()", newPassword);
		selenium.type("ui=userManagement::content_userdetail_changePasswordTab_confirmPassword()", newPassword);
		
		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");		
	}
	
	private void selectChangePasswordTab() {
		selenium.click("ui=userManagement::content_userdetail_changePassword()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Sets users roles. 
	 * If a parameter is null, the role state doesn't change.
	 * 
	 * @param isUserManager, if null, no change, else on/off accordingly with the boolean value.
	 * @param isGroupManager
	 * @param isAuthor
	 * @param isSysAdmin
	 * @param isLearningResourceAdmin
	 */
	public void setRoles(Boolean isUserManager, Boolean isGroupManager, Boolean isAuthor, 
			Boolean isSysAdmin, Boolean isLearningResourceAdmin) {
		
		selectRolesTab();
		if (isUserManager != null) {
			if (isUserManager) {
				selenium.check("ui=userManagement::content_userdetail_roles_isUsermanager()");
			} else {
				selenium.uncheck("ui=userManagement::content_userdetail_roles_isUsermanager()");
			}
		}
		if (isGroupManager != null) {
			if (isGroupManager) {
				selenium.check("ui=userManagement::content_userdetail_roles_isGroupmanager()");
			} else {
				selenium.uncheck("ui=userManagement::content_userdetail_roles_isGroupmanager()");
			}
		}
		if (isAuthor != null) {
			if (isAuthor) {
				selenium.check("ui=userManagement::content_userdetail_roles_isAuthor()");
			} else {
				selenium.uncheck("ui=userManagement::content_userdetail_roles_isAuthor()");
			}
		}
		if (isSysAdmin != null) {
			if (isSysAdmin) {
				selenium.check("ui=userManagement::content_userdetail_roles_isAdmin()");
			} else {
				selenium.uncheck("ui=userManagement::content_userdetail_roles_isAdmin()");
			}
		}
		if (isLearningResourceAdmin != null) {
			if (isLearningResourceAdmin) {
				selenium.check("ui=userManagement::content_userdetail_roles_isLearningResourceManager()");
			} else {
				selenium.uncheck("ui=userManagement::content_userdetail_roles_isLearningResourceManager()");
			}
		}

		selenium.click("ui=commons::flexiForm_saveButton()");
		selenium.waitForPageToLoad("30000");
	}
	
	private void selectRolesTab() {
		if(selenium.isElementPresent("ui=userManagement::content_userdetail_roles()")) {
		  selenium.click("ui=userManagement::content_userdetail_roles()");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	private void selectUserProfileTab() {
		if(selenium.isElementPresent("ui=userManagement::content_userdetail_userProfile()")) {
		  selenium.click("ui=userManagement::content_userdetail_roles()");
		  selenium.waitForPageToLoad("30000");
		}
	}
}
