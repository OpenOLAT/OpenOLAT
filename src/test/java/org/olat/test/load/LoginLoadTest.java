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
package org.olat.test.load;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.olatapi.OLATWorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * Performance test; it measures the time needed to login and logout "iterations" times, using the same browser window. <br/>
 * 
 * Test case: <br/>
 * Login/logout on one node numOfIterations times.
 * 
 * @author lavinia
 *
 */
public class LoginLoadTest extends BaseSeleneseTestCase {

	private int numOfIterations = 80;

	public void setUp() throws Exception {
		Context context = Context.setupContext(getFullName(), SetupType.TWO_NODE_CLUSTER);     	
	}

	/**
	 * We would like to login on 2 different nodes, but depends on the multiVmOlatUrl1, multiVmOlatUrl2 whether
	 * we really get to login in two different nodes.
	 * 
	 * @throws Exception
	 */
	public void testLoginForEachNode() throws Exception {
		long duration1 = loginOnOneNode(1);
		long duration2 = loginOnOneNode(2);
		System.out.println("$$$ login comparison - duration1: " + duration1/1000 + " s");
		System.out.println("$$$ login comparison - duration2: " + duration2/1000 + " s");
	}

	/**
	 * Login/logout on one node iterations times.
	 * 
	 * @param nodeId
	 * @return
	 * @throws Exception
	 */
	private long loginOnOneNode(int nodeId) throws Exception {
		long initialTime = System.currentTimeMillis();
		OLATWorkflowHelper workflow = Context.getContext().getOLATWorkflowHelper(Context.getContext().getStandardAdminOlatLoginInfos(nodeId));
		workflow.logout();
		int iterationCounter = 0;
		for(int i=0; i<numOfIterations; i++) {
			simpleLoginSameBrowserWindow(workflow.getSelenium(), Context.getContext().getStandardAdminOlatLoginInfos(nodeId));
			iterationCounter++;
			workflow.logout();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - initialTime;
		System.out.println("loginOnOneNode " + nodeId + " took: " + duration/1000 + " s --- " + iterationCounter + " times");
		return duration;
	}

	/**
	 * 
	 * @param selenium
	 * @param loginInfos
	 * @throws Exception
	 */
	private void simpleLoginSameBrowserWindow(Selenium selenium, OlatLoginInfos loginInfos) throws Exception {
		String username = loginInfos.getUsername();
		String password = loginInfos.getPassword();

		OlatLoginHelper.inputUserNameAndPassword(selenium, username, password);
		int second = 0;
		while(second<20) {
			try {
				if (selenium.isElementPresent("ui=home::menu_settings()")) //logged in
					break;
			} catch (Exception e) {
			}
			Thread.sleep(500);
			second++;
		}
		assertTrue(selenium.isElementPresent("ui=home::menu_settings()"));
	}

}
