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

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.Encoder;
import org.olat.course.assessment.model.SafeExamBrowserCheck;
import org.olat.course.assessment.model.SafeExamBrowserProbe;
import org.olat.course.assessment.model.SafeExamBrowserStatus;
import org.olat.course.assessment.model.SafeExamBrowserProbe.Source;
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
	
	@Test
	public void isVersionAllowed() {
		Assert.assertTrue(SafeExamBrowserValidator.isBrowserVersionAllowed("3.8.1", "3.8"));
		Assert.assertTrue(SafeExamBrowserValidator.isBrowserVersionAllowed("3.10.1", "3.8"));
		Assert.assertTrue(SafeExamBrowserValidator.isBrowserVersionAllowed("3.10.1", "3.8.1"));
		Assert.assertTrue(SafeExamBrowserValidator.isBrowserVersionAllowed("3.10", "3.8.1"));
	}
	
	@Test
	public void isVersionNotAllowed() {
		Assert.assertFalse(SafeExamBrowserValidator.isBrowserVersionAllowed("3.8", "3.8.1"));
		Assert.assertFalse(SafeExamBrowserValidator.isBrowserVersionAllowed("3.8", "3.10.1"));
		Assert.assertFalse(SafeExamBrowserValidator.isBrowserVersionAllowed("3.6.1", "3.6.2"));
	}

	private static final String SEB_KEY = "a3fa755508fa1ed69de26840012fb397bb0a527b55ca35f299fa89cb4da232c6";
	private static final String URL = "http://localhost/exam";
	private static final Function<String,String> MINIMAL_VERSIONS = _ -> "3.8";
	
	@Test
	public void isSafeExamBrowser_jsApi() {
		SafeExamBrowserProbe probe = jsProbe("wrong", "SEB_macOS_3.8.0_1_org.safeexambrowser");
		Assert.assertTrue(SafeExamBrowserValidator.isSafeExamBrowser(probe));
	}
	
	@Test
	public void isSafeExamBrowser_requestHashHeader() {
		MockHttpServletRequest request = request();
		request.addHeader("X-SafeExamBrowser-RequestHash", "anything");
		Assert.assertTrue(SafeExamBrowserValidator.isSafeExamBrowser(headersProbe(request)));
	}
	
	@Test
	public void isSafeExamBrowser_configKeyHashHeader() {
		MockHttpServletRequest request = request();
		request.addHeader("X-SafeExamBrowser-ConfigKeyHash", "anything");
		Assert.assertTrue(SafeExamBrowserValidator.isSafeExamBrowser(headersProbe(request)));
	}
	
	@Test
	public void isSafeExamBrowser_noHeader() {
		Assert.assertFalse(SafeExamBrowserValidator.isSafeExamBrowser(headersProbe(request())));
		Assert.assertFalse(SafeExamBrowserValidator.isSafeExamBrowser(SafeExamBrowserProbe.NONE));
		Assert.assertFalse(SafeExamBrowserValidator.isSafeExamBrowser(null));
	}
	
	/**
	 * The User-Agent suffix of SEB can be configured, it's not used for the detection.
	 */
	@Test
	public void isSafeExamBrowser_userAgentOnly() {
		MockHttpServletRequest request = request();
		request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh) AppleWebKit/605.1.15 SEB/3.8.0");
		Assert.assertFalse(SafeExamBrowserValidator.isSafeExamBrowser(headersProbe(request)));
	}
	
	@Test
	public void check_notRequired() {
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, headersProbe(request()), null, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.NOT_REQUIRED, check.status());
		Assert.assertTrue(check.isPassed());
	}
	
	@Test
	public void check_pending() {
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, SafeExamBrowserProbe.NONE, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.PENDING, check.status());
		Assert.assertFalse(check.isPassed());
	}
	
	@Test
	public void check_missing() {
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, headersProbe(request()), SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.MISSING, check.status());
	}
	
	@Test
	public void check_outdatedBeforeWrongConfig() {
		SafeExamBrowserProbe probe = jsProbe("wrong", "SEB_Windows_3.7.1_1_org.safeexambrowser");
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, probe, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OUTDATED, check.status());
		Assert.assertEquals("3.8", check.minimalVersion());
	}
	
	@Test
	public void check_outdatedNotEnforced() {
		SafeExamBrowserProbe probe = jsProbe(hash(SEB_KEY), "SEB_Windows_3.7.1_1_org.safeexambrowser");
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, probe, SEB_KEY, null, false, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OK, check.status());
	}
	
	/**
	 * Without JavaScript API, the version is unknown: an old SEB.
	 */
	@Test
	public void check_outdatedHeadersOnly() {
		MockHttpServletRequest request = request();
		request.addHeader("X-SafeExamBrowser-RequestHash", "anything");
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, headersProbe(request), SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OUTDATED, check.status());
		Assert.assertNull(check.minimalVersion());
	}
	
	/**
	 * Timeout of the keys update: the headers check sends the version of the
	 * JavaScript object, the version check doesn't fail.
	 */
	@Test
	public void check_headersWithVersion() {
		MockHttpServletRequest request = request();
		request.addHeader("X-SafeExamBrowser-RequestHash", "anything");
		SafeExamBrowserProbe probe = new SafeExamBrowserProbe(Source.HEADERS, null, null, URL,
				"SEB_Windows_3.8.0_1_org.safeexambrowser", request);
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, probe, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.WRONG_CONFIG, check.status());
	}

	@Test
	public void check_headersWithOutdatedVersion() {
		MockHttpServletRequest request = request();
		request.addHeader("X-SafeExamBrowser-RequestHash", "anything");
		SafeExamBrowserProbe probe = new SafeExamBrowserProbe(Source.HEADERS, null, null, URL,
				"SEB_Windows_3.7.1_1_org.safeexambrowser", request);
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, probe, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OUTDATED, check.status());
		Assert.assertEquals("3.8", check.minimalVersion());
	}

	@Test
	public void check_wrongConfig() {
		SafeExamBrowserProbe probe = jsProbe("wrong", "SEB_macOS_3.8.0_1_org.safeexambrowser");
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, probe, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.WRONG_CONFIG, check.status());
	}
	
	@Test
	public void check_okJsApi() {
		SafeExamBrowserProbe probe = jsProbe(hash(SEB_KEY), "SEB_macOS_3.8.0_1_org.safeexambrowser");
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, probe, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OK, check.status());
		Assert.assertTrue(check.isPassed());
	}
	
	@Test
	public void check_okConfigurationKeyHeaders() {
		String configurationKey = "gdfkhjsduzezrutuzsf";
		MockHttpServletRequest request = request();
		request.addHeader("x-safeexambrowser-configkeyhash", Encoder.sha256Exam("http://localhost/exam" + configurationKey));
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(null, headersProbe(request), null, configurationKey, false, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OK, check.status());
	}
	
	@Test
	public void check_reusePreviousWithoutAnswer() {
		SafeExamBrowserCheck previous = SafeExamBrowserCheck.valueOf(SafeExamBrowserStatus.WRONG_CONFIG, Source.JS_API);
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(previous, SafeExamBrowserProbe.NONE, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertSame(previous, check);
	}
	
	@Test
	public void check_jsApiWinsOverHeaders() {
		SafeExamBrowserCheck previous = SafeExamBrowserCheck.valueOf(SafeExamBrowserStatus.OK, Source.JS_API);
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(previous, headersProbe(request()), SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertSame(previous, check);
	}
	
	@Test
	public void check_jsApiUpgradesHeaders() {
		SafeExamBrowserCheck previous = SafeExamBrowserCheck.valueOf(SafeExamBrowserStatus.MISSING, Source.HEADERS);
		SafeExamBrowserProbe probe = jsProbe(hash(SEB_KEY), "SEB_iOS_3.8.0_1_org.safeexambrowser");
		SafeExamBrowserCheck check = SafeExamBrowserValidator.check(previous, probe, SEB_KEY, null, true, MINIMAL_VERSIONS);
		Assert.assertEquals(SafeExamBrowserStatus.OK, check.status());
	}
	
	private static String hash(String key) {
		return Encoder.sha256Exam(URL + key);
	}
	
	private static SafeExamBrowserProbe jsProbe(String configKeyHash, String browserVersion) {
		return new SafeExamBrowserProbe(Source.JS_API, configKeyHash, null, URL, browserVersion, request());
	}
	
	private static SafeExamBrowserProbe headersProbe(MockHttpServletRequest request) {
		return new SafeExamBrowserProbe(Source.HEADERS, null, null, URL, null, request);
	}
	
	private static MockHttpServletRequest request() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setServerName("localhost");
		request.setScheme("http");
		request.setRequestURI("/exam");
		return request;
	}

}
