package org.olat.repository.manager;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.manager.CurriculumDAO;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryTemplateToGroupRelation;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryTemplateRelationDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private RepositoryTemplateRelationDAO repositoryTemplateRelationDao;
	
	@Test
	public void createRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-template-1", "Curriculum for template", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Template-1", "1. Template",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		Assert.assertNotNull(element);

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryTemplateToGroupRelation rel = repositoryTemplateRelationDao.createRelation(element.getGroup(), re);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(rel);
		Assert.assertNotNull(rel.getKey());
		Assert.assertNotNull(rel.getCreationDate());
		Assert.assertEquals(re, rel.getEntry());
		Assert.assertEquals(element.getGroup(), rel.getGroup());	
	}
	
	@Test
	public void hasRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-template-2", "Curriculum for template", "Curriculum", false, null);
		CurriculumElement element1 = curriculumElementDao.createCurriculumElement("Template-2-1", "2.1 Template",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumElementDao.createCurriculumElement("Template-2-2", "2.2 Template",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryTemplateToGroupRelation rel = repositoryTemplateRelationDao.createRelation(element1.getGroup(), re);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rel);
		
		boolean hasRelation = repositoryTemplateRelationDao.hasRelation(element1.getGroup(), re);
		Assert.assertTrue(hasRelation);
		
		boolean hasNotRelation = repositoryTemplateRelationDao.hasRelation(element2.getGroup(), re);
		Assert.assertFalse(hasNotRelation);
	}
	
	@Test
	public void hasRelations() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-template-3", "Curriculum for template", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Template-3", "3.0 Template",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryTemplateToGroupRelation rel = repositoryTemplateRelationDao.createRelation(element.getGroup(), re);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rel);
		
		boolean hasRelation = repositoryTemplateRelationDao.hasRelations(re);
		Assert.assertTrue(hasRelation);
	}
	
	@Test
	public void hasNoRelations() {
		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		boolean hasNoRelation = repositoryTemplateRelationDao.hasRelations(re);
		Assert.assertFalse(hasNoRelation);
	}
	
	@Test
	public void removeRelation() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-template-3", "Curriculum for template", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Template-3", "3.0 Template",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryTemplateToGroupRelation rel = repositoryTemplateRelationDao.createRelation(element.getGroup(), re);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rel);
		
		boolean hasRelation = repositoryTemplateRelationDao.hasRelations(re);
		Assert.assertTrue(hasRelation);
		
		// Remove relation
		int removed = repositoryTemplateRelationDao.removeRelation(element.getGroup(), re);
		Assert.assertEquals(1, removed);
		dbInstance.commitAndCloseSession();
		
		boolean hasNoRelation = repositoryTemplateRelationDao.hasRelations(re);
		Assert.assertFalse(hasNoRelation);
	}
	
	@Test
	public void deleteRelations() {
		Curriculum curriculum = curriculumDao.createAndPersist("Cur-for-template-3", "Curriculum for template", "Curriculum", false, null);
		CurriculumElement element = curriculumElementDao.createCurriculumElement("Template-3", "3.0 Template",
				CurriculumElementStatus.active, new Date(), new Date(), null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);

		RepositoryEntry re = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryTemplateToGroupRelation rel = repositoryTemplateRelationDao.createRelation(element.getGroup(), re);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(rel);
		
		boolean hasRelation = repositoryTemplateRelationDao.hasRelations(re);
		Assert.assertTrue(hasRelation);
		
		// Delete all relations of the entry
		int deleted = repositoryTemplateRelationDao.deleteRelations(re);
		Assert.assertEquals(1, deleted);
		dbInstance.commitAndCloseSession();
		
		boolean hasNoRelation = repositoryTemplateRelationDao.hasRelations(re);
		Assert.assertFalse(hasNoRelation);
	}
	
	

}
