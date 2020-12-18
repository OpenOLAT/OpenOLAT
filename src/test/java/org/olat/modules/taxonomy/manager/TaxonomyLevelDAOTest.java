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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyLevelTypeDAO taxonomyLevelTypeDao;
	
	@Test
	public void createTaxonomyLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-98", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commit();
		
		Assert.assertNotNull(level);
		Assert.assertNotNull(level.getKey());
		Assert.assertNotNull(level.getCreationDate());
		Assert.assertNotNull(level.getTaxonomy());
		Assert.assertEquals(taxonomy, level.getTaxonomy());
	}

	@Test
	public void createAndLoadTaxonomyLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-100", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "A taxonomy level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		TaxonomyLevel reloadedLevel = taxonomyLevelDao.loadByKey(level.getKey());
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(reloadedLevel);
		Assert.assertNotNull(reloadedLevel.getKey());
		Assert.assertEquals(level, reloadedLevel);
		Assert.assertEquals(taxonomy, reloadedLevel.getTaxonomy());
	}
	
	@Test
	public void createAndLoadTaxonomyLevel_withType() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-103", "Typed taxonomy", null, null);
		TaxonomyLevelType type = taxonomyLevelTypeDao.createTaxonomyLevelType("Type-t", "A type", "Typed", "TYP-T", taxonomy);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", "A taxonomy level", "A basic level", null, null, null, type, taxonomy);
		dbInstance.commitAndCloseSession();
		
		TaxonomyLevel reloadedLevel = taxonomyLevelDao.loadByKey(level.getKey());
		dbInstance.commitAndCloseSession();

		Assert.assertNotNull(reloadedLevel);
		Assert.assertNotNull(reloadedLevel.getKey());
		Assert.assertEquals(level, reloadedLevel);
		Assert.assertEquals(taxonomy, reloadedLevel.getTaxonomy());
		Assert.assertEquals(type, reloadedLevel.getType());
	}

	@Test
	public void createTaxonomyCompetence_2Level() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-99", "Leveled taxonomy", null, null);
		TaxonomyLevel rootLevel = taxonomyLevelDao.createTaxonomyLevel("ID-Level-1", "My root level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commit();
		
		TaxonomyLevel secondLevel = taxonomyLevelDao.createTaxonomyLevel("ID-Level-2", "My second level", "A basic level", null, null, rootLevel, null, taxonomy);
		dbInstance.commit();
		
		TaxonomyLevel reloadedSecondLevel = taxonomyLevelDao.loadByKey(secondLevel.getKey());
		
		Assert.assertNotNull(reloadedSecondLevel);
		Assert.assertNotNull(reloadedSecondLevel.getKey());
		Assert.assertNotNull(reloadedSecondLevel.getCreationDate());
		Assert.assertEquals(taxonomy, reloadedSecondLevel.getTaxonomy());
		Assert.assertEquals(rootLevel, reloadedSecondLevel.getParent());
	}
	
	@Test
	public void loadTaxonomyLevels_byKeys() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), null, null, null, null, taxonomy);
		TaxonomyLevel levelOther = taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), null, null, null, null, taxonomy);
		dbInstance.commit();
		
		List<TaxonomyLevel> levels = taxonomyLevelDao.loadLevels(List.of(level1, level2));
		assertThat(levels)
				.containsExactlyInAnyOrder(level1, level2)
				.doesNotContain(levelOther);
	}
	
	@Test
	public void getTaxonomyLevels_byTaxonomy() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-99", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-3", "A level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commit();
		
		List<TaxonomyLevel> levels = taxonomyLevelDao.getLevels(taxonomy);
		Assert.assertNotNull(levels);
		Assert.assertTrue(levels.contains(level));
	}

	@Test
	public void getTaxonomyLevels_all() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-103", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-4", "A level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commit();
		
		List<TaxonomyLevel> levels = taxonomyLevelDao.getLevels(null);
		Assert.assertNotNull(levels);
		Assert.assertTrue(levels.contains(level));
	}
	
	@Test
	public void getParentLine() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-103", "Deeply leveled taxonomy", null, null);
		TaxonomyLevel level_1 = taxonomyLevelDao.createTaxonomyLevel("L-1", "A level", "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level_2 = taxonomyLevelDao.createTaxonomyLevel("L-2", "A level", "A basic level", null, null, level_1, null, taxonomy);
		TaxonomyLevel level_3 = taxonomyLevelDao.createTaxonomyLevel("L-3", "A level", "A basic level", null, null, level_2, null, taxonomy);
		TaxonomyLevel level_4 = taxonomyLevelDao.createTaxonomyLevel("L-4", "A level", "A basic level", null, null, level_3, null, taxonomy);
		TaxonomyLevel level_5 = taxonomyLevelDao.createTaxonomyLevel("L-5", "A level", "A basic level", null, null, level_4, null, taxonomy);
		dbInstance.commit();

		List<TaxonomyLevel> levels = taxonomyLevelDao.getParentLine(level_5, taxonomy);
		Assert.assertNotNull(levels);
		Assert.assertEquals(5, levels.size());
		Assert.assertEquals(level_1, levels.get(0));
		Assert.assertEquals(level_2, levels.get(1));
		Assert.assertEquals(level_3, levels.get(2));
		Assert.assertEquals(level_4, levels.get(3));
		Assert.assertEquals(level_5, levels.get(4));
	}
	
	@Test
	public void getDescendants() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-103b", "Deeply leveled taxonomy", null, null);
		TaxonomyLevel level_1 = taxonomyLevelDao.createTaxonomyLevel("L-1", "A level", "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level_2 = taxonomyLevelDao.createTaxonomyLevel("L-2", "A level", "A basic level", null, null, level_1, null, taxonomy);
		TaxonomyLevel level_3 = taxonomyLevelDao.createTaxonomyLevel("L-3", "A level", "A basic level", null, null, level_2, null, taxonomy);
		TaxonomyLevel level_4 = taxonomyLevelDao.createTaxonomyLevel("L-4", "A level", "A basic level", null, null, level_3, null, taxonomy);
		TaxonomyLevel level_5 = taxonomyLevelDao.createTaxonomyLevel("L-5", "A level", "A basic level", null, null, level_4, null, taxonomy);
		dbInstance.commit();

		List<TaxonomyLevel> levels = taxonomyLevelDao.getDescendants(level_2, taxonomy);
		Assert.assertNotNull(levels);
		Assert.assertEquals(3, levels.size());
		Assert.assertTrue(levels.contains(level_3));
		Assert.assertTrue(levels.contains(level_4));
		Assert.assertTrue(levels.contains(level_5));
	}
	
	@Test
	public void getLevelsByExternalId() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-103", "Externalized taxonomy", null, null);
		String externalId = UUID.randomUUID().toString();
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("L-1", "A level", "A basic level", externalId, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyLevel> levels = taxonomyLevelDao.getLevelsByExternalId(taxonomy, externalId);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level, levels.get(0));
	}
	
	@Test
	public void getLevelsByDisplayName() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-104", "Named taxonomy", null, null);
		String displayName = UUID.randomUUID().toString();
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("L-1", displayName, "A basic level", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyLevel> levels = taxonomyLevelDao.getLevelsByDisplayName(taxonomy, displayName);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level, levels.get(0));
	}
	
	@Test
	public void searchLevelsByDisplayName() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-105A", "Named taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("L-1A", "A basic level", "", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("L-1G", "A complex level", "", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		TaxonomyLevelSearchParameters searchParams = new TaxonomyLevelSearchParameters();
		searchParams.setQuickSearch("basic");
		List<TaxonomyLevel> levels = taxonomyLevelDao.searchLevels(taxonomy, searchParams);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level1, levels.get(0));
		Assert.assertNotEquals(level2, levels.get(0));
	}
	
	@Test
	public void searchLevelsByKey() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-105B", "Named taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("L-1E", "A numerated level", "", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("L-1F", "A numerated level", "", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//key
		TaxonomyLevelSearchParameters searchParams = new TaxonomyLevelSearchParameters();
		searchParams.setQuickSearch(level1.getKey().toString());
		List<TaxonomyLevel> levels = taxonomyLevelDao.searchLevels(taxonomy, searchParams);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level1, levels.get(0));
		Assert.assertNotEquals(level2, levels.get(0));
	}
	
	@Test
	public void searchLevelsByExternalId() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-105B", "Named taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("L-1C", "A numerated level", "", "34765", null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("L-1D", "A numerated level", "", "34766", null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//key
		TaxonomyLevelSearchParameters searchParams = new TaxonomyLevelSearchParameters();
		searchParams.setQuickSearch("34765");
		List<TaxonomyLevel> levels = taxonomyLevelDao.searchLevels(taxonomy, searchParams);
		Assert.assertNotNull(levels);
		Assert.assertEquals(1, levels.size());
		Assert.assertEquals(level1, levels.get(0));
		Assert.assertNotEquals(level2, levels.get(0));
	}
	
	
	@Test
	public void updateTaxonomyLevel_simple() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-105", "Updated taxonomy", null, null);
		String displayName = UUID.randomUUID().toString();
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("U-1", displayName, "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("U-2", displayName, "A basic level", null, null, level1, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		// update the level 2
		TaxonomyLevel reloadedLevel2 = taxonomyLevelDao.loadByKey(level2.getKey());
		reloadedLevel2.setDisplayName("Updated");
		reloadedLevel2.setIdentifier("UU");
		TaxonomyLevel updatedLevel2 = taxonomyLevelDao.updateTaxonomyLevel(reloadedLevel2);
		dbInstance.commitAndCloseSession();
		
		// check its path
		String identifiersPath = updatedLevel2.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-1/UU/", identifiersPath);
	}
	
	@Test
	public void updateTaxonomyLevel_withChildren() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-105", "Updated taxonomy", null, null);
		String displayName = UUID.randomUUID().toString();
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("U-1", displayName, "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("U-2", displayName, "A basic level", null, null, level1, null, taxonomy);
		TaxonomyLevel level3 = taxonomyLevelDao.createTaxonomyLevel("U-3", displayName, "A basic level", null, null, level2, null, taxonomy);
		TaxonomyLevel level4_1 = taxonomyLevelDao.createTaxonomyLevel("U-4-1", displayName, "A basic level", null, null, level3, null, taxonomy);
		TaxonomyLevel level4_2 = taxonomyLevelDao.createTaxonomyLevel("U-4-2", displayName, "A basic level", null, null, level3, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//update the level 2
		TaxonomyLevel reloadedLevel2 = taxonomyLevelDao.loadByKey(level2.getKey());
		reloadedLevel2.setDisplayName("Updated");
		reloadedLevel2.setIdentifier("UBU");
		TaxonomyLevel updatedLevel2 = taxonomyLevelDao.updateTaxonomyLevel(reloadedLevel2);
		dbInstance.commitAndCloseSession();
		
		//check the different levels paths
		String identifiersPath2 = updatedLevel2.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-1/UBU/", identifiersPath2);

		TaxonomyLevel updatedLevel3 = taxonomyLevelDao.loadByKey(level3.getKey());
		String identifiersPath3 = updatedLevel3.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-1/UBU/U-3/", identifiersPath3);

		TaxonomyLevel updatedLevel4_1 = taxonomyLevelDao.loadByKey(level4_1.getKey());
		String identifiersPath4_1 = updatedLevel4_1.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-1/UBU/U-3/U-4-1/", identifiersPath4_1);
		
		TaxonomyLevel updatedLevel4_2 = taxonomyLevelDao.loadByKey(level4_2.getKey());
		String identifiersPath4_2 = updatedLevel4_2.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-1/UBU/U-3/U-4-2/", identifiersPath4_2);
	}
	
	@Test
	public void moveTaxonomyLevel_simple() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-106", "Moving taxonomy", null, null);
		String displayName = UUID.randomUUID().toString();
		TaxonomyLevel parentLevel = taxonomyLevelDao.createTaxonomyLevel("U-1", displayName, "A root level", null, null, null, null, taxonomy);
		TaxonomyLevel targetLevel = taxonomyLevelDao.createTaxonomyLevel("T-1", displayName, "A root level", null, null, null, null, taxonomy);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("U-2", displayName, "A basic level", null, null, parentLevel, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//move the level
		TaxonomyLevel reloadedLevel = taxonomyLevelDao.loadByKey(level.getKey());
		TaxonomyLevel movedLevel = taxonomyLevelDao.moveTaxonomyLevel(reloadedLevel, targetLevel);
		dbInstance.commitAndCloseSession();

		TaxonomyLevel reloadedMovedLevel = taxonomyLevelDao.loadByKey(movedLevel.getKey());
		String identifiersPath = reloadedMovedLevel.getMaterializedPathIdentifiers();
		Assert.assertEquals("/T-1/U-2/", identifiersPath);
	}
	
	@Test
	public void moveTaxonomyLevel_withChildren() {
		// prepare some levels
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-107", "Large moving taxonomy", null, null);
		String displayName = UUID.randomUUID().toString();
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("U-1", displayName, "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("U-2", displayName, "A basic level", null, null, level1, null, taxonomy);
		TaxonomyLevel level3 = taxonomyLevelDao.createTaxonomyLevel("U-3", displayName, "A basic level", null, null, level2, null, taxonomy);
		TaxonomyLevel level4_1 = taxonomyLevelDao.createTaxonomyLevel("U-4-1", displayName, "A basic level", null, null, level3, null, taxonomy);
		TaxonomyLevel level4_2 = taxonomyLevelDao.createTaxonomyLevel("U-4-2", displayName, "A basic level", null, null, level3, null, taxonomy);
		TaxonomyLevel targetLevel1 = taxonomyLevelDao.createTaxonomyLevel("T-1", displayName, "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel targetLevel2 = taxonomyLevelDao.createTaxonomyLevel("T-2", displayName, "A basic level", null, null, targetLevel1, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//move the level 2
		TaxonomyLevel reloadedLevel2 = taxonomyLevelDao.loadByKey(level2.getKey());
		TaxonomyLevel movedLevel2 = taxonomyLevelDao.moveTaxonomyLevel(reloadedLevel2, targetLevel2);
		dbInstance.commitAndCloseSession();

		//check the different levels paths
		TaxonomyLevel reloadedMovedLevel2 = taxonomyLevelDao.loadByKey(movedLevel2.getKey());
		String identifiersPath2 = reloadedMovedLevel2.getMaterializedPathIdentifiers();
		Assert.assertEquals("/T-1/T-2/U-2/", identifiersPath2);

		TaxonomyLevel movedLevel3 = taxonomyLevelDao.loadByKey(level3.getKey());
		String identifiersPath3 = movedLevel3.getMaterializedPathIdentifiers();
		Assert.assertEquals("/T-1/T-2/U-2/U-3/", identifiersPath3);

		TaxonomyLevel movedLevel4_1 = taxonomyLevelDao.loadByKey(level4_1.getKey());
		String identifiersPath4_1 = movedLevel4_1.getMaterializedPathIdentifiers();
		Assert.assertEquals("/T-1/T-2/U-2/U-3/U-4-1/", identifiersPath4_1);
		
		TaxonomyLevel movedLevel4_2 = taxonomyLevelDao.loadByKey(level4_2.getKey());
		String identifiersPath4_2 = movedLevel4_2.getMaterializedPathIdentifiers();
		Assert.assertEquals("/T-1/T-2/U-2/U-3/U-4-2/", identifiersPath4_2);
	}
	
	@Test
	public void moveTaxonomyLevel_toRoot() {
		// prepare some levels
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-107", "Large moving taxonomy", null, null);
		String displayName = UUID.randomUUID().toString();
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("U-1", displayName, "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("U-2", displayName, "A basic level", null, null, level1, null, taxonomy);
		TaxonomyLevel level3 = taxonomyLevelDao.createTaxonomyLevel("U-3", displayName, "A basic level", null, null, level2, null, taxonomy);
		TaxonomyLevel level4_1 = taxonomyLevelDao.createTaxonomyLevel("U-4-1", displayName, "A basic level", null, null, level3, null, taxonomy);
		TaxonomyLevel level4_2 = taxonomyLevelDao.createTaxonomyLevel("U-4-2", displayName, "A basic level", null, null, level3, null, taxonomy);
		dbInstance.commitAndCloseSession();
		
		//move the level 2
		TaxonomyLevel reloadedLevel3 = taxonomyLevelDao.loadByKey(level3.getKey());
		TaxonomyLevel movedLevel3 = taxonomyLevelDao.moveTaxonomyLevel(reloadedLevel3, null);
		dbInstance.commitAndCloseSession();

		//check the different levels paths
		TaxonomyLevel reloadedMovedLevel3 = taxonomyLevelDao.loadByKey(movedLevel3.getKey());
		String identifiersPath3 = reloadedMovedLevel3.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-3/", identifiersPath3);

		TaxonomyLevel movedLevel4_1 = taxonomyLevelDao.loadByKey(level4_1.getKey());
		String identifiersPath4_1 = movedLevel4_1.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-3/U-4-1/", identifiersPath4_1);
		
		TaxonomyLevel movedLevel4_2 = taxonomyLevelDao.loadByKey(level4_2.getKey());
		String identifiersPath4_2 = movedLevel4_2.getMaterializedPathIdentifiers();
		Assert.assertEquals("/U-3/U-4-2/", identifiersPath4_2);
	}
}
