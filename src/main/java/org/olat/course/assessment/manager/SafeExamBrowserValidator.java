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
import org.olat.core.gui.UserRequest;
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
	
	public static boolean hasSEBHeaders(UserRequest ureq) {
		HttpServletRequest request = ureq.getHttpReq();
		String safeExamHash1 = request.getHeader("x-safeexambrowser-requesthash");
		String safeExamHash2 = request.getHeader("x-safeexambrowser-configkeyhash");
		return StringHelper.containsNonWhitespace(safeExamHash1)
				|| StringHelper.containsNonWhitespace(safeExamHash2);
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
	public static boolean isSafelyAllowedJs(String safeExamHash, String url, String safeExamBrowserKeys, String configurationKey) {
		boolean safe = false;

		if(StringHelper.containsNonWhitespace(safeExamHash) && StringHelper.containsNonWhitespace(url)) {
			if(StringHelper.containsNonWhitespace(safeExamBrowserKeys)) {
				for(StringTokenizer tokenizer = new StringTokenizer(safeExamBrowserKeys); tokenizer.hasMoreTokens() && !safe; ) {
					String safeExamBrowserKey = tokenizer.nextToken();
					safe = isSafeExam(safeExamHash, safeExamBrowserKey, url);
				}
			} else if(StringHelper.containsNonWhitespace(configurationKey)) {
				safe = isSafeExam(safeExamHash, configurationKey, url);
			} else {
				safe = true;
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

}
