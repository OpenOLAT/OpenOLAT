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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
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
	public void getLectureBlocks() {
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
	public void updateLogLectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock block = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		Long blockKey = block.getKey();
		
		String update = "update lectureblock block set block.log=concat(block.log,',',:newLog) where block.key=:blockKey";
		int rows = dbInstance.getCurrentEntityManager()
			.createQuery(update)
			.setParameter("blockKey", blockKey)
			.setParameter("newLog", "New infos")
			.executeUpdate();
		
		Assert.assertEquals(1, rows);
		dbInstance.commitAndCloseSession();

		LectureBlock updatedBlock = lectureBlockDao.loadByKey(blockKey);
		Assert.assertEquals(",New infos", updatedBlock.getLog());
		
		int rows2 = dbInstance.getCurrentEntityManager()
				.createQuery(update)
				.setParameter("blockKey", blockKey)
				.setParameter("newLog", "More infos")
				.executeUpdate();
		
		Assert.assertEquals(1, rows2);
		dbInstance.commitAndCloseSession();

		LectureBlock updated2Block = lectureBlockDao.loadByKey(blockKey);
		Assert.assertEquals(",New infos,More infos", updated2Block.getLog());
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
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock.setLog("");
		return lectureBlockDao.update(lectureBlock);
	}
}
