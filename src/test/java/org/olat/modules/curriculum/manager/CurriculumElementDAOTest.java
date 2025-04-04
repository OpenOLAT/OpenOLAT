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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.modules.curriculum.model.AutomationImpl;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumElementNode;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void createCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-cur-el-1", "Type for", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		//check
		Assert.assertNotNull(element.getKey());
		Assert.assertNotNull(element.getCreationDate());
		Assert.assertNotNull(element.getLastModified());
		Assert.assertNotNull(element.getBeginDate());
		Assert.assertNotNull(element.getEndDate());
		Assert.assertEquals("Element-1", element.getIdentifier());
		Assert.assertEquals("1. Element", element.getDisplayName());
		Assert.assertEquals(curriculum, element.getCurriculum());
		Assert.assertEquals(type, element.getType());
		Assert.assertTrue(element.getTaughtBys().isEmpty());
	}
	
	/**
	 * Check if the root elements come in curriculum list
	 */
	@Test
	public void createCurriculumElement_rootElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false,
				null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-cur-el-1",
				"Type for", "First element", "AC-234");
		CurriculumElement root1 = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(root1);
		dbInstance.commit();
		curriculum = curriculumDao.loadByKey(curriculum.getKey());
		CurriculumElement root2 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		curriculum = curriculumDao.loadByKey(curriculum.getKey());
		CurriculumElement root3 = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3_1 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), root3, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();

		//check the curriculum to elements list
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertEquals(3, rootElements.size());
		Assert.assertEquals(root1, rootElements.get(0));
		Assert.assertEquals(root2, rootElements.get(1));
		Assert.assertEquals(root3, rootElements.get(2));
		
		//check the element to elements list
		List<CurriculumElement> root3Children = curriculumElementDao.getChildren(root3);
		Assert.assertEquals(1, root3Children.size());
		Assert.assertEquals(element3_1, root3Children.get(0));
	}
	
	@Test
	public void updateCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1a", "Curriculum for element", "Curriculum", false, null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-cur-el-1a", "Type for", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1a", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		element.setTaughtBys(Set.of(TaughtBy.owners, TaughtBy.teachers));
		curriculumElementDao.update(element);
		dbInstance.commitAndCloseSession();
		
		element = curriculumElementDao.loadByKey(element.getKey());
		Assert.assertEquals(2, element.getTaughtBys().size());
		Assert.assertTrue(element.getTaughtBys().contains(TaughtBy.owners));
		Assert.assertTrue(element.getTaughtBys().contains(TaughtBy.teachers));
	}
	
	@Test
	public void updateCurriculumElementAutomation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1b", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1b", "1. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		//check
		element.setAutoInstantiation(AutomationImpl.valueOf(5, AutomationUnit.DAYS));
		element.setAutoAccessForCoach(AutomationImpl.valueOf(6, AutomationUnit.WEEKS));
		element.setAutoPublished(AutomationImpl.valueOf(7, AutomationUnit.MONTHS));
		element.setAutoClosed(AutomationImpl.valueOf(8, AutomationUnit.YEARS));
		element = curriculumElementDao.update(element);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement reloadedElement = curriculumElementDao.loadByKey(element.getKey());
		Assert.assertNotNull(reloadedElement);
		Assert.assertNotNull(reloadedElement.getAutoInstantiation());
		Assert.assertEquals(5, reloadedElement.getAutoInstantiation().getValue().intValue());
		Assert.assertEquals(AutomationUnit.DAYS, reloadedElement.getAutoInstantiation().getUnit());
		Assert.assertEquals(6, reloadedElement.getAutoAccessForCoach().getValue().intValue());
		Assert.assertEquals(AutomationUnit.WEEKS, reloadedElement.getAutoAccessForCoach().getUnit());
		Assert.assertEquals(7, reloadedElement.getAutoPublished().getValue().intValue());
		Assert.assertEquals(AutomationUnit.MONTHS, reloadedElement.getAutoPublished().getUnit());
		Assert.assertEquals(8, reloadedElement.getAutoClosed().getValue().intValue());
		Assert.assertEquals(AutomationUnit.YEARS, reloadedElement.getAutoClosed().getUnit());
	}
	
	@Test
	public void copyCurriculumElementSimple() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-copy-el-1", "Type for copy", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-to-copy-1", "1. Element to copy",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement copyElement = curriculumElementDao.copyCurriculumElement(element, "New identifier copy", "New displayname copy", new Date(), new Date(), null, curriculum);

		//check
		Assert.assertNotNull(copyElement.getKey());
		Assert.assertNotNull(copyElement.getCreationDate());
		Assert.assertNotNull(copyElement.getLastModified());
		Assert.assertNotNull(copyElement.getBeginDate());
		Assert.assertNotNull(copyElement.getEndDate());
		Assert.assertEquals("New identifier copy", copyElement.getIdentifier());
		Assert.assertEquals("New displayname copy", copyElement.getDisplayName());
		
		Assert.assertNull(copyElement.getAutoInstantiation());

		Assert.assertEquals(curriculum, copyElement.getCurriculum());
		Assert.assertEquals(type, copyElement.getType());
		Assert.assertTrue(copyElement.getTaughtBys().isEmpty());
	}
	
	@Test
	public void copyCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-copy-el-1", "Type for copy", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-to-copy-1", "1. Element to copy",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		element.setAuthors("Marie Curie");
		element.setMainLanguage("French");
		element.setCredits("Team");
		element.setDescription("Very precise");
		element.setExpenditureOfWork("1 week");
		element.setTaughtBys(Set.of(TaughtBy.teachers));
		element.setAutoInstantiation(AutomationImpl.valueOf(5, AutomationUnit.DAYS));
		element.setAutoAccessForCoach(AutomationImpl.valueOf(4, AutomationUnit.WEEKS));
		element.setAutoPublished(AutomationImpl.valueOf(3, AutomationUnit.MONTHS));
		element.setAutoClosed(AutomationImpl.valueOf(2, AutomationUnit.YEARS));
		
		CurriculumElement copyElement = curriculumElementDao.copyCurriculumElement(element, "New identifier copy", "New displayname copy", new Date(), new Date(), null, curriculum);

		//check
		Assert.assertNotNull(copyElement.getKey());
		Assert.assertNotNull(copyElement.getCreationDate());
		Assert.assertNotNull(copyElement.getLastModified());
		Assert.assertNotNull(copyElement.getBeginDate());
		Assert.assertNotNull(copyElement.getEndDate());
		Assert.assertEquals("New identifier copy", copyElement.getIdentifier());
		Assert.assertEquals("New displayname copy", copyElement.getDisplayName());
		
		Assert.assertEquals("Marie Curie", copyElement.getAuthors());
		Assert.assertEquals("French", copyElement.getMainLanguage());
		Assert.assertEquals("Team", copyElement.getCredits());
		Assert.assertEquals("Very precise", copyElement.getDescription());
		Assert.assertEquals("1 week", copyElement.getExpenditureOfWork());
		
		Assert.assertEquals(element.getAutoInstantiation().getValue(), copyElement.getAutoInstantiation().getValue());
		Assert.assertEquals(element.getAutoInstantiation().getUnit(), copyElement.getAutoInstantiation().getUnit());
		Assert.assertEquals(element.getAutoAccessForCoach().getValue(), copyElement.getAutoAccessForCoach().getValue());
		Assert.assertEquals(element.getAutoAccessForCoach().getUnit(), copyElement.getAutoAccessForCoach().getUnit());
		Assert.assertEquals(element.getAutoPublished().getValue(), copyElement.getAutoPublished().getValue());
		Assert.assertEquals(element.getAutoPublished().getUnit(), copyElement.getAutoPublished().getUnit());
		Assert.assertEquals(element.getAutoClosed().getValue(), copyElement.getAutoClosed().getValue());
		Assert.assertEquals(element.getAutoClosed().getUnit(), copyElement.getAutoClosed().getUnit());
		
		Assert.assertEquals(curriculum, copyElement.getCurriculum());
		Assert.assertEquals(type, copyElement.getType());
		Assertions.assertThat(copyElement.getTaughtBys())
			.hasSize(1)
			.containsExactly(TaughtBy.teachers);
	}
	
	@Test
	public void loadByKey() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-2", "Curriculum for element", "Curriculum", false, null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-cur-el-2", "Type for", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, type, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		//load
		CurriculumElement reloadedElement = curriculumElementDao.loadByKey(element.getKey());
		Assert.assertNotNull(reloadedElement);
		dbInstance.commitAndCloseSession();
		//check
		Assert.assertEquals(element, reloadedElement);
		Assert.assertNotNull(reloadedElement.getCreationDate());
		Assert.assertNotNull(reloadedElement.getLastModified());
		Assert.assertNotNull(reloadedElement.getBeginDate());
		Assert.assertNotNull(reloadedElement.getEndDate());
		Assert.assertEquals("Element-2", reloadedElement.getIdentifier());
		Assert.assertEquals("2. Element", reloadedElement.getDisplayName());
		Assert.assertEquals(curriculum, reloadedElement.getCurriculum());
		Assert.assertEquals(type, reloadedElement.getType());
	}
	
	@Test
	public void loadByKeys() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-2", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement otherElement = curriculumElementDao.createCurriculumElement("Element-2", "2. Element",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElement> elements = curriculumElementDao.loadByKeys(Arrays.asList(element1, element2));
		
		Assert.assertTrue(elements.contains(element1));
		Assert.assertTrue(elements.contains(element2));
		Assert.assertFalse(elements.contains(otherElement));
	}
	
	@Test
	public void loadElements_curricullum() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-6", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-6", "6.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-6", "6.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-6", "6.2 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		//load all elements of the curriculum
		List<CurriculumElement> elements = curriculumElementDao.loadElements(curriculum, CurriculumElementStatus.values());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(elements);
		Assert.assertEquals(3, elements.size());
		Assert.assertTrue(elements.contains(element1));
		Assert.assertTrue(elements.contains(element2));
		Assert.assertTrue(elements.contains(element3));
	}
	
	@Test
	public void loadElementsWithInfos() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry1, true);
		curriculumService.addRepositoryEntry(element, entry2, true);
		dbInstance.commit();
		
		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElementsOf(null, curriculum);
		List<CurriculumElementInfos> relations = curriculumElementDao.loadElementsWithInfos(searchParams);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(element, relations.get(0).curriculumElement());
		Assert.assertEquals(element.getKey(), relations.get(0).getKey());
		Assert.assertEquals(2, relations.get(0).numOfResources());
	}
	
	@Test
	public void loadElementsWithInfosWithCurriculumOwner() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-admin");
		Identity notManager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-not-admin");
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", false, null);
		curriculumService.addMember(curriculum, manager, CurriculumRoles.curriculumowner);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();

		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElementsOf(manager, curriculum);
		List<CurriculumElementInfos> elements = curriculumElementDao.loadElementsWithInfos(searchParams);
		Assertions.assertThat(elements)
			.hasSize(1)
			.map(CurriculumElementInfos::curriculumElement)
			.containsExactly(element);
		
		CurriculumElementInfosSearchParams notSearchParams = CurriculumElementInfosSearchParams.searchElementsOf(notManager, curriculum);
		List<CurriculumElementInfos> notElements = curriculumElementDao.loadElementsWithInfos(notSearchParams);
		Assertions.assertThat(notElements)
			.isEmpty();
	}
	
	@Test
	public void loadElementsWithInfosWithCurriculumElementOwner() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-owner");
		Identity notOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-not-owner");
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, owner, CurriculumRoles.curriculumelementowner, owner);
		dbInstance.commit();

		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElementsOf(owner, curriculum);
		List<CurriculumElementInfos> elements = curriculumElementDao.loadElementsWithInfos(searchParams);
		Assertions.assertThat(elements)
			.hasSize(1)
			.map(CurriculumElementInfos::curriculumElement)
			.containsExactly(element);
		
		CurriculumElementInfosSearchParams notSearchParams = CurriculumElementInfosSearchParams.searchElementsOf(notOwner, curriculum);
		List<CurriculumElementInfos> notElements = curriculumElementDao.loadElementsWithInfos(notSearchParams);
		Assertions.assertThat(notElements)
			.isEmpty();
	}
	
	@Test
	public void loadElementsWithInfosWithCurriculumManager() {
		// Organisation with curriculum manager / course planner
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager");
		Identity notManager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-not-manager");
		Organisation organisation = organisationService.createOrganisation("Curriculum Inc.", "curriculum-1-inc", null, null, null, manager);
		organisationService.addMember(organisation, manager, OrganisationRoles.curriculummanager, manager);
		// Curriculum with organisation
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();

		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElementsOf(manager, curriculum);
		List<CurriculumElementInfos> elements = curriculumElementDao.loadElementsWithInfos(searchParams);
		Assertions.assertThat(elements)
			.hasSize(1)
			.map(CurriculumElementInfos::curriculumElement)
			.containsExactly(element);
		
		CurriculumElementInfosSearchParams notSearchParams = CurriculumElementInfosSearchParams.searchElementsOf(notManager, curriculum);
		List<CurriculumElementInfos> notElements = curriculumElementDao.loadElementsWithInfos(notSearchParams);
		Assertions.assertThat(notElements)
			.isEmpty();
	}
	
	@Test
	public void countElementsRepositoryEntry() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element1, entry, true);
		curriculumService.addRepositoryEntry(element2, entry, false);
		dbInstance.commit();
		
		Long count = curriculumElementDao.countElements(entry);
		Assert.assertEquals(2, count.longValue());
	}
	
	@Test
	public void loadElements_repoEntry() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, true);
		dbInstance.commit();
		
		List<CurriculumElement> relations = curriculumElementDao.loadElements(entry);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(element, relations.get(0));
	}
	
	@Test
	public void loadElementsByCurriculums() {
		Curriculum curriculum1 = curriculumDao.createAndPersist("Cur-for-load-1", "Curriculum to load 1", null, false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("El-load-1", "Element 1",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum1);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("El-load-2", "Element 2",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum1);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("El-load-3", "Element 3",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum1);
		Curriculum curriculum2 = curriculumDao.createAndPersist("Cur-for-load-2", "Curriculum to load 2", null, false, null);
		CurriculumElement parentElement2 = curriculumElementDao.createCurriculumElement("El-load-4", "Element 4",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum2);
		Curriculum otherCurriculum = curriculumDao.createAndPersist("Cur-for-load-3", "Curriculum to load3", null, false,
				null);
		CurriculumElement otherElement = curriculumElementDao.createCurriculumElement("El-load-5", "Element 5",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, otherCurriculum);
		dbInstance.commitAndCloseSession();
		
		Collection<CurriculumRef> curriculumRefs = Arrays.asList(curriculum1, curriculum2);
		 List<CurriculumElement> elements = curriculumElementDao.loadElementsByCurriculums(curriculumRefs);
		
		assertThat(elements)
				.containsExactlyInAnyOrder(parentElement, element1, element2, parentElement2)
				.doesNotContain(otherElement);
	}

	@Test
	public void createCurriculumElementParentChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-3", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-3", "3. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-3-1", "3.1 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-3-2", "3.2 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-3-3", "3.3 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		//reload parents
		CurriculumElement reloadedElement = curriculumElementDao.loadByKey(parentElement.getKey());
		List<CurriculumElement> children = ((CurriculumElementImpl)reloadedElement).getChildren();
		Assert.assertNotNull(children);
		Assert.assertEquals(3, children.size());
		Assert.assertEquals(element1, children.get(0));
		Assert.assertEquals(element2, children.get(1));
		Assert.assertEquals(element3, children.get(2));
	}
	
	@Test
	public void getParentLine() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-4-1", "4.1 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-4-2", "4.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-4-3", "4.1.1.1 Element",
				CurriculumElementStatus.active, null, null, element2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		//reload parents
		List<CurriculumElement> parentLine = curriculumElementDao.getParentLine(element3);
		Assert.assertNotNull(parentLine);
		Assert.assertEquals(4, parentLine.size());
		Assert.assertEquals(parentElement, parentLine.get(0));
		Assert.assertEquals(element1, parentLine.get(1));
		Assert.assertEquals(element2, parentLine.get(2));
		Assert.assertEquals(element3, parentLine.get(3));
	}
	
	@Test
	public void searchElements() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-6", "Curriculum for element", "Curriculum", false, null);
		String externalId = UUID.randomUUID().toString();
		String identifier = UUID.randomUUID().toString();
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, "6.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		element.setExternalId(externalId);
		element = curriculumElementDao.update(element);
		dbInstance.commitAndCloseSession();
		
		//search by external id
		List<CurriculumElement> elementsByExternalId = curriculumElementDao.searchElements(externalId, null, null);
		Assert.assertNotNull(elementsByExternalId);
		Assert.assertEquals(1, elementsByExternalId.size());
		Assert.assertEquals(element, elementsByExternalId.get(0));
		
		//search by identifier 
		List<CurriculumElement> elementsByIdentifier = curriculumElementDao.searchElements(null, identifier, null);
		Assert.assertNotNull(elementsByIdentifier);
		Assert.assertEquals(1, elementsByIdentifier.size());
		Assert.assertEquals(element, elementsByIdentifier.get(0));
		
		// search by primary key
		List<CurriculumElement> elementsByKey = curriculumElementDao.searchElements(null, null, element.getKey());
		Assert.assertNotNull(elementsByKey);
		Assert.assertEquals(1, elementsByKey.size());
		Assert.assertEquals(element, elementsByKey.get(0));
	}
	
	@Test
	public void searchElementsWithParams_permissions() {
		Identity nobody = JunitTestHelper.createAndPersistIdentityAsRndUser("curriculum-nobody");
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity curriculumAdmin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("curriculum-admin");
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-6", "Curriculum for element", "Curriculum", false, defOrganisation);
		String externalId = UUID.randomUUID().toString();
		String identifier = UUID.randomUUID().toString();
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, "6.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		element.setExternalId(externalId);
		element = curriculumElementDao.update(element);
		dbInstance.commitAndCloseSession();

		//search by external id, administrator
		CurriculumElementSearchParams adminSearchParams = new CurriculumElementSearchParams(curriculumAdmin);
		adminSearchParams.setElementId(externalId);
		List<CurriculumElementSearchInfos> elementsByExternalId = curriculumElementDao.searchElements(adminSearchParams);
		Assertions.assertThat(elementsByExternalId)
			.hasSize(1)
			.map(CurriculumElementSearchInfos::curriculumElement)
			.containsExactly(element);
		
		//search by identifier 
		CurriculumElementSearchParams nobodySearchParams = new CurriculumElementSearchParams(nobody);
		nobodySearchParams.setElementId(externalId);
		List<CurriculumElementSearchInfos> noElements = curriculumElementDao.searchElements(nobodySearchParams);
		Assertions.assertThat(noElements)
			.isEmpty();
	}
	
	@Test
	public void searchElementsWithParamsCurriculumElementOwner() {
		Identity nobody = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-not-owner");
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-owner");
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-6", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element owner", "1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element owner", "2 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element1, owner, CurriculumRoles.curriculumelementowner, owner);
		dbInstance.commitAndCloseSession();

		//search with owner
		CurriculumElementSearchParams adminSearchParams = new CurriculumElementSearchParams(owner);
		List<CurriculumElementSearchInfos> ownedElements = curriculumElementDao.searchElements(adminSearchParams);
		Assertions.assertThat(ownedElements)
			.hasSize(1)
			.map(CurriculumElementSearchInfos::curriculumElement)
			.containsExactly(element1)
			.doesNotContain(element2);
		
		//search with random user 
		CurriculumElementSearchParams nobodySearchParams = new CurriculumElementSearchParams(nobody);
		List<CurriculumElementSearchInfos> noElements = curriculumElementDao.searchElements(nobodySearchParams);
		Assertions.assertThat(noElements)
			.isEmpty();
	}
	
	@Test
	public void searchElementsWithParamsCurriculumManager() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-owner");
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-manager");
		Identity notOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-not");
		Organisation organisation = organisationService.createOrganisation("Curriculum management", "Curriculum management", null, null, null, manager);
		organisationService.addMember(organisation, manager, OrganisationRoles.curriculummanager, manager);
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-6", "Curriculum for element", "Curriculum", false, organisation);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element owner", "1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element owner", "2 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element1, owner, CurriculumRoles.curriculumelementowner, manager);
		dbInstance.commitAndCloseSession();

		//search by manager
		CurriculumElementSearchParams adminSearchParams = new CurriculumElementSearchParams(manager);
		List<CurriculumElementSearchInfos> managedElements = curriculumElementDao.searchElements(adminSearchParams);
		Assertions.assertThat(managedElements)
			.hasSize(2)
			.map(CurriculumElementSearchInfos::curriculumElement)
			.containsExactlyInAnyOrder(element1, element2);
		
		//search by element owner
		CurriculumElementSearchParams ownerSearchParams = new CurriculumElementSearchParams(owner);
		List<CurriculumElementSearchInfos> ownedElements = curriculumElementDao.searchElements(ownerSearchParams);
		Assertions.assertThat(ownedElements)
			.hasSize(1)
			.map(CurriculumElementSearchInfos::curriculumElement)
			.containsExactly(element1);
		
		//search with a random user 
		CurriculumElementSearchParams nobodySearchParams = new CurriculumElementSearchParams(notOwner);
		List<CurriculumElementSearchInfos> noElements = curriculumElementDao.searchElements(nobodySearchParams);
		Assertions.assertThat(noElements)
			.isEmpty();
	}
	
	/**
	 * The method only checks the query syntax, not the results.
	 */
	@Test
	public void searchElementsWithParams_allParameters() {
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		Identity curriculumAdmin = JunitTestHelper.createAndPersistIdentityAsRndAdmin("curriculum-admin");
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-6", "Curriculum for element", "Curriculum", false, defOrganisation);
		String externalId = UUID.randomUUID().toString();
		String identifier = UUID.randomUUID().toString();
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, "6.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		element.setExternalId(externalId);
		element = curriculumElementDao.update(element);
		dbInstance.commitAndCloseSession();

		//search by external id, administrator
		CurriculumElementSearchParams adminSearchParams = new CurriculumElementSearchParams(curriculumAdmin);
		adminSearchParams.setElementId(externalId);
		adminSearchParams.setElementBeginDate(new Date());
		adminSearchParams.setElementEndDate(new Date());
		adminSearchParams.setElementText("Hello");
		adminSearchParams.setEntryId("734");
		adminSearchParams.setEntryText("Course");
		adminSearchParams.setSearchString("Search");
		List<CurriculumElementSearchInfos> elementsByExternalId = curriculumElementDao.searchElements(adminSearchParams);
		Assert.assertNotNull(elementsByExternalId);

	}
	
	@Test
	public void getImplementations() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-impl-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement implementationElement = curriculumElementDao.createCurriculumElement("Element-impl", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-impl-1.1", "1.1 Element",
				CurriculumElementStatus.active, null, null, implementationElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		List<CurriculumElement> implementations = curriculumElementDao.getImplementations(curriculum);
		Assertions.assertThat(implementations)
			.hasSize(1)
			.containsExactly(implementationElement)
			.doesNotContain(element1);
	}
	
	@Test
	public void getImplementationsWithStatus() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-impl-1", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement implementation1Element = curriculumElementDao.createCurriculumElement("Element-impl", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-impl-1.1", "1.1 Element",
				CurriculumElementStatus.active, null, null, implementation1Element, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement implementationFinishedElement = curriculumElementDao.createCurriculumElement("Element-impl", "2. Element",
				CurriculumElementStatus.finished, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement implementationDeletedElement = curriculumElementDao.createCurriculumElement("Element-impl", "3. Element",
				CurriculumElementStatus.deleted, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		List<CurriculumElement> implementations = curriculumElementDao.getImplementations(curriculum, CurriculumElementStatus.notDeleted());
		Assertions.assertThat(implementations)
			.hasSize(2)
			.containsExactlyInAnyOrder(implementation1Element, implementationFinishedElement)
			.doesNotContain(element1, implementationDeletedElement);
	}
	
	@Test
	public void getDescendants() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-5", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-5-1", "5.1 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-5-1-1", "5.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-5-2", "5.2 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// load descendants of the root element
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(parentElement);
		Assert.assertNotNull(descendants);
		Assert.assertEquals(3, descendants.size());
		Assert.assertTrue(descendants.contains(element1));
		Assert.assertTrue(descendants.contains(element1_1));
		Assert.assertTrue(descendants.contains(element2));
	}
	
	@Test
	public void getDescendantsTree() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-35", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-35", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-35-1", "1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element11 = curriculumElementDao.createCurriculumElement("Element-35-1-1", "1.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-35-2", "1.2 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// load descendants of the root element
		CurriculumElementNode rootNode = curriculumElementDao.getDescendantTree(rootElement);
		Assert.assertNotNull(rootNode);
		Assert.assertEquals(rootElement, rootNode.getElement());
		
		List<CurriculumElementNode> elements = rootNode.getChildrenNode();
		Assertions.assertThat(elements)
			.hasSize(2)
			.map(CurriculumElementNode::getElement)
			.containsExactlyInAnyOrder(element1, element2);

		CurriculumElementNode node1 = elements.stream()
				.filter(el -> element1.equals(el.getElement()))
				.findFirst().orElse(null);
		Assert.assertNotNull(node1);
		
		List<CurriculumElementNode> element1Children = node1.getChildrenNode();
		Assertions.assertThat(element1Children)
			.hasSize(1)
			.map(CurriculumElementNode::getElement)
			.containsExactlyInAnyOrder(element11);
	}
	
	@Test
	public void getChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-15", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-15", "15. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-15-1", "15.1 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-15-2", "15.2 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2_1 = curriculumElementDao.createCurriculumElement("Element-15-2-1", "15.2.1 Element",
				CurriculumElementStatus.active, null, null, element2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// get children of the root element
		List<CurriculumElement> children = curriculumElementDao.getChildren(parentElement);
		Assert.assertNotNull(children);
		Assert.assertEquals(2, children.size());
		Assert.assertTrue(children.contains(element1));
		Assert.assertTrue(children.contains(element2));
		
		// check more
		List<CurriculumElement> secondChildren = curriculumElementDao.getChildren(element2);
		Assert.assertNotNull(secondChildren);
		Assert.assertEquals(1, secondChildren.size());
		Assert.assertTrue(secondChildren.contains(element2_1));
	}
	
	@Test
	public void countChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-33", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-33", "33. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-33-1", "33 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-33-2", "33 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// Count parent children
		int numOfChildren = curriculumElementDao.countChildren(parentElement);
		Assert.assertEquals(2, numOfChildren);
		
		// check more
		int numOfGreatChildren1 = curriculumElementDao.countChildren(element1);
		Assert.assertEquals(0, numOfGreatChildren1);
		int numOfGreatChildren2 = curriculumElementDao.countChildren(element2);
		Assert.assertEquals(0, numOfGreatChildren2);
	}
	
	@Test
	public void hasChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-32", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-32", "32. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-32-1", "32 Element",
				CurriculumElementStatus.active, null, null, parentElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		
		// Check
		boolean hasChildren = curriculumElementDao.hasChildren(parentElement);
		Assert.assertTrue(hasChildren);
		boolean hasNoChildren = curriculumElementDao.hasChildren(element);
		Assert.assertFalse(hasNoChildren);
	}
	
	@Test
	public void moveCurriculumElement_elementToOtherElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-7", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-7", "7. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-7-1", "7.1 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1_1 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element",
				CurriculumElementStatus.active, null, null, element1_1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1_2 = curriculumElementDao.createCurriculumElement("Element-7-1-2", "7.1.2 Element",
				CurriculumElementStatus.active, null, null, element1_1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-7-2", "7.2 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1_1 under element2
		curriculumElementDao.move(element1_1, element2, null, curriculum);
		dbInstance.commit();
		
		// check parent line of element1_1_2
		CurriculumElement reloadElement1_1_2 = curriculumElementDao.loadByKey(element1_1_2.getKey());
		List<CurriculumElement> parentLine1_1_2 = curriculumElementDao.getParentLine(reloadElement1_1_2);
		Assert.assertNotNull(parentLine1_1_2);
		Assert.assertEquals(4, parentLine1_1_2.size());
		Assert.assertEquals(rootElement, parentLine1_1_2.get(0));
		Assert.assertEquals(element2, parentLine1_1_2.get(1));
		Assert.assertEquals(element1_1, parentLine1_1_2.get(2));
		Assert.assertEquals(element1_1_2, parentLine1_1_2.get(3));
		
		// check descendants element1_1
		CurriculumElement reloadElement1_1 = curriculumElementDao.loadByKey(element1_1.getKey());
		List<CurriculumElement> descendants1_1 = curriculumElementDao.getDescendants(reloadElement1_1);
		Assert.assertNotNull(descendants1_1);
		Assert.assertEquals(2, descendants1_1.size());
		Assert.assertTrue(descendants1_1.contains(element1_1_1));
		Assert.assertTrue(descendants1_1.contains(element1_1_2));
	}
	
	@Test
	public void moveCurriculumElement_underSameElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-8", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-8", "8. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-8-1-1", "8.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-8-1-2", "8.1.2 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_3 = curriculumElementDao.createCurriculumElement("Element-8-1-3", "8.1.3 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_4 = curriculumElementDao.createCurriculumElement("Element-8-1-4", "8.1.4 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_5 = curriculumElementDao.createCurriculumElement("Element-8-1-5", "8.1.5 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1_1 at third posistion
		curriculumElementDao.move(element1_2, rootElement, element1_4, curriculum);
		dbInstance.commit();
		
		CurriculumElementImpl element = (CurriculumElementImpl)curriculumElementDao.loadByKey(rootElement.getKey());
		List<CurriculumElement> children = element.getChildren();
		Assert.assertEquals(children.get(0), element1_1);
		Assert.assertEquals(children.get(1), element1_3);
		Assert.assertEquals(children.get(2), element1_4);
		Assert.assertEquals(children.get(3), element1_2);
		Assert.assertEquals(children.get(4), element1_5);
	}
	
	@Test
	public void moveCurriculumElement_underSameElement_v2() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-8", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-8", "8. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-8-1-1", "8.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-8-1-2", "8.1.2 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_3 = curriculumElementDao.createCurriculumElement("Element-8-1-3", "8.1.3 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_4 = curriculumElementDao.createCurriculumElement("Element-8-1-4", "8.1.4 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_5 = curriculumElementDao.createCurriculumElement("Element-8-1-5", "8.1.5 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1_5 at third position
		curriculumElementDao.move(element1_5, rootElement, element1_2, curriculum);
		dbInstance.commit();
		
		CurriculumElementImpl element = (CurriculumElementImpl)curriculumElementDao.loadByKey(rootElement.getKey());
		List<CurriculumElement> children = element.getChildren();
		Assert.assertEquals(element1_1, children.get(0));
		Assert.assertEquals(element1_2, children.get(1));
		Assert.assertEquals(element1_5, children.get(2));
		Assert.assertEquals(element1_3, children.get(3));
		Assert.assertEquals(element1_4, children.get(4));
	}

	@Test
	public void moveCurriculumElement_rootElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-9-1", "9.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-9-2", "9.2 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-9-3", "9.3 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element4 = curriculumElementDao.createCurriculumElement("Element-9-4", "9.4 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element5 = curriculumElementDao.createCurriculumElement("Element-9-5", "9.5 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1_1 under element2
		curriculumElementDao.move(element2, null, element4, curriculum);
		dbInstance.commit();
		
		// check parent line of element1_1_2
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertEquals(element1, rootElements.get(0));
		Assert.assertEquals(element3, rootElements.get(1));
		Assert.assertEquals(element4, rootElements.get(2));
		Assert.assertEquals(element2, rootElements.get(3));
		Assert.assertEquals(element5, rootElements.get(4));
	}
	
	@Test
	public void moveCurriculumElement_rootToElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-7", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement1 = curriculumElementDao.createCurriculumElement("Element-10-1", "10.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1", "10.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1-1",
				"10.1.1.1 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_1_2 = curriculumElementDao.createCurriculumElement("Element-10-1-1-2",
				"10.1.1.2 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-10-1-2", "10.1.2 Element",
				CurriculumElementStatus.active, null, null, rootElement1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commit();
		Assert.assertNotNull(element1_2);
		
		curriculum = curriculumDao.loadByKey(curriculum.getKey());
		CurriculumElement rootElement2 = curriculumElementDao.createCurriculumElement("Element-10-2", "10.2 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1_1 under element2
		CurriculumElement reloadedElement1_1 = curriculumElementDao.loadByKey(element1_1.getKey());
		curriculumElementDao.move(rootElement2, reloadedElement1_1, element1_1_1, curriculum);
		dbInstance.commit();
		
		// check children element1_1
		CurriculumElementImpl reloadElement1_1 = (CurriculumElementImpl)curriculumElementDao.loadByKey(element1_1.getKey());
		List<CurriculumElement> element11children = reloadElement1_1.getChildren();
		Assert.assertEquals(3, element11children.size());
		Assert.assertEquals(element1_1_1, element11children.get(0));
		Assert.assertEquals(rootElement2, element11children.get(1));
		Assert.assertEquals(element1_1_2, element11children.get(2));
		
		
		// check children curriculum
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertEquals(1, rootElements.size());
		Assert.assertEquals(rootElement1, rootElements.get(0));
	}
	
	@Test
	public void moveCurriculumElement_elementToRoot() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-7", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement1 = curriculumElementDao.createCurriculumElement("Element-10-1", "10.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1", "10.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1-1",
				"10.1.1.1 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_1_2 = curriculumElementDao.createCurriculumElement("Element-10-1-1-2",
				"10.1.1.2 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_1_3 = curriculumElementDao.createCurriculumElement("Element-10-1-1-3",
				"10.1.1.3 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		dbInstance.commit();

		
		// move element1_1 under element2
		CurriculumElement reloadedElement1_1_3 = curriculumElementDao.loadByKey(element1_1_3.getKey());
		curriculumElementDao.move(reloadedElement1_1_3, null, rootElement1, curriculum);
		dbInstance.commit();
		
		// check children element1_1
		CurriculumElementImpl reloadElement1_1 = (CurriculumElementImpl)curriculumElementDao.loadByKey(element1_1.getKey());
		List<CurriculumElement> element11children = reloadElement1_1.getChildren();
		Assert.assertEquals(2, element11children.size());
		Assert.assertEquals(element1_1_1, element11children.get(0));
		Assert.assertEquals(element1_1_2, element11children.get(1));
		
		
		// check children curriculum
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertEquals(2, rootElements.size());
		Assert.assertEquals(rootElement1, rootElements.get(0));
		Assert.assertEquals(element1_1_3, rootElements.get(1));
	}
	
	@Test
	public void moveRootCurriculumElement_curriculumToCurriculum() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-start-", "Curriculum to start", "Curriculum", false, null);
		Curriculum targetCurriculum = curriculumDao.createAndPersist("cur-new-home-", "Curriculum as new home", "Curriculum", false, null);

		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-1-1", "1.1. Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-1-2", "1.2. Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2_1 = curriculumElementDao.createCurriculumElement("Element-1-2-1", "1.2.1. Element",
				CurriculumElementStatus.active, null, null, element1_2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2_2 = curriculumElementDao.createCurriculumElement("Element-1-2-2", "1.2.2 Element",
				CurriculumElementStatus.active, null, null, element1_2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1 under its new curriculum
		curriculumElementDao.move(element1, targetCurriculum);
		dbInstance.commitAndCloseSession();
		
		// check the source curriculum
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertTrue(rootElements.isEmpty());
		
		// check the target curriculum
		CurriculumImpl reloadedTargetCurriculum = (CurriculumImpl)curriculumDao.loadByKey(targetCurriculum.getKey());
		List<CurriculumElement> targetRootElements = reloadedTargetCurriculum.getRootElements();
		Assert.assertEquals(1, targetRootElements.size());
		Assert.assertTrue(targetRootElements.contains(element1));
		
		List<CurriculumElement> movedElements = curriculumElementDao.loadElements(reloadedTargetCurriculum, CurriculumElementStatus.values());
		Assert.assertTrue(movedElements.contains(element1));
		Assert.assertTrue(movedElements.contains(element1_1));
		Assert.assertTrue(movedElements.contains(element1_2));
		Assert.assertTrue(movedElements.contains(element1_2_1));
		Assert.assertTrue(movedElements.contains(element1_2_2));
	}
	
	@Test
	public void moveRootCurriculumElement_curriculumToCurriculum_v2() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-start-", "Curriculum to start", "Curriculum", false, null);
		Curriculum targetCurriculum = curriculumDao.createAndPersist("cur-new-home-", "Curriculum as new home", "Curriculum", false, null);

		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-1-1", "1.1. Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-1-2", "1.2. Element",
				CurriculumElementStatus.active, null, null, element1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2_1 = curriculumElementDao.createCurriculumElement("Element-1-2-1", "1.2.1. Element",
				CurriculumElementStatus.active, null, null, element1_2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2_2 = curriculumElementDao.createCurriculumElement("Element-1-2-2", "1.2.2 Element",
				CurriculumElementStatus.active, null, null, element1_2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		CurriculumElement stayingElement1 = curriculumElementDao.createCurriculumElement("T-Element-1", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement stayingElement1_1 = curriculumElementDao.createCurriculumElement("T-Element-1-1",
				"1.1. Element", CurriculumElementStatus.active, null, null, stayingElement1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);

		CurriculumElement targetElement1 = curriculumElementDao.createCurriculumElement("T-Element-1", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, targetCurriculum);
		CurriculumElement targetElement1_1 = curriculumElementDao.createCurriculumElement("T-Element-1-1",
				"1.1. Element", CurriculumElementStatus.active, null, null, targetElement1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				targetCurriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1 under its new curriculum
		curriculumElementDao.move(element1, targetCurriculum);
		dbInstance.commitAndCloseSession();
		
		// check the source curriculum
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertEquals(1, rootElements.size());
		Assert.assertTrue(rootElements.contains(stayingElement1));
		
		// check the target curriculum
		CurriculumImpl reloadedTargetCurriculum = (CurriculumImpl)curriculumDao.loadByKey(targetCurriculum.getKey());
		List<CurriculumElement> targetRootElements = reloadedTargetCurriculum.getRootElements();
		Assert.assertEquals(2, targetRootElements.size());
		Assert.assertTrue(targetRootElements.contains(targetElement1));
		Assert.assertTrue(targetRootElements.contains(element1));
		
		List<CurriculumElement> targetElements = curriculumElementDao.loadElements(reloadedTargetCurriculum, CurriculumElementStatus.values());
		Assert.assertEquals(7, targetElements.size());
		// current ones
		Assert.assertTrue(targetElements.contains(targetElement1));
		Assert.assertTrue(targetElements.contains(targetElement1_1));
		// moved ones
		Assert.assertTrue(targetElements.contains(element1));
		Assert.assertTrue(targetElements.contains(element1_1));
		Assert.assertTrue(targetElements.contains(element1_2));
		Assert.assertTrue(targetElements.contains(element1_2_1));
		Assert.assertTrue(targetElements.contains(element1_2_2));
		
		List<CurriculumElement> stayingElements = curriculumElementDao.loadElements(curriculum, CurriculumElementStatus.values());
		Assert.assertEquals(2, stayingElements.size());
		// current ones
		Assert.assertTrue(stayingElements.contains(stayingElement1));
		Assert.assertTrue(stayingElements.contains(stayingElement1_1));
	}
	
	@Test
	public void moveCurriculumElement_elementCurriculumToCurriculum() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-start-", "Curriculum to start", "Curriculum", false, null);
		Curriculum targetCurriculum = curriculumDao.createAndPersist("cur-new-home-", "Curriculum as new home", "Curriculum", false, null);

		CurriculumElement stayingElement1 = curriculumElementDao.createCurriculumElement("Element-1", "1. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement stayingElement1_1 = curriculumElementDao.createCurriculumElement("Element-1-1",
				"1.1. Element", CurriculumElementStatus.active, null, null, stayingElement1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-1-2", "1.2. Element",
				CurriculumElementStatus.active, null, null, stayingElement1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2_1 = curriculumElementDao.createCurriculumElement("Element-1-2-1", "1.2.1. Element",
				CurriculumElementStatus.active, null, null, element1_2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2_2 = curriculumElementDao.createCurriculumElement("Element-1-2-2", "1.2.2 Element",
				CurriculumElementStatus.active, null, null, element1_2, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		CurriculumElement stayingElement2 = curriculumElementDao.createCurriculumElement("T-Element-2", "2. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement stayingElement2_1 = curriculumElementDao.createCurriculumElement("T-Element-2-1",
				"2.1. Element", CurriculumElementStatus.active, null, null, stayingElement1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);

		CurriculumElement targetElement1 = curriculumElementDao.createCurriculumElement("Ta-Element-3", "3. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, targetCurriculum);
		CurriculumElement targetElement1_1 = curriculumElementDao.createCurriculumElement("Ta-Element-3-1",
				"3.1. Element", CurriculumElementStatus.active, null, null, targetElement1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				targetCurriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1 under its new curriculum
		curriculumElementDao.move(element1_2, targetElement1_1, null, targetCurriculum);
		dbInstance.commitAndCloseSession();
		
		// check the source curriculum
		CurriculumImpl reloadedCurriculum = (CurriculumImpl)curriculumDao.loadByKey(curriculum.getKey());
		List<CurriculumElement> rootElements = reloadedCurriculum.getRootElements();
		Assert.assertEquals(2, rootElements.size());
		Assert.assertTrue(rootElements.contains(stayingElement1));
		Assert.assertTrue(rootElements.contains(stayingElement2));
		
		// check the target curriculum
		CurriculumImpl reloadedTargetCurriculum = (CurriculumImpl)curriculumDao.loadByKey(targetCurriculum.getKey());
		List<CurriculumElement> targetRootElements = reloadedTargetCurriculum.getRootElements();
		Assert.assertEquals(1, targetRootElements.size());
		Assert.assertTrue(targetRootElements.contains(targetElement1));
		
		List<CurriculumElement> targetElements = curriculumElementDao.loadElements(reloadedTargetCurriculum, CurriculumElementStatus.values());
		Assert.assertEquals(5, targetElements.size());
		// current ones
		Assert.assertTrue(targetElements.contains(targetElement1));
		Assert.assertTrue(targetElements.contains(targetElement1_1));
		// check curriculum references
		Assert.assertEquals(targetCurriculum, targetElement1.getCurriculum());
		Assert.assertEquals(targetCurriculum, targetElement1_1.getCurriculum());
		// moved ones
		Assert.assertTrue(targetElements.contains(element1_2));
		Assert.assertTrue(targetElements.contains(element1_2_1));
		Assert.assertTrue(targetElements.contains(element1_2_2));
		// check curriculum reference
		for(CurriculumElement targetElement:targetElements) {
			Assert.assertEquals(targetCurriculum, targetElement.getCurriculum());
		}
		// check parent
		CurriculumElement reloadedElement1_2 = curriculumElementDao.loadByKey(element1_2.getKey());
		Assert.assertEquals(targetElement1_1, reloadedElement1_2.getParent());
		
		List<CurriculumElement> stayingElements = curriculumElementDao.loadElements(curriculum, CurriculumElementStatus.values());
		Assert.assertEquals(4, stayingElements.size());
		// current ones
		Assert.assertTrue(stayingElements.contains(stayingElement1));
		Assert.assertTrue(stayingElements.contains(stayingElement1_1));
		Assert.assertTrue(stayingElements.contains(stayingElement2));
		Assert.assertTrue(stayingElements.contains(stayingElement2_1));
		// check curriculum references
		Assert.assertEquals(curriculum, stayingElement1.getCurriculum());
		Assert.assertEquals(curriculum, stayingElement1_1.getCurriculum());
		Assert.assertEquals(curriculum, stayingElement2.getCurriculum());
		Assert.assertEquals(curriculum, stayingElement2_1.getCurriculum());
	}
	
	@Test
	public void moveCurriculumElement_elementToRootOtherCurriculum() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-7", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement1 = curriculumElementDao.createCurriculumElement("Element-10-1", "10.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1", "10.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement1, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1-1",
				"10.1.1.1 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_1_2 = curriculumElementDao.createCurriculumElement("Element-10-1-1-2",
				"10.1.1.2 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_1_3 = curriculumElementDao.createCurriculumElement("Element-10-1-1-3",
				"10.1.1.3 Element", CurriculumElementStatus.active, null, null, element1_1, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement element1_1_3_1 = curriculumElementDao.createCurriculumElement("Element-10-1-1-3-1",
				"10.1.1.3.1 Element", CurriculumElementStatus.active, null, null, element1_1_3, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);

		Curriculum targetCurriculum = curriculumDao.createAndPersist("cur-for-el-8", "Target curriculum for element", "Curriculum", false, null);
		CurriculumElement rootElement2 = curriculumElementDao.createCurriculumElement("Element-10-2", "10.2 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, targetCurriculum);
		dbInstance.commit();

		// move element1_1_3 under the target curriculum
		CurriculumElement reloadedElement1_1_3 = curriculumElementDao.loadByKey(element1_1_3.getKey());
		curriculumElementDao.move(reloadedElement1_1_3, null, rootElement2, targetCurriculum);
		dbInstance.commit();
		
		// check children element1_1
		CurriculumElementImpl reloadElement1_1 = (CurriculumElementImpl)curriculumElementDao.loadByKey(element1_1.getKey());
		List<CurriculumElement> element11children = reloadElement1_1.getChildren();
		Assert.assertEquals(2, element11children.size());
		Assert.assertEquals(element1_1_1, element11children.get(0));
		Assert.assertEquals(element1_1_2, element11children.get(1));
		
		// check target curriculum
		List<CurriculumElement> targetElements = curriculumElementDao.loadElements(targetCurriculum, CurriculumElementStatus.values());
		Assert.assertEquals(3, targetElements.size());
		// check elements
		Assert.assertTrue(targetElements.contains(rootElement2));
		Assert.assertTrue(targetElements.contains(element1_1_3));
		Assert.assertTrue(targetElements.contains(element1_1_3_1));
		
		// check children curriculum
		CurriculumImpl reloadedTargetCurriculum = (CurriculumImpl)curriculumDao.loadByKey(targetCurriculum.getKey());
		List<CurriculumElement> rootElements = reloadedTargetCurriculum.getRootElements();
		Assert.assertEquals(2, rootElements.size());
		Assert.assertEquals(rootElement2, rootElements.get(0));
		Assert.assertEquals(element1_1_3, rootElements.get(1));
	}
	
	@Test
	public void reorderCurriculumElementsChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-order-1", "Curriculum for element to reorder", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-30-1", "30.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-30-1-1", "30.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-30-1-2", "30.1.2 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_3 = curriculumElementDao.createCurriculumElement("Element-30-1-3", "30.1.3 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_4 = curriculumElementDao.createCurriculumElement("Element-30-1-4", "30.1.4 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement parentElement = curriculumElementDao.loadByKey(rootElement.getKey());
		List<CurriculumElementRef> order = List.of(
				new CurriculumElementRefImpl(element1_3.getKey()),
				new CurriculumElementRefImpl(element1_2.getKey()),
				new CurriculumElementRefImpl(element1_4.getKey()),
				new CurriculumElementRefImpl(element1_1.getKey()));
		curriculumElementDao.orderList(parentElement, order);
		dbInstance.commitAndCloseSession();
		
		// check order by pos
		CurriculumElement reloadedElement = curriculumElementDao.loadByKey(rootElement.getKey());
		List<CurriculumElement> reorderedElements = curriculumElementDao.getChildren(reloadedElement);
		Assert.assertEquals(element1_3, reorderedElements.get(0));
		Assert.assertEquals(element1_2, reorderedElements.get(1));
		Assert.assertEquals(element1_4, reorderedElements.get(2));
		Assert.assertEquals(element1_1, reorderedElements.get(3));
		dbInstance.commitAndCloseSession();
		
		// check list 
		CurriculumElementImpl fullyLoadedElement = (CurriculumElementImpl)curriculumElementDao.loadByKey(rootElement.getKey());
		Assert.assertEquals(element1_3, fullyLoadedElement.getChildren().get(0));
		Assert.assertEquals(element1_2, fullyLoadedElement.getChildren().get(1));
		Assert.assertEquals(element1_4, fullyLoadedElement.getChildren().get(2));
		Assert.assertEquals(element1_1, fullyLoadedElement.getChildren().get(3));
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void reorderPartiallyCurriculumElementsChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-order-1", "Curriculum for element to reorder", "Curriculum", false, null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-30-1", "30.1 Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-31-1-1", "31.1.1 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_2 = curriculumElementDao.createCurriculumElement("Element-31-1-2", "31.1.2 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_3 = curriculumElementDao.createCurriculumElement("Element-31-1-3", "31.1.3 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_4 = curriculumElementDao.createCurriculumElement("Element-31-1-4", "31.1.4 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_5 = curriculumElementDao.createCurriculumElement("Element-31-1-5", "31.1.5 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element1_6 = curriculumElementDao.createCurriculumElement("Element-31-1-6", "31.1.6 Element",
				CurriculumElementStatus.active, null, null, rootElement, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		CurriculumElement parentElement = curriculumElementDao.loadByKey(rootElement.getKey());
		List<CurriculumElementRef> order = List.of(
				new CurriculumElementRefImpl(element1_4.getKey()),
				new CurriculumElementRefImpl(element1_2.getKey()),
				new CurriculumElementRefImpl(element1_6.getKey()),
				new CurriculumElementRefImpl(element1_1.getKey()));
		curriculumElementDao.orderList(parentElement, order);
		dbInstance.commitAndCloseSession();
		
		// check order by pos
		CurriculumElement reloadedElement = curriculumElementDao.loadByKey(rootElement.getKey());
		List<CurriculumElement> reorderedElements = curriculumElementDao.getChildren(reloadedElement);
		for(CurriculumElement reorderedElement:reorderedElements) {
			System.out.println(reorderedElement.getDisplayName());
		}
		Assert.assertEquals(element1_4, reorderedElements.get(0));
		Assert.assertEquals(element1_2, reorderedElements.get(1));
		Assert.assertEquals(element1_6, reorderedElements.get(2));
		Assert.assertEquals(element1_1, reorderedElements.get(3));
		Assert.assertEquals(element1_3, reorderedElements.get(4));
		Assert.assertEquals(element1_5, reorderedElements.get(5));
		dbInstance.commitAndCloseSession();
		
		// check list 
		CurriculumElementImpl fullyLoadedElement = (CurriculumElementImpl)curriculumElementDao.loadByKey(rootElement.getKey());
		Assert.assertEquals(element1_4, fullyLoadedElement.getChildren().get(0));
		Assert.assertEquals(element1_2, fullyLoadedElement.getChildren().get(1));
		Assert.assertEquals(element1_6, fullyLoadedElement.getChildren().get(2));
		Assert.assertEquals(element1_1, fullyLoadedElement.getChildren().get(3));
		Assert.assertEquals(element1_3, fullyLoadedElement.getChildren().get(4));
		Assert.assertEquals(element1_5, fullyLoadedElement.getChildren().get(5));
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void getMembersIdentity() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager, actor);
		dbInstance.commitAndCloseSession();
		
		List<Identity> members = curriculumElementDao.getMembersIdentity(element, CurriculumRoles.curriculummanager.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor, members.get(0));
	}
	
	@Test
	public void getMembersIdentity_elementKeys() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager, actor);
		dbInstance.commitAndCloseSession();
		
		// check with something to find
		List<Long> elementKeys = Collections.singletonList(element.getKey());
		List<Identity> members = curriculumElementDao.getMembersIdentity(elementKeys, CurriculumRoles.curriculummanager.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor, members.get(0));
		
		// negative check
		List<Identity> participants = curriculumElementDao.getMembersIdentity(elementKeys, CurriculumRoles.participant.name());
		Assert.assertNotNull(participants);
		Assert.assertTrue(participants.isEmpty());
	}
	
	@Test
	public void hasCurriculumElementRole() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-30");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-30", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-30", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, actor);
		dbInstance.commitAndCloseSession();
		
		boolean isCoach = curriculumElementDao.hasCurriculumElementRole(coach, CurriculumRoles.coach.name());
		Assert.assertTrue(isCoach);
	}
	
	@Test
	public void hasNotCurriculumElementRole() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-31");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-31", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-31", "32. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, coach, CurriculumRoles.owner, actor);
		dbInstance.commitAndCloseSession();
		
		boolean isNotCoach = curriculumElementDao.hasCurriculumElementRole(coach, CurriculumRoles.coach.name());
		Assert.assertFalse(isNotCoach);
	}
	
	@Test
	public void getMemberKeys() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-24", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, actor);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		List<Long> members = curriculumElementDao.getMemberKeys(elements, CurriculumRoles.coach.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(coach.getKey(), members.get(0));
	}
	
	@Test
	public void getMembers() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-24b", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24b", "4. Element (b)",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, coach, CurriculumRoles.coach, actor);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		List<Identity> members = curriculumElementDao.getMembers(elements, CurriculumRoles.coach.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(coach, members.get(0));
	}
	
	@Test
	public void getMembershipInfos_elements() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculumelementowner, actor);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementMembership> members = curriculumElementDao.getMembershipInfos(null, Collections.singletonList(element), supervisor);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor.getKey(), members.get(0).getIdentityKey());
		Assert.assertEquals(element.getKey(), members.get(0).getCurriculumElementKey());
		Assert.assertTrue(members.get(0).isCurriculumElementOwner());
		Assert.assertFalse(members.get(0).isRepositoryEntryOwner());
		Assert.assertFalse(members.get(0).isCoach());
		Assert.assertFalse(members.get(0).isParticipant());
	}
	
	@Test
	public void getMembershipInfos_curriculum() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-5", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "5. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculumelementowner, actor);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumRef> curriculumList = Collections.singletonList(curriculum);
		List<CurriculumElementMembership> members = curriculumElementDao.getMembershipInfos(curriculumList, null, supervisor);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor.getKey(), members.get(0).getIdentityKey());
		Assert.assertEquals(element.getKey(), members.get(0).getCurriculumElementKey());
		Assert.assertTrue(members.get(0).isCurriculumElementOwner());
		Assert.assertFalse(members.get(0).isRepositoryEntryOwner());
		Assert.assertFalse(members.get(0).isCoach());
		Assert.assertFalse(members.get(0).isParticipant());
	}
	
	@Test
	public void getMembershipInfosAndHistory_elements() {
		Identity actor = JunitTestHelper.getDefaultActor();
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-6");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-6", "Curriculum for element", "Curriculum", false, null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-6", "6. Element",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculumelementowner, actor);
		dbInstance.commitAndCloseSession();
		
		CurriculumElementMembershipHistorySearchParameters params = new CurriculumElementMembershipHistorySearchParameters();
		params.setElements(List.of(element));
		List<CurriculumElementMembershipHistory> memberships = curriculumElementDao.getMembershipInfosAndHistory(params);
		Assert.assertNotNull(memberships);
		Assert.assertEquals(1, memberships.size());

		CurriculumElementMembershipHistory membership = memberships.get(0);
		Assert.assertEquals(element.getKey(), membership.getCurriculumElementKey());
		Assert.assertEquals(supervisor.getKey(), membership.getIdentityKey());
		
		Assert.assertEquals(1, membership.getHistory().size());
		GroupMembershipHistory historyPoint = membership.getHistory().get(0);
		Assert.assertEquals(CurriculumRoles.curriculumelementowner.name(), historyPoint.getRole());
		Assert.assertEquals(GroupMembershipStatus.active, historyPoint.getStatus());
		
		Assert.assertEquals(supervisor, historyPoint.getIdentity());
		Assert.assertEquals(actor, historyPoint.getCreator());
		Assert.assertEquals(element.getGroup(), historyPoint.getGroup());
	}
}
