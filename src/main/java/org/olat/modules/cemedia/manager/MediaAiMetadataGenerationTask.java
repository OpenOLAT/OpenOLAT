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
package org.olat.modules.cemedia.manager;

import java.io.Serial;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.logging.Tracing;

/**
 * {@link LongRunnable} that generates AI image metadata (title, description,
 * alt text, tags, taxonomy) for a single media. Persisted by the generic task
 * executor in {@code o_ex_task}: one task per image, so a Word import with
 * many images spreads across the {@code aiBatch} pool and a crash loses at
 * most the in-flight image. The task is self-contained and carries only
 * serialisable keys; the media file is re-resolved from the media storage at
 * run time. Failures are reflected on the executor's task row.
 *
 * Initial date: 2026-07-06<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class MediaAiMetadataGenerationTask implements LongRunnable {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final Logger log = Tracing.createLoggerFor(MediaAiMetadataGenerationTask.class);

	private final Long mediaKey;
	private final Long requesterKey;
	private final String languageTag;
	private final String usageContextType;
	private final String resourceType;
	private final Long resourceId;
	private final String resourceSubId;
	private final boolean overwrite;

	public MediaAiMetadataGenerationTask(Long mediaKey, Long requesterKey, String languageTag,
			String usageContextType, String resourceType, Long resourceId, String resourceSubId,
			boolean overwrite) {
		this.mediaKey = mediaKey;
		this.requesterKey = requesterKey;
		this.languageTag = languageTag;
		this.usageContextType = usageContextType;
		this.resourceType = resourceType;
		this.resourceId = resourceId;
		this.resourceSubId = resourceSubId;
		this.overwrite = overwrite;
	}

	public Long getMediaKey() {
		return mediaKey;
	}

	public Long getRequesterKey() {
		return requesterKey;
	}

	public String getLanguageTag() {
		return languageTag;
	}

	public String getUsageContextType() {
		return usageContextType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public String getResourceSubId() {
		return resourceSubId;
	}

	/**
	 * @return true when the AI metadata replaces existing values (media
	 *         created by an import with machine-generated metadata), false
	 *         when only empty fields are filled (media saved from a form
	 *         where the user may have entered values)
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	@Override
	public Queue getExecutorsQueue() {
		// Nobody is actively waiting for the result — keep the interactive
		// pool free for learner-facing calls.
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
			MediaAiMetadataService service = CoreSpringFactory.getImpl(MediaAiMetadataService.class);
			service.runTask(this);
		} catch (Exception e) {
			// Re-throw so the task executor marks the task record failed.
			log.error("Media AI metadata generation task failed for media {}", mediaKey, e);
			throw e;
		} finally {
			log.info("Finished media AI metadata generation task for media {} in {} ms",
					mediaKey, System.currentTimeMillis() - startTime);
		}
	}
}
