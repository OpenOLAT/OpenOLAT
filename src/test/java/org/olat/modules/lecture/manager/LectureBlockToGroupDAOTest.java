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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockToGroupDAOTest extends OlatTestCase {

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
	private BusinessGroupService businessGroupService;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	
	@Test
	public void getGroups() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Group defGroup = repositoryService.getDefaultGroup(entry);
		LectureBlock block = createMinimalLectureBlock(entry, List.of(defGroup));
		
		dbInstance.commitAndCloseSession();
		
		List<Group> groups = lectureBlockToGroupDao.getGroups(block);
		assertThat(groups)
			.isNotNull()
			.containsExactly(defGroup);
	}
	
	@Test
	public void getGroupsByType() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Group defGroup = repositoryService.getDefaultGroup(entry);
		
		String elementId = UUID.randomUUID().toString();
		Curriculum curriculum = curriculumService.createCurriculum("Lecture group 1", "Curriculum with lecture 2", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement(elementId, "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addRepositoryEntry(element, entry, false);
		
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "Lecture business group", "LG", BusinessGroup.BUSINESS_TYPE,
				null, null, false, false, entry);

		LectureBlock block = createMinimalLectureBlock(entry, List.of(defGroup, element.getGroup(), businessGroup.getBaseGroup()));
		dbInstance.commitAndCloseSession();
		
		// retrieve default group
		List<Group> defGroups = lectureBlockToGroupDao.getGroups(block, RepositoryEntryRelationType.defaultGroup);
		assertThat(defGroups)
			.isNotNull()
			.containsExactly(defGroup);
		
		// retrieve curriculum related groups
		List<Group> curriculumGroups = lectureBlockToGroupDao.getGroups(block, RepositoryEntryRelationType.curriculums);
		assertThat(curriculumGroups)
			.isNotNull()
			.containsExactly(element.getGroup());
		
		// retrieve business group related groups
		List<Group> businessGroups = lectureBlockToGroupDao.getGroups(block, RepositoryEntryRelationType.businessGroups);
		assertThat(businessGroups)
			.isNotNull()
			.containsExactly(businessGroup.getBaseGroup());
		
		// retrieve entry & curriculums
		List<Group> entryAndcurriculumsGroups = lectureBlockToGroupDao.getGroups(block, RepositoryEntryRelationType.entryAndCurriculums);
		assertThat(entryAndcurriculumsGroups)
			.isNotNull()
			.containsExactlyInAnyOrder(defGroup, element.getGroup());
		
		// retrieve entry & curriculums
		List<Group> allGroups = lectureBlockToGroupDao.getGroups(block, RepositoryEntryRelationType.all);
		assertThat(allGroups)
			.isNotNull()
			.containsExactlyInAnyOrder(defGroup, element.getGroup(), businessGroup.getBaseGroup());
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry, List<Group> groups) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -15);
		lectureBlock.setStartDate(cal.getTime());
		cal.add(Calendar.MINUTE, 30);
		lectureBlock.setEndDate(cal.getTime());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, groups);
	}

}
