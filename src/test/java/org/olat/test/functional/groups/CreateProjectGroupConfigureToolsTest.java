package org.olat.test.functional.groups;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.group.Group;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * User creates course and configures group tools.
 * <br/>
 * Test setup: <br/>
 * 1. Clean-up: delete all groups from author
 * <p>
 * Test case: <br/>
 * 1. author creates group GROUP_NAME and adds student as participant.<br/>
 * 2. author changes group description, activates all group tools, sets info, sets calendar writing access. <br/>
 * 3. student logs in, checks if all tools are available as configured. <br/>
 * 4. student leaves group.<br/>
 * 5. author deletes group. <br/>
 * 
 * @author sandra
 * 
 */

public class CreateProjectGroupConfigureToolsTest extends BaseSeleneseTestCase {
	
	private final String GROUP_NAME_CHANGED = "project group selenium 2 changed";

	public void testCourseOpenClose() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		// delete all my groups first !!!
		WorkflowHelper.deleteAllGroupsFromAuthor(context.getStandardAuthorOlatLoginInfos(1));

		//author creates group and adds user		
		OLATWorkflowHelper olatWorkflow1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		Groups groups1 = olatWorkflow1.getGroups();
		GroupAdmin group = groups1.createProjectGroup("project group selenisum 2", "second test");
		group.setTitleAndDescription(GROUP_NAME_CHANGED, "description changed");
		group.setTools(true, true, true, true, true, true, false);
		group.setInfo("hello everybody");
		group.selectCalendarWriteAccess("Owners and tutors respectively");
		String[] userNames = {context.getStandardStudentOlatLoginInfos(1).getUsername()};
		group.addMembers(userNames, new String[0]);
		olatWorkflow1.logout();
		
    // user clicks all tools and leaves group		
		OLATWorkflowHelper olatWorkflow2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		Group myGroup = olatWorkflow2.getHome().selectMyGroup(GROUP_NAME_CHANGED);
		myGroup.selectInfo();
		assertTrue(myGroup.isTextPresent("hello everybody"));
		myGroup.selectCalendar();
		Thread.sleep(3000);
		assertTrue(myGroup.getSelenium().isElementPresent("ui=group::menu_calendar_readOnly()"));
		myGroup.selectMembers();
		myGroup.selectEmail();
		myGroup.selectFolder();		
		myGroup.selectWiki();
		myGroup.selectForum();
		
		Groups groups2 = olatWorkflow2.getGroups();		
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (groups2.getSelenium().isElementPresent("ui=groups::content_leaveGroup(nameOfGroup=" + GROUP_NAME_CHANGED + ")")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		groups2.leaveGroup(GROUP_NAME_CHANGED);
		olatWorkflow2.logout();	
		
    //author deletes group		
		olatWorkflow1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		olatWorkflow1.getGroups().deleteGroup(GROUP_NAME_CHANGED);
		olatWorkflow1.logout();		
	}
	
			
}
