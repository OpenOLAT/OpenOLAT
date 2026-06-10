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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.olat.core.commons.services.ai.content.AiContentHardener;

/**
 *
 * Static sanity checks for {@link EssayItemDraft} instances returned by the
 * generator. Drafts that fail validation are dropped by
 * {@link EssayGenerationService#runTask(EssayGenerationTask)} with the
 * reasons written to the server log so the author sees exactly why a draft
 * was dropped.
 * <p>
 * Limits are intentionally conservative — they catch pathological LLM output
 * without second-guessing the author's final editorial choices.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public final class GeneratedItemValidator {

	public static final int QUESTION_TITLE_MAX = 200;
	public static final int LEARNING_OBJECTIVE_MAX = 500;
	public static final int REFERENCE_EXCERPT_MAX = 2000;
	public static final int MODEL_ANSWER_MAX = 1500;
	public static final double RUBRIC_WEIGHT_TOLERANCE = 0.01d;
	public static final double RUBRIC_WEIGHT_SUM = 1.0d;

	private static final Pattern SCRIPT_TAG = Pattern.compile("(?is)<\\s*script[^>]*>");
	private static final Pattern IFRAME_TAG = Pattern.compile("(?is)<\\s*iframe[^>]*>");
	private static final Pattern JAVASCRIPT_URL = Pattern.compile("(?i)javascript\\s*:");

	private GeneratedItemValidator() { /* utility */ }

	/**
	 * Run all validations against the given draft. Returns an empty list if
	 * the draft passes.
	 */
	public static List<ValidationIssue> validate(EssayItemDraft draft) {
		List<ValidationIssue> issues = new ArrayList<>();
		if (draft == null) {
			issues.add(new ValidationIssue("draft", "Draft is null"));
			return issues;
		}

		checkText("questionTitle", draft.questionTitle(), QUESTION_TITLE_MAX, issues);
		checkText("learningObjective", draft.learningObjective(), LEARNING_OBJECTIVE_MAX, issues);
		checkText("referenceExcerpt", draft.referenceExcerpt(), REFERENCE_EXCERPT_MAX, issues);
		checkText("modelAnswer", draft.modelAnswer(), MODEL_ANSWER_MAX, issues);

		checkKeyPoints(draft.keyPoints(), issues);
		checkRubric(draft.rubricCriteria(), issues);
		checkBloomLevel(draft.bloomLevel(), issues);
		checkInjectionSafe("stimulus", draft.stimulus(), issues);
		checkInjectionSafe("modelAnswer", draft.modelAnswer(), issues);
		checkInjectionSafe("referenceExcerpt", draft.referenceExcerpt(), issues);

		return issues;
	}

	private static void checkText(String fieldName, String value, int maxChars, List<ValidationIssue> issues) {
		if (value == null || value.isBlank()) {
			issues.add(new ValidationIssue(fieldName, fieldName + " is blank"));
			return;
		}
		if (value.length() > maxChars) {
			issues.add(new ValidationIssue(fieldName,
					fieldName + " length " + value.length() + " exceeds max " + maxChars));
		}
	}

	private static void checkKeyPoints(List<EssayItemDraft.KeyPoint> keyPoints, List<ValidationIssue> issues) {
		if (keyPoints == null || keyPoints.isEmpty()) {
			issues.add(new ValidationIssue("keyPoints", "No key points provided"));
			return;
		}
		boolean anyValid = false;
		for (int i = 0; i < keyPoints.size(); i++) {
			EssayItemDraft.KeyPoint kp = keyPoints.get(i);
			if (kp == null || kp.text() == null || kp.text().isBlank()) {
				issues.add(new ValidationIssue("keyPoints[" + i + "].text", "Key point text is blank"));
				continue;
			}
			if (kp.weight() <= 0.0d || kp.weight() > 1.0d) {
				issues.add(new ValidationIssue("keyPoints[" + i + "].weight",
						"Key point weight " + kp.weight() + " is out of (0, 1]"));
				continue;
			}
			anyValid = true;
		}
		if (!anyValid) {
			issues.add(new ValidationIssue("keyPoints", "No valid key point found"));
		}
	}

	private static void checkRubric(List<EssayItemDraft.RubricCriterion> rubric, List<ValidationIssue> issues) {
		if (rubric == null || rubric.isEmpty()) {
			issues.add(new ValidationIssue("rubricCriteria", "No rubric criteria provided"));
			return;
		}
		double sum = 0.0d;
		for (int i = 0; i < rubric.size(); i++) {
			EssayItemDraft.RubricCriterion rc = rubric.get(i);
			if (rc == null) {
				issues.add(new ValidationIssue("rubricCriteria[" + i + "]", "Rubric criterion is null"));
				continue;
			}
			sum += rc.weight();
		}
		if (Math.abs(sum - RUBRIC_WEIGHT_SUM) > RUBRIC_WEIGHT_TOLERANCE) {
			issues.add(new ValidationIssue("rubricCriteria.weightSum",
					"Rubric weights sum to " + sum + ", expected 1.0 ± " + RUBRIC_WEIGHT_TOLERANCE));
		}
	}

	private static void checkBloomLevel(AiBloomLevel level, List<ValidationIssue> issues) {
		if (level == null) {
			issues.add(new ValidationIssue("bloomLevel", "Bloom level missing"));
		}
		// enum values are constrained at parse time — no additional check needed.
	}

	/**
	 * Reject drafts that carry script tags, iframes, {@code javascript:}
	 * URLs or chat-template tokens. Uses the hardener's injection pattern
	 * set plus a handful of always-rejected HTML tags.
	 */
	private static void checkInjectionSafe(String fieldName, String value, List<ValidationIssue> issues) {
		if (value == null || value.isEmpty()) return;
		if (SCRIPT_TAG.matcher(value).find()) {
			issues.add(new ValidationIssue(fieldName, "Contains <script> tag"));
		}
		if (IFRAME_TAG.matcher(value).find()) {
			issues.add(new ValidationIssue(fieldName, "Contains <iframe> tag"));
		}
		if (JAVASCRIPT_URL.matcher(value).find()) {
			issues.add(new ValidationIssue(fieldName, "Contains javascript: URL"));
		}
		for (Pattern pattern : AiContentHardener.INJECTION_PATTERNS) {
			if (pattern.matcher(value).find()) {
				issues.add(new ValidationIssue(fieldName,
						"Contains prompt-injection pattern: " + pattern.pattern()));
				return;
			}
		}
	}

	/**
	 * Single validation failure. Both {@code fieldPath} and {@code reason}
	 * are kept short so they serialise cleanly into the job error JSON.
	 */
	public record ValidationIssue(String fieldPath, String reason) { }

}
