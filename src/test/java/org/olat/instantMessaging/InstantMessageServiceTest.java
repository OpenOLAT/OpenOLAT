/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.instantMessaging;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.instantMessaging.manager.InstantMessageDAO;
import org.olat.instantMessaging.manager.InstantMessagePreferencesDAO;
import org.olat.instantMessaging.manager.RosterDAO;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.BuddyStats;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InstantMessageServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InstantMessageDAO imDao;
	@Autowired
	private InstantMessagePreferencesDAO preferencesDao;
	@Autowired
	private RosterDAO rosterDao;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

	@Test
	public void should_service_present() {
		Assert.assertNotNull(dbInstance);
		Assert.assertNotNull(imDao);
		Assert.assertNotNull(preferencesDao);
		Assert.assertNotNull(rosterDao);
		Assert.assertNotNull(imService);
	}
	
	@Test
	public void testGetBuddiesListenTo() {
		DummyListener dummyListener = new DummyListener();
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-1-" + UUID.randomUUID().toString());
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-2-" + UUID.randomUUID().toString());
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), chatter1.getKey());
		imService.listenChat(chatter1, chatResource, null, false, false, dummyListener);
		imService.listenChat(chatter2, chatResource, "Chatter-2", true, true, dummyListener);
		dbInstance.commitAndCloseSession();

		//check if the buddies listen to the chat
		List<Buddy> buddies = imService.getBuddiesListenTo(chatResource);
		Assert.assertNotNull(buddies);
		Assert.assertEquals(2, buddies.size());
		
		//check the properties of buddy 1
		Buddy buddy1 = buddies.get(0).getIdentityKey().equals(chatter1.getKey()) ? buddies.get(0) : buddies.get(1);
		Assert.assertTrue(buddy1.getUsername().equals(chatter1.getName()));
		Assert.assertFalse(buddy1.isAnonym());
		Assert.assertFalse(buddy1.isVip());

		//check the properties of buddy 2
		Buddy buddy2 = buddies.get(0).getIdentityKey().equals(chatter2.getKey()) ? buddies.get(0) : buddies.get(1);
		Assert.assertTrue(buddy2.getUsername().equals(chatter2.getName()));
		Assert.assertTrue(buddy2.isAnonym());
		Assert.assertTrue(buddy2.isVip());
	}
	
	@Test
	public void testGetBuddyStats_empty() {
		//create a chat
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-3-" + UUID.randomUUID().toString());
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance(UUID.randomUUID().toString(), chatter1.getKey());
		imService.listenChat(chatter1, chatResource, null, false, false, new DummyListener());
		dbInstance.commitAndCloseSession();
		
		BuddyStats stats = imService.getBuddyStats(chatter1);
		Assert.assertNotNull(stats);
		Assert.assertEquals(0, stats.getOfflineBuddies());
		Assert.assertEquals(0, stats.getOnlineBuddies());
	}
	
	/**
	 * Two users in the same group but the visibility is set to false
	 */
	@Test
	public void testGetBuddyStats_mustBeEmpty() {
		//create a group with owner and participant
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-4-" + UUID.randomUUID().toString());
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-5-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Chat-1-", "testGetBuddyStats_mustBeEmpty", 0, 10, false, false, null);
		businessGroupRelationDao.addRole(chatter1, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(chatter2, group, GroupRoles.participant.name());
		dbInstance.commit();
		businessGroupService.updateDisplayMembers(group, false, false, false, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//check the buddies
		BuddyStats stats = imService.getBuddyStats(chatter1);
		Assert.assertNotNull(stats);
		Assert.assertEquals(0, stats.getOfflineBuddies());
		Assert.assertEquals(0, stats.getOnlineBuddies());
	}
	
	/**
	 * Two users in the same group (which displays its members)
	 */
	@Test
	public void testGetBuddyStats_visible() {
		//create a group with owner and participant
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-6-" + UUID.randomUUID().toString());
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsUser("Chat-7-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Chat-2-", "testGetBuddyStats_visible", 0, 10, false, false, null);
		businessGroupRelationDao.addRole(chatter1, group, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(chatter2, group, GroupRoles.participant.name());
		dbInstance.commit();
		businessGroupService.updateDisplayMembers(group, true, true, false, false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//check the buddies
		BuddyStats stats = imService.getBuddyStats(chatter1);
		Assert.assertNotNull(stats);
		Assert.assertEquals(1, stats.getOfflineBuddies());
		Assert.assertEquals(0, stats.getOnlineBuddies());
	}
	
	
	private class DummyListener implements GenericEventListener {
		@Override
		public void event(Event event) {
			//
		}
	}
}