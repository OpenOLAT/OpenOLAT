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
package org.olat.test.tutorial.singlenode.junit;

import org.olat.test.util.selenium.BaseSeleneseTestCase;
import org.olat.test.util.setup.SetupType;
import org.olat.test.util.setup.context.Context;
import org.olat.testutils.codepoints.client.CodepointClient;
import org.olat.testutils.codepoints.client.CodepointRef;
import org.olat.testutils.codepoints.client.TemporaryPausedThread;

public class MacartneyBreakpointTest extends BaseSeleneseTestCase {

    protected com.thoughtworks.selenium.Selenium selenium1;
    protected com.thoughtworks.selenium.Selenium selenium2;
	private CodepointClient codepointClient_;

	public void testMacartneyBreakpoint() throws Exception {
		//if instance restart (SetupType.CLEAN_AND_RESTARTED_SINGLE_VM) is not implemented this test is not relevat!
		if(true) {
			return;
		}
		
		
		Context context = Context.setupContext(getFullName(), SetupType.CLEAN_AND_RESTARTED_SINGLE_VM);
//		selenium1 = context.createSeleniumAndLogin(context.getStandardAdminOlatLoginInfos());
//		selenium2 = context.createSeleniumAndLogin(context.getStandardStudentOlatLoginInfos());
		
		selenium1 = context.createSelenium();
		selenium2 = context.createSelenium();
		
		// setup and get ready
    	selenium1.openWindow(context.getStandardAdminOlatLoginInfos().getFullOlatServerUrl(), "olat");
    	selenium2.openWindow(context.getStandardStudentOlatLoginInfos().getFullOlatServerUrl(), "olat");
    	System.out.println("olatLogin: waiting 5sec.");
		Thread.sleep(5000);
		System.out.println("olatLogin: selecting the olat window");
		selenium1.selectWindow("olat");
		selenium2.selectWindow("olat");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			System.out.println("title now: "+selenium1.getTitle());
			if (selenium1.getTitle().indexOf("Error")!=-1) {
				System.out.println(selenium1.getBodyText());
				fail("Error encountered in selenium1");
			}
			try { if ("OLAT - Online Learning And Training".equals(selenium1.getTitle())) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			System.out.println("title now: "+selenium2.getTitle());
			if (selenium2.getTitle().indexOf("Error")!=-1) {
				System.out.println(selenium2.getBodyText());
				fail("Error encountered in selenium2");
			}
			try { if ("OLAT - Online Learning And Training".equals(selenium2.getTitle())) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}		
		selenium1.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=OLAT user name)", context.getStandardAdminOlatLoginInfos().getUsername());
		selenium1.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=OLAT password)", context.getStandardAdminOlatLoginInfos().getPassword());
		
		selenium2.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=OLAT user name)", context.getStandardStudentOlatLoginInfos().getUsername());
		selenium2.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=OLAT password)", context.getStandardStudentOlatLoginInfos().getPassword());
		
		
		codepointClient_ = context.createCodepointClient(1);
		codepointClient_.setAllHitCounts(0);
		CodepointRef beforeSyncCp = codepointClient_.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-before-sync.org.olat.portal.macartney.MacartneyPortletRunController.<init>");
		beforeSyncCp.setHitCount(0);
		beforeSyncCp.enableBreakpoint();
		
		CodepointRef inSyncCp = codepointClient_.getCodepoint("org.olat.commons.coordinate.cluster.ClusterSyncer.doInSync-in-sync.org.olat.portal.macartney.MacartneyPortletRunController.<init>");
		inSyncCp.setHitCount(0);
		
		//MacartneyPortletRunController
		
		selenium1.click("ui=dmz::login()");
		selenium2.click("ui=dmz::login()");
		
		Thread.sleep(10000);
		inSyncCp.assertHitCount(0);
		beforeSyncCp.assertHitCount(2);
		
		TemporaryPausedThread[] threads = beforeSyncCp.getPausedThreads();
		assertNotNull(threads);
		assertEquals(2, threads.length);
		
		// continue the first
		threads[0].continueThread();
		Thread.sleep(500);
		
		threads = beforeSyncCp.getPausedThreads();
		assertNotNull(threads);
		assertEquals(1, threads.length);
		inSyncCp.assertHitCount(1);
		
		//selenium1.waitForPageToLoad("30000");
		//assertEquals("OLAT-Home", selenium.getTitle());
	}
	
}
