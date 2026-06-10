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
package org.olat.core.commons.services.ai.manager;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 *
 * Facade over the two dedicated AI thread pools of the generic task
 * executor ({@code aiInteractive} / {@code aiBatch} queues, defined in
 * {@code taskExecutorCorecontext.xml}). Provides runtime resizing — the
 * pool sizes are configured in the AI module and depend entirely on the
 * infrastructure behind the AI provider (cloud API vs. self-hosted
 * GPU) — and exposes running/waiting counts for the admin UI.
 * <p>
 * Sizes are applied per node: in a cluster the effective concurrency
 * against the provider is {@code poolSize * numberOfNodes}.
 *
 * Initial date: 2026-06-10<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiTaskExecutorService {

	private static final Logger log = Tracing.createLoggerFor(AiTaskExecutorService.class);

	@Autowired
	@Qualifier("aiInteractiveTaskSpringExecutor")
	private ThreadPoolTaskExecutor aiInteractiveExecutor;
	@Autowired
	@Qualifier("aiBatchTaskSpringExecutor")
	private ThreadPoolTaskExecutor aiBatchExecutor;

	/**
	 * Resize the interactive AI pool (essay correction at learner submit).
	 * Takes effect immediately; queued tasks are not lost.
	 */
	public void setInteractivePoolSize(int size) {
		resize(aiInteractiveExecutor, size, "ai-interactive");
	}

	/**
	 * Resize the batch AI pool (question generation and similar long jobs).
	 * Takes effect immediately; queued tasks are not lost.
	 */
	public void setBatchPoolSize(int size) {
		resize(aiBatchExecutor, size, "ai-batch");
	}

	public AiTaskQueueStats getInteractiveStats() {
		return stats(aiInteractiveExecutor);
	}

	public AiTaskQueueStats getBatchStats() {
		return stats(aiBatchExecutor);
	}

	/**
	 * Apply a new pool size with core == max. Order matters with the
	 * underlying {@code ThreadPoolExecutor}: growing must raise max before
	 * core, shrinking must lower core before max, otherwise the executor
	 * rejects the intermediate state (core > max).
	 */
	private void resize(ThreadPoolTaskExecutor executor, int size, String poolName) {
		if (size < 1) {
			log.warn("Ignoring invalid pool size {} for {}", size, poolName);
			return;
		}
		int current = executor.getMaxPoolSize();
		if (size == current && size == executor.getCorePoolSize()) {
			return;
		}
		if (size > current) {
			executor.setMaxPoolSize(size);
			executor.setCorePoolSize(size);
		} else {
			executor.setCorePoolSize(size);
			executor.setMaxPoolSize(size);
		}
		log.info("AI task pool {} resized from {} to {} threads", poolName, current, size);
	}

	private AiTaskQueueStats stats(ThreadPoolTaskExecutor executor) {
		return new AiTaskQueueStats(
				executor.getActiveCount(),
				executor.getThreadPoolExecutor().getQueue().size(),
				executor.getMaxPoolSize());
	}

	/**
	 * Point-in-time snapshot of one AI pool (this node only).
	 *
	 * @param running  tasks currently executing
	 * @param waiting  tasks queued on this node, not yet started
	 * @param poolSize configured number of worker threads
	 */
	public record AiTaskQueueStats(int running, int waiting, int poolSize) { }
}
