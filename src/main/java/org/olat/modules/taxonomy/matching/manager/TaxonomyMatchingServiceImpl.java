/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.ai.AiEmbeddingSPI;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.manager.AiLoggingEmbeddingModel;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.TaskRunnable;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.event.FrameworkStartedEvent;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.matching.PgVectorType;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingReindexEvent;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingService;
import org.olat.modules.taxonomy.matching.manager.TaxonomyLevelEmbeddingDAO.TaxonomyLevelEmbeddingWithVector;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexState;
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
 * Uses a durable per-level index state table as a queue. Embedding work is
 * fully deferred to an async single-flight indexing task running on the
 * {@link TaskRunnable.Queue#aiBatch} pool.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
@Service
public class TaxonomyMatchingServiceImpl implements TaxonomyMatchingService, GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyMatchingServiceImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyLevelEmbeddingDAO embeddingDao;
	@Autowired
	private TaxonomyLevelIndexStateDAO indexStateDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyMatchingModule matchingModule;
	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUsageLogDAO aiUsageLogDAO;
	@Autowired
	private TaskExecutorManager taskExecutorManager;

	private EmbeddingModel embeddingModel;

	private volatile String pgVectorEnsuredKey;
	private volatile PgVectorType pgVectorType;

	private final AtomicBoolean indexerRunning = new AtomicBoolean(false);
	private final AtomicInteger indexGeneration = new AtomicInteger(0);
	private volatile boolean reindexPending = false;

	public TaxonomyMatchingServiceImpl() {
		FrameworkStartupEventChannel.registerForStartupEvent(this);
	}

	@Override
	public void event(Event event) {
		if (event instanceof FrameworkStartedEvent) {
			try {
				init();
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	private void init() {
		int reset = indexStateDao.resetInWorkToScheduled();
		if (reset > 0) {
			log.info("TaxonomyMatchingServiceImpl boot: reset {} stuck indexing rows to scheduled", reset);
		}
		dbInstance.commitAndCloseSession();
		if (needsInitialReindex()) {
			log.info("TaxonomyMatchingServiceImpl boot: index state empty, triggering initial full reindex");
			scheduleFullReindex();
		} else {
			startIndexing();
		}
	}

	private boolean needsInitialReindex() {
		if (!aiModule.isTaxonomyMatchingEnabled()) {
			return false;
		}
		return indexStateDao.countByStatuses(EnumSet.allOf(TaxonomyLevelIndexState.IndexStatus.class)) == 0;
	}

	@Override
	public List<TaxonomyMatch> suggestLevels(AiUsageContext context, String text, TaxonomyRef taxonomy, int limit, double minScore) {
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
			float[] queryVector = embed(model, matchingModule.getQueryPrefix() + text, context);
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
	public void scheduleIndex(TaxonomyLevel level) {
		if (!aiModule.isTaxonomyMatchingEnabled()) {
			return;
		}
		indexStateDao.upsertScheduled(level);
	}

	@Override
	public void scheduleSubtree(TaxonomyLevel level) {
		if (!aiModule.isTaxonomyMatchingEnabled()) {
			return;
		}
		indexStateDao.scheduleSubtree(level);
	}

	@Override
	public void deleteIndexState(TaxonomyLevelRef level) {
		indexStateDao.deleteByLevel(level);
	}

	@Override
	public void startIndexing() {
		if (!aiModule.isTaxonomyMatchingEnabled()) {
			return;
		}
		if (!indexerRunning.compareAndSet(false, true)) {
			return;
		}
		try {
			taskExecutorManager.execute(new IndexingTask());
			reindexPending = false;
		} catch (Exception e) {
			indexerRunning.set(false);
			log.warn("Failed to submit taxonomy embedding indexing task", e);
		}
	}

	private void requestStartIndexing() {
		reindexPending = true;
		startIndexing();
	}

	@Override
	public void scheduleFullReindex() {
		if (!aiModule.isTaxonomyMatchingEnabled()) {
			return;
		}
		
		EmbeddingModel model = getEmbeddingModel();
		if (model == null) {
			log.warn("Taxonomy embedding full reindex requested but no embedding model configured");
			return;
		}

		pgVectorEnsuredKey = null;
		pgVectorType = null;
		matchingModule.setPgVectorActive(false);

		ensurePgVector(model, true);
		dbInstance.commitAndCloseSession();

		if ("postgresql".equals(dbInstance.getDbVendor()) && pgVectorType == null) {
			log.warn("Taxonomy embedding full reindex aborted: could not resolve pgvector column type for model '{}' — existing embeddings preserved",
					matchingModule.getModel());
			return;
		}

		embeddingDao.deleteAll();
		matchingModule.clearVectorKeyAndDim();
		dbInstance.commitAndCloseSession();

		indexStateDao.scheduleAll();
		dbInstance.commitAndCloseSession();

		indexGeneration.incrementAndGet();
		requestStartIndexing();
	}

	@Override
	public void deleteEmbeddings(TaxonomyLevelRef level) {
		embeddingDao.deleteByLevel(level);
	}

	public void indexLevelInternal(TaxonomyLevel level, EmbeddingModel model, Map<Long, TaxonomyLevel> levelMap,
			String usageContextType, String usageContextId, PgVectorType vectorType) {
		if ("postgresql".equals(dbInstance.getDbVendor()) && vectorType == null) {
			log.warn("Skipping index of taxonomy level {}: pgvector column type not resolved", level.getKey());
			return;
		}
		List<Locale> locales = getConfiguredLocales();
		for (Locale locale : locales) {
			var translator = Util.createPackageTranslator(TaxonomyUIFactory.class, locale);
			for (TaxonomyEmbeddingTextVariant textVariant : TaxonomyEmbeddingTextVariant.values()) {
				String text = TaxonomyEmbeddingTextBuilder.build(level, translator, levelMap, textVariant);
				if (text == null) {
					continue;
				}
				AiUsageContext indexContext = AiUsageContext.builder()
						.usageContextType(usageContextType)
						.usageContextId(usageContextId)
						.resourceType("TaxonomyLevel")
						.resourceId(level.getKey())
						.resourceSubId(locale.getLanguage())
						.build();
				float[] vector = embed(model, matchingModule.getPassagePrefix() + text, indexContext);
				if (vector != null) {
					embeddingDao.upsert(level, locale.getLanguage(), textVariant, text, vector,
							matchingModule.getModel(), null, vectorType);
				}
			}
		}
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
			Response<Embedding> probeResponse = model.embed("dim");
			float[] probe = (probeResponse != null && probeResponse.content() != null) ? probeResponse.content().vector() : null;
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

	private float[] embed(EmbeddingModel model, String text, AiUsageContext context) {
		try {
			EmbeddingModel logged = context != null
					? new AiLoggingEmbeddingModel(model, aiUsageLogDAO, matchingModule.getSpiId(), matchingModule.getModel(), AiFeature.TaxonomyMatching.getType(), context)
					: model;
			Response<Embedding> response = logged.embed(text);
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

	/**
	 * Single-flight indexing task submitted to the {@code aiBatch} pool.
	 * Loops until the scheduled backlog is empty, then clears the indexerRunning flag.
	 * Captures the generation counter at submit time; exits the loop early when the
	 * generation changes (model change in progress).
	 */
	private class IndexingTask implements TaskRunnable {

		private final int taskGeneration = indexGeneration.get();

		@Override
		public Queue getExecutorsQueue() {
			return Queue.aiBatch;
		}

		@Override
		public void run() {
			try {
				indexScheduled();
			} finally {
				indexerRunning.set(false);
				if (reindexPending) {
					reindexPending = false;
					startIndexing();
				}
			}
		}

		private void indexScheduled() {
			if (!aiModule.isTaxonomyMatchingEnabled()) {
				log.debug("TaxonomyEmbeddingIndexing: taxonomy matching disabled, leaving rows scheduled");
				return;
			}
			EmbeddingModel model = getEmbeddingModel();
			if (model == null) {
				log.debug("TaxonomyEmbeddingIndexing: no embedding model available, leaving rows scheduled");
				return;
			}

			ensurePgVector(model, false);
			dbInstance.commitAndCloseSession();
			PgVectorType taskVectorType = pgVectorType;

			String contextId = AiUsageContext.createContextId();
			int indexed = 0;

			while (true) {
				if (taskGeneration != indexGeneration.get()) {
					log.debug("TaxonomyEmbeddingIndexing: generation changed, stopping stale task");
					break;
				}
				TaxonomyLevelIndexState state = indexStateDao.claimNextScheduled();
				dbInstance.commitAndCloseSession();
				if (state == null) {
					break;
				}

				TaxonomyLevel level = state.getLevel();
				try {
					Map<Long, TaxonomyLevel> levelMap = buildLevelMap(level);
					indexLevelInternal(level, model, levelMap, "taxonomy-embedding-index", contextId, taskVectorType);
					if (dbInstance.isError()) {
						throw new RuntimeException("DB error after indexLevelInternal for level " + level.getKey());
					}
					indexStateDao.markIndexed(state, matchingModule.getModel(), null);
					dbInstance.commitAndCloseSession();
					indexed++;
					if (indexed % 50 == 0) {
						log.info("TaxonomyEmbeddingIndexing: indexed {} levels so far", indexed);
					}
				} catch (Exception e) {
					log.warn("TaxonomyEmbeddingIndexing: failed to index level {}: {}", level.getKey(), e.getMessage(), e);
					dbInstance.rollbackAndCloseSession();
					try {
						indexStateDao.markFailed(state, e.getMessage());
						dbInstance.commitAndCloseSession();
					} catch (Exception ex) {
						dbInstance.rollbackAndCloseSession();
					}
				}
			}

			if (indexed > 0) {
				log.info("TaxonomyEmbeddingIndexing: completed, indexed {} levels", indexed);
				if (indexStateDao.countByStatuses(List.of(TaxonomyLevelIndexState.IndexStatus.scheduled,
						TaxonomyLevelIndexState.IndexStatus.indexing)) == 0) {
					matchingModule.setPgVectorActive(true);
					org.olat.core.util.coordinate.CoordinatorManager.getInstance().getCoordinator()
							.getEventBus().fireEventToListenersOf(
									new TaxonomyMatchingReindexEvent(),
									OresHelper.createOLATResourceableType("TaxonomyMatching"));
				}
			}
		}

		private Map<Long, TaxonomyLevel> buildLevelMap(TaxonomyLevel level) {
			try {
				Taxonomy taxonomy = level.getTaxonomy();
				List<TaxonomyLevel> levels = taxonomyLevelDao.getLevels(List.of(taxonomy));
				return levels.stream().collect(Collectors.toMap(TaxonomyLevel::getKey, Function.identity()));
			} catch (Exception e) {
				return Map.of();
			}
		}
	}
}
