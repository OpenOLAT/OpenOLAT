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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.test.util.selenium.olatapi;


import org.olat.test.util.selenium.olatapi.admin.Administration;
import org.olat.test.util.selenium.olatapi.components.ChatComponent;
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.selenium.olatapi.home.Home;
import org.olat.test.util.selenium.olatapi.lr.LearningResources;
import org.olat.test.util.selenium.olatapi.user.UserManagement;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the entry point for the OLAT abstraction around the selenium commands.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class OLATWorkflowHelper extends OLATSeleniumWrapper {
	
	/**
	 * @param selenium
	 */
	public OLATWorkflowHelper(Selenium selenium) {
		super(selenium);		
	}

	/**
	 * Select the Learning resources tab and return the corresponding abstraction.
	 * @param selenium
	 */
	public LearningResources getLearningResources() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {			
		}
		selenium.click("ui=tabs::learningResources()");
		selenium.waitForPageToLoad("30000");
		return new LearningResources(selenium); 
	}
	
	/**
	 * Selects the Groups tab and returns the corresponding abstraction.
	 * @return
	 */
	public Groups getGroups() {
		selenium.click("ui=tabs::groups()");
		selenium.waitForPageToLoad("30000");
    //click show all groups, if "Show all" link present.
		if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		}		
		return new Groups(selenium);
	}
	/**
	 * Select the Home tab and return the corresponding abstraction.
	 * @param selenium
	 */
	public Home getHome() {
		//we are on Home just after login
		if(selenium.isTextPresent("Welcome to OLAT")) {
		  return new Home(selenium);
		}
		//if not already on Home, go to Home
		selenium.click("ui=tabs::home()");
		selenium.waitForPageToLoad("30000");
		return new Home(selenium);
	}
	
	public UserManagement getUserManagement() {
		selenium.click("ui=tabs::userManagement()");
		selenium.waitForPageToLoad("30000");
		return new UserManagement(selenium);
	}
	
	public Administration getAdministration() {
		selenium.click("ui=tabs::administration()");
    	selenium.waitForPageToLoad("30000");
    	return new Administration(selenium);
	}
	
	/*public HelpCourse getHelp() throws Exception{		
		selenium.click("ui=home::topNav_olatHelp()");				
		Thread.sleep(10000);		
		
		//select window		
		System.out.println(" title: " + selenium.getAllWindowTitles()[0]);
		System.out.println(" title: " + selenium.getAllWindowTitles()[1]);		
		selenium.selectWindow(selenium.getAllWindowTitles()[0]);
		
		Thread.sleep(10000);	
		return new HelpCourse(selenium);
	}*/
	
		
	public void logout() {
		selenium.click("ui=tabs::logOut()");
		selenium.waitForPageToLoad("30000");
	}
	
	public ChatComponent getChatComponent() {
	  return new ChatComponent(selenium);
	}
}
