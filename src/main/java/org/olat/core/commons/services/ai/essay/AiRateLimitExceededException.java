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
 * Thrown by the AI essay-grading and question-generation submit paths when
 * the caller exceeds the per-minute, per-user rate limit. The controllers
 * catch this exception and surface a translated error message
 * ({@code ai.essay.feedback.error.ratelimit} /
 * {@code ai.questions.error.ratelimit}) without leaking internal counters.
 *
 * Initial date: 2026-05-03<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiRateLimitExceededException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AiRateLimitExceededException(String message) {
		super(message);
	}
}
