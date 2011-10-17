package org.olat.test.functional.home;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.home.MySettings;
import org.olat.test.util.selenium.olatapi.user.UserManagement;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * Tests change email for a user in Home Settings.
 * <p>
 * <br/>
 * Test case: <br/>
 * create user <br/>
 * log in user <br/>
 * open settings of user <br/>
 * change email and show email on vcard <br/>
 * assert activation e-mail for e-mail address confirmation was sent <br/>
 * logout user <br/>
 * admin deletes user <br/>
 * 
 *
 * @author alberto
 */
public class HomeSettingsTest extends BaseSeleneseTestCase {
	
	
	private final String USER_NAME = "emailtestdummyuser" + System.currentTimeMillis();
	private final String USER_FNAME = "First";
	private final String USER_LNAME = "Last";
	private final String USER_EMAIL = System.currentTimeMillis() + "@user.com";
	private final String USER_PW = "olat3";
	
	
	public void testHome_settings() throws Exception {
		
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

		OLATWorkflowHelper olatWorkflowAdmin = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		
		UserManagement userManagement = olatWorkflowAdmin.getUserManagement();
	   
		//create user
		userManagement.createUser(USER_NAME, USER_FNAME, USER_LNAME, USER_EMAIL, USER_PW);

		// log in user 
		OLATWorkflowHelper olatWorkflowUserToDelete = context.getOLATWorkflowHelper(context.getOlatLoginInfo(1, USER_NAME, USER_PW));
		
		//open settings of user
		MySettings mySettings = olatWorkflowUserToDelete.getHome().getUserSettings();
	
		selenium = mySettings.getSelenium();
		
		//change email and show email on vcard
		mySettings.setEmail("NewEmailOfStudent01@olat-newinstall.com", true);
		
		//assert activation e-mail for e-mail address confirmation was sent
		//TODO:LD: outcomment as soon as we have the email configured on olat instance: seleniumsinglevm & OLATNG (15.09.2010)
		//assertTrue(selenium.isTextPresent("E-mail sent successfully"));
		
		//user logout 
		olatWorkflowUserToDelete.logout();
		
		//admin deletes user
		userManagement.deleteUserImmediately(USER_NAME);
		olatWorkflowAdmin.logout(); //release possible locks
	}


  @Override
  protected void cleanUpAfterRun() {
    //login to release all possible locks
    OLATWorkflowHelper olatWorkflowAdmin = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
  }
	
	
}
