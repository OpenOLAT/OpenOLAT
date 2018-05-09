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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementMember;
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
	private CurriculumService curriculumService;
	
	@Test
	public void createCurriculumElement() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-1", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-1", "1. Element", null, curriculum);
		Assert.assertNotNull(element);
		dbInstance.commitAndCloseSession();
		
		//check
		Assert.assertNotNull(element.getKey());
		Assert.assertNotNull(element.getCreationDate());
		Assert.assertNotNull(element.getLastModified());
		Assert.assertEquals("Element-1", element.getIdentifier());
		Assert.assertEquals("1. Element", element.getDisplayName());
		Assert.assertEquals(curriculum, element.getCurriculum());
	}
	
	@Test
	public void loadByKey() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-el-2", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Element-2", "2. Element", null, curriculum);
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
		Assert.assertEquals("Element-2", reloadedElement.getIdentifier());
		Assert.assertEquals("2. Element", reloadedElement.getDisplayName());
		Assert.assertEquals(curriculum, reloadedElement.getCurriculum());
	}

	@Test
	public void createCurriculumElementParentChildren() {
		Curriculum curriculum = curriculumDao.createAndPersist("cur-for-el-3", "Curriculum for element", "Curriculum", null);
		CurriculumElement parentElement = curriculumElementDao.createCurriculumElement("Element-3", "3. Element", null, curriculum);
		dbInstance.commit();
		// save 3 children
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Element-3-1", "3.1 Element", parentElement, curriculum);
		dbInstance.commit();
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Element-3-2", "3.2 Element", parentElement, curriculum);
		dbInstance.commit();
		CurriculumElement element3 = curriculumElementDao.createCurriculumElement("Element-3-3", "3.3 Element", parentElement, curriculum);
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
	public void getMembers() {
		Identity supervisor = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-supervisor-1");
		Curriculum curriculum = curriculumService.createCurriculum("cur-for-el-4", "Curriculum for element", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-4", "4. Element", null, curriculum);
		curriculumService.addMember(element, supervisor, CurriculumRoles.supervisor);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumElementMember> members = curriculumElementDao.getMembers(element);
		Assert.assertNotNull(members);
		Assert.assertEquals(1, members.size());
		CurriculumElementMember member = members.get(0);
		Assert.assertEquals(supervisor, member.getIdentity());
		Assert.assertEquals(CurriculumRoles.supervisor.name(), member.getRole());
	}

}
