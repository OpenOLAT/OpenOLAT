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
import org.olat.test.util.selenium.olatapi.components.ChatComponent;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.course.run.Forum;

import com.thoughtworks.selenium.Selenium;

/**
 * @author Lavinia Dumitrescu
 *
 */
public class Group extends OLATSeleniumWrapper {
	
	private String groupName;

	/**
	 * @param selenium
	 */
	public Group(Selenium selenium, String groupName) {
		super(selenium);
		this.groupName = groupName;
		try {
			//delay at load group since 30.08.09
			Thread.sleep(10000);
		} catch (Exception e) {			
		}
    //Check that we're on the right place
		if(!selenium.isTextPresent(groupName)) { 
			throw new IllegalStateException("This is not the - Group - page");
		}
	}
	
	/**
	 * TODO: LD: check this out!!!
	 * Use carefully this method. 
	 * This starts a course if the group was achieved via the My groups portlet,
	 * else if the group was achieved via the GroupManager, this doesn't return
	 * a CourseRun.
	 * <p>
	 * It is assumed that there is only one course associated with this group.
	 * 
	 * @return Returns a CourseRun instance.
	 */
	public CourseRun startCourse() {
		selenium.click("ui=group::menu_course()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=group::content_startCourse()");
		selenium.waitForPageToLoad("30000");
		if(!selenium.isTextPresent("General")) {
			throw new IllegalStateException("This is not the - CourseRun - page, this Group was not selected via the My groups portlet");
		}
		return new CourseRun(selenium);
	}

	public void selectInfo() {
		selenium.click("ui=group::menu_information()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void selectCalendar() {
		selenium.click("ui=group::menu_calendar()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void selectMembers() {
		selenium.click("ui=group::menu_members()");
		selenium.waitForPageToLoad("30000");
	}
	
	public Forum selectForum() {
		selenium.click("ui=group::menu_forum()");
		selenium.waitForPageToLoad("30000");
		return new Forum(selenium);
	}
	
	public void selectEmail() {
		selenium.click("ui=group::menu_email()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void selectFolder() {
		selenium.click("ui=group::menu_folder()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void selectWiki() {
		selenium.click("ui=group::menu_wiki()");
		//outcommented selenium.waitForPageToLoad on 28.04.2010 to avoid getting the "Translation Issue encountered!" error
		selenium.waitForPageToLoad("30000");
	}
	
	public boolean hasWiki() {
		return selenium.isElementPresent("ui=group::menu_wiki()");
	}
	
	public GroupAdmin selectAdministration() {
		selenium.click("ui=group::menu_administration()");
		selenium.waitForPageToLoad("30000");
		return new GroupAdmin(selenium);
	}
	
	public ChatComponent selectChat() {
	  selenium.click("ui=group::menu_chat()");
    selenium.waitForPageToLoad("30000");
    return new ChatComponent(selenium);
	}
	
	public void close(String groupName) {    
    if(selenium.isElementPresent("ui=tabs::closeGroup(nameOfGroup=" + groupName + ")")) {
      selenium.click("ui=tabs::closeGroup(nameOfGroup=" + groupName + ")");
      selenium.waitForPageToLoad("30000");
    } else {
      System.out.println("Could not close this group!");
    }
  }
	
}
