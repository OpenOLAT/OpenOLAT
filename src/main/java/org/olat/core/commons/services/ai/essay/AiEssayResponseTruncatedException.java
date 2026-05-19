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
 * Raised when the LLM response cannot be parsed because it was cut off
 * before the closing token (typically a {@code max_tokens} cap or an
 * upstream stream interruption). The formative feedback service maps this
 * to a graceful student-facing rejection card rather than surfacing the
 * raw Jackson stack trace.
 *
 * Initial date: 2026-05-19<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiEssayResponseTruncatedException extends AiEssayGradingException {

	private static final long serialVersionUID = 1L;

	private final Long usageLogKey;

	public AiEssayResponseTruncatedException(String message, Long usageLogKey, Throwable cause) {
		super(message, cause);
		this.usageLogKey = usageLogKey;
	}

	/**
	 * Key of the {@code o_ai_usage_log} row already written by
	 * {@link org.olat.core.commons.services.ai.manager.AiLoggingChatModel} for
	 * the original LLM call. May be {@code null} if no row was created.
	 */
	public Long getUsageLogKey() {
		return usageLogKey;
	}
}
