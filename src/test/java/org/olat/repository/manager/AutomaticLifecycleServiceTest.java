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
package org.olat.repository.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This only check if the queries. Not the logic of the queries
 * 
 * Initial date: 24 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AutomaticLifecycleServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AutomaticLifecycleService automaticLifecycleService;
	@Autowired
	private RepositoryEntryLifecycleDAO repositoryEntryLifecycleDao;

	
	@Test
	public void getRepositoryEntries() {
		RepositoryEntry entry = createRepositoryEntry("To close 1", RepositoryEntryStatusEnum.published, -120, -60);	
		
		List<RepositoryEntry> entriesToClose2 = automaticLifecycleService
				.getRepositoryEntries(DateUtils.addDays(new Date(), -200), RepositoryEntryStatusEnum.preparationToPublished());
		Assert.assertNotNull(entriesToClose2);
		Assert.assertFalse(entriesToClose2.contains(entry));
		
		List<RepositoryEntry> entriesToClose50 = automaticLifecycleService
				.getRepositoryEntries(DateUtils.addDays(new Date(), -50), RepositoryEntryStatusEnum.preparationToPublished());
		Assert.assertNotNull(entriesToClose50);
		Assert.assertTrue(entriesToClose50.contains(entry));
	}
	
	@Test
	public void getDeletedRepositoryEntries() {
		RepositoryEntry entry = createRepositoryEntry("To close 1", RepositoryEntryStatusEnum.trash, -120, -60);
		entry.setDeletionDate(DateUtils.addDays(new Date(), -80));
		entry = dbInstance.getCurrentEntityManager().merge(entry);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> deletedEntries = automaticLifecycleService
				.getRepositoryEntriesInTrash(DateUtils.addDays(new Date(), -200));
		Assert.assertNotNull(deletedEntries);
		Assert.assertFalse(deletedEntries.contains(entry));
		
		List<RepositoryEntry> deletedEntries70 = automaticLifecycleService
				.getRepositoryEntriesInTrash(DateUtils.addDays(new Date(), -70));
		Assert.assertNotNull(deletedEntries70);
		Assert.assertTrue(deletedEntries70.contains(entry));
	}
	
	@Test
	public void getDeletedRepositoryEntriesnegativeTest() {
		RepositoryEntry entry = createTrashedRepositoryEntry("To close 1", RepositoryEntryStatusEnum.published, -120, -60, -80);

		List<RepositoryEntry> deletedEntries = automaticLifecycleService
				.getRepositoryEntriesInTrash(DateUtils.addDays(new Date(), -20));
		Assert.assertNotNull(deletedEntries);
		Assert.assertFalse(deletedEntries.contains(entry));
	}
	
	@Test
	public void manageAutoClose() {
		repositoryModule.setLifecycleAutoClose("40day");
		repositoryModule.setLifecycleAutoDelete(null);
		repositoryModule.setLifecycleAutoDefinitivelyDelete(null);
		
		RepositoryEntry entryToClose = createRepositoryEntry("To close 3", RepositoryEntryStatusEnum.published, -120, -60);	
		RepositoryEntry entryInUse = createRepositoryEntry("In use 1", RepositoryEntryStatusEnum.published, -20, 60);	
		dbInstance.commitAndCloseSession();

		automaticLifecycleService.manage();
		
		// check the entry to close
		RepositoryEntry reloadEntryToClose = repositoryManager.lookupRepositoryEntry(entryToClose.getKey());
		Assert.assertEquals(entryToClose, reloadEntryToClose);
		Assert.assertEquals(RepositoryEntryStatusEnum.closed, reloadEntryToClose.getEntryStatus());
		
		// check the used entry is not closed
		RepositoryEntry reloadEntryInUse = repositoryManager.lookupRepositoryEntry(entryInUse.getKey());
		Assert.assertEquals(entryInUse, reloadEntryInUse);
		Assert.assertEquals(RepositoryEntryStatusEnum.published, reloadEntryInUse.getEntryStatus());
	}
	
	@Test
	public void manageAutoDelete() {
		repositoryModule.setLifecycleAutoClose(null);
		repositoryModule.setLifecycleAutoDelete("120day");
		repositoryModule.setLifecycleAutoDefinitivelyDelete(null);
		
		RepositoryEntry entryToClose1 = createRepositoryEntry("To trash 4", RepositoryEntryStatusEnum.published, -180, -130);	
		RepositoryEntry entryToClose2 = createRepositoryEntry("To trash 5 ", RepositoryEntryStatusEnum.closed, -210, -180);	
		RepositoryEntry entryInUse = createRepositoryEntry("In use 2", RepositoryEntryStatusEnum.published, -30, 60);	
		dbInstance.commitAndCloseSession();

		automaticLifecycleService.manage();
		
		// check the entries to delete
		RepositoryEntry reloadEntryToClose1 = repositoryManager.lookupRepositoryEntry(entryToClose1.getKey());
		Assert.assertEquals(entryToClose1, reloadEntryToClose1);
		Assert.assertEquals(RepositoryEntryStatusEnum.trash, reloadEntryToClose1.getEntryStatus());
		
		RepositoryEntry reloadEntryToClose2 = repositoryManager.lookupRepositoryEntry(entryToClose2.getKey());
		Assert.assertEquals(entryToClose2, reloadEntryToClose2);
		Assert.assertEquals(RepositoryEntryStatusEnum.trash, reloadEntryToClose2.getEntryStatus());
		
		// check the used entry is not closed
		RepositoryEntry reloadEntryInUse = repositoryManager.lookupRepositoryEntry(entryInUse.getKey());
		Assert.assertEquals(entryInUse, reloadEntryInUse);
		Assert.assertEquals(RepositoryEntryStatusEnum.published, reloadEntryInUse.getEntryStatus());
	}
	
	@Test
	public void manageAutoDefinitivelyDelete() {
		repositoryModule.setLifecycleAutoClose(null);
		repositoryModule.setLifecycleAutoDelete(null);
		repositoryModule.setLifecycleAutoDefinitivelyDelete("210day");
		
		RepositoryEntry entryToDelete= createTrashedRepositoryEntry("To def. delete 6", RepositoryEntryStatusEnum.trash, -120, -70, -240);	
		RepositoryEntry entryClosed = createTrashedRepositoryEntry("To def. delete 7", RepositoryEntryStatusEnum.closed, -210, -90, -300);	
		RepositoryEntry entryInUse = createRepositoryEntry("In use 2", RepositoryEntryStatusEnum.published, -30, 60);	
		dbInstance.commitAndCloseSession();

		automaticLifecycleService.manage();
		
		// check the entries to delete
		RepositoryEntry reloadEntryToDelete1 = repositoryManager.lookupRepositoryEntry(entryToDelete.getKey());
		Assert.assertNull(reloadEntryToDelete1);
		
		RepositoryEntry reloadEntryClosed = repositoryManager.lookupRepositoryEntry(entryClosed.getKey());
		Assert.assertEquals(entryClosed, reloadEntryClosed);
		Assert.assertEquals(RepositoryEntryStatusEnum.closed, reloadEntryClosed.getEntryStatus());
		
		// check the used entry is not closed
		RepositoryEntry reloadEntryInUse = repositoryManager.lookupRepositoryEntry(entryInUse.getKey());
		Assert.assertEquals(entryInUse, reloadEntryInUse);
		Assert.assertEquals(RepositoryEntryStatusEnum.published, reloadEntryInUse.getEntryStatus());
	}
	
	@Test
	public void deleteCoursePermanentlyByLifecycle() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-del-1");
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(initialAuthor);
		dbInstance.commitAndCloseSession();
		
		boolean deleted = automaticLifecycleService.definitivelyDelete(re);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deleted);
	}

	private RepositoryEntry createTrashedRepositoryEntry(String displayName, RepositoryEntryStatusEnum status, int startDays, int endDays, int trashedDays) {
		RepositoryEntry entry = createRepositoryEntry(displayName, status, startDays, endDays);
		
		entry.setDeletionDate(DateUtils.addDays(new Date(), trashedDays));
		entry = dbInstance.getCurrentEntityManager().merge(entry);
		dbInstance.commitAndCloseSession();
		
		return entry;
	}
	
	private RepositoryEntry createRepositoryEntry(String displayName, RepositoryEntryStatusEnum status, int startDays, int endDays) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		entry = repositoryManager.setStatus(entry, status);
		Date start = DateUtils.addDays(new Date(), startDays);
		Date end = DateUtils.addDays(new Date(), endDays);
		RepositoryEntryLifecycle cycle = repositoryEntryLifecycleDao.create("Sem.", null, true, start, end);
		entry = repositoryManager.setDescriptionAndName(entry, displayName, "Fake course to close", null, null, null, null, null, null, cycle);
		dbInstance.commitAndCloseSession();
		return entry;
	}
}
