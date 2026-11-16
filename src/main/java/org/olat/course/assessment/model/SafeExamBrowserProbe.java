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

import jakarta.servlet.http.HttpServletRequest;

/**
 * Everything the browser sent to answer the Safe Exam Browser check.
 * 
 * Initial date: 16 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 * @param source How the browser answered
 * @param configKey The configuration key hash sent by the JavaScript API (SafeExamBrowser.security.configKey)
 * @param browserExamKey The browser exam key hash sent by the JavaScript API (SafeExamBrowser.security.browserExamKey)
 * @param url The URL used to calculate the hashes (window.location)
 * @param browserVersion The version sent by the JavaScript API (SafeExamBrowser.version), also sent
 * 		with the headers check if the JavaScript object exists
 * @param request The HTTP request with the headers of the Safe Exam Browser
 */
public record SafeExamBrowserProbe(Source source, String configKey, String browserExamKey, String url,
		String browserVersion, HttpServletRequest request) {
	
	public static final SafeExamBrowserProbe NONE = new SafeExamBrowserProbe(Source.NONE, null, null, null, null, null);
	
	public enum Source {
		/** The JavaScript API of the Safe Exam Browser answered */
		JS_API,
		/** No JavaScript API, the request headers must be checked */
		HEADERS,
		/** The browser has not answered yet (first rendering, push update...) */
		NONE
	}
}
