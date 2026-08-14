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

/**
 *
 * Raised when the essay-generation LLM response cannot be parsed because it
 * was empty or cut off before the closing token — typically a reasoning
 * model (e.g. {@code fx-reasoning}) that exhausted the whole token budget on
 * internal reasoning and returned no content at all. Distinguished from
 * {@link AiEssayGenerationException} so callers can surface a graceful
 * "response truncated" outcome instead of a generic provider error.
 *
 * Initial date: 13 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiEssayGenerationResponseTruncatedException extends AiEssayGenerationException {

	private static final long serialVersionUID = 1L;

	public AiEssayGenerationResponseTruncatedException(String message, Throwable cause) {
		super(message, cause);
	}
}
