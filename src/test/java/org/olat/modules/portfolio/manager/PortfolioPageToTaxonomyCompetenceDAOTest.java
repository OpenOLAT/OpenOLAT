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
package org.olat.modules.portfolio.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioPageToTaxonomyCompetence;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 19.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class PortfolioPageToTaxonomyCompetenceDAOTest extends OlatTestCase {
	
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
	private PortfolioPageToTaxonomyCompetenceDAO portfolioPageToTaxonomyCompetenceDAO;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDAO;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDAO;

	@Before
	public void setUp() {
		this.identity = JunitTestHelper.createAndPersistIdentityAsRndUser("portfolioPageToTaxonomyCompetenceTest");
		
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
		
		
		this.level1 = taxonomyLevelDAO.createTaxonomyLevel("l1", "level1", null, null, null, null, type1, this.taxonomy);
		this.level2 = taxonomyLevelDAO.createTaxonomyLevel("l2", "level2", null, null, null, null, type2, this.taxonomy);
		this.level3 = taxonomyLevelDAO.createTaxonomyLevel("l3", "level3", null, null, null, null, null, this.taxonomy);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAndGetRelation() {
		TaxonomyCompetence competence = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level1, identity, null);
		PortfolioPageToTaxonomyCompetence relation = portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence);
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getTaxonomyCompetence());
		Assert.assertNotNull(relation.getPortfolioPage());
		
		List<TaxonomyCompetence> competences = portfolioService.getRelatedCompetences(page, true);
		
		Assert.assertNotNull(competences);
		Assert.assertNotNull(competences.get(0));
	}
	
	@Test
	public void deleteRelations() {
		TaxonomyCompetence competence1 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level1, identity, null);
		TaxonomyCompetence competence2 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level2, identity, null);
		TaxonomyCompetence competence3 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level3, identity, null);
		
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence1);
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence2);
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence3);
		
		List<TaxonomyCompetence> competences = portfolioService.getRelatedCompetences(page, true);
		
		Assert.assertEquals(3, competences.size());
		
		portfolioPageToTaxonomyCompetenceDAO.deleteRelation(page, competence1);
		competences = portfolioService.getRelatedCompetences(page, true);
		
		Assert.assertFalse(competences.contains(competence1));
		
		portfolioPageToTaxonomyCompetenceDAO.deleteRelation(page);
		competences = portfolioService.getRelatedCompetences(page, true);
		
		Assert.assertEquals(0, competences.size());
	}
	
	@Test
	public void deleteRelationsByType() {
		TaxonomyCompetence competence1 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level1, identity, null);
		TaxonomyCompetence competence2 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level2, identity, null);
		TaxonomyCompetence competence3 = taxonomyCompetenceDAO.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level3, identity, null);
		
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence1);
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence2);
		portfolioPageToTaxonomyCompetenceDAO.createRelation(page, competence3);
		
		List<TaxonomyCompetence> competences = portfolioService.getRelatedCompetences(page, true);
		Assert.assertEquals(3, competences.size());
		
		portfolioPageToTaxonomyCompetenceDAO.deleteRelationsByLevelType(type1);
		portfolioPageToTaxonomyCompetenceDAO.deleteRelationsByLevelType(type2);
		
		competences = portfolioService.getRelatedCompetences(page, true);
		Assert.assertEquals(2, competences.size());
	}
}
