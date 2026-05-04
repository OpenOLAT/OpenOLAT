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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import dev.langchain4j.model.output.structured.Description;

/**
 * Immutable draft of a generated essay item with its AI-grading metadata.
 * Produced by {@link AiEssayQuestionGeneratorSPI}, reviewed by the author,
 * and on accept persisted as a QTI essay item plus an {@link EssayAiGrading}
 * row, plus a {@code ai-grading.json} companion and an {@code <ooExt:aiGrading>}
 * marker in the item XML.
 * <p>
 * The {@link Description} annotations are used by LangChain4j's structured-output
 * extraction to derive a JSON schema for the LLM response.
 *
 * Initial date: 2026-04-18<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EssayItemDraft(
		@Description("Self-contained essay question stimulus / task statement for the student")
		String stimulus,
		@JsonProperty(required = true)
		@Description("Reference model answer that fully satisfies the question; used for grading")
		String modelAnswer,
		@JsonProperty(required = true)
		@Description("Required and optional key points the student answer should cover; at least one entry")
		List<KeyPoint> keyPoints,
		@JsonProperty(required = true)
		@Description("Rubric criteria scoring dimensions (content + language) with weights summing to 1.0; at least one entry")
		List<RubricCriterion> rubricCriteria,
		@JsonProperty(required = true)
		@Description("Bloom taxonomy level of the question")
		AiBloomLevel bloomLevel,
		@JsonProperty(required = true)
		@Description("Short label (max 120 characters) summarising WHAT this specific question asks — not the answer, "
				+ "not the learning objective. Suitable as a list/menu title, distinct per question. "
				+ "Example: \"Three trends in European higher education\" — NOT \"Understand the key concepts...\".")
		String questionTitle,
		@JsonProperty(required = true)
		@Description("Concise learning objective this question tests")
		String learningObjective,
		@Description("BCP-47 language tag of the question (e.g. \"en\", \"de\", \"de-CH\", \"fr\")")
		String languageTag,
		@JsonProperty(required = true)
		@Description("Short verbatim excerpt from the source material the question is derived from")
		String referenceExcerpt,
		@Description("Approximate output token budget for a good student answer (10–500)")
		int tokenEstimate,
		@Description("Short authoring hint for the downstream grader: what to watch for, common pitfalls; empty string if none")
		String gradingHints,
		@Description("Common misconceptions or typical wrong-answer patterns the grader should be aware of; empty list if none")
		List<String> commonMisconceptions,
		@Description("Estimated difficulty on a 1 (easy) to 5 (hard) scale, from the perspective of the target audience")
		int difficulty) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record KeyPoint(
			@Description("Stable short id, e.g. kp1, kp2 — referenced by the grader response")
			String id,
			@JsonProperty(required = true)
			@Description("Human-readable description of the key point")
			String text,
			@JsonProperty(required = true)
			@Description("Weight contribution to the content score; all required weights should sum to roughly 1.0")
			double weight,
			@Description("True if omitting this key point must reduce the score significantly; "
					+ "defaults to true when omitted, i.e. required for a complete answer.")
			Boolean required) {

		/**
		 * Effective required flag with the null→true default applied. Use this
		 * instead of {@link #required()} whenever the value is read for display
		 * or grading logic — LLMs frequently omit the field and our contract
		 * treats a missing flag as "required for a complete answer".
		 */
		@JsonIgnore
		public boolean isRequiredEffective() {
			return required == null || required.booleanValue();
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record RubricCriterion(
			@Description("Stable short id, e.g. c1, c2")
			String id,
			@JsonProperty(required = true)
			@Description("Human-readable name, e.g. Content, Language")
			String name,
			@Description("Longer descriptor of what this criterion measures")
			String descriptor,
			@JsonProperty(required = true)
			@Description("Weight in the final score; all criteria weights should sum to 1.0")
			double weight,
			@JsonProperty(required = true)
			@Description("CONTENT for substance, LANGUAGE for grammar/spelling/style")
			RubricScope scope) { }

	public enum RubricScope { CONTENT, LANGUAGE }
}
