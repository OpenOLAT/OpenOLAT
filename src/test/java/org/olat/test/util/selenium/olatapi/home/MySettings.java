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
package org.olat.test.util.selenium.olatapi.home;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * The user settings in Home.
 * @author Lavinia Dumitrescu
 *
 */
public class MySettings extends OLATSeleniumWrapper {

	public MySettings(Selenium selenium) {
		super(selenium);

		if(!selenium.isElementPresent("ui=home::content_settings_tabs_password()")) {
			throw new IllegalStateException("This is not the - UserSettings - page");
		}
	}

	/**
	 * 
	 * @param newPassword
	 */
	public void setPassword(String newPassword) {
		selectPasswordTab();
		selenium.type("ui=userManagement::content_userdetail_changePasswordTab_newPassword()", newPassword);
		selenium.type("ui=userManagement::content_userdetail_changePasswordTab_confirmPassword()", newPassword);
		
		selenium.click("ui=commons::saveInput()");
		selenium.waitForPageToLoad("30000");		
	}
	
	private void selectPasswordTab() {
		if(selenium.isElementPresent("ui=home::content_settings_tabs_password()")) {
		  selenium.click("ui=home::content_settings_tabs_password()");
	    selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Sets email and/or showOnVisitingCard status.
	 * @param email
	 * @param showOnVisitingCard
	 */
	public void setEmail(String email, Boolean showOnVisitingCard) {
		selectProfileTab();
		setTextInput("E-mail", email, showOnVisitingCard); 
		selenium.click("ui=home::content_settings_profile_yesLink()");
		selenium.waitForPageToLoad("30000");		
	}
	
	/**
	 * 
	 * @return
	 */
	public String getEmail() {		
		selectProfileTab();
		return getText("E-mail");
	}
	
	/**
	 * Gets the value for the labeled input element.
	 * @param label
	 * @return
	 */
	public String getText(String label) {
		return selenium.getValue("ui=commons::flexiForm_labeledTextInput(formElementLabel=" + label + ")");
	}
	
	/*public String getDisabledText(String label) {
		return selenium.getValue("ui=commons::flexiForm_disabledLabeledTextInput(formElementLabel=" + label + ")");
	}*/
	
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
	
	/**
	 * If showOnVisitingCard!=null, check/uncheck the show on visit card checkbox, depending on the boolean value.
	 * @param showOnVisitingCard
	 */
	public void showFirstNameOnVisitingCard(Boolean showOnVisitingCard) {
		selectProfileTab();
		checkShowOnVisitingCard("First name", showOnVisitingCard); 		
	}
		
	
	private void checkShowOnVisitingCard(String inputLabel,Boolean showOnVisitingCard) {
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
	
	/**
	 * Check if the input test is present as value for a disabled input field: e.g. firstname, lastname.
	 * @param text
	 * @return
	 */
	public boolean isDisabledTextPresent(String text) {
		return selenium.isElementPresent("ui=commons::flexiForm_disabledTextInput(inputValue=" + text + ")");
	}
	
	/**
	 * Selects the System tab.
	 */
	private void selectSystemTab() {
		if(selenium.isElementPresent("ui=home::content_settings_tabs_system()")) {
		  selenium.click("ui=home::content_settings_tabs_system()");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Selects the profile tab, if not already there.
	 */
	private void selectProfileTab() {
		if(selenium.isElementPresent("ui=home::content_settings_tabs_profile()")) {
		  selenium.click("ui=home::content_settings_tabs_profile()");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Select System tab, if not already there, set language and save.
	 * @param languageLabel
	 */
	public void selectSystemLanguage(String languageLabel) {
		selectSystemTab();
		selenium.select("language", "label=" + languageLabel);
		selenium.click("ui=home::content_settings_system_general_save()");
	  selenium.waitForPageToLoad("30000");
	}
	
	
}
