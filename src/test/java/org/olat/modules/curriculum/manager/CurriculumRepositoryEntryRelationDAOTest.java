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
import org.olat.modules.curriculum.CurriculumRepositoryEntryRelation;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
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
	
	@Test
	public void createRelation() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-1", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		CurriculumRepositoryEntryRelation relation = curriculumRepositoryEntryRelationDao.createRelation(entry, element, true);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertNotNull(relation.getLastModified());
		Assert.assertEquals(element, relation.getCurriculumElement());
		Assert.assertEquals(entry, relation.getEntry());
		Assert.assertTrue(relation.isMaster());
	}
	
	@Test
	public void getRepositoryEntries() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = curriculumRepositoryEntryRelationDao.getRepositoryEntries(element);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entry, entries.get(0));
	}
	
	@Test
	public void getCurriculumElements() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-2", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, curriculum);
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
	public void getRelations() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-3", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		List<CurriculumRepositoryEntryRelation> relations = curriculumRepositoryEntryRelationDao.getRelations(entry, element);
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		Assert.assertEquals(element, relations.get(0).getCurriculumElement());
		Assert.assertEquals(entry, relations.get(0).getEntry());
	}
	
	@Test
	public void deleteRelations() {
		Curriculum curriculum = curriculumService.createCurriculum("cur-el-rel-4", "Curriculum for relation", "Curriculum", null);
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, null, null, null, curriculum);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("cur-el-re-auth");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commit();
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		// check if the relation is really there.
		List<CurriculumRepositoryEntryRelation> relations = curriculumRepositoryEntryRelationDao.getRelations(entry, element);
		Assert.assertEquals(1, relations.size());
		
		// delete the relations
		curriculumRepositoryEntryRelationDao.deleteRelation(entry, element);
		dbInstance.commitAndCloseSession();
		
		// check that there isn't a relation left
		List<CurriculumRepositoryEntryRelation> deletedRelations = curriculumRepositoryEntryRelationDao.getRelations(entry, element);
		Assert.assertTrue(deletedRelations.isEmpty());
	}
}
