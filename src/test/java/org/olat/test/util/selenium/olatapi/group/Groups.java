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
package org.olat.test.util.selenium.olatapi.group;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This represents the Groups tab.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class Groups extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public Groups(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isTextPresent("Groups")) {
			throw new IllegalStateException("This is not the - Groups - page");
		}
	}

	/**
	 * 
	 * @param groupName
	 * @param groupDescription
	 * @return
	 */
	public GroupAdmin createProjectGroup(String groupName, String groupDescription) {
		selenium.click("ui=groups::toolbox_create_projectGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Group name)", groupName);
	  //the description shows up in an iframe
		selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.type("ui=commons::tinyMce_styledTextArea()", groupDescription);
		selenium.selectFrame("relative=top");	
		
		selenium.click("ui=commons::flexiForm_finishButton()");
		selenium.waitForPageToLoad("30000");
		return new GroupAdmin(selenium);
	}
	
	/**
	 * Deletes group with the given name.
	 * @param groupName
	 */
	public void deleteGroup(String groupName) {
		selenium.click("ui=groups::content_deleteGroup(nameOfGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_deleteYes()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Leave group.
	 * @param groupName
	 */
	public void leaveGroup(String groupName) {
		selenium.click("ui=groups::content_leaveGroup(nameOfGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groups::content_leaveYes()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @param groupName
	 * @return Returns the selected group.
	 */
	public Group selectGroup(String groupName) {
		selenium.click("ui=groups::content_clickGroupEntry(nameOfGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");
		return new Group(selenium, groupName);
	}
	
	public boolean hasGroup(String groupName) {
	  if(selenium.isElementPresent("ui=groups::content_clickGroupEntry(nameOfGroup=" + groupName + ")")) {
	    return true;
	  }
	  return false;
	}
	
}
