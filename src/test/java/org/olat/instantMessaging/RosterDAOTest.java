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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.manager.InstantMessageDAO;
import org.olat.instantMessaging.manager.RosterDAO;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.instantMessaging.model.RosterEntryImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RosterDAO rosterDao;
	@Autowired
	private InstantMessageDAO messageDao;
	
	@Test
	public void testCreateRosterEntry() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-1", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-1-");
		
		RosterEntryImpl entry = rosterDao.createRosterEntry(chatResource, null, null, id,
				"My full name", "A nick name", false, false, false, true);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(entry);
		Assert.assertNotNull(entry.getKey());
		Assert.assertEquals(id.getKey(), entry.getIdentityKey());
		Assert.assertEquals("My full name", entry.getFullName());
		Assert.assertEquals("A nick name", entry.getNickName());
		Assert.assertFalse(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), entry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), entry.getResourceId());
	}

	@Test
	public void testLoadRosterEntries() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-2", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-2-");
		rosterDao.createRosterEntry(chatResource, null, null, id,
				"My full name", "A nick name", false, true, false, true);
		dbInstance.commitAndCloseSession();
		
		//load the entries
		List<RosterEntry> entries = rosterDao.getRoster(chatResource, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntry entry = entries.get(0);
		Assert.assertNotNull(entry);
		Assert.assertNotNull(entry.getKey());
		Assert.assertEquals(id.getKey(), entry.getIdentityKey());
		Assert.assertEquals("My full name", entry.getFullName());
		Assert.assertEquals("A nick name", entry.getNickName());
		Assert.assertFalse(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), entry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), entry.getResourceId());
	}
	
	@Test
	public void testGetRoster() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-3", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-8-");
		rosterDao.createRosterEntry(chatResource, null, null,  id, "My little name", "Nock", false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		List<RosterEntry> entries = rosterDao.getRoster(chatResource, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntry entry = entries.get(0);
		Assert.assertNotNull(entry);
		Assert.assertNotNull(entry.getKey());
		Assert.assertEquals(id.getKey(), entry.getIdentityKey());
		Assert.assertEquals("My little name", entry.getFullName());
		Assert.assertEquals("Nock", entry.getNickName());
		Assert.assertFalse(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), entry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), entry.getResourceId());
	}
	
	@Test
	public void testUpdateRosterEntry() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-4", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-7-");
		rosterDao.createRosterEntry(chatResource, null, null,  id, "My name", "Nick", false, false, false, false);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		rosterDao.updateRosterEntry(chatResource, null, null,  id, "My updated full name", "My updated nick name", true, false, false, false, true);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntry> entries = rosterDao.getRoster(chatResource, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
			
		RosterEntry entry = entries.get(0);
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntry reloadEntry = entries.get(0);
		Assert.assertNotNull(reloadEntry);
		Assert.assertNotNull(reloadEntry.getKey());
		Assert.assertEquals(id.getKey(), reloadEntry.getIdentityKey());
		Assert.assertEquals("My updated full name", reloadEntry.getFullName());
		Assert.assertEquals("My updated nick name", reloadEntry.getNickName());
		Assert.assertTrue(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), reloadEntry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), reloadEntry.getResourceId());
	}
	
	@Test
	public void testUpdateRosterEntryCreateNew() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-5", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-7-");
		rosterDao.updateRosterEntry(chatResource, null, null, id, "My old name", "Truck", true, false, false, false, true);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntry> entries = rosterDao.getRoster(chatResource, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntry entry = entries.get(0);
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntry reloadEntry = entries.get(0);
		Assert.assertNotNull(reloadEntry);
		Assert.assertNotNull(reloadEntry.getKey());
		Assert.assertEquals(id.getKey(), reloadEntry.getIdentityKey());
		Assert.assertEquals("My old name", reloadEntry.getFullName());
		Assert.assertEquals("Truck", reloadEntry.getNickName());
		Assert.assertTrue(entry.isAnonym());
		Assert.assertEquals(chatResource.getResourceableTypeName(), reloadEntry.getResourceTypeName());
		Assert.assertEquals(chatResource.getResourceableId(), reloadEntry.getResourceId());
	}
	
	@Test
	public void updateRosterLastSeen() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-6", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-8");
		RosterEntry entry = rosterDao.createRosterEntry(chatResource, null, null, id, null, null, false, true, true, true);
		dbInstance.commitAndCloseSession();
		
		rosterDao.updateLastSeen(id, chatResource, null, null);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntry> entries = rosterDao.getRoster(chatResource, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entry, entries.get(0));
		Assert.assertNotNull(entries.get(0).getLastSeen());
	}
	
	@Test
	public void inactivateEntry() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-6", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-8");
		RosterEntry entry = rosterDao.createRosterEntry(chatResource, "sub-path", "channel", id, null, null, false, true, true, true);
		dbInstance.commitAndCloseSession();
		
		rosterDao.inactivateEntry(id, chatResource, "sub-path", "channel");
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntry> entries = rosterDao.getRoster(chatResource, "sub-path", "channel");
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entry, entries.get(0));
		Assert.assertFalse(entries.get(0).isActive());
	}
	
	@Test
	public void getRosterAroundChannels() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-7", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-9");
		RosterEntry entry = rosterDao.createRosterEntry(chatResource, "sub-path", "channel", id, null, null, false, true, true, true);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(entry);
		
		//load the entry
		List<RosterChannelInfos> infos = rosterDao.getRosterAroundChannels(chatResource, "sub-path", "channel", id, false);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals("channel", infos.get(0).getChannel());
		Assert.assertTrue(infos.get(0).inRoster(id));
		Assert.assertTrue(infos.get(0).inRosterAndActive(id));
	}
	
	@Test
	public void getRosterAroundChannelsWithMessage() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-8", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-10");
		RosterEntry entry = rosterDao.createRosterEntry(chatResource, "sub-path", "msg-channel", id, null, null, false, true, true, true);
		InstantMessage msg = messageDao.createMessage(id, "From me", false, "Hello world", null, null,
				chatResource, "sub-path", "msg-channel", InstantMessageTypeEnum.text);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(msg);
		Assert.assertNotNull(entry);
		
		//load the entry
		List<RosterChannelInfos> infos = rosterDao.getRosterAroundChannels(chatResource, "sub-path", "msg-channel", id, false);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals("msg-channel", infos.get(0).getChannel());
		Assert.assertNull(infos.get(0).getLastStatusMessage());
		Assert.assertEquals(msg, infos.get(0).getLastTextMessage());
		Assert.assertEquals(RosterStatus.active, infos.get(0).getRosterStatus());
	}
	
	@Test
	public void getRosterAroundChannelsWithStatusMessage() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-roster-dao-7", System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("im-roster-9");
		RosterEntry entry = rosterDao.createRosterEntry(chatResource, "sub-path", "msg-channel-2", id, null, null, false, true, true, true);
		InstantMessage msg = messageDao.createMessage(id, "From me", false, "Hello world", null, null,
				chatResource, "sub-path", "msg-channel-2", InstantMessageTypeEnum.text);
		sleep(1200);
		InstantMessage status = messageDao.createMessage(id, "From me", false, null, null, null,
				chatResource, "sub-path", "msg-channel-2", InstantMessageTypeEnum.close);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(msg);
		Assert.assertNotNull(entry);
		Assert.assertNotNull(status);
		
		//load the entry
		List<RosterChannelInfos> infos = rosterDao.getRosterAroundChannels(chatResource, "sub-path", "msg-channel-2", id, false);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals("msg-channel-2", infos.get(0).getChannel());
		Assert.assertEquals(status, infos.get(0).getLastStatusMessage());
		Assert.assertEquals(msg, infos.get(0).getLastTextMessage());
		Assert.assertEquals(RosterStatus.completed, infos.get(0).getRosterStatus());
	}
	
	
}
