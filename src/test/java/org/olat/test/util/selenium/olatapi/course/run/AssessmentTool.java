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

import java.util.Iterator;
import java.util.Map;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the AssessmentTool page abstraction.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class AssessmentTool extends OLATSeleniumWrapper  {

	 
	/**
	 * @param selenium
	 */
	public AssessmentTool(Selenium selenium) {
		super(selenium);

    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=course::assessment_selectType(text=As per user)")) {
			throw new IllegalStateException("This is not the - Assessment tool - page");
		}
	}

	/**
	 * Selects "As per user" if not already selected, selects user, and selects the courseElemTitle
	 * to get to the AssessmentForm.
	 * 
	 * @param userName
	 * @param courseElemTitle
	 * @return
	 */
	public AssessmentForm getAssessmentFormAsPerUser(String userName, String courseElemTitle) {
		getAssessmentTableAsPerUser(userName, courseElemTitle, false);
		selenium.click("ui=course::assessment_selectAssessmentCourseNode(title=" + courseElemTitle + ")");
		selenium.waitForPageToLoad("30000");
		return new AssessmentForm(selenium);
	}
	
	public String getScoreInTableAsPerUser(String userName, String courseElemTitle, boolean reloadTable) {
		getAssessmentTableAsPerUser(userName, courseElemTitle, reloadTable);
		return selenium.getText("ui=course::assessment_scoreInTable(title=" + courseElemTitle + ")");
	}
	
	public String getPassedStatusInTableAsPerUser(String userName, String courseElemTitle, boolean reloadTable) {
		getAssessmentTableAsPerUser(userName, courseElemTitle, reloadTable);
		return selenium.getText("ui=course::assessment_passedStatusInTable(title=" + courseElemTitle + ")");
	}
	
	public String getAttemptsInTableAsPerUser(String userName, String courseElemTitle, boolean reloadTable) {
		getAssessmentTableAsPerUser(userName, courseElemTitle, reloadTable);
		return selenium.getText("ui=course::assessment_attemptsInTable(title=" + courseElemTitle + ")");		
	}
	
	/**
	 * Go to the assessment table - via "As per user".
	 * @param userName
	 * @param courseElemTitle
	 * @param reloadTable - this is a workaround for an assessment tool bug. (the table doesn't update if a value changes
	 * on another cluster node)
	 */
	private void getAssessmentTableAsPerUser(String userName, String courseElemTitle, boolean reloadTable) {
		if(!selenium.isElementPresent("ui=course::assessment_tableFilterForm()") || reloadTable) {
		  selenium.click("ui=course::assessment_selectType(text=As per user)");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=course::assessment_selectUser(username=" + userName + ")");
		  selenium.waitForPageToLoad("10000");
		}
	}
	
	public void bulkAssessment(String courseElementTitle, Map<String,Integer> userScoreMap) {
		selenium.click("ui=course::assessment_selectType(text=Bulk assessment)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=course::assessment_startBulkAssessment()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=course::assessment_selectAssessmentCourseNodeInWizard(title=" + courseElementTitle + ")");
		selenium.waitForPageToLoad("30000");
		Iterator<String> userIterator = userScoreMap.keySet().iterator();
		String userScoreString = "";
		while(userIterator.hasNext()) {
			String user = userIterator.next();
			Integer score = userScoreMap.get(user);
			if(userScoreString.length()>0) {
				userScoreString += "\n";
			}
			userScoreString += user + "	" + score;
		}		
		System.out.println("userScoreString " + userScoreString);
		selenium.type("ui=course::assessment_bulkAssessmentValues()", userScoreString);
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=course::assessment_closeBulkAssessmentWizard()");
	}
	
	/**
	 * Closes the AssessmentTool.
	 * @return
	 */
	public CourseRun close() {
		selenium.click("ui=course::assessment_closeAssessmentTool()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}
	
}
