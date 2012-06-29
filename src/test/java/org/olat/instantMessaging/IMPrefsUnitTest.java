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
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
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

	@Autowired
	private DB dbInstance;
	@Autowired
	private ImPrefsManager imPrefsManager;
	

	@After
	public void tearDown() {
		try {
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
		}
	}
	
	@Test
	public void testPrefs() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsUser("im-1-" + UUID.randomUUID().toString());
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("im-1-" + UUID.randomUUID().toString());
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsUser("im-1-" + UUID.randomUUID().toString());
		Identity id4 = JunitTestHelper.createAndPersistIdentityAsUser("im-1-" + UUID.randomUUID().toString());

		List<Identity> identities = new ArrayList<Identity>();
		identities.add(id1);
		identities.add(id2);
		identities.add(id3);
		identities.add(id4);
		dbInstance.commitAndCloseSession();
		
		long start = System.currentTimeMillis();

		for (int runs=0; runs<100; runs++) {
			double rand = Math.random() * 3.0d;
			int i = Long.valueOf((Math.round(rand))).intValue();
			
			Identity randomIdentity = identities.get(i);
			ImPreferences prefs = imPrefsManager.loadOrCreatePropertiesFor(randomIdentity);
			assertNotNull(prefs);
			assertNotNull(prefs.getDbProperty());
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				log.error("", e1);
			}
			for (Identity identity:identities) {
				TaskExecutorManager.getInstance().runTask(new IMPrefsTask(identity));
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					log.error("", e);
				}
			}
		}
		
		long stop = System.currentTimeMillis();
		System.out.println("took time in s:"+(stop-start)/1000);
	}
}