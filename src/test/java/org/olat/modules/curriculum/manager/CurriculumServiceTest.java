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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumServiceTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(OlatTestCase.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private LectureService lectureService;
	
	@Test
	public void addCurriculumManagers() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-1", "Curriculum 1", "Short desc.", false, null);
		dbInstance.commitAndCloseSession();
		
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		// check if we can retrieve the managers
		List<Identity> managers = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.curriculummanager);
		Assert.assertNotNull(managers);
		Assert.assertEquals(1, managers.size());
		Assert.assertEquals(manager, managers.get(0));
		
		// check that there is not an other member with an other role
		List<Identity> owners = curriculumService.getMembersIdentity(curriculum, CurriculumRoles.owner);
		Assert.assertTrue(owners.isEmpty());
	}
	
	@Test
	public void addToCurriculumElementWithMoveLectureBlocks() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		
		Curriculum curriculum = curriculumService.createCurriculum("CUR-8", "Curriculum 8", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		LectureBlock lectureBlock = lectureService.createLectureBlock(element, null);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello curriculum 8");
		lectureBlock.setPlannedLecturesNumber(4);
		lectureBlock = lectureService.save(lectureBlock, null);
		
		RepositoryEntry courseEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();

		// Add the course to the curriculum
		curriculumService.addRepositoryEntry(element, courseEntry, true);
		dbInstance.commitAndCloseSession();
		
		//Check the transfer of block
		
		List<LectureBlock> courseLectureBlocks = lectureService.getLectureBlocks(courseEntry);
		Assertions
			.assertThat(courseLectureBlocks)
			.isNotNull()
			.hasSize(1)
			.containsExactly(lectureBlock);
	}
	
	@Test
	public void getCurriculumElements() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-2", "Curriculum 2", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry publishedEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry reviewedEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		
		publishedEntry = repositoryManager.setRuntimeType(publishedEntry, RepositoryEntryRuntimeType.standalone);
		publishedEntry = repositoryManager.setStatus(publishedEntry, RepositoryEntryStatusEnum.published);
		reviewedEntry = repositoryManager.setRuntimeType(reviewedEntry, RepositoryEntryRuntimeType.standalone);
		reviewedEntry = repositoryManager.setStatus(reviewedEntry, RepositoryEntryStatusEnum.review);
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element, publishedEntry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element, participant, CurriculumRoles.participant, author);
		dbInstance.commitAndCloseSession();

		List<CurriculumRef> curriculumList = Collections.singletonList(curriculum);
		List<CurriculumElementRepositoryEntryViews> myElements = curriculumService
				.getCurriculumElements(participant, Roles.userRoles(), curriculumList, CurriculumElementStatus.visibleUser());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(1, myElements.size());
		
		CurriculumElementRepositoryEntryViews myElement = myElements.get(0);
		Assert.assertEquals(element, myElement.getCurriculumElement());
		Assert.assertEquals(1, myElement.getEntries().size());
		Assert.assertEquals(publishedEntry.getKey(), myElement.getEntries().get(0).getKey());
		
		CurriculumElementMembership membership = myElement.getCurriculumMembership();
		Assert.assertTrue(membership.isParticipant());
		Assert.assertFalse(membership.isCoach());
		Assert.assertFalse(membership.isRepositoryEntryOwner());
		Assert.assertFalse(membership.isCurriculumElementOwner());
	}
	
	@Test
	public void getCurriculumElementsMore() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-2b", "Curriculum 2b", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry publishedEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry reviewedEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		
		publishedEntry = repositoryManager.setStatus(publishedEntry, RepositoryEntryStatusEnum.published);
		reviewedEntry = repositoryManager.setStatus(reviewedEntry, RepositoryEntryStatusEnum.review);
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element1, publishedEntry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element1, participant, CurriculumRoles.participant, author);
		dbInstance.commitAndCloseSession();

		List<CurriculumRef> curriculumList = Collections.singletonList(curriculum);
		List<CurriculumElementRepositoryEntryViews> myElements = curriculumService
				.getCurriculumElements(participant, Roles.userRoles(), curriculumList, CurriculumElementStatus.visibleUser());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(2, myElements.size());
		
		CurriculumElement firstElement = myElements.get(0).getCurriculumElement();
		CurriculumElement secondElement = myElements.get(0).getCurriculumElement();
		Assert.assertTrue(firstElement.equals(element1) || firstElement.equals(rootElement));
		Assert.assertTrue(secondElement.equals(element1) || secondElement.equals(rootElement));
		// Element 2 ist not in the output
		Assert.assertFalse(firstElement.equals(element2) || secondElement.equals(element2));
	}
	
	@Test
	public void deleteSoftlyCurriculum() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-3", "Curriculum 3", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-del", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element2, entry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element1, participant, CurriculumRoles.participant, author);
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> myElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.values());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(2, myElements.size());
		
		curriculumService.deleteSoftlyCurriculum(curriculum, null, false);
		dbInstance.commitAndCloseSession();
		
		// check
		Curriculum deletedCurriculum = curriculumService.getCurriculum(curriculum);
		Assert.assertNotNull(deletedCurriculum);
		Assert.assertEquals(CurriculumStatus.deleted.name(), deletedCurriculum.getStatus());
	}
	
	@Test
	public void deleteCurriculumInQuality() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Curriculum curriculum = curriculumService.createCurriculum("CUR-3", "Curriculum 3", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-del", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		RepositoryEntry entry = qualityTestHelper.createFormEntry();
		Organisation organisation = organisationService.getDefaultOrganisation();
		QualityDataCollection dataCollection = qualityService.createDataCollection(List.of(organisation), entry);
		dataCollection.setTopicCurriculumElement(element1);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element2, entry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element1, participant, CurriculumRoles.participant, actor);
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> myElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.values());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(2, myElements.size());
		
		curriculumService.deleteSoftlyCurriculum(curriculum, actor, true);
		dbInstance.commitAndCloseSession();
		
		// check
		Curriculum deletedCurriculum = curriculumService.getCurriculum(curriculum);
		Assert.assertNotNull(deletedCurriculum);
		Assert.assertEquals(CurriculumStatus.deleted.name(), deletedCurriculum.getStatus());
	}
	
	
	@Test
	public void deleteCurriculumInQualityWithSubElement() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-3", "Curriculum 3", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel-10", "Element for nothing",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-del-11", "Element for nothing",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2under = curriculumService.createCurriculumElement("Element-for-del-under-11-1", "Element under for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Identity actor = JunitTestHelper.getDefaultActor();
		RepositoryEntry entry = qualityTestHelper.createFormEntry();
		Organisation organisation = organisationService.getDefaultOrganisation();
		QualityDataCollection dataCollection = qualityService.createDataCollection(Collections.singletonList(organisation), entry);
		dataCollection.setTopicCurriculumElement(element2under);
		qualityService.updateDataCollection(dataCollection);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element2, entry, false);
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-part");
		curriculumService.addMember(element1, participant, CurriculumRoles.participant, actor);
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> myElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.values());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(3, myElements.size());
		
		curriculumService.deleteSoftlyCurriculum(curriculum, actor, false);
		dbInstance.commitAndCloseSession();
		
		// check
		Curriculum deletedCurriculum = curriculumService.getCurriculum(curriculum);
		Assert.assertNotNull(deletedCurriculum);
		Assert.assertEquals(CurriculumStatus.deleted.name(), deletedCurriculum.getStatus());
	}
	
	@Test
	public void deleteSoftlyCurriculumElementAndCloseEntry() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-10", "Curriculum 10", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		Organisation defOrganisation = JunitTestHelper.getDefaultOrganisation();
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, RepositoryEntryRuntimeType.curricular, defOrganisation);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> myElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.values());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(1, myElements.size());
		
		curriculumService.deleteSoftlyCurriculumElement(element, null, false);
		dbInstance.commitAndCloseSession();
		
		// Check curriculum element status
		CurriculumElement deletedElement = curriculumService.getCurriculumElement(element);
		Assert.assertNotNull(deletedElement);
		Assert.assertEquals(CurriculumElementStatus.deleted, deletedElement.getElementStatus());
		
		// Check repository entry status
		entry = repositoryService.loadBy(entry);
		Assert.assertEquals(RepositoryEntryStatusEnum.closed.name(), entry.getStatus());
	}
	
	/**
	 * Course with are hold by 2 elements aren't closed by deleting one of them.
	 */
	@Test
	public void deleteSoftlyCurriculumElementAndDontCloseEntry() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-11", "Curriculum 11", "Curriculum", false, null);
		
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		Organisation defOrganisation = JunitTestHelper.getDefaultOrganisation();
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, RepositoryEntryRuntimeType.curricular, defOrganisation);
		dbInstance.commit();

		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element1, entry, false);
		curriculumService.addRepositoryEntry(element2, entry, false);
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> myElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.values());
		Assertions.assertThat(myElements)
			.hasSize(2)
			.containsExactlyInAnyOrder(element1, element2);
		
		curriculumService.deleteSoftlyCurriculumElement(element1, null, false);
		dbInstance.commitAndCloseSession();
		
		// Check curriculum element status
		CurriculumElement deletedElement = curriculumService.getCurriculumElement(element1);
		Assert.assertNotNull(deletedElement);
		Assert.assertEquals(CurriculumElementStatus.deleted, deletedElement.getElementStatus());
		CurriculumElement otherElement = curriculumService.getCurriculumElement(element2);
		Assert.assertNotNull(otherElement);
		Assert.assertEquals(CurriculumElementStatus.active, otherElement.getElementStatus());
		
		// Check repository entry status
		entry = repositoryService.loadBy(entry);
		Assert.assertEquals(RepositoryEntryStatusEnum.preparation.name(), entry.getStatus());
	}
	
	@Test
	public void numberRootCurriculumElement() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-4", "Curriculum 4", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-num-1", "Element to number",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-num 1.1", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-to-num 1.2", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element21 = curriculumService.createCurriculumElement("Element-to-num 1.2.1", "Element to number",
				CurriculumElementStatus.active, null, null, element2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		dbInstance.commit();

		// Number this implementation tree
		curriculumService.numberRootCurriculumElement(element);
		dbInstance.commitAndCloseSession();
		
		element = curriculumService.getCurriculumElement(element);
		Assert.assertNull(element.getNumberImpl());
		
		element1 = curriculumService.getCurriculumElement(element1);
		Assert.assertEquals("1", element1.getNumberImpl());
		element2 = curriculumService.getCurriculumElement(element2);
		Assert.assertEquals("2", element2.getNumberImpl());
		element21 = curriculumService.getCurriculumElement(element21);
		Assert.assertEquals("2.1", element21.getNumberImpl());
	}
	
	
	@Test
	public void numberRootCurriculumElementWithDeletion() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-5", "Curriculum 5", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-num-1", "Element to number",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-num 1.1", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-to-num 1.2", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3 = curriculumService.createCurriculumElement("Element-to-num 1.3", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		dbInstance.commit();

		// Number this implementation tree
		curriculumService.numberRootCurriculumElement(element);
		dbInstance.commitAndCloseSession();
		
		element = curriculumService.getCurriculumElement(element);
		Assert.assertNull(element.getNumberImpl());
		
		element1 = curriculumService.getCurriculumElement(element1);
		Assert.assertEquals("1", element1.getNumberImpl());
		element2 = curriculumService.getCurriculumElement(element2);
		Assert.assertEquals("2", element2.getNumberImpl());
		element3 = curriculumService.getCurriculumElement(element3);
		Assert.assertEquals("3", element3.getNumberImpl());
		
		curriculumService.deleteSoftlyCurriculumElement(element1, null, false);
		dbInstance.commitAndCloseSession();
		
		element = curriculumService.getCurriculumElement(element);
		
		CurriculumElement element4 = curriculumService.createCurriculumElement("Element-to-num 1.4", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// Number this implementation tree
		element = curriculumService.getImplementationOf(element2);
		curriculumService.numberRootCurriculumElement(element);
		dbInstance.commitAndCloseSession();
		
		element1 = curriculumService.getCurriculumElement(element1);
		Assert.assertNotNull(element1);
		Assert.assertEquals(CurriculumElementStatus.deleted, element1.getElementStatus());
		element2 = curriculumService.getCurriculumElement(element2);
		element3 = curriculumService.getCurriculumElement(element3);
		element4 = curriculumService.getCurriculumElement(element4);
		
		log.info("Sequence pos: {} - {} - {}", element2.getPos(), element3.getPos(), element4.getPos());
		log.info("Numbering: {} - {} - {}", element2.getNumberImpl(), element3.getNumberImpl(), element4.getNumberImpl());
		
		Assert.assertEquals("1", element2.getNumberImpl());
		Assert.assertEquals("2", element3.getNumberImpl());
		Assert.assertEquals("3", element4.getNumberImpl());
	}
	
	@Test
	public void getImplementationOfRoot() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-5", "Curriculum 5", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-num-1", "Element to implement",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement rootElement = curriculumService.getImplementationOf(element);
		Assert.assertEquals(element, rootElement);
	}
	
	@Test
	public void getImplementationOfElement() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-6", "Curriculum 6", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-num-6", "Element to implement",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-num 6.1", "Element to number",
				CurriculumElementStatus.active, null, null, element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element11 = curriculumService.createCurriculumElement("Element-to-num 6.1.1", "Element to number",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement rootElement = curriculumService.getImplementationOf(element11);
		Assert.assertEquals(element, rootElement);
	}
}
