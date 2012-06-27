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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.instantMessaging;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

	/**
	 * Description:<br>
	 * Unit test for IM Preferences
	 * 
	 * <P>
	 * Initial Date:  12.08.2008 <br>
	 * @author guido
	 */
public class IMPrefsUnitTest extends OlatTestCase {
	private OLog log = Tracing.createLoggerFor(IMPrefsUnitTest.class);
	private String testUserA = "anIdentity1-" + UUID.randomUUID().toString();
	private String testUserB = "anIdentity2-" + UUID.randomUUID().toString();
	private String testUserC = "anIdentity3-" + UUID.randomUUID().toString();
	private String testUserD = "anIdentity4-" + UUID.randomUUID().toString();

	@Autowired
	private BaseSecurity securityManager;
	
	@Test
	public void testPrefs() {
		List<String> usernames = new ArrayList<String>();
		List<Identity> identities = new ArrayList<Identity>();
		usernames.add(testUserA);
		usernames.add(testUserB);
		usernames.add(testUserC);
		usernames.add(testUserD);
		
		for (Iterator<String> iterator = usernames.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			Identity ident = securityManager.findIdentityByName(name);
			assertNotNull(ident);
			identities.add(ident);
		}
		long start = System.currentTimeMillis();

		int runs = 0;
		while (runs  < 100) {
			double rand = Math.random()*3;
			int i = Long.valueOf((Math.round(rand))).intValue();
			ImPrefsManager mgr = ImPrefsManager.getInstance();
			Identity ident = identities.get(i);
			ImPreferences prefs = mgr.loadOrCreatePropertiesFor(ident);
			assertNotNull(prefs);
			assertNotNull(prefs.getDbProperty());
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				log.error("", e1);
			}
			for (Iterator<Identity> iterator = identities.iterator(); iterator.hasNext();) {
				ident = iterator.next();
				TaskExecutorManager.getInstance().runTask(new IMPrefsTask(ident));
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					log.error("", e);
				}
			}
			runs++;
		}
		
		long stop = System.currentTimeMillis();
		System.out.println("took time in s:"+(stop-start)/1000);
	}	
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before
	public void setup()throws Exception {
		JunitTestHelper.createAndPersistIdentityAsUser(testUserA);
		JunitTestHelper.createAndPersistIdentityAsUser(testUserB);
		JunitTestHelper.createAndPersistIdentityAsUser(testUserC);
		JunitTestHelper.createAndPersistIdentityAsUser(testUserD);
		DBFactory.getInstance().closeSession();
	}

	/**
	 * TearDown is called after each test
	 */
	@After public void tearDown() {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
		}
	}
}
