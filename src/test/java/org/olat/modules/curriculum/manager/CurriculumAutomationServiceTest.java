/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.manager;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.AutomationImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumAutomationServiceTest extends OlatTestCase {
	

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumAutomationServiceImpl automationService;
	
	@Test
	public void loadElementsInstatiate() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-1", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoInstantiation(AutomationImpl.valueOf(5, AutomationUnit.WEEKS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-1-1", "1.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-1-1-1", "1.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry template = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryTemplate(element11, template);
		curriculumService.addRepositoryTemplate(element1, template);
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element1, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());

		// Negative test
		CurriculumElement notAutomatedImplementation = curriculumElementDao.createCurriculumElement("Element-1b", "1b. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement  notAutomatedElement = curriculumElementDao.createCurriculumElement("Element-1b-1", "1b.1 Element",
				CurriculumElementStatus.active, null, null, notAutomatedImplementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		List<CurriculumElement> elements = automationService.loadElementsInstantiate();
		Assertions.assertThat(elements)
			.contains(element11)
			.doesNotContain(implementation, element1, notAutomatedImplementation, notAutomatedElement);
	}
	
	@Test
	public void instatiate() {
		Date beginDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -2);
		Date endDate = DateUtils.addDays(beginDate, 10);
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-2", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-2", "12 Element",
				CurriculumElementStatus.confirmed, beginDate, endDate, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoInstantiation(AutomationImpl.valueOf(5, AutomationUnit.DAYS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 1 child
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-2-1", "2.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry template = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryTemplate(element1, template);
		dbInstance.commit();
		
		// Run the instantiation process
		automationService.instantiate();
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element1);
		Assertions.assertThat(courses)
			.hasSize(1);
		
		RepositoryEntry course = courses.get(0);
		Assert.assertNotNull(course);
		Assert.assertTrue(course.getExternalRef().contains("Element-2-1"));
		Assert.assertEquals("2.1 Element", course.getDisplayname());
	}
	
	@Test
	public void loadElementsToAccessForCoach() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-3", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoAccessForCoach(AutomationImpl.valueOf(5, AutomationUnit.WEEKS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-3-1", "3.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-3-1-1", "3.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		List<CurriculumElement> elements = automationService.loadElementsToAccessForCoach();
		Assertions.assertThat(elements)
			.contains(element11)
			.doesNotContain(implementation, element1);
	}
	
	@Test
	public void changeAccessForCoach() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-4", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoAccessForCoach(AutomationImpl.valueOf(2, AutomationUnit.WEEKS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		Date beginDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -10);
		Date endDate = DateUtils.addDays(beginDate, 30);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-4-1", "4.1 Element",
				CurriculumElementStatus.active, beginDate, endDate, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-4-1-1", "4.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		// Try to change courses status to published for coach
		automationService.accessForCoach();
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element11);
		Assertions.assertThat(courses)
			.hasSize(1);
		RepositoryEntry element11Course = courses.get(0);
		Assert.assertEquals(RepositoryEntryStatusEnum.coachpublished, element11Course.getEntryStatus());
	}
	
	@Test
	public void loadElementsToPublish() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-5", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoPublished(AutomationImpl.valueOf(5, AutomationUnit.WEEKS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-5-1", "5.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-5-1-1", "5.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		List<CurriculumElement> elements = automationService.loadElementsToPublish();
		Assertions.assertThat(elements)
			.contains(element11)
			.doesNotContain(implementation, element1);
	}
	
	@Test
	public void publish() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-6", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-6", "6. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoPublished(AutomationImpl.valueOf(0, AutomationUnit.SAME_DAY));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-6-1", "6.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		Date beginDate = DateUtils.getStartOfDay(new Date());
		Date endDate = DateUtils.addDays(beginDate, 3);
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-6-1-1", "6.1.1 Element",
				CurriculumElementStatus.active, beginDate, endDate, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		// Try to change courses status to published for coach
		automationService.publish();
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element11);
		Assertions.assertThat(courses)
			.hasSize(1);
		RepositoryEntry element11Course = courses.get(0);
		Assert.assertEquals(RepositoryEntryStatusEnum.published, element11Course.getEntryStatus());
	}
	
	@Test
	public void publishNotReady() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-7", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-7", "7. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoPublished(AutomationImpl.valueOf(5, AutomationUnit.DAYS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-7-1", "1.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		Date beginDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), 20);
		Date endDate = DateUtils.addDays(beginDate, 3);
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element",
				CurriculumElementStatus.active, beginDate, endDate, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		// Try to change courses status to published for coach
		automationService.publish();
		
		// Still in preparation
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element11);
		Assertions.assertThat(courses)
			.hasSize(1);
		RepositoryEntry element11Course = courses.get(0);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, element11Course.getEntryStatus());
	}

	@Test
	public void loadElementsToClose() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-8", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-8", "8. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoClosed(AutomationImpl.valueOf(5, AutomationUnit.WEEKS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-8-1", "8.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-8-1-1", "8.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		List<CurriculumElement> elements = automationService.loadElementsToClose();
		Assertions.assertThat(elements)
			.contains(element11)
			.doesNotContain(implementation, element1);
	}
	
	@Test
	public void close() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-9", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-9", "9. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoClosed(AutomationImpl.valueOf(5, AutomationUnit.DAYS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-9-1", "9.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		Date beginDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -30);
		Date endDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -10);
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-9-1-1", "9.1.1 Element",
				CurriculumElementStatus.active, beginDate, endDate, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		// Try to change courses status to published for coach
		automationService.close();
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element11);
		Assertions.assertThat(courses)
			.hasSize(1);
		RepositoryEntry element11Course = courses.get(0);
		Assert.assertEquals(RepositoryEntryStatusEnum.closed, element11Course.getEntryStatus());
	}

	@Test
	public void closeNotReady() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-10", "Curriculum for automation", "Curriculum", false, null);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-10", "10. Element",
				CurriculumElementStatus.confirmed, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoClosed(AutomationImpl.valueOf(1, AutomationUnit.MONTHS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-10-1", "10.1 Element",
				CurriculumElementStatus.active, null, null, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		Date beginDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -30);
		Date endDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), 20);
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-10-1-1", "10.1.1 Element",
				CurriculumElementStatus.active, beginDate, endDate, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(JunitTestHelper.getDefaultActor());
		curriculumService.addRepositoryEntry(element11, course, false);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, course.getEntryStatus());
		
		// Try to change courses status to published for coach
		automationService.close();
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element11);
		Assertions.assertThat(courses)
			.hasSize(1);
		RepositoryEntry element11Course = courses.get(0);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation, element11Course.getEntryStatus());
	}
	
	@Test
	public void getBeginDate() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-automation-11", "Curriculum for automation", "Curriculum", false, null);
		Date beginDateImpl = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -40);
		Date endDateImpl = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), 60);
		CurriculumElement implementation = curriculumElementDao.createCurriculumElement("Element-11", "11. Element",
				CurriculumElementStatus.confirmed, beginDateImpl, endDateImpl, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		implementation.setAutoInstantiation(AutomationImpl.valueOf(5, AutomationUnit.WEEKS));
		implementation = curriculumElementDao.update(implementation);
		dbInstance.commit();
		// save 2 children
		Date beginDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), -30);
		Date endDate = DateUtils.addDays(DateUtils.getStartOfDay(new Date()), 20);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-11-1", "11.1 Element",
				CurriculumElementStatus.active, beginDate, endDate, implementation, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-11-1-1", "11.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		Date element1BeginDate = automationService.getBeginDate(element11);
		Assert.assertTrue(DateUtils.isSameDay(beginDate, element1BeginDate));
	}
}
