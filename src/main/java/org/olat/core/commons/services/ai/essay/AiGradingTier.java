/**
 * <a href="http://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.essay;

/**
 * Length-based tier for essay grading requests. {@link #SHORT} and
 * {@link #MEDIUM} answers are graded live; {@link #LONG} is an internal
 * sentinel meaning "over the admin-configured maximum word count" and is
 * always refused by {@link LengthPreFilter} before any grading call is made
 * — there is no separate Long-tier grading path.
 *
 * Initial date: 2026-04-18<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 */
public enum AiGradingTier {

	SHORT  ( 150, 16_384,  8_000L),
	MEDIUM ( 400, 16_384, 15_000L),
	LONG   (Integer.MAX_VALUE, 16_384, 0L);

	private final int wordThreshold;
	private final int maxTokens;
	private final long softTimeoutMs;

	AiGradingTier(int wordThreshold, int maxTokens, long softTimeoutMs) {
		this.wordThreshold = wordThreshold;
		this.maxTokens = maxTokens;
		this.softTimeoutMs = softTimeoutMs;
	}

	/**
	 * Inclusive upper bound (in words) of this tier.
	 * {@link #LONG} is unbounded (Integer.MAX_VALUE).
	 */
	public int wordThreshold() {
		return wordThreshold;
	}

	/**
	 * vLLM {@code max_tokens} cap for the grader output in this tier.
	 */
	public int maxTokens() {
		return maxTokens;
	}

	/**
	 * Client-side soft timeout in milliseconds before the UI would switch to
	 * a background-fallback microcopy. Not currently read by any caller —
	 * {@link #LONG} answers are refused before a grading call is ever made.
	 */
	public long softTimeoutMs() {
		return softTimeoutMs;
	}

	/**
	 * Classify a student answer by word count with CJK-aware fallback.
	 * For CJK-heavy text (> 30 % of chars in Han/Kana/Hangul ranges)
	 * the character count is divided by 3 to approximate words.
	 */
	public static AiGradingTier classify(String studentAnswer) {
		return classify(studentAnswer, MEDIUM.wordThreshold());
	}

	/**
	 * @param mediumWordThreshold the admin-configured maximum word count. An
	 *                            answer at or below it classifies as
	 *                            {@link #SHORT} or {@link #MEDIUM} and is
	 *                            graded live; above it, it classifies as
	 *                            {@link #LONG} and is refused. Defaults to
	 *                            {@link #MEDIUM}'s own 400-word threshold.
	 */
	public static AiGradingTier classify(String studentAnswer, int mediumWordThreshold) {
		if (studentAnswer == null || studentAnswer.isBlank()) {
			return SHORT;
		}
		int words = countWords(studentAnswer);
		if (words <= SHORT.wordThreshold) return SHORT;
		if (words <= mediumWordThreshold) return MEDIUM;
		return LONG;
	}

	private static int countWords(String text) {
		int cjk = 0;
		int total = 0;
		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			if (!Character.isWhitespace(cp)) {
				total++;
				if (isCJK(cp)) cjk++;
			}
			i += Character.charCount(cp);
		}
		boolean cjkHeavy = total > 0 && ((double) cjk / total) > 0.30;
		if (cjkHeavy) {
			return Math.max(1, total / 3);
		}
		return text.trim().split("\\s+").length;
	}

	private static boolean isCJK(int cp) {
		// Han, Hiragana, Katakana, Hangul Syllables, CJK Unified Ideographs
		return (cp >= 0x3040 && cp <= 0x30FF)
				|| (cp >= 0x4E00 && cp <= 0x9FFF)
				|| (cp >= 0xAC00 && cp <= 0xD7AF);
	}
}
