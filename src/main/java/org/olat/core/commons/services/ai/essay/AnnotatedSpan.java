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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import dev.langchain4j.model.output.structured.Description;

/**
 * A verbatim segment of a student's paragraph, tagged with a
 * {@link MarkKind} and an optional inline comment.
 * <p>
 * Invariant: all {@code AnnotatedSpan} instances within an
 * {@link AnnotatedParagraph}, when their {@code text} fields are
 * concatenated, MUST equal the original paragraph text exactly.
 * The {@link EssayFormativeFeedbackService} enforces this and falls back to
 * a single {@link MarkKind#NEUTRAL} span when the invariant is violated.
 *
 * @param text    Verbatim substring of the student's answer paragraph.
 *                Never paraphrased or summarised.
 * @param kind    Classification of this span. See {@link MarkKind}.
 * @param comment Optional inline remark shown as a tooltip or hover overlay
 *                (max 120 chars). Only populate on {@link MarkKind#CORRECT}
 *                or {@link MarkKind#WRONG} spans when there is a specific
 *                concrete remark worth surfacing. May be {@code null} or blank.
 *
 * Initial date: 2026-05-07<br>
 *
 * @author Alan (AIT), https://www.frentix.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Description("A verbatim segment of a student paragraph tagged with a mark kind and an optional inline comment (max 120 chars). The concatenation of all span texts in a paragraph must equal the original paragraph exactly.")
public record AnnotatedSpan(
		@Description("Verbatim substring from the student's paragraph; do NOT paraphrase or summarise")
		String text,
		@Description("Classification: CORRECT, AMBIGUOUS, WRONG, or NEUTRAL (filler/connective — use sparingly)")
		MarkKind kind,
		@Description("Optional inline remark for tooltip (max 120 chars); only on CORRECT or WRONG spans with a specific concrete point; null or empty otherwise")
		String comment) {
}
