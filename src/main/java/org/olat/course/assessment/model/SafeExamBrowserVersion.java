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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 26 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record SafeExamBrowserVersion(String os, String version) {
	
	private static final Logger log = Tracing.createLoggerFor(SafeExamBrowserVersion.class);
	
	
	/**
	 * Extract the operating system and the version from the value sent by Safe Exam Browser
	 * in the parameter "browserVersion" .
	 * <p>
	 * The expected format is {@code appDisplayName_<OS>_versionString_buildNumber_bundleID},
	 * e.g. {@code SEB_macOS_3.8.0_(build)_org.safeexambrowser.SafeExamBrowser}.
	 */
	public static final SafeExamBrowserVersion valueOf(String browserVersion) {
		if(!StringHelper.containsNonWhitespace(browserVersion)) {
			return null;
		}

		// appDisplayName _ OS _ versionString _ buildNumber _ bundleID
		String[] parts = browserVersion.split("_");
		if(parts.length < 3) {
			log.warn("Cannot extract operating system and version from Safe Exam Browser version: {}", browserVersion);
			return null;
		}

		String os = parts[1];
		String version = parts[2];
		return new SafeExamBrowserVersion(os, version);
	}
}
