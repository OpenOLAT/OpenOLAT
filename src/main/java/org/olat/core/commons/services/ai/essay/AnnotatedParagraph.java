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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dev.langchain4j.model.output.structured.Description;

/**
 * One paragraph of the student's answer, reproduced verbatim as a list of
 * {@link AnnotatedSpan} instances, plus a short paragraph-level feedback note.
 * <p>
 * Invariant: the concatenation of all {@link AnnotatedSpan#text()} values in
 * {@code spans} MUST equal the original paragraph text exactly (case-insensitive
 * whitespace-collapsed comparison is used for the integrity check in
 * {@link EssayFormativeFeedbackService}). If the invariant is violated the
 * service falls back to a single {@link MarkKind#NEUTRAL} span.
 *
 * @param spans             Ordered list of verbatim span segments covering the
 *                          entire paragraph. Must not be {@code null}; may be
 *                          an empty list only when the paragraph itself is empty.
 * @param paragraphFeedback One to two sentences explaining what was good and
 *                          what to improve in this paragraph. Written in the
 *                          student's language.
 *
 * Initial date: 2026-05-07<br>
 *
 * @author Alan (AIT), https://www.frentix.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Description("One paragraph of the student's answer reproduced as verbatim spans, each tagged CORRECT / AMBIGUOUS / WRONG / NEUTRAL, plus a one-to-two sentence paragraph-level feedback note. The concatenation of all span texts MUST equal the original paragraph text exactly.")
public record AnnotatedParagraph(
		@Description("Ordered list of verbatim spans covering the full paragraph; concatenated they must equal the original paragraph text")
		List<AnnotatedSpan> spans,
		@Description("One to two sentences explaining what was good and what to improve in this paragraph; written in the student's language")
		String paragraphFeedback) {
}
