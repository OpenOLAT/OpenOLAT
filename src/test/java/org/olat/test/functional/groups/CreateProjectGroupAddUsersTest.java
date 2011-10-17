package org.olat.test.functional.groups;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * User creates course and tests case tests if he can add owner and participants.
 * <br/>
 * Test setup: <br/>
 * 1. Clean-up: delete all groups from author
 * 2. Create test user
 * <p>
 * Test case: <br/>
 * 1. author creates group GROUP_NAME and adds users and owners
 * 2. student logs in, checks if he's in GROUP_NAME and leaves GROUP_NAME	
 * 3. author deletes group <br/>
 * 
 * 
 * @author sandra
 * 
 */

public class CreateProjectGroupAddUsersTest extends BaseSeleneseTestCase {
	
	public void testCreateProjectGroupAddUsersTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		String standardPassword = context.getStandardStudentOlatLoginInfos(1).getPassword();
		OlatLoginInfos author02= context.createuserIfNotExists(1, "testauthor02", standardPassword, true, false, false, false, false);
		
		// delete all my groups first !!!
		WorkflowHelper.deleteAllGroupsFromAuthor(author02);
		
		// author creates group and adds users and owners		
		OLATWorkflowHelper oLATWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		Groups groupsTab = oLATWorkflowHelper.getGroups();
		GroupAdmin groupAdmin = groupsTab.createProjectGroup("project group selenium 3", "selenium 3");
		String[] owners = {"testauthor02"};
		String[] participants = {context.getStandardStudentOlatLoginInfos(1).getUsername()};
		groupAdmin.addMembers(participants, owners);
		oLATWorkflowHelper.logout();
		
		
		//student logs in and leaves group	
		oLATWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(1));
		groupsTab = oLATWorkflowHelper.getGroups();
		assertFalse(groupsTab.getSelenium().isElementPresent("ui=groups::content_deleteGroup(nameOfGroup=project group selenium 3)"));
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (groupsTab.getSelenium().isElementPresent("ui=groups::content_leaveGroup(nameOfGroup=project group selenium 3)")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		groupsTab.leaveGroup("project group selenium 3");
		oLATWorkflowHelper.logout();
		
		
		// testauthor02 deletes group		
		oLATWorkflowHelper = context.getOLATWorkflowHelper(author02);
		oLATWorkflowHelper.getGroups().deleteGroup("project group selenium 3");
		oLATWorkflowHelper.logout();		
		assertEquals("OLAT - Online Learning And Training", oLATWorkflowHelper.getSelenium().getTitle());
	}
}
