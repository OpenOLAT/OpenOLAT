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
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Enrolment course run element.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class EnrolmentRun extends OLATSeleniumWrapper {
	
	private final String ENROLLED = "enrolled";
	

	/**
	 * @param selenium
	 */
	public EnrolmentRun(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=course::content_enrollment_enrolmentType()") && !selenium.isElementPresent("ui=course::content_contentElement()")) {
			throw new IllegalStateException("This is not the - Enrolment - page");
		}
	}

	public void enrol(String groupName) {		
	  selenium.click("ui=course::content_enrollment_enrolOnGroup(nameOfGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");		
	}
	
	public void cancelEnrolment(String groupName) {
		if(alreadyEnrolled(groupName)) {
		  selenium.click("ui=course::content_enrollment_cancelEnrolment(nameOfGroup=" + groupName + ")");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	public boolean alreadyEnrolled(String groupName) {
		return ENROLLED.equals(selenium.getText("ui=course::content_enrollment_enrolledOrNot(nameOfGroup=" + groupName + ")"));
	}
}
