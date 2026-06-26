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
package org.olat.course.assessment.manager;

import java.util.StringTokenizer;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserValidator {
	
	private static final Logger log = Tracing.createLoggerFor(SafeExamBrowserValidator.class);
	
	private SafeExamBrowserValidator() {
		//
	}
	
	/**
	 * Check the Headers of the requests.
	 * 
	 * @param request The HTTP request
	 * @param safeExamBrowserKey The key
	 * @return true if the request is allowed based on the specified key
	 */
	public static boolean isSafelyAllowed(HttpServletRequest request, String safeExamBrowserKeys, String configurationKey) {
		boolean safe = false;
		if(StringHelper.containsNonWhitespace(safeExamBrowserKeys)) {
			String safeExamHash = request.getHeader("x-safeexambrowser-requesthash");
			String url = request.getRequestURL().toString();
			for(StringTokenizer tokenizer = new StringTokenizer(safeExamBrowserKeys); tokenizer.hasMoreTokens() && !safe; ) {
				String safeExamBrowserKey = tokenizer.nextToken();
				safe = isSafeExam(safeExamHash, safeExamBrowserKey, url);
			}
		} else if(StringHelper.containsNonWhitespace(configurationKey)) {
			String safeExamHash = request.getHeader("x-safeexambrowser-configkeyhash");
			String url = request.getRequestURL().toString();
			safe = isSafeExam(safeExamHash, configurationKey, url);
		} else {
			safe = true;
		}
		return safe;
	}
	
	/**
	 * Check the parameters sent by the JavaScript API of Safe Exam Browser.
	 * 
	 * @param safeExamHash Correspond to SafeExamBrowser.security.configKey
	 * @param url Correspond to window.location.toString()
	 * @param safeExamBrowserKeys
	 * @param configurationKey
	 * @return
	 */
	public static boolean isSafelyAllowedJs(String safeExamHash, String browserExamHash, String url, String safeExamBrowserKeys, String configurationKey) {
		boolean safe = false;

		if(StringHelper.containsNonWhitespace(safeExamHash) && StringHelper.containsNonWhitespace(url)) {
			if(StringHelper.containsNonWhitespace(safeExamBrowserKeys)) {
				for(StringTokenizer tokenizer = new StringTokenizer(safeExamBrowserKeys); tokenizer.hasMoreTokens() && !safe; ) {
					String safeExamBrowserKey = tokenizer.nextToken();
					safe = isSafeExam(safeExamHash, safeExamBrowserKey, url) || isSafeExam(browserExamHash, safeExamBrowserKey, url) ;
				}
			} else if(StringHelper.containsNonWhitespace(configurationKey)) {
				safe = isSafeExam(safeExamHash, configurationKey, url) || isSafeExam(browserExamHash, configurationKey, url);
			} else {
				safe = true;
				log.warn("Really safe???");
			}
		}
		return safe;
	}
	
	private static boolean isSafeExam(String safeExamHash, String safeExamBrowserKey, String url) {
		boolean safe = false;
		
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);
		if(safeExamHash != null && safeExamHash.equals(hash)) {
			safe = true;
		}

		if(!safe && url.endsWith("/")) {
			String strippedUrl = url.substring(0, url.length() - 1);
			String strippedHash = Encoder.sha256Exam(strippedUrl + safeExamBrowserKey);
			if(safeExamHash != null && safeExamHash.equals(strippedHash)) {
				safe = true;
			}
		}
		
		if(safeExamHash == null) {
			log.warn("Failed safeexambrowser request hash is null for URL: {} and key: {}", url, safeExamBrowserKey);
		} else {
			if(!safe) {
				log.warn("Failed safeexambrowser check: {} (Header) {} (Calculated) for URL: {}", safeExamHash, hash, url);
			}
			log.info("safeexambrowser {} : {} (Header) {} (Calculated) for URL: {} and key: {}", (safeExamHash.equals(hash) ? "Success" : "Failed") , safeExamHash, hash, url, safeExamBrowserKey);
		}
		return safe;
	}

	public static boolean isBrowserVersionAllowed(String browserVersion, String minimalVersion) {
		return compareVersions(browserVersion, minimalVersion) >= 0;
	}

	/**
	 * Compare 2 version strings segment by segment, each dot-separated segment being compared
	 * as an integer. Missing segments are treated as 0, e.g. {@code 3.8} equals {@code 3.8.0}.
	 *
	 * @param version The version to check
	 * @param minimalVersion The minimal required version
	 * @return a negative integer, zero or a positive integer as version is lower than, equal to
	 * 		or greater than the minimal version
	 */
	private static int compareVersions(String version, String minimalVersion) {
		int[] versionSegments = toSegments(version);
		int[] minimalSegments = toSegments(minimalVersion);

		int length = Math.max(versionSegments.length, minimalSegments.length);
		for(int i=0; i<length; i++) {
			int v = i < versionSegments.length ? versionSegments[i] : 0;
			int m = i < minimalSegments.length ? minimalSegments[i] : 0;
			if(v != m) {
				return Integer.compare(v, m);
			}
		}
		return 0;
	}

	private static int[] toSegments(String version) {
		if(!StringHelper.containsNonWhitespace(version)) {
			return new int[0];
		}

		String[] rawSegments = version.trim().split("\\.");
		int[] segments = new int[rawSegments.length];
		for(int i=0; i<rawSegments.length; i++) {
			segments[i] = parseSegment(rawSegments[i]);
		}
		return segments;
	}

	private static int parseSegment(String segment) {
		// Keep the leading digits only to be tolerant with suffixes like "3rc1"
		int end = 0;
		while(end < segment.length() && Character.isDigit(segment.charAt(end))) {
			end++;
		}
		if(end == 0) {
			return 0;
		}
		try {
			return Integer.parseInt(segment.substring(0, end));
		} catch (NumberFormatException e) {
			log.warn("Cannot parse Safe Exam Browser version segment: {}", segment);
			return 0;
		}
	}

}
