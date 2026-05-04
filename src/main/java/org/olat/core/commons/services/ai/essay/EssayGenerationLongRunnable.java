/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai.essay;

import java.io.Serial;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.logging.Tracing;

/**
 *
 * {@link LongRunnable} wrapper persisted by the task executor. Thin —
 * it only carries the {@code jobKey} and delegates the actual work to
 * {@link EssayGenerationService#runJob(Long)} so we keep
 * database/identity/locale access behind the Spring-managed service
 * (the runnable itself is serialised to the tasks table and must stay
 * lightweight).
 * <p>
 * This mirrors the pattern of {@code UserDataExportTask}: the task holds
 * only the primary key of the row that describes the work, and the
 * service does the heavy lifting.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class EssayGenerationLongRunnable implements LongRunnable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger log = Tracing.createLoggerFor(EssayGenerationLongRunnable.class);

	private final Long jobKey;

	public EssayGenerationLongRunnable(Long jobKey) {
		this.jobKey = jobKey;
	}

	public Long getJobKey() {
		return jobKey;
	}

	@Override
	public Queue getExecutorsQueue() {
		return Queue.lowPriority;
	}

	@Override
	public boolean isDelayed() {
		return false;
	}

	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		try {
			EssayGenerationService service = CoreSpringFactory.getImpl(EssayGenerationService.class);
			service.runJob(jobKey);
		} catch (Exception e) {
			// The service already persists FAILED; re-throw so the task executor marks the task record failed too.
			log.error("Essay generation job {} failed", jobKey, e);
			throw e;
		} finally {
			log.info("Finished essay generation job {} in {} ms", jobKey, System.currentTimeMillis() - startTime);
		}
	}
}
