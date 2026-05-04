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

import java.util.Optional;

/**
 *
 * Length-based pre-filter for student essay answers. Rejects empty
 * submissions outright, and rejects submissions larger than the MVP hard
 * cap (400 words — the upper bound of {@link AiGradingTier#MEDIUM}). The
 * Long tier is deferred to Phase 2, so anything that classifies as Long
 * is refused at this stage with the stable message key
 * {@code ai.essay.error.too.long}.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public final class LengthPreFilter {

	/** i18n key surfaced when the answer is empty. */
	public static final String REASON_EMPTY = "ai.essay.error.empty";
	/** i18n key surfaced when the answer exceeds the MVP 400-word cap. */
	public static final String REASON_TOO_LONG = "ai.essay.error.too.long";

	private LengthPreFilter() { /* utility */ }

	public static Optional<RejectionReason> check(String studentAnswer) {
		if (studentAnswer == null || studentAnswer.isBlank()) {
			return Optional.of(new RejectionReason(REASON_EMPTY, "student answer is empty"));
		}
		AiGradingTier tier = AiGradingTier.classify(studentAnswer);
		if (tier == AiGradingTier.LONG) {
			return Optional.of(new RejectionReason(REASON_TOO_LONG,
					"answer exceeds MVP hard cap of " + AiGradingTier.MEDIUM.wordThreshold() + " words"));
		}
		return Optional.empty();
	}
}
