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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeToRepositoryEntry;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.vitero.model.GroupRole;
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
public class AbsenceNoticeToRepositoryEntryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDAO;
	@Autowired
	private AbsenceNoticeToRepositoryEntryDAO absenceNoticeToRepositoryEntryDao;
	
	@Test
	public void createAbsenceNoticeToRepositoryEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");

		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				start, end, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		AbsenceNoticeToRepositoryEntry relation = absenceNoticeToRepositoryEntryDao.createRelation(notice, entry);
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(notice, relation.getAbsenceNotice());
		Assert.assertEquals(entry, relation.getEntry());
	}
	
	@Test
	public void searchRollCallsByRepositoryEntry() {
		// make a course, with a participant, with a lecture block, an absence
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry, new Date(), new Date());
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		AbsenceNotice notice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()),
				null, null, Boolean.TRUE, Collections.singletonList(entry), null, null);
		dbInstance.commit();
		
		LectureBlockRollCall rollCall = lectureService.getOrCreateRollCall(participant, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall);
		
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRollCall> rollCallList = absenceNoticeToRepositoryEntryDao.searchRollCallsByRepositoryEntry(notice);
		Assert.assertNotNull(rollCallList);
		Assert.assertEquals(1, rollCallList.size());
		Assert.assertTrue(rollCallList.contains(rollCall));
	}
	
	/**
	 * The test make sure that the search method returns the roll call
	 * of the right user, at the right dates, with the right course. It
	 * adds noise in the form of a second course, second lecture block
	 * with different dates and a second participant (with a lecture at
	 * the moment of the search).
	 * 
	 */
	@Test
	public void searchRollCallsByRepositoryEntry_severalParticipants() {
		// make a course, with a participant, with a lecture block, an absence
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 12);
		Date startLecture = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 14);
		Date endLecture = cal.getTime();
		
		LectureBlock lectureBlock = createMinimalLectureBlock(entry, startLecture, endLecture);
		
		cal.add(Calendar.DATE, -7);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		Date oldStartLecture = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 14);
		Date oldEndLecture = cal.getTime();
		LectureBlock oldLectureBlock = createMinimalLectureBlock(entry, oldStartLecture, oldEndLecture);
		
		// second repository entry for some noise
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlockEntry2 = createMinimalLectureBlock(entry2, startLecture, endLecture);
		
		// memberships
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-2");
		repositoryEntryRelationDAO.addRole(participant1, entry, GroupRole.participant.name());
		repositoryEntryRelationDAO.addRole(participant2, entry, GroupRole.participant.name());
		repositoryEntryRelationDAO.addRole(participant1, entry2, GroupRole.participant.name());
		repositoryEntryRelationDAO.addRole(participant2, entry2, GroupRole.participant.name());
		
		
		AbsenceNotice notice1 = lectureService.createAbsenceNotice(participant1, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()),
				null, null, Boolean.TRUE, Collections.singletonList(entry), null, null);
		AbsenceNotice notice2 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()),
				null, null, Boolean.TRUE, Collections.singletonList(entry), null, null);
		dbInstance.commit();
		Assert.assertNotNull(notice1);
		Assert.assertNotNull(notice2);
		
		// roll call first entry
		LectureBlockRollCall rollCall1 = lectureService.getOrCreateRollCall(participant1, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall1);
		LectureBlockRollCall rollCall2 = lectureService.getOrCreateRollCall(participant2, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall2);
		LectureBlockRollCall oldRollCall1 = lectureService.getOrCreateRollCall(participant1, oldLectureBlock, null, null, null);
		Assert.assertNotNull(oldRollCall1);
		// roll call second entry
		LectureBlockRollCall rollCall1Entry2 = lectureService.getOrCreateRollCall(participant1, lectureBlockEntry2 , null, null, null);
		Assert.assertNotNull(rollCall1Entry2);
		LectureBlockRollCall rollCall2Entry2  = lectureService.getOrCreateRollCall(participant2, lectureBlockEntry2, null, null, null);
		Assert.assertNotNull(rollCall2Entry2);
		
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRollCall> rollCallList = absenceNoticeToRepositoryEntryDao.searchRollCallsByRepositoryEntry(notice1);
		Assert.assertNotNull(rollCallList);
		Assert.assertEquals(1, rollCallList.size());
		Assert.assertTrue(rollCallList.contains(rollCall1));
		Assert.assertFalse(rollCallList.contains(rollCall2));
		Assert.assertFalse(rollCallList.contains(oldRollCall1));
	}
	
	@Test
	public void searchRollCallsOfAllEntries() {
		// make a course, with a participant, with a lecture block, an absence
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 12);
		Date startLecture = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 14);
		Date endLecture = cal.getTime();
		
		LectureBlock lectureBlock = createMinimalLectureBlock(entry, startLecture, endLecture);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		AbsenceNotice notice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()),
				null, null, Boolean.TRUE, null, null, null);
		dbInstance.commit();
		
		LectureBlockRollCall rollCall = lectureService.getOrCreateRollCall(participant, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall);
		
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRollCall> rollCallList = absenceNoticeToRepositoryEntryDao
				.searchRollCallsOfAllEntries(notice.getIdentity(), notice.getStartDate(), notice.getEndDate());
		Assert.assertNotNull(rollCallList);
		Assert.assertEquals(1, rollCallList.size());
		Assert.assertTrue(rollCallList.contains(rollCall));
	}
	
	/**
	 * The test make sure that the search method returns the roll call
	 * of the right user, at the right dates, with the right course. It
	 * adds noise in the form of a second course, second lecture block
	 * with different dates and a second participant (with a lecture at
	 * the moment of the search).
	 * 
	 */
	@Test
	public void searchRollCallsOfAllEntries_severalParticipants() {
		// make a course, with a participant, with a lecture block, an absence
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 12);
		Date startLecture = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 14);
		Date endLecture = cal.getTime();
		
		LectureBlock lectureBlock = createMinimalLectureBlock(entry, startLecture, endLecture);
		
		cal.add(Calendar.DATE, -7);
		cal.set(Calendar.HOUR_OF_DAY, 12);
		Date oldStartLecture = cal.getTime();
		cal.set(Calendar.HOUR_OF_DAY, 14);
		Date oldEndLecture = cal.getTime();
		LectureBlock oldLectureBlock = createMinimalLectureBlock(entry, oldStartLecture, oldEndLecture);
		
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-2");
		repositoryEntryRelationDAO.addRole(participant1, entry, GroupRole.participant.name());
		repositoryEntryRelationDAO.addRole(participant2, entry, GroupRole.participant.name());
		AbsenceNotice notice1 = lectureService.createAbsenceNotice(participant1, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()),
				null, null, Boolean.TRUE, null, null, null);
		AbsenceNotice notice2 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.absence, AbsenceNoticeTarget.allentries,
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()),
				null, null, Boolean.TRUE, null, null, null);
		dbInstance.commit();
		Assert.assertNotNull(notice1);
		Assert.assertNotNull(notice2);
		
		LectureBlockRollCall rollCall1 = lectureService.getOrCreateRollCall(participant1, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall1);
		LectureBlockRollCall rollCall2 = lectureService.getOrCreateRollCall(participant2, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall2);
		LectureBlockRollCall oldRollCall1 = lectureService.getOrCreateRollCall(participant1, oldLectureBlock, null, null, null);
		Assert.assertNotNull(oldRollCall1);
		
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRollCall> rollCallList = absenceNoticeToRepositoryEntryDao
				.searchRollCallsOfAllEntries(notice1.getIdentity(), notice1.getStartDate(), notice1.getEndDate());
		Assert.assertNotNull(rollCallList);
		Assert.assertEquals(1, rollCallList.size());
		Assert.assertTrue(rollCallList.contains(rollCall1));
		Assert.assertFalse(rollCallList.contains(rollCall2));
		Assert.assertFalse(rollCallList.contains(oldRollCall1));
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry, Date start, Date end) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(start);
		lectureBlock.setEndDate(end);
		lectureBlock.setTitle("Absence");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setEffectiveLecturesNumber(4);
		return lectureBlockDao.update(lectureBlock);
	}
}
