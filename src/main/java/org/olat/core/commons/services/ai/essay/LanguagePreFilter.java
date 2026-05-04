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

import java.util.Locale;
import java.util.Optional;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.text.TextService;

/**
 *
 * Language-mismatch pre-filter. Detects the locale of the student answer
 * via OpenOlat's existing {@link TextService} (Nutch-backed language
 * identifier) and compares against the {@link EssayAiGrading#getLanguage()}
 * expected by the item.
 * <p>
 * MVP policy: the filter never rejects — it only emits a
 * {@link WarningReason} when the detected language disagrees. The grader
 * still runs, but the UI can surface a "detected language differs" hint
 * and the LLM will typically pick this up too and emit the
 * {@code LANGUAGE_MISMATCH} signal in its off-topic flag.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public final class LanguagePreFilter {

	/** i18n key surfaced when the detected language differs from the expected one. */
	public static final String WARNING_LANGUAGE_MISMATCH = "ai.essay.warn.language.mismatch";

	private LanguagePreFilter() { /* utility */ }

	/**
	 * Run the language check. Returns empty on pass; returns a
	 * {@link RejectionReason} that the caller treats as a non-fatal
	 * warning ({@link EssayFormativeFeedbackService} attaches it as a
	 * {@code WARNING} entry and still calls the grader).
	 */
	public static Optional<RejectionReason> check(String studentAnswer, String expectedLanguage) {
		if (studentAnswer == null || studentAnswer.isBlank() || expectedLanguage == null
				|| expectedLanguage.isBlank()) {
			return Optional.empty();
		}
		try {
			TextService textService = CoreSpringFactory.getImpl(TextService.class);
			if (textService == null) {
				return Optional.empty();
			}
			Locale detected = textService.detectLocale(studentAnswer);
			if (detected == null) return Optional.empty();
			String detectedLang = detected.getLanguage();
			if (detectedLang == null || detectedLang.isBlank()) return Optional.empty();
			if (!detectedLang.equalsIgnoreCase(expectedLanguage)) {
				return Optional.of(new RejectionReason(WARNING_LANGUAGE_MISMATCH,
						"expected language '" + expectedLanguage + "', detected '" + detectedLang + "'"));
			}
			return Optional.empty();
		} catch (Exception e) {
			// Detection is best-effort — never fail grading because of it.
			return Optional.empty();
		}
	}
}
