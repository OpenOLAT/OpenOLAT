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

import com.thoughtworks.selenium.Selenium;

/**
 * This is the EvidencesOfAchievement page.
 * @author Lavinia Dumitrescu
 *
 */
public class EvidencesOfAchievement extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public EvidencesOfAchievement(Selenium selenium) {
		super(selenium);
		// TODO check for label
	}

	/**
	 * Get the Passed status for the courseName.
	 * @param courseName
	 * @return
	 */
	public String getCoursePassedStatus(String courseName) {
		return selenium.getText("ui=home::content_evidencesOfAchievement_passedStatus(title=" + courseName + ")");
	}
	
	/**
	 * Starts course.
	 * @return
	 */
	public CourseRun startCourse(String title) {
		selenium.click("ui=home::content_evidencesOfAchievement_startCourse(title=" + title + ")");		
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}
	/**
	 * Selects the details page for the given course.
	 * @param courseName
	 * @throws Exception
	 */
	public void selectDetails(String courseName) throws Exception {
		//if too many entries found - show all
	  	if(selenium.isElementPresent("ui=commons::table_showAll()")) {
		  selenium.click("ui=commons::table_showAll()");
		  selenium.waitForPageToLoad("30000");
		}  
		selenium.click("ui=home::content_evidencesOfAchievement_selectDetails(title=" + courseName + ")");	
		Thread.sleep(10000);		
		selenium.selectWindow(selenium.getAllWindowTitles()[2]); 		
		assertTrue(selenium.isElementPresent("ui=home::content_evidencesOfAchievement_evidenceOfAchievement()"));
	}
	
	/**
	 * Set the score for the courseElemTitle in the EvidenceOfAchievement details.
	 * @param courseElemTitle
	 * @return
	 */
	public String getCourseElementScore(String courseElemTitle) {		
		return selenium.getText("ui=course::assessment_scoreInTable(title=" + courseElemTitle + ")");
	}
	
	/**
	 * Get the passed status for the courseElemTitle in the EvidenceOfAchievement details.
	 * @param courseElemTitle
	 * @return
	 */
	public String getCourseElementPassedStatus(String courseElemTitle) {		
		return selenium.getText("ui=course::assessment_passedStatusInTable(title=" + courseElemTitle + ")");
	}
	
	public String getCourseElementAttempts(String courseElemTitle) {
		return selenium.getText("ui=course::assessment_attemptsInTable(title=" + courseElemTitle + ")");
	}
	
	/**
	 * delete evidences of achievement for the courses containing courseName.
	 * @param courseName
	 */
	public void deleteAll(String courseName) {
		while(selenium.isElementPresent("ui=home::content_evidencesOfAchievement_delete(title=" + courseName + ")")) {
			System.out.println("Delete link found, delete evidence of achievement!");
			selenium.click("ui=home::content_evidencesOfAchievement_delete(title=" + courseName + ")");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=dialog::Yes()");
			selenium.waitForPageToLoad("30000");
		}		
	}
}
