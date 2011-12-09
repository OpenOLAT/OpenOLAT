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
*/
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
