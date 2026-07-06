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
package org.olat.core.commons.services.ai.essay;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the queue-overload decision used by the correction submit fail-fast.
 * A correction only makes sense if it can start within the learner's
 * polling window (~36s); beyond a backlog of twice the pool size the task
 * would burn provider cost on a result nobody sees.
 *
 * Initial date: 5 Jul 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class EssayAiCorrectionServiceStaticsTest {

	@Test
	public void isQueueOverloaded_atTwicePoolSizeStillAccepted() {
		Assert.assertFalse(EssayAiCorrectionService.isQueueOverloaded(8, 4));
	}

	@Test
	public void isQueueOverloaded_beyondTwicePoolSizeRefused() {
		Assert.assertTrue(EssayAiCorrectionService.isQueueOverloaded(9, 4));
		Assert.assertTrue(EssayAiCorrectionService.isQueueOverloaded(5, 2));
	}

	@Test
	public void isQueueOverloaded_emptyQueueAccepted() {
		Assert.assertFalse(EssayAiCorrectionService.isQueueOverloaded(0, 4));
		Assert.assertFalse(EssayAiCorrectionService.isQueueOverloaded(4, 2));
	}

	@Test
	public void isQueueOverloaded_zeroPoolSizeRefusesAnyBacklog() {
		Assert.assertTrue(EssayAiCorrectionService.isQueueOverloaded(1, 0));
		Assert.assertFalse(EssayAiCorrectionService.isQueueOverloaded(0, 0));
	}
}
