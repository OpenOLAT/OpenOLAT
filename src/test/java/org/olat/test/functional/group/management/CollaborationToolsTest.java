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
package org.olat.test.functional.group.management;

import org.junit.Ignore;
import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.group.GroupAdmin;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

/**
 * test groupChat stuff and polling (changing interval)
 * 
 * @author Guido
 *
 */
@Ignore
public class CollaborationToolsTest extends BaseSeleneseTestCase {
	
	
    private OLATWorkflowHelper workflow1;
    private OLATWorkflowHelper workflow2;
    
    public void testCourseChat() throws Exception {
    	Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);

    	workflow1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
    	workflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));

    	String nameOfGroup ="junittest-"+System.currentTimeMillis();
    	//browser 1
    	GroupAdmin groupAdmin1 = workflow1.getGroups().createProjectGroup(nameOfGroup, "junittest");
    	groupAdmin1.setTools(true, true, true, true, true, false, true);
    	String[] owners = {context.getStandardAuthorOlatLoginInfos(2).getUsername()};
    	groupAdmin1.addMembers(new String[0], owners);
    	groupAdmin1.close(nameOfGroup);

    	//browser 2
    	GroupAdmin groupAdmin2 = workflow2.getGroups().selectGroup(nameOfGroup).selectAdministration();
    	assertTrue(groupAdmin2.isChatSelected());
    	groupAdmin2.setTools(false, false, false, false, false, true, false);
    	groupAdmin2.close(nameOfGroup);

    	//browser 1
    	groupAdmin1 = workflow1.getGroups().selectGroup(nameOfGroup).selectAdministration();
    	assertTrue(groupAdmin1.isWikiSelected());

    }

}
