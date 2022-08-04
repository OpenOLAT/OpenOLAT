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
package org.olat.modules.curriculum.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.manager.RepositoryEntryLectureConfigurationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumRepositoryEntryRelationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumRepositoryEntryRelationDAO curriculumRepositoryEntryRelationDao;
	@Autowired
	private RepositoryEntryLectureConfigurationDAO repositoryEntryLectureConfigurationDao;
	
	@Test
	public void getRepositoryEntries() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		List<RepositoryEntry> entries = curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, elements, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entry, entries.get(0));
	}
	
	@Test
	public void getRepositoryEntries_withLectures() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entryLecture = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryLectureConfiguration lectureConfig = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture);
		lectureConfig.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entryLecture, false);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		List<RepositoryEntry> entries = curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, elements, RepositoryEntryStatusEnum.preparationToClosed(), true, null, null);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entryLecture, entries.get(0));
	}
	
	@Test
	public void getRepositoryEntries_withPermissions() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for checked relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel",
				"Element for checked relation", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-not-user");
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entryLecture = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryLectureConfiguration lectureConfig = repositoryEntryLectureConfigurationDao.createConfiguration(entryLecture);
		lectureConfig.setLectureEnabled(true);
		repositoryEntryLectureConfigurationDao.update(lectureConfig);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entryLecture, false);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		// check author
		List<String> roles = Arrays.asList(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name());
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		List<RepositoryEntry> entries = curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, elements, RepositoryEntryStatusEnum.preparationToClosed(), true, author, roles);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entryLecture, entries.get(0));
		
		// check the second user without permission
		List<RepositoryEntry> noEntries = curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, elements, RepositoryEntryStatusEnum.preparationToClosed(), true, user, roles);
		Assert.assertNotNull(noEntries);
		Assert.assertTrue(noEntries.isEmpty());
	}
	
	@Test
	public void getCurriculumElements() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElement> elements = curriculumRepositoryEntryRelationDao.getCurriculumElements(entry);
		Assert.assertNotNull(elements);
		Assert.assertEquals(1, elements.size());
		Assert.assertEquals(element, elements.get(0));
	}
	
	@Test
	public void getCurriculumElementsRepositoryEntryAnsUser() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element, participant, CurriculumRoles.participant);
		curriculumService.addMember(element, participant, CurriculumRoles.coach);
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-re-part2");
		curriculumService.addMember(element, participant2, CurriculumRoles.participant);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumRoles> roles = Arrays.asList(CurriculumRoles.participant);
		List<CurriculumElement> elements = curriculumRepositoryEntryRelationDao.getCurriculumElements(entry, participant, roles);
		Assert.assertNotNull(elements);
		Assert.assertEquals(1, elements.size());
		Assert.assertEquals(element, elements.get(0));
	}
}
