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
import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.model.SafeExamBrowserCheck;
import org.olat.course.assessment.model.SafeExamBrowserProbe;
import org.olat.course.assessment.model.SafeExamBrowserStatus;
import org.olat.course.assessment.model.SafeExamBrowserVersion;
import org.olat.course.assessment.model.SafeExamBrowserProbe.Source;

/**
 * 
 * Initial date: 18 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserValidator {
	
	private static final Logger log = Tracing.createLoggerFor(SafeExamBrowserValidator.class);
	
	private static final String HEADER_REQUEST_HASH = "x-safeexambrowser-requesthash";
	private static final String HEADER_CONFIG_KEY_HASH = "x-safeexambrowser-configkeyhash";
	
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
			String safeExamHash = request.getHeader(HEADER_REQUEST_HASH);
			String url = request.getRequestURL().toString();
			for(StringTokenizer tokenizer = new StringTokenizer(safeExamBrowserKeys); tokenizer.hasMoreTokens() && !safe; ) {
				String safeExamBrowserKey = tokenizer.nextToken();
				safe = isSafeExam(safeExamHash, safeExamBrowserKey, url);
			}
		} else if(StringHelper.containsNonWhitespace(configurationKey)) {
			String safeExamHash = request.getHeader(HEADER_CONFIG_KEY_HASH);
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

	/**
	 * Detects a Safe Exam Browser independently of its configuration. The browser is a
	 * Safe Exam Browser if its JavaScript API answered, or if the request has one of the
	 * headers of the Safe Exam Browser (the value is not checked here). The User-Agent
	 * is not used because its suffix can be changed in the configuration of SEB.
	 *
	 * @param probe The answer of the browser
	 * @return true if the browser is a Safe Exam Browser
	 */
	public static boolean isSafeExamBrowser(SafeExamBrowserProbe probe) {
		if(probe == null) {
			return false;
		}
		if(probe.source() == Source.JS_API) {
			return true;
		}
		HttpServletRequest request = probe.request();
		return request != null
				&& (StringHelper.containsNonWhitespace(request.getHeader(HEADER_REQUEST_HASH))
						|| StringHelper.containsNonWhitespace(request.getHeader(HEADER_CONFIG_KEY_HASH)));
	}

	/**
	 * Evaluates the check chain: SEB detected, version allowed (only if enforced),
	 * configuration matches. The first failing check decides, the later checks are
	 * not evaluated.
	 * <ul>
	 * 	<li>Without key and configuration key, the result is always {@link SafeExamBrowserStatus#NOT_REQUIRED}.</li>
	 * 	<li>Without an answer of the browser, the previous result is reused if there is one, else
	 * 		the result is {@link SafeExamBrowserStatus#PENDING}.</li>
	 * 	<li>A result based on the JavaScript API is never replaced by a result based on the headers.</li>
	 * </ul>
	 *
	 * @param previous The previous result (can be null)
	 * @param probe The answer of the browser
	 * @param safeExamBrowserKeys The list of SEB keys (separated by white spaces)
	 * @param configurationKey The key of the configuration
	 * @param enforceMinimalVersion true if the minimal version is enforced
	 * @param minimalVersionByOs Returns the minimal version for an operating system
	 * @return The result of the chain
	 */
	public static SafeExamBrowserCheck check(SafeExamBrowserCheck previous, SafeExamBrowserProbe probe,
			String safeExamBrowserKeys, String configurationKey,
			boolean enforceMinimalVersion, Function<String,String> minimalVersionByOs) {
		if(!StringHelper.containsNonWhitespace(safeExamBrowserKeys) && !StringHelper.containsNonWhitespace(configurationKey)) {
			return SafeExamBrowserCheck.NOT_REQUIRED;
		}

		Source source = probe == null ? Source.NONE : probe.source();
		if(previous != null && previous.isBrowserChecked()
				&& (source == Source.NONE || (source == Source.HEADERS && previous.source() == Source.JS_API))) {
			return previous;
		}
		if(source == Source.NONE) {
			return SafeExamBrowserCheck.PENDING;
		}

		// Q1: is it a Safe Exam Browser?
		if(!isSafeExamBrowser(probe)) {
			return SafeExamBrowserCheck.valueOf(SafeExamBrowserStatus.MISSING, source);
		}

		// Q2: is the version allowed?
		if(enforceMinimalVersion) {
			// The version is sent with the headers too if the JavaScript object exists
			// (timeout of the keys update or SEB without updateKeys)
			SafeExamBrowserVersion versionInfos = SafeExamBrowserVersion.valueOf(probe.browserVersion());
			if(versionInfos == null) {
				// No JavaScript API or unknown format: an old Safe Exam Browser
				return SafeExamBrowserCheck.valueOf(SafeExamBrowserStatus.OUTDATED, source);
			}
			String minimalVersion = minimalVersionByOs == null ? null : minimalVersionByOs.apply(versionInfos.os());
			if(!isBrowserVersionAllowed(versionInfos.version(), minimalVersion)) {
				return new SafeExamBrowserCheck(SafeExamBrowserStatus.OUTDATED, source, minimalVersion);
			}
		}

		// Q3: does the configuration match?
		boolean safe = source == Source.JS_API
				? isSafelyAllowedJs(probe.configKey(), probe.browserExamKey(), probe.url(), safeExamBrowserKeys, configurationKey)
				: isSafelyAllowed(probe.request(), safeExamBrowserKeys, configurationKey);
		return SafeExamBrowserCheck.valueOf(safe ? SafeExamBrowserStatus.OK : SafeExamBrowserStatus.WRONG_CONFIG, source);
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
