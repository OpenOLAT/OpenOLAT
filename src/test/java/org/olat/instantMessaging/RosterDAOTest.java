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
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.manager.RosterDAO;
import org.olat.instantMessaging.model.RosterEntryImpl;
import org.olat.instantMessaging.model.RosterEntryView;
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
	
	@Test
	public void testCreateRosterEntry() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-1-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-roster-1-");
		
		RosterEntryImpl entry = rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false, false);
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
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-2-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-roster-2-");
		rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false, true);
		dbInstance.commitAndCloseSession();
		
		//load the entries
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryImpl entry = entries.get(0);
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
	public void testGetRosterViews() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-8-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-roster-8-");
		rosterDao.createRosterEntry(chatResource, id, "My little name", "Nock", false, false);
		dbInstance.commitAndCloseSession();
		
		List<RosterEntryView> entries = rosterDao.getRosterView(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryView entry = entries.get(0);
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
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-7-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-roster-7-");
		rosterDao.createRosterEntry(chatResource, id, "My name", "Nick", false, false);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		rosterDao.updateRosterEntry(chatResource, id, "My updated full name", "My updated nick name", true, false);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
			
		RosterEntryImpl entry = entries.get(0);
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryImpl reloadEntry = entries.get(0);
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
	public void testUpdateRosterEntry_createNew() {
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-7-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-roster-7-");
		rosterDao.updateRosterEntry(chatResource, id, "My old name", "Truck", true, false);
		dbInstance.commitAndCloseSession();
		
		//load the entry
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryImpl entry = entries.get(0);
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		
		RosterEntryImpl reloadEntry = entries.get(0);
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
	public void testDeleteRosterEntries() {
		//create an entry
		OLATResourceable chatResource = OresHelper.createOLATResourceableInstance("unit-test-3-" + UUID.randomUUID().toString(), System.currentTimeMillis());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndAdmin("im-roster-3-");
		rosterDao.createRosterEntry(chatResource, id, "My full name", "A nick name", false, false);
		dbInstance.commitAndCloseSession();
		
		//check the presence of the entry
		List<RosterEntryImpl> entries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		dbInstance.commitAndCloseSession();
		
		//delete the entry
		rosterDao.deleteEntry(id, chatResource);
		dbInstance.commitAndCloseSession();
		
		//check the absence of the entry
		List<RosterEntryImpl> reloadedEntries = rosterDao.getRoster(chatResource, 0, -1);
		Assert.assertNotNull(reloadedEntries);
		Assert.assertTrue(reloadedEntries.isEmpty());
	}
}
