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
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
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
	
	@Test
	public void createCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-cur-el-1", "Type for", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element", new Date(), new Date(), null,
				type, CurriculumCalendars.disabled, curriculum);
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
	}
	
	@Test
	public void loadByKey() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-2", "Curriculum for element", "Curriculum", null);
		CurriculumElementType type = curriculumElementTypeDao.createCurriculumElementType("typ-for-cur-el-2", "Type for", "First element", "AC-234");
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element", new Date(), new Date(), null,
				type, CurriculumCalendars.disabled, curriculum);
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
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-2", "Curriculum for element", "Curriculum", null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element", new Date(), new Date(), null,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-2", "2. Element", new Date(), new Date(), null,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement otherElement = curriculumElementDao.createCurriculumElement("Element-2", "2. Element", new Date(), new Date(), null,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElement> elements = curriculumElementDao.loadByKeys(Arrays.asList(element1, element2));
		
		Assert.assertTrue(elements.contains(element1));
		Assert.assertTrue(elements.contains(element2));
		Assert.assertFalse(elements.contains(otherElement));
	}
	
	@Test
	public void loadElements_curricullum() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-6", "Curriculum for element", "Curriculum", null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-6", "6.1 Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-6", "6.1.1 Element", null, null, element1,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-6", "6.2 Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
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
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, CurriculumCalendars.disabled, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry1, true);
		curriculumService.addRepositoryEntry(element, entry2, true);
		dbInstance.commit();
		
		List<CurriculumElementInfos> relations = curriculumElementDao.loadElementsWithInfos(curriculum);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(element, relations.get(0).getCurriculumElement());
		Assert.assertEquals(element.getKey(), relations.get(0).getKey());
		Assert.assertEquals(2, relations.get(0).getNumOfResources());
	}
	
	@Test
	public void loadElements_repoEntry() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, CurriculumCalendars.disabled, curriculum);
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
		Curriculum curriculum1 = curriculumDao.createAndPersist("", "", null, null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("", "", null, null,  null,
				null, CurriculumCalendars.disabled, curriculum1);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("", "", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum1);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("", "", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum1);
		Curriculum curriculum2 = curriculumDao.createAndPersist("", "", null, null);
		CurriculumElement parentElement2 = curriculumElementDao.createCurriculumElement("", "", null, null,  null,
				null, CurriculumCalendars.disabled, curriculum2);
		Curriculum otherCurriculum = curriculumDao.createAndPersist("", "", null, null);
		CurriculumElement otherElement = curriculumElementDao.createCurriculumElement("", "", null, null,  null,
				null, CurriculumCalendars.disabled, otherCurriculum);
		dbInstance.commitAndCloseSession();
		
		Collection<CurriculumRef> curriculumRefs = Arrays.asList(curriculum1, curriculum2);
		 List<CurriculumElement> elements = curriculumElementDao.loadElementsByCurriculums(curriculumRefs);
		
		assertThat(elements)
				.containsExactlyInAnyOrder(parentElement, element1, element2, parentElement2)
				.doesNotContain(otherElement);
	}

	@Test
	public void createCurriculumElementParentChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-3", "Curriculum for element", "Curriculum", null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-3", "3. Element", null, null,  null,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-3-1", "3.1 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-3-2", "3.2 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-3-3", "3.3 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
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
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-4", "4. Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-4-1", "4.1 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-4-2", "4.1.1 Element", null, null, element1,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-4-3", "4.1.1.1 Element", null, null, element2,
				null, CurriculumCalendars.disabled, curriculum);
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
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-6", "Curriculum for element", "Curriculum", null);
		String externalId = UUID.randomUUID().toString();
		String identifier = UUID.randomUUID().toString();
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, "6.1 Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
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
	public void getDescendants() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-5", "Curriculum for element", "Curriculum", null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-5", "5. Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-5-1", "5.1 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-5-1-1", "5.1.1 Element", null, null, element1,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-5-2", "5.2 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
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
	public void getChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-15", "Curriculum for element", "Curriculum", null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-15", "15. Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-15-1", "15.1 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-15-2", "15.2 Element", null, null, parentElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commit();
		CurriculumElement element2_1 = curriculumElementDao.createCurriculumElement("Element-15-2-1", "15.2.1 Element", null, null, element2,
				null, CurriculumCalendars.disabled, curriculum);
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
	public void moveCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-7", "Curriculum for element", "Curriculum", null);
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-7", "7. Element", null, null, null,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-7-1", "7.1 Element", null, null, rootElement,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element", null, null, element1,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element1_1_1 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element", null, null, element1_1,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element1_1_2 = curriculumElementDao.createCurriculumElement("Element-7-1-2", "7.1.2 Element", null, null, element1_1,
				null, CurriculumCalendars.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-7-2", "7.2 Element", null, null, rootElement,
				null, CurriculumCalendars.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		// move element1_1 under element2
		curriculumElementDao.move(element1_1, element2);
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
	public void getMembersIdentity() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element", null, null, null, null, CurriculumCalendars.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		List<Identity> members = curriculumElementDao.getMembersIdentity(element, CurriculumRoles.curriculummanager.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor, members.get(0));
	}
	
	@Test
	public void getMemberKeys() {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-24", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-24", "4. Element", null, null, null, null, CurriculumCalendars.disabled, curriculum);
		curriculumService.addMember(element, coach, CurriculumRoles.coach);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		List<Long> members = curriculumElementDao.getMemberKeys(elements, CurriculumRoles.coach.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(coach.getKey(), members.get(0));
	}
	
	@Test
	public void getMembershipInfos_elements() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element", null, null, null, null, CurriculumCalendars.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementMembership> members = curriculumElementDao.getMembershipInfos(null, Collections.singletonList(element), supervisor);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor.getKey(), members.get(0).getIdentityKey());
		Assert.assertEquals(element.getKey(), members.get(0).getCurriculumElementKey());
		Assert.assertTrue(members.get(0).isCurriculumManager());
		Assert.assertFalse(members.get(0).isRepositoryEntryOwner());
		Assert.assertFalse(members.get(0).isCoach());
		Assert.assertFalse(members.get(0).isParticipant());
	}
	
	@Test
	public void getMembershipInfos_curriculum() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-5", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "5. Element", null, null, null, null, CurriculumCalendars.disabled, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementMembership> members = curriculumElementDao.getMembershipInfos(curriculum, null, supervisor);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor.getKey(), members.get(0).getIdentityKey());
		Assert.assertEquals(element.getKey(), members.get(0).getCurriculumElementKey());
		Assert.assertTrue(members.get(0).isCurriculumManager());
		Assert.assertFalse(members.get(0).isRepositoryEntryOwner());
		Assert.assertFalse(members.get(0).isCoach());
		Assert.assertFalse(members.get(0).isParticipant());
	}
}
