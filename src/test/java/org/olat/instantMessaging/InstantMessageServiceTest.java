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
import org.olat.instantMessaging.model.RosterEntryImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserLifecycleManager;
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
	private RosterDAO rosterDao;
	@Autowired
	private InstantMessageDAO imDao;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private InstantMessagePreferencesDAO instantMessagePreferencesDao;

	
	@Test
	public void testGetBuddiesListenTo() {
		DummyListener dummyListener = new DummyListener();
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-1-");
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-2-");
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
		Assert.assertEquals(buddy1.getIdentityKey(), chatter1.getKey());
		Assert.assertTrue(buddy1.getName().contains(chatter1.getUser().getLastName()));
		Assert.assertFalse(buddy1.isAnonym());
		Assert.assertFalse(buddy1.isVip());

		//check the properties of buddy 2
		Buddy buddy2 = buddies.get(0).getIdentityKey().equals(chatter2.getKey()) ? buddies.get(0) : buddies.get(1);
		Assert.assertEquals("Chatter-2", buddy2.getName());
		Assert.assertTrue(buddy2.isAnonym());
		Assert.assertTrue(buddy2.isVip());
	}
	
	@Test
	public void testGetBuddyStats_empty() {
		//create a chat
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-3-");
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
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-4-");
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-5-");
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Chat-1-", "testGetBuddyStats_mustBeEmpty", BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, null);
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
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-6-");
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-7-");
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "Chat-2-", "testGetBuddyStats_visible", BusinessGroup.BUSINESS_TYPE,
				0, 10, false, false, null);
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
	
	@Test
	public void deleteUser() {
		Identity chatter1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-8");
		Identity chatter2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Chat-9");

		OLATResourceable chat = imService.getPrivateChatResource(chatter1.getKey(), chatter2.getKey());
		imService.sendPresence(chatter1, "Me", false, true, chat);
		imService.sendPresence(chatter2, "Me", false, true, chat);
		ImPreferences preferences1 = imService.getImPreferences(chatter1);
		ImPreferences preferences2 = imService.getImPreferences(chatter2);
		dbInstance.commit();
		Assert.assertNotNull(preferences1);
		Assert.assertNotNull(preferences2);
		InstantMessage message = imService.sendMessage(chatter1, "Me", false, "Hello", chat);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(message);

		// delete the user
		userLifecycleManager.deleteIdentity(chatter1, null);
		dbInstance.commitAndCloseSession();
		
		// check preferences are deleted
		Assert.assertNull(instantMessagePreferencesDao.getStatus(chatter1.getKey()));
		Assert.assertNotNull(instantMessagePreferencesDao.getStatus(chatter2.getKey()));
		// check roster
		List<RosterEntryImpl> entries = rosterDao.getRoster(chat, 0, -1);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(chatter2.getKey(), entries.get(0).getIdentityKey());
		// check messages
		InstantMessage deletedMessage = imDao.loadMessageById(message.getKey());
		Assert.assertNull(deletedMessage);
	}
	
	
	private class DummyListener implements GenericEventListener {
		@Override
		public void event(Event event) {
			//
		}
	}
}