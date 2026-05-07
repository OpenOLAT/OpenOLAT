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
 * Classification of a text span within an {@link AnnotatedSpan}.
 * <p>
 * The LLM assigns one of these kinds to every segment of the student's
 * answer when producing the inline annotation view.
 *
 * <ul>
 *   <li>{@link #CORRECT} — the span demonstrates correct understanding.</li>
 *   <li>{@link #AMBIGUOUS} — the span is partially correct or unclear.</li>
 *   <li>{@link #WRONG} — the span contains a factual or conceptual error.</li>
 *   <li>{@link #NEUTRAL} — filler or connective text that does not warrant
 *       marking; rendered as plain text by the UI. Use sparingly so the
 *       learner sees a clear signal where it matters.</li>
 * </ul>
 *
 * Initial date: 2026-05-07<br>
 *
 * @author Alan (AIT), https://www.frentix.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Description("Classification of a text span: CORRECT = clearly right, AMBIGUOUS = partial or unclear, WRONG = factual or conceptual error, NEUTRAL = filler/connective text not worth marking")
public enum MarkKind {

	CORRECT,
	AMBIGUOUS,
	WRONG,
	NEUTRAL;

}
