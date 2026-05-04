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

import java.util.List;

/**
 *
 * Return shape of {@link EssayFormativeFeedbackService#grade}.
 * <p>
 * Three terminal states:
 * <ul>
 *   <li>{@link Type#OK} — grading ran, {@code suggestion} carries the
 *       parsed + sanitised grader output and {@code usageLogKey} points
 *       at the persisted {@code AiUsageLog} row.</li>
 *   <li>{@link Type#REJECTED} — a pre-filter rejected the answer before
 *       the LLM call; {@code rejection} carries the i18n key.</li>
 *   <li>{@link Type#REFUSED_LONG} — answer classified as Long tier,
 *       refused in MVP; {@code rejection} carries {@code ai.essay.error.too.long}.</li>
 * </ul>
 * {@code warnings} hold non-fatal pre-filter warnings (currently only
 * {@link LanguagePreFilter} emits these).
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public record FormativeFeedback(
		Type type,
		AiGradingTier tier,
		GradingSuggestion suggestion,
		RejectionReason rejection,
		List<RejectionReason> warnings,
		Long usageLogKey) {

	public enum Type { OK, REJECTED, REFUSED_LONG }

	public static FormativeFeedback ok(AiGradingTier tier, GradingSuggestion suggestion,
			List<RejectionReason> warnings, Long usageLogKey) {
		return new FormativeFeedback(Type.OK, tier, suggestion, null,
				warnings == null ? List.of() : List.copyOf(warnings), usageLogKey);
	}

	public static FormativeFeedback rejected(RejectionReason reason) {
		return new FormativeFeedback(Type.REJECTED, null, null, reason, List.of(), null);
	}

	public static FormativeFeedback refusedLong(RejectionReason reason) {
		return new FormativeFeedback(Type.REFUSED_LONG, AiGradingTier.LONG, null, reason, List.of(), null);
	}
}
