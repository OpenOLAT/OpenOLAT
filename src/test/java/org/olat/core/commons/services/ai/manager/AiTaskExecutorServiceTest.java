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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.ai.manager.AiTaskExecutorService.AiTaskQueueStats;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests for the AI task pool facade: stats snapshot and runtime resizing
 * in both directions (the underlying ThreadPoolExecutor rejects
 * core &gt; max, so the resize order matters).
 *
 * Initial date: 2026-06-10<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 */
public class AiTaskExecutorServiceTest extends OlatTestCase {

	@Autowired
	private AiTaskExecutorService aiTaskExecutorService;

	@Test
	public void statsAvailable() {
		AiTaskQueueStats interactive = aiTaskExecutorService.getInteractiveStats();
		AiTaskQueueStats batch = aiTaskExecutorService.getBatchStats();
		Assert.assertNotNull(interactive);
		Assert.assertNotNull(batch);
		Assert.assertTrue(interactive.poolSize() >= 1);
		Assert.assertTrue(batch.poolSize() >= 1);
		Assert.assertTrue(interactive.running() >= 0);
		Assert.assertTrue(interactive.waiting() >= 0);
	}

	@Test
	public void resizeGrowAndShrink() {
		int original = aiTaskExecutorService.getInteractiveStats().poolSize();
		try {
			aiTaskExecutorService.setInteractivePoolSize(original + 3);
			Assert.assertEquals(original + 3, aiTaskExecutorService.getInteractiveStats().poolSize());

			aiTaskExecutorService.setInteractivePoolSize(1);
			Assert.assertEquals(1, aiTaskExecutorService.getInteractiveStats().poolSize());
		} finally {
			aiTaskExecutorService.setInteractivePoolSize(original);
		}
		Assert.assertEquals(original, aiTaskExecutorService.getInteractiveStats().poolSize());
	}

	@Test
	public void resizeIgnoresInvalidSize() {
		int original = aiTaskExecutorService.getBatchStats().poolSize();
		aiTaskExecutorService.setBatchPoolSize(0);
		aiTaskExecutorService.setBatchPoolSize(-5);
		Assert.assertEquals(original, aiTaskExecutorService.getBatchStats().poolSize());
	}
}
