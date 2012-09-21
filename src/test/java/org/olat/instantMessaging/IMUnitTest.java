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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.instantMessaging.ui.ConnectedUsersListEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * IM junit tests
 * 
 * <P>
 * Initial Date:  Nov 16, 2006 <br>
 * @author guido
 */
public class IMUnitTest extends OlatTestCase {
	private static final OLog log = Tracing.createLoggerFor(OlatTestCase.class);
	
	private final String testUserA = "anIdentity1";
	private final String testUserB = "anIdentity2";
	private final String testUserC = "testuser@thankyou2010.com";
	
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup() throws Exception {
		JunitTestHelper.createAndPersistIdentityAsUser(testUserA);
		JunitTestHelper.createAndPersistIdentityAsUser(testUserB);
		JunitTestHelper.createAndPersistIdentityAsUser(testUserC);
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
			log.error("Exception in tearDown(): ", e);
		}
	}
	
	@Test
	public void testIMStuff() {
		if(InstantMessagingModule.isEnabled()) {
			try {
			String groupIdPlain = "junittestgroup-12343w5234";
			String groupName = "junittestgroup";
			InstantMessaging im = InstantMessagingModule.getAdapter();
			String groupId = im.getNameHelper().getGroupnameForOlatInstance(groupIdPlain);
			
			//first delete possible accounts and groups on the IM server
			InstantMessagingModule.getAdapter().deleteAccount(testUserA);
			InstantMessagingModule.getAdapter().deleteAccount(testUserB);
			InstantMessagingModule.getAdapter().deleteAccount(testUserC);
			im.deleteRosterGroup(groupId);
			
			Authentication authC = BaseSecurityManager.getInstance().findAuthenticationByAuthusername(testUserC, ClientManager.PROVIDER_INSTANT_MESSAGING);
			if(authC != null) BaseSecurityManager.getInstance().deleteAuthentication(authC);
			DBFactory.getInstance().intermediateCommit();
			InstantMessagingClient imClientC = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(testUserC);
			//wait some time as connection process is in background thread
			Thread.sleep(3000);
			assertTrue(imClientC.isConnected());
			imClientC.closeConnection(true);
			assertTrue(InstantMessagingModule.getAdapter().deleteAccount(testUserC));
			
			//delete IM passwords, otherwise accounts don't get created
			Authentication authA = BaseSecurityManager.getInstance().findAuthenticationByAuthusername(testUserA, ClientManager.PROVIDER_INSTANT_MESSAGING);
			Authentication authB = BaseSecurityManager.getInstance().findAuthenticationByAuthusername(testUserB, ClientManager.PROVIDER_INSTANT_MESSAGING);
			
			if(authA != null) BaseSecurityManager.getInstance().deleteAuthentication(authA);
			if(authB != null) BaseSecurityManager.getInstance().deleteAuthentication(authB);
					
			//get the IM client, it connects automatically to the server (creates an account on the im server)
			InstantMessagingClient imClientA = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(testUserA);
			assertNotNull(imClientA);
			InstantMessagingClient imClientB = InstantMessagingModule.getAdapter().getClientManager().getInstantMessagingClient(testUserB);
			assertNotNull(imClientB);
			Thread.sleep(1000);
			assertEquals(true, imClientA.isConnected());
			int groupCountA = imClientA.getRoster().getGroupCount();
			assertEquals(true, imClientB.isConnected());
			
			assertTrue(im.countConnectedUsers() >= 2); //there is may be as well an admin user connected
			
			//add user to roster
			List<String> userToAdd = new ArrayList<String>(2);
			userToAdd.add(testUserA);
			userToAdd.add(testUserB);
			im.syncFriendsRoster(groupId, groupName, userToAdd, null);
			Thread.sleep(1000);
			assertEquals(1, imClientA.getRoster().getGroup(groupName).getEntryCount());
			Thread.sleep(1000);
			im.renameRosterGroup(groupId, groupName+"ABC");
			Thread.sleep(1000);
			assertEquals(1, imClientA.getRoster().getGroup(groupName+"ABC").getEntryCount());
			Thread.sleep(1000);
			im.syncFriendsRoster(groupId, groupName, null, Collections.singletonList(testUserB));
			Thread.sleep(1000);
			im.deleteRosterGroup(groupId);
			Thread.sleep(1000);
			assertEquals(groupCountA, imClientA.getRoster().getGroupCount());
			
			
			//localy we do not have all information we need
			//todo, add dummy values locally as we do not have authUserSessions
			if (CoordinatorManager.getInstance().getCoordinator().isClusterMode()) {
				List<ConnectedUsersListEntry> l = im.getAllConnectedUsers(null);
				ConnectedUsersListEntry entry = l.get(1);
				assertNotNull(entry);
			}
			
			
			im.getClientManager().destroyInstantMessagingClient(testUserA);
			im.getClientManager().destroyInstantMessagingClient(testUserB);
			
			//delete the accounts with random passwords and recreate the default ones
			assertTrue(InstantMessagingModule.getAdapter().deleteAccount(testUserA));
			assertTrue(InstantMessagingModule.getAdapter().deleteAccount(testUserB));
			
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
	}
}
