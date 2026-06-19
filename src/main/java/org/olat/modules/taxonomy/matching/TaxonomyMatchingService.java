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
package org.olat.modules.taxonomy.matching;

import java.util.List;

import org.olat.core.commons.services.ai.model.AiUsageContext;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.matching.model.TaxonomyMatch;

/**
 * Service for semantic, embedding-based taxonomy level matching.
 * Falls back gracefully when the service is unconfigured or unavailable.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
public interface TaxonomyMatchingService {

	/**
	 * Find the {@code limit} most similar taxonomy levels for the given text.
	 * Only results with {@code score >= minScore} are included.
	 *
	 * @param text     free-text query (e.g. an AI-generated subject field)
	 * @param taxonomy the taxonomy to search within
	 * @param limit    maximum number of results
	 * @param minScore minimum cosine similarity (0.0 – 1.0)
	 * @return matches ordered descending by score, never null
	 */
	List<TaxonomyMatch> suggestLevels(AiUsageContext context, String text, TaxonomyRef taxonomy, int limit, double minScore);

	/**
	 * Schedule a single taxonomy level for async embedding indexing.
	 * Cheap: only writes a state row; does not call the embedding model.
	 * The caller must invoke {@link #startIndexing()} after all DB changes are committed.
	 */
	void scheduleIndex(TaxonomyLevel level);

	/**
	 * Schedule the given level and its entire subtree for async indexing.
	 * Used when a level is renamed or moved so descendants are also re-embedded.
	 */
	void scheduleSubtree(TaxonomyLevel level);

	/**
	 * Remove the index state row for a deleted taxonomy level.
	 */
	void deleteIndexState(TaxonomyLevelRef level);

	/**
	 * Start the async indexing worker if it is not already running.
	 * Must be called after the triggering transaction is committed so the worker
	 * reads up-to-date data.
	 */
	void startIndexing();

	/**
	 * Trigger a full asynchronous reindex of all taxonomy levels across all taxonomies.
	 * Supersedes any in-progress reindex via a generation counter.
	 */
	void scheduleFullReindex();

	/**
	 * Remove all stored embeddings for a deleted taxonomy level.
	 */
	void deleteEmbeddings(TaxonomyLevelRef level);
}
