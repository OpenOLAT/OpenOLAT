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
*/
package org.olat.instantMessaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olat.test.MockServletContextWebContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Instant Messaging integration JUnit Tests, relying on a working IM OpenFire Jabberserver 
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
		"classpath:org/olat/instantMessaging/_spring/instantMessagingTestContext.xml" })
public class IMUnitTestWithoutOLAT extends AbstractJUnit4SpringContextTests {

	@Test
	public void testNormal() {
		/**
		 * Precondition
		 */
		IMConfig config = (IMConfig) applicationContext.getBean("org.olat.im.IMConfig");
		assertNotNull(config);
		// only run IM tests if enabled
		assumeTrue(config.isEnabled());

		/**
		 * test
		 */
		InstantMessaging im = (InstantMessaging) applicationContext.getBean("org.olat.instantMessaging.InstantMessaging");
		assertNotNull(im);
		assertTrue(im.getConfig().isEnabled());
		
		String username = "unittest";
		String password = "test";
		String fullname = "test test";
		String email = "@test.ch";
		String groupId = "testgroup-1234556";
		String groupname = "testgroupABC";
		
		groupId = im.getNameHelper().getGroupnameForOlatInstance(groupId);

		// test api functions that do not need OLAT runtime
		String tmpUsermaster = username + 0;
		for (int j = 0; j < 4; j++) {
			String tmpUsername = username + j;
			assertFalse(im.hasAccount(tmpUsername));
			assertTrue(im.createAccount(tmpUsername, password, fullname, username + j + email));
			assertTrue(im.hasAccount(tmpUsername));
			List<String> userToAdd = new ArrayList<String>();
			userToAdd.add(tmpUsermaster);
			userToAdd.add(tmpUsername);
			assertTrue(im.syncFriendsRoster(groupId, groupname, userToAdd, null));
		}
		assertTrue(im.renameRosterGroup(groupId, groupname + "CDEF"));
		
		List<String> userToRemove = new ArrayList<String>();
		userToRemove.add(tmpUsermaster);
		userToRemove.add(username + 1);
		assertTrue(im.syncFriendsRoster(groupId, groupname, null, userToRemove));
		assertTrue(im.deleteRosterGroup(groupId));
		for (int j = 0; j < 4; j++) {
			String tmpUsername = username + j;
			assertTrue(im.deleteAccount(tmpUsername));
		}	
	}

}