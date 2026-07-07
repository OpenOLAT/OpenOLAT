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

import org.olat.core.commons.services.ai.model.MCQuestionData;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LangChain4j AiServices interface for multiple choice question generation.
 * Instantiated per provider via AiServices.builder(...).chatModel(model).build().
 *
 * Initial date: 27.03.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface MCQuestionAiService {

	@SystemMessage("""
			You are an expert assessment designer. You create challenging multiple choice questions \
			that test genuine understanding, not just surface recall. \
			You always verify that correct answers are factually accurate and that wrong answers are \
			genuinely incorrect. Wrong answers must be plausible distractors."""
			+ AiPromptRules.OUTPUT_STYLE_RULES)
	@UserMessage("""
			Create {{number}} different multiple choice questions with {{correct}} correct \
			and {{wrong}} wrong answers each.

			Distribute the questions across these Bloom levels: {{bloomLevels}}.

			Target these learning objectives:
			{{learningObjectives}}

			{{targetDifficulty}}

			For each question, you MUST provide ALL of the following fields:
			- title: Short descriptive title for the question topic, max 10 words
			- topic: The specific topic within the subject area
			- subject: The broad subject area the question belongs to
			- keywords: Comma-separated keywords related to the question
			- question: The multiple choice question text, self-contained, no reference to 'the text' or 'the passage'
			- correctAnswers: List of correct answer options. Each option has two fields:
			  * text: the answer string shown to the learner, verifiably true based on the input
			  * feedback: leave this empty — correct answers do NOT need a feedback sentence
			- wrongAnswers: List of wrong answer options. Each option has two fields:
			  * text: the answer string shown to the learner, plausible but unambiguously incorrect
			  * feedback: ONE pedagogically helpful sentence that explains WHY this option is wrong in a way that helps the learner understand the underlying concept. Focus on the specific misconception or factual error this option represents, contrast it briefly with what would be correct, and use language that supports learning. Avoid generic phrases like "this is incorrect" or "this is wrong" — give a concrete, instructive reason.

			Rules for answer quality:
			- CORRECT answers: Must be verifiably true based on the provided input. Double-check each one.
			- WRONG answers: Must be clearly incorrect but plausible. Use these strategies:
			  * Take a correct fact and introduce a subtle but meaningful change (e.g. wrong number, swapped name, changed date)
			  * Invent a plausible-sounding but fictitious detail
			  * Use a true statement from a different context that does not answer this specific question
			- Each wrong answer must be unambiguously wrong — a knowledgeable person must be able to reject it.
			- Each correct answer must be unambiguously correct — it must be fully supported by the provided input.
			- Each wrong-answer feedback sentence must be concise (one sentence), written directly to the learner, instructive about the underlying concept, and must not just repeat the answer text.

			Language and terminology rules:
			- Generate all questions, answers, and metadata in {{language}}.
			- Use the exact terminology and wording from the input — do NOT translate or rephrase the source material.
			- Questions must be self-contained. Do NOT refer to "the text", "the passage", "the article", or similar.

			Use the following as the knowledge domain:
			-----
			{{input}}
			-----
			""")
	List<MCQuestionData> generateQuestions(
			@V("number") int number,
			@V("correct") int correct,
			@V("wrong") int wrong,
			@V("language") String language,
			@V("bloomLevels") String bloomLevels,
			@V("targetDifficulty") String targetDifficulty,
			@V("learningObjectives") String learningObjectives,
			@V("input") String input);
}
