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

import com.thoughtworks.selenium.Selenium;

/**
 * This is the GroupAdministration page.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public class GroupAdmin extends OLATSeleniumWrapper {

	/**
	 * @param selenium
	 */
	public GroupAdmin(Selenium selenium) {
		super(selenium);		
		sleepThread(3000);
    //Check that we're on the right place
		if(!selenium.isElementPresent("ui=group::content_tools_tabTools()")) {
			throw new IllegalStateException("This is not the - Administration group - page");
		}
	}
	
	/**
	 * Adds participants and owners to the current group, looping through Add user(s).
	 * @param participants
	 * @param owners
	 * @throws Exception
	 */
	public void addMembers(String[] participants, String[] owners) throws Exception {
		selectMembersTab();
		
		//add participants
		for(String userName:participants) {
		  selenium.click("ui=commons::usertable_participants_addUsers()");
		  selenium.waitForPageToLoad("30000");
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", userName);
		  selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		  selenium.waitForPageToLoad("30000");
		  Thread.sleep(5000);
		  if(selenium.isElementPresent("ui=commons::usertable_adduser_checkUsername(nameOfUser=" + userName + ")")) {
		    selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser=" + userName + ")");
		    selenium.click("ui=commons::usertable_adduser_choose()");
		    selenium.waitForPageToLoad("30000");
		    selenium.click("ui=commons::usertable_adduser_finish()");
		    selenium.waitForPageToLoad("30000");
		  } else {
		  	System.out.println("GroupAdmin.addMembers - since no user found, skip participant: " + userName);
		  }
		}
		//add owners
		for(String userName:owners) {
			selenium.click("ui=commons::usertable_owners_addUsers()");
			selenium.waitForPageToLoad("30000");			
			selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", userName);
			selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser=" + userName + ")");
			selenium.click("ui=commons::usertable_adduser_choose()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=commons::usertable_adduser_finish()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Import participants to this group. Accepts a formatted input string, one username per row.
	 * Assumes that there is at least a new user in the participants list
	 * @param participants
	 */
	public boolean importParticipants(String participants) {
		selectMembersTab();
		if(participants==null || participants.trim().equals("")) {
			throw new IllegalArgumentException("participants string is not valid!");
		}
		
		selenium.click("ui=commons::usertable_participants_import()");
		selenium.waitForPageToLoad("30000");
		//wizard
		//step 1: enter username and press next
		selenium.type("ui=commons::flexiForm_labeledTextArea(formElementLabel=User names)", participants);
		sleepThread(3000);
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		//selenium.waitForPageToLoad("30000");
		sleepThread(3000);
		//step2: suppose they are all new users
		if(selenium.isElementPresent("ui=commons::anyLink(linkText=Next)")) {
		  selenium.click("ui=commons::anyLink(linkText=Next)");
		  selenium.waitForPageToLoad("30000");
		  //step 3: do not sent email, just go on
		  selenium.click("ui=commons::usertable_adduser_finish()");
		  //selenium.waitForPageToLoad("30000");
		  sleepThread(3000);
		  return true;
		} else if(selenium.isElementPresent("ui=commons::anyLink(linkText=Next)")) {
			//cancel wizard
			selenium.click("ui=commons::usertable_adduser_cancelWizard()");
			selenium.waitForPageToLoad("30000");				
		}
		return false;
	}
	
	public void removeAllWaiting() {
		selectMembersTab();
		if (selenium.isElementPresent("ui=commons::usertable_waitingList_selectAll()")) {
			selenium.click("ui=commons::usertable_waitingList_selectAll()");
			selenium.click("ui=commons::usertable_waitingList_remove()");
			selenium.waitForPageToLoad("30000");
			//per default is "Send e-mail" not checked
			//selenium.check("ui=commons::flexiForm_labeledCheckbox(formElementLabel=" + Send e-mail + ")")
			selenium.click("ui=commons::usertable_adduser_finish()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=dialog::Yes()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	public void removeAllParticipants() {
		selectMembersTab();
		if (selenium.isElementPresent("ui=commons::usertable_participants_selectAll()")) {
			selenium.click("ui=commons::usertable_participants_selectAll()");
			selenium.click("ui=commons::usertable_participants_remove()");
			selenium.waitForPageToLoad("30000");
			//per default is "Send e-mail" not checked
			//selenium.check("ui=commons::flexiForm_labeledCheckbox(formElementLabel=" + Send e-mail + ")")
			selenium.click("ui=commons::usertable_adduser_finish()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=dialog::Yes()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	public void removeParticipant(String userName, boolean confirmRemoval) {
		selectMembersTab();
		selenium.check("ui=commons::usertable_userlist_checkUsername(nameOfUser=" + userName + ")");
		selenium.click("ui=commons::usertable_participants_remove()");
		selenium.waitForPageToLoad("30000");
		//per default is "Send e-mail" not checked
		//selenium.check("ui=commons::flexiForm_labeledCheckbox(formElementLabel=" + Send e-mail + ")")
		selenium.click("ui=commons::usertable_adduser_finish()");
		selenium.waitForPageToLoad("30000");
		if(confirmRemoval) {
			selenium.click("ui=dialog::Yes()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	public void confirmRemove() {
		selenium.click("ui=dialog::Yes()");		
	}
	
	private void selectMembersTab() {
	//go to the Members tab, if not already there
		if(selenium.isElementPresent("ui=group::content_members_tabMembers()")) {
			selenium.click("ui=group::content_members_tabMembers()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	private void selectToolsTab() {
	  //go to the Tool tab, if not already there
		if(selenium.isElementPresent("ui=group::content_tools_tabTools()")) {
		  selenium.click("ui=group::content_tools_tabTools()");
		  selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * 
	 * @return Returns a CourseRun instance.
	 */
	/*public CourseRun close() {
		selenium.click("ui=groupManagement::toolbox_groupManagement_close()");
		selenium.waitForPageToLoad("30000");
		return new CourseRun(selenium);
	}*/

	/**
	 * Change title and description, if in Administration page.
	 * @param groupName
	 * @param groupDescription
	 */
	public void setTitleAndDescription(String groupName, String groupDescription) {
		//go to the Description tab, if not already there
		if(selenium.isElementPresent("ui=group::content_description_tabDescription()")) {
			selenium.click("ui=group::content_description_tabDescription()");
			selenium.waitForPageToLoad("30000");
		}
		
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Group name)", groupName);				
	  //the description shows up in an iframe
		selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		selenium.type("ui=commons::tinyMce_styledTextArea()", groupDescription);
		selenium.selectFrame("relative=top");	
		selenium.click("ui=commons::flexiForm_finishButton()");
		selenium.waitForPageToLoad("30000");
	}

	/**
	 * Toggle tool selection, if in Administration/Tools page.
	 * (e.g. if you want to change the selection ON/OFF for wiki, just pass true for this parameter)
	 * @param toggleInfo
	 * @param toggleContactForm
	 * @param toggleCalendar
	 * @param toggleFolder
	 * @param toggleForum
	 * @param toggleWiki
	 */
	public void setTools(boolean toggleInfo, boolean toggleContactForm, boolean toggleCalendar, boolean toggleFolder, boolean toggleForum, boolean toggleWiki, boolean toggleChat) {
		selectToolsTab();
		if(toggleInfo) {
		  selenium.click("ui=group::content_tools_collaborationTools_information()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		if(toggleContactForm) {
			selenium.click("ui=group::content_tools_collaborationTools_contactForm()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		if(toggleCalendar) {
		  selenium.click("ui=group::content_tools_collaborationTools_calendar()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		if(toggleFolder) {
		  selenium.click("ui=group::content_tools_collaborationTools_folder()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		if(toggleForum) {
		  selenium.click("ui=group::content_tools_collaborationTools_forum()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		if(toggleWiki) {
		  selenium.click("ui=group::content_tools_collaborationTools_wiki()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		if(toggleChat && selenium.isElementPresent("ui=group::content_tools_collaborationTools_chat()")) {
			selenium.click("ui=group::content_tools_collaborationTools_chat()");
		}
		try { Thread.sleep(3000); } catch (Exception e) {}
		//save button was removed
		//selenium.click("ui=commons::flexiForm_saveButton()");		
		//selenium.waitForPageToLoad("30000");		
	}
	
	public boolean isChatSelected() {
		selectToolsTab();
		return selenium.isChecked("ui=group::content_tools_collaborationTools_chat()");
	}
	
	public boolean isWikiSelected() {
		selectToolsTab();
		return selenium.isChecked("ui=group::content_tools_collaborationTools_wiki()");
	}

	/**
	 * Sets info, only if the info text field is available.
	 * @param text
	 */
	public void setInfo(String text) {
    //	go to the Tool tab, if not already there
		if(selenium.isElementPresent("ui=group::content_tools_tabTools()")) {
		  selenium.click("ui=group::content_tools_tabTools()");
		  selenium.waitForPageToLoad("30000");
		}
		//selenium.selectFrame("//iframe[contains(@src,'javascript:\"\"')]");
		if(selenium.isElementPresent("ui=group::content_tools_informationForMembers_formFieldInformationMembers()")) {
		  selenium.click("ui=group::content_tools_informationForMembers_formFieldInformationMembers()");
		  selenium.type("ui=group::content_tools_informationForMembers_formFieldInformationMembers()", text);
		  //selenium.selectFrame("relative=top");	
		  selenium.click("ui=group::content_tools_informationForMembers_save()");
		} else {
			throw new IllegalStateException("Select Info in Tools first!");
		}
	}

	/**
	 * 
	 * @param text
	 */
	public void selectCalendarWriteAccess(String text) {
   //	go to the Tool tab, if not already there
		if(selenium.isElementPresent("ui=group::content_tools_tabTools()")) {
		  selenium.click("ui=group::content_tools_tabTools()");
		  selenium.waitForPageToLoad("30000");
		}
		if(selenium.isElementPresent("ui=group::content_tools_calendarAccess_save()") && !selenium.isChecked("ui=group::content_tools_calendarAccess_calendarAccess(label=" + text + ")")) {
		  selenium.click("ui=group::content_tools_calendarAccess_calendarAccess(label=" + text + ")");
		  selenium.click("ui=group::content_tools_calendarAccess_save()");
		  selenium.waitForPageToLoad("30000");
		} else if(!selenium.isElementPresent("ui=group::content_tools_calendarAccess_save()")) {
			throw new IllegalStateException("Select Calendar in Tools first!");
		}
	}
	
	/**
	 * Select the description tab, if not already there, and enable/disable waiting list and/or move up, save.
	 * @param maxParticipants
	 * @param hasWaitingList
	 * @param moveUp
	 */
	public void configureParticipantsAndWaitingList(int maxParticipants, Boolean hasWaitingList, Boolean moveUp) {
		if(selenium.isElementPresent("ui=group::content_description_tabDescription()")) {
		  selenium.click("ui=group::content_description_tabDescription()");
		  selenium.waitForPageToLoad("30000");
		}
		selenium.type("ui=groupManagement::content_learningGroupsEditor_maxParticipants()", String.valueOf(maxParticipants));
		if (hasWaitingList!=null && hasWaitingList && !selenium.isChecked("ui=groupManagement::content_learningGroupsEditor_enableWaitinglist()")) {
			//enableWaitinglist
			selenium.check("ui=groupManagement::content_learningGroupsEditor_enableWaitinglist()");
		} else if (hasWaitingList!=null && !hasWaitingList && selenium.isChecked("ui=groupManagement::content_learningGroupsEditor_enableWaitinglist()")) {
			selenium.uncheck("ui=groupManagement::content_learningGroupsEditor_enableWaitinglist()");
		}
		if (moveUp!=null && moveUp && !selenium.isChecked("ui=groupManagement::content_learningGroupsEditor_moveUpAutomatically()")) {
			//moveUpAutomatically
			selenium.check("ui=groupManagement::content_learningGroupsEditor_moveUpAutomatically()");
		} else if (moveUp!=null && !moveUp && selenium.isChecked("ui=groupManagement::content_learningGroupsEditor_moveUpAutomatically()")) {
			selenium.uncheck("ui=groupManagement::content_learningGroupsEditor_moveUpAutomatically()");
		}
		
		selenium.click("ui=commons::flexiForm_finishButton()");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @return
	 */
 	public Group start(String name) {
		selenium.click("ui=group::menu_startCourse()");
		selenium.waitForPageToLoad("30000");
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {			
		}
		return new Group(selenium, name);
	}
 	
 	/**
 	 * 
 	 * @param groupName
 	 */
 	public void close(String groupName) { 		
 		if(selenium.isElementPresent("ui=tabs::closeGroup(nameOfGroup=" + groupName + ")")) {
 			selenium.click("ui=tabs::closeGroup(nameOfGroup=" + groupName + ")");
 			selenium.waitForPageToLoad("30000");
 		} else {
 			System.out.println("Call GroupManager.close() to close this!");
 		}
 	}
 	
 	public String getMaxNumParticipants() {
 		return selenium.getValue("ui=groupManagement::toolbox_groupManagement_formNewLearningGroup_maxParticipants()");
 	}
 	
 	/**
 	 * Change displayXXX if the corresponding input is not null.
 	 * If one input param is null, do nothing for that one.
 	 * @param dsiplayOwners
 	 * @param displayParticipants
 	 * @param displayWaitingList
 	 */
 	public void setMemberDisplayOptions(Boolean displayOwners, Boolean displayParticipants, Boolean displayWaitingList) {
 	  selectMembersTab();
 	  //TODO: impl on/off displayOwners
 	  sleepThread(3000);
 	  
 	  //TODO:LD: remove debug code!
 	  if(!selenium.isTextPresent("Display members")) {
 		System.out.println("Display members - still unavailable");  
 		sleepThread(3000);
 		if(!selenium.isTextPresent("Display members")) {
 	 		System.out.println("Display members - still unavailable");  
 		}
 	  } else if(selenium.isTextPresent("Members can see")){
 		System.out.println("Members can see...");  
 		if(selenium.isTextPresent("Members can see participants")) {
 		  System.out.println("Members can see participants");  
 		}
 	  }
 	  
 	  //on/off displayParticipants
 	  if(displayParticipants!=null && displayParticipants && !selenium.isChecked("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Members can see participants)")) {
 	    selenium.click("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Members can see participants)");
 	  } else if(displayParticipants!=null && !displayParticipants && selenium.isChecked("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Members can see participants)")) {
 		selenium.uncheck("ui=commons::flexiForm_labeledCheckbox(formElementLabel=Members can see participants)");
 	  }
 	  sleepThread(1000);
 	  //TODO: impl on/off displayWaitingList 
 	}
	
}
