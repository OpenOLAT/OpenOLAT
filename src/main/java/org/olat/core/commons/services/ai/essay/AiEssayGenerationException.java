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
 * Thrown by {@link AiEssayQuestionGeneratorSPI#generateEssayQuestions} when
 * the underlying provider fails in a way the caller must surface as a
 * user-visible error. Unchecked to keep the SPI method signature terse —
 * callers are service-layer code that wraps these into a UI-friendly error
 * state in the drafts drawer.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiEssayGenerationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AiEssayGenerationException(String message) {
		super(message);
	}

	public AiEssayGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
