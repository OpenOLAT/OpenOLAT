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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private AbsenceNoticeDAO absenceNoticeDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private LectureBlockRollCallDAO lectureBlockRollCallDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO repositoryEntryLectureConfigurationDao;
	
	@Test
	public void createAndPersistRollCall() {
		LectureBlock lectureBlock = createMinimalLectureBlock(2);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(rollCall);
		Assert.assertNotNull(rollCall.getKey());
		Assert.assertNotNull(rollCall.getCreationDate());
		Assert.assertNotNull(rollCall.getLastModified());
		Assert.assertEquals(lectureBlock, rollCall.getLectureBlock());
		Assert.assertEquals(id, rollCall.getIdentity());	
	}
	
	@Test
	public void createAndLoadRollCall() {
		LectureBlock lectureBlock = createMinimalLectureBlock(2);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, null);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		
		Assert.assertNotNull(reloadRollCall);
		Assert.assertNotNull(reloadRollCall.getKey());
		Assert.assertNotNull(reloadRollCall.getCreationDate());
		Assert.assertNotNull(reloadRollCall.getLastModified());
		Assert.assertEquals(rollCall, reloadRollCall);
		Assert.assertEquals(lectureBlock, reloadRollCall.getLectureBlock());
		Assert.assertEquals(id, reloadRollCall.getIdentity());	
	}
	
	@Test
	public void createRollCall_absences() {
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Collections.singletonList(2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(rollCall, reloadRollCall);
		
		//check absence
		Assert.assertEquals(1, reloadRollCall.getLecturesAbsentNumber());
		List<Integer> absenceList = reloadRollCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(1, absenceList.size());
		Assert.assertEquals(2, absenceList.get(0).intValue());
		
		//check attendee
		Assert.assertEquals(2, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(2, attendeeList.size());
		Assert.assertEquals(0, attendeeList.get(0).intValue());
		Assert.assertEquals(1, attendeeList.get(1).intValue());
	}
	
	@Test
	public void addLectures() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(0);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(1, reloadRollCall.getLecturesAbsentNumber());
		
		List<Integer> additionalAbsences = Arrays.asList(1, 2);
		lectureBlockRollCallDao.addLecture(lectureBlock, reloadRollCall, additionalAbsences);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(3, reloadRollCall.getLecturesAbsentNumber());
		List<Integer> absenceList = reloadRollCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(3, absenceList.size());
		Assert.assertEquals(0, absenceList.get(0).intValue());
		Assert.assertEquals(1, absenceList.get(1).intValue());
		Assert.assertEquals(2, absenceList.get(2).intValue());
		
		//check attendee
		Assert.assertEquals(1, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(1, attendeeList.size());
		Assert.assertEquals(3, attendeeList.get(0).intValue());
	}
	
	@Test
	public void removeLectures() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(0, 1, 2, 3);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(4, reloadRollCall.getLecturesAbsentNumber());
		
		List<Integer> removedAbsences = Arrays.asList(1, 2);
		lectureBlockRollCallDao.removeLecture(lectureBlock, reloadRollCall, removedAbsences);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(2, reloadRollCall.getLecturesAbsentNumber());
		List<Integer> absenceList = reloadRollCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(2, absenceList.size());
		Assert.assertEquals(0, absenceList.get(0).intValue());
		Assert.assertEquals(3, absenceList.get(1).intValue());
		
		//check attendee
		Assert.assertEquals(2, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(2, attendeeList.size());
		Assert.assertEquals(1, attendeeList.get(0).intValue());
		Assert.assertEquals(2, attendeeList.get(1).intValue());
	}
	
	@Test
	public void adaptLectures_removeAbsences() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(0, 1, 2, 3);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(4, reloadRollCall.getLecturesAbsentNumber());
		
		//adapt the number of lectures
		LectureBlockRollCall adaptedCall = lectureBlockRollCallDao.adaptLecture(lectureBlock, reloadRollCall, 2, id);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(2, adaptedCall.getLecturesAbsentNumber());
		List<Integer> absenceList = adaptedCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(2, absenceList.size());
		Assert.assertEquals(0, absenceList.get(0).intValue());
		Assert.assertEquals(1, absenceList.get(1).intValue());
		
		//check attendee
		Assert.assertEquals(0, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(0, attendeeList.size());
	}
	
	@Test
	public void adaptLectures_removeMixed() {
		LectureBlock lectureBlock = createMinimalLectureBlock(4);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id, null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(2, reloadRollCall.getLecturesAbsentNumber());
		
		//adapt the number of lectures
		LectureBlockRollCall adaptedCall = lectureBlockRollCallDao.adaptLecture(lectureBlock, reloadRollCall, 2, id);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(1, adaptedCall.getLecturesAbsentNumber());
		List<Integer> absenceList = adaptedCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(1, absenceList.size());
		Assert.assertEquals(1, absenceList.get(0).intValue());
		
		//check attendee
		Assert.assertEquals(1, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(1, attendeeList.size());
		Assert.assertEquals(0, attendeeList.get(0).intValue());
	}
	
	@Test
	public void adaptLectures_addMixed() {
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, id,
				null, null, null, null, null, absences);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNotNull(reloadRollCall);
		Assert.assertEquals(2, reloadRollCall.getLecturesAbsentNumber());
		
		//adapt the number of lectures
		LectureBlockRollCall adaptedCall = lectureBlockRollCallDao.adaptLecture(lectureBlock, reloadRollCall, 4, id);
		dbInstance.commitAndCloseSession();
		
		//check absence
		Assert.assertEquals(2, adaptedCall.getLecturesAbsentNumber());
		List<Integer> absenceList = adaptedCall.getLecturesAbsentList();
		Assert.assertNotNull(absenceList);
		Assert.assertEquals(2, absenceList.size());
		Assert.assertEquals(1, absenceList.get(0).intValue());
		Assert.assertEquals(2, absenceList.get(1).intValue());
		
		//check attendee
		Assert.assertEquals(2, reloadRollCall.getLecturesAttendedNumber());
		List<Integer> attendeeList = reloadRollCall.getLecturesAttendedList();
		Assert.assertNotNull(attendeeList);
		Assert.assertEquals(2, attendeeList.size());
		Assert.assertEquals(0, attendeeList.get(0).intValue());
		Assert.assertEquals(3, attendeeList.get(1).intValue());
	}
	
	@Test
	public void getRollCalls_searchParams_True() {
		// an open lecture block
		LectureBlock openLectureBlock = createMinimalLectureBlock(3);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-2");
		dbInstance.commitAndCloseSession();

		// a closed lecture block
		LectureBlock closedLectureBlock = createMinimalLectureBlock(3);
		dbInstance.commitAndCloseSession();
		closedLectureBlock.setStatus(LectureBlockStatus.done);
		closedLectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlockDao.update(closedLectureBlock);

		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall1 = lectureBlockRollCallDao.createAndPersistRollCall(closedLectureBlock, id1,
				null, null, null, null, null, Collections.emptyList());
		LectureBlockRollCall rollCall2 = lectureBlockRollCallDao.createAndPersistRollCall(closedLectureBlock, id2,
				null, null, null, null, null, absences);
		LectureBlockRollCall rollCall3 = lectureBlockRollCallDao.createAndPersistRollCall(openLectureBlock, id1,
				null, null, null, null, null, absences);
		LectureBlockRollCall rollCall4 = lectureBlockRollCallDao.createAndPersistRollCall(openLectureBlock, id2,
				null, null, null, null, null, Collections.emptyList());
		dbInstance.commit();
		
		rollCall2.setAbsenceSupervisorNotificationDate(new Date());
		rollCall2 = lectureBlockRollCallDao.update(rollCall2);
		dbInstance.commitAndCloseSession();
		
		{//only absences
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setHasAbsence(Boolean.TRUE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertFalse(rollCalls.contains(rollCall1));
			Assert.assertTrue(rollCalls.contains(rollCall2));
			Assert.assertTrue(rollCalls.contains(rollCall3));
			Assert.assertFalse(rollCalls.contains(rollCall4));
		}
		
		{//only with supervisor date
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setHasSupervisorNotificationDate(Boolean.TRUE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertFalse(rollCalls.contains(rollCall1));
			Assert.assertTrue(rollCalls.contains(rollCall2));
			Assert.assertFalse(rollCalls.contains(rollCall3));
			Assert.assertFalse(rollCalls.contains(rollCall4));
		}
		
		{//only closed
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setClosed(Boolean.TRUE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertTrue(rollCalls.contains(rollCall1));
			Assert.assertTrue(rollCalls.contains(rollCall2));
			Assert.assertFalse(rollCalls.contains(rollCall3));
			Assert.assertFalse(rollCalls.contains(rollCall4));
		}

		{//only with supervisor date
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setClosed(Boolean.TRUE);
			searchParams.setHasAbsence(Boolean.TRUE);
			searchParams.setHasSupervisorNotificationDate(Boolean.TRUE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertFalse(rollCalls.contains(rollCall1));
			Assert.assertTrue(rollCalls.contains(rollCall2));
			Assert.assertFalse(rollCalls.contains(rollCall3));
			Assert.assertFalse(rollCalls.contains(rollCall4));
		}
	}
	
	@Test
	public void getRollCalls_searchParams_False() {
		// an open lecture block
		LectureBlock openLectureBlock = createMinimalLectureBlock(3);
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-2");
		dbInstance.commitAndCloseSession();

		// a closed lecture block
		LectureBlock closedLectureBlock = createMinimalLectureBlock(3);
		dbInstance.commitAndCloseSession();
		closedLectureBlock.setStatus(LectureBlockStatus.done);
		closedLectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlockDao.update(closedLectureBlock);

		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall1 = lectureBlockRollCallDao.createAndPersistRollCall(closedLectureBlock, id1,
				null, null, null, null, null, Collections.emptyList());
		LectureBlockRollCall rollCall2 = lectureBlockRollCallDao.createAndPersistRollCall(closedLectureBlock, id2,
				null, null, null, null, null, absences);
		LectureBlockRollCall rollCall3 = lectureBlockRollCallDao.createAndPersistRollCall(openLectureBlock, id1,
				null, null, null, null, null, absences);
		LectureBlockRollCall rollCall4 = lectureBlockRollCallDao.createAndPersistRollCall(openLectureBlock, id2,
				null, null, null, null, null, Collections.emptyList());
		dbInstance.commit();
		
		rollCall2.setAbsenceSupervisorNotificationDate(new Date());
		rollCall2 = lectureBlockRollCallDao.update(rollCall2);
		dbInstance.commitAndCloseSession();
		
		{// only not closed
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setClosed(Boolean.FALSE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertFalse(rollCalls.contains(rollCall1));
			Assert.assertFalse(rollCalls.contains(rollCall2));
			Assert.assertTrue(rollCalls.contains(rollCall3));
			Assert.assertTrue(rollCalls.contains(rollCall4));
		}
		
		{// without absence
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setHasAbsence(Boolean.FALSE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertTrue(rollCalls.contains(rollCall1));
			Assert.assertFalse(rollCalls.contains(rollCall2));
			Assert.assertFalse(rollCalls.contains(rollCall3));
			Assert.assertTrue(rollCalls.contains(rollCall4));
		}
		
		{// without supervisor date
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setHasSupervisorNotificationDate(Boolean.FALSE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertTrue(rollCalls.contains(rollCall1));
			Assert.assertFalse(rollCalls.contains(rollCall2));
			Assert.assertTrue(rollCalls.contains(rollCall3));
			Assert.assertTrue(rollCalls.contains(rollCall4));
		}
		
		{// open, without supervisor date
			LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
			searchParams.setClosed(Boolean.FALSE);
			searchParams.setHasAbsence(Boolean.FALSE);
			searchParams.setHasSupervisorNotificationDate(Boolean.FALSE);
			List<LectureBlockRollCall> rollCalls = lectureBlockRollCallDao.getRollCalls(searchParams);
			Assert.assertFalse(rollCalls.contains(rollCall1));
			Assert.assertFalse(rollCalls.contains(rollCall2));
			Assert.assertFalse(rollCalls.contains(rollCall3));
			Assert.assertTrue(rollCalls.contains(rollCall4));
		}
	}
	
	@Test
	public void updateWithAbsenceNotice() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-10");
		Identity authorizer = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-11");
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		AbsenceNotice notice = absenceNoticeDao.createAbsenceNotice(identity, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				lectureBlock.getStartDate(), lectureBlock.getEndDate(), null, "A very good reason", Boolean.TRUE, authorizer, authorizer);
		dbInstance.commitAndCloseSession();
		
		List<Integer> absences = Arrays.asList(1, 2);
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, identity,
				null, null, null, null, null, absences);
		dbInstance.commit();
		
		int rows = lectureBlockRollCallDao.updateLectureBlockRollCallAbsenceNotice(rollCall, notice);
		Assert.assertEquals(1, rows);
		dbInstance.commitAndCloseSession();
		
		LectureBlockRollCall reloadedRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertEquals(notice, reloadedRollCall.getAbsenceNotice());
		
		int removeRows = lectureBlockRollCallDao.removeLectureBlockRollCallAbsenceNotice(reloadedRollCall);
		Assert.assertEquals(1, removeRows);
		dbInstance.commitAndCloseSession();
		
		reloadedRollCall = lectureBlockRollCallDao.loadByKey(rollCall.getKey());
		Assert.assertNull(reloadedRollCall.getAbsenceNotice());
	}
	
	/**
	 * Very simple example with only one participant and one lecture block.
	 */
	@Test
	public void getStatistics() {
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-lectures-1");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("author-lectures-1");
		RepositoryEntry entryLecture = JunitTestHelper.createRandomRepositoryEntry(author);
		
		entryLecture.setEntryStatus(RepositoryEntryStatusEnum.published);
		entryLecture = repositoryService.update(entryLecture);

		RepositoryEntryLectureConfiguration lectureConfig = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture);
		lectureConfig.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig);
		dbInstance.commitAndCloseSession();
		
		repositoryEntryRelationDao.addRole(user, entryLecture, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(author, entryLecture, GroupRoles.owner.name());
		
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entryLecture);
		lectureBlock.setStartDate(DateUtils.addHours(new Date(), -2));
		lectureBlock.setEndDate(DateUtils.addHours(new Date(), -1));
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setEffectiveLecturesNumber(4);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlock.setStatus(LectureBlockStatus.done);
		lectureBlock = lectureBlockDao.update(lectureBlock);
		
		lectureParticipantSummaryDao.createSummary(entryLecture, user, DateUtils.addDays(new Date(), -2));
		
		Group entryGroup = repositoryEntryRelationDao.getDefaultGroup(entryLecture);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, entryGroup);
		
		LectureBlockRollCall rollCall = lectureBlockRollCallDao.createAndPersistRollCall(lectureBlock, user,
				null, null, null, null, null, Collections.emptyList());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rollCall);
		
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		params.setEntries(List.of(entryLecture));
		List<LectureBlockIdentityStatistics> stats = lectureBlockRollCallDao.getStatistics(params, List.of(), author, false, false, false, false, true, 0.8d);
		Assert.assertNotNull(stats);
		Assert.assertEquals(1, stats.size());
		
		LectureBlockIdentityStatistics userStats = stats.get(0);
		Assert.assertEquals(4l, userStats.getTotalAttendedLectures());
		Assert.assertEquals(4l, userStats.getTotalEffectiveLectures());
		Assert.assertEquals(4l, userStats.getTotalPersonalPlannedLectures());
	}
	
	@Test
	public void getCoaches() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("lecturer-15");
		LectureBlock lectureBlock = createMinimalLectureBlock(3);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		Set<Long> blockKeys = new HashSet<>();
		blockKeys.add(lectureBlock.getKey());
		Map<Long,String> coaches = lectureBlockRollCallDao.getCoaches(blockKeys, ",");
		Assert.assertEquals(1, coaches.size());
		String teacherName = coaches.values().iterator().next();
		System.out.println(teacherName);
		Assert.assertEquals(teacher.getUser().getLastName() + ", " + teacher.getUser().getFirstName(), teacherName);
	}

	private LectureBlock createMinimalLectureBlock(int numOfLectures) {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(numOfLectures);
		lectureBlock.setEffectiveLecturesNumber(numOfLectures);
		return lectureBlockDao.update(lectureBlock);
	}
}
