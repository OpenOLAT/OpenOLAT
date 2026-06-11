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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.NativeQueryBuilder;
import org.olat.core.logging.Tracing;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.matching.PgVectorType;
import org.olat.modules.taxonomy.matching.TaxonomyEmbeddingTextVariant;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelEmbeddingImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO for taxonomy level embedding vectors.
 * <p>
 * When {@link TaxonomyMatchingModule#isPgVectorActive()} is true, the
 * {@code t_vector} column (pgvector) is used and similarity search is
 * delegated to the HNSW index. Otherwise, vectors are stored as JSON in
 * {@code t_vector_json} and cosine similarity is computed in-memory by the
 * caller.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
@Service
public class TaxonomyLevelEmbeddingDAO {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyLevelEmbeddingDAO.class);

	@Autowired
	private DB dbInstance;

	public TaxonomyLevelEmbeddingImpl upsert(TaxonomyLevel level, String locale, TaxonomyEmbeddingTextVariant textVariant,
			String embeddingText, float[] vector, String modelId, String modelVersion, PgVectorType vectorType) {
		TaxonomyLevelEmbeddingImpl existing = findByLevelLocaleModelTextVariant(level, locale, modelId, textVariant);
		if (existing != null) {
			existing.setLastModified(new Date());
			existing.setEmbeddingText(embeddingText);
			existing.setModelVersion(modelVersion);
			existing = dbInstance.getCurrentEntityManager().merge(existing);
			updateVector(existing.getKey(), vector, vectorType);
			return existing;
		}

		TaxonomyLevelEmbeddingImpl emb = new TaxonomyLevelEmbeddingImpl();
		Date now = new Date();
		emb.setCreationDate(now);
		emb.setLastModified(now);
		emb.setLevel(level);
		emb.setTaxonomy(level.getTaxonomy());
		emb.setLocale(locale);
		emb.setTextVariant(textVariant);
		emb.setEmbeddingText(embeddingText);
		emb.setModelId(modelId);
		emb.setModelVersion(modelVersion);
		try {
			dbInstance.getCurrentEntityManager().persist(emb);
			dbInstance.getCurrentEntityManager().flush();
		} catch (Exception e) {
			dbInstance.rollbackAndCloseSession();
			TaxonomyLevelEmbeddingImpl raced = findByLevelLocaleModelTextVariant(level, locale, modelId, textVariant);
			if (raced != null) {
				raced.setLastModified(new Date());
				raced.setEmbeddingText(embeddingText);
				raced.setModelVersion(modelVersion);
				raced = dbInstance.getCurrentEntityManager().merge(raced);
				updateVector(raced.getKey(), vector, vectorType);
				return raced;
			}
			throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
		}
		updateVector(emb.getKey(), vector, vectorType);
		return emb;
	}

	private void updateVector(Long embeddingKey, float[] vector, PgVectorType vectorType) {
		NativeQueryBuilder sb = new NativeQueryBuilder(160, dbInstance);
		sb.append("update o_tax_level_embedding set t_vector_json = :json");
		if (vectorType != null) {
			sb.append(", t_vector = cast(:vec as ").append(vectorType.sqlType()).append(")");
		}
		sb.append(" where id = :key");

		Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sb.toString())
			.setParameter("json", vectorToJson(vector))
			.setParameter("key", embeddingKey);
		if (vectorType != null) {
			query.setParameter("vec", vectorToString(vector));
		}
		query.executeUpdate();
	}

	public TaxonomyLevelEmbeddingImpl findByLevelLocaleModelTextVariant(TaxonomyLevel level, String locale, String modelId,
			TaxonomyEmbeddingTextVariant textVariant) {
		String q = """
				select e from ctaxonomylevelembedding e
				 where e.level.key=:levelKey and e.locale=:locale and e.modelId=:modelId and e.textVariant=:textVariant""";
		List<TaxonomyLevelEmbeddingImpl> results = dbInstance.getCurrentEntityManager()
				.createQuery(q, TaxonomyLevelEmbeddingImpl.class)
				.setParameter("levelKey", level.getKey())
				.setParameter("locale", locale)
				.setParameter("modelId", modelId)
				.setParameter("textVariant", textVariant)
				.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<TaxonomyLevelEmbeddingWithVector> findByTaxonomy(TaxonomyRef taxonomy, PgVectorType vectorType) {
		if (vectorType != null) {
			String sql = """
					select e.id, e.fk_level, e.t_locale, e.t_embedding_text, e.t_model_id,
					 e.t_model_version, e.t_vector::text
					 from o_tax_level_embedding e
					 where e.fk_taxonomy = :taxKey""";
			Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sql)
					.setParameter("taxKey", taxonomy.getKey());
			return buildEmbeddingsFromRows(query.getResultList());
		}
		
		String sql = """
				select e.id, e.fk_level, e.t_locale, e.t_embedding_text, e.t_model_id,
				 e.t_model_version, e.t_vector_json
				 from o_tax_level_embedding e
				 where e.fk_taxonomy = :taxKey""";
		Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sql)
				.setParameter("taxKey", taxonomy.getKey());
		return buildEmbeddingsFromRows(query.getResultList());
	}

	/**
	 * Find the {@code limit} most similar embeddings using pgvector's cosine distance operator.
	 * Falls back to loading all embeddings for in-memory cosine search when vectorType is null.
	 */
	@SuppressWarnings("unchecked")
	public List<TaxonomyLevelEmbeddingWithVector> findSimilar(TaxonomyRef taxonomy, float[] queryVector, int limit, PgVectorType vectorType) {
		if (vectorType != null) {
			String sql = """
					select e.id, e.fk_level, e.t_locale, e.t_embedding_text, e.t_model_id,
					 e.t_model_version, e.t_vector::text,
					 1 - (e.t_vector <=> cast(:vec as %s)) as score
					 from o_tax_level_embedding e
					 where e.fk_taxonomy = :taxKey
					 order by e.t_vector <=> cast(:vec as %s)
					 limit :lim""".formatted(vectorType.sqlType(), vectorType.sqlType());
			Query query = dbInstance.getCurrentEntityManager().createNativeQuery(sql)
					.setParameter("vec", vectorToString(queryVector))
					.setParameter("taxKey", taxonomy.getKey())
					.setParameter("lim", limit);
			return buildEmbeddingsFromRowsWithScore(query.getResultList());
		}
		return findByTaxonomy(taxonomy, null);
	}

	public int deleteAll() {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from ctaxonomylevelembedding e")
				.executeUpdate();
	}

	public int deleteByLevel(TaxonomyLevelRef level) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from ctaxonomylevelembedding e where e.level.key=:levelKey")
				.setParameter("levelKey", level.getKey())
				.executeUpdate();
	}

	public int deleteByTaxonomy(TaxonomyRef taxonomy) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("delete from ctaxonomylevelembedding e where e.taxonomy.key=:taxKey")
				.setParameter("taxKey", taxonomy.getKey())
				.executeUpdate();
	}

	public long countByTaxonomy(TaxonomyRef taxonomy, String modelId) {
		String q = """
				select count(e) from ctaxonomylevelembedding e
				 where e.taxonomy.key=:taxKey""";
		if (modelId != null) {
			q += " and e.modelId=:modelId";
		}
		var jq = dbInstance.getCurrentEntityManager().createQuery(q, Long.class)
				.setParameter("taxKey", taxonomy.getKey());
		if (modelId != null) {
			jq.setParameter("modelId", modelId);
		}
		return jq.getSingleResult();
	}

	/**
	 * Ensures the {@code t_vector} column and (optionally) the HNSW index exist with
	 * the correct type for the given embedding dimension.
	 *
	 * pgvector dimension limits for HNSW index:
	 *   vector  (float32): max 2000 dims — full precision, fast search
	 *   halfvec (float16): max 4000 dims — half precision, same cosine API, ~2× storage savings
	 *
	 * For dim > 2000 we switch to halfvec to keep HNSW support up to 4000 dims.
	 * For dim > 4000 (e.g. Qwen3-Embedding at 4096) halfvec is stored without an HNSW index;
	 * pgvector falls back to exact sequential scan — acceptable for thousands of taxonomy levels.
	 *
	 * @return the resolved column type on success, null on failure
	 */
	public PgVectorType ensureVectorColumnAndIndex(int dim, boolean allowRebuild) {
		PgVectorType colType = (dim <= 2000) ? PgVectorType.VECTOR : PgVectorType.HALFVEC;
		String indexOps = (dim <= 2000) ? "vector_cosine_ops" : "halfvec_cosine_ops";
		boolean hnswSupported = (dim <= 4000);

		try {
			List<?> colRows = dbInstance.getCurrentEntityManager()
					.createNativeQuery("""
							select a.atttypid::regtype, a.atttypmod from pg_attribute a
							 where a.attrelid = 'o_tax_level_embedding'::regclass
							 and a.attname = 't_vector' and a.attnum > 0 and not a.attisdropped""")
					.getResultList();

			if (!colRows.isEmpty()) {
				Object[] row = (Object[]) colRows.get(0);
				String existingType = String.valueOf(row[0]);
				int existingTypmod = ((Number) row[1]).intValue();
				int existingDim = existingTypmod;
				if (existingDim == dim && existingType.contains(colType.sqlType())) {
					ensureHnswIndex(indexOps, hnswSupported);
					return colType;
				}
				if (!allowRebuild) {
					log.warn("pgvector column mismatch ({} {} != {} {}): reindex required — leaving column untouched",
							existingType, existingDim, colType.sqlType(), dim);
					return null;
				}
				log.info("Embedding column change ({} {} -> {} {}): recreating t_vector",
						existingType, existingDim, colType.sqlType(), dim);
				dbInstance.getCurrentEntityManager()
						.createNativeQuery("drop index if exists idx_tax_emb_vector")
						.executeUpdate();
				dbInstance.getCurrentEntityManager()
						.createNativeQuery("alter table o_tax_level_embedding drop column t_vector")
						.executeUpdate();
			} else if (!allowRebuild) {
				log.warn("pgvector column missing for dim {}: reindex required", dim);
				return null;
			}

			dbInstance.getCurrentEntityManager()
					.createNativeQuery("alter table o_tax_level_embedding add column t_vector %s(%d)".formatted(colType.sqlType(), dim))
					.executeUpdate();

			if (hnswSupported) {
				dbInstance.getCurrentEntityManager()
						.createNativeQuery("create index idx_tax_emb_vector on o_tax_level_embedding using hnsw (t_vector %s) with (m = 16, ef_construction = 64)".formatted(indexOps))
						.executeUpdate();
				log.info("Created t_vector {}({}) column with HNSW index on o_tax_level_embedding", colType.sqlType(), dim);
			} else {
				log.info("Created t_vector {}({}) column on o_tax_level_embedding"
						+ " (dim > 4000: HNSW not supported, sequential scan will be used)", colType.sqlType(), dim);
			}
			return colType;
		} catch (Exception e) {
			log.warn("Failed to ensure pgvector column/index (pgvector may not be installed): {}", e.getMessage());
			try {
				dbInstance.rollbackAndCloseSession();
			} catch (Exception re) {
				log.debug("Rollback after pgvector DDL failure: {}", re.getMessage());
			}
			return null;
		}
	}

	private void ensureHnswIndex(String indexOps, boolean hnswSupported) {
		if (!hnswSupported) {
			return;
		}
		try {
			List<?> idxRows = dbInstance.getCurrentEntityManager()
					.createNativeQuery("""
							select 1 from pg_indexes
							 where tablename = 'o_tax_level_embedding'
							 and indexname = 'idx_tax_emb_vector'""")
					.getResultList();
			if (idxRows.isEmpty()) {
				dbInstance.getCurrentEntityManager()
						.createNativeQuery("create index idx_tax_emb_vector on o_tax_level_embedding using hnsw (t_vector %s) with (m = 16, ef_construction = 64)".formatted(indexOps))
						.executeUpdate();
				log.info("Recreated missing idx_tax_emb_vector HNSW index");
			}
		} catch (Exception e) {
			log.warn("Failed to ensure HNSW index: {}", e.getMessage(), e);
		}
	}

	// --- helpers ---

	private String vectorToString(float[] vector) {
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < vector.length; i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(vector[i]);
		}
		sb.append(']');
		return sb.toString();
	}

	private String vectorToJson(float[] vector) {
		return Arrays.toString(vector);
	}

	private float[] parseVector(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String trimmed = raw.trim().replaceAll("[\\[\\]]", "");
		String[] parts = trimmed.split(",");
		float[] result = new float[parts.length];
		for (int i = 0; i < parts.length; i++) {
			result[i] = Float.parseFloat(parts[i].trim());
		}
		return result;
	}

	private List<TaxonomyLevelEmbeddingWithVector> buildEmbeddingsFromRows(List<Object[]> rows) {
		List<TaxonomyLevelEmbeddingWithVector> result = new ArrayList<>(rows.size());
		for (Object[] row : rows) {
			TaxonomyLevelEmbeddingWithVector e = new TaxonomyLevelEmbeddingWithVector();
			e.setId(toLong(row[0]));
			e.setLevelKey(toLong(row[1]));
			e.setLocale((String) row[2]);
			e.setEmbeddingText((String) row[3]);
			e.setModelId((String) row[4]);
			e.setModelVersion((String) row[5]);
			e.setVector(parseVector((String) row[6]));
			result.add(e);
		}
		return result;
	}

	private List<TaxonomyLevelEmbeddingWithVector> buildEmbeddingsFromRowsWithScore(List<Object[]> rows) {
		List<TaxonomyLevelEmbeddingWithVector> result = new ArrayList<>(rows.size());
		for (Object[] row : rows) {
			TaxonomyLevelEmbeddingWithVector e = new TaxonomyLevelEmbeddingWithVector();
			e.setId(toLong(row[0]));
			e.setLevelKey(toLong(row[1]));
			e.setLocale((String) row[2]);
			e.setEmbeddingText((String) row[3]);
			e.setModelId((String) row[4]);
			e.setModelVersion((String) row[5]);
			e.setVector(parseVector((String) row[6]));
			e.setScore(row[7] == null ? 0.0 : ((Number) row[7]).doubleValue());
			result.add(e);
		}
		return result;
	}

	private Long toLong(Object obj) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof Long l) {
			return l;
		}
		return ((Number) obj).longValue();
	}

	/**
	 * Value object returned by {@link #findByTaxonomy} and {@link #findSimilar}.
	 */
	static class TaxonomyLevelEmbeddingWithVector {
		private Long id;
		private Long levelKey;
		private String locale;
		private String embeddingText;
		private String modelId;
		private String modelVersion;
		private float[] vector;
		private double score;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Long getLevelKey() {
			return levelKey;
		}

		public void setLevelKey(Long levelKey) {
			this.levelKey = levelKey;
		}

		public String getLocale() {
			return locale;
		}

		public void setLocale(String locale) {
			this.locale = locale;
		}

		public String getEmbeddingText() {
			return embeddingText;
		}

		public void setEmbeddingText(String embeddingText) {
			this.embeddingText = embeddingText;
		}

		public String getModelId() {
			return modelId;
		}

		public void setModelId(String modelId) {
			this.modelId = modelId;
		}

		public String getModelVersion() {
			return modelVersion;
		}

		public void setModelVersion(String modelVersion) {
			this.modelVersion = modelVersion;
		}

		public float[] getVector() {
			return vector;
		}

		public void setVector(float[] vector) {
			this.vector = vector;
		}

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}
	}
}
