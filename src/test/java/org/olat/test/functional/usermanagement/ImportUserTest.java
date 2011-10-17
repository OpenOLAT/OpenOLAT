package org.olat.test.functional.usermanagement;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.user.UserManagement;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * Import users, assert that existing users don't get imported.
 * <br/>
 * Test setup: <br/>
 * 1. Concatenate strings with all user attributes for user import. 
 * <p>
 * Test case: <br/>
 * 1. Administrator opens user management. <br/>
 * 2. Admin imports NEW_USER_NAME1. <br/>
 * 3. Admin imports multiple new users. <br/>
 * 4. Admin imports one existing user and checks that existing user cannot be imported. <br/>
 * 5. Admin imports one new and one existing user and checks that only new users can be imported. <br/>
 * 6. Imported users check if the can all log in. <br/>
 * 
 * @author sandra
 * 
 */

public class ImportUserTest extends BaseSeleneseTestCase {

	private final String NEW_USER_NAME1 = "newuser1" + System.currentTimeMillis();
	private final String NEW_USER_NAME2 = "newuser2" + System.currentTimeMillis();
	private final String NEW_USER_NAME3 = "newuser3" + System.currentTimeMillis();
	private final String NEW_USER_NAME4 = "newuser4" + System.currentTimeMillis();
	private final String USER_FNAME = "First";
	private final String USER_LNAME = "Last";
	private final String USER_EMAIL = System.currentTimeMillis() + "@user1.com";
	private final String USER_EMAIL2 = System.currentTimeMillis() + "@user2.com";
	private final String USER_EMAIL3 = System.currentTimeMillis() + "@user3.com";
	private final String USER_EMAIL4 = System.currentTimeMillis() + "@user4.com";
	private final String USER_PW = "olat3";
	
	private final String NEW_USER_TO_IMPORT = NEW_USER_NAME1 + "	" + USER_PW + "	en	" +  USER_FNAME + "	" + USER_LNAME + "	" + USER_EMAIL;
	private final String MORETHANONE_NEW_USER_TO_IMPORT = NEW_USER_NAME2 + "	" + USER_PW + "	en	" +  USER_FNAME + "	" + USER_LNAME + "	" + USER_EMAIL2 + "\n"+
			NEW_USER_NAME3 + "	" + USER_PW + "	en	" +  USER_FNAME + "	" + USER_LNAME + "	" + USER_EMAIL3;
	private final String NEW_AND_EXISTING_USER_TO_IMPORT = NEW_USER_TO_IMPORT + "\n" +
			NEW_USER_NAME4 + "	" + USER_PW + "	en	" +  USER_FNAME + "	" + USER_LNAME + "	" + USER_EMAIL4;;
	
	public void testUserImport() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.SINGLE_VM);
		
		OLATWorkflowHelper olatWorkflow = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos());
		UserManagement userManagement = olatWorkflow.getUserManagement();
		
		//case 1: import one new user from excel string
		assertTrue(userManagement.importAllUsers(NEW_USER_TO_IMPORT));
		
		// case 2: import multiple new users from excel string
		assertTrue(userManagement.importAllUsers(MORETHANONE_NEW_USER_TO_IMPORT));								

		// case 3: try to import one existing user, assert that existing user cannot be imported
		assertTrue(userManagement.importUsersExpectingError(NEW_USER_TO_IMPORT));
		
		// case 4: try to import one new user and one existing user
		assertTrue(userManagement.importOnlyNewUsers(NEW_AND_EXISTING_USER_TO_IMPORT));
		
		// check that newly imported users can log in. 
		OLATWorkflowHelper olatWorkflowImportedUser1 = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, NEW_USER_NAME1, USER_PW));
		olatWorkflowImportedUser1.getHome();
		olatWorkflowImportedUser1.logout();
		
		OLATWorkflowHelper olatWorkflowImportedUser2 = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, NEW_USER_NAME2, USER_PW));
		olatWorkflowImportedUser2.getHome();
		olatWorkflowImportedUser2.logout();
		
		OLATWorkflowHelper olatWorkflowImportedUser3 = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, NEW_USER_NAME3, USER_PW));
		olatWorkflowImportedUser3.getHome();
		olatWorkflowImportedUser3.logout();

		OLATWorkflowHelper olatWorkflowImportedUser4 = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, NEW_USER_NAME4, USER_PW));
		olatWorkflowImportedUser4.getHome();
		olatWorkflowImportedUser4.logout();
	}
}

