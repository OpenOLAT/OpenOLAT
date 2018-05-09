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
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, curriculum);
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
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, curriculum);
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
		CurriculumElement element = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation", null, curriculum);
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
	

}
