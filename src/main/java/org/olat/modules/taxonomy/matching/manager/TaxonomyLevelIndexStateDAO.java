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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexState;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexState.IndexStatus;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexStateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO for the durable taxonomy level indexing queue table {@code o_tax_level_index_state}.
 *
 * Initial date: 2026-06-19<br>
 * @author uhensler, https://www.frentix.com
 */
@Service
public class TaxonomyLevelIndexStateDAO {

	@Autowired
	private DB dbInstance;

	/**
	 * Upsert a single level to {@code scheduled}. If a row already exists the
	 * status is reset to {@code scheduled}; attempt count and error are cleared
	 * only on a fresh insert. On update we preserve attempt count so the nightly
	 * job can still enforce maxAttempts after repeated transient errors.
	 */
	public void upsertScheduled(TaxonomyLevel level) {
		TaxonomyLevelIndexStateImpl existing = findByLevel(level);
		if (existing != null) {
			if (existing.getStatus() != IndexStatus.scheduled) {
				existing.setStatus(IndexStatus.scheduled);
				existing.setLastModified(new Date());
				dbInstance.getCurrentEntityManager().merge(existing);
			}
			return;
		}
		TaxonomyLevelIndexStateImpl state = new TaxonomyLevelIndexStateImpl();
		Date now = new Date();
		state.setCreationDate(now);
		state.setLastModified(now);
		state.setLevel(level);
		state.setStatus(IndexStatus.scheduled);
		state.setAttemptCount(0);
		dbInstance.getCurrentEntityManager().persist(state);
	}

	/**
	 * Bulk-upsert all levels whose materialized path keys start with the given
	 * level's path (i.e. the level itself and all descendants).
	 */
	public void scheduleSubtree(TaxonomyLevel level) {
		String pathPrefix = level.getMaterializedPathKeys();

		String updateSql = """
				update ctaxonomylevelindexstate s set s.status = :scheduled, s.lastModified = :now
				 where s.level.key in (
				   select l.key from org.olat.modules.taxonomy.model.TaxonomyLevelImpl l
				    where l.materializedPathKeys like :pathPrefix or l.key = :levelKey
				 )
				 and s.status != :scheduled""";
		dbInstance.getCurrentEntityManager()
				.createQuery(updateSql)
				.setParameter("scheduled", IndexStatus.scheduled)
				.setParameter("now", new Date())
				.setParameter("pathPrefix", pathPrefix + "%")
				.setParameter("levelKey", level.getKey())
				.executeUpdate();
		dbInstance.commitAndCloseSession();

		String insertSubtreeSql = """
				select l from org.olat.modules.taxonomy.model.TaxonomyLevelImpl l
				 where (l.materializedPathKeys like :pathPrefix or l.key = :levelKey)
				 and l.key not in (select s.level.key from ctaxonomylevelindexstate s)""";
		List<?> missing = dbInstance.getCurrentEntityManager()
				.createQuery(insertSubtreeSql)
				.setParameter("pathPrefix", pathPrefix + "%")
				.setParameter("levelKey", level.getKey())
				.getResultList();
		insertMissingScheduled(missing);
	}

	/**
	 * Atomically claim the next {@code scheduled} row by transitioning it to
	 * {@code indexing}. Returns null when the ledger is empty.
	 */
	public TaxonomyLevelIndexState claimNextScheduled() {
		List<TaxonomyLevelIndexStateImpl> candidates = dbInstance.getCurrentEntityManager()
				.createQuery("""
						select s from ctaxonomylevelindexstate s
						 where s.status = :status
						 order by s.creationDate asc""", TaxonomyLevelIndexStateImpl.class)
				.setParameter("status", IndexStatus.scheduled)
				.setMaxResults(1)
				.getResultList();
		if (candidates.isEmpty()) {
			return null;
		}
		TaxonomyLevelIndexStateImpl candidate = candidates.get(0);
		candidate.setStatus(IndexStatus.indexing);
		candidate.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(candidate);
	}

	public void markIndexed(TaxonomyLevelIndexState state, String modelId, String modelVersion) {
		state.setStatus(IndexStatus.indexed);
		state.setIndexedModelId(modelId);
		state.setIndexedModelVersion(modelVersion);
		state.setLastIndexDate(new Date());
		state.setLastError(null);
		state.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().merge(state);
	}

	public void markFailed(TaxonomyLevelIndexState state, String errorMessage) {
		state.setStatus(IndexStatus.failed);
		state.setAttemptCount(state.getAttemptCount() + 1);
		state.setLastError(errorMessage != null && errorMessage.length() > 4000
				? errorMessage.substring(0, 4000)
				: errorMessage);
		state.setLastModified(new Date());
		dbInstance.getCurrentEntityManager().merge(state);
	}

	/**
	 * On boot: reset any rows stuck in {@code indexing} back to {@code scheduled}
	 * (they were in-flight when the JVM died).
	 */
	public int resetInWorkToScheduled() {
		return dbInstance.getCurrentEntityManager()
				.createQuery("""
						update ctaxonomylevelindexstate s
						 set s.status = :scheduled, s.lastModified = :now
						 where s.status = :indexing""")
				.setParameter("scheduled", IndexStatus.scheduled)
				.setParameter("indexing", IndexStatus.indexing)
				.setParameter("now", new Date())
				.executeUpdate();
	}

	/**
	 * Nightly: re-schedule {@code failed} rows that have not exceeded
	 * {@code maxAttempts}.
	 */
	public int rescheduleFailed(int maxAttempts) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("""
						update ctaxonomylevelindexstate s
						 set s.status = :scheduled, s.lastModified = :now
						 where s.status = :failed and s.attemptCount < :maxAttempts""")
				.setParameter("scheduled", IndexStatus.scheduled)
				.setParameter("failed", IndexStatus.failed)
				.setParameter("now", new Date())
				.setParameter("maxAttempts", maxAttempts)
				.executeUpdate();
	}

	/**
	 * Nightly: re-schedule rows that were indexed with a different model id
	 * (model drift detection).
	 */
	public int rescheduleStaleModel(String currentModelId) {
		if (currentModelId == null || currentModelId.isBlank()) {
			return 0;
		}
		return dbInstance.getCurrentEntityManager()
				.createQuery("""
						update ctaxonomylevelindexstate s
						 set s.status = :scheduled, s.lastModified = :now
						 where s.status = :indexed and (s.indexedModelId is null or s.indexedModelId != :modelId)""")
				.setParameter("scheduled", IndexStatus.scheduled)
				.setParameter("indexed", IndexStatus.indexed)
				.setParameter("now", new Date())
				.setParameter("modelId", currentModelId)
				.executeUpdate();
	}

	/**
	 * Bulk-schedule all levels across all taxonomies (used at start of full reindex).
	 */
	public int scheduleAll() {
		int updated = dbInstance.getCurrentEntityManager()
				.createQuery("""
						update ctaxonomylevelindexstate s
						 set s.status = :scheduled, s.lastModified = :now
						 where s.status != :scheduled""")
				.setParameter("scheduled", IndexStatus.scheduled)
				.setParameter("now", new Date())
				.executeUpdate();
		dbInstance.commitAndCloseSession();

		List<?> allLevels = dbInstance.getCurrentEntityManager()
				.createQuery("""
						select l from org.olat.modules.taxonomy.model.TaxonomyLevelImpl l
						 where l.key not in (select s.level.key from ctaxonomylevelindexstate s)""")
				.getResultList();
		insertMissingScheduled(allLevels);
		return updated;
	}

	private void insertMissingScheduled(List<?> missing) {
		for (Object obj : missing) {
			if (obj instanceof TaxonomyLevel level) {
				try {
					insertScheduled(level);
					dbInstance.commitAndCloseSession();
				} catch (Exception e) {
					dbInstance.rollbackAndCloseSession();
				}
			}
		}
	}

	private void insertScheduled(TaxonomyLevel level) {
		TaxonomyLevelIndexStateImpl state = new TaxonomyLevelIndexStateImpl();
		Date now = new Date();
		state.setCreationDate(now);
		state.setLastModified(now);
		state.setLevel(level);
		state.setStatus(IndexStatus.scheduled);
		state.setAttemptCount(0);
		dbInstance.getCurrentEntityManager().persist(state);
	}

	public void deleteByLevel(TaxonomyLevelRef level) {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from ctaxonomylevelindexstate s where s.level.key = :levelKey")
				.setParameter("levelKey", level.getKey())
				.executeUpdate();
	}

	public long countByStatus(IndexStatus status) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select count(s) from ctaxonomylevelindexstate s where s.status = :status", Long.class)
				.setParameter("status", status)
				.getSingleResult();
	}

	public long countByStatuses(Collection<IndexStatus> statuses) {
		return dbInstance.getCurrentEntityManager()
				.createQuery("select count(s) from ctaxonomylevelindexstate s where s.status in :statuses", Long.class)
				.setParameter("statuses", statuses)
				.getSingleResult();
	}

	public List<TaxonomyLevelIndexStateImpl> findFailed() {
		return dbInstance.getCurrentEntityManager()
				.createQuery("""
						select s from ctaxonomylevelindexstate s
						 where s.status = :failed
						 order by s.lastModified desc""", TaxonomyLevelIndexStateImpl.class)
				.setParameter("failed", IndexStatus.failed)
				.getResultList();
	}

	private TaxonomyLevelIndexStateImpl findByLevel(TaxonomyLevel level) {
		List<TaxonomyLevelIndexStateImpl> results = dbInstance.getCurrentEntityManager()
				.createQuery("""
						select s from ctaxonomylevelindexstate s
						 where s.level.key = :levelKey""", TaxonomyLevelIndexStateImpl.class)
				.setParameter("levelKey", level.getKey())
				.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
}
