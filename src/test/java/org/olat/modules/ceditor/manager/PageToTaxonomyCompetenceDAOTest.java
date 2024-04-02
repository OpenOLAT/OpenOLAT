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
package org.olat.modules.ceditor.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.PageToTaxonomyCompetence;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.BinderDAO;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyCompetenceDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserLifecycleManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 19.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class PageToTaxonomyCompetenceDAOTest extends OlatTestCase {
	
	private Identity identity;
	private Page page;
	private Taxonomy taxonomy;
	private TaxonomyLevel level1;
	private TaxonomyLevel level2;
	private TaxonomyLevel level3;
	private TaxonomyLevelType type1;
	private TaxonomyLevelType type2;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private PageService pageService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDAO;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDAO;
	@Autowired 
	private PageToTaxonomyCompetenceDAO pageToTaxonomyCompetenceDAO;

	@Before
	public void setUp() {
		this.identity = JunitTestHelper.createAndPersistIdentityAsRndUser("page-to-tax-1");
		
		BinderImpl binder = binderDao.createAndPersist("Binder p1", "A binder with a page", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		this.page = pageDao.createAndPersist("New page", "A brand new page.", null, null, true, reloadedSection, null);
		dbInstance.commitAndCloseSession();
		
		this.taxonomy = taxonomyService.createTaxonomy("taxonomy", "taxonomy", null, null);
		dbInstance.commitAndCloseSession();
		
		this.type1 = taxonomyService.createTaxonomyLevelType("T1", "Type 1", "", null, true, taxonomy);
		this.type2 = taxonomyService.createTaxonomyLevelType("T2", "Type 2", "", null, false, taxonomy);
		
		
		this.level1 = taxonomyLevelDAO.createTaxonomyLevel("l1", random(), "level1", null, null, null, null, type1, this.taxonomy);
		this.level2 = taxonomyLevelDAO.createTaxonomyLevel("l2", random(), "level2", null, null, null, null, type2, this.taxonomy);
		this.level3 = taxonomyLevelDAO.createTaxonomyLevel("l3", random(), "level3", null, null, null, null, null, this.taxonomy);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAndGetRelation() {
		TaxonomyCompetence competence = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level1, identity, null);
		PageToTaxonomyCompetence relation = pageToTaxonomyCompetenceDAO.createRelation(page, competence);
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getTaxonomyCompetence());
		Assert.assertNotNull(relation.getPage());
		
		List<TaxonomyCompetence> competences = pageService.getRelatedCompetences(page, true);
		
		Assert.assertNotNull(competences);
		Assert.assertNotNull(competences.get(0));
	}
	
	@Test
	public void deleteRelations() {
		TaxonomyCompetence competence1 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level1, identity, null);
		TaxonomyCompetence competence2 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level2, identity, null);
		TaxonomyCompetence competence3 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level3, identity, null);
		
		pageToTaxonomyCompetenceDAO.createRelation(page, competence1);
		pageToTaxonomyCompetenceDAO.createRelation(page, competence2);
		pageToTaxonomyCompetenceDAO.createRelation(page, competence3);
		
		List<TaxonomyCompetence> competences = pageService.getRelatedCompetences(page, true);
		
		Assert.assertEquals(3, competences.size());
		
		pageToTaxonomyCompetenceDAO.deleteRelation(page, competence1);
		competences = pageService.getRelatedCompetences(page, true);
		
		Assert.assertFalse(competences.contains(competence1));
		
		pageToTaxonomyCompetenceDAO.deleteRelation(page);
		competences = pageService.getRelatedCompetences(page, true);
		
		Assert.assertEquals(0, competences.size());
	}
	
	@Test
	public void deleteRelationsByType() {
		TaxonomyCompetence competence1 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level1, identity, null);
		TaxonomyCompetence competence2 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level2, identity, null);
		TaxonomyCompetence competence3 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level3, identity, null);
		
		pageToTaxonomyCompetenceDAO.createRelation(page, competence1);
		pageToTaxonomyCompetenceDAO.createRelation(page, competence2);
		pageToTaxonomyCompetenceDAO.createRelation(page, competence3);
		
		List<TaxonomyCompetence> competences = pageService.getRelatedCompetences(page, true);
		Assert.assertEquals(3, competences.size());
		
		pageToTaxonomyCompetenceDAO.deleteRelationsByLevelType(type1);
		pageToTaxonomyCompetenceDAO.deleteRelationsByLevelType(type2);
		
		competences = pageService.getRelatedCompetences(page, true);
		Assert.assertEquals(2, competences.size());
	}
	
	@Test
	public void deleteUser() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("page-to-tax-2");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("page-to-tax-3");
		BinderImpl binder = binderDao.createAndPersist("Binder p2", "A binder with a page", null, null);
		Section section = binderDao.createSection("Section", "First section", null, null, binder);
		dbInstance.commitAndCloseSession();
		
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist("New page", "A brand new short lived page.", null, null, true, reloadedSection, null);
		dbInstance.commitAndCloseSession();
		
		Taxonomy taxonomy = taxonomyService.createTaxonomy("taxonomy", "taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDAO.createTaxonomyLevel("l1", random(), "level1", null, null, null, null, null, taxonomy);
		TaxonomyCompetence competence1 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level, id1, null);
		pageToTaxonomyCompetenceDAO.createRelation(page, competence1);
		TaxonomyCompetence competence2 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level, id2, null);
		pageToTaxonomyCompetenceDAO.createRelation(page, competence2);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyCompetence> competences = pageService.getRelatedCompetences(page, true);
		Assert.assertEquals(2, competences.size());
		
		// delete the user
		boolean deleted = userLifecycleManager.deleteIdentity(id1, null);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deleted);
		
		List<TaxonomyCompetence> competencesAfter = pageService.getRelatedCompetences(page, true);
		Assert.assertEquals(1, competencesAfter.size());
	}
	
}
