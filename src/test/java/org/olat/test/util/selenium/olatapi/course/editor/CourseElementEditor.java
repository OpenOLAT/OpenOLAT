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

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Covers the common tabs of the course elements editor
 * ("Title and description", "Visibility", and "Access" tabs).
 * <p>
 * 
 * 
 * @author Lavinia Dumitrescu
 * 
 */
public class CourseElementEditor extends OLATSeleniumWrapper {
  
  public enum ACCESS_TYPE {ACCESS, READ_ONLY, READ_AND_WRITE, MODERATE, CREATE_EDIT_WIKI, PRESENT}//MISSING FOR FILE_DIALOG, TASK, TOPIC_ASSIGNMENT,CALENDAR

	/**
	 * @param selenium
	 */
	protected CourseElementEditor(Selenium selenium) {
		super(selenium);
		sleepThread(3000);
		// Check that we're on the right place
		if (!selenium.isElementPresent("ui=courseEditor::content_TitleDescription_shortTitle()") 
				&& !selenium.isElementPresent("ui=courseEditor::content_undeleteCourseElement()")) {
			throw new IllegalStateException("This is not the - Course element - page");
		}
	}

	public void setTitle(String title) {
		selenium.type("ui=courseEditor::content_TitleDescription_shortTitle()",	title);
		selenium.click("ui=courseEditor::content_TitleDescription_save()");
		selenium.waitForPageToLoad("30000");
	}
	
	public void setDescription(String description) {
		this.typeInRichText(description);
		selenium.click("ui=commons::flexiForm_saveButton()");				
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Changes visibility the current selected course element.
	 * 
	 * @param groupName
	 */
	public void changeVisibilityDependingOnGroup(String groupName) {
		selectVisibilityTab();
		selenium.click("ui=courseEditor::content_visibility_dependingGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_visibility_selectLearningGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_checkGroup(nameOfGroup="	+ groupName + ")");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_apply()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_visibility_save()");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Changes access to the current selected course element, depending on
	 * assessment.
	 * 
	 * @param title
	 */
	public void changeVisibilityDependingOnAssessment(String title) {
		selectVisibilityTab();
		selenium.click("ui=courseEditor::content_visibility_dependingAssessment()");
		selenium.waitForPageToLoad("30000");
		selenium.select("ui=courseEditor::content_visibility_selectElement()", "label=" + title);
		selenium.click("ui=courseEditor::content_visibility_save()");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Changes access to the current selected course element, block for learners. <br/>
	 * Switches state.
	 * 
	 */
	public void changeVisibilityBlockForLearners() {
		selectVisibilityTab();
		selenium.click("ui=courseEditor::content_visibility_blockedForLearners()");
		selenium.waitForPageToLoad("30000");
		//Save button is no more visible in olat7
		if(selenium.isElementPresent("ui=courseEditor::content_visibility_save()")) {
		  selenium.click("ui=courseEditor::content_visibility_save()");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	private void selectVisibilityTab() {
		if (selenium.isElementPresent("ui=courseEditor::content_visibility_tabVisibility()")) {
			selenium.click("ui=courseEditor::content_visibility_tabVisibility()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	private void selectAccessTab() {
		if (selenium.isElementPresent("ui=courseEditor::content_access_tabAccess()")) {
			selenium.click("ui=courseEditor::content_access_tabAccess()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	
	public boolean changeVisibilityExpertMode(boolean swichToExpertMode, String appendCondition) {
		selectVisibilityTab();
		return appendCondition(swichToExpertMode, appendCondition);
	}
	
	public boolean changeAccessExpertMode(boolean swichToExpertMode,String appendCondition) {
		selectAccessTab();
		return appendCondition(swichToExpertMode, appendCondition);
	}
	
	/**
	 * Append condition to the expert mode.
	 * Cases:
	 * 1. append condition only if the expert mode is already selected,
	 * 2. append condition in any case, that is select the expert mode before. 
	 * 
	 * @param swichToExpertMode
	 * @param appendCondition
	 */
	private boolean appendCondition(boolean swichToExpertMode, String appendCondition) {
		if(swichToExpertMode && selenium.isElementPresent("ui=courseEditor::content_visibilityOrAccess_displayExpertMode()")) {
		  selenium.click("ui=courseEditor::content_visibility_displayExpertMode()");
		  selenium.waitForPageToLoad("30000");
		}
		if(selenium.isElementPresent("ui=courseEditor::content_visibilityOrAccess_displaySimpleMode()")) {		
		  String expertRuleString = selenium.getValue("ui=commons::flexiForm_labeledTextArea(formElementLabel=Expert rule)");
		  if(expertRuleString!=null && !expertRuleString.equals("")) {
			expertRuleString += appendCondition;
			selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=Expert rule)", expertRuleString);
			//selenium.click("ui=commons::saveInput()");
			if(selenium.isElementPresent("ui=courseEditor::content_visibilityOrAccess_saveVisibilityExpertRule()")) {
				selenium.click("ui=courseEditor::content_visibilityOrAccess_saveVisibilityExpertRule()");
			} else if(selenium.isElementPresent("ui=courseEditor::content_visibilityOrAccess_saveAccessExpertRule()")) {
				selenium.click("ui=courseEditor::content_visibilityOrAccess_saveAccessExpertRule()");
			} else {
				System.out.println("Warning: no save button found, could not save expert rule!");
			}
			selenium.waitForPageToLoad("30000");
			return true;
			}
		}
		return false;
	}
	
	

	/**
	 * Changes access to the current selected course element depending on the
	 * input group.
	 * 
	 * @param groupName
	 */
	public void changeAccessyDependingOnGroup(String groupName) {
		selectAccessTab();
		selenium.click("ui=courseEditor::content_access_dependingGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_access_selectLearningGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_checkGroup(nameOfGroup="	+ groupName + ")");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_apply()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_access_save()");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Edits the visibility info for the current selected course element.
	 * 
	 * @param infoText
	 */
	public void editVisibilityInfo(String infoText) {
		selectVisibilityTab();
		// the description shows up in an iframe
		//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.type("ui=commons::tinyMce_styledTextArea()", infoText);
		//selenium.selectFrame("relative=top");

		selenium.click("ui=courseEditor::content_visibility_saveInfo()");
		selenium.waitForPageToLoad("30000");
	}
		
	public boolean isVisibilityDependingOnDate() {
		selectVisibilityTab();
		if(selenium.isElementPresent("ui=courseEditor::content_visibility_dependingDate()")) {
			return selenium.isChecked("ui=courseEditor::content_visibility_dependingDate()");
		}
	  return false;
	}
	
	/**
	 * Changes visibility depending on date, 
	 * assuming at least startDateString or endDateString must be not null or not empty!
	 * @param startDateString
	 * @param endDateString
	 */
	public boolean changeVisibilityDependingOnDate(String startDateString, String endDateString) {
	  if((startDateString==null||startDateString.equals("")) && (endDateString==null || endDateString.equals(""))) {
		  throw new IllegalStateException("at least startDateString or endDateString must be not null or not empty!");
	  }
	  boolean changed = false;
	  selectVisibilityTab();
	  if(!isVisibilityDependingOnDate()) {
	    selenium.check("ui=courseEditor::content_visibility_dependingDate()");
	    selenium.waitForPageToLoad("30000");
	  }
	  if(startDateString!=null && !startDateString.equals("")) {
	    selenium.type("ui=courseEditor::content_visibility_startDate()", startDateString);
	    changed = true;
	  }
	  if(endDateString!=null && !endDateString.equals("")) {
	    selenium.type("ui=courseEditor::content_visibility_endDate()", endDateString);
	    changed = true;
	  }
	  selenium.click("ui=courseEditor::content_visibility_save()");
	  selenium.waitForPageToLoad("30000");
	  return changed;
	}
	
	public boolean isAccessDependingOnDate() {
		selectAccessTab();
		if(selenium.isElementPresent("ui=courseEditor::content_access_dependingDate()")) {
	    return selenium.isChecked("ui=courseEditor::content_access_dependingDate()");
		}
		return false;
	}
	
	/**
	 * Changes access depending on date, 
	 * assuming at least startDateString or endDateString must be not null or not empty!
	 * @param startDateString
	 * @param endDateString
	 */
	public boolean changeAccessDependingOnDate(String startDateString, String endDateString) {
	  selectAccessTab();
	  if((startDateString==null||startDateString.equals("")) && (endDateString==null || endDateString.equals(""))) {
		  throw new IllegalStateException("at least startDateString or endDateString must be not null and not empty!");
	  }
	  boolean changed = false;
	  if(!isAccessDependingOnDate()) {
	    selenium.check("ui=courseEditor::content_access_dependingDate()");
		selenium.waitForPageToLoad("30000");
	  }
	  if(startDateString!=null && !startDateString.equals("")) {
	    selenium.type("ui=courseEditor::content_access_startDate()", startDateString);
	    changed = true;
	  }
	  if(endDateString!=null && !endDateString.equals("")) {
	    selenium.type("ui=courseEditor::content_access_endDate()", endDateString);
	    changed = true;
	  }
	  selenium.click("ui=courseEditor::content_access_save()");
	  selenium.waitForPageToLoad("30000");
	  return changed;
	}	
	
	/**
	 * Switch "Blocked for learners" access, for the given section, and save.
	 */
	public void changeAccessBlockedForLearners(ACCESS_TYPE accessType) {
	  selectAccessTab();
	  if(ACCESS_TYPE.PRESENT.equals(accessType)) {
	    selenium.click("ui=courseEditor::content_access_blockedForLearners(fieldsetLegend=Present)");
	    selenium.waitForPageToLoad("30000");
	    selenium.click("ui=courseEditor::content_access_saveAccess(fieldsetLegend=Present)");
	    selenium.waitForPageToLoad("30000");
	  } else if (ACCESS_TYPE.READ_AND_WRITE.equals(accessType)){
	    selenium.click("ui=courseEditor::content_access_blockedForLearners(fieldsetLegend=Read and write)");
      selenium.waitForPageToLoad("30000");	
      selenium.click("ui=courseEditor::content_access_saveAccess(fieldsetLegend=Read and write)");
      selenium.waitForPageToLoad("30000");
	  } else {
	    throw new UnsupportedOperationException("Not yet implemented");
	  }
	  
	}

}
