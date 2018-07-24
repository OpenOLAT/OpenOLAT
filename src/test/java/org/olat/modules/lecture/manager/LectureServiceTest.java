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

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.vitero.model.GroupRole;
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
	private RepositoryEntryRelationDAO repositoryEntryRelationDAO;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	
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
		BusinessGroup group = businessGroupService.createBusinessGroup(coachGroup, "For lectures", "tg", null, null, false, false, entry);
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
		businessGroupService.deleteBusinessGroup(group);
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
		repositoryService.deletePermanently(entry, owner, roles, Locale.ENGLISH);
		dbInstance.commit();
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
