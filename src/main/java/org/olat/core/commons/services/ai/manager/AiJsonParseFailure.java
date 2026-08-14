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
package org.olat.core.commons.services.ai.manager;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.langchain4j.service.output.OutputParsingException;

/**
 * Initial date: 14.08.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
class AiJsonParseFailure {

	private AiJsonParseFailure() {
		//
	}

	/**
	 * Walk the exception cause chain looking for a truncated-response
	 * indicator. Two shapes occur in practice:
	 * <ul>
	 *   <li>A {@link JsonProcessingException} somewhere in the chain —
	 *   LangChain4j wraps Jackson failures in its own runtime exceptions
	 *   when the LLM reply does not validate against the structured-output
	 *   schema.</li>
	 *   <li>An {@link OutputParsingException} with no cause of its own —
	 *   LangChain4j's output parser throws this directly, without ever
	 *   reaching Jackson, when the model message content is {@code null}
	 *   or blank. This happens with reasoning models (e.g.
	 *   {@code fx-reasoning}) that exhaust the whole token budget on
	 *   internal reasoning and return no content at all. (When
	 *   {@code OutputParsingException} instead wraps a real Jackson
	 *   failure, that cause is picked up by the check above on the next
	 *   loop iteration.)</li>
	 * </ul>
	 */
	static boolean isJsonParseFailure(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof JsonProcessingException) {
				return true;
			}
			if (c instanceof OutputParsingException && c.getCause() == null) {
				return true;
			}
			if (c == c.getCause()) {
				break;
			}
		}
		return false;
	}

}
