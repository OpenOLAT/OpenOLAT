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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Cluster-singleton nightly Quartz job that recovers stuck rows,
 * reschedules failed/stale-model rows, and starts the indexing worker.
 *
 * Initial date: 2026-06-19<br>
 * @author uhensler, https://www.frentix.com
 */
@DisallowConcurrentExecution
public class TaxonomyEmbeddingIndexJob extends JobWithDB {

	private static final Logger log = Tracing.createLoggerFor(TaxonomyEmbeddingIndexJob.class);

	private static final int MAX_ATTEMPTS = 5;

	@Override
	public void executeWithDB(JobExecutionContext context) throws JobExecutionException {
		AiModule aiModule = CoreSpringFactory.getImpl(AiModule.class);
		if (!aiModule.isTaxonomyMatchingEnabled()) {
			return;
		}

		TaxonomyLevelIndexStateDAO indexStateDao = CoreSpringFactory.getImpl(TaxonomyLevelIndexStateDAO.class);
		TaxonomyMatchingModule matchingModule = CoreSpringFactory.getImpl(TaxonomyMatchingModule.class);
		TaxonomyMatchingService matchingService = CoreSpringFactory.getImpl(TaxonomyMatchingService.class);

		try {
			int reset = indexStateDao.resetInWorkToScheduled();
			if (reset > 0) {
				log.info("TaxonomyEmbeddingIndexJob: reset {} stuck indexing rows to scheduled", reset);
			}

			int rescheduled = indexStateDao.rescheduleFailed(MAX_ATTEMPTS);
			if (rescheduled > 0) {
				log.info("TaxonomyEmbeddingIndexJob: rescheduled {} failed rows (maxAttempts={})", rescheduled, MAX_ATTEMPTS);
			}

			String currentModelId = matchingModule.getModel();
			int stale = indexStateDao.rescheduleStaleModel(currentModelId);
			if (stale > 0) {
				log.info("TaxonomyEmbeddingIndexJob: rescheduled {} stale-model rows for model '{}'", stale, currentModelId);
			}

			matchingService.startIndexing();
		} catch (Exception e) {
			log.error("TaxonomyEmbeddingIndexJob failed", e);
		}
	}
}
