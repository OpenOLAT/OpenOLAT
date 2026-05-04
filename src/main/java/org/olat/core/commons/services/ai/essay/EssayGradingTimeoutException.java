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
 * Raised when the synchronous grading call exceeds the 30-second hard
 * timeout. The UI turns this into the "bitte erneut versuchen" microcopy
 * so the student can retry without losing their answer.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class EssayGradingTimeoutException extends AiEssayGradingException {

	private static final long serialVersionUID = 1L;

	public EssayGradingTimeoutException(String message) {
		super(message);
	}

	public EssayGradingTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}
}
