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

package org.olat.commons.lifecycle;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Initial Date:  Mar 11, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class LifeCycleManagerTest extends OlatTestCase implements OLATResourceable {

	private long RESOURCE_ID = 144;
	private String RESOURCE_TYPE = "org.olat.commons.lifecycle.LifeCycleManagerTest";
	private BusinessGroupManager gm;
	private static Logger log = Logger.getLogger(LifeCycleManagerTest.class);
	private static boolean isInitialized = false;
	private static Identity identity = null;
	private static BusinessGroup group = null;
	private static org.olat.resource.OLATResource res = null;


	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup() {

				// identity with null User should be ok for test case
				res = OLATResourceManager.getInstance().createOLATResourceInstance(this);
				OLATResourceManager.getInstance().saveOLATResource(res);
				identity = JunitTestHelper.createAndPersistIdentityAsUser("foo");
				gm = BusinessGroupManagerImpl.getInstance();
				group = gm.createAndPersistBusinessGroup(BusinessGroup.TYPE_BUDDYGROUP, 
				identity, "a buddygroup", "a desc", null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, null);

	}
	
	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@After public void tearDown() {
		try {
			OLATResourceManager.getInstance().deleteOLATResource(res);
			gm.deleteBusinessGroup(group);
			Tracing.logInfo("tearDown: DB.getInstance().closeSession()", this.getClass());
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}
	
	/**
	 * Test creation of LifeCycleManager.
	 */
	@Test public void testCreateInstanceFor() {
		LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(group);
		LifeCycleManager lcm2 = LifeCycleManager.createInstanceFor(res);
		assertNotSame("testCreateInstanceFor should NOT return the same instance", lcm1,lcm2);
	}
	
	/**
	 * Test: mark two timestamp in different context.
	 */
	@Test public void testMarkTimestampFor() {
		String action = "doTest";
		LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(group);
		LifeCycleManager lcm2 = LifeCycleManager.createInstanceFor(res);
		lcm1.markTimestampFor(action);
		lcm2.markTimestampFor(action);
		DBFactory.getInstance().closeSession();
		LifeCycleEntry lce = lcm1.lookupLifeCycleEntry(action, null);
		assertNotNull("Does not found LifeCycleEntry", lce);
		assertEquals("Invalid action",lce.getAction(), action);
		// try second instance of LifeCycleManager
		LifeCycleEntry lce2 = lcm2.lookupLifeCycleEntry(action, null);
		assertNotNull("Does not found LifeCycleEntry", lce2);
		assertNotSame("LifeCycleEntry have not the same reference", lce2.getPersistentRef(), lce.getPersistentRef());
		assertNotSame("LifeCycleEntry have not the same type-name",lce2.getPersistentTypeName(), lce.getPersistentTypeName());
	}
	
	/**
	 * Test: Delete Timestamp for certain action
	 */
	@Test public void testDeleteTimestampFor() {
		String action = "doTestDelete";
		LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(group);
		lcm1.markTimestampFor(action);
		LifeCycleEntry lce = lcm1.lookupLifeCycleEntry(action, null);
		assertNotNull("Does not found LifeCycleEntry", lce);
		lcm1.deleteTimestampFor(action);
		LifeCycleEntry lce2 = lcm1.lookupLifeCycleEntry(action, null);
		assertNull("Found deleted LifeCycleEntry", lce2);
	
	}

	//////////////////////////////
	// Implements OLATResourceable
	//////////////////////////////
	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return RESOURCE_TYPE;
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		return new Long(RESOURCE_ID);
	}
}
