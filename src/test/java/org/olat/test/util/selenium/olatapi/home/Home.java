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
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;
import org.olat.test.util.selenium.olatapi.folder.Folder;
import org.olat.test.util.selenium.olatapi.group.Group;
import org.olat.test.util.selenium.olatapi.portfolio.EPExtensions;

import com.thoughtworks.selenium.Selenium;

/**
 * @author Lavinia Dumitrescu
 *
 */
public class Home extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public Home(Selenium selenium) {
		super(selenium);
		
    //Check that we're on the right place
		if(!selenium.isTextPresent("Welcome to OLAT")) {
			throw new IllegalStateException("This is not the - Home - page");
		}
	}

	/**
	 * Select the group in MyGroups portlet with the given name.
	 * @param title
	 * @return
	 */
	public Group selectMyGroup(String title) {
		//increase number of entries for the "My groups" portlet
		getHomeConfigurator().configMyGroupPortlet(99, HomeConfigurator.SORT_TYPE.ALPHABET, true);
		
		selenium.click("ui=home::content_portlets_myGroups(nameOfGroup=" + title + ")");
		selenium.waitForPageToLoad("30000");
		return new Group(selenium, title);
	}
	
	/**
	 * Select the course in MyBookmarks portlets with the given title.
	 * @param title
	 * @return
	 */
	public CourseRun selectMyBookmarkedCourse(String title) {
		//increase number of entries for the "My bookmarks" portlet
		getHomeConfigurator().configMyBookmarkPortlet(99, HomeConfigurator.SORT_TYPE.ALPHABET, true);
		
		selenium.click("ui=home::content_portlets_myBookmarks(nameOfBookmark=" + title + ")");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}
	
	/**
	 * Selects the EvidencesOfAchievement.
	 * @return Returns the EvidencesOfAchievement page.
	 */
	public EvidencesOfAchievement getEvidencesOfAchievement() {
		selenium.click("ui=home::menu_evidencesOfAchievement()");
		selenium.waitForPageToLoad("30000");
		return new EvidencesOfAchievement(selenium);
	}
	
	public MySettings getUserSettings() {
		selenium.click("ui=home::menu_settings()");
		selenium.waitForPageToLoad("30000");
		return new MySettings(selenium);
	}
	
	public Folder getPersonalFolder() {
		selenium.click("ui=home::menu_personalFolder()");
		selenium.waitForPageToLoad("30000");
		return new Folder(selenium);
	}
	
	public HomeConfigurator getHomeConfigurator() {
		selenium.click("ui=home::config_editConfig()");
		selenium.waitForPageToLoad("30000");
		return new HomeConfigurator(selenium);
	}
	
	public EPExtensions getEPortfolio(){
		selenium.click("ui=home::menu_ePortfolio()");
		selenium.waitForPageToLoad("30000");
		return new EPExtensions(selenium);
	}
}
