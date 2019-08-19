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
package org.olat.modules.lecture.manager;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeSearchParameters;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.model.AbsenceNoticeInfos;
import org.olat.modules.lecture.model.LectureBlockWithNotice;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private AbsenceCategoryDAO absenceCategoryDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private AbsenceNoticeToLectureBlockDAO absenceNoticeToLectureBlockDao;
	@Autowired
	private AbsenceNoticeToRepositoryEntryDAO absenceNoticeToRepositoryEntryDao;
	
	@Test
	public void createAbsenceNotice() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity notifier = JunitTestHelper.createAndPersistIdentityAsRndUser("notifier-1");
		Identity authorizer = JunitTestHelper.createAndPersistIdentityAsRndUser("authorizer-1");
		
		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());

		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				start, end, null, "A very good reason", Boolean.TRUE, authorizer, notifier);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(notice);
		Assert.assertNotNull(notice.getKey());
		Assert.assertNotNull(notice.getCreationDate());
		Assert.assertNotNull(notice.getLastModified());
		Assert.assertNotNull(notice.getStartDate());
		Assert.assertNotNull(notice.getEndDate());
		Assert.assertEquals(identity, notice.getIdentity());
		Assert.assertEquals(notifier, notice.getNotifier());
		Assert.assertEquals(authorizer, notice.getAuthorizer());
		Assert.assertEquals(Boolean.TRUE, notice.getAbsenceAuthorized());
		Assert.assertEquals("A very good reason", notice.getAbsenceReason());
		Assert.assertEquals(AbsenceNoticeType.absence, notice.getNoticeType());
		Assert.assertEquals(AbsenceNoticeTarget.lectureblocks, notice.getNoticeTarget());
	}
	
	@Test
	public void createAbsenceNoticeToRollCall() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-2");
		Identity notifier = JunitTestHelper.createAndPersistIdentityAsRndUser("notifier-2");
		Identity authorizer = JunitTestHelper.createAndPersistIdentityAsRndUser("authorizer-2");
		
		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());
		
		LectureBlock lectureBlock = createMinimalLectureBlock();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				start, end, null, "Too sleepy", Boolean.FALSE, authorizer, notifier);
		
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, notice, null, null);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadedRollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		AbsenceNotice reloadedNotice = reloadedRollCall.getAbsenceNotice();
		
		// check
		Assert.assertEquals(rollCall, reloadedRollCall);
		Assert.assertEquals(identity, reloadedRollCall.getIdentity());
		Assert.assertEquals(identity, reloadedNotice.getIdentity());
		Assert.assertEquals(notifier, reloadedNotice.getNotifier());
		Assert.assertEquals(authorizer, reloadedNotice.getAuthorizer());
		Assert.assertEquals(notice, reloadedNotice);
		Assert.assertEquals(Boolean.FALSE, reloadedNotice.getAbsenceAuthorized());
		Assert.assertEquals("Too sleepy", reloadedNotice.getAbsenceReason());
		Assert.assertNotNull(reloadedNotice.getStartDate());
		Assert.assertNotNull(reloadedNotice.getEndDate());
		Assert.assertEquals(AbsenceNoticeType.absence, reloadedNotice.getNoticeType());
		Assert.assertEquals(AbsenceNoticeTarget.lectureblocks, reloadedNotice.getNoticeTarget());
	}
	
	@Test
	public void createAbsenceNoticeToRollCall_update() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		linkNoticeToRollCall(rollCall, notice);
		
		rollCall.setAbsenceNotice(notice);
		rollCall.setAbsenceNoticeLectures("0,1");
		lectureBlockRollCallDao.update(rollCall);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadedRollCall = lectureBlockRollCallDao.getRollCall(lectureBlock, identity);
		AbsenceNotice reloadedNotice = reloadedRollCall.getAbsenceNotice();
		
		// check
		Assert.assertEquals(identity, reloadedRollCall.getIdentity());
		Assert.assertEquals(identity, reloadedNotice.getIdentity());
		Assert.assertEquals(notice, reloadedNotice);
		Assert.assertEquals(AbsenceNoticeType.absence, reloadedNotice.getNoticeType());
		Assert.assertEquals(AbsenceNoticeTarget.entries, reloadedNotice.getNoticeTarget());
	}
	
	@Test
	public void searchAbsenceNotice() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-3");
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		AbsenceNoticeSearchParameters searchParams = new AbsenceNoticeSearchParameters();
		List<AbsenceNoticeInfos> foundNotices = absenceNoticeDao.search(searchParams, true);
		Assert.assertNotNull(foundNotices);
		Assert.assertFalse(foundNotices.isEmpty());
		
		long count = foundNotices.stream()
				.filter(n -> notice.equals(n.getAbsenceNotice())).count(); 
		Assert.assertEquals(1, count);
	}
	
	/**
	 * This test checks the query syntax and not the return values
	 */
	@Test
	public void searchAbsenceNotice_allParameters() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-3b");
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-3c");
		AbsenceCategory absenceCategory = absenceCategoryDao
				.createAbsenceCategory(UUID.randomUUID().toString(), "Test category");
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				null, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(notice);
		
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		
		AbsenceNoticeSearchParameters searchParams = new AbsenceNoticeSearchParameters();
		searchParams.setAuthorized(Boolean.TRUE);
		searchParams.setAbsenceCategory(absenceCategory);
		searchParams.setStartDate(CalendarUtils.startOfDay(new Date()));
		searchParams.setEndDate(CalendarUtils.endOfDay(new Date()));
		searchParams.setLinkedToRollCall(true);
		searchParams.setManagedOrganisations(Collections.singletonList(defOrganisation));
		searchParams.setMasterCoach(coach);
		searchParams.setParticipant(identity);
		searchParams.setTypes(Collections.singletonList(AbsenceNoticeType.absence));
		
		List<AbsenceNoticeInfos> foundNotices = absenceNoticeDao.search(searchParams, true);
		Assert.assertNotNull(foundNotices);
	}
	
	@Test
	public void getRollCalls() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-4");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				null, null, null, null, null, null, null);
		linkNoticeToRollCall(rollCall, notice);
		
		List<LectureBlockRollCall> noticedRollCalls = absenceNoticeDao.getRollCalls(notice);
		Assert.assertNotNull(noticedRollCalls);
		Assert.assertEquals(1, noticedRollCalls.size());
		Assert.assertEquals(rollCall, noticedRollCalls.get(0));
	}
	
	@Test
	public void loadLectureBlocksOf_lectureblocks() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-5");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commit();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				null, null, null, null, null, null, null);
		linkNoticeToRollCall(rollCall, notice);
		absenceNoticeToLectureBlockDao.createRelation(notice, lectureBlock);
		dbInstance.commitAndCloseSession();
		
		// load by lecture blocks
		List<AbsenceNotice> notices = Collections.singletonList(notice);
		List<LectureBlockWithNotice> noticedBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.lectureblocks);
		Assert.assertNotNull(noticedBlocks);
		Assert.assertEquals(1, noticedBlocks.size());
		Assert.assertEquals(lectureBlock, noticedBlocks.get(0).getLectureBlock());
		Assert.assertEquals(notice.getKey(), noticedBlocks.get(0).getAbsenceNotice().getKey());
		
		// check for other types
		List<LectureBlockWithNotice> noticedEntriesBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.entries);
		Assert.assertTrue(noticedEntriesBlocks.isEmpty());
		List<LectureBlockWithNotice> noticedAllEntriesBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.allentries);
		Assert.assertTrue(noticedAllEntriesBlocks.isEmpty());
	}
	
	@Test
	public void loadLectureBlocksOf_entries() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-6");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commit();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				null, null, null, null, null, null, null);
		linkNoticeToRollCall(rollCall, notice);
		absenceNoticeToRepositoryEntryDao.createRelation(notice, lectureBlock.getEntry());
		dbInstance.commitAndCloseSession();
		
		// load by entries
		List<AbsenceNotice> notices = Collections.singletonList(notice);
		List<LectureBlockWithNotice> noticedBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.entries);
		Assert.assertNotNull(noticedBlocks);
		Assert.assertEquals(1, noticedBlocks.size());
		Assert.assertEquals(lectureBlock, noticedBlocks.get(0).getLectureBlock());
		Assert.assertEquals(notice.getKey(), noticedBlocks.get(0).getAbsenceNotice().getKey());
		
		// check for other types
		List<LectureBlockWithNotice> noticedLectureBlocksBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.lectureblocks);
		Assert.assertTrue(noticedLectureBlocksBlocks.isEmpty());
		List<LectureBlockWithNotice> noticedAllEntriesBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.allentries);
		Assert.assertTrue(noticedAllEntriesBlocks.isEmpty());
	}
	
	@Test
	public void loadLectureBlocksOf_allEntries() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-7");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		// create the participant's relation (lecture block use repo entry group, participant is participant in this group)
		repositoryEntryRelationDao.addRole(identity, lectureBlock.getEntry(), GroupRoles.participant.name());
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(lectureBlock.getEntry());
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commit();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()), null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rollCall);
		
		// load by entries
		List<AbsenceNotice> notices = Collections.singletonList(notice);
		List<LectureBlockWithNotice> noticedBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.allentries);
		Assert.assertNotNull(noticedBlocks);
		Assert.assertEquals(1, noticedBlocks.size());
		Assert.assertEquals(lectureBlock, noticedBlocks.get(0).getLectureBlock());
		Assert.assertEquals(notice.getKey(), noticedBlocks.get(0).getAbsenceNotice().getKey());
		
		// check for other types
		List<LectureBlockWithNotice> noticedLectureBlocksBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.lectureblocks);
		Assert.assertTrue(noticedLectureBlocksBlocks.isEmpty());
		List<LectureBlockWithNotice> noticedAllEntriesBlocks = absenceNoticeDao.loadLectureBlocksOf(notices, AbsenceNoticeTarget.entries);
		Assert.assertTrue(noticedAllEntriesBlocks.isEmpty());
	}
	
	@Test
	public void loadAbsenceNotice() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-8");
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()), null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		AbsenceNotice reloadedNotice = absenceNoticeDao.loadAbsenceNotice(notice.getKey());
		Assert.assertNotNull(reloadedNotice);
		Assert.assertEquals(notice, reloadedNotice);
	}
	
	@Test
	public void getAbsenceNotices_lectureblocks() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-9");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commit();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				null, null, null, null, null, null, null);
		linkNoticeToRollCall(rollCall, notice);
		absenceNoticeToLectureBlockDao.createRelation(notice, lectureBlock);
		dbInstance.commitAndCloseSession();
		
		// load by lecture blocks
		List<AbsenceNotice> noticedBlocks = absenceNoticeDao.getAbsenceNotices(identity, lectureBlock);
		Assert.assertNotNull(noticedBlocks);
		Assert.assertEquals(1, noticedBlocks.size());
		Assert.assertEquals(notice, noticedBlocks.get(0));
	}
	
	@Test
	public void getAbsenceNotices_entries() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-10");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commit();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				null, null, null, null, null, null, null);
		linkNoticeToRollCall(rollCall, notice);
		absenceNoticeToRepositoryEntryDao.createRelation(notice, lectureBlock.getEntry());
		dbInstance.commitAndCloseSession();
		
		// load by entries
		List<AbsenceNotice> noticedBlocks = absenceNoticeDao.getAbsenceNotices(identity, lectureBlock);
		Assert.assertNotNull(noticedBlocks);
		Assert.assertEquals(1, noticedBlocks.size());
		Assert.assertEquals(notice, noticedBlocks.get(0));
	}
	
	@Test
	public void getAbsenceNotices_allEntries() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-11");
		LectureBlock lectureBlock = createMinimalLectureBlock();
		// create the participant's relation (lecture block use repo entry group, participant is participant in this group)
		repositoryEntryRelationDao.addRole(identity, lectureBlock.getEntry(), GroupRoles.participant.name());
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(lectureBlock.getEntry());
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity, null, null, null, null, null, null);
		dbInstance.commit();
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()), null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rollCall);
		
		// load by entries
		List<AbsenceNotice> noticedBlocks = absenceNoticeDao.getAbsenceNotices(identity, lectureBlock);
		Assert.assertNotNull(noticedBlocks);
		Assert.assertEquals(1, noticedBlocks.size());
		Assert.assertEquals(notice, noticedBlocks.get(0));
	}
	
	private void linkNoticeToRollCall(LectureBlockRollCall rollCall, AbsenceNotice notice) {
		rollCall.setAbsenceNotice(notice);
		rollCall.setAbsenceNoticeLectures("0,1");
		lectureBlockRollCallDao.update(rollCall);
		dbInstance.commitAndCloseSession();
	}
	
	private LectureBlock createMinimalLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Absence");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setEffectiveLecturesNumber(4);
		return lectureBlockDao.update(lectureBlock);
	}

}
