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
import org.olat.core.util.DateUtils;
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
import org.olat.modules.curriculum.CurriculumService.AddRepositoryEntry;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyElementSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyOfferSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.provider.invoice.model.InvoiceAccessMethod;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SmtpMessage;

/**
 * 
 * Initial date: 18 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumServiceTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(OlatTestCase.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private TaxonomyService taxonomyService;
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
		Assertions.assertThat(managers)
			.hasSize(1)
			.containsExactly(manager);
		
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
		// Element 2 is not in the output
		Assert.assertFalse(firstElement.equals(element2) || secondElement.equals(element2));
	}
	
	@Test
	public void copyCurriculumElement() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-20", "Curriculum 20", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element11 = curriculumService.createCurriculumElement("Element-to-copy-1-1", "Element to copy 1.1",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element111 = curriculumService.createCurriculumElement("Element-to-copy-1-1-1", "Element to copy 1.1.1",
				CurriculumElementStatus.active, null, null, element11, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		Assert.assertNotNull(element111);
		
		Date begin = DateUtils.getStartOfDay(new Date());
		Date end = DateUtils.getEndOfDay(new Date());
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setCopyElementSettings(List.of(new CopyElementSetting(element1, null, null, begin, end)));
		
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element1, copySettings, actor);
		dbInstance.commit();
		
		Assert.assertEquals(begin, copiedElement.getBeginDate());
		Assert.assertEquals(end, copiedElement.getEndDate());
		Assert.assertEquals("Element to copy 1 (Copy)", copiedElement.getDisplayName());
		
		List<CurriculumElement> copiedDescendantsElements = curriculumService.getCurriculumElementsDescendants(copiedElement);
		Assertions.assertThat(copiedDescendantsElements)
			.hasSize(2)
			.map(CurriculumElement::getDisplayName)
			.containsAnyOf("Element to copy 1.1.1 (Copy)");
		
		List<CurriculumElement> implementations = curriculumService.getImplementations(curriculum);
		Assertions.assertThat(implementations)
			.hasSize(2)
			.containsExactlyInAnyOrder(element1, copiedElement);
	}
	
	@Test
	public void copyCurriculumElementAndReuseCourse() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-2");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-21", "Curriculum 21", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(actor);
		AddRepositoryEntry addEntry = curriculumService.addRepositoryEntry(element1, entry, false);
		dbInstance.commit();
		Assert.assertTrue(addEntry.entryAdded());
		
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setCopyResources(CopyResources.relation);
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element1, copySettings, actor);
		dbInstance.commit();
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(copiedElement);
		Assertions.assertThat(courses)
			.hasSize(1)
			.containsExactly(entry);
	}
	
	@Test
	public void copyCurriculumElementAndTemplate() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-2");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-21", "Curriculum 21", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry template = JunitTestHelper.createRandomRepositoryEntry(actor);
		boolean addEntry = curriculumService.addRepositoryTemplate(element1, template);
		dbInstance.commit();
		Assert.assertTrue(addEntry);
		
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setCopyResources(CopyResources.relation);
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element1, copySettings, actor);
		dbInstance.commit();
		
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(copiedElement);
		Assertions.assertThat(templates)
			.hasSize(1)
			.containsExactly(template);
	}
	
	@Test
	public void copyCurriculumElementWithOffer() {
		// Create curriculum
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-3");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-22", "Curriculum 22", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		// Add an offer and an access to it
		Offer offer = acService.createOffer(element.getResource(), "Invoice curriculum element");
		offer.setConfirmationByManagerRequired(true);
		offer.setLabel("Offer to copy");
		offer.setConfirmationEmail(false);
		offer.setConfirmationByManagerRequired(true);
		offer = acService.save(offer);
		
		List<AccessMethod> methods = acService.getAvailableMethodsByType(InvoiceAccessMethod.class);
		OfferAccess offerAccess = acService.createOfferAccess(offer, methods.get(0));
		offerAccess = acService.saveOfferAccess(offerAccess);
		dbInstance.commitAndCloseSession();
		
		// Copy the element
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setCopyOffers(true);
		copySettings.setCopyOfferSettings(List.of(new CopyOfferSetting(offer, new Date(), new Date())));
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element, copySettings, actor);
		dbInstance.commit();

		List<OfferAndAccessInfos> offerAndAccessList = acService.findOfferAndAccessByResource(copiedElement.getResource(), true);
		Assertions.assertThat(offerAndAccessList)
			.hasSize(1);
		
		OfferAndAccessInfos infosOfCopy = offerAndAccessList.get(0);
		Assert.assertNotNull(infosOfCopy.offer());
		Assert.assertNotNull(infosOfCopy.offerAccess());
		
		Assert.assertEquals("Element to copy 1 (Copy)", infosOfCopy.offer().getResourceDisplayName());
		Assert.assertEquals("Offer to copy", infosOfCopy.offer().getLabel());
		Assert.assertFalse(infosOfCopy.offer().isConfirmationEmail());
		Assert.assertTrue(infosOfCopy.offer().isConfirmationByManagerRequired());
		Assert.assertNotNull(infosOfCopy.offer().getValidFrom());
		Assert.assertNotNull(infosOfCopy.offer().getValidTo());
		Assert.assertNotNull(infosOfCopy.offerAccess().getValidFrom());
		Assert.assertNotNull(infosOfCopy.offerAccess().getValidTo());
		
		Assert.assertNotEquals(offer, infosOfCopy.offer());
		Assert.assertNotEquals(offerAccess, infosOfCopy.offerAccess());
	}
	
	@Test
	public void copyCurriculumElementAndLectureBlock() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-25");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-25-participant");
		
		Curriculum curriculum = curriculumService.createCurriculum("CUR-25", "Curriculum 25", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("ORIGINAL-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		LectureBlock lectureBlock = lectureService.createLectureBlock(element, null);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setExternalId("ORIGINAL-ID");
		lectureBlock.setExternalRef("ORIGINAL-1-EV-1");
		lectureBlock.setTitle("Hello curriculum 25");
		lectureBlock = lectureService.save(lectureBlock, null);
		dbInstance.commit();
		
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setBaseIdentifier(element.getIdentifier());
		copySettings.setIdentifier("COPY-1");
		copySettings.setShiftDateByDays(2);
		copySettings.setCopyStandaloneEvents(true);
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element, copySettings, actor);
		dbInstance.commit();
		
		// Add a participant to the copied element
		curriculumService.addMember(copiedElement, participant, CurriculumRoles.participant, actor);
		dbInstance.commit();
		
		List<LectureBlock> copiedLectureBlocks = lectureService.getLectureBlocks(copiedElement, false);
		Assertions.assertThat(copiedLectureBlocks)
			.hasSize(1);

		LectureBlock copiedLectureBlock = copiedLectureBlocks.get(0);
		Assert.assertNotNull(copiedLectureBlock.getStartDate());
		Assert.assertNotNull(copiedLectureBlock.getEndDate());
		Assert.assertEquals("Hello curriculum 25", copiedLectureBlock.getTitle());
		Assert.assertEquals("COPY-1-EV-1", copiedLectureBlock.getExternalRef());
		Assert.assertNull(copiedLectureBlock.getExternalId());
		
		// Check participant
		LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
		searchParams.setParticipant(participant);
		searchParams.setLectureConfiguredRepositoryEntry(false);
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(searchParams, 0, Boolean.FALSE);
		Assertions.assertThat(lectureBlocks)
			.hasSize(1)
			.containsExactly(copiedLectureBlock);
	}
	
	/**
	 * If the curriculum element has course and template, only links the
	 * template and copy the lecture blocks back to the curriculum element.
	 */
	@Test
	public void copyCurriculumElementCourseTemplateAndLectureBlock() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-26");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-26", "Curriculum 26", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry template = JunitTestHelper.createRandomRepositoryEntry(actor);
		boolean addTemplate = curriculumService.addRepositoryTemplate(element, template);
		dbInstance.commit();
		Assert.assertTrue(addTemplate);
		
		RepositoryEntry course = JunitTestHelper.createRandomRepositoryEntry(actor);
		AddRepositoryEntry addCourse = curriculumService.addRepositoryEntry(element, course, false);
		dbInstance.commit();
		Assert.assertTrue(addCourse.entryAdded());
		
		LectureBlock lectureBlock = lectureService.createLectureBlock(element, course);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setExternalId("ELEM-COURSE-ID");
		lectureBlock.setExternalRef("LEM-COURSE-1-EV-1");
		lectureBlock.setTitle("Hello curriculum 26");
		lectureBlock = lectureService.save(lectureBlock, null);
		dbInstance.commit();
		Assert.assertNotNull(lectureBlock);
		
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setCopyResources(CopyResources.resource);
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element, copySettings, actor);
		dbInstance.commit();
		
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(copiedElement);
		Assertions.assertThat(templates)
			.hasSize(1)
			.containsExactly(template);
		
		List<LectureBlock> copiedLectureBlocks = lectureService.getLectureBlocks(copiedElement, false);
		Assertions.assertThat(copiedLectureBlocks)
			.hasSize(1);
	}
	
	@Test
	public void copyCurriculumElementWithTaxonomy() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-1");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-28", "Curriculum 28", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		Taxonomy taxonomy = taxonomyService.createTaxonomy("CURRICULUM-" + curriculum.getKey(), "Curriculum 28", null, null);
		TaxonomyLevel taxonomyLevel = taxonomyService.createTaxonomyLevel("A-CURRICULUM-LEVEL", "A curriculum level", null, null, null, taxonomy);
		curriculumService.updateTaxonomyLevels(element1, List.of(taxonomyLevel), List.of());
		dbInstance.commit();
		
		CurriculumCopySettings copySettings = new CurriculumCopySettings();
		copySettings.setCopyTaxonomy(true);
		CurriculumElement copiedElement = curriculumService.copyCurriculumElement(curriculum, null, element1, copySettings, actor);
		dbInstance.commit();

		Assert.assertEquals("Element to copy 1 (Copy)", copiedElement.getDisplayName());
		
		List<TaxonomyLevel> levelsOnCopiedElement = curriculumService.getTaxonomy(copiedElement);
		Assertions.assertThat(levelsOnCopiedElement)
			.hasSize(1)
			.containsExactlyInAnyOrder(taxonomyLevel);
	}
	
	/**
	 * If the curriculum element has course and template, only links the
	 * template and copy the lecture blocks back to the curriculum element.
	 */
	@Test
	public void instantiateTemplate() {
		Identity actor = JunitTestHelper.createAndPersistIdentityAsRndUser("copy-cur-27");
		Curriculum curriculum = curriculumService.createCurriculum("CUR-27", "Curriculum 27", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-to-copy-1", "Element to copy 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		RepositoryEntry template = JunitTestHelper.createRandomRepositoryEntry(actor);
		boolean addTemplate = curriculumService.addRepositoryTemplate(element, template);
		dbInstance.commit();
		Assert.assertTrue(addTemplate);
		
		LectureBlock lectureBlock = lectureService.createLectureBlock(element, null);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setExternalId("ELEM-COURSE-ID");
		lectureBlock.setExternalRef("LEM-COURSE-1-EV-1");
		lectureBlock.setTitle("Hello curriculum 26");
		lectureBlock = lectureService.save(lectureBlock, null);
		dbInstance.commit();
		Assert.assertNotNull(lectureBlock);
		
		String displayName = "Course instance";
		String externalRef = "CI-27";
		Date beginDate = DateUtils.getStartOfDay(DateUtils.addDays(new Date(), 2));
		Date endDate = DateUtils.getStartOfDay(DateUtils.addDays(new Date(), 22));
		
		RepositoryEntry instantiatedEntry = curriculumService.instantiateTemplate(template, element,
				displayName, externalRef, beginDate, endDate, actor);
		dbInstance.commit();
		
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(element);
		Assertions.assertThat(templates)
			.hasSize(1)
			.containsExactly(template);
		
		List<RepositoryEntry> courses = curriculumService.getRepositoryEntries(element);
		Assertions.assertThat(courses)
			.hasSize(1)
			.containsExactly(instantiatedEntry);
		
		RepositoryEntry course = courses.get(0);
		Assert.assertEquals("Course instance", course.getDisplayname());
		Assert.assertEquals("CI-27", course.getExternalRef());
		Assert.assertNotNull(course.getLifecycle());
		Assert.assertTrue(DateUtils.isSameDate(beginDate, course.getLifecycle().getValidFrom()));
		Assert.assertTrue(DateUtils.isSameDate(endDate, course.getLifecycle().getValidTo()));
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

		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-part");
		curriculumService.addMember(element, participant, CurriculumRoles.participant, author);
		
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
	
	@Test
	public void deleteSoftlyCurriculumElementNotification() {
		Curriculum curriculum = curriculumService.createCurriculum("CUR-12", "Curriculum 12", "Curriculum notifications", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		Organisation defOrganisation = JunitTestHelper.getDefaultOrganisation();
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, RepositoryEntryRuntimeType.curricular, defOrganisation);
		dbInstance.commit();

		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-part");
		curriculumService.addMember(element, participant, CurriculumRoles.participant, author);
		
		// add the course and a participant to the curriculum
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();

		List<CurriculumElement> myElements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.values());
		Assert.assertNotNull(myElements);
		Assert.assertEquals(1, myElements.size());
		
		// Delete with notifications
		Identity actor = JunitTestHelper.getDefaultActor();
		curriculumService.deleteSoftlyCurriculumElement(element, actor, true);
		dbInstance.commitAndCloseSession();

		// Check curriculum element status
		CurriculumElement deletedElement = curriculumService.getCurriculumElement(element);
		Assert.assertNotNull(deletedElement);
		Assert.assertEquals(CurriculumElementStatus.deleted, deletedElement.getElementStatus());
		
		// Check repository entry status
		entry = repositoryService.loadBy(entry);
		Assert.assertEquals(RepositoryEntryStatusEnum.closed.name(), entry.getStatus());
		
		// Check emails for author (closing the course) and participant removed from element
		List<SmtpMessage> messages = getSmtpServer().getReceivedEmails();
		Assertions.assertThat(messages)
			.hasSizeGreaterThanOrEqualTo(2)
			.map(message -> message.getHeaderValue("To"))
			.contains(participant.getUser().getEmail(), author.getUser().getEmail());
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
