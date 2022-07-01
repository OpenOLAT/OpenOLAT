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
package org.olat.modules.taxonomy.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyCompetenceDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	
	@Test
	public void createTaxonomyCompetence() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-1");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-24", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level", "A very difficult competence", null, null, null, null, taxonomy);
		TaxonomyCompetence competence = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level, id, null);
		dbInstance.commit();
		
		Assert.assertNotNull(competence);
		Assert.assertNotNull(competence.getCreationDate());
	}
	
	@Test
	public void createAndReloadTaxonomyCompetence() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-1");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-24b", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1b", random(), "Competence level", "An important competence to have", null, null, null, null, taxonomy);
		TaxonomyCompetence competence = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level, id, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(competence);
		
		TaxonomyCompetence reloadedCompetence = taxonomyCompetenceDao.loadCompetenceByKey(competence.getKey());
		Assert.assertNotNull(reloadedCompetence);
		Assert.assertEquals(competence, reloadedCompetence);
		Assert.assertEquals(competence.getKey(), reloadedCompetence.getKey());
		Assert.assertEquals(id, reloadedCompetence.getIdentity());
		Assert.assertEquals(level, reloadedCompetence.getTaxonomyLevel());
		Assert.assertEquals(TaxonomyCompetenceTypes.have, reloadedCompetence.getCompetenceType());
	}
	
	@Test
	public void getCompetenceByLevel() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-2");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-25", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		TaxonomyCompetence competence = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level, id, null);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyCompetence> loadedCompetences = taxonomyCompetenceDao.getCompetenceByLevel(level);
		Assert.assertNotNull(loadedCompetences);
		Assert.assertEquals(1, loadedCompetences.size());
		TaxonomyCompetence loadedCompetence = loadedCompetences.get(0);
		Assert.assertNotNull(loadedCompetence);
		Assert.assertEquals(competence, loadedCompetence);
		Assert.assertEquals(level, competence.getTaxonomyLevel());
		Assert.assertEquals(id, competence.getIdentity());
		Assert.assertEquals(TaxonomyCompetenceTypes.target, competence.getCompetenceType());
	}
	
	@Test
	public void getCompetenceByLevel_withIdentity() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-3");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-4");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-25", "Competence", "", null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		
		TaxonomyCompetence competence1_1 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level1, id1, null);
		TaxonomyCompetence competence1_2 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level1, id2, null);
		TaxonomyCompetence competence2_1 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level2, id1, null);
		TaxonomyCompetence competence2_2 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level2, id2, null);
		dbInstance.commitAndCloseSession();
		
		// check the different possiblities
		List<TaxonomyCompetence> loadedCompetences1_1 = taxonomyCompetenceDao.getCompetenceByLevel(level1, id1);
		Assert.assertNotNull(loadedCompetences1_1);
		Assert.assertEquals(1, loadedCompetences1_1.size());
		Assert.assertEquals(competence1_1,  loadedCompetences1_1.get(0));
		
		List<TaxonomyCompetence> loadedCompetences1_2 = taxonomyCompetenceDao.getCompetenceByLevel(level1, id2);
		Assert.assertNotNull(loadedCompetences1_2);
		Assert.assertEquals(1, loadedCompetences1_2.size());
		Assert.assertEquals(competence1_2,  loadedCompetences1_2.get(0));
		
		List<TaxonomyCompetence> loadedCompetences2_1 = taxonomyCompetenceDao.getCompetenceByLevel(level2, id1);
		Assert.assertNotNull(loadedCompetences2_1);
		Assert.assertEquals(1, loadedCompetences2_1.size());
		Assert.assertEquals(competence2_1,  loadedCompetences2_1.get(0));
		
		List<TaxonomyCompetence> loadedCompetences2_2 = taxonomyCompetenceDao.getCompetenceByLevel(level2, id2);
		Assert.assertNotNull(loadedCompetences2_2);
		Assert.assertEquals(1, loadedCompetences2_2.size());
		Assert.assertEquals(competence2_2,  loadedCompetences2_2.get(0));
	}
	
	@Test
	public void getCompetenceByTaxonomy() {
		//make 2 taxonomy trees
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-5");
		Taxonomy taxonomy1 = taxonomyDao.createTaxonomy("ID-27", "Competence", "", null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level taxonomy 1", "A competence", null, null, null, null, taxonomy1);
		TaxonomyCompetence competence1 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level1, id, null);
		
		Taxonomy taxonomy2 = taxonomyDao.createTaxonomy("ID-28", "Competence", "", null);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-2", random(), "Competence level taxonomy 2", "A competence", null, null, null, null, taxonomy2);
		TaxonomyCompetence competence2 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level2, id, null);
		dbInstance.commitAndCloseSession();
		
		//check the competences of the 2 taxonomy trees
		List<TaxonomyCompetence> loadedCompetences1 = taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy1, id, new Date());
		Assert.assertNotNull(loadedCompetences1);
		Assert.assertEquals(1, loadedCompetences1.size());
		Assert.assertEquals(competence1, loadedCompetences1.get(0));

		List<TaxonomyCompetence> loadedCompetences2 = taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy2, id, new Date());
		Assert.assertNotNull(loadedCompetences2);
		Assert.assertEquals(1, loadedCompetences2.size());
		Assert.assertEquals(competence2, loadedCompetences2.get(0));
	}
	
	@Test
	public void getCompetenceByTaxonomyAndCompetenceTypes() {
		//make 2 taxonomy trees
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-5");
		Taxonomy taxonomy1 = taxonomyDao.createTaxonomy("ID-31", "Competence", "", null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", random(), "Competence level taxonomy 1", "A competence", null, null, null, null, taxonomy1);
		TaxonomyCompetence competence1 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level1, id, null);
		
		Taxonomy taxonomy2 = taxonomyDao.createTaxonomy("ID-32", "Competence", "", null);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-2", random(), "Competence level taxonomy 2", "A competence", null, null, null, null, taxonomy2);
		TaxonomyCompetence competence2 = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level2, id, null);
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, level2, id, null);
		Identity otherId = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-5");
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level2, otherId, null);
		dbInstance.commitAndCloseSession();
		
		//check the competences of the 2 taxonomy trees
		List<TaxonomyCompetence> loadedCompetences1 = taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy1, id, new Date(), TaxonomyCompetenceTypes.target);
		Assert.assertNotNull(loadedCompetences1);
		Assert.assertEquals(1, loadedCompetences1.size());
		Assert.assertEquals(competence1, loadedCompetences1.get(0));
		loadedCompetences1 = taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy1, id, new Date(), TaxonomyCompetenceTypes.have);
		Assert.assertEquals(0, loadedCompetences1.size());

		List<TaxonomyCompetence> loadedCompetences2 = taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy2, id, new Date(), TaxonomyCompetenceTypes.target);
		Assert.assertNotNull(loadedCompetences2);
		Assert.assertEquals(1, loadedCompetences2.size());
		Assert.assertEquals(competence2, loadedCompetences2.get(0));
		loadedCompetences2 = taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy2, id, new Date(), TaxonomyCompetenceTypes.manage, TaxonomyCompetenceTypes.teach);
		Assert.assertEquals(0, loadedCompetences2.size());
	}
	
	@Test
	public void getCompetences_identityAndTypes() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-6");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-6");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel levelA = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		TaxonomyLevel levelB = taxonomyLevelDao.createTaxonomyLevel("ID-Level-B", random(), "Competence level", "B competence", null, null, null, null, taxonomy);
		
		TaxonomyCompetence competenceTarget = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, levelA, id1, null);
		TaxonomyCompetence competenceHave = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, levelB, id1, null);
		TaxonomyCompetence competenceTeach = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, levelB, id2, null);
		
		dbInstance.commitAndCloseSession();
		
		//check the competences
		List<TaxonomyCompetence> loadedTargetCompetences1 = taxonomyCompetenceDao.getCompetences(id1, TaxonomyCompetenceTypes.target);
		Assert.assertNotNull(loadedTargetCompetences1);
		Assert.assertEquals(1, loadedTargetCompetences1.size());
		Assert.assertEquals(competenceTarget, loadedTargetCompetences1.get(0));
		
		//have
		List<TaxonomyCompetence> loadedTargetHaveCompetences1 = taxonomyCompetenceDao
				.getCompetences(id1, TaxonomyCompetenceTypes.target, TaxonomyCompetenceTypes.have, TaxonomyCompetenceTypes.teach);
		Assert.assertNotNull(loadedTargetHaveCompetences1);
		Assert.assertEquals(2, loadedTargetHaveCompetences1.size());
		Assert.assertTrue(loadedTargetHaveCompetences1.contains(competenceTarget));
		Assert.assertTrue(loadedTargetHaveCompetences1.contains(competenceHave));

		//all
		List<TaxonomyCompetence> loadedAllCompetences2 = taxonomyCompetenceDao.getCompetences(id2);
		Assert.assertNotNull(loadedAllCompetences2);
		Assert.assertEquals(1, loadedAllCompetences2.size());
		Assert.assertEquals(competenceTeach, loadedAllCompetences2.get(0));
	}
	
	@Test
	public void hasCompetenceByLevel() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);

		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level, id, null);
		dbInstance.commitAndCloseSession();
		
		boolean hasCompetence = taxonomyCompetenceDao.hasCompetenceByLevel(level, id, new Date(), TaxonomyCompetenceTypes.target);
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByLevel_hasOnlyOtherCompetence() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);

		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level, id, null);
		dbInstance.commitAndCloseSession();
		
		boolean hasCompetence = taxonomyCompetenceDao.hasCompetenceByLevel(level, id, new Date(), TaxonomyCompetenceTypes.teach);
		Assert.assertFalse(hasCompetence);
	}
		
	@Test
	public void hasCompetenceByLevel_hasNoCompetence() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		boolean hasCompetence = taxonomyCompetenceDao.hasCompetenceByLevel(level, id, new Date(), TaxonomyCompetenceTypes.teach);
		Assert.assertFalse(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);

		TaxonomyCompetence competenceTarget = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level, id, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(competenceTarget);
		
		boolean hasCompetence = taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, id, new Date());
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_negative() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(level);
		
		boolean hasCompetence = taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, id, new Date());
		Assert.assertFalse(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_competence() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy);

		TaxonomyCompetence competenceTarget = taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, level, id, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(competenceTarget);
		
		boolean hasTarget = taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, id, new Date(), TaxonomyCompetenceTypes.target);
		Assert.assertTrue(hasTarget);
		boolean hasTeach = taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, id, new Date(), TaxonomyCompetenceTypes.teach);
		Assert.assertFalse(hasTeach);
	}
	
	@Test
	public void deleteCompetencesByIdentity() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-6");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-6");
		Taxonomy taxonomy1 = taxonomyDao.createTaxonomy("ID-30", "Competence", "", null);
		Taxonomy taxonomy2 = taxonomyDao.createTaxonomy("ID-31", "Competence", "", null);
		TaxonomyLevel levelA = taxonomyLevelDao.createTaxonomyLevel("ID-Level-A", random(), "Competence level", "A competence", null, null, null, null, taxonomy1);
		TaxonomyLevel levelB = taxonomyLevelDao.createTaxonomyLevel("ID-Level-B", random(), "Competence level", "B competence", null, null, null, null, taxonomy1);
		TaxonomyLevel levelC = taxonomyLevelDao.createTaxonomyLevel("ID-Level-C", random(), "Competence level", "C competence", null, null, null, null, taxonomy2);
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, levelA, id1, null);
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.have, levelB, id1, null);
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, levelC, id1, null);
		taxonomyCompetenceDao.createTaxonomyCompetence(TaxonomyCompetenceTypes.target, levelA, id2, null);
		dbInstance.commitAndCloseSession();
		
		int deleteCompetences = taxonomyCompetenceDao.deleteCompetences(id1);
		
		Assert.assertEquals(3, deleteCompetences);
		List<TaxonomyCompetence> competencesID1 = taxonomyCompetenceDao.getCompetences(id1);
		Assert.assertEquals(0, competencesID1.size());
		List<TaxonomyCompetence> competencesID2 = taxonomyCompetenceDao.getCompetences(id2);
		Assert.assertEquals(1, competencesID2.size());
	}
}
