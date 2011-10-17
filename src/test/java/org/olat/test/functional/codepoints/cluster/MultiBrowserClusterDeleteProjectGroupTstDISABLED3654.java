package org.olat.test.functional.codepoints.cluster;

import org.junit.Ignore;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointClientFactory;
import org.olat.testutils.codepoints.client.CodepointRef;

@Ignore
public class MultiBrowserClusterDeleteProjectGroupTstDISABLED3654 extends BaseSeleneseTestCase {
	
    protected com.thoughtworks.selenium.Selenium selenium1;
    protected com.thoughtworks.selenium.Selenium selenium2;

    public void testMultiBrowserClusterNewLearningArea() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.RESTARTED_TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos user1 = context.createuserIfNotExists(1, "mbcnla1", standardPassword, true, true, true, true, true);
		OlatLoginInfos user2 = context.createuserIfNotExists(2, "mbcnla2", standardPassword, true, true, true, true, true);

		{
			System.out.println("logging in browser 1...");
			selenium1 = context.createSeleniumAndLogin(user1);
			selenium1.click("ui=tabs::groups()");
			selenium1.waitForPageToLoad("30000");
			assertEquals("OLAT - Groups", selenium1.getTitle());
			
			// delete the group if it already exists
			if (selenium1.isElementPresent("ui=groups::content_deleteGroup(nameOfGroup=deleteprojectgrouptest)")) {
				selenium1.click("ui=groups::content_deleteGroup(nameOfGroup=deleteprojectgrouptest)");
				selenium1.waitForPageToLoad("30000");
				selenium1.click("ui=groups::content_deleteYes()");
				selenium1.waitForPageToLoad("30000");
			}
			
			// create the group
			selenium1.click("ui=groups::toolbox_create_projectGroup()");
			selenium1.waitForPageToLoad("30000");
			assertEquals("OLAT - Groups", selenium1.getTitle());
			selenium1.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=Group name)", "deleteprojectgrouptest");
			selenium1.click("ui=commons::flexiForm_finishButton()");
			selenium1.waitForPageToLoad("30000");
			assertEquals("OLAT - deleteprojectgrouptest", selenium1.getTitle());
			selenium1.click("ui=tabs::closeGroup(nameOfGroup=deleteprojectgrouptest)");
			selenium1.waitForPageToLoad("30000");

			// add 'mbcnla2' as owner to the group
			selenium1.click("ui=groups::content_clickGroupEntry(nameOfGroup=deleteprojectgrouptest)");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=group::menu_administration()");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=group::content_members_tabMembers()");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=commons::usertable_owners_addUsers()");
			selenium1.waitForPageToLoad("30000");			
			selenium1.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=User name)", "mbcnla2");
			selenium1.click("ui=commons::flexiForm_genericLink(buttonLabel=Search)");
			selenium1.waitForPageToLoad("30000");
			selenium1.check("ui=commons::usertable_adduser_checkUsername(nameOfUser=mbcnla2)");
			selenium1.click("ui=commons::usertable_adduser_choose()");
			selenium1.waitForPageToLoad("30000");
			//selenium1.select("ui=commons::usertable_adduser_sendEmailSwitch()", "label=No");
			selenium1.click("ui=commons::usertable_adduser_finish()");
			selenium1.waitForPageToLoad("30000");
			selenium1.click("ui=tabs::closeGroup(nameOfGroup=deleteprojectgrouptest)");
			selenium1.waitForPageToLoad("30000");
			
			// click on 'delete group' now
			selenium1.click("ui=groups::content_deleteGroup(nameOfGroup=deleteprojectgrouptest)");
			selenium1.waitForPageToLoad("30000");
		}			
			
		
		{
			System.out.println("logging in browser 2...");
			selenium2 = context.createSeleniumAndLogin(user2);

			selenium2.click("ui=tabs::groups()");
			selenium2.waitForPageToLoad("30000");
			assertEquals("OLAT - Groups", selenium2.getTitle());
			
			// click on 'delete group' now
			selenium2.click("ui=groups::content_deleteGroup(nameOfGroup=deleteprojectgrouptest)");
			selenium2.waitForPageToLoad("30000");
		}
		
		CodepointClient codepointClientA = context.createCodepointClient(1);
		CodepointRef createAreaCpA = codepointClientA.getCodepoint("org.olat.group.BusinessGroupManagerImpl.deleteBusinessGroupWithMail");
		createAreaCpA.setHitCount(0);
		createAreaCpA.enableBreakpoint();
		
		CodepointClient codepointClientB = context.createCodepointClient(2);
		CodepointRef createAreaCpB = codepointClientB.getCodepoint("org.olat.group.BusinessGroupManagerImpl.deleteBusinessGroupWithMail");
		createAreaCpB.setHitCount(0);
		createAreaCpB.enableBreakpoint();
		
		selenium1.click("ui=groups::content_deleteYes()");		
		selenium2.click("ui=groups::content_deleteYes()");		
		
		createAreaCpA.assertBreakpointReached(1, 10000);
		createAreaCpB.assertBreakpointReached(1, 10000);
		
		createAreaCpA.disableBreakpoint(true);
		createAreaCpB.disableBreakpoint(true);
		
		selenium1.waitForPageToLoad("30000");
		selenium2.waitForPageToLoad("30000");
		
		// check that the group is gone
		// note[se]: since the test currently fails with an exception, it is hard to tell what the exact assertion here should be for the OK case...
		//           one of the two should probably get a warning saying that the group was deleted by someone else at the same time - or nothing, since it was successfully deleted - just not by the person him/herself...
		assertFalse(selenium1.isElementPresent("ui=groups::content_clickGroupEntry(nameOfGroup=deleteprojectgrouptest)"));
		assertFalse(selenium2.isElementPresent("ui=groups::content_clickGroupEntry(nameOfGroup=deleteprojectgrouptest)"));
	}
}
