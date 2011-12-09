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
package org.olat.test.functional.administration;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.selenium.olatapi.admin.Administration;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * test info message occurences when changed on other node
 * 
 * @author Guido
 *
 */
public class InfoMessageClusterTest extends BaseSeleneseTestCase {
	
    private final String MESSAGE_1 =  "may the force be with you!";
    
    public void testInfoMessage() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);
		
		OLATWorkflowHelper workflow1 = context.getOLATWorkflowHelper(context.getStandardAdminOlatLoginInfos(1));
		OLATWorkflowHelper workflow2 = context.getOLATWorkflowHelper(context.getStandardAuthorOlatLoginInfos(2));
		
		Administration admin1 = workflow1.getAdministration();
		admin1.editInfoMessage(MESSAGE_1);
		assertTrue("Assert that the info message was set.", admin1.hasInfoMessage(MESSAGE_1));
		
		workflow2.logout();    	
		assertTrue(workflow2.getSelenium().isTextPresent(MESSAGE_1));
		
		workflow1.logout();		
		assertTrue(workflow1.getSelenium().isTextPresent(MESSAGE_1));   
		
		//TODO: change message
		
		//TODO: reset message
    	
	}

	@Override
	protected void cleanUpAfterRun() {
		OLATWorkflowHelper workflow1 = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(1));
		workflow1.getAdministration().editInfoMessage("");
	}
    
    

}
