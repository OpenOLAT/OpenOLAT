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
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRef;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToGroup;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.lecture.model.LectureBlockToGroupImpl;
import org.olat.modules.lecture.model.LectureBlockWithTeachers;
import org.olat.modules.lecture.model.LectureCurriculumElementInfos;
import org.olat.modules.lecture.model.LectureCurriculumElementSearchParameters;
import org.olat.modules.lecture.model.LectureReportRow;
import org.olat.modules.lecture.model.LectureRepositoryEntryInfos;
import org.olat.modules.lecture.model.LectureRepositoryEntrySearchParameters;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.lecture.ui.LectureRoles;
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
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
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
	public void loadByKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello loader of block");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		LectureBlock loadedBlock = lectureBlockDao.loadByKey(lectureBlock.getKey());
		Assert.assertNotNull(loadedBlock);
		Assert.assertEquals(lectureBlock, loadedBlock);
	}
	
	@Test
	public void loadByKeys() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello loader of block");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		List<Long> keys = List.of(lectureBlock.getKey());
		List<LectureBlock> loadedBlocks = lectureBlockDao.loadByKeys(keys);
		Assert.assertNotNull(loadedBlocks);
		Assert.assertEquals(1, loadedBlocks.size());
		Assert.assertEquals(lectureBlock, loadedBlocks.get(0));
	}
	
	@Test
	public void searchLectureBlocks_lectureManager() {
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
	
	/**
	 * Check only the syntax by filling almost all parameters
	 */
	@Test
	public void searchLectureBlocks_allParameters() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("id-1");

		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(id);
		searchParams.setEndDate(new Date());
		searchParams.setParticipant(id);
		searchParams.setSearchString("Hello");
		searchParams.setStartDate(new Date());
		searchParams.setTeacher(id);
		searchParams.setMasterCoach(id);
		searchParams.setManager(id);
		
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
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
	public void searchLectureBlocks_lectureManager_negative() {
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
		
		// other course, other lecture manager
		Identity otherLectureManager = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-manager-alien");
		RepositoryEntry otherEntry = createResourceWithLecturesEnabled();
		repositoryEntryRelationDao.addRole(otherLectureManager, otherEntry, OrganisationRoles.lecturemanager.name());
		dbInstance.commitAndCloseSession();

		// first see something
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(lectureManager);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		
		// second not
		LecturesBlockSearchParameters otherSearchParams = new LecturesBlockSearchParameters();
		otherSearchParams.setManager(otherLectureManager);
		List<LectureBlock> otherBlocks = lectureBlockDao.searchLectureBlocks(otherSearchParams);
		Assert.assertNotNull(otherBlocks);
		Assert.assertTrue(otherBlocks.isEmpty());
	}
	
	@Test
	public void searchLectureBlocks_lectureManager_organisation() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-teacher-1");
		Identity lectureManager = JunitTestHelper.createAndPersistIdentityAsRndUser("lec-manager-org");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		organisationService.addMember(lectureManager, OrganisationRoles.lecturemanager);
		
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecture manager");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// first see something
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(lectureManager);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertFalse(blocks.isEmpty());
		Assert.assertTrue(blocks.contains(lectureBlock));
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
	public void searchLectureBlocks_asTeachers() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search all
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setViewAs(teacher, LectureRoles.teacher);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
	}
	
	@Test
	public void searchLectureBlocks_asTeachers_lectureDisabled() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search all
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setViewAs(teacher, LectureRoles.teacher);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertTrue(blocks.isEmpty());
	}
	
	@Test
	public void searchLectureBlocks_asTeachers_searchString() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setSearchString("lecturers");
		searchParams.setViewAs(teacher, LectureRoles.teacher);
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
		
		//search lectures with a string which is not available to this teacher
		LecturesBlockSearchParameters searchNegativeParams = new LecturesBlockSearchParameters();
		searchNegativeParams.setSearchString("goodbye");
		searchNegativeParams.setViewAs(teacher, LectureRoles.teacher);
		List<LectureBlock> negativeBlocks = lectureBlockDao.searchLectureBlocks(searchNegativeParams);
		Assert.assertNotNull(negativeBlocks);
		Assert.assertEquals(0, negativeBlocks.size());
	}
	
	@Test
	public void searchLectureBlocks_asTeachers_startEndDates() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchNowParams = new LecturesBlockSearchParameters();
		searchNowParams.setViewAs(teacher, LectureRoles.teacher);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		searchNowParams.setStartDate(now.getTime());
		now.add(Calendar.DATE, 2);
		searchNowParams.setEndDate(now.getTime());
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchNowParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
		
		//search in future
		LecturesBlockSearchParameters searchFutureParams = new LecturesBlockSearchParameters();
		searchFutureParams.setViewAs(teacher, LectureRoles.teacher);
		now.add(Calendar.DATE, 2);
		searchFutureParams.setStartDate(now.getTime());
		now.add(Calendar.DATE, 2);
		searchFutureParams.setEndDate(now.getTime());
		List<LectureBlock> futureBlocks = lectureBlockDao.searchLectureBlocks(searchFutureParams);
		Assert.assertNotNull(futureBlocks);
		Assert.assertEquals(0, futureBlocks.size());
	}
	
	@Test
	public void searchLectureBlocks_asTeachers_startDate() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchNowParams = new LecturesBlockSearchParameters();
		searchNowParams.setViewAs(teacher, LectureRoles.teacher);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		searchNowParams.setStartDate(now.getTime());
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchNowParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertEquals(lectureBlock, blocks.get(0));
		
		//search in future
		LecturesBlockSearchParameters searchFutureParams = new LecturesBlockSearchParameters();
		searchFutureParams.setViewAs(teacher, LectureRoles.teacher);
		now.add(Calendar.DATE, 2);
		searchFutureParams.setStartDate(now.getTime());
		List<LectureBlock> futureBlocks = lectureBlockDao.searchLectureBlocks(searchFutureParams);
		Assert.assertNotNull(futureBlocks);
		Assert.assertEquals(0, futureBlocks.size());
	}
	
	@Test
	public void searchLectureBlocks_asTeachers_endDate() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		//search lectures with the string
		LecturesBlockSearchParameters searchNowParams = new LecturesBlockSearchParameters();
		searchNowParams.setViewAs(teacher, LectureRoles.teacher);
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -1);
		searchNowParams.setEndDate(now.getTime());
		List<LectureBlock> blocks = lectureBlockDao.searchLectureBlocks(searchNowParams);
		Assert.assertNotNull(blocks);
		Assert.assertEquals(0, blocks.size());
		
		//search in future
		LecturesBlockSearchParameters searchFutureParams = new LecturesBlockSearchParameters();
		searchFutureParams.setViewAs(teacher, LectureRoles.teacher);
		now.add(Calendar.DATE, 2);
		searchFutureParams.setEndDate(now.getTime());
		List<LectureBlock> futureBlocks = lectureBlockDao.searchLectureBlocks(searchFutureParams);
		Assert.assertNotNull(futureBlocks);
		Assert.assertEquals(1, futureBlocks.size());
		Assert.assertEquals(lectureBlock, futureBlocks.get(0));
	}
	
	@Test
	public void searchAssessedLectureBlocks_asTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-23");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commitAndCloseSession();

		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// the lecture is not assessed -> return empty
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setViewAs(teacher, LectureRoles.teacher);
		List<LectureBlockRef> blockRefs = lectureBlockDao.searchAssessedLectureBlocks(searchParams);
		Assert.assertTrue(blockRefs.isEmpty());
		
		// add an assessment mode
		AssessmentMode assessmentMode = assessmentModeManager.createAssessmentMode(lectureBlock, 5, 5, "", null);
		assessmentMode = assessmentModeManager.persist(assessmentMode);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlockRef> assessedBlockRefs = lectureBlockDao.searchAssessedLectureBlocks(searchParams);
		Assert.assertEquals(1, assessedBlockRefs.size());
		Assert.assertEquals(lectureBlock.getKey(), assessedBlockRefs.get(0).getKey());
		Assert.assertEquals(lectureBlock, assessmentMode.getLectureBlock());
	}
	
	/**
	 * Check only the syntax by filling almost all parameters
	 */
	@Test
	public void searchAssessedLectureBlocks_allParameters() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("id-1");

		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(id);
		searchParams.setEndDate(new Date());
		searchParams.setParticipant(id);
		searchParams.setSearchString("Hello");
		searchParams.setStartDate(new Date());
		searchParams.setTeacher(id);
		
		List<LectureBlockRef> blocks = lectureBlockDao.searchAssessedLectureBlocks(searchParams);
		Assert.assertNotNull(blocks);
	}
	
	@Test
	public void searchRepositoryEntries_byTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-23");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		LectureRepositoryEntrySearchParameters searchParams = new LectureRepositoryEntrySearchParameters();
		searchParams.setTeacher(teacher);
		
		List<LectureRepositoryEntryInfos> infos = lectureBlockDao.searchRepositoryEntries(searchParams);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(entry, infos.get(0).getEntry());
	}
	
	@Test
	public void searchCurriculumElements_byTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-23");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-participant-23a");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(lectureBlock, teacher);
		
		String elementId = UUID.randomUUID().toString();
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-cur-1", "Curriculum with lectures 2", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		lectureService.save(lectureBlock, Collections.singletonList(element.getGroup()));
		dbInstance.commitAndCloseSession();

		LectureCurriculumElementSearchParameters searchParams = new LectureCurriculumElementSearchParameters();
		searchParams.setSearchString(elementId);
		searchParams.setViewAs(teacher, LectureRoles.teacher);
		
		List<LectureCurriculumElementInfos> infos = lectureBlockDao.searchCurriculumElements(searchParams);	
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(curriculum, infos.get(0).getCurriculum());
		Assert.assertEquals(element, infos.get(0).getElement());
		Assert.assertEquals(1, infos.get(0).getNumOfParticipants());
	}
	
	@Test
	public void searchCurriculumElements_byTeacher_negativeTest() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("not-teacher-23");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(teacher, entry, GroupRoles.owner.name());
		Assert.assertNotNull(lectureBlock);
		
		String elementId = UUID.randomUUID().toString();
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-cur-2", "Curriculum with lectures 2", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();

		LectureCurriculumElementSearchParameters searchParams = new LectureCurriculumElementSearchParameters();
		searchParams.setSearchString(elementId);
		searchParams.setViewAs(teacher, LectureRoles.teacher);
		
		List<LectureCurriculumElementInfos> infos = lectureBlockDao.searchCurriculumElements(searchParams);	
		Assert.assertNotNull(infos);
		Assert.assertTrue(infos.isEmpty());
	}
	
	@Test
	public void searchCurriculumElements_byMasterCoach() {
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("masterCoach-23");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-participant-23a");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-participant-23b");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		String elementId = UUID.randomUUID().toString();
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-cur-1", "Curriculum with lectures 2", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commit();
		curriculumService.addMember(element, masterCoach, CurriculumRoles.mastercoach);
		curriculumService.addMember(element, participant1, CurriculumRoles.participant);
		curriculumService.addMember(element, participant2, CurriculumRoles.participant);
		lectureService.save(lectureBlock, Collections.singletonList(element.getGroup()));
		dbInstance.commitAndCloseSession();

		LectureCurriculumElementSearchParameters searchParams = new LectureCurriculumElementSearchParameters();
		searchParams.setSearchString(elementId);
		searchParams.setViewAs(masterCoach, LectureRoles.mastercoach);
		
		List<LectureCurriculumElementInfos> infos = lectureBlockDao.searchCurriculumElements(searchParams);	
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(curriculum, infos.get(0).getCurriculum());
		Assert.assertEquals(element, infos.get(0).getElement());
		Assert.assertEquals(2, infos.get(0).getNumOfParticipants());
	}
	
	@Test
	public void searchCurriculumElements_byManager() {
		Identity lectureManager = JunitTestHelper.createAndPersistIdentityAsRndUser("lectureMgr-23");
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-participant-23a");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-participant-23b");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		String elementId = UUID.randomUUID().toString();
		Organisation curOrganisation = organisationService.createOrganisation("cur-lecture", "cur-lecture", null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-cur-1", "Curriculum with lectures 2", "Curriculum", false, curOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commit();
		organisationService.addMember(curOrganisation, lectureManager, OrganisationRoles.lecturemanager);
		curriculumService.addMember(element, participant1, CurriculumRoles.participant);
		curriculumService.addMember(element, participant2, CurriculumRoles.participant);
		lectureService.save(lectureBlock, Collections.singletonList(element.getGroup()));
		dbInstance.commitAndCloseSession();

		LectureCurriculumElementSearchParameters searchParams = new LectureCurriculumElementSearchParameters();
		searchParams.setSearchString(elementId);
		searchParams.setViewAs(lectureManager, LectureRoles.lecturemanager);
		
		List<LectureCurriculumElementInfos> infos = lectureBlockDao.searchCurriculumElements(searchParams);	
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(curriculum, infos.get(0).getCurriculum());
		Assert.assertEquals(element, infos.get(0).getElement());
		Assert.assertEquals(2, infos.get(0).getNumOfParticipants());
	}
	
	@Test
	public void getLecturesBlockWithTeachers() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-23");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		
		List<LectureBlockWithTeachers> infos = lectureBlockDao.getLecturesBlockWithTeachers(entry, null);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(lectureBlock, infos.get(0).getLectureBlock());
		Assert.assertFalse(infos.get(0).isAssessmentMode());
		// teachers
		Assert.assertNotNull(infos.get(0).getTeachers());
		Assert.assertEquals(1, infos.get(0).getTeachers().size());
		Assert.assertEquals(teacher, infos.get(0).getTeachers().get(0));
	}
	
	@Test
	public void getLecturesBlockWithTeachers_searchParams_asTeacher() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-23");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();
		
		// fill a lot of search parameters
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setTeacher(teacher);
		searchParams.setEntry(entry);
		searchParams.setStartDate(CalendarUtils.startOfDay(new Date()));
		searchParams.setEndDate(CalendarUtils.endOfDay(new Date()));
		searchParams.addLectureBlockStatus(LectureBlockStatus.values());
		searchParams.setSearchString(entry.getDisplayname());
		
		List<LectureBlockWithTeachers> infos = lectureBlockDao.getLecturesBlockWithTeachers(searchParams);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(lectureBlock, infos.get(0).getLectureBlock());
		Assert.assertFalse(infos.get(0).isAssessmentMode());
		// teachers
		Assert.assertNotNull(infos.get(0).getTeachers());
		Assert.assertEquals(1, infos.get(0).getTeachers().size());
		Assert.assertEquals(teacher, infos.get(0).getTeachers().get(0));
	}
	
	@Test
	public void getLecturesBlockWithTeachers_searchParams_asManager() {
		Identity admin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("admin-lecture-1");
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-24");
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-25");
		RepositoryEntry entry = createResourceWithLecturesEnabled();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(lectureBlock, teacher1);
		lectureService.addTeacher(lectureBlock, teacher2);
		dbInstance.commitAndCloseSession();
		
		// fill a lot of search parameters
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setManager(admin);
		searchParams.setEntry(entry);
		searchParams.setStartDate(CalendarUtils.startOfDay(new Date()));
		searchParams.setEndDate(CalendarUtils.endOfDay(new Date()));
		searchParams.addLectureBlockStatus(LectureBlockStatus.values());
		searchParams.setSearchString(entry.getDisplayname());
		
		List<LectureBlockWithTeachers> infos = lectureBlockDao.getLecturesBlockWithTeachers(searchParams);
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		Assert.assertEquals(lectureBlock, infos.get(0).getLectureBlock());
		Assert.assertFalse(infos.get(0).isAssessmentMode());
		// teachers
		Assert.assertNotNull(infos.get(0).getTeachers());
		Assert.assertEquals(2, infos.get(0).getTeachers().size());
		Assert.assertTrue(infos.get(0).getTeachers().contains(teacher1));
		Assert.assertTrue(infos.get(0).getTeachers().contains(teacher2));
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
	public void getTeachers_entry() {
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
	public void getTeachers_lectureBlock() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-7");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// get teachers
		List<Identity> teachers = lectureBlockDao.getTeachers(Collections.singletonList(lectureBlock));
		Assert.assertNotNull(teachers);
		Assert.assertEquals(1, teachers.size());
		Assert.assertTrue(teachers.contains(teacher));
	}
	
	@Test
	public void getTeachers_participantAndDates() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-7");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-7");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// get teachers
		List<Identity> teachers = lectureBlockDao.getTeachers(participant,
				Collections.singletonList(lectureBlock), Collections.singletonList(entry),
				CalendarUtils.startOfDay(new Date()), CalendarUtils.endOfDay(new Date()));
		Assert.assertNotNull(teachers);
		Assert.assertEquals(1, teachers.size());
		Assert.assertTrue(teachers.contains(teacher));
	}
	
	/**
	 * Check only the query syntax and not the return value.
	 */
	@Test
	public void getTeachers_params() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-7");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, defGroup);
		dbInstance.commit();
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commitAndCloseSession();

		// get teachers
		LecturesMemberSearchParameters searchParams = new LecturesMemberSearchParameters();
		searchParams.setManager(teacher);
		searchParams.setMasterCoach(teacher);
		searchParams.setTeacher(teacher);
		searchParams.setRepositoryEntry(entry);
		searchParams.setSearchString("Test");
		List<Identity> teachers = lectureBlockDao.getTeachers(searchParams);
		Assert.assertNotNull(teachers);
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
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "count relation 1", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);
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
		BusinessGroup group = businessGroupService.createBusinessGroup(null, "lectures 2", "tg", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);
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
	public void isMasterCoach() {
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("master-coach-3");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-mc-4");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		String elementId = UUID.randomUUID().toString();
		Organisation curOrganisation = organisationService.createOrganisation("cur-lecture-mc", "cur-lecture-mc", null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-cur-mc-3", "Curriculum with lectures 5", "Curriculum", false, curOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation with master coaches",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commit();
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, masterCoach, CurriculumRoles.mastercoach);
		lectureService.save(lectureBlock, Collections.singletonList(element.getGroup()));
		dbInstance.commitAndCloseSession();

		// Check master coach is what it is
		boolean isMasterCoach = lectureBlockDao.isMasterCoach(lectureBlock, masterCoach);
		Assert.assertTrue(isMasterCoach);
		// Participant is not master coach
		boolean isNotMasterCoach = lectureBlockDao.isMasterCoach(lectureBlock, participant);
		Assert.assertFalse(isNotMasterCoach);
	}
	
	@Test
	public void getMasterCoaches() {
		Identity masterCoach = JunitTestHelper.createAndPersistIdentityAsRndUser("master-coach-1");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-mc-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();
		
		String elementId = UUID.randomUUID().toString();
		Organisation curOrganisation = organisationService.createOrganisation("cur-lecture-mc", "cur-lecture-mc", null, null, null);
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-cur-mc-1", "Curriculum with lectures 4", "Curriculum", false, curOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation with master coaches",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commit();
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, masterCoach, CurriculumRoles.mastercoach);
		lectureService.save(lectureBlock, Collections.singletonList(element.getGroup()));
		dbInstance.commitAndCloseSession();

		// get teachers
		Date now = new Date();
		List<Identity> masterCoaches = lectureBlockDao.getMasterCoaches(participant,
				List.of(lectureBlock), List.of(entry), DateUtils.addDays(now, -5), DateUtils.addDays(now, 5));
		Assert.assertNotNull(masterCoaches);
		Assert.assertEquals(1, masterCoaches.size());
		Assert.assertTrue(masterCoaches.contains(masterCoach));
	}
	
	@Test
	public void getMasterCoachesNegativeTest() {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("not-master-coach-2");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-mc-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		lectureService.addTeacher(lectureBlock, teacher);
		dbInstance.commit();
		
		// get teachers
		Date now = new Date();
		List<Identity> masterCoaches = lectureBlockDao.getMasterCoaches(participant,
				List.of(lectureBlock), List.of(entry), DateUtils.addDays(now, -5), DateUtils.addDays(now, 5));
		Assert.assertNotNull(masterCoaches);
		Assert.assertTrue(masterCoaches.isEmpty());
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
