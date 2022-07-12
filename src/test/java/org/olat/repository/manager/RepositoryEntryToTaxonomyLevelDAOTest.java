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
package org.olat.repository.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.olat.repository.RepositoryService;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryToTaxonomyLevelDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	
	@Test
	public void createRelation() {
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, null);
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-400", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
	
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryToTaxonomyLevel relation = repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commit();
		Assert.assertNotNull(relation);
	}
	
	@Test
	public void createAndGetRelation() {
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, null);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-401", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntry reloadedEntry = repositoryService.loadByKey(re.getKey());
		Set<RepositoryEntryToTaxonomyLevel> relationToLevels = reloadedEntry.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		RepositoryEntryToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
		Assert.assertEquals(re, relationToLevel.getEntry());
	}
	
	@Test
	public void getTaxonomyLevels() {
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, null);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-402", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyLevel> loadedLevels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(re);
		Assert.assertNotNull(loadedLevels);
		Assert.assertEquals(1, loadedLevels.size());
		Assert.assertEquals(level, loadedLevels.get(0));
	}
	
	@Test
	public void getTaxonomyLevels_entries() {
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, null);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-402", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commitAndCloseSession();
		
		Map<RepositoryEntryRef,List<TaxonomyLevel>> loadedLevels = repositoryEntryToTaxonomyLevelDao
				.getTaxonomyLevels(Collections.singletonList(re), true);
		Assert.assertNotNull(loadedLevels);
		Assert.assertEquals(1, loadedLevels.size());
		Assert.assertEquals(re, loadedLevels.keySet().iterator().next());
		
		List<TaxonomyLevel> entryLevels = loadedLevels.get(re);
		Assert.assertNotNull(entryLevels);
		Assert.assertEquals(1, entryLevels.size());
		Assert.assertEquals(level, entryLevels.get(0));
	}
	
	@Test
	public void getTaxonomyLevelsByKeys() {
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, null);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-402-by-keys", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-402", random(), "My taxonomy level by key", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commitAndCloseSession();
		
		Map<Long,List<TaxonomyLevel>> loadedLevels = repositoryEntryToTaxonomyLevelDao
				.getTaxonomyLevelsByEntryKeys(List.of(re.getKey()));
		Assert.assertNotNull(loadedLevels);
		Assert.assertEquals(1, loadedLevels.size());
		Assert.assertEquals(re.getKey(), loadedLevels.keySet().iterator().next());
		
		List<TaxonomyLevel> entryLevels = loadedLevels.get(re.getKey());
		Assert.assertNotNull(entryLevels);
		Assert.assertEquals(1, entryLevels.size());
		Assert.assertEquals(level, entryLevels.get(0));
	}
	
	@Test
	public void getRepositoryEntries() {
		RepositoryEntry re = repositoryService.create(null, "Asuka Langley", "rel", "rel", null, null,
				RepositoryEntryStatusEnum.trash, null);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-402", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		repositoryEntryToTaxonomyLevelDao.createRelation(re, level);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> entries = repositoryEntryToTaxonomyLevelDao.getRepositoryEntries(level);
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(re, entries.get(0));
	}

}
