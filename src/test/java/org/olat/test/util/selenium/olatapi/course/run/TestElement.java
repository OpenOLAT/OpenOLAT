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
package org.olat.test.util.selenium.olatapi.course.run;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Test course run element.
 * @author Lavinia Dumitrescu
 *
 */
public class TestElement extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public TestElement(Selenium selenium) {
		super(selenium);
		// TODO : LD: add check: where am I?
	}

	/**
	 * Starts the current selected test.
	 * @return a TestRun instance.
	 */
	public TestRun startTest() {
		if(selenium.isElementPresent("ui=commons::start()")) {
			selenium.click("ui=commons::start()");
			selenium.waitForPageToLoad("30000");
			return new TestRun(selenium);
		} else {
			throw new IllegalStateException("This is not a test element - cannot start test!");
		}
	}
	
	public boolean cannotStartTestAnymore() {
		return !selenium.isElementPresent("ui=commons::start()");
	}
	
	/**
	 * 
	 * @return the achieved score, if any, throws IllegalStateException otherwise.
	 */
	public String getAchievedScore() {
		if(selenium.isElementPresent("ui=qti::yourScore()")) {
		  return selenium.getText("ui=qti::yourScore()");
		}
		throw new IllegalStateException("There is no score information to be displayed yet.");
	}
	
	public String getStatus() {
		if(selenium.isElementPresent("ui=qti::yourStatus()")) {
		  return selenium.getText("ui=qti::yourStatus()");
		}
		throw new IllegalStateException("There is no status information to be displayed yet.");
	}
	
	public boolean isShowResultsPresent() {
		return selenium.isElementPresent("ui=qti::showHideResults(text=Show results)");
	}
	
	public String getCommentFromTutor() {
		if(selenium.isElementPresent("ui=qti::commentFromTutor()")) {
		  return selenium.getText("ui=qti::commentFromTutor()");
		}
		System.out.println("No comment found");
		return "";
	}
}
