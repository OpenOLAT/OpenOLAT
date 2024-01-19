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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.Encoder;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 
 * Initial date: 18 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SafeExamBrowserValidatorTest {
	
	@Test
	public void isSafelyAllowed() {
		String safeExamBrowserKey = "gdfkhjsduzezrutuzsf";
		String url = "http://localhost";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-requesthash", hash);
		request.setRequestURI("");
		
		boolean allowed = SafeExamBrowserValidator.isSafelyAllowed(request, safeExamBrowserKey, null);
		Assert.assertTrue(allowed);
	}
	
	/**
	 * SEB 2.1 and SEB 2.2 use slightly different URLs to calculate
	 * the hash. The first use the raw URL, the second remove the
	 * trailing /.
	 */
	@Test
	public void isSafelyAllowed_seb22() {
		String safeExamBrowserKey = "a3fa755508fa1ed69de26840012fb397bb0a527b55ca35f299fa89cb4da232c6";
		String url = "http://kivik.frentix.com";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("kivik.frentix.com");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-requesthash", hash);
		request.setRequestURI("/");
		
		boolean allowed = SafeExamBrowserValidator.isSafelyAllowed(request, safeExamBrowserKey, null);
		Assert.assertTrue(allowed);
	}
	
	@Test
	public void isSafelyAllowed_fail() {
		String safeExamBrowserKey = "gdfkhjsduzezrutuzsf";
		String url = "http://localhost";
		String hash = Encoder.sha256Exam(url + safeExamBrowserKey);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.addHeader("x-safeexambrowser-requesthash", hash);
		request.setRequestURI("/unauthorized/url");
		
		boolean allowed = SafeExamBrowserValidator.isSafelyAllowed(request, safeExamBrowserKey, null);
		Assert.assertFalse(allowed);
	}
	
	@Test
	public void isSafelyAllowed_missingHeader() {
		String safeExamBrowserKey = "gdfkhjsduzezrutuzsf";

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.setRequestURI("/unauthorized/url");
		
		boolean allowed = SafeExamBrowserValidator.isSafelyAllowed(request, safeExamBrowserKey, null);
		Assert.assertFalse(allowed);
	}

}
