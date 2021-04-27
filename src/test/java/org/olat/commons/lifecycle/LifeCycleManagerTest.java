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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Mar 11, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class LifeCycleManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	/**
	 * Test creation of LifeCycleManager.
	 */
	@Test
	public void testCreateInstanceFor() {
		OLATResource res = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("life-1-");
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "life cycled group 1", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();

		LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(group);
		LifeCycleManager lcm2 = LifeCycleManager.createInstanceFor(res);
		assertNotSame("testCreateInstanceFor should NOT return the same instance", lcm1, lcm2);
	}
	
	/**
	 * Test: mark two timestamp in different context.
	 */
	@Test
	public void testMarkTimestampFor() {
		OLATResource res = JunitTestHelper.createRandomResource();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("life-2-");
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "life cycled group 2", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		String action = "doTest";
		LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(group);
		LifeCycleManager lcm2 = LifeCycleManager.createInstanceFor(res);
		lcm1.markTimestampFor(action);
		lcm2.markTimestampFor(action);
		dbInstance.closeSession();
		
		LifeCycleEntry lce = lcm1.lookupLifeCycleEntry(action, null);
		assertNotNull("Does not found LifeCycleEntry", lce);
		assertEquals("Invalid action",lce.getAction(), action);
		// try second instance of LifeCycleManager
		LifeCycleEntry lce2 = lcm2.lookupLifeCycleEntry(action, null);
		assertNotNull("Does not found LifeCycleEntry", lce2);
		assertNotSame("LifeCycleEntry have not the same reference", lce2.getPersistentRef(), lce.getPersistentRef());
		assertNotSame("LifeCycleEntry have not the same type-name",lce2.getPersistentTypeName(), lce.getPersistentTypeName());
		
		//check if has
		 Assert.assertTrue(lcm1.hasLifeCycleEntry(action));
		 Assert.assertTrue(lcm2.hasLifeCycleEntry(action));
	}
	
	/**
	 * Test: Delete Timestamp for certain action
	 */
	@Test
	public void testDeleteTimestampFor() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("life-3-");
		BusinessGroup group = businessGroupService.createBusinessGroup(identity, "life cycled group 3", "a desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, null);
		dbInstance.commitAndCloseSession();
		
		String action = "doTestDelete";
		LifeCycleManager lcm1 = LifeCycleManager.createInstanceFor(group);
		lcm1.markTimestampFor(action);
		LifeCycleEntry lce = lcm1.lookupLifeCycleEntry(action, null);
		assertNotNull("Does not found LifeCycleEntry", lce);
		lcm1.deleteTimestampFor(action);
		LifeCycleEntry lce2 = lcm1.lookupLifeCycleEntry(action, null);
		assertNull("Found deleted LifeCycleEntry", lce2);
	}
}