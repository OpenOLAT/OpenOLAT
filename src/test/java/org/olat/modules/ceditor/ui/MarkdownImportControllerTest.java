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
package org.olat.modules.ceditor.ui;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the static count helpers of the Markdown import dialog. The AI
 * question generation section must request at least one question in total
 * when the toggle is on (same rule as the question pool import dialog).
 *
 * Initial date: 5 Jul 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownImportControllerTest {

	@Test
	public void totalAiQuestionCount_sumsBothLegs() {
		Assert.assertEquals(3, MarkdownImportController.totalAiQuestionCount("2", "1"));
	}

	@Test
	public void totalAiQuestionCount_zeroZeroIsZero() {
		Assert.assertEquals(0, MarkdownImportController.totalAiQuestionCount("0", "0"));
	}

	@Test
	public void totalAiQuestionCount_blankAndNullCountAsZero() {
		Assert.assertEquals(0, MarkdownImportController.totalAiQuestionCount(null, ""));
		Assert.assertEquals(2, MarkdownImportController.totalAiQuestionCount(" ", "2"));
	}

	@Test
	public void totalAiQuestionCount_unparsableCountsAsZero() {
		Assert.assertEquals(2, MarkdownImportController.totalAiQuestionCount("x", "2"));
	}
}
