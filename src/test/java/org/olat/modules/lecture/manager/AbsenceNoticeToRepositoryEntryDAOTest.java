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
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				null, null, null, null, null, null, null);
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
	public void getRollCalls() {
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
		
		List<LectureBlockRollCall> rollCallList = absenceNoticeToRepositoryEntryDao.getRollCallsByRepositoryEntry(notice);
		Assert.assertNotNull(rollCallList);
		Assert.assertEquals(1, rollCallList.size());
		Assert.assertTrue(rollCallList.contains(rollCall));
	}
	
	@Test
	public void getRollCallsOfAllEntries() {
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
				null, null, Boolean.TRUE, Collections.singletonList(entry), null, null);
		dbInstance.commit();
		
		LectureBlockRollCall rollCall = lectureService.getOrCreateRollCall(participant, lectureBlock, null, null, null);
		Assert.assertNotNull(rollCall);
		
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRollCall> rollCallList = absenceNoticeToRepositoryEntryDao.getRollCallsOfAllEntries(notice);
		Assert.assertNotNull(rollCallList);
		Assert.assertEquals(1, rollCallList.size());
		Assert.assertTrue(rollCallList.contains(rollCall));
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
