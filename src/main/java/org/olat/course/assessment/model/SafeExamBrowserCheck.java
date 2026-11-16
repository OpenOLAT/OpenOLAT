/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.assessment.model;

import org.olat.course.assessment.model.SafeExamBrowserProbe.Source;

/**
 * The result of the Safe Exam Browser check chain: SEB detected, version
 * allowed, configuration matches. The first failing check decides.
 * 
 * Initial date: 16 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 * @param status The result of the chain
 * @param source The source of the browser answer which produced the result
 * @param minimalVersion The minimal version for the operating system of the browser, only
 * 		set for {@link SafeExamBrowserStatus#OUTDATED} and if the operating system is known
 */
public record SafeExamBrowserCheck(SafeExamBrowserStatus status, Source source, String minimalVersion) {
	
	public static final SafeExamBrowserCheck NOT_REQUIRED = new SafeExamBrowserCheck(SafeExamBrowserStatus.NOT_REQUIRED, Source.NONE, null);
	public static final SafeExamBrowserCheck PENDING = new SafeExamBrowserCheck(SafeExamBrowserStatus.PENDING, Source.NONE, null);
	
	public static SafeExamBrowserCheck valueOf(SafeExamBrowserStatus status, Source source) {
		return new SafeExamBrowserCheck(status, source, null);
	}
	
	/**
	 * @return true if the participant can access the resource (as far as the Safe Exam Browser is concerned)
	 */
	public boolean isPassed() {
		return status == SafeExamBrowserStatus.NOT_REQUIRED || status == SafeExamBrowserStatus.OK;
	}
	
	/**
	 * @return true if the result is based on an answer of the browser
	 */
	public boolean isBrowserChecked() {
		return source == Source.JS_API || source == Source.HEADERS;
	}
}
