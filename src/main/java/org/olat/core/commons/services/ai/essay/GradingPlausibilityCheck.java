/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.services.ai.essay.GradingSuggestion.Confidence;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.ContentSignals;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.KeyPointHit;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.KeyPointMissed;
import org.olat.core.commons.services.ai.essay.GradingSuggestion.OffTopicFlag;
import org.olat.core.util.StringHelper;

/**
 * Post-call plausibility check on a {@link GradingSuggestion}. The LLM
 * result must agree with the grading kit and with the student answer; where
 * it does not, the check adjusts the result server-side instead of trusting
 * the model. Pure function, no I/O.
 * <p>
 * Rules:
 * <ol>
 *   <li>Key-point ids that are not in the grading kit are dropped from the
 *       hit and the missed list.</li>
 *   <li>A hit whose evidence snippet is not found in the student answer is
 *       dropped. A hit without evidence is kept but counts as unevidenced.</li>
 *   <li>Off-topic flag other than NONE: score capped at
 *       {@link #OFF_TOPIC_SCORE_CAP}, confidence LOW.</li>
 *   <li>Kit has key points, no hit survived, score above
 *       {@link #NO_HIT_SCORE_CAP}: score capped, confidence LOW.</li>
 *   <li>Unevidenced hits: confidence capped at MEDIUM; all hits unevidenced:
 *       confidence LOW.</li>
 *   <li>Language-mismatch warning from the pre-filter with a score of
 *       {@link #LANGUAGE_MISMATCH_SCORE_THRESHOLD} or more: confidence
 *       capped at MEDIUM.</li>
 * </ol>
 *
 * Initial date: 2026-10-20<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 */
public final class GradingPlausibilityCheck {

	/** Highest score an answer flagged off-topic, gibberish or wrong-language can keep. */
	public static final int OFF_TOPIC_SCORE_CAP = 30;
	/** Highest score an answer without a single surviving key-point hit can keep. */
	public static final int NO_HIT_SCORE_CAP = 50;
	/** Score from which a language mismatch lowers the confidence. */
	public static final int LANGUAGE_MISMATCH_SCORE_THRESHOLD = 70;

	/** Minimum share of significant evidence tokens that must appear in the answer. */
	private static final int TOKEN_OVERLAP_PERCENT = 70;
	/** Tokens shorter than this do not count for the overlap fallback. */
	private static final int MIN_SIGNIFICANT_TOKEN_LENGTH = 4;
	/** Evidence snippets with fewer tokens than this get no overlap fallback. */
	private static final int MIN_TOKENS_FOR_OVERLAP = 3;

	private GradingPlausibilityCheck() { /* utility */ }

	/**
	 * Result of a check: the (possibly adjusted) suggestion and one line per
	 * adjustment for the debug log. {@code adjustments} is empty when the
	 * suggestion passed unchanged.
	 */
	public record Result(GradingSuggestion suggestion, List<String> adjustments) {
		public boolean adjusted() {
			return !adjustments.isEmpty();
		}
	}

	/**
	 * @param in               the sanitised suggestion from the grader
	 * @param kitKeyPoints     the key points of the grading kit; empty list
	 *                         disables the id rules
	 * @param studentAnswer    the answer as sent to the grader
	 * @param languageMismatch true when the language pre-filter warned
	 */
	public static Result apply(GradingSuggestion in, List<EssayItemDraft.KeyPoint> kitKeyPoints,
			String studentAnswer, boolean languageMismatch) {
		if (in == null) {
			return new Result(null, List.of());
		}
		List<String> adjustments = new ArrayList<>();
		Set<String> kitIds = new HashSet<>();
		if (kitKeyPoints != null) {
			for (EssayItemDraft.KeyPoint kp : kitKeyPoints) {
				if (kp != null && StringHelper.containsNonWhitespace(kp.id())) {
					kitIds.add(kp.id().trim());
				}
			}
		}

		// Rules 1 and 2: key-point lists.
		ContentSignals signals = in.contentSignals();
		List<KeyPointHit> hits = new ArrayList<>();
		List<KeyPointMissed> missed = new ArrayList<>();
		int unevidenced = 0;
		if (signals != null) {
			if (signals.keyPointsHit() != null) {
				for (KeyPointHit hit : signals.keyPointsHit()) {
					if (hit == null) {
						continue;
					}
					String id = hit.id() == null ? "" : hit.id().trim();
					if (!kitIds.isEmpty() && !kitIds.contains(id)) {
						adjustments.add("dropped hit with unknown key point id '" + id + "'");
						continue;
					}
					if (!StringHelper.containsNonWhitespace(hit.evidence())) {
						unevidenced++;
						hits.add(hit);
						continue;
					}
					if (!evidenceFoundIn(studentAnswer, hit.evidence())) {
						adjustments.add("dropped hit '" + id + "': evidence not found in the student answer");
						continue;
					}
					hits.add(hit);
				}
			}
			if (signals.keyPointsMissed() != null) {
				for (KeyPointMissed miss : signals.keyPointsMissed()) {
					if (miss == null) {
						continue;
					}
					String id = miss.id() == null ? "" : miss.id().trim();
					if (!kitIds.isEmpty() && !kitIds.contains(id)) {
						adjustments.add("dropped missed entry with unknown key point id '" + id + "'");
						continue;
					}
					missed.add(miss);
				}
			}
		}

		int score = Math.max(0, Math.min(100, in.estimatedScorePercent()));
		if (score != in.estimatedScorePercent()) {
			adjustments.add("score " + in.estimatedScorePercent() + " clamped to " + score);
		}
		Confidence confidence = in.confidence();
		if (confidence == null) {
			confidence = Confidence.LOW;
			adjustments.add("missing confidence set to LOW");
		}

		// Rule 3: off-topic flag.
		OffTopicFlag flag = in.offTopicFlag();
		if (flag != null && flag != OffTopicFlag.NONE) {
			if (score > OFF_TOPIC_SCORE_CAP) {
				adjustments.add("offTopicFlag " + flag + ": score " + score + " capped to " + OFF_TOPIC_SCORE_CAP);
				score = OFF_TOPIC_SCORE_CAP;
			}
			if (confidence != Confidence.LOW) {
				adjustments.add("offTopicFlag " + flag + ": confidence " + confidence + " lowered to LOW");
				confidence = Confidence.LOW;
			}
		}

		// Rule 4: no surviving hit against a kit with key points.
		if (!kitIds.isEmpty() && hits.isEmpty() && score > NO_HIT_SCORE_CAP) {
			adjustments.add("no key point hit: score " + score + " capped to " + NO_HIT_SCORE_CAP);
			score = NO_HIT_SCORE_CAP;
			if (confidence != Confidence.LOW) {
				adjustments.add("no key point hit: confidence " + confidence + " lowered to LOW");
				confidence = Confidence.LOW;
			}
		}

		// Rule 5: hits without evidence.
		if (unevidenced > 0) {
			Confidence cap = unevidenced == hits.size() ? Confidence.LOW : Confidence.MEDIUM;
			if (confidence.ordinal() > cap.ordinal()) {
				adjustments.add(unevidenced + " of " + hits.size() + " hits without evidence: confidence "
						+ confidence + " lowered to " + cap);
				confidence = cap;
			}
		}

		// Rule 6: language mismatch warning with a high score.
		if (languageMismatch && score >= LANGUAGE_MISMATCH_SCORE_THRESHOLD
				&& confidence.ordinal() > Confidence.MEDIUM.ordinal()) {
			adjustments.add("language mismatch with score " + score + ": confidence " + confidence
					+ " lowered to MEDIUM");
			confidence = Confidence.MEDIUM;
		}

		if (adjustments.isEmpty()) {
			return new Result(in, List.of());
		}
		ContentSignals adjustedSignals = signals == null
				? new ContentSignals(hits, missed, null, null, null)
				: new ContentSignals(hits, missed, signals.coherenceNote(), signals.argumentNote(),
						signals.relevanceNote());
		GradingSuggestion adjusted = new GradingSuggestion(adjustedSignals, in.languageSignals(),
				in.offTopicFlag(), confidence, in.feedbackToStudent(), in.feedbackToCoach(),
				in.overallAssessment(), score, in.annotatedParagraphs());
		return new Result(adjusted, List.copyOf(adjustments));
	}

	/**
	 * True when the evidence snippet occurs in the answer after
	 * normalisation, or when at least {@link #TOKEN_OVERLAP_PERCENT} of its
	 * significant tokens do. The fallback tolerates a model that trims or
	 * lightly rewords the quote.
	 */
	static boolean evidenceFoundIn(String studentAnswer, String evidence) {
		String answer = normalise(studentAnswer);
		String snippet = normalise(evidence);
		if (snippet.isEmpty()) {
			return true;
		}
		if (answer.isEmpty()) {
			return false;
		}
		if (answer.contains(snippet)) {
			return true;
		}
		String[] tokens = snippet.split(" ");
		if (tokens.length < MIN_TOKENS_FOR_OVERLAP) {
			return false;
		}
		int significant = 0;
		int found = 0;
		for (String token : tokens) {
			if (token.length() < MIN_SIGNIFICANT_TOKEN_LENGTH) {
				continue;
			}
			significant++;
			if (answer.contains(token)) {
				found++;
			}
		}
		return significant > 0 && found * 100 / significant >= TOKEN_OVERLAP_PERCENT;
	}

	/** Lower-case, letters and digits only, single spaces. */
	static String normalise(String text) {
		if (text == null) {
			return "";
		}
		return text.toLowerCase(Locale.ROOT)
				.replaceAll("[^\\p{L}\\p{N}]+", " ")
				.trim();
	}
}
