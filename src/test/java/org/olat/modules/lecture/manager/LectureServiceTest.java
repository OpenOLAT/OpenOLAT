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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupService;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockAuditLog;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private LectureBlockAuditLogDAO lectureBlockAuditLogDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDAO;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	@Test
	public void addTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		Identity notTeacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		
		// enable lecture on this entry
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		config = lectureService.updateRepositoryEntryLectureConfiguration(config);

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		boolean isTeacher = lectureService.hasLecturesAsTeacher(entry, teacher);
		Assert.assertTrue(isTeacher);
		boolean isNotTeacher = lectureService.hasLecturesAsTeacher(entry, notTeacher);
		Assert.assertFalse(isNotTeacher);
	}
	
	@Test
	public void getLectureBlocks_teacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlock> myBlocks = lectureService.getLectureBlocks(entry, teacher);
		Assert.assertNotNull(myBlocks);
		Assert.assertEquals(1, myBlocks.size());
		Assert.assertEquals(lectureBlock, myBlocks.get(0));
	}
	
	@Test
	public void getParticipants() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-4-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-4-2");
		// a lecture block
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		// add 2 participants to the "course"
		repositoryEntryRelationDAO.addRole(participant1, entry, GroupRole.participant.name());
		repositoryEntryRelationDAO.addRole(participant2, entry, GroupRole.participant.name());
		dbInstance.commitAndCloseSession();
		// add the course to the lecture
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock = lectureService.save(lectureBlock, Collections.singletonList(defGroup));
		dbInstance.commitAndCloseSession();
		
		List<Identity> participants = lectureService.getParticipants(lectureBlock);
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue( participants.contains(participant1));
		Assert.assertTrue( participants.contains(participant2));
	}
	
	@Test
	public void getLectureStatistics_checkQuerySyntax() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-5-1");
		// a lecture block
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		// add participant to the "course"
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		dbInstance.commitAndCloseSession();
		//enable lectures
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);
		
		// add the course to the lecture
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock = lectureService.save(lectureBlock, Collections.singletonList(defGroup));
		dbInstance.commitAndCloseSession();
		lectureService.addRollCall(participant, lectureBlock, null, Collections.singletonList(3));
		dbInstance.commitAndCloseSession();
		
		//add
		List<LectureBlockStatistics> statistics = lectureService.getParticipantLecturesStatistics(participant);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(1, statistics.size());
	}
	
	@Test
	public void getParticipantLecturesStatistics() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-6-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-6-2");
		// a closed lecture block in the past
		LectureBlock lectureBlock1 = createClosedLectureBlockInPast(entry);
		LectureBlock lectureBlock2 = createClosedLectureBlockInPast(entry);
		LectureBlock lectureBlock3 = createClosedLectureBlockInPast(entry);
		// create summary in the past
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -4);
		lectureParticipantSummaryDao.createSummary(entry, participant1, cal.getTime());
		lectureParticipantSummaryDao.createSummary(entry, participant2, cal.getTime());
		// add participants to the "course"
		repositoryEntryRelationDAO.addRole(participant1, entry, GroupRole.participant.name());
		repositoryEntryRelationDAO.addRole(participant2, entry, GroupRole.participant.name());
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);
		
		// add the course to the lectures
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock1 = lectureService.save(lectureBlock1, Collections.singletonList(defGroup));
		lectureBlock2 = lectureService.save(lectureBlock2, Collections.singletonList(defGroup));
		lectureBlock3 = lectureService.save(lectureBlock3, Collections.singletonList(defGroup));
		dbInstance.commitAndCloseSession();
		lectureService.addRollCall(participant1, lectureBlock1, null, toList(1, 2));
		lectureService.addRollCall(participant1, lectureBlock2, null, toList(1, 2, 3, 4));
		lectureService.addRollCall(participant2, lectureBlock1, null, toList(1, 2, 3, 4));
		lectureService.addRollCall(participant2, lectureBlock3, null, toList(2, 3, 4));
		dbInstance.commitAndCloseSession();

		//check first participant
		List<LectureBlockStatistics> statistics_1 = lectureService.getParticipantLecturesStatistics(participant1);
		Assert.assertNotNull(statistics_1);
		Assert.assertEquals(1, statistics_1.size());
		LectureBlockStatistics statistic_1 = statistics_1.get(0);
		Assert.assertEquals(12, statistic_1.getTotalPersonalPlannedLectures());
		Assert.assertEquals(2, statistic_1.getTotalAttendedLectures());
		Assert.assertEquals(6, statistic_1.getTotalAbsentLectures());
		
		//check second participant
		List<LectureBlockStatistics> statistics_2 = lectureService.getParticipantLecturesStatistics(participant2);
		Assert.assertNotNull(statistics_2);
		Assert.assertEquals(1, statistics_2.size());
		LectureBlockStatistics statistic_2 = statistics_2.get(0);
		Assert.assertEquals(12, statistic_2.getTotalPersonalPlannedLectures());
		Assert.assertEquals(1, statistic_2.getTotalAttendedLectures());
		Assert.assertEquals(7, statistic_2.getTotalAbsentLectures());
	}
	
	@Test
	public void getRepositoryEntryLectureConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(config);
		Assert.assertEquals(entry, config.getEntry());
	}
	
	@Test
	public void deleteBusinessGroupWithLectures() {
		//prepare a course with a business group
		Identity coachGroup = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-grp");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		//add business group
		BusinessGroup group = businessGroupService.createBusinessGroup(coachGroup, "For lectures", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);
	    businessGroupService.addResourceTo(group, entry);
	    dbInstance.commit();
	    
	    // create a lecture block
	    LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		List<Group> groups = new ArrayList<>();
		groups.add(group.getBaseGroup());
		Group defGroup = repositoryService.getDefaultGroup(entry);
		groups.add(defGroup);
		LectureBlock block = lectureService.save(lectureBlock, groups);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(block);
		
		//delete the group
		businessGroupLifecycleManager.deleteBusinessGroup(group, null, false);
		dbInstance.commitAndCloseSession();
		
		//retrieve lecture block
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry);
		Assert.assertNotNull(blocks);
	    Assert.assertEquals(1, blocks.size());
	    LectureBlock reloadedBlock = blocks.get(0);
	    Assert.assertNotNull(reloadedBlock);
	    //check that the group associate with the repository entry is there
	    Set<LectureBlockToGroup> lectureBlockToGroups = ((LectureBlockImpl)reloadedBlock).getGroups();
	    Assert.assertNotNull(lectureBlockToGroups);
	    Assert.assertEquals(1, lectureBlockToGroups.size());
	    LectureBlockToGroup lectureBlockToGroup = lectureBlockToGroups.iterator().next();
	    Assert.assertEquals(defGroup, lectureBlockToGroup.getGroup());
	}
	
	@Test
	public void deleteLectureBlocksWithTeachers() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-owner-del");
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-del");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(owner);
		LectureBlock lectureBlock1 = createMinimalLectureBlock(entry);
		LectureBlock lectureBlock2 = createMinimalLectureBlock(entry);
		LectureBlock lectureBlock3 = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock1, teacher);
		lectureService.addTeacher(lectureBlock2, teacher);
		lectureService.addTeacher(lectureBlock3, teacher);
		dbInstance.commitAndCloseSession();
		
		//delete and hope
		Roles roles = Roles.administratorRoles();
		ErrorList errors = repositoryService.deletePermanently(entry, owner, roles, Locale.ENGLISH);
		dbInstance.commit();
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	
	@Test
	public void moveAndHeal() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry targetEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-9");
		// a closed lecture block in the past
		LectureBlock lectureBlock1 = createClosedLectureBlockInPast(entry);
		LectureBlock lectureBlock2 = createClosedLectureBlockInPast(entry);
		LectureBlock lectureBlock3 = createClosedLectureBlockInPast(entry);
		// create summary in the past
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -4);
		lectureParticipantSummaryDao.createSummary(entry, participant, cal.getTime());
		// add participants to the "course"
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		dbInstance.commitAndCloseSession();
		LectureParticipantSummary firstSummary = lectureParticipantSummaryDao.getSummary(entry, participant);
		
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);

		// add the course to the lectures
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock1 = lectureService.save(lectureBlock1, Collections.singletonList(defGroup));
		lectureBlock2 = lectureService.save(lectureBlock2, Collections.singletonList(defGroup));
		lectureBlock3 = lectureService.save(lectureBlock3, Collections.singletonList(defGroup));
		dbInstance.commitAndCloseSession();
		
		lectureBlockAuditLogDao.auditLog(LectureBlockAuditLog.Action.createLectureBlock, "3", "4", "Update absence", lectureBlock1, null, entry, participant, participant);

		// add roll call
		lectureService.addRollCall(participant, lectureBlock1, null, toList(1, 2));
		lectureService.addRollCall(participant, lectureBlock2, null, toList(1, 2, 3, 4));
		lectureService.addRollCall(participant, lectureBlock3, null, toList(2, 3, 4));
		dbInstance.commitAndCloseSession();

		lectureService.moveLectureBlock(lectureBlock1, targetEntry);
		lectureService.moveLectureBlock(lectureBlock2, targetEntry);
		lectureService.moveLectureBlock(lectureBlock3, targetEntry);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockAuditLog> beforeHealLogs = lectureBlockAuditLogDao.getAuditLog(entry);
		Assert.assertNotNull(beforeHealLogs);
		Assert.assertEquals(1, beforeHealLogs.size());
		
		
		// heal
		int rows = ((LectureServiceImpl)lectureService).healMovedLectureBlocks(targetEntry, entry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotEquals(0, rows);
		
		LectureParticipantSummary summary = lectureParticipantSummaryDao.getSummary(targetEntry, participant);
		Assert.assertNotNull(summary);
		Assert.assertEquals(firstSummary.getFirstAdmissionDate(), summary.getFirstAdmissionDate());
		

		List<LectureBlockAuditLog> targetLogs = lectureBlockAuditLogDao.getAuditLog(targetEntry);
		Assert.assertNotNull(targetLogs);
		Assert.assertEquals(1, targetLogs.size());
		
		List<LectureBlockAuditLog> logs = lectureBlockAuditLogDao.getAuditLog(entry);
		Assert.assertNotNull(logs);
		Assert.assertEquals(0, logs.size());
	}
	
	@Test
	public void addNotice_lectureblocks() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		
		// add roles
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		
		LectureBlock block1 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block1, teacher);
		LectureBlock block2 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block2, teacher);

		dbInstance.commit();
		
		List<LectureBlock> lectureBlocks = new ArrayList<>();
		lectureBlocks.add(block1);

		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());
		AbsenceNotice notice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.absence, AbsenceNoticeTarget.lectureblocks,
				start, end, null, null, null, null, lectureBlocks, teacher);
		dbInstance.commitAndCloseSession();

		// first roll call
		LectureBlockRollCall rollCall1 = lectureService.getOrCreateRollCall(participant, block1, null, null, null);
		dbInstance.commitAndCloseSession();

		LectureBlockRollCall reloadedRollCall1 = lectureService.getRollCall(rollCall1);
		Assert.assertNotNull(notice);
		Assert.assertNotNull(reloadedRollCall1);
		Assert.assertEquals(notice, reloadedRollCall1.getAbsenceNotice());
		
		// second roll call
		LectureBlockRollCall rollCall2 = lectureService.getOrCreateRollCall(participant, block2, null, null, null);
		dbInstance.commitAndCloseSession();

		LectureBlockRollCall reloadedRollCall2 = lectureService.getRollCall(rollCall2);
		Assert.assertNotNull(notice);
		Assert.assertNotNull(reloadedRollCall2);
		Assert.assertNull(reloadedRollCall2.getAbsenceNotice());
	}
	
	@Test
	public void addNotice_entries() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		
		// add roles
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		
		LectureBlock block1 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block1, teacher);
		LectureBlock block2 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block2, teacher);

		dbInstance.commit();
		
		List<RepositoryEntry> entries = new ArrayList<>();
		entries.add(entry);

		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());
		AbsenceNotice notice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.absence, AbsenceNoticeTarget.entries,
				start, end, null, null, null, entries, null, teacher);
		dbInstance.commitAndCloseSession();

		// first roll call
		LectureBlockRollCall rollCall1 = lectureService.getOrCreateRollCall(participant, block1, null, null, null);
		dbInstance.commitAndCloseSession();

		LectureBlockRollCall reloadedRollCall1 = lectureService.getRollCall(rollCall1);
		Assert.assertNotNull(notice);
		Assert.assertNotNull(reloadedRollCall1);
		Assert.assertEquals(notice, reloadedRollCall1.getAbsenceNotice());
		
		// second roll call
		LectureBlockRollCall rollCall2 = lectureService.getOrCreateRollCall(participant, block2, null, null, null);
		dbInstance.commitAndCloseSession();

		LectureBlockRollCall reloadedRollCall2 = lectureService.getRollCall(rollCall2);
		Assert.assertNotNull(notice);
		Assert.assertNotNull(reloadedRollCall2);
		Assert.assertEquals(notice, reloadedRollCall2.getAbsenceNotice());
	}
	
	@Test
	public void addNoticeAfterRollCall_entries() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("absent-1");
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		
		// add roles
		repositoryEntryRelationDAO.addRole(participant, entry, GroupRole.participant.name());
		
		LectureBlock block1 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block1, teacher);
		LectureBlock block2 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block2, teacher);
		LectureBlock block3 = createMinimalLectureBlock(entry);
		lectureService.addTeacher(block3, teacher);
		dbInstance.commit();
		
		// make roll call
		LectureBlockRollCall rollCall1 = lectureService.getOrCreateRollCall(participant, block1, null, null, null);
		LectureBlockRollCall rollCall2 = lectureService.getOrCreateRollCall(participant, block2, null, null, null);
		LectureBlockRollCall rollCall3 = lectureService.getOrCreateRollCall(participant, block3, null, null, null);
		dbInstance.commitAndCloseSession();

		// add an notice of absence
		List<LectureBlock> lectureBlocks = new ArrayList<>();
		lectureBlocks.add(block1);
		lectureBlocks.add(block2);

		Date start = CalendarUtils.startOfDay(new Date());
		Date end = CalendarUtils.endOfDay(new Date());
		AbsenceNotice notice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				start, end, null, null, null, null, lectureBlocks, teacher);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(notice);

		// check first roll call
		LectureBlockRollCall reloadedRollCall1 = lectureService.getRollCall(rollCall1);
		Assert.assertNotNull(reloadedRollCall1);
		Assert.assertEquals(notice, reloadedRollCall1.getAbsenceNotice());

		// check second roll call
		LectureBlockRollCall reloadedRollCall2 = lectureService.getRollCall(rollCall2);
		Assert.assertNotNull(reloadedRollCall2);
		Assert.assertEquals(notice, reloadedRollCall2.getAbsenceNotice());
		
		// check third roll call
		LectureBlockRollCall reloadedRollCall3 = lectureService.getRollCall(rollCall3);
		Assert.assertNotNull(reloadedRollCall3);
		Assert.assertNull(reloadedRollCall3.getAbsenceNotice());
	}
	
	@Test
	public void getAbsenceNoticeUniquelyRelatedTo() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-2");
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();

		LectureBlock block1 = createMinimalLectureBlock(entry1);
		LectureBlock block2 = createMinimalLectureBlock(entry2);
		LectureBlock block3 = createMinimalLectureBlock(entry2);
		LectureBlock block4 = createMinimalLectureBlock(entry2);
		dbInstance.commit();

		List<LectureBlock> lectureBlocks = new ArrayList<>();
		lectureBlocks.add(block1);
		lectureBlocks.add(block2);

		AbsenceNotice notice1_2 = lectureService.createAbsenceNotice(participant1, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, null, null, null, lectureBlocks, null);
		AbsenceNotice notice1_2_alt = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, null, null, null, lectureBlocks, null);
		AbsenceNotice notice3 = lectureService.createAbsenceNotice(participant1, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, null, null, null, Collections.singletonList(block3), null);
		AbsenceNotice notice3b = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, null, null, null, Collections.singletonList(block3), null);
		
		List<LectureBlock> lectureSecondBlocks = new ArrayList<>();
		lectureSecondBlocks.add(block1);
		lectureSecondBlocks.add(block2);
		lectureSecondBlocks.add(block4);
		
		AbsenceNotice notice4 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, null, null, null, lectureSecondBlocks, null);
		dbInstance.commit();
		Assert.assertNotNull(notice4);
		Assert.assertNotNull(notice1_2);
		Assert.assertNotNull(notice1_2_alt);
		
		List<LectureBlock> allLectureBlocks = new ArrayList<>();
		allLectureBlocks.add(block1);
		allLectureBlocks.add(block3);
		
		// check block 1 + 3
		List<AbsenceNotice> uniquelyRelatedNotices = lectureService.getAbsenceNoticeUniquelyRelatedTo(allLectureBlocks);
		Assert.assertNotNull(uniquelyRelatedNotices);
		Assert.assertEquals(2, uniquelyRelatedNotices.size());
		Assert.assertTrue(uniquelyRelatedNotices.contains(notice3));
		Assert.assertTrue(uniquelyRelatedNotices.contains(notice3b));

		List<AbsenceNotice> uniquelyRelatedNoticesTo4 = lectureService.getAbsenceNoticeUniquelyRelatedTo(Collections.singletonList(block4));
		Assert.assertNotNull(uniquelyRelatedNoticesTo4);
		Assert.assertTrue(uniquelyRelatedNoticesTo4.isEmpty());
		
		// dummy check
		List<AbsenceNotice> emptyList = lectureService.getAbsenceNoticeUniquelyRelatedTo(Collections.emptyList());
		Assert.assertNotNull(emptyList);
		Assert.assertTrue(emptyList.isEmpty());
	}
	
	@Test
	public void getAbsenceNoticeUniquelyRelatedTo_minimal() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();

		LectureBlock block = createMinimalLectureBlock(entry);
		dbInstance.commit();

		AbsenceNotice noticeEntries = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.allentries,
				new Date(), new Date(), null, null, null, null, null, null);
		AbsenceNotice noticeLectures = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, null, null, null, Collections.singletonList(block), null);

		// check
		List<AbsenceNotice> uniquelyRelatedNotices = lectureService.getAbsenceNoticeUniquelyRelatedTo(Collections.singletonList(block));
		Assert.assertNotNull(uniquelyRelatedNotices);
		Assert.assertEquals(1, uniquelyRelatedNotices.size());
		Assert.assertTrue(uniquelyRelatedNotices.contains(noticeLectures));
		Assert.assertFalse(uniquelyRelatedNotices.contains(noticeEntries));
	}
	
	/**
	 * Lengthy test to check the method with more than a few notices
	 * and cross check different cases.
	 * 
	 */
	@Test
	public void getAbsenceNoticeUniquelyRelatedTo_variant() {
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-2");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-3");
		RepositoryEntry entry1 = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry entry2 = JunitTestHelper.createAndPersistRepositoryEntry();

		LectureBlock block1 = createMinimalLectureBlock(entry1);
		LectureBlock block2 = createMinimalLectureBlock(entry1);
		LectureBlock block3 = createMinimalLectureBlock(entry1);
		LectureBlock block4 = createMinimalLectureBlock(entry2);
		LectureBlock block5 = createMinimalLectureBlock(entry2);
		LectureBlock block6 = createMinimalLectureBlock(entry2);
		dbInstance.commit();

		List<LectureBlock> lectureBlocks1_2 = new ArrayList<>();
		lectureBlocks1_2.add(block1);
		lectureBlocks1_2.add(block2);

		AbsenceNotice notice1_2 = lectureService.createAbsenceNotice(participant1, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "1 2", null, null, lectureBlocks1_2, null);
		
		List<LectureBlock> lectureBlocks2_3_4 = new ArrayList<>();
		lectureBlocks2_3_4.add(block2);
		lectureBlocks2_3_4.add(block3);
		lectureBlocks2_3_4.add(block4);
		AbsenceNotice notice2_3_4 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "2 3 4", null, null, lectureBlocks2_3_4, null);
		
		List<LectureBlock> lectureBlocks1_3_4 = new ArrayList<>();
		lectureBlocks1_3_4.add(block1);
		lectureBlocks1_3_4.add(block3);
		lectureBlocks1_3_4.add(block4);
		AbsenceNotice notice1_3_4 = lectureService.createAbsenceNotice(participant3, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "1 3 4", null, null, lectureBlocks1_3_4, null);
		
		AbsenceNotice notice4 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "4", null, null, Collections.singletonList(block4), null);
		
		List<LectureBlock> lectureSecondBlocks4_5 = new ArrayList<>();
		lectureSecondBlocks4_5.add(block4);
		lectureSecondBlocks4_5.add(block5);
		AbsenceNotice notice4_5 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "4 5", null, null, lectureSecondBlocks4_5, null);
		
		List<LectureBlock> lectureBlocks4_5_6 = new ArrayList<>();
		lectureBlocks4_5_6.add(block4);
		lectureBlocks4_5_6.add(block5);
		lectureBlocks4_5_6.add(block6);
		AbsenceNotice notice4_5_6 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "4 5 6", null, null, lectureBlocks4_5_6, null);
		
		AbsenceNotice notice6 = lectureService.createAbsenceNotice(participant2, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				new Date(), new Date(), null, "6", null, null, Collections.singletonList(block6), null);
		dbInstance.commit();
		
		
		// check blocks 4, 5, 6
		List<AbsenceNotice> uniquelyRelatedNotices4_5_6 = lectureService.getAbsenceNoticeUniquelyRelatedTo(lectureBlocks4_5_6);
		Assert.assertNotNull(uniquelyRelatedNotices4_5_6);
		Assert.assertEquals(4, uniquelyRelatedNotices4_5_6.size());
		Assert.assertTrue(uniquelyRelatedNotices4_5_6.contains(notice4));
		Assert.assertTrue(uniquelyRelatedNotices4_5_6.contains(notice4_5));
		Assert.assertTrue(uniquelyRelatedNotices4_5_6.contains(notice4_5_6));
		Assert.assertTrue(uniquelyRelatedNotices4_5_6.contains(notice6));
		
		// check blocks 1 5
		List<LectureBlock> lectureBlocks1_5 = new ArrayList<>();
		lectureBlocks1_5.add(block1);
		lectureBlocks1_5.add(block3);
		List<AbsenceNotice> uniquelyRelatedNotices1_5 = lectureService.getAbsenceNoticeUniquelyRelatedTo(lectureBlocks1_5);
		Assert.assertNotNull(uniquelyRelatedNotices1_5);
		Assert.assertTrue(uniquelyRelatedNotices1_5.isEmpty());
		
		// check blocks 1 2
		lectureBlocks1_2.add(block1);// part of the test
		lectureBlocks1_2.add(block2);
		List<AbsenceNotice> uniquelyRelatedNotices1_2 = lectureService.getAbsenceNoticeUniquelyRelatedTo(lectureBlocks1_2);
		Assert.assertNotNull(uniquelyRelatedNotices1_2);
		Assert.assertEquals(1, uniquelyRelatedNotices1_2.size());
		Assert.assertTrue(uniquelyRelatedNotices1_2.contains(notice1_2));
		
		// check blocks 4
		List<LectureBlock> lectureBlocks4 = new ArrayList<>();
		lectureBlocks4.add(block4);
		List<AbsenceNotice> uniquelyRelatedNotices4 = lectureService.getAbsenceNoticeUniquelyRelatedTo(lectureBlocks4);
		Assert.assertNotNull(uniquelyRelatedNotices4);
		Assert.assertEquals(1, uniquelyRelatedNotices4.size());
		Assert.assertTrue(uniquelyRelatedNotices4.contains(notice4));
		
		// check blocks 2, 3, 4, 5, 6
		List<LectureBlock> lectureBlocks2_3_4_5_6 = new ArrayList<>();
		lectureBlocks2_3_4_5_6.add(block2);
		lectureBlocks2_3_4_5_6.add(block3);
		lectureBlocks2_3_4_5_6.add(block4);
		lectureBlocks2_3_4_5_6.add(block5);
		lectureBlocks2_3_4_5_6.add(block6);
		List<AbsenceNotice> uniquelyRelatedNotices2_3_4_5_6 = lectureService.getAbsenceNoticeUniquelyRelatedTo(lectureBlocks2_3_4_5_6);
		Assert.assertNotNull(uniquelyRelatedNotices2_3_4_5_6);
		Assert.assertEquals(5, uniquelyRelatedNotices2_3_4_5_6.size());
		Assert.assertTrue(uniquelyRelatedNotices2_3_4_5_6.contains(notice2_3_4));
		Assert.assertTrue(uniquelyRelatedNotices2_3_4_5_6.contains(notice4));
		Assert.assertTrue(uniquelyRelatedNotices2_3_4_5_6.contains(notice4_5));
		Assert.assertTrue(uniquelyRelatedNotices2_3_4_5_6.contains(notice4_5_6));
		Assert.assertTrue(uniquelyRelatedNotices2_3_4_5_6.contains(notice6));
		
		// check all blocks
		List<LectureBlock> lectureBlocks1_2_3_4_5_6 = new ArrayList<>();
		lectureBlocks1_2_3_4_5_6.add(block1);
		lectureBlocks1_2_3_4_5_6.add(block2);
		lectureBlocks1_2_3_4_5_6.add(block3);
		lectureBlocks1_2_3_4_5_6.add(block4);
		lectureBlocks1_2_3_4_5_6.add(block5);
		lectureBlocks1_2_3_4_5_6.add(block6);
		List<AbsenceNotice> uniquelyRelatedNotices1_2_3_4_5_6 = lectureService.getAbsenceNoticeUniquelyRelatedTo(lectureBlocks1_2_3_4_5_6);
		Assert.assertNotNull(uniquelyRelatedNotices1_2_3_4_5_6);
		Assert.assertEquals(7, uniquelyRelatedNotices1_2_3_4_5_6.size());
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice1_2));
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice1_3_4));
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice2_3_4));
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice4));
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice4_5));
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice4_5_6));
		Assert.assertTrue(uniquelyRelatedNotices1_2_3_4_5_6.contains(notice6));
	}
	
	@Test
	public void getAbsenceNoticePreferedOne() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock block = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		AbsenceNotice longNotice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.entries,
				DateUtils.addDays(new Date(), -2), DateUtils.addDays(new Date(), 2), null, null, Boolean.TRUE, List.of(entry), null, null);
		sleep(100);// ensure different creation date
		AbsenceNotice specificNotice = lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				block.getStartDate(), block.getEndDate(), null, null, null, null, List.of(block), null);
		dbInstance.commitAndCloseSession();
		
		AbsenceNotice notice = lectureService.getAbsenceNotice(participant, block);
		Assert.assertEquals(longNotice, notice);
		Assert.assertNotEquals(specificNotice, notice);
	}
	
	@Test
	public void getAbsenceNoticePreferedOneNot() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("noticee-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock block = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.entries,
				DateUtils.addDays(new Date(), -2), DateUtils.addDays(new Date(), 2), null, null, null, List.of(entry), null, null);
		lectureService.createAbsenceNotice(participant, AbsenceNoticeType.notified, AbsenceNoticeTarget.lectureblocks,
				block.getStartDate(), block.getEndDate(), null, null, null, null, List.of(block), null);
		dbInstance.commitAndCloseSession();
		
		AbsenceNotice notice = lectureService.getAbsenceNotice(participant, block);
		Assert.assertNotNull(notice);
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
	private LectureBlock createClosedLectureBlockInPast(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -2);
		lectureBlock.setStartDate(cal.getTime());
		lectureBlock.setEndDate(cal.getTime());
		lectureBlock.setStatus(LectureBlockStatus.done);
		lectureBlock.setRollCallStatus(LectureRollCallStatus.closed);
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setEffectiveLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
	private List<Integer> toList(Integer... integers) {
		List<Integer> list = new ArrayList<>();
		for(int i=0; i<integers.length; i++) {
			list.add(integers[i]);
		}
		return list;
	}
}
