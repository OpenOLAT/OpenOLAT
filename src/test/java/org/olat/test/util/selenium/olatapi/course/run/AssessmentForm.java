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
 * This is the AssessmentForm page abstraction.
 * TODO: LD: add methods for comments, and test if setComments/getComments work!
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class AssessmentForm extends OLATSeleniumWrapper {

	public static final String PASSED_NO_INFO = "undefined";
	public static final String PASSED_YES = "true";
	public static final String PASSED_NO = "false";
	
	/**
	 * @param selenium
	 */
	public AssessmentForm(Selenium selenium) {
		super(selenium);
		
    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=commons::flexiForm_labeledTextInput(formElementLabel=Score)") 
				&& !selenium.isElementPresent("ui=course::assessment_setPassedYes()")) {
			//no set score and no set passed present
			throw new IllegalStateException("This is not the - Assessment form - page");
		}
	}

	/**
	 * Only type the score.
	 * Call save to commit the change.
	 * 
	 * @param score
	 */
	public void setScore(double score) {		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Score)", String.valueOf(score));
	}
	
	public String getScore() {
		return selenium.getValue("ui=commons::flexiForm_labeledTextInput(formElementLabel=Score)");		
	}
	
	public String getMinScore() {
		return selenium.getText("ui=course::assessment_minMaxScore(title=Minimum score)");		
	}
	
	public String getMaxScore() {
		return selenium.getText("ui=course::assessment_minMaxScore(title=Maximum score)");		
	}
	
	public String getPassedCutScore() {
		return selenium.getText("ui=course::assessment_minMaxScore(title=Passed cut value)");		
	}
	
	/**
	 * Only set passed info. 
	 * Call save to commit the change.
	 * 
	 * @param passed
	 */
	public void setPassed(Boolean passed) {
		if(passed==null) {
			selenium.click("ui=course::assessment_setPassedNoInfo()");
		} else if(passed) {
		  selenium.click("ui=course::assessment_setPassedYes()");
		} else {			
			selenium.click("ui=course::assessment_setPassedNo()");
		}
	}
	
	public String getPassed() {
		if(selenium.isChecked("ui=course::assessment_setPassedYes()"))
			return PASSED_YES;
		if(selenium.isChecked("ui=course::assessment_setPassedNo()"))
			return PASSED_NO;
		
		return PASSED_NO_INFO;
	}
	
	public void setAttempts(int attempts) {
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Number of attempts)", String.valueOf(attempts));
	}
	
	public String getAttempts() {
		return  selenium.getValue("ui=commons::flexiForm_labeledTextInput(formElementLabel=Number of attempts)");
	}
	
	public void setUserComments(String text) {
		selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Comments for users)", String.valueOf(text));
	}
	
	public String getUserComment() {
		return selenium.getValue("ui=commons::flexiForm_labeledTextArea(formElementLabel=Comments for users)");
	}
	
	public void setCoachComments(String text) {
		selenium.type("ui=course::assessment_coachComment()", String.valueOf(text));
	}
	
	public String getCoachComment() {
		return selenium.getValue("ui=course::assessment_coachComment()");
	}
	
	/**
	 * Saves changes.
	 * @return
	 */
  public AssessmentTool save() {
  	selenium.click("ui=commons::save()");
		selenium.waitForPageToLoad("30000");
  	return new AssessmentTool(selenium);
  }
  
  /**
   * Close AssessmentTool and get to the courseRun.
   * @return
   */
  public CourseRun close() {
  	selenium.click("ui=course::assessment_closeAssessmentTool()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
  }
	
}
