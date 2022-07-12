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
package org.olat.modules.lecture.manager;

import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockToTaxonomyLevel;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockToTaxonomyLevelDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private LectureBlockToTaxonomyLevelDAO lectureBlockToTaxonomyLevelDao;
	
	@Test
	public void createRelation() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello taxonomists");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-199", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
	
		lectureBlockToTaxonomyLevelDao.createRelation(lectureBlock, level);
		dbInstance.commit();
	}
	
	@Test
	public void createAndGetRelation() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello taxonomists");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-200", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
	
		lectureBlockToTaxonomyLevelDao.createRelation(lectureBlock, level);
		dbInstance.commitAndCloseSession();
		
		LectureBlock reloadedBlock = lectureBlockDao.loadByKey(lectureBlock.getKey());
		Set<LectureBlockToTaxonomyLevel> relations = reloadedBlock.getTaxonomyLevels();
		Assert.assertNotNull(relations);
		Assert.assertEquals(1, relations.size());
		TaxonomyLevel relationLevel = relations.iterator().next().getTaxonomyLevel();
		Assert.assertEquals(level, relationLevel);
	}
	
	@Test
	public void getTaxonomyLevels() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello taxonomists");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-201", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);

		lectureBlockToTaxonomyLevelDao.createRelation(lectureBlock, level);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyLevel> loadedLevels = lectureBlockToTaxonomyLevelDao.getTaxonomyLevels(lectureBlock);
		Assert.assertNotNull(loadedLevels);
		Assert.assertEquals(1, loadedLevels.size());
		Assert.assertEquals(level, loadedLevels.get(0));
	}
	
	
	@Test
	public void getLectureBlocks() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lectures of taxonomists");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-202", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);

		lectureBlockToTaxonomyLevelDao.createRelation(lectureBlock, level);
		dbInstance.commitAndCloseSession();
		
		List<LectureBlock> loadedBlocks = lectureBlockToTaxonomyLevelDao.getLectureBlocks(level);
		Assert.assertNotNull(loadedBlocks);
		Assert.assertEquals(1, loadedBlocks.size());
		Assert.assertEquals(lectureBlock, loadedBlocks.get(0));
	}
}
