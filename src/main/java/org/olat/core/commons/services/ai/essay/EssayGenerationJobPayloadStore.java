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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

/**
 *
 * In-memory hand-off store for the full {@link EssayGenerationService.GenerationRequest}
 * payload while the corresponding {@link EssayGenerationLongRunnable} is in
 * flight on the task executor.
 * <p>
 * We intentionally keep the {@code pageMarkdown} + settings off the
 * serialised LongRunnable record: task-executor rows persist the runnable
 * via standard Java serialisation and the Markdown payload can easily be
 * hundreds of kilobytes. The MVP runs single-node, so an in-memory handoff
 * is sufficient — a cluster-safe handoff (e.g. via the job row or VFS
 * scratch files) is flagged for the senior dev in the report.
 * <p>
 * Semantics: {@link #store(Long, EssayGenerationService.GenerationRequest)}
 * attaches a payload to the job key; {@link #take(Long)} reads and removes
 * it. A second {@code take} returns {@code null}, which the service turns
 * into a {@code FAILED} job with a clear reason.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayGenerationJobPayloadStore {

	private final ConcurrentMap<Long, EssayGenerationService.GenerationRequest> payloads =
			new ConcurrentHashMap<>();

	public void store(Long jobKey, EssayGenerationService.GenerationRequest request) {
		if (jobKey == null || request == null) return;
		payloads.put(jobKey, request);
	}

	public EssayGenerationService.GenerationRequest take(Long jobKey) {
		if (jobKey == null) return null;
		return payloads.remove(jobKey);
	}

	public boolean has(Long jobKey) {
		return jobKey != null && payloads.containsKey(jobKey);
	}

	/** For test teardown. */
	void clear() {
		payloads.clear();
	}
}
