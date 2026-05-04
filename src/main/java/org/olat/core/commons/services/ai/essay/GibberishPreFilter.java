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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * Cheap gibberish pre-filter. Rejects answers whose 3-character n-gram
 * entropy falls below the {@link #ENTROPY_THRESHOLD} (in bits) and
 * whose letter-to-non-letter ratio is pathologically low.
 * <p>
 * This is deliberately conservative — the grader downstream will still
 * flag likely-off-topic / gibberish via its {@code OffTopicFlag} signal.
 * The filter's purpose is to save an LLM call when the input is
 * obviously degenerate (empty whitespace-only, keyboard-mash, copy-paste
 * of binary, etc.).
 * <p>
 * MVP note (per Phase B brief): a simple trigram entropy calculation is
 * acceptable. Tune the threshold after the first real corpus.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public final class GibberishPreFilter {

	/** i18n key surfaced when the answer looks like gibberish. */
	public static final String REASON_GIBBERISH = "ai.essay.error.gibberish";

	/**
	 * Trigram entropy threshold in bits. Empirical: real prose in Latin
	 * scripts usually sits above 3 bits; random keyboard mash tends to
	 * pile up on the same few keys and drops well below 1.5.
	 */
	public static final double ENTROPY_THRESHOLD = 1.5d;

	/** Minimum fraction of letter characters required (blocks pure symbol spam). */
	public static final double MIN_LETTER_RATIO = 0.45d;

	/** Answers shorter than this many non-whitespace chars skip the entropy test. */
	private static final int MIN_LENGTH_FOR_ENTROPY_CHECK = 15;

	private GibberishPreFilter() { /* utility */ }

	public static Optional<RejectionReason> check(String studentAnswer) {
		if (studentAnswer == null || studentAnswer.isBlank()) {
			return Optional.empty();
		}
		String normalised = studentAnswer.replaceAll("\\s+", "");
		if (normalised.length() < MIN_LENGTH_FOR_ENTROPY_CHECK) {
			return Optional.empty();
		}

		double letterRatio = letterRatio(normalised);
		if (letterRatio < MIN_LETTER_RATIO) {
			return Optional.of(new RejectionReason(REASON_GIBBERISH,
					"letter ratio " + String.format(java.util.Locale.ROOT, "%.2f", letterRatio)
							+ " below threshold " + MIN_LETTER_RATIO));
		}

		double entropy = trigramEntropy(normalised.toLowerCase(java.util.Locale.ROOT));
		if (entropy < ENTROPY_THRESHOLD) {
			return Optional.of(new RejectionReason(REASON_GIBBERISH,
					"trigram entropy " + String.format(java.util.Locale.ROOT, "%.2f", entropy)
							+ " below threshold " + ENTROPY_THRESHOLD));
		}
		return Optional.empty();
	}

	private static double letterRatio(String text) {
		if (text.isEmpty()) return 0.0d;
		int letters = 0;
		for (int i = 0; i < text.length(); ) {
			int cp = text.codePointAt(i);
			if (Character.isLetter(cp)) letters++;
			i += Character.charCount(cp);
		}
		return (double) letters / text.length();
	}

	/**
	 * Shannon entropy over character trigram frequencies. Returns 0 for
	 * trivially short input. Values above roughly 2 indicate natural
	 * language; values below 1.5 are almost always keyboard-mash.
	 */
	private static double trigramEntropy(String text) {
		if (text.length() < 3) return 0.0d;
		Map<String, Integer> counts = new HashMap<>();
		int total = 0;
		for (int i = 0; i + 3 <= text.length(); i++) {
			String trigram = text.substring(i, i + 3);
			counts.merge(trigram, 1, Integer::sum);
			total++;
		}
		if (total == 0) return 0.0d;
		double entropy = 0.0d;
		for (int count : counts.values()) {
			double p = (double) count / total;
			entropy -= p * (Math.log(p) / Math.log(2));
		}
		return entropy;
	}
}
