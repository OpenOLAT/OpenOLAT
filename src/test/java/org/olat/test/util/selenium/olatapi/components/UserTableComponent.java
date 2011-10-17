package org.olat.test.util.selenium.olatapi.components;

import org.olat.test.util.selenium.olatapi.OLATSeleniumWrapper;

import com.thoughtworks.selenium.Selenium;

/**
 * This a page component representing a user table.
 * 
 * @author lavinia
 *
 */
public class UserTableComponent extends OLATSeleniumWrapper {

	public UserTableComponent(Selenium selenium) {
		super(selenium);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This assumes that "Add users" button was pressed, just before.
	 */
	public void chooseUser(String userName) {
		//fill in username info in the search mask
		selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", userName);
		selenium.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
		selenium.waitForPageToLoad("30000");
		this.sleepThread(5000);
		if(selenium.isElementPresent("ui=commons::usertable_adduser_checkUsername(nameOfUser=" + userName + ")")) {
			selenium.click("ui=commons::usertable_adduser_checkUsername(nameOfUser=" + userName + ")");
			selenium.click("ui=commons::usertable_adduser_choose()");
			selenium.waitForPageToLoad("30000");
			selenium.click("ui=commons::usertable_adduser_finish()");
			selenium.waitForPageToLoad("30000");
		} else {
			System.out.println("bummer! no such user found!");
			throw new IllegalStateException("No such user found!");
		}
	}
	
	public void removeUser() {
		
	}
	
}
