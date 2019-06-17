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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void createLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(lectureBlock);
		Assert.assertNotNull(lectureBlock.getKey());
		Assert.assertNotNull(lectureBlock.getCreationDate());
		Assert.assertNotNull(lectureBlock.getLastModified());
		Assert.assertNotNull(lectureBlock.getStartDate());
		Assert.assertNotNull(lectureBlock.getEndDate());
		Assert.assertEquals("Hello lecturers", lectureBlock.getTitle());
		Assert.assertEquals(LectureBlockStatus.active, lectureBlock.getStatus());
		Assert.assertEquals(LectureRollCallStatus.open, lectureBlock.getRollCallStatus());
	}
	
	@Test
	public void createAndLoadLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Bienvenue");
		lectureBlock.setExternalId("XYZ-8976");
		lectureBlock.setLocation("Basel");
		lectureBlock.setDescription("Welcome");
		lectureBlock.setPreparation("Prepare you");
		lectureBlock.setEffectiveEndDate(new Date());
		lectureBlock.setComment("A little comment");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		LectureBlock reloadedBlock = lectureBlockDao.loadByKey(lectureBlock.getKey());
		Assert.assertNotNull(reloadedBlock);
		Assert.assertNotNull(reloadedBlock.getKey());
		Assert.assertNotNull(reloadedBlock.getCreationDate());
		Assert.assertNotNull(reloadedBlock.getLastModified());
		Assert.assertNotNull(reloadedBlock.getStartDate());
		Assert.assertNotNull(reloadedBlock.getEndDate());
		Assert.assertNotNull(reloadedBlock.getEffectiveEndDate());
		
		Assert.assertEquals("Bienvenue", reloadedBlock.getTitle());
		Assert.assertEquals("XYZ-8976", reloadedBlock.getExternalId());
		Assert.assertEquals("Basel", reloadedBlock.getLocation());
		Assert.assertEquals("Welcome", reloadedBlock.getDescription());
		Assert.assertEquals("Prepare you", reloadedBlock.getPreparation());
		Assert.assertEquals("A little comment", reloadedBlock.getComment());
		
		Assert.assertEquals(LectureBlockStatus.active, reloadedBlock.getStatus());
		Assert.assertEquals(LectureRollCallStatus.open, reloadedBlock.getRollCallStatus());
	}
	
	@Test
	public void getLectureBlocks_entry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlock> blocks = lectureBlockDao.getLectureBlocks(entry);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		LectureBlock loadedBlock = blocks.get(0);
		Assert.assertEquals(lectureBlock, loadedBlock);
	}
	
	@Test
	public void searchLectureBlocks() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-teacher-1");
		Identity lectureManager = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-manager-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		repositoryEntryRelationDao.addRole(lectureManager, entry, OrganisationRoles.lecturemanager.name());
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecture manager");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(lectureManager);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		LectureBlock loadedBlock = blocks.get(0);
		Assert.assertEquals(lectureBlock, loadedBlock);
	}
	
	@Test
	public void searchLectureBlocks_lectureDisabled() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-teacher-1");
		Identity lectureManager = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-manager-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryRelationDao.addRole(lectureManager, entry, OrganisationRoles.lecturemanager.name());
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecture manager");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(lectureManager);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertTrue(blocks.isEmpty());
	}
	
	@Test
	public void getLectureBlocks_all() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Get them all");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlock> blocks = lectureBlockDao.getLectureBlocks();
		Assert.assertNotNull(blocks);
		Assert.assertTrue(blocks.size() >= 1);
		Assert.assertTrue(blocks.contains(lectureBlock));
	}
	
	@Test
	public void loadByTeachers() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search all
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		List<LectureBlock> blocks = lectureBlockDao.loadByTeacher(teacher, searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
	}
	
	@Test
	public void loadByTeachers_lectureDisabled() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search all
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		List<LectureBlock> blocks = lectureBlockDao.loadByTeacher(teacher, searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertTrue(blocks.isEmpty());
	}
	
	@Test
	public void loadByTeachers_searchString() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setSearchString("lecturers");
		List<LectureBlock> blocks = lectureBlockDao.loadByTeacher(teacher, searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
		
		//search lectures with a string which is not available to this teacher
		LecturesBlockSearchParameters searchNegativeParams = new LecturesBlockSearchParameters();
		searchNegativeParams.setSearchString("goodbye");
		List<LectureBlock> negativeBlocks = lectureBlockDao.loadByTeacher(teacher, searchNegativeParams);
		Assert.assertNotNull(negativeBlocks);
		Assert.assertEquals(0, negativeBlocks.size());
	}
	
	@Test
	public void loadByTeachers_startEndDates() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchNowParams = new LecturesBlockSearchParameters();
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		searchNowParams.setStartDate(now.getTime());
		now.add(Calendar.DATE, 2);
		searchNowParams.setEndDate(now.getTime());
		List<LectureBlock> blocks = lectureBlockDao.loadByTeacher(teacher, searchNowParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
		
		//search in future
		LecturesBlockSearchParameters searchFutureParams = new LecturesBlockSearchParameters();
		now.add(Calendar.DATE, 2);
		searchFutureParams.setStartDate(now.getTime());
		now.add(Calendar.DATE, 2);
		searchFutureParams.setEndDate(now.getTime());
		List<LectureBlock> futureBlocks = lectureBlockDao.loadByTeacher(teacher, searchFutureParams);
		Assert.assertNotNull(futureBlocks);
		Assert.assertEquals(0, futureBlocks.size());
	}
	
	@Test
	public void loadByTeachers_startDate() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchNowParams = new LecturesBlockSearchParameters();
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		searchNowParams.setStartDate(now.getTime());
		List<LectureBlock> blocks = lectureBlockDao.loadByTeacher(teacher, searchNowParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
		
		//search in future
		LecturesBlockSearchParameters searchFutureParams = new LecturesBlockSearchParameters();
		now.add(Calendar.DATE, 2);
		searchFutureParams.setStartDate(now.getTime());
		List<LectureBlock> futureBlocks = lectureBlockDao.loadByTeacher(teacher, searchFutureParams);
		Assert.assertNotNull(futureBlocks);
		Assert.assertEquals(0, futureBlocks.size());
	}
	
	@Test
	public void loadByTeachers_endDate() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchNowParams = new LecturesBlockSearchParameters();
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		searchNowParams.setEndDate(now.getTime());
		List<LectureBlock> blocks = lectureBlockDao.loadByTeacher(teacher, searchNowParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(0, blocks.size());
		
		//search in future
		LecturesBlockSearchParameters searchFutureParams = new LecturesBlockSearchParameters();
		now.add(Calendar.DATE, 2);
		searchFutureParams.setEndDate(now.getTime());
		List<LectureBlock> futureBlocks = lectureBlockDao.loadByTeacher(teacher, searchFutureParams);
		Assert.assertNotNull(futureBlocks);
		Assert.assertEquals(1, futureBlocks.size());
		Assert.assertEquals(lectureBlock, futureBlocks.get(0));
	}
	
	@Test
	public void loadAssessedByTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-23");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// the lecture is not assessed -> return empty
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		List<LectureBlockRef> blockRefs = lectureBlockDao.loadAssessedByTeacher(teacher, searchParams);
		Assert.assertTrue(blockRefs.isEmpty());
		
		// add an assessment mode
		AssessmentMode assessmentMode = assessmentModeManager.createAssessmentMode(lectureBlock, 5, 5, "", null);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRef> assessedBlockRefs = lectureBlockDao.loadAssessedByTeacher(teacher, searchParams);
		Assert.assertEquals(1, assessedBlockRefs.size());
		Assert.assertEquals(lectureBlock, assessedBlockRefs.get(0));
		Assert.assertEquals(lectureBlock, assessmentMode.getLectureBlock());
	}
	
	@Test
	public void addGroup() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		dbInstance.commitAndCloseSession();

		LectureBlockImpl reloadedLectureBlock = (LectureBlockImpl)lectureBlockDao.loadByKey(lectureBlock.getKey());
		Set<LectureBlockToGroup> blockToGroupSet = reloadedLectureBlock.getGroups();
		Assert.assertNotNull(blockToGroupSet);
		Assert.assertEquals(1,  blockToGroupSet.size());
		LectureBlockToGroupImpl defBlockToGroup = (LectureBlockToGroupImpl)blockToGroupSet.iterator().next();
		Assert.assertEquals(defGroup, defBlockToGroup.getGroup());
		Assert.assertEquals(lectureBlock, defBlockToGroup.getLectureBlock());
	}
	
	@Test
	public void getTeachers() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-7");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// get teachers
		List<Identity> teachers = lectureBlockDao.getTeachers(entry);
		Assert.assertNotNull(teachers);
		Assert.assertEquals(1, teachers.size());
		Assert.assertTrue(teachers.contains(teacher));
	}
	
	@Test
	public void getParticipants_lectureBlock() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-3");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		//add teacher
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commit();
		// add participants
		repositoryEntryRelationDao.addRole(participant1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		// add the course to the lectures
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock = lectureService.save(lectureBlock, Collections.singletonList(defGroup));
		dbInstance.commitAndCloseSession();
		
		List<Identity> participants = lectureBlockDao.getParticipants(lectureBlock);
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(participant1));
		Assert.assertTrue(participants.contains(participant2));
	}

	@Test
	public void getParticipants_repositoryEntry() {
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-1");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-5");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-6");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-7");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();

		// add 2 participants to a business group linked to the repository entry
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "count relation 1", "tg", null, null, false, false, entry);
	    businessGroupRelationDao.addRole(coach1, group, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(participant1, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(participant2, group, GroupRoles.participant.name());

	    // add a participant to the course itself as noise
		repositoryEntryRelationDao.addRole(participant3, entry, GroupRoles.participant.name());
		// add the course to the lectures
		lectureBlock = lectureService.save(lectureBlock, Collections.singletonList(group.getBaseGroup()));
		dbInstance.commitAndCloseSession();
		
		List<Identity> participants = lectureBlockDao.getParticipants(entry);
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(participant1));
		Assert.assertTrue(participants.contains(participant2));
		Assert.assertFalse(participants.contains(participant3));
		Assert.assertFalse(participants.contains(coach1));
	}
	
	@Test
	public void getParticipants_repositoryEntryTeacher() {
		
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-6");
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-2");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-7");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-8");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();

		//add teacher
		lectureService.addTeacher(lectureBlock, teacher);
	    // add a participant to the course itself as noise
		repositoryEntryRelationDao.addRole(coach1, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		// add the course to the lectures
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock = lectureService.save(lectureBlock, Collections.singletonList(defGroup));
		dbInstance.commitAndCloseSession();
		
		List<Identity> participants = lectureBlockDao.getParticipants(entry, teacher);
		Assert.assertNotNull(participants);
		Assert.assertEquals(2, participants.size());
		Assert.assertTrue(participants.contains(participant1));
		Assert.assertTrue(participants.contains(participant2));
		Assert.assertFalse(participants.contains(coach1));
	}
	
	@Test
	public void getParticipants_repositoryEntryTeacher_paranoiaCheck() {
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-7");
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-8");
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("coach-2");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-8");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-9");
		Identity participant3 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-10");
		Identity participant4 = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-11");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock1 = createMinimalLectureBlock(entry);
		LectureBlock lectureBlock2 = createMinimalLectureBlock(entry);
		dbInstance.commit();

		//add teachers
		lectureService.addTeacher(lectureBlock1, teacher1);
		lectureService.addTeacher(lectureBlock2, teacher2);

	    // add a participant to the course itself as noise
		repositoryEntryRelationDao.addRole(coach1, entry, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(participant1, entry, GroupRoles.participant.name());
		repositoryEntryRelationDao.addRole(participant2, entry, GroupRoles.participant.name());
		// add the course to the lectures
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureBlock1 = lectureService.save(lectureBlock1, Collections.singletonList(defGroup));
		dbInstance.commit();
		
		// add 2 participants to a business group linked to the repository entry
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "lectures 2", "tg", null, null, false, false, entry);
	    businessGroupRelationDao.addRole(coach1, group, GroupRoles.coach.name());
	    businessGroupRelationDao.addRole(participant3, group, GroupRoles.participant.name());
	    businessGroupRelationDao.addRole(participant4, group, GroupRoles.participant.name());
		// add the group to the lectures
		lectureBlock2 = lectureService.save(lectureBlock2, Collections.singletonList(group.getBaseGroup()));
		dbInstance.commitAndCloseSession();
		
		// teacher1 see participant 1 and 2
		List<Identity> participantsBlocks1 = lectureBlockDao.getParticipants(entry, teacher1);
		Assert.assertNotNull(participantsBlocks1);
		Assert.assertEquals(2, participantsBlocks1.size());
		Assert.assertTrue(participantsBlocks1.contains(participant1));
		Assert.assertTrue(participantsBlocks1.contains(participant2));
		
		//teacher 2 see participants 3 and 4
		List<Identity> participantsBlock2 = lectureBlockDao.getParticipants(entry, teacher2);
		Assert.assertNotNull(participantsBlock2);
		Assert.assertEquals(2, participantsBlock2.size());
		Assert.assertTrue(participantsBlock2.contains(participant3));
		Assert.assertTrue(participantsBlock2.contains(participant4));
	}
	
	@Test
	public void hasLecturesAsTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		
		// enable lecture on this entry
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		config = lectureService.updateRepositoryEntryLectureConfiguration(config);

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		boolean isTeacher = lectureBlockDao.hasLecturesAsTeacher(entry, teacher);
		Assert.assertTrue(isTeacher);
	}
	
	@Test
	public void getRollCallAsTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		
		// enable lecture on this entry
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		config = lectureService.updateRepositoryEntryLectureConfiguration(config);

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlock> rollcalls = lectureBlockDao.getRollCallAsTeacher(teacher);
		Assert.assertNotNull(rollcalls);
		Assert.assertEquals(1, rollcalls.size());
		Assert.assertEquals(lectureBlock, rollcalls.get(0));
	}
	
	@Test
	public void getLecturesBlocksReport() {
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		
		List<LectureReportRow> allRows = lectureBlockDao.getLecturesBlocksReport(null, null, null);
		Assert.assertNotNull(allRows);
		Assert.assertFalse(allRows.isEmpty());
		
		boolean found = false;
		for(LectureReportRow row:allRows) {
			if(row.getKey().equals(lectureBlock.getKey())) {
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void getLecturesBlocksReport_withParams() {
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -2);
		Date start = calendar.getTime();
		calendar.add(Calendar.DATE, 4);
		Date end = calendar.getTime();
		
		List<LectureRollCallStatus> openStatus = Collections.singletonList(LectureRollCallStatus.open);
		List<LectureReportRow> openRows = lectureBlockDao.getLecturesBlocksReport(start, end, openStatus);
		Assert.assertNotNull(openRows);
		Assert.assertFalse(openRows.isEmpty());
		boolean foundOpen = false;
		for(LectureReportRow row:openRows) {
			if(row.getKey().equals(lectureBlock.getKey())) {
				foundOpen = true;
			}
		}
		Assert.assertTrue(foundOpen);
		
		List<LectureRollCallStatus> closedStatus = Collections.singletonList(LectureRollCallStatus.closed);
		List<LectureReportRow> closedRows = lectureBlockDao.getLecturesBlocksReport(start, end, closedStatus);
		Assert.assertNotNull(closedRows);
		boolean foundClosed = false;
		for(LectureReportRow row:closedRows) {
			if(row.getKey().equals(lectureBlock.getKey())) {
				foundClosed = true;
			}
		}
		Assert.assertFalse(foundClosed);
	}
	
	@Test
	public void deleteLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock block = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		Long blockKey = block.getKey();
		
		// delete the block
		lectureBlockDao.delete(block);
		dbInstance.commitAndCloseSession();
		
		// try to relaod the block
		LectureBlock deletedBlock = lectureBlockDao.loadByKey(blockKey);
		Assert.assertNull(deletedBlock);
	}
	
	private RepositoryEntry createResourceWithLecturesEnabled() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commit();
		RepositoryEntryLectureConfiguration config = lectureService.getRepositoryEntryLectureConfiguration(entry);
		config.setLectureEnabled(true);
		lectureService.updateRepositoryEntryLectureConfiguration(config);
		dbInstance.commit();
		return entry;
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -15);
		lectureBlock.setStartDate(cal.getTime());
		cal.add(Calendar.MINUTE, 30);
		lectureBlock.setEndDate(cal.getTime());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureBlockDao.update(lectureBlock);
	}
}
