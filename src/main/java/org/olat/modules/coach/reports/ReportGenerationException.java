/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.coach.reports;

/**
 * Thrown if a report cannot be generated, for example because a parameter
 * references a deleted object.
 *
 * Initial date: 25 Nov 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ReportGenerationException extends RuntimeException {

	private static final long serialVersionUID = -3794017316286512270L;

	public ReportGenerationException(String message) {
		super(message);
	}

	public ReportGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
