package org.olat.test.util.selenium.olatapi.course.editor;

import org.olat.test.util.selenium.olatapi.components.UserTableComponent;

import com.thoughtworks.selenium.Selenium;

public class TopicAssignmentEditor extends CourseElementEditor {

	public TopicAssignmentEditor(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

	private void selectConfigurationTab() {
		if(selenium.isElementPresent("ui=courseEditor::content_bbTopicAssignment_tabConfig()")) {
			selenium.click("ui=courseEditor::content_bbTopicAssignment_tabConfig()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * It assumes that there are no default values selected. <br/>
	 * If limitNumOfTopics is true, topicLimit must be >0.
	 * If acceptParticipants is true, onlyOneTopic is relevant, else not.
	 * 
	 * @param limitNumOfTopics
	 * @param topicLimit
	 * @param acceptParticipants
	 * @param onlyOneTopic
	 */
	public void configure (boolean limitNumOfTopics, int topicLimit, boolean acceptParticipants, boolean onlyOneTopic) {
		selectConfigurationTab();
		if(limitNumOfTopics) {
		  selenium.click("ui=courseEditor::content_bbTopicAssignment_topicsPerParticipant()");
		  selenium.waitForPageToLoad("30000");
		  selenium.type("ui=courseEditor::content_bbTopicAssignment_howManyTopicsPerParticipant()",String.valueOf(topicLimit));
		}
		if(acceptParticipants) {
		  selenium.click("ui=courseEditor::content_bbTopicAssignment_topicSelectionAccept()");
		  selenium.waitForPageToLoad("30000");
		  if(!selenium.isElementPresent("ui=courseEditor::content_bbTopicAssignment_onlyOneTopicAllowed()")){
			  selenium.click("ui=courseEditor::content_bbTopicAssignment_topicSelectionAccept()");
			  selenium.waitForPageToLoad("30000");
		  }
		  selenium.click("ui=courseEditor::content_bbTopicAssignment_onlyOneTopicAllowed()");
		  selenium.waitForPageToLoad("30000");
		  selenium.click("ui=courseEditor::content_bbTopicAssignment_saveConfig()");
		  selenium.waitForPageToLoad("30000");
		}
		
	}
	
	private void selectPersonsInCharge() {
		if(selenium.isElementPresent("ui=courseEditor::content_bbTopicAssignment_tabPersons()")) {
			selenium.click("ui=courseEditor::content_bbTopicAssignment_tabPersons()");
			selenium.waitForPageToLoad("30000");
		}
	}
	
	/**
	 * Add user(s) with this userNamePrefix.
	 * @param userNamePrefix
	 */
	public void addUsers(String userNamePrefix) {
		selectPersonsInCharge();
		//select all users starting with userNamePrefix
		selenium.click("ui=commons::usertable_addUsers()");
		selenium.waitForPageToLoad("30000");
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", userNamePrefix);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		sleepThread(5000);
		selenium.click("ui=commons::usertable_adduser_selectAll()");
		selenium.click("ui=commons::usertable_adduser_choose()");
		selenium.waitForPageToLoad("30000");
		sleepThread(3000);
		selenium.click("ui=commons::flexiForm_genericButton(buttonLabel=Next)");
		selenium.waitForPageToLoad("30000");
	}
	
	public void addUser(String userName) {
		selectPersonsInCharge();
		selenium.click("ui=commons::usertable_addUsers()");
		selenium.waitForPageToLoad("30000");
		UserTableComponent userTable = new UserTableComponent(selenium);
		userTable.chooseUser(userName);
	}
	
}
