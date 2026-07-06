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
 * Thrown by the AI essay-correction submit path when the interactive AI
 * task queue is so deep that the correction cannot start within the
 * learner's polling window. Failing fast at submit avoids creating a result
 * row and burning provider cost on a correction whose result nobody will
 * see. The controllers catch this exception and surface a translated
 * "high load, try again later" message
 * ({@code ai.essay.correction.overloaded}).
 *
 * Initial date: 2026-07-05<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiOverloadedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AiOverloadedException(String message) {
		super(message);
	}
}
