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
package org.olat.modules.cemedia.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaToTaxonomyLevel;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 mai 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaToTaxonomyLevelDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaDAO mediaDao;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private MediaToTaxonomyLevelDAO mediaToTaxonomyLevelDao;
	
	@Test
	public void createRelation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-rel-1");
		Media media = mediaDao.createMedia("Media relation", "Media relation description", null, "Media relation content", "Forum", "[Media:1]", null, 10, id);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-2300", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My relation level", "A basic level", null, null, null, null, taxonomy);
		MediaToTaxonomyLevel relation = mediaToTaxonomyLevelDao.createRelation(media, level);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(relation);
		Assert.assertNotNull(relation.getKey());
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertEquals(media, relation.getMedia());
		Assert.assertEquals(level, relation.getTaxonomyLevel());
	}
	
	@Test
	public void loadRelationsByMedia() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-rel-2");
		Media media = mediaDao.createMedia("Media relation 2", "Media relation description", null, "Media relation content", "Forum", "[Media:2]", null, 10, id);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-2301", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My relation level", "A basic level", null, null, null, null, taxonomy);
		MediaToTaxonomyLevel relation = mediaToTaxonomyLevelDao.createRelation(media, level);
		dbInstance.commitAndCloseSession();
		
		List<MediaToTaxonomyLevel> relations = mediaToTaxonomyLevelDao.loadRelations(media);
		assertThat(relations)
			.hasSize(1)
			.containsExactly(relation);
	}
	
	@Test
	public void loadTaxonomyLevels() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-rel-3");
		Media media = mediaDao.createMedia("Media relation 3", "Media relation description", null, "Media relation content", "Forum", "[Media:3]", null, 10, id);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-2303", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My relation level", "A basic level", null, null, null, null, taxonomy);
		mediaToTaxonomyLevelDao.createRelation(media, level);
		dbInstance.commitAndCloseSession();
		
		List<TaxonomyLevel> levels = mediaToTaxonomyLevelDao.loadTaxonomyLevels(media);
		assertThat(levels)
			.hasSize(1)
			.containsExactly(level);
	}
	
	@Test
	public void loadRelationsByIdentity() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("media-rel-4");
		Media media = mediaDao.createMedia("Media relation 4", "Media relation description", null, "Media relation content", "Forum", "[Media:3]", null, 10, id);

		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-2303", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My relation level", "A basic level", null, null, null, null, taxonomy);
		MediaToTaxonomyLevel relation = mediaToTaxonomyLevelDao.createRelation(media, level);
		dbInstance.commitAndCloseSession();
		
		List<MediaToTaxonomyLevel> levels = mediaToTaxonomyLevelDao.loadRelations(id);
		assertThat(levels)
			.hasSize(1)
			.containsExactly(relation);
	}
}
