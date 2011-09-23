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
* <p>
*/
package org.olat.instantMessaging;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import junit.framework.TestSuite;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

	/**
	 * Description:<br>
	 * Unit test for IM Preferences
	 * 
	 * <P>
	 * Initial Date:  12.08.2008 <br>
	 * @author guido
	 */
	public class IMPrefsUnitTest extends OlatTestCase {
		OLog log = Tracing.createLoggerFor(this.getClass());
		String testUserA = "anIdentity1";
		String testUserB = "anIdentity2";
		String testUserC = "anIdentity3";
		String testUserD = "anIdentity4";
		

		
	@SuppressWarnings("unchecked")
	@Test public void testPrefs() {
		List usernames = new ArrayList();
		List indentities = new ArrayList();
		usernames.add(testUserA);
		usernames.add(testUserB);
		usernames.add(testUserC);
		usernames.add(testUserD);
		
		
		for (Iterator iterator = usernames.iterator(); iterator.hasNext();) {
			String name = (String) iterator.next();
			Identity ident = BaseSecurityManager.getInstance().findIdentityByName(name);
			assertNotNull(ident);
			indentities.add(ident);
		}
		long start = System.currentTimeMillis();

		int runs = 0;
		while (runs  < 100) {
			double rand = Math.random()*3;
			int i = Long.valueOf((Math.round(rand))).intValue();
			ImPrefsManager mgr = ImPrefsManager.getInstance();
			Identity ident = (Identity)indentities.get(i);
			ImPreferences prefs = mgr.loadOrCreatePropertiesFor((Identity)indentities.get(i));
			assertNotNull(prefs);
			assertNotNull(prefs.getDbProperty());
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for (Iterator iterator = indentities.iterator(); iterator.hasNext();) {
				ident = (Identity) iterator.next();
				TaskExecutorManager.getInstance().runTask(new IMPrefsTask(ident));
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	@Before public void setup()throws Exception {
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
			DB db = DBFactory.getInstance();
			db.closeSession();
		} catch (Exception e) {
			Tracing.logError("Exception in tearDown(): " + e, IMUnitTest.class);
		}
	}
	
}
