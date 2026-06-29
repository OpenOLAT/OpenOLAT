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
package org.olat.core.commons.services.ai.service;

import java.util.List;

import org.olat.core.commons.services.ai.essay.EssayItemDraft;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LangChain4j AiServices interface for essay question generation.
 * Instantiated per provider via {@code AiServices.builder(...).chatModel(model).build()}.
 * The JSON schema is derived from {@link EssayItemDraft}'s {@code @Description}
 * annotations by LangChain4j's structured-output extraction.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public interface EssayGenerationAiService {

	@SystemMessage("""
			You are an expert assessment designer. You produce clear essay-style exam items \
			calibrated to a requested difficulty level. Easy items must be genuinely easy and \
			answerable in a sentence or a few keywords; you are encouraging, not adversarial. \
			For each item you also produce the grading metadata (reference excerpt, model answer, \
			key points, rubric criteria) that a downstream grader will use to evaluate student \
			answers objectively.""")
	@UserMessage("""
			Generate {{number}} different essay questions from the source material below.

			Distribute the questions across these Bloom levels: {{bloomLevels}}.

			Target these learning objectives:
			{{learningObjectives}}

			{{targetDifficulty}}

			For each item you MUST produce ALL of the following fields:
			- stimulus: Self-contained task statement, no reference to "the text". For difficulty 1 \
			(quiz mode) the stimulus MUST invite a short answer (name, list, or state briefly in one \
			sentence or a few keywords) and feel motivating and approachable.
			- modelAnswer: A concise reference answer that fully satisfies the question.
			- keyPoints: A list of ids (kp1, kp2, …) with text, weight, required flag. \
			Keep key points FEW so questions stay answerable; the count scales gently with \
			difficulty: difficulty 1 -> exactly 1 key point and it MUST NOT be required (quiz mode: \
			a single sentence or a few correct keywords already score well), difficulty 2 -> 1-2, \
			difficulty 3 -> 2, difficulty 4 -> 2-3, difficulty 5 -> 3-4. Mark a key point 'required' \
			only at difficulty 4-5 and only when its absence truly makes the answer wrong. Never \
			over-constrain easy questions.
			- rubricCriteria: A list of ids (c1, c2, …) with name, descriptor, weight, scope \
			CONTENT or LANGUAGE. Keep criteria minimal: difficulty 1 -> 1 simple CONTENT criterion, difficulty 2-3 -> 1-2, \
			difficulty 4-5 -> 2-3. Do not add a LANGUAGE criterion below difficulty 4.
			- bloomLevel: The Bloom level this item targets.
			- questionTitle: A short label (max 120 characters) of WHAT this specific question asks. \
			Distinct per question, suitable as a list/menu title. NOT the learning objective, NOT the answer. \
			Example: "Three trends in European higher education" — NOT "Understand the key concepts...".
			- learningObjective: Which learning objective this item maps to.
			- languageTag: BCP-47 tag (e.g. en, de); this must equal the question language.
			- referenceExcerpt: A short verbatim snippet from the source supporting the item.
			- tokenEstimate: Approximate token count for a good student answer, scaled to difficulty: \
			difficulty 1 -> 10-40 (one sentence or keywords), difficulty 2 -> 40-100, \
			difficulty 3 -> 80-200, difficulty 4-5 -> 150-500.
			- gradingHints: 1–2 short sentences of authoring guidance for the downstream grader \
			(what to reward, what to penalise); empty string if none.
			- commonMisconceptions: 0–3 short strings naming typical wrong-answer patterns the grader \
			should watch for; empty list if none.
			- difficulty: Estimated difficulty on a 1 (easy) to 5 (hard) scale.

			Quality rules:
			- Questions must be self-contained. Do NOT refer to "the text", "the passage", or "the source".
			- Model answers must be verifiable from the source. Do not invent facts.
			- Key-point weights must be positive. Required key points are the ones whose absence should \
			lower the student score meaningfully.
			- Rubric weights across all criteria should sum to roughly 1.0.
			- gradingHints and commonMisconceptions must be derivable from the source material; do not invent.

			Language and terminology rules:
			- Generate every field in {{language}}.
			- Use the exact terminology and wording from the source. Do NOT translate or rephrase \
			source material.

			Source material:
			-----
			{{chunks}}
			-----
			""")
	List<EssayItemDraft> generateEssayQuestions(
			@V("number") int number,
			@V("bloomLevels") String bloomLevels,
			@V("learningObjectives") String learningObjectives,
			@V("targetDifficulty") String targetDifficulty,
			@V("language") String language,
			@V("chunks") String chunks);

}
