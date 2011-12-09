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
import org.olat.test.util.selenium.olatapi.group.Groups;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * 
 * User creates and deletes project group.
 * <br/>
 * Test setup: <br/>
 * 1. Clean-up: missing?
 * <p>
 * Test case: <br/>
 * 1. author creates group GROUP_NAME 
 * 2. author deletes group <br/>
 * 
 * 
 * @author sandra
 * 
 */
public class CreateProjectGroupTest extends BaseSeleneseTestCase {
	
	public void testCreateProjectGroupTest() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
				
		OLATWorkflowHelper oLATWorkflowHelper = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(1));
		oLATWorkflowHelper.getGroups().createProjectGroup("project group selenium 1", "");
		Groups groups = oLATWorkflowHelper.getGroups();
		assertTrue(groups.isTextPresent("selenium 1"));
		groups.deleteGroup("project group selenium 1");
		assertFalse(groups.isTextPresent("selenium 1"));
		oLATWorkflowHelper.logout();
		assertEquals("OLAT - Online Learning And Training", oLATWorkflowHelper.getSelenium().getTitle());				
	}
}
