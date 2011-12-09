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
package org.olat.test.tutorial.reg;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.selenium.OlatLoginHelper;
import org.olat.test.util.selenium.SeleniumManager;
import org.olat.test.util.selenium.olatapi.WorkflowHelper;
import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * This class tests 10x a singlevm server start and 10x a cluster server start
 * .
 * @author eglis
 */
public class ServerRestartTest extends BaseSeleneseTestCase {
	
	public void testRestarts() throws Exception {
		for(int i=0; i<5; i++) {
			Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_TWO_NODE_CLUSTER);
			Selenium s = context.createSeleniumAndLogin();
			s.close();
			s.stop();
			Context.tearDown();
		}
		for(int i=0; i<5; i++) {
			Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);
			Selenium s = context.createSeleniumAndLogin();
			s.close();
			s.stop();
			Context.tearDown();
		}
		Context.setupContext(getFullName(), SetupType.SINGLE_VM);
	}
}
