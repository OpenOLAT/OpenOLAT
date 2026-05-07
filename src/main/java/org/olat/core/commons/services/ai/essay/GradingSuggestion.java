/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

import dev.langchain4j.model.output.structured.Description;

/**
 * Structured grading output returned by {@link AiEssayGradingSPI#gradeEssayAnswer}.
 * <p>
 * Numeric score computation (contentScore, languageScore) happens server-side
 * in {@link EssayFormativeFeedbackService} using the persisted rubric weights;
 * this record carries raw signals only.
 * <p>
 * The {@link Description} annotations are used by LangChain4j's structured-output
 * extraction to derive a JSON schema for the LLM response.
 *
 * Initial date: 2026-04-18<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GradingSuggestion(
		@JsonProperty(required = true)
		@Description("Content signals: which key points the student hit / missed, plus structural notes")
		ContentSignals contentSignals,
		@JsonProperty(required = true)
		@Description("Language signals: a small number of notable grammar and spelling issues")
		LanguageSignals languageSignals,
		@JsonProperty(required = true)
		@Description("NONE unless the answer is clearly off-topic, gibberish, or in the wrong language")
		OffTopicFlag offTopicFlag,
		@JsonProperty(required = true)
		@Description("Grader's confidence in its own assessment")
		Confidence confidence,
		@JsonProperty(required = true)
		@Description("Short plain-language feedback to the student (3 fields, each 1–2 sentences, max ~500 chars each)")
		StudentFeedback feedbackToStudent,
		@JsonProperty(required = true)
		@Description("Short note to the coach / author (max ~500 chars); empty string when not useful")
		String feedbackToCoach,
		@JsonProperty(required = true)
		@Description("Holistic 1–2 sentence overall assessment of the answer in the student's language (max ~400 chars)")
		String overallAssessment,
		@Description("Model's self-estimated holistic score on a 0–100 scale; advisory only, server-side weighting is authoritative")
		int estimatedScorePercent,
		@JsonSetter(nulls = Nulls.AS_EMPTY)
		@Description("Inline annotation of the student's answer: one entry per paragraph, each paragraph reproduced verbatim as tagged spans (CORRECT / AMBIGUOUS / WRONG / NEUTRAL) plus a one-to-two sentence paragraph-level feedback note. Empty list when not provided.")
		List<AnnotatedParagraph> annotatedParagraphs) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record ContentSignals(
			@JsonProperty(required = true)
			@Description("Key points the answer clearly demonstrates, with a short evidence snippet each; empty list if none")
			List<KeyPointHit> keyPointsHit,
			@JsonProperty(required = true)
			@Description("Key points the answer fails to demonstrate, with a short reason each; empty list if none")
			List<KeyPointMissed> keyPointsMissed,
			@Description("One-sentence note on overall coherence / structure (max ~250 chars)")
			String coherenceNote,
			@Description("One-sentence note on argument quality (max ~250 chars)")
			String argumentNote,
			@Description("One-sentence note on how well the answer addresses the stimulus / stays on topic (max ~250 chars)")
			String relevanceNote) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KeyPointHit(
			@Description("Key point id as declared in the item definition (e.g. kp1)")
			String id,
			@Description("Short evidence snippet from the student answer supporting this hit (max ~200 chars)")
			String evidence) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KeyPointMissed(
			@Description("Key point id as declared in the item definition (e.g. kp1)")
			String id,
			@Description("Short reason why this key point is considered missing (max ~200 chars)")
			String reason) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record LanguageSignals(
			@JsonProperty(required = true)
			@Description("A few notable grammar issues; empty list when no issues to report")
			List<GrammarIssue> grammarIssues,
			@JsonProperty(required = true)
			@Description("A few notable spelling issues; empty list when no issues to report")
			List<SpellingIssue> spellingIssues) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record GrammarIssue(
			@Description("Short span from the student answer that contains the issue (max ~120 chars)")
			String span,
			@Description("Brief human-readable note on what is wrong (max ~200 chars)")
			String note,
			@Description("MINOR for cosmetic, MAJOR for comprehension-impacting")
			Severity severity) { }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record SpellingIssue(
			@Description("Misspelled word as it appears in the student answer")
			String word,
			@Description("Suggested correct spelling")
			String suggestion) { }

	public enum Severity { MINOR, MAJOR }

	public enum OffTopicFlag { NONE, LIKELY_OFF_TOPIC, GIBBERISH, LANGUAGE_MISMATCH }

	public enum Confidence { LOW, MEDIUM, HIGH }

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record StudentFeedback(
			@JsonProperty(required = true)
			@Description("One to two short sentences on what went well (max ~500 chars)")
			String whatWentWell,
			@JsonProperty(required = true)
			@Description("One to two short sentences on what is missing or should be revised (max ~500 chars)")
			String whatIsMissing,
			@JsonProperty(required = true)
			@Description("One short actionable next step for the student to improve (max ~500 chars)")
			String nextStep) { }
}
