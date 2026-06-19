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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexState;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexState.IndexStatus;
import org.olat.modules.taxonomy.matching.model.TaxonomyLevelIndexStateImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for {@link TaxonomyLevelIndexStateDAO}.
 *
 * Initial date: 2026-06-19<br>
 * @author uhensler, https://www.frentix.com
 */
public class TaxonomyLevelIndexStateDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyLevelIndexStateDAO indexStateDao;

	// -----------------------------------------------------------------------
	// upsertScheduled
	// -----------------------------------------------------------------------

	@Test
	public void upsertScheduled_shouldInsertNewRow() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		long before = indexStateDao.countByStatus(IndexStatus.scheduled);

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		long after = indexStateDao.countByStatus(IndexStatus.scheduled);
		assertThat(after).isEqualTo(before + 1);
	}

	@Test
	public void upsertScheduled_shouldBeIdempotent() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		long countAfterFirst = indexStateDao.countByStatus(IndexStatus.scheduled);

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		long countAfterSecond = indexStateDao.countByStatus(IndexStatus.scheduled);
		assertThat(countAfterSecond).isEqualTo(countAfterFirst);
	}

	@Test
	public void upsertScheduled_shouldResetNonScheduledRowToScheduled() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		indexStateDao.markFailed(state, "boom");
		dbInstance.commitAndCloseSession();

		assertThat(state.getStatus()).isEqualTo(IndexStatus.failed);

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl reloaded = loadStateByLevel(level);
		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getStatus()).isEqualTo(IndexStatus.scheduled);
	}

	// -----------------------------------------------------------------------
	// scheduleSubtree
	// -----------------------------------------------------------------------

	@Test
	public void scheduleSubtree_shouldScheduleLevelAndDescendants() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel root = createLevel(taxonomy, null);
		TaxonomyLevel child = createLevel(taxonomy, root);
		createLevel(taxonomy, child);
		dbInstance.commitAndCloseSession();

		long before = indexStateDao.countByStatus(IndexStatus.scheduled);

		indexStateDao.scheduleSubtree(root);
		dbInstance.commitAndCloseSession();

		long after = indexStateDao.countByStatus(IndexStatus.scheduled);
		assertThat(after).isEqualTo(before + 3);
	}

	@Test
	public void scheduleSubtree_shouldNotTouchUnrelatedLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel root = createLevel(taxonomy, null);
		TaxonomyLevel unrelated = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(unrelated);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState unrelatedState = claimByLevel(unrelated);
		assertThat(unrelatedState).isNotNull();
		indexStateDao.markIndexed(unrelatedState, "model-1", "v1");
		dbInstance.commitAndCloseSession();

		assertThat(unrelatedState.getStatus()).isEqualTo(IndexStatus.indexed);

		indexStateDao.scheduleSubtree(root);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl unrelatedReloaded = loadStateByLevel(unrelated);
		assertThat(unrelatedReloaded).isNotNull();
		assertThat(unrelatedReloaded.getStatus()).isEqualTo(IndexStatus.indexed);
	}

	// -----------------------------------------------------------------------
	// claimNextScheduled
	// -----------------------------------------------------------------------

	@Test
	public void claimNextScheduled_shouldTransitionRowToIndexing() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState claimed = claimNextScheduledUntilLevel(level);

		assertThat(claimed).isNotNull();
		assertThat(claimed.getStatus()).isEqualTo(IndexStatus.indexing);
		assertThat(claimed.getLevel().getKey()).isEqualTo(level.getKey());
	}

	@Test
	public void claimNextScheduled_shouldNotClaimRowInIndexingStatus() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState claimed = claimNextScheduledUntilLevel(level);
		assertThat(claimed).isNotNull();
		assertThat(claimed.getStatus()).isEqualTo(IndexStatus.indexing);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState reloaded = loadStateByLevel(level);
		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getStatus()).isEqualTo(IndexStatus.indexing);

		TaxonomyLevelIndexState second = claimByLevel(level);
		assertThat(second).isNull();
	}

	@Test
	public void claimNextScheduled_consecutiveCallsShouldReturnDifferentRows() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelA);
		indexStateDao.upsertScheduled(levelB);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState claimedA = claimNextScheduledUntilLevel(levelA);
		TaxonomyLevelIndexState claimedB = claimNextScheduledUntilLevel(levelB);

		assertThat(claimedA).isNotNull();
		assertThat(claimedB).isNotNull();
		assertThat(claimedA.getKey()).isNotEqualTo(claimedB.getKey());
	}

	// -----------------------------------------------------------------------
	// markIndexed
	// -----------------------------------------------------------------------

	@Test
	public void markIndexed_shouldSetStatusAndRecordModel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();

		indexStateDao.markIndexed(state, "model-x", "v2");
		dbInstance.commitAndCloseSession();

		assertThat(state.getStatus()).isEqualTo(IndexStatus.indexed);
		assertThat(state.getIndexedModelId()).isEqualTo("model-x");
		assertThat(state.getIndexedModelVersion()).isEqualTo("v2");
		assertThat(state.getLastIndexDate()).isNotNull();
		assertThat(state.getLastError()).isNull();
	}

	// -----------------------------------------------------------------------
	// markFailed
	// -----------------------------------------------------------------------

	@Test
	public void markFailed_shouldSetStatusAndIncrementAttemptCount() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		int attemptsBefore = state.getAttemptCount();

		indexStateDao.markFailed(state, "connection timeout");
		dbInstance.commitAndCloseSession();

		assertThat(state.getStatus()).isEqualTo(IndexStatus.failed);
		assertThat(state.getAttemptCount()).isEqualTo(attemptsBefore + 1);
		assertThat(state.getLastError()).isEqualTo("connection timeout");
	}

	// -----------------------------------------------------------------------
	// resetInWorkToScheduled
	// -----------------------------------------------------------------------

	@Test
	public void resetInWorkToScheduled_shouldResetIndexingRowsToScheduled() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState inWork = claimByLevel(level);
		assertThat(inWork).isNotNull();
		assertThat(inWork.getStatus()).isEqualTo(IndexStatus.indexing);
		dbInstance.commitAndCloseSession();

		long indexingBefore = indexStateDao.countByStatus(IndexStatus.indexing);
		assertThat(indexingBefore).isGreaterThanOrEqualTo(1L);

		int reset = indexStateDao.resetInWorkToScheduled();
		dbInstance.commitAndCloseSession();

		assertThat(reset).isGreaterThanOrEqualTo(1);
		assertThat(indexStateDao.countByStatus(IndexStatus.indexing)).isEqualTo(0L);

		TaxonomyLevelIndexStateImpl inWorkReloaded = loadStateByLevel(level);
		assertThat(inWorkReloaded).isNotNull();
		assertThat(inWorkReloaded.getStatus()).isEqualTo(IndexStatus.scheduled);
	}

	@Test
	public void resetInWorkToScheduled_shouldNotTouchOtherStatuses() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelA);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState stateA = claimByLevel(levelA);
		assertThat(stateA).isNotNull();
		indexStateDao.markIndexed(stateA, "m", "v1");
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelB);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState stateB = claimByLevel(levelB);
		assertThat(stateB).isNotNull();
		indexStateDao.markFailed(stateB, "err");
		dbInstance.commitAndCloseSession();

		indexStateDao.resetInWorkToScheduled();
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl stateAReloaded = loadStateByLevel(levelA);
		TaxonomyLevelIndexStateImpl stateBReloaded = loadStateByLevel(levelB);
		assertThat(stateAReloaded).isNotNull();
		assertThat(stateBReloaded).isNotNull();
		assertThat(stateAReloaded.getStatus()).isEqualTo(IndexStatus.indexed);
		assertThat(stateBReloaded.getStatus()).isEqualTo(IndexStatus.failed);
	}

	// -----------------------------------------------------------------------
	// rescheduleFailed
	// -----------------------------------------------------------------------

	@Test
	public void rescheduleFailed_shouldRescheduleRowsBelowMaxAttempts() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		indexStateDao.markFailed(state, "err");
		dbInstance.commitAndCloseSession();

		assertThat(state.getAttemptCount()).isEqualTo(1);

		indexStateDao.rescheduleFailed(3);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl reloaded = loadStateByLevel(level);
		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getStatus()).isEqualTo(IndexStatus.scheduled);
	}

	@Test
	public void rescheduleFailed_shouldNotRescheduleRowsAtOrAboveMaxAttempts() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		indexStateDao.markFailed(state, "err1");
		dbInstance.commitAndCloseSession();

		assertThat(state.getAttemptCount()).isEqualTo(1);

		indexStateDao.rescheduleFailed(1);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl reloaded = loadStateByLevel(level);
		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getStatus()).isEqualTo(IndexStatus.failed);
	}

	// -----------------------------------------------------------------------
	// rescheduleStaleModel
	// -----------------------------------------------------------------------

	@Test
	public void rescheduleStaleModel_shouldRescheduleRowsWithDifferentModelId() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		indexStateDao.markIndexed(state, "old-model", "v1");
		dbInstance.commitAndCloseSession();

		assertThat(state.getStatus()).isEqualTo(IndexStatus.indexed);

		indexStateDao.rescheduleStaleModel("new-model");
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl reloaded = loadStateByLevel(level);
		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getStatus()).isEqualTo(IndexStatus.scheduled);
	}

	@Test
	public void rescheduleStaleModel_shouldNotTouchRowsWithCurrentModelId() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		indexStateDao.markIndexed(state, "current-model", "v1");
		dbInstance.commitAndCloseSession();

		assertThat(state.getStatus()).isEqualTo(IndexStatus.indexed);

		indexStateDao.rescheduleStaleModel("current-model");
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl reloaded = loadStateByLevel(level);
		assertThat(reloaded).isNotNull();
		assertThat(reloaded.getStatus()).isEqualTo(IndexStatus.indexed);
	}

	@Test
	public void rescheduleStaleModel_shouldReturnZeroForBlankModelId() {
		int result = indexStateDao.rescheduleStaleModel("   ");
		assertThat(result).isEqualTo(0);

		int resultNull = indexStateDao.rescheduleStaleModel(null);
		assertThat(resultNull).isEqualTo(0);
	}

	// -----------------------------------------------------------------------
	// scheduleAll
	// -----------------------------------------------------------------------

	@Test
	public void scheduleAll_shouldSetAllExistingRowsToScheduled() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelA);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState stateA = claimByLevel(levelA);
		assertThat(stateA).isNotNull();
		indexStateDao.markIndexed(stateA, "m", "v1");

		indexStateDao.upsertScheduled(levelB);
		dbInstance.commitAndCloseSession();
		TaxonomyLevelIndexState stateB = claimByLevel(levelB);
		assertThat(stateB).isNotNull();
		indexStateDao.markFailed(stateB, "err");
		dbInstance.commitAndCloseSession();

		assertThat(stateA.getStatus()).isEqualTo(IndexStatus.indexed);
		assertThat(stateB.getStatus()).isEqualTo(IndexStatus.failed);

		indexStateDao.scheduleAll();
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexStateImpl stateAReloaded = loadStateByLevel(levelA);
		TaxonomyLevelIndexStateImpl stateBReloaded = loadStateByLevel(levelB);
		assertThat(stateAReloaded).isNotNull();
		assertThat(stateBReloaded).isNotNull();
		assertThat(stateAReloaded.getStatus()).isEqualTo(IndexStatus.scheduled);
		assertThat(stateBReloaded.getStatus()).isEqualTo(IndexStatus.scheduled);
	}

	// -----------------------------------------------------------------------
	// deleteByLevel
	// -----------------------------------------------------------------------

	@Test
	public void deleteByLevel_shouldDeleteOnlyTheTargetLevel() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelA);
		indexStateDao.upsertScheduled(levelB);
		dbInstance.commitAndCloseSession();

		long before = indexStateDao.countByStatus(IndexStatus.scheduled);

		indexStateDao.deleteByLevel(levelA);
		dbInstance.commitAndCloseSession();

		long after = indexStateDao.countByStatus(IndexStatus.scheduled);
		assertThat(after).isEqualTo(before - 1);
	}

	@Test
	public void deleteByLevel_shouldNotDeleteOtherLevels() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelA);
		indexStateDao.upsertScheduled(levelB);
		dbInstance.commitAndCloseSession();

		indexStateDao.deleteByLevel(levelA);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState claimedB = claimByLevel(levelB);
		assertThat(claimedB).isNotNull();
		assertThat(claimedB.getLevel().getKey()).isEqualTo(levelB.getKey());
	}

	// -----------------------------------------------------------------------
	// countByStatus
	// -----------------------------------------------------------------------

	@Test
	public void countByStatus_shouldReturnCorrectCountsAfterMixedStatuses() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		TaxonomyLevel levelC = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		long scheduledBefore = indexStateDao.countByStatus(IndexStatus.scheduled);
		long indexedBefore = indexStateDao.countByStatus(IndexStatus.indexed);
		long failedBefore = indexStateDao.countByStatus(IndexStatus.failed);

		indexStateDao.upsertScheduled(levelA);
		indexStateDao.upsertScheduled(levelB);
		indexStateDao.upsertScheduled(levelC);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState stateA = claimByLevel(levelA);
		assertThat(stateA).isNotNull();
		indexStateDao.markIndexed(stateA, "m", "v1");

		TaxonomyLevelIndexState stateB = claimByLevel(levelB);
		assertThat(stateB).isNotNull();
		indexStateDao.markFailed(stateB, "err");
		dbInstance.commitAndCloseSession();

		long scheduledAfter = indexStateDao.countByStatus(IndexStatus.scheduled);
		long indexedAfter = indexStateDao.countByStatus(IndexStatus.indexed);
		long failedAfter = indexStateDao.countByStatus(IndexStatus.failed);

		assertThat(scheduledAfter).isEqualTo(scheduledBefore + 1);
		assertThat(indexedAfter).isEqualTo(indexedBefore + 1);
		assertThat(failedAfter).isEqualTo(failedBefore + 1);
	}

	// -----------------------------------------------------------------------
	// findFailed
	// -----------------------------------------------------------------------

	@Test
	public void findFailed_shouldReturnOnlyFailedRows() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel levelA = createLevel(taxonomy, null);
		TaxonomyLevel levelB = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(levelA);
		indexStateDao.upsertScheduled(levelB);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState stateA = claimByLevel(levelA);
		assertThat(stateA).isNotNull();
		indexStateDao.markIndexed(stateA, "m", "v1");

		TaxonomyLevelIndexState stateB = claimByLevel(levelB);
		assertThat(stateB).isNotNull();
		indexStateDao.markFailed(stateB, "connection refused");
		dbInstance.commitAndCloseSession();

		List<TaxonomyLevelIndexStateImpl> failed = indexStateDao.findFailed();

		assertThat(failed).isNotEmpty();
		assertThat(failed).allMatch(s -> s.getStatus() == IndexStatus.failed);
		assertThat(failed).anyMatch(s -> s.getLevel().getKey().equals(levelB.getKey()));
		assertThat(failed).noneMatch(s -> s.getLevel().getKey().equals(levelA.getKey()));
	}

	@Test
	public void findFailed_shouldNotContainNonFailedRows() {
		Taxonomy taxonomy = taxonomyDao.createTaxonomy(random(), random(), null, null);
		TaxonomyLevel level = createLevel(taxonomy, null);
		dbInstance.commitAndCloseSession();

		indexStateDao.upsertScheduled(level);
		dbInstance.commitAndCloseSession();

		TaxonomyLevelIndexState state = claimByLevel(level);
		assertThat(state).isNotNull();
		indexStateDao.markIndexed(state, "m", "v1");
		dbInstance.commitAndCloseSession();

		List<TaxonomyLevelIndexStateImpl> failed = indexStateDao.findFailed();
		assertThat(failed).noneMatch(s -> s.getLevel().getKey().equals(level.getKey()));
	}

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------

	private TaxonomyLevel createLevel(Taxonomy taxonomy, TaxonomyLevel parent) {
		return taxonomyLevelDao.createTaxonomyLevel(random(), random(), random(), null, null, null, parent, null, taxonomy);
	}

	private TaxonomyLevelIndexStateImpl loadStateByLevel(TaxonomyLevel level) {
		List<TaxonomyLevelIndexStateImpl> results = dbInstance.getCurrentEntityManager()
				.createQuery("select s from ctaxonomylevelindexstate s where s.level.key = :levelKey",
						TaxonomyLevelIndexStateImpl.class)
				.setParameter("levelKey", level.getKey())
				.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	private TaxonomyLevelIndexState claimByLevel(TaxonomyLevel level) {
		TaxonomyLevelIndexStateImpl state = loadStateByLevel(level);
		if (state == null) {
			return null;
		}
		int rows = dbInstance.getCurrentEntityManager()
				.createQuery("update ctaxonomylevelindexstate s set s.status = :indexing, s.lastModified = :now"
						+ " where s.key = :key and s.status = :scheduled")
				.setParameter("indexing", IndexStatus.indexing)
				.setParameter("now", new java.util.Date())
				.setParameter("key", state.getKey())
				.setParameter("scheduled", IndexStatus.scheduled)
				.executeUpdate();
		if (rows == 0) {
			return null;
		}
		dbInstance.getCurrentEntityManager().refresh(state);
		return state;
	}

	private TaxonomyLevelIndexState claimNextScheduledUntilLevel(TaxonomyLevel level) {
		List<Long> sideClaimedKeys = new ArrayList<>();
		TaxonomyLevelIndexState result = null;
		for (int i = 0; i < 1000; i++) {
			TaxonomyLevelIndexState claimed = indexStateDao.claimNextScheduled();
			if (claimed == null) {
				break;
			}
			dbInstance.commitAndCloseSession();
			if (claimed.getLevel().getKey().equals(level.getKey())) {
				result = claimed;
				break;
			}
			sideClaimedKeys.add(claimed.getKey());
		}
		if (!sideClaimedKeys.isEmpty()) {
			for (Long key : sideClaimedKeys) {
				dbInstance.getCurrentEntityManager()
						.createQuery("update ctaxonomylevelindexstate s set s.status = :scheduled, s.lastModified = :now"
								+ " where s.key = :key")
						.setParameter("scheduled", IndexStatus.scheduled)
						.setParameter("now", new java.util.Date())
						.setParameter("key", key)
						.executeUpdate();
			}
			dbInstance.commitAndCloseSession();
		}
		return result;
	}
}
