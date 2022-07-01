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
package org.olat.modules.docpool.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.docpool.DocumentPoolManager;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.manager.TaxonomyCompetenceDAO;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelTypeDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentPoolManagerTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private DocumentPoolModule documentPoolModule;
	@Autowired
	private DocumentPoolManager documentPoolManager;
	@Autowired
	private TaxonomyLevelTypeDAO taxonomyLevelTypeDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	
	@Before
	public void setupTaxonomy() {
		Taxonomy taxonomy = null;
		String taxonomyTreeKey = documentPoolModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			taxonomy = taxonomyDao.loadByKey(new Long(taxonomyTreeKey));
		}
		
		if(taxonomy == null) {
			taxonomy = taxonomyDao.createTaxonomy("DP-1", "Doc-pool", "Taxonomy for document pool", null);
			dbInstance.commitAndCloseSession();
			documentPoolModule.setTaxonomyTreeKey(taxonomy.getKey().toString());
		}
	}
	
	@Test
	public void hasCompetenceByTaxonomy_manage() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.manage);
		// set read for teach competence
		type.setDocumentsLibraryManageCompetenceEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_teachRead() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.teach);
		// set read for teach competence
		type.setDocumentsLibraryTeachCompetenceReadEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_teachWrite() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.teach);
		// set read for teach competence
		type.setDocumentsLibraryTeachCompetenceWriteEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_teach_negative() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.teach);
		// set read for teach competence
		type.setDocumentsLibraryManageCompetenceEnabled(true);
		type.setDocumentsLibraryHaveCompetenceReadEnabled(true);
		type.setDocumentsLibraryTargetCompetenceReadEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertFalse(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_have() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.have);
		// set read for teach competence
		type.setDocumentsLibraryHaveCompetenceReadEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_have_negative() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.have);
		// set read for teach competence
		type.setDocumentsLibraryTeachCompetenceReadEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertFalse(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_target() {
		// create a level and competence with a type teach
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		TaxonomyLevelType type = createTypeLevelCompetence(id, null, TaxonomyCompetenceTypes.target);
		// set read for teach competence
		type.setDocumentsLibraryTargetCompetenceReadEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();

		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasCompetence);
	}
	
	@Test
	public void hasCompetenceByTaxonomy_negative() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		String levelId = "DP-Lev. " + UUID.randomUUID();
		Taxonomy taxonomy = getDocumentPoolTaxonomy();
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(levelId, random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(level);
		
		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertFalse(hasCompetence);
	}
	
	/**
	 * The test check if the presence of a level with the identifier lost+found
	 * in the parent line stop the competence.
	 */
	@Test
	public void lostAndFoundSpecialCase() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("competent-8");
		Taxonomy taxonomy = getDocumentPoolTaxonomy();
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("DP-Lev. " + UUID.randomUUID(), random(), "Competence level", "A competence", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("DP-Lev. " + UUID.randomUUID(), random(), "Competence level", "A competence", null, null, level1, null, taxonomy);
		TaxonomyLevelType type = createTypeLevelCompetence(id, level2, TaxonomyCompetenceTypes.have);
		type.setDocumentsLibraryHaveCompetenceReadEnabled(true);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commitAndCloseSession();
		
		//check first without lost+found
		boolean hasCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasCompetence);
		
		//change the identifier level 2
		level2.setIdentifier("lost+found");
		level2 = taxonomyLevelDao.updateTaxonomyLevel(level2);
		dbInstance.commitAndCloseSession();
		
		//access refused
		boolean hasLostCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertFalse(hasLostCompetence);
		
		// remove lost + found
		level2.setIdentifier("DP-Lev. " + UUID.randomUUID());
		level2 = taxonomyLevelDao.updateTaxonomyLevel(level2);
		dbInstance.commitAndCloseSession();
		// access allowed
		boolean hasAgainCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertTrue(hasAgainCompetence);
		
		//root will be lost
		level1.setIdentifier("lost+found");
		level1 = taxonomyLevelDao.updateTaxonomyLevel(level1);
		dbInstance.commitAndCloseSession();
		boolean hasLostAgainCompetence = documentPoolManager.hasValidCompetence(id);
		Assert.assertFalse(hasLostAgainCompetence);
	}
	
	private TaxonomyLevelType createTypeLevelCompetence(Identity id, TaxonomyLevel parent, TaxonomyCompetenceTypes competenceType) {
		String levelId = "DP-Lev. " + UUID.randomUUID();
		Taxonomy taxonomy = getDocumentPoolTaxonomy();
		TaxonomyLevelType type = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-docpool", "A type for document pool", "Typed", "TYP-0", true, taxonomy);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(levelId, random(), "Competence level", "A competence", null, null, parent, type, taxonomy);
		TaxonomyCompetence competenceTarget = taxonomyCompetenceDao.createTaxonomyCompetence(competenceType, level, id, null);
		dbInstance.commit();
		type.setDocumentsLibraryManageCompetenceEnabled(false);
		type.setDocumentsLibraryTeachCompetenceWriteEnabled(false);
		type.setDocumentsLibraryTeachCompetenceReadEnabled(false);
		type.setDocumentsLibraryHaveCompetenceReadEnabled(false);
		type.setDocumentsLibraryTargetCompetenceReadEnabled(false);
		type = taxonomyLevelTypeDao.updateTaxonomyLevelType(type);
		dbInstance.commit();
		Assert.assertNotNull(competenceTarget);
		return type;
	}
	
	private Taxonomy getDocumentPoolTaxonomy() {
		String taxonomyTreeKey = documentPoolModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			return taxonomyDao.loadByKey(new Long(taxonomyTreeKey));
		}
		return null;
	}

}
