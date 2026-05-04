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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

/**
 * Unit tests for {@link AiGradingTier#classify(String)} — word-count
 * boundaries, null/blank safety, CJK-aware approximation.
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AiGradingTierTest {

	// ---------------------------------------------------------------- null / blank

	@Test
	public void classify_nullReturnsShort() {
		assertEquals(AiGradingTier.SHORT, AiGradingTier.classify(null));
	}

	@Test
	public void classify_blankReturnsShort() {
		assertEquals(AiGradingTier.SHORT, AiGradingTier.classify("   "));
	}

	// ---------------------------------------------------------------- SHORT boundary

	@Test
	public void classify_singleWordIsShort() {
		assertEquals(AiGradingTier.SHORT, AiGradingTier.classify("hello"));
	}

	@Test
	public void classify_atShortThresholdIsShort() {
		String answer = words(AiGradingTier.SHORT.wordThreshold());
		assertEquals(AiGradingTier.SHORT, AiGradingTier.classify(answer));
	}

	// ---------------------------------------------------------------- MEDIUM boundary

	@Test
	public void classify_justAboveShortThresholdIsMedium() {
		String answer = words(AiGradingTier.SHORT.wordThreshold() + 1);
		assertEquals(AiGradingTier.MEDIUM, AiGradingTier.classify(answer));
	}

	@Test
	public void classify_atMediumThresholdIsMedium() {
		String answer = words(AiGradingTier.MEDIUM.wordThreshold());
		assertEquals(AiGradingTier.MEDIUM, AiGradingTier.classify(answer));
	}

	// ---------------------------------------------------------------- LONG boundary

	@Test
	public void classify_justAboveMediumThresholdIsLong() {
		String answer = words(AiGradingTier.MEDIUM.wordThreshold() + 1);
		assertEquals(AiGradingTier.LONG, AiGradingTier.classify(answer));
	}

	// ---------------------------------------------------------------- CJK-aware approximation

	@Test
	public void classify_heavilyCjkTextUsesCharCount() {
		// 150 CJK chars ≈ 50 words via the /3 approximation → SHORT
		String cjk = "好".repeat(150);
		assertEquals(AiGradingTier.SHORT, AiGradingTier.classify(cjk));
	}

	@Test
	public void classify_mixedCjkBelowThreshold() {
		// 90% CJK — 300 chars ≈ 100 words → SHORT (threshold 150)
		String cjk300 = "字".repeat(300);
		assertEquals(AiGradingTier.SHORT, AiGradingTier.classify(cjk300));
	}

	// ---------------------------------------------------------------- tier properties

	@Test
	public void shortTier_wordThresholdIs150() {
		assertEquals(150, AiGradingTier.SHORT.wordThreshold());
	}

	@Test
	public void mediumTier_wordThresholdIs400() {
		assertEquals(400, AiGradingTier.MEDIUM.wordThreshold());
	}

	@Test
	public void longTier_softTimeoutIsZero() {
		assertEquals(0L, AiGradingTier.LONG.softTimeoutMs());
	}

	// ---------------------------------------------------------------- helpers

	private String words(int count) {
		return IntStream.range(0, count)
				.mapToObj(i -> "word" + i)
				.collect(Collectors.joining(" "));
	}
}
