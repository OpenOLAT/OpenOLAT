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
 * This is the Enrolment course element editor.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class EnrolmentEditor extends CourseElementEditor {

	/**
	 * @param selenium
	 */
	public EnrolmentEditor(Selenium selenium) {
		super(selenium);	
		
    //	Check that we're on the right place		
		if(!selenium.isElementPresent("ui=courseEditor::content_bbEnrolment_tabConfiguration()")) {
			throw new IllegalStateException("This is not the - Enrolment course element - page");
		}
	}
	
	/**
	 * 
	 * @param groupName
	 */
	public void selectLearningGroups(String groupName) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbEnrolment_tabConfiguration()")) {
		  selenium.click("ui=courseEditor::content_bbEnrolment_tabConfiguration()");
		  selenium.waitForPageToLoad("30000");
		}
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_checkGroup(nameOfGroup=" + groupName + ")");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_apply()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbEnrolment_save()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * Selects groups in groupEnumerationString (Comma separated Value),
	 * and creates them if not already created.
	 * 
	 * @param groupEnumerationString
	 */
	public void createAndSelectGroups(String groupEnumerationString) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbEnrolment_tabConfiguration()")) {
		  selenium.click("ui=courseEditor::content_bbEnrolment_tabConfiguration()");
		  selenium.waitForPageToLoad("30000");
		}
		if(selenium.isElementPresent("ui=commons::flexiForm_labeledTextInput(formElementLabel=Learning groups)")) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Learning groups)", groupEnumerationString);
		  selenium.click("ui=courseEditor::content_bbEnrolment_save()");
		  selenium.waitForPageToLoad("30000");
		} else if(selenium.isElementPresent("ui=courseEditor::commons_groupBulk_selectGroupsIfError()")) {
			//if error, type in the new groupEnumerationString
			selenium.type("ui=courseEditor::commons_groupBulk_selectGroupsIfError()", groupEnumerationString);
			selenium.click("ui=courseEditor::content_bbEnrolment_save()");
		  selenium.waitForPageToLoad("30000");
		}
		//if create button present, click create
		if(selenium.isElementPresent("ui=courseEditor::commons_groupBulk_createGroups()")) {
			selenium.click("ui=courseEditor::commons_groupBulk_createGroups()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=commons::flexiForm_finishButton()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Check if the groupName is selected.
	 *  
	 * @param groupName
	 * @return
	 */
	public boolean isGroupSelected(String groupName) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbEnrolment_tabConfiguration()")) {
		  selenium.click("ui=courseEditor::content_bbEnrolment_tabConfiguration()");
		  selenium.waitForPageToLoad("30000");
		}
		String groupValue = selenium.getValue("ui=commons::flexiForm_labeledTextInput(formElementLabel=Learning groups)");
		return groupValue.indexOf(groupName)!=-1;
	}
	
	/**
	 * 
	 * @param areaName
	 */
	public void selectLearningAreas(String areaName) {
		if(selenium.isElementPresent("ui=courseEditor::content_bbEnrolment_tabConfiguration()")) {
		  selenium.click("ui=courseEditor::content_bbEnrolment_tabConfiguration()");
		  selenium.waitForPageToLoad("30000");
		}
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningArea()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_checkGroup(nameOfGroup=" + areaName + ")");
		selenium.click("ui=courseEditor::content_bbEnrolment_selectLearningGroupDialog_apply()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=courseEditor::content_bbEnrolment_save()");
		selenium.waitForPageToLoad("30000");
	}
}
