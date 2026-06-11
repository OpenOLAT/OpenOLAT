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
import org.olat.modules.taxonomy.matching.model.TaxonomyMatch;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * Integration tests for {@link TaxonomyMatchingServiceImpl}.
 * Uses a deterministic in-process mock embedding model — no external API.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyMatchingServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyLevelEmbeddingDAO embeddingDao;
	@Autowired
	private TaxonomyMatchingServiceImpl matchingService;

	@Test
	public void shouldReturnMatchesAboveThreshold() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Match test", null, null);
		TaxonomyLevel biology = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Biology", null, null, null, null, null, taxonomy);
		TaxonomyLevel chemistry = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Chemistry", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		EmbeddingModel model = deterministicModel();
		float[] bioVec = model.embed("Biology").content().vector();
		float[] chemVec = model.embed("Chemistry").content().vector();
		embeddingDao.upsert(biology, "en", TaxonomyEmbeddingTextVariant.NAME, "Biology", bioVec, "test-model", "v1", null);
		embeddingDao.upsert(chemistry, "en", TaxonomyEmbeddingTextVariant.NAME, "Chemistry", chemVec, "test-model", "v1", null);
		dbInstance.commitAndCloseSession();

		matchingService.setEmbeddingModel(model);
		List<TaxonomyMatch> matches = matchingService.suggestLevels("Biology", taxonomy, 5, 0.5);

		assertThat(matches).isNotEmpty();
		assertThat(matches.get(0).level()).isEqualTo(biology);
		assertThat(matches.get(0).score()).isGreaterThan(0.9);
	}

	@Test
	public void shouldRespectLimit() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Limit test", null, null);
		for (int i = 0; i < 5; i++) {
			TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Level" + i, null, null, null, null, null, taxonomy);
			dbInstance.commitAndCloseSession();
			EmbeddingModel model = deterministicModel();
			embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.NAME, "Level" + i, model.embed("Level" + i).content().vector(), "test-model", "v1", null);
		}
		dbInstance.commitAndCloseSession();

		matchingService.setEmbeddingModel(deterministicModel());
		List<TaxonomyMatch> matches = matchingService.suggestLevels("Level1", taxonomy, 2, 0.0);

		assertThat(matches).hasSize(2);
	}

	@Test
	public void shouldReturnEmptyWhenNoMatch() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Empty test", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Physics", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		EmbeddingModel model = deterministicModel();
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.NAME, "Physics", model.embed("Physics").content().vector(), "test-model", "v1", null);
		dbInstance.commitAndCloseSession();

		matchingService.setEmbeddingModel(model);
		List<TaxonomyMatch> matches = matchingService.suggestLevels("Physics", taxonomy, 5, 2.0);

		assertThat(matches).isEmpty();
	}

	@Test
	public void shouldSearchAcrossLocales() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Locale test", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Biologie", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		EmbeddingModel model = deterministicModel();
		embeddingDao.upsert(level, "de", TaxonomyEmbeddingTextVariant.NAME, "Biologie", model.embed("Biologie").content().vector(), "test-model", "v1", null);
		embeddingDao.upsert(level, "en", TaxonomyEmbeddingTextVariant.NAME, "Biology", model.embed("Biology").content().vector(), "test-model", "v1", null);
		dbInstance.commitAndCloseSession();

		matchingService.setEmbeddingModel(model);
		List<TaxonomyMatch> byDe = matchingService.suggestLevels("Biologie", taxonomy, 5, 0.5);
		assertThat(byDe).isNotEmpty();
		assertThat(byDe.get(0).level()).isEqualTo(level);

		List<TaxonomyMatch> byEn = matchingService.suggestLevels("Biology", taxonomy, 5, 0.5);
		assertThat(byEn).isNotEmpty();
		assertThat(byEn.get(0).level()).isEqualTo(level);
	}

	@Test
	public void shouldCollapseMultipleFormsToSingleMatch() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), "Collapse test", null, null);
		TaxonomyLevel biology = taxonomyLevelDao.createTaxonomyLevel(random(), random(), "Biology", null, null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		EmbeddingModel model = deterministicModel();
		embeddingDao.upsert(biology, "en", TaxonomyEmbeddingTextVariant.NAME, "Biology",
				model.embed("Biology").content().vector(), "test-model", "v1", null);
		embeddingDao.upsert(biology, "en", TaxonomyEmbeddingTextVariant.PATH_NAME, "Natural Sciences, Biology",
				model.embed("Natural Sciences, Biology").content().vector(), "test-model", "v1", null);
		dbInstance.commitAndCloseSession();

		matchingService.setEmbeddingModel(model);
		List<TaxonomyMatch> matches = matchingService.suggestLevels("Biology", taxonomy, 5, 0.0);

		assertThat(matches).hasSize(1);
		assertThat(matches.get(0).level()).isEqualTo(biology);
	}

	private EmbeddingModel deterministicModel() {
		return new EmbeddingModel() {
			private static final int DIMS = 8;

			@Override
			public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
				List<Embedding> embeddings = segments.stream()
						.map(s -> new Embedding(makeVector(s.text())))
						.toList();
				return Response.from(embeddings);
			}

			private float[] makeVector(String text) {
				float[] v = new float[DIMS];
				int hash = text.hashCode();
				for (int i = 0; i < DIMS; i++) {
					v[i] = (float) Math.sin(hash + i);
				}
				normalize(v);
				return v;
			}

			private void normalize(float[] v) {
				double norm = 0;
				for (float f : v) {
					norm += (double) f * f;
				}
				norm = Math.sqrt(norm);
				if (norm > 0) {
					for (int i = 0; i < v.length; i++) {
						v[i] = (float) (v[i] / norm);
					}
				}
			}
		};
	}
}
