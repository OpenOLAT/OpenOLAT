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
package org.olat.modules.qpool.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the {@code suggestedEssayCount} and {@code suggestedMcCount}
 * static helpers in {@link NewAiQuestionsImportController}.
 *
 * These are pure static methods, so no Spring context is required.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class NewAiQuestionsImportControllerTest {

	// ---------------------------------------------------------------- suggestedEssayCount bucket boundaries

	@Test
	public void suggestedEssayCount_zeroLengthReturns1() {
		assertEquals(1, NewAiQuestionsImportController.suggestedEssayCount(0));
	}

	@Test
	public void suggestedEssayCount_below200Returns1() {
		assertEquals(1, NewAiQuestionsImportController.suggestedEssayCount(199));
	}

	@Test
	public void suggestedEssayCount_at200Returns2() {
		assertEquals(2, NewAiQuestionsImportController.suggestedEssayCount(200));
	}

	@Test
	public void suggestedEssayCount_below500Returns2() {
		assertEquals(2, NewAiQuestionsImportController.suggestedEssayCount(499));
	}

	@Test
	public void suggestedEssayCount_at500Returns3() {
		assertEquals(3, NewAiQuestionsImportController.suggestedEssayCount(500));
	}

	@Test
	public void suggestedEssayCount_below1500Returns3() {
		assertEquals(3, NewAiQuestionsImportController.suggestedEssayCount(1499));
	}

	@Test
	public void suggestedEssayCount_at1500Returns4() {
		assertEquals(4, NewAiQuestionsImportController.suggestedEssayCount(1500));
	}

	@Test
	public void suggestedEssayCount_below4000Returns4() {
		assertEquals(4, NewAiQuestionsImportController.suggestedEssayCount(3999));
	}

	@Test
	public void suggestedEssayCount_at4000Returns5() {
		assertEquals(5, NewAiQuestionsImportController.suggestedEssayCount(4000));
	}

	@Test
	public void suggestedEssayCount_largeInputCapsAt5() {
		assertEquals(5, NewAiQuestionsImportController.suggestedEssayCount(100000));
	}

	// ---------------------------------------------------------------- suggestedMcCount bucket boundaries

	@Test
	public void suggestedMcCount_zeroLengthReturns2() {
		assertEquals(2, NewAiQuestionsImportController.suggestedMcCount(0));
	}

	@Test
	public void suggestedMcCount_below200Returns2() {
		assertEquals(2, NewAiQuestionsImportController.suggestedMcCount(199));
	}

	@Test
	public void suggestedMcCount_at200Returns3() {
		assertEquals(3, NewAiQuestionsImportController.suggestedMcCount(200));
	}

	@Test
	public void suggestedMcCount_below500Returns3() {
		assertEquals(3, NewAiQuestionsImportController.suggestedMcCount(499));
	}

	@Test
	public void suggestedMcCount_at500Returns4() {
		assertEquals(4, NewAiQuestionsImportController.suggestedMcCount(500));
	}

	@Test
	public void suggestedMcCount_below1500Returns4() {
		assertEquals(4, NewAiQuestionsImportController.suggestedMcCount(1499));
	}

	@Test
	public void suggestedMcCount_at1500Returns5() {
		assertEquals(5, NewAiQuestionsImportController.suggestedMcCount(1500));
	}

	@Test
	public void suggestedMcCount_below4000Returns5() {
		assertEquals(5, NewAiQuestionsImportController.suggestedMcCount(3999));
	}

	@Test
	public void suggestedMcCount_at4000Returns6() {
		assertEquals(6, NewAiQuestionsImportController.suggestedMcCount(4000));
	}

	@Test
	public void suggestedMcCount_largeInputCapsAt6() {
		assertEquals(6, NewAiQuestionsImportController.suggestedMcCount(100000));
	}

	// ---------------------------------------------------------------- invariant: MC always > Essay for same length

	@Test
	public void mcCount_alwaysGreaterThanEssayCount() {
		int[] lengths = { 0, 100, 200, 499, 500, 1499, 1500, 3999, 4000, 50000 };
		for (int len : lengths) {
			int essay = NewAiQuestionsImportController.suggestedEssayCount(len);
			int mc    = NewAiQuestionsImportController.suggestedMcCount(len);
			assertTrue("MC count must be > essay count for length=" + len + " (essay=" + essay + ", mc=" + mc + ")",
					mc > essay);
		}
	}
}
