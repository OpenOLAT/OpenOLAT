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
package org.olat.test.functional.usermanagement;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.home.MySettings;
import org.olat.test.util.selenium.olatapi.user.UserManagement;
import org.olat.test.util.selenium.olatapi.user.UserSettings;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Test if user settings can be changed in user management.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Administrator opens user management and searches for user USER_NAME. <br/>
 * 2. Admin changes USER_NAME's last name and password. <br/>
 * 3. User USER_NAME logs in and checks if changes apply. <br/>
 * 
 * @author sandra
 * 
 */

public class UserSettingsTest extends BaseSeleneseTestCase {
	
	//TODO:LD: temporary  changed usernames - workaround for OLAT-5249
	//private final String USER_NAME = "usermngt_testuser" + System.currentTimeMillis();
	private final String USER_NAME = "usermngttestuser" + System.currentTimeMillis();
	private final String USER_FNAME = "First";
	private final String USER_LNAME = "Last";
	private final String USER_EMAIL = System.currentTimeMillis() + "@user.com";
	private final String USER_PW = "TestuserPW1";
	private final String USER_LNAME_NEW = "LastNew";
	private final String USER_PW_NEW = "TestuserPWNew2";

	public void testUserSettings() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		OLATWorkflowHelper olatWorkflowAdmin = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		UserManagement userManagement = olatWorkflowAdmin.getUserManagement();
		
		//create user
		userManagement.createUser(USER_NAME, USER_FNAME, USER_LNAME, USER_EMAIL, USER_PW);
		
	 	// search and select user, change first name, change password
		UserSettings userSettings = olatWorkflowAdmin.getUserManagement().selectUser(USER_NAME);
		userSettings.setLastName(USER_LNAME_NEW, true);
		userSettings.setPassword(USER_PW_NEW);

		// log in as user, check if new password is valid
		OLATWorkflowHelper olatWorkflowUser = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW_NEW));
		
		// check if user's last name was changed
		MySettings myNewSettings = olatWorkflowUser.getHome().getUserSettings();		
		assertTrue(myNewSettings.isDisabledTextPresent(USER_LNAME_NEW));
	}
}

