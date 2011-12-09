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
package org.olat.test.functional.groups;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.selenium.olatapi.group.Group;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.SeleniumException;

/**
 * 
 * Group owner and participant test project group concurrencies.
 * <br/>
 * Test setup: <br/>
 * 1. Clean-up: delete all groups from author <br/>
 * <p>
 * Test case: <br/>
 * 1. author creates group GROUP_NAME with group tool wiki <br/>
 * 2. author adds participant <br/>
 * 3. student logs in, check if he can select wiki <br/>
 * 4. author removes wiki <br/>
 * 5. student checks that wiki is no longer available and that he gets appropriate error message <br/>
 * 6. author deletes group <br/>
 * 
 * 
 * @author sandra
 * 
 */

public class GroupConcurrenciesTest extends BaseSeleneseTestCase {
	
    public void testGroupConcurrencies() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		// delete all my groups first !!!
		WorkflowHelper.deleteAllGroupsFromAuthor(context.getStandardAuthorOlatLoginInfos(1));
		
		// Author01 creates project group with wiki
		System.out.println("logging in browser 1...");			
		OLATWorkflowHelper oLATWorkflowHelper1 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		GroupAdmin groupAdmin1 = oLATWorkflowHelper1.getGroups().createProjectGroup("project group selenium 4", "");
		groupAdmin1.setTools(false, false, false, false, false, true, false);
		String[] participants = {context.getStandardStudentOlatLoginInfos(1).getUsername()};
		groupAdmin1.addMembers(participants, new String[0]);
		groupAdmin1.close("project group selenium 4");

		// student01 opens group with wiki
		System.out.println("logging in browser 2...");
		OLATWorkflowHelper oLATWorkflowHelper2 = context.getOLATWorkflowHelper(context.getStandardStudentOlatLoginInfos(2));
		Group group2 = oLATWorkflowHelper2.getGroups().selectGroup("project group selenium 4");
		group2.selectWiki();		
				
		// Author01 removes wiki 
		groupAdmin1 = oLATWorkflowHelper1.getGroups().selectGroup("project group selenium 4").selectAdministration();
		groupAdmin1.setTools(false, false, false, false, false, true, false);
				
		// Student01 wants to click on Wiki, but wiki was removed by the group owner		
		if(group2.hasWiki()) {
		  group2.selectWiki();
		}		
		Thread.sleep(10000);
		//wiki dissapears silently/or not (why behaviour changes?) with a certain delay after removal
		assertFalse(group2.hasWiki());
				
		// Author01 deletes group 
		oLATWorkflowHelper1.getGroups().deleteGroup("project group selenium 4");		

		//student clicks on group and gets appropriate message
		try{
			if (group2.getSelenium().isElementPresent("ui=group::menu_members()")) {
				group2.selectMembers();					
			}
		} catch(SeleniumException e) {
			// ok - ajax could come and refresh the group tab and notice that it has been modified right
			// after we asked 'isElementPresent' .. hence not doing anything with this exception!
		}
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (group2.isTextPresent("This group's configuration has been modified (group deleted, members changed). Please close the tab.")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}						
	}
}
