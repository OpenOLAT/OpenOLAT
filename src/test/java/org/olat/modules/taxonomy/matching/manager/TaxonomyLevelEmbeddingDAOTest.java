/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.matching.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.matching.manager.TaxonomyLevelEmbeddingDAO.TaxonomyLevelEmbeddingWithVector;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelEmbeddingImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link TaxonomyLevelEmbeddingDAO}.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyLevelEmbeddingDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyLevelEmbeddingDAO embeddingDao;

	@Test
	public void shouldPersistAndLoadEmbedding() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Embed test", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Bio", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		float[] vector = buildVector(384, 0.1f);
		TaxonomyLevelEmbeddingImpl emb = embeddingDao.upsert(level, "de", TaxonomyEmbeddingTextVariant.FULL, "Biologie", vector, "test-model", "v1", null);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelEmbeddingImpl loaded = embeddingDao.findByLevelLocaleModelTextVariant(level, "de", "test-model", TaxonomyEmbeddingTextVariant.FULL);
		assertThat(loaded).isNotNull();
		assertThat(loaded.getKey()).isEqualTo(emb.getKey());
		assertThat(loaded.getLocale()).isEqualTo("de");
		assertThat(loaded.getTextVariant()).isEqualTo(TaxonomyEmbeddingTextVariant.FULL);
		assertThat(loaded.getEmbeddingText()).isEqualTo("Biologie");
		assertThat(loaded.getModelId()).isEqualTo("test-model");
		assertThat(loaded.getModelVersion()).isEqualTo("v1");
	}

	@Test
	public void shouldUpsertOnDuplicateLevelLocaleForm() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Upsert test", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Chem", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		float[] v1 = buildVector(384, 0.1f);
		TaxonomyLevelEmbeddingImpl first = embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.FULL, "Chemistry", v1, "model-a", "v1", null);
		dbInstance.commitAndCloseSession();

		float[] v2 = buildVector(384, 0.2f);
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.FULL, "Chemistry updated", v2, "model-a", "v2", null);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelEmbeddingImpl updated = embeddingDao.findByLevelLocaleModelTextVariant(level, "en", "model-a", TaxonomyEmbeddingTextVariant.FULL);
		assertThat(updated).isNotNull();
		assertThat(updated.getKey()).isEqualTo(first.getKey());
		assertThat(updated.getEmbeddingText()).isEqualTo("Chemistry updated");
		assertThat(updated.getModelVersion()).isEqualTo("v2");

		long count = embeddingDao.countByTaxonomy(taxonomy, "model-a");
		assertThat(count).isEqualTo(1L);
	}

	@Test
	public void differentFormsShouldCreateSeparateRows() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Form test", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Physics", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		float[] vector = buildVector(384, 0.5f);
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.NAME, "Physics", vector, "m1", "v1", null);
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.PATH_NAME, "Natural Sciences, Physics", vector, "m1", "v1", null);
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.FULL, "Natural Sciences, Physics. Study of matter.", vector, "m1", "v1", null);
		dbInstance.commitAndCloseSession();

		assertThat(embeddingDao.countByTaxonomy(taxonomy, "m1")).isEqualTo(3L);
		assertThat(embeddingDao.findByLevelLocaleModelTextVariant(level, "en", "m1", TaxonomyEmbeddingTextVariant.NAME)).isNotNull();
		assertThat(embeddingDao.findByLevelLocaleModelTextVariant(level, "en", "m1", TaxonomyEmbeddingTextVariant.PATH_NAME)).isNotNull();
		assertThat(embeddingDao.findByLevelLocaleModelTextVariant(level, "en", "m1", TaxonomyEmbeddingTextVariant.FULL)).isNotNull();
	}

	@Test
	public void shouldDeleteByLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Delete test", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Math", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		embeddingDao.upsert(level, "de", TaxonomyEmbeddingTextVariant.NAME, "Mathematik", buildVector(384, 0.3f), "m1", "v1", null);
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.NAME, "Mathematics", buildVector(384, 0.4f), "m1", "v1", null);
		dbInstance.commitAndCloseSession();

		int deleted = embeddingDao.deleteByLevel(level);
		dbInstance.commitAndCloseSession();

		assertThat(deleted).isEqualTo(2);
		assertThat(embeddingDao.countByTaxonomy(taxonomy, "m1")).isEqualTo(0L);
	}

	@Test
	public void shouldFindByTaxonomy() {
		Taxonomy taxA = taxonomyDao.createTaxonomy(random(), "Tax A", null, null);
		Taxonomy taxB = taxonomyDao.createTaxonomy(random(), "Tax B", null, null);
		TaxonomyLevel levelA = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Physics", null, null, null, null, null, taxA);
		TaxonomyLevel levelB = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "History", null, null, null, null, null, taxB);
		dbInstance.commitAndCloseSession();

		embeddingDao.upsert(levelA, "en", TaxonomyEmbeddingTextVariant.NAME, "Physics", buildVector(384, 0.5f), "m1", "v1", null);
		embeddingDao.upsert(levelB, "en", TaxonomyEmbeddingTextVariant.NAME, "History", buildVector(384, 0.6f), "m1", "v1", null);
		dbInstance.commitAndCloseSession();

		List<TaxonomyLevelEmbeddingWithVector> forA = embeddingDao.findByTaxonomy(taxA, null);
		assertThat(forA).hasSize(1);
		assertThat(forA.get(0).getLevelKey()).isEqualTo(levelA.getKey());
	}

	private float[] buildVector(int dims, float seed) {
		float[] v = new float[dims];
		for (int i = 0; i < dims; i++) {
			v[i] = seed + i * 0.001f;
		}
		double norm = 0;
		for (float f : v) {
			norm += f * f;
		}
		norm = Math.sqrt(norm);
		for (int i = 0; i < dims; i++) {
			v[i] = (float) (v[i] / norm);
		}
		return v;
	}
}
