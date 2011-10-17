package org.olat.test.util.selenium.olatapi.group;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * Page for Rightsgroup Administration
 * @author Thomas Linowsky, BPS GmbH
 *
 */


public class RightsAdmin extends OLATSeleniumWrapper{
	
	/**
	 * Default constructor
	 * @param selenium
	 */
	public RightsAdmin(Selenium selenium){
		super(selenium);
	}
	
	
	/**
	 * Add members to Group (e.g. Rightsgroup) looping through Add user(s).
	 * @param members The usernames of the members to add  
	 * @throws InterruptedException
	 */
	
	public void addMembers(String[] members) throws InterruptedException{
		selectMembersTab();
		for(String userName:members) {
		  selenium.click("ui=commons::usertable_members_addUsers()");
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
	}
	
	/**
	 * Go to Tab Members
	 */
	
	private void selectMembersTab() {
	//go to the Members tab, if not already there
		if(selenium.isElementPresent("ui=group::content_members_tabMembers()")) {
			selenium.click("ui=group::content_members_tabMembers()");
			selenium.waitForPageToLoad("30000");
		}
	}

}
