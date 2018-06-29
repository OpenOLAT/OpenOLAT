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
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumMember;
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
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element", new Date(), new Date(), null, type, curriculum);
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
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element", new Date(), new Date(), null, type, curriculum);
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
	public void loadElements_curricullum() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-6", "Curriculum for element", "Curriculum", null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-6", "6.1 Element", null, null, null, null, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-6", "6.1.1 Element", null, null, element1, null, curriculum);
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-6", "6.2 Element", null, null, null, null, curriculum);
		dbInstance.commitAndCloseSession();
		
		//load all elements of the curriculum
		List<CurriculumElement> elements = curriculumElementDao.loadElements(curriculum);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(elements);
		Assert.assertEquals(3, elements.size());
		Assert.assertTrue(elements.contains(element1));
		Assert.assertTrue(elements.contains(element2));
		Assert.assertTrue(elements.contains(element3));
	}
	
	@Test
	public void loadElements_repoEntry() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, curriculum);
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
	public void createCurriculumElementParentChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-3", "Curriculum for element", "Curriculum", null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-3", "3. Element", null, null,  null, null, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-3-1", "3.1 Element", null, null, parentElement, null, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-3-2", "3.2 Element", null, null, parentElement, null, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-3-3", "3.3 Element", null, null, parentElement, null, curriculum);
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
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-4", "4. Element", null, null, null, null, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-4-1", "4.1 Element", null, null, parentElement, null, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-4-2", "4.1.1 Element", null, null, element1, null, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-4-3", "4.1.1.1 Element", null, null, element2, null, curriculum);
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
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, "6.1 Element", null, null, null, null, curriculum);
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
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-5", "5. Element", null, null, null, null, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-5-1", "5.1 Element", null, null, parentElement, null, curriculum);
		dbInstance.commit();
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-5-1-1", "5.1.1 Element", null, null, element1, null, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-5-2", "5.2 Element", null, null, parentElement, null, curriculum);
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
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-15", "15. Element", null, null, null, null, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-15-1", "15.1 Element", null, null, parentElement, null, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-15-2", "15.2 Element", null, null, parentElement, null, curriculum);
		dbInstance.commit();
		CurriculumElement element2_1 = curriculumElementDao.createCurriculumElement("Element-15-2-1", "15.2.1 Element", null, null, element2, null, curriculum);
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
		CurriculumElement rootElement = curriculumElementDao.createCurriculumElement("Element-7", "7. Element", null, null, null, null, curriculum);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-7-1", "7.1 Element", null, null, rootElement, null, curriculum);
		CurriculumElement element1_1 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element", null, null, element1, null, curriculum);
		CurriculumElement element1_1_1 = curriculumElementDao.createCurriculumElement("Element-7-1-1", "7.1.1 Element", null, null, element1_1, null, curriculum);
		CurriculumElement element1_1_2 = curriculumElementDao.createCurriculumElement("Element-7-1-2", "7.1.2 Element", null, null, element1_1, null, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-7-2", "7.2 Element", null, null, rootElement, null, curriculum);
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
	public void getMembers() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element", null, null, null, null, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumMember> members = curriculumElementDao.getMembers(element);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		CurriculumMember member = members.get(0);
		Assert.assertEquals(supervisor, member.getIdentity());
		Assert.assertEquals(CurriculumRoles.curriculummanager.name(), member.getRole());
	}
	
	@Test
	public void getMembersIdentity() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element", null, null, null, null, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.curriculummanager);
		dbInstance.commitAndCloseSession();
		
		List<Identity> members = curriculumElementDao.getMembersIdentity(element, CurriculumRoles.curriculummanager.name());
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		Assert.assertEquals(supervisor, members.get(0));
	}
	
	@Test
	public void getMembershipInfos_elements() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element", null, null, null, null, curriculum);
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
		CurriculumElement element = curriculumService.createCurriculumElement("Element-5", "5. Element", null, null, null, null, curriculum);
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
