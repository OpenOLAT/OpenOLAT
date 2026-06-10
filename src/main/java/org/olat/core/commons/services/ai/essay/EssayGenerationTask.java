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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationDestination;
import org.olat.core.commons.services.ai.essay.EssayGenerationService.GenerationRequest;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;

/**
 *
 * {@link LongRunnable} for an AI question-generation run, persisted by the
 * generic task executor in {@code o_ex_task}. The task is self-contained:
 * it carries the complete {@link GenerationRequest} payload as plain
 * serialisable fields (the requester only as identity key — the executor
 * row already stores the creator) and rebuilds the request at run time.
 * No companion job table is needed; failures are reflected on the
 * executor's task row, user-visible outcomes are written by the
 * destination sinks.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class EssayGenerationTask implements LongRunnable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger log = Tracing.createLoggerFor(EssayGenerationTask.class);

	private final String pageMarkdown;
	private final Long repositoryEntryKey;
	private final int targetQuestionCount;
	private final int mcQuestionCount;
	private final List<String> targetBloomLevels;
	private final Integer targetDifficulty;
	private final List<String> learningObjectives;
	private final String languageTag;
	private final Long requesterKey;
	private final Long pageKey;
	private final Long quizPartKey;
	private final String destination;
	private final Long taxonomyLevelKey;

	public EssayGenerationTask(GenerationRequest request) {
		pageMarkdown = request.pageMarkdown();
		repositoryEntryKey = request.repositoryEntryKey();
		targetQuestionCount = request.targetQuestionCount();
		mcQuestionCount = request.mcQuestionCount();
		// Plain ArrayList copies — the task is XStream-serialised into
		// o_ex_task and JDK-internal immutable list classes do not survive
		// that round-trip reliably.
		targetBloomLevels = new ArrayList<>();
		if (request.targetBloomLevels() != null) {
			for (AiBloomLevel level : request.targetBloomLevels()) {
				targetBloomLevels.add(level.name());
			}
		}
		targetDifficulty = request.targetDifficulty();
		learningObjectives = request.learningObjectives() == null ? new ArrayList<>()
				: new ArrayList<>(request.learningObjectives());
		languageTag = request.language() == null ? null : request.language().toLanguageTag();
		requesterKey = request.requester() == null ? null : request.requester().getKey();
		pageKey = request.pageKey();
		quizPartKey = request.quizPartKey();
		destination = request.destination() == null ? null : request.destination().name();
		taxonomyLevelKey = request.taxonomyLevelKey();
	}

	public Long getRequesterKey() {
		return requesterKey;
	}

	/**
	 * Rebuild the {@link GenerationRequest} this task was created from.
	 *
	 * @param requester the resolved requester identity (loaded from
	 *                  {@link #getRequesterKey()} by the service)
	 */
	public GenerationRequest toGenerationRequest(Identity requester) {
		List<AiBloomLevel> bloomLevels = new ArrayList<>(targetBloomLevels.size());
		for (String name : targetBloomLevels) {
			try {
				bloomLevels.add(AiBloomLevel.valueOf(name));
			} catch (IllegalArgumentException e) {
				log.warn("Unknown Bloom level '{}' in persisted generation task — ignoring", name);
			}
		}
		Locale language = languageTag == null ? null : Locale.forLanguageTag(languageTag);
		GenerationDestination dest = null;
		if (destination != null) {
			try {
				dest = GenerationDestination.valueOf(destination);
			} catch (IllegalArgumentException e) {
				log.warn("Unknown destination '{}' in persisted generation task — using fallback", destination);
			}
		}
		return new GenerationRequest(pageMarkdown, repositoryEntryKey,
				targetQuestionCount, mcQuestionCount, bloomLevels, targetDifficulty,
				learningObjectives, language, requester, pageKey, quizPartKey,
				dest, taxonomyLevelKey);
	}

	@Override
	public Queue getExecutorsQueue() {
		// Long-running batch work (multiple sequential LLM calls) — must not
		// block interactive corrections, hence the separate batch pool.
		return Queue.aiBatch;
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
			service.runTask(this);
		} catch (Exception e) {
			// Re-throw so the task executor marks the task record failed.
			log.error("Essay generation task failed", e);
			throw e;
		} finally {
			log.info("Finished essay generation task in {} ms", System.currentTimeMillis() - startTime);
		}
	}
}
