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
package org.olat.test.util.selenium.olatapi.group;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;
import org.olat.test.util.selenium.olatapi.course.run.CourseRun;

import com.thoughtworks.selenium.Selenium;

/**
 * OLAT abstraction for the GroupManagement page.
 * This is acquired via the CourseRun.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class GroupManagement extends OLATSeleniumWrapper {

	
	/**
	 * @param selenium
	 */
	public GroupManagement(Selenium selenium) {
		super(selenium);	
		
		//Check that we're on the right place
		if(!selenium.isTextPresent("Group management")) {
			throw new IllegalStateException("This is not the - Group management - page");
		}
	}

	/**
	 * create new group with the given name, 
	 * add one tutor to group, 
	 * add one participant to the group, 
	 * and close the group management. 
	 * @param groupName
	 * @param userName
	 */
	public void createGroupAndAddMembers(String groupName, String tutorName, String participantName) {
		//create new learning group			
		selenium.click("ui=groupManagement::toolbox_groupManagement_newLearningGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_name()", groupName);
		selenium.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_save()");
		selenium.waitForPageToLoad("30000");
		//add members
		selenium.click("ui=group::content_members_tabMembers()");
		selenium.waitForPageToLoad("30000");
		//add tutor
		if(tutorName!=null) {
		  selenium.click("ui=groupManagement::content_learningGroupsEditor_addMembers_addTutor()");
		  selenium.waitForPageToLoad("30000");		  
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", tutorName);
		  selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser="+tutorName+")");
		  selenium.click("ui=commons::usertable_adduser_choose()");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=commons::usertable_adduser_finish()");
		  selenium.waitForPageToLoad("30000");
		}
		//add participants
		if(participantName!=null) {
		  selenium.click("ui=commons::usertable_participants_addUsers()");
		  selenium.waitForPageToLoad("30000");		 
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", participantName);
		  selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser="+participantName+")");
		  selenium.click("ui=commons::usertable_adduser_choose()");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=commons::usertable_adduser_finish()");
		  selenium.waitForPageToLoad("30000");		  
		}
		selenium.click("ui=groupManagement::toolbox_groupManagement_close()");
	  selenium.waitForPageToLoad("30000");
	}
	
  /**
   * If maxParticipants>0 type the value into the appropriate field.
   * @param groupName
   * @param description
   * @param maxParticipants
   * @param hasWaitingList
   * @param moveUpAutomatically
   * @return Returns the newly created GroupAdmin. 
   */
	public GroupAdmin createLearningGroup(String groupName, String description, int maxParticipants, boolean hasWaitingList, boolean moveUpAutomatically) {
	
    //	create new learning group		
		selenium.click("ui=groupManagement::toolbox_groupManagement_newLearningGroup()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_name()", groupName);		
		if(description!=null) {
			//uses a Rich text element
		  selenium.type("ui=commons::tinyMce_styledTextArea()", description);
		}		
		selenium.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_save()");		
		selenium.waitForPageToLoad("30000");
		
		if(maxParticipants>0) {
			selenium.type("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_maxParticipants()", String.valueOf(maxParticipants));
		}
		if(hasWaitingList) {
			selenium.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_enableWaitinglist()");
		}		
		if(moveUpAutomatically) {
			selenium.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_moveUpAutomatically()");
		}
		selenium.click("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_save()");		
		selenium.waitForPageToLoad("30000");
		
		return new GroupAdmin(selenium);
	}
	
	/**
	 * Select "All learning groups" and select the group with the given name.
	 * @param groupName
	 * @return Returns a GroupAdmin instance.
	 */
	public GroupAdmin editLearningGroup(String groupName) {
		selenium.click("ui=groupManagement::menu_allLearningGroups()");
		selenium.waitForPageToLoad("30000");		
		// if too many entries found - show all
  	if(selenium.isElementPresent("ui=commons::table_showAll()")) {
			selenium.click("ui=commons::table_showAll()");
			selenium.waitForPageToLoad("30000");
		} 
		selenium.click("ui=groupManagement::content_learningGroupTable_editLearningGroup(nameOfLearningGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");
		return new GroupAdmin(selenium);
	}
	
	/**
	 * Selects the learning group with the given name.
	 * @param groupName
	 * @return
	 */
	public Group selectLearningGroup(String groupName) {
		selenium.click("ui=groupManagement::menu_allLearningGroups()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groupManagement::content_learningGroupTable_selectGroup(nameOfGroup="+ groupName + ")");
		selenium.waitForPageToLoad("30000");
		return new Group(selenium, groupName);
	}
	
		
	/**
	 * 
	 * @param areaName
	 * @param description
	 */
	public LearningArea createLearningArea(String areaName, String description) {
		selenium.click("ui=groupManagement::toolbox_groupManagement_newLearningArea()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=groupManagement::toolbox_groupManagement_formNewLearningArea_name()", areaName);
		if(description!=null) {
			typeInRichText(description);
		}
		selenium.click("ui=commons::flexiForm_finishButton()");
		selenium.waitForPageToLoad("30000");
		
		return new LearningArea(selenium); 
	}
	
	/**
	 * 
	 * @param areaName
	 * @return
	 */
	public LearningArea editLearningArea(String areaName) {
		selenium.click("ui=groupManagement::menu_allLearningAreas()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groupManagement::content_learningAreaTable_editLearningArea(nameOfLearningArea=learning area selenium 1)");
		selenium.waitForPageToLoad("30000");
		
		return new LearningArea(selenium); 
	}
	
	/**
	 * 
	 * @param userName
	 * @param groupName
	 */
	public void removeMemberFromGroup(String userName, String groupName) {
		selenium.click("ui=groupManagement::menu_allMembers()");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=commons::usertable_userlist_clickUserName(nameOfUser="+userName+")");
		selenium.waitForPageToLoad("30000");
		selenium.click("ui=groupManagement::content_userDetails_removeFromGroup(nameOfGroup=" + groupName + ")");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @return Returns a CourseRun instance.
	 */
	public CourseRun close() {
		selenium.click("ui=groupManagement::toolbox_groupManagement_close()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}
	
	/**
	 * Deletes group only if such group found.
	 * @param groupName
	 */
	public void deleteGroup(String groupName) {
		if(selenium.isElementPresent("ui=groupManagement::menu_allLearningGroups()")) {
		  selenium.click("ui=groupManagement::menu_allLearningGroups()");
		  selenium.waitForPageToLoad("30000");
		}
		if (selenium.isElementPresent("ui=groupManagement::content_learningGroupTable_deleteLearningGroup(nameOfLearningGroup=" + groupName + ")")) {
			selenium.click("ui=groupManagement::content_learningGroupTable_deleteLearningGroup(nameOfLearningGroup=" + groupName + ")");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=dialog::Yes()");
			selenium.waitForPageToLoad("30000");
			System.out.println("Group deleted: " + groupName);
		} else {
			System.out.println("No such group found, could not delete group: " + groupName);
		}
	}
}


