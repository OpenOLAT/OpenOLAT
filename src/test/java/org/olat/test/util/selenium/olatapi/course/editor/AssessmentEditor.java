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
package org.olat.test.util.selenium.olatapi.course.editor;

import com.thoughtworks.selenium.Selenium;

/**
 * This is the Assessment Course Element page.
 * @author Lavinia Dumitrescu
 *
 */
public class AssessmentEditor extends CourseElementEditor {

	/**
	 * @param selenium
	 */
	public AssessmentEditor(Selenium selenium) {
		super(selenium);
		
    //	Check that we're on the right place		
		if(!selenium.isElementPresent("ui=courseEditor::content_bbAssessment_tabAssessment()")) {
			throw new IllegalStateException("This is not the - Assessment course element - page");
		}
	}

	/**
	 * By default the scoreGranted is "No" for a new AssessmentCourseElement.
	 * @param scoreGranted
	 * @param minScore
	 * @param maxScore
	 * @param typeOfDisplayAuto
	 * @param passedCutValue
	 */
	public void configure(Boolean scoreGranted, int minScore, int maxScore, Boolean typeOfDisplayAuto, int passedCutValue) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbAssessment_tabAssessment()")) {
			selenium.click("ui=courseEditor::content_bbAssessment_tabAssessment()");
			selenium.waitForPageToLoad("30000");
		}
		if(scoreGranted!=null && scoreGranted) {
		  if(selenium.isElementPresent("ui=courseEditor::content_assessmentElemConfig_scoreGranted()") && !selenium.isChecked("ui=courseEditor::content_assessmentElemConfig_scoreGranted()")) {
		    selenium.click("ui=courseEditor::content_assessmentElemConfig_scoreGranted()");		 
		  }		
		  selenium.waitForPageToLoad("30000");
		  selenium.type("ui=courseEditor::content_assessmentElemConfig_minimumScore()", String.valueOf(minScore));		
		  selenium.type("ui=courseEditor::content_assessmentElemConfig_maximumScore()", String.valueOf(maxScore));
		  if(typeOfDisplayAuto!=null && typeOfDisplayAuto) {
			  //selenium.check("ui=courseEditor::content_assessmentElemConfig_passedTypeAuto()");	//WARNING: check DOES NOT WORK!!!
			  selenium.click("ui=courseEditor::content_assessmentElemConfig_passedTypeAuto()");	
			  sleepThread(3000);
			  if(selenium.isElementPresent("ui=courseEditor::content_assessmentElemConfig_passedCutValue()")) {
			    selenium.type("ui=courseEditor::content_assessmentElemConfig_passedCutValue()", String.valueOf(passedCutValue));
			    System.out.println("passedCutValue setted");
			  } else if(selenium.isElementPresent("ui=courseEditor::content_assessmentElemConfig_passedCutValueErr()")) {
				selenium.type("ui=courseEditor::content_assessmentElemConfig_passedCutValueErr()", String.valueOf(passedCutValue));
				System.out.println("passedCutValue setted");
			  }
			}
		} else {
			//default "No"
		}
		sleepThread(3000);
		selenium.click("ui=commons::flexiForm_saveButton()");				
		selenium.waitForPageToLoad("30000");
	}
}
