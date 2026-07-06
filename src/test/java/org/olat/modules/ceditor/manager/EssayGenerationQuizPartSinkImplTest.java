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
package org.olat.modules.ceditor.manager;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the failed-title composition of the quiz part sink. The persisted
 * title must carry only the failed marker plus the original base title —
 * never a failure reason string, which is operator information and would
 * otherwise be rendered to learners.
 *
 * Initial date: 5 Jul 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class EssayGenerationQuizPartSinkImplTest {

	@Test
	public void failedTitle_replacesGeneratingMarkerAndKeepsBaseTitle() {
		Assert.assertEquals("[AI:failed] My Quiz",
				EssayGenerationQuizPartSinkImpl.failedTitle("[AI:generating] My Quiz"));
	}

	@Test
	public void failedTitle_markerOnlyWhenNoBaseTitle() {
		Assert.assertEquals("[AI:failed]",
				EssayGenerationQuizPartSinkImpl.failedTitle("[AI:generating]"));
		Assert.assertEquals("[AI:failed]", EssayGenerationQuizPartSinkImpl.failedTitle(null));
		Assert.assertEquals("[AI:failed]", EssayGenerationQuizPartSinkImpl.failedTitle("  "));
	}

	@Test
	public void failedTitle_keepsPlainTitle() {
		Assert.assertEquals("[AI:failed] My Quiz",
				EssayGenerationQuizPartSinkImpl.failedTitle("My Quiz"));
	}

	@Test
	public void failedTitle_idempotentOnAlreadyFailedTitle() {
		Assert.assertEquals("[AI:failed] My Quiz",
				EssayGenerationQuizPartSinkImpl.failedTitle("[AI:failed] My Quiz"));
	}
}
