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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Unit tests for {@link LengthPreFilter} — null/blank short-circuits,
 * empty rejection, too-long rejection, CJK edge cases.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class LengthPreFilterTest {

	// ---------------------------------------------------------------- null/blank

	@Test
	public void check_nullReturnsEmptyRejection() {
		Optional<RejectionReason> result = LengthPreFilter.check(null);
		// null is still "empty" so we get a REASON_EMPTY rejection
		assertTrue(result.isPresent());
		assertEquals(LengthPreFilter.REASON_EMPTY, result.get().messageKey());
	}

	@Test
	public void check_blankReturnsEmptyRejection() {
		Optional<RejectionReason> result = LengthPreFilter.check("   ");
		assertTrue(result.isPresent());
		assertEquals(LengthPreFilter.REASON_EMPTY, result.get().messageKey());
	}

	// ---------------------------------------------------------------- short (well within budget)

	@Test
	public void check_shortAnswerPasses() {
		String shortAnswer = words(10);
		Optional<RejectionReason> result = LengthPreFilter.check(shortAnswer);
		assertFalse("Short answer must pass length filter", result.isPresent());
	}

	// ---------------------------------------------------------------- exactly at MEDIUM threshold

	@Test
	public void check_exactlyAtMediumThresholdPasses() {
		// MEDIUM threshold is 400 words; exactly 400 should be MEDIUM and pass
		String answer = words(AiGradingTier.MEDIUM.wordThreshold());
		Optional<RejectionReason> result = LengthPreFilter.check(answer);
		assertFalse("Answer at MEDIUM threshold must not be rejected", result.isPresent());
	}

	// ---------------------------------------------------------------- over MEDIUM → LONG → reject

	@Test
	public void check_overMediumThresholdRejectsTooLong() {
		String answer = words(AiGradingTier.MEDIUM.wordThreshold() + 1);
		Optional<RejectionReason> result = LengthPreFilter.check(answer);
		assertTrue("Answer over MEDIUM threshold must be rejected", result.isPresent());
		assertEquals(LengthPreFilter.REASON_TOO_LONG, result.get().messageKey());
	}

	// ---------------------------------------------------------------- CJK text — 3 chars ≈ 1 word

	@Test
	public void check_cjkTextWithinBudgetPasses() {
		// 150 CJK chars ≈ 50 words — well within SHORT tier
		String cjk = "这".repeat(150);
		Optional<RejectionReason> result = LengthPreFilter.check(cjk);
		assertFalse("Short CJK text must pass", result.isPresent());
	}

	// ---------------------------------------------------------------- helpers

	private String words(int count) {
		return IntStream.range(0, count)
				.mapToObj(i -> "word" + i)
				.collect(Collectors.joining(" "));
	}
}
