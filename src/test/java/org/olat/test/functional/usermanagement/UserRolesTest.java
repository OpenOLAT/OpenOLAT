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
import org.olat.test.util.selenium.olatapi.user.UserManagement;
import org.olat.test.util.selenium.olatapi.user.UserSettings;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Test if user roles can be changed and the changes apply for user.
 * <br/>
 * Test setup: <br/>
 * -
 * <p>
 * Test case: <br/>
 * 1. Administrator opens user management. <br/>
 * 2. Admin creates user USER_NAME. <br/>
 * 3. Admin selects USER_NAME and sets role to author. <br/>
 * 4. User USER_NAME logs in and checks if he has the according authoring rights but no rights for administrator, 
 * group and user management. <br/>
 * 5. Admin resets roles, USER_NAME checks if he has no authoring rights anymore. <br/>
 * 6. Admin sets roles to user manager. <br/>
 * 7. USER_NAME logs in and checks if he has rights for user administration (edit other users but no admins).<br/>
 * 8. Admin sets roles to group manager. <br/>
 * 9. USER_NAME logs in and checks if he has rights for group administration.<br/>
 * 10. Admin sets roles to system administrator. <br/>
 * 11. USER_NAME logs in and checks if he has access to all administration and management tabs.<br/>
 * 12. Admin resets roles. <br/>
 * 13. USER_NAME logs in and checks if he has no more authoring, manager or administrator rights on the system. <br/>
 * 
 * @author sandra
 * 
 */

public class UserRolesTest extends BaseSeleneseTestCase {
	
	//TODO:LD: temporary  changed usernames - workaround for OLAT-5249
	//private final String USER_NAME = "usermngt_testuser" + System.currentTimeMillis();
	private final String USER_NAME = "usermngttestuser" + System.currentTimeMillis();

	private final String USER_FNAME = "First";
	private final String USER_LNAME = "Last";
	private final String USER_EMAIL = System.currentTimeMillis() + "@user.com";
	private final String USER_PW = "TestuserPW1";

	
	public void testUserRoles() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		UserManagement userManagement = olatWorkflow.getUserManagement();
		
		//create user
		userManagement.createUser(USER_NAME, USER_FNAME, USER_LNAME, USER_EMAIL, USER_PW);
		
	 	// search and select user
		UserSettings userSettings = olatWorkflow.getUserManagement().selectUser(USER_NAME);
		
		// change role: author
		userSettings.setRoles(false, false, true, false, false);

		// log in user and check if he has authoring rights, but not tabs group management, user management, administration
		OLATWorkflowHelper olatWorkflowAuthor = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		assertTrue(olatWorkflowAuthor.getLearningResources().getSelenium().isElementPresent("ui=learningResources::toolbox_create_course()"));
		assertFalse(olatWorkflowAuthor.getSelenium().isElementPresent("ui=tabs::groupAdministration()"));
		assertFalse(olatWorkflowAuthor.getSelenium().isElementPresent("ui=tabs::userManagement()"));
		assertFalse(olatWorkflowAuthor.getSelenium().isElementPresent("ui=tabs::administration()"));
		olatWorkflowAuthor.logout();
		
		// uncheck author role and make sure user has no more authoring rights after login
		userSettings.setRoles(false, false, false, false, false);
		
		// login user, make sure he cannot create course, no tabs group management, user management, administration
		OLATWorkflowHelper olatWorkflowStudent = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		assertFalse(olatWorkflowStudent.getLearningResources().getSelenium().isElementPresent("ui=learningResources::toolbox_create_course()"));
		assertFalse(olatWorkflowStudent.getSelenium().isElementPresent("ui=tabs::groupAdministration()"));
		assertFalse(olatWorkflowStudent.getSelenium().isElementPresent("ui=tabs::userManagement()"));
		assertFalse(olatWorkflowStudent.getSelenium().isElementPresent("ui=tabs::administration()"));
		olatWorkflowStudent.logout();

		// allocate user manager role
		userSettings.setRoles(true, false, false, false, false);
		
		//login user manager, can create course, has tab user management, but not group management and administration
		OLATWorkflowHelper olatWorkflowUserManager = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		assertFalse(olatWorkflowUserManager.getLearningResources().getSelenium().isElementPresent("ui=learningResources::toolbox_create_course()"));
		assertTrue(olatWorkflowUserManager.getSelenium().isElementPresent("ui=tabs::userManagement()"));
		assertFalse(olatWorkflowUserManager.getSelenium().isElementPresent("ui=tabs::groupAdministration()"));
		assertFalse(olatWorkflowUserManager.getSelenium().isElementPresent("ui=tabs::administration()"));

		//can edit student but not admin
		UserSettings studentSettings = olatWorkflowUserManager.getUserManagement().selectUser(context.getStandardStudentOlatLoginInfos().getUsername());
		studentSettings.isTextPresent(USER_NAME);
		assertTrue("Asserts that the current user doesn't not have enough rights to edit user", olatWorkflowUserManager.getUserManagement().cannotEditUser(context.getStandardAdminOlatLoginInfos().getUsername()));
		olatWorkflowUserManager.logout();
		
		// allocate group manager role
		userSettings.setRoles(false, true, false, false, false);
		
		//login group manager, can create course, has tab group management, but not user management and administration
		OLATWorkflowHelper olatWorkflowGroupManager = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		assertTrue(olatWorkflowGroupManager.getSelenium().isElementPresent("ui=tabs::groupAdministration()"));
		assertFalse(olatWorkflowGroupManager.getSelenium().isElementPresent("ui=tabs::userManagement()"));
		assertFalse(olatWorkflowGroupManager.getSelenium().isElementPresent("ui=tabs::administration()"));
		assertFalse(olatWorkflowGroupManager.getLearningResources().getSelenium().isElementPresent("ui=learningResources::toolbox_create_course()"));
		olatWorkflowGroupManager.logout();
		
		// allocate admin role
		userSettings.setRoles(false, false, false, true, false);
		
		// log in admin, can create course, has all tabs
		OLATWorkflowHelper olatWorkflowAdmin = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		assertTrue(olatWorkflowAdmin.getLearningResources().getSelenium().isElementPresent("ui=learningResources::toolbox_create_course()"));
		assertTrue(olatWorkflowAdmin.getSelenium().isElementPresent("ui=tabs::groupAdministration()"));
		assertTrue(olatWorkflowAdmin.getSelenium().isElementPresent("ui=tabs::userManagement()"));
		assertTrue(olatWorkflowAdmin.getSelenium().isElementPresent("ui=tabs::administration()"));
		olatWorkflowAdmin.logout();
		
		// uncheck all roles and make sure user has no more rights after login
		userSettings.setRoles(false, false, false, false, false);
		OLATWorkflowHelper olatWorkflowNoRights = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		assertFalse(olatWorkflowNoRights.getLearningResources().getSelenium().isElementPresent("ui=learningResources::toolbox_create_course()"));
		assertFalse(olatWorkflowNoRights.getSelenium().isElementPresent("ui=tabs::groupAdministration()"));
		assertFalse(olatWorkflowNoRights.getSelenium().isElementPresent("ui=tabs::userManagement()"));
		assertFalse(olatWorkflowNoRights.getSelenium().isElementPresent("ui=tabs::administration()"));
		olatWorkflowNoRights.logout();
	}
}

