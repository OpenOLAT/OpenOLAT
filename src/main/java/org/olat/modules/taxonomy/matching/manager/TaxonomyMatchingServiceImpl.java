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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiEmbeddingSPI;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.matching.PgVectorType;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingReindexEvent;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingService;
import org.olat.modules.taxonomy.matching.manager.TaxonomyLevelEmbeddingDAO.TaxonomyLevelEmbeddingWithVector;
import org.olat.modules.taxonomy.matching.model.TaxonomyMatch;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * Implementation of {@link TaxonomyMatchingService}.
 * <p>
 * When pgvector is available and a model dimension can be resolved, creates the
 * {@code t_vector vector(dim)} column and {@code idx_tax_emb_vector} HNSW index
 * at runtime and delegates similarity search to the database. Without pgvector,
 * or before the first indexing pass, falls back to in-memory cosine similarity.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
@Service
public class TaxonomyMatchingServiceImpl implements TaxonomyMatchingService {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyMatchingServiceImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyLevelEmbeddingDAO embeddingDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyMatchingModule matchingModule;
	@Autowired
	private AiModule aiModule;

	private EmbeddingModel embeddingModel;

	private volatile String pgVectorEnsuredKey;
	private volatile PgVectorType pgVectorType;
	private final AtomicLong reindexGeneration = new AtomicLong(0);
	private final ExecutorService reindexExecutor = Executors.newSingleThreadExecutor(
			r -> new Thread(r, "taxonomy-embedding-reindex"));
	private volatile boolean reindexing;

	@Override
	public List<TaxonomyMatch> suggestLevels(String text, TaxonomyRef taxonomy, int limit, double minScore) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		EmbeddingModel model = getEmbeddingModel();
		if (model == null) {
			log.debug("No embedding model configured for taxonomy matching");
			return List.of();
		}

		log.debug("suggestLevels: text='{}', taxonomy={}, limit={}, minScore={}", text, taxonomy.getKey(), limit, minScore);
		try {
			ensurePgVector(model, false);
			float[] queryVector = embed(model, matchingModule.getQueryPrefix() + text);
			if (queryVector == null) {
				return List.of();
			}

			List<TaxonomyMatch> matches;
			if (pgVectorType != null) {
				matches = pgvectorSearch(taxonomy, text, queryVector, minScore, limit);
			} else {
				matches = inMemorySearch(taxonomy, text, queryVector, minScore, limit);
			}
			log.debug("suggestLevels: suggested {} level(s): {}", matches.size(),
					matches.stream().map(m -> m.level().getMaterializedPathIdentifiers() + "(" + m.score() + ")").toList());
			return matches;
		} catch (Exception e) {
			log.warn("Taxonomy embedding search failed for text '{}': {}", text, e.getMessage(), e);
			return List.of();
		}
	}

	private List<TaxonomyMatch> pgvectorSearch(TaxonomyRef taxonomy, String queryText, float[] queryVector, double minScore, int limit) {
		int rowsPerLevel = TaxonomyEmbeddingTextVariant.values().length * getConfiguredLocales().size();
		List<TaxonomyLevelEmbeddingWithVector> rows = embeddingDao.findSimilar(taxonomy, queryVector, limit * rowsPerLevel, pgVectorType);
		log.debug("pgvectorSearch: {} candidate row(s) from DB (minScore={}, top {}): {}",
				rows.size(), minScore, Math.min(10, rows.size()),
				rows.stream().limit(10).map(r -> r.getScore() + " " + r.getEmbeddingText()).toList());
		return toMatches(rows, queryText, minScore, limit);
	}

	private List<TaxonomyMatch> inMemorySearch(TaxonomyRef taxonomy, String queryText, float[] queryVector, double minScore, int limit) {
		List<TaxonomyLevelEmbeddingWithVector> all = embeddingDao.findByTaxonomy(taxonomy, null);
		all.removeIf(row -> row.getVector() == null);
		for (TaxonomyLevelEmbeddingWithVector row : all) {
			row.setScore(cosineSimilarity(queryVector, row.getVector()));
		}
		all.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
		log.debug("inMemorySearch: {} candidate row(s) from DB (minScore={}, top {}): {}",
				all.size(), minScore, Math.min(10, all.size()),
				all.stream().limit(10).map(r -> r.getScore() + " " + r.getEmbeddingText()).toList());
		return toMatches(all, queryText, minScore, limit);
	}

	private List<TaxonomyMatch> toMatches(List<TaxonomyLevelEmbeddingWithVector> rows, String queryText, double minScore, int limit) {
		String normalizedQuery = (queryText != null) ? queryText.trim() : null;

		Set<Long> exactMatchLevels = new HashSet<>();
		Map<Long, TaxonomyLevelEmbeddingWithVector> exactMatchRowByLevel = new HashMap<>();
		if (normalizedQuery != null && !normalizedQuery.isEmpty()) {
			for (TaxonomyLevelEmbeddingWithVector row : rows) {
				if (normalizedQuery.equalsIgnoreCase(row.getEmbeddingText() != null ? row.getEmbeddingText().trim() : null)) {
					exactMatchLevels.add(row.getLevelKey());
					exactMatchRowByLevel.putIfAbsent(row.getLevelKey(), row);
				}
			}
		}

		Map<Long, TaxonomyLevelEmbeddingWithVector> bestByLevel = new HashMap<>();
		for (TaxonomyLevelEmbeddingWithVector row : rows) {
			bestByLevel.merge(row.getLevelKey(), row,
					(a, b) -> a.getScore() >= b.getScore() ? a : b);
		}

		List<TaxonomyLevelEmbeddingWithVector> candidates = bestByLevel.values().stream()
				.filter(r -> exactMatchLevels.contains(r.getLevelKey()) || r.getScore() >= minScore)
				.sorted((a, b) -> {
					boolean aExact = exactMatchLevels.contains(a.getLevelKey());
					boolean bExact = exactMatchLevels.contains(b.getLevelKey());
					if (aExact != bExact) {
						return aExact ? -1 : 1;
					}
					return Double.compare(b.getScore(), a.getScore());
				}).toList();

		if (candidates.isEmpty() && !rows.isEmpty()) {
			double bestScore = rows.stream().mapToDouble(TaxonomyLevelEmbeddingWithVector::getScore).max().orElse(0.0);
			log.debug("toMatches: no candidate passed minScore={} (best was {})", minScore, bestScore);
		}

		List<Long> candidateKeys = candidates.stream().map(TaxonomyLevelEmbeddingWithVector::getLevelKey).toList();
		Map<Long, TaxonomyLevel> levelsByKey = taxonomyLevelDao.loadLevelsByKeys(candidateKeys).stream()
				.collect(Collectors.toMap(TaxonomyLevel::getKey, Function.identity()));

		List<TaxonomyMatch> matches = new ArrayList<>();
		for (TaxonomyLevelEmbeddingWithVector row : candidates) {
			TaxonomyLevel level = levelsByKey.get(row.getLevelKey());
			if (level != null) {
				boolean exact = exactMatchLevels.contains(row.getLevelKey());
				TaxonomyLevelEmbeddingWithVector sourceRow = exact
						? exactMatchRowByLevel.getOrDefault(row.getLevelKey(), row)
						: row;
				matches.add(new TaxonomyMatch(level, row.getScore(), sourceRow.getLocale(), sourceRow.getEmbeddingText(), exact));
			}
			if (matches.size() >= limit) {
				break;
			}
		}
		return matches;
	}

	@Override
	public void indexLevel(TaxonomyLevelRef levelRef) {
		if (reindexing) {
			return;
		}
		TaxonomyLevel level = taxonomyLevelDao.loadByKey(levelRef.getKey());
		if (level == null) {
			return;
		}
		EmbeddingModel model = getEmbeddingModel();
		if (model == null) {
			return;
		}
		ensurePgVector(model, false);
		indexLevelInternal(level, model);
	}

	public boolean isReindexing() {
		return reindexing;
	}

	@Override
	public void scheduleFullReindex() {
		long myGen = reindexGeneration.incrementAndGet();
		reindexExecutor.submit(() -> runFullReindex(myGen));
	}

	private void runFullReindex(long myGen) {
		reindexing = true;
		try {
			log.info("Taxonomy embedding full reindex started (generation {})", myGen);
			EmbeddingModel model = getEmbeddingModel();
			if (model == null) {
				log.warn("Taxonomy embedding reindex aborted: no embedding model configured");
				return;
			}

			pgVectorEnsuredKey = null;
			pgVectorType = null;
			matchingModule.setPgVectorActive(false);

			ensurePgVector(model, true);
			dbInstance.commitAndCloseSession();

			if ("postgresql".equals(dbInstance.getDbVendor()) && pgVectorType == null) {
				log.warn("Taxonomy embedding reindex aborted: could not resolve pgvector column type for model '{}' — existing embeddings preserved",
						matchingModule.getModel());
				return;
			}

			embeddingDao.deleteAll();
			matchingModule.clearVectorKeyAndDim();
			dbInstance.commitAndCloseSession();

			List<Taxonomy> taxonomies = taxonomyDao.getTaxonomyList();
			for (Taxonomy taxonomy : taxonomies) {
				List<TaxonomyLevel> levels = taxonomyLevelDao.getLevels(List.of(taxonomy));
				Map<Long, TaxonomyLevel> levelMap = levels.stream()
						.collect(Collectors.toMap(TaxonomyLevel::getKey, Function.identity()));
				int count = 0;
				for (TaxonomyLevel level : levels) {
					if (reindexGeneration.get() != myGen) {
						log.info("Taxonomy embedding reindex cancelled (generation {} superseded by {})",
								myGen, reindexGeneration.get());
						return;
					}
					indexLevelInternal(level, model, levelMap);
					count++;
					if (count % 50 == 0) {
						dbInstance.intermediateCommit();
						log.info("Taxonomy embedding reindex progress: {}/{} levels indexed for taxonomy {} (generation {})",
								count, levels.size(), taxonomy.getKey(), myGen);
					}
				}
				dbInstance.intermediateCommit();
			}

			log.info("Taxonomy embedding full reindex completed (generation {})", myGen);

			org.olat.core.util.coordinate.CoordinatorManager.getInstance().getCoordinator()
					.getEventBus().fireEventToListenersOf(
							new TaxonomyMatchingReindexEvent(),
							OresHelper.createOLATResourceableType("TaxonomyMatching"));
		} catch (Exception e) {
			log.error("Taxonomy embedding full reindex failed (generation {}): {}", myGen, e.getMessage(), e);
		} finally {
			dbInstance.commitAndCloseSession();
			reindexing = false;
		}
	}

	public void indexLevelInternal(TaxonomyLevel level, EmbeddingModel model) {
		indexLevelInternal(level, model, null);
	}

	public void indexLevelInternal(TaxonomyLevel level, EmbeddingModel model, Map<Long, TaxonomyLevel> levelMap) {
		if ("postgresql".equals(dbInstance.getDbVendor()) && pgVectorType == null) {
			log.warn("Skipping index of taxonomy level {}: pgvector column type not resolved", level.getKey());
			return;
		}
		List<Locale> locales = getConfiguredLocales();
		for (Locale locale : locales) {
			var translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
			for (TaxonomyEmbeddingTextVariant textVariant : TaxonomyEmbeddingTextVariant.values()) {
				try {
					String text = TaxonomyEmbeddingTextBuilder.build(level, translator, levelMap, textVariant);
					if (text == null) {
						continue;
					}
					float[] vector = embed(model, matchingModule.getPassagePrefix() + text);
					if (vector != null) {
						embeddingDao.upsert(level, locale.getLanguage(), textVariant, text, vector,
								matchingModule.getModel(), null, pgVectorType);
					}
				} catch (Exception e) {
					log.warn("Failed to index taxonomy level {} for locale {} textVariant {}: {}", level.getKey(), locale, textVariant, e.getMessage(), e);
				}
			}
		}
	}
	
	@Override
	public void deleteEmbeddings(TaxonomyLevelRef level) {
		embeddingDao.deleteByLevel(level);
	}

	@Override
	public boolean isIndexStale(TaxonomyRef taxonomy) {
		String configuredModel = matchingModule.getModel();
		if (configuredModel == null || configuredModel.isBlank()) {
			return false;
		}
		long count = embeddingDao.countByTaxonomy(taxonomy, configuredModel);
		return count == 0;
	}

	private void ensurePgVector(EmbeddingModel model, boolean allowRebuild) {
		if (!"postgresql".equals(dbInstance.getDbVendor())) {
			return;
		}
		String key = matchingModule.getSpiId() + ":" + matchingModule.getModel();
		if (Objects.equals(key, pgVectorEnsuredKey)) {
			return;
		}
		int dim = resolveDimension(model, key);
		if (dim <= 0) {
			log.warn("Could not resolve embedding dimension for model '{}' — pgvector column not created", matchingModule.getModel());
			return;
		}
		PgVectorType resolvedType = embeddingDao.ensureVectorColumnAndIndex(dim, allowRebuild);
		if (resolvedType != null) {
			pgVectorEnsuredKey = key;
			pgVectorType = resolvedType;
			matchingModule.setPgVectorActive(true);
		} else if (!allowRebuild) {
			pgVectorType = null;
			matchingModule.setPgVectorActive(false);
		}
	}

	private int resolveDimension(EmbeddingModel model, String key) {
		if (aiModule == null) {
			return -1;
		}
		AiEmbeddingSPI spi = aiModule.getConfiguredEmbeddingSPI();
		if (spi == null) {
			return -1;
		}
		int dim = spi.getEmbeddingDimension(matchingModule.getModel());
		if (dim > 0) {
			matchingModule.setVectorKeyAndDim(key, dim);
			return dim;
		}
		if (Objects.equals(key, matchingModule.getVectorKey()) && matchingModule.getVectorDim() > 0) {
			return matchingModule.getVectorDim();
		}
		if (model == null) {
			return -1;
		}
		try {
			float[] probe = embed(model, "dim");
			dim = probe != null ? probe.length : -1;
		} catch (Exception e) {
			log.warn("Dimension probe embed failed for model '{}': {}", matchingModule.getModel(), e.getMessage());
			return -1;
		}
		if (dim > 0) {
			matchingModule.setVectorKeyAndDim(key, dim);
		}
		return dim;
	}

	private float[] embed(EmbeddingModel model, String text) {
		try {
			Response<Embedding> response = model.embed(text);
			if (response == null || response.content() == null) {
				return null;
			}
			return response.content().vector();
		} catch (Exception e) {
			log.warn("Embedding call failed: {}", e.getMessage());
			log.debug("Embedding call failed (detail)", e);
			return null;
		}
	}

	private double cosineSimilarity(float[] a, float[] b) {
		if (a.length != b.length) {
			return 0.0;
		}
		double dot = 0, normA = 0, normB = 0;
		for (int i = 0; i < a.length; i++) {
			dot += (double) a[i] * b[i];
			normA += (double) a[i] * a[i];
			normB += (double) b[i] * b[i];
		}
		double denom = Math.sqrt(normA) * Math.sqrt(normB);
		return denom == 0.0 ? 0.0 : dot / denom;
	}

	private List<Locale> getConfiguredLocales() {
		List<Locale> locales = new ArrayList<>();
		locales.add(Locale.GERMAN);
		locales.add(Locale.ENGLISH);
		return locales;
	}

	public EmbeddingModel getEmbeddingModel() {
		if (embeddingModel != null) {
			return embeddingModel;
		}
		if (aiModule != null) {
			AiEmbeddingSPI spi = aiModule.getConfiguredEmbeddingSPI();
			if (spi != null) {
				return spi.buildEmbeddingModel(matchingModule.getModel());
			}
		}
		return null;
	}

	public void setEmbeddingModel(EmbeddingModel embeddingModel) {
		this.embeddingModel = embeddingModel;
	}
}
