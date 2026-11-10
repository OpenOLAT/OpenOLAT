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
package org.olat.core.dispatcher;

import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.dispatcher.ContentDomainNavigation.Navigation;
import org.olat.core.helpers.Settings;
import org.olat.core.util.WebappHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * 
 * Initial date: 10 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentDomainNavigationTest extends OlatTestCase {

	private static final String SERVER = "olat.example.org";
	private static final String CONTENT = "content.example.org";

	private String originalDomain;
	private String originalContentDomain;
	private int originalSecurePort;
	private String originalContextPath;
	
	@Autowired
	private Settings settings;
	@Autowired
	private CSPModule cspModule;

	@Before
	public void setUpDomains() {
		originalDomain = Settings.getServerDomainName();
		originalContentDomain = Settings.getContentDomainName();
		originalSecurePort = Settings.getServerSecurePort();
		originalContextPath = WebappHelper.getServletContextPath();

		settings.setServerDomainName(SERVER);
		settings.setServerSecurePort(443);
		Settings.setContentDomainName(CONTENT);
		WebappHelper.setServletContextPath("");
	}

	@After
	public void restoreDomains() {
		settings.setServerDomainName(originalDomain);
		settings.setServerSecurePort(originalSecurePort);
		Settings.setContentDomainName(originalContentDomain);
		WebappHelper.setServletContextPath(originalContextPath);
	}

	@Test
	public void navigationTypes() {
		Assert.assertEquals(Navigation.DOCUMENT, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "navigate", "document")));
		Assert.assertEquals(Navigation.FRAME, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "navigate", "iframe")));
		Assert.assertEquals(Navigation.FRAME, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "navigate", "frame")));
		Assert.assertEquals(Navigation.FRAME, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "navigate", "embed")));
		Assert.assertEquals(Navigation.FRAME, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "navigate", "object")));
		Assert.assertEquals(Navigation.NONE, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "no-cors", "image")));
		Assert.assertEquals(Navigation.NONE, ContentDomainNavigation.getNavigation(request("GET", "/auth/", "cors", "empty")));
		Assert.assertEquals(Navigation.NONE, ContentDomainNavigation.getNavigation(request("GET", "/auth/", null, null)));

		MockHttpServletRequest oldBrowser = request("GET", "/auth/", null, null);
		oldBrowser.addHeader("Accept", "text/html,application/xhtml+xml");
		Assert.assertEquals(Navigation.UNKNOWN, ContentDomainNavigation.getNavigation(oldBrowser));
	}

	@Test
	public void dispatchUri() {
		Assert.assertTrue(ContentDomainNavigation.isDispatchUri("/auth/1:2:3:4:0/"));
		Assert.assertTrue(ContentDomainNavigation.isDispatchUri("/auth/1%3A2%3A3%3A4%3A0/"));
		Assert.assertTrue(ContentDomainNavigation.isDispatchUri("/auth/1%3a2%3a3/file.pdf"));
		Assert.assertFalse(ContentDomainNavigation.isDispatchUri("/auth/RepositoryEntry/123/CourseNode/456"));
		Assert.assertFalse(ContentDomainNavigation.isDispatchUri("/auth/RepositoryEntry/123/path%3D~~a%3Ab.html/0"));
		Assert.assertFalse(ContentDomainNavigation.isDispatchUri("/url/1:2:3/"));
		Assert.assertFalse(ContentDomainNavigation.isDispatchUri("/auth/"));
	}

	@Test
	public void sanitize() {
		Assert.assertEquals("/auth/RepositoryEntry/123", ContentDomainNavigation.sanitize("/auth/RepositoryEntry/123", null));
		Assert.assertEquals("/auth/RepositoryEntry/123/CourseNode/456",
				ContentDomainNavigation.sanitize("/auth/RepositoryEntry/123;jsessionid=ABCDEF/CourseNode/456", null));
		Assert.assertEquals("/auth/RepositoryEntry/123",
				ContentDomainNavigation.sanitize("/auth/RepositoryEntry/123;JSESSIONID=ABCDEF", null));
		Assert.assertEquals("/auth/RepositoryEntry/123?a=b&c",
				ContentDomainNavigation.sanitize("/auth/RepositoryEntry/123",
						"o_winrndo=1&a=b&oow=12&token=secret&X-OLAT-TOKEN=t&_csrf=x&jsessionid=y&c"));
		Assert.assertEquals("/auth/RepositoryEntry/123",
				ContentDomainNavigation.sanitize("/auth/RepositoryEntry/123", "o_winrndo=1"));

		Assert.assertNull(ContentDomainNavigation.sanitize("/auth/Repository%0AEntry/123", null));
		Assert.assertNull(ContentDomainNavigation.sanitize("/auth/RepositoryEntry/123", "a=%0d%0aSet-Cookie"));
		Assert.assertNull(ContentDomainNavigation.sanitize("/auth/Repository\tEntry/123", null));
		Assert.assertNull(ContentDomainNavigation.sanitize("/auth/" + "a".repeat(ContentDomainNavigation.MAX_PATH_LENGTH), null));
		Assert.assertNull(ContentDomainNavigation.sanitize("auth/RepositoryEntry/123", null));
	}

	@Test
	public void documentGetBusinessPath() {
		MockHttpServletRequest request = request("GET", "/auth/RepositoryEntry/123/CourseNode/456", "navigate", "document");
		request.setQueryString("o_winrndo=1&oow=3");
		MockHttpServletResponse response = new MockHttpServletResponse();

		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(302, response.getStatus());
		Assert.assertEquals("https://" + SERVER + "/auth/RepositoryEntry/123/CourseNode/456", response.getRedirectedUrl());
		Assert.assertNull(request.getSession(false));
	}

	@Test
	public void documentGetOtherPaths() {
		assertRedirect(request("GET", "/", "navigate", "document"), "https://" + SERVER + "/");
		assertRedirect(request("GET", "/dmz/", "navigate", "document"), "https://" + SERVER + "/dmz/");
		// bookmark of a component URL of the SCORM delivery window
		assertRedirect(request("GET", "/auth/1%3A2%3A3%3A4/", "navigate", "document"), "https://" + SERVER + "/");
		// not acceptable path, start page
		assertRedirect(request("GET", "/auth/Repository%0AEntry/1", "navigate", "document"), "https://" + SERVER + "/");
	}

	@Test
	public void frameBusinessPath() throws Exception {
		MockHttpServletRequest request = request("GET", "/auth/RepositoryEntry/123", "navigate", "iframe");
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
		response.setHeader("Content-Security-Policy", "frame-ancestors 'self'");

		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(200, response.getStatus());
		Assert.assertNull(response.getHeader("X-FRAME-OPTIONS"));
		Assert.assertNull(response.getHeader("Set-Cookie"));
		Assert.assertEquals("no-store", response.getHeader("Cache-Control"));
		String csp = response.getHeader("Content-Security-Policy");
		Assert.assertTrue(csp.contains("frame-ancestors 'self'"));
		Assert.assertTrue(csp.contains(" https://" + SERVER + " https://" + CONTENT + ";"));
		Assert.assertTrue(csp.contains("default-src 'none'"));
		Assert.assertNull(request.getSession(false));

		String page = response.getContentAsString();
		Assert.assertTrue(page.contains("href=\"https://" + SERVER + "/auth/RepositoryEntry/123\""));
		Assert.assertTrue(page.contains("data-path=\"/auth/RepositoryEntry/123\""));
		Assert.assertTrue(page.contains("data-server=\"https://" + SERVER + "\""));
		Assert.assertTrue(page.contains("js/openolat/contentnav.js"));
	}

	@Test
	public void frameOtherPath() throws Exception {
		MockHttpServletRequest request = request("GET", "/catalog/", "navigate", "iframe");
		MockHttpServletResponse response = new MockHttpServletResponse();

		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(200, response.getStatus());
		String page = response.getContentAsString();
		Assert.assertTrue(page.contains("href=\"https://" + SERVER + "/catalog/\""));
		Assert.assertFalse(page.contains("data-path"));
	}

	@Test
	public void documentPost() {
		MockHttpServletRequest request = request("POST", "/auth/RepositoryEntry/123", "navigate", "document");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(200, response.getStatus());
		Assert.assertNull(response.getRedirectedUrl());
	}

	@Test
	public void unchanged() {
		// delivery paths
		assertUnchanged(request("GET", "/m/12345/index.html", "navigate", "document"));
		assertUnchanged(request("GET", "/m/12345/index.html", "navigate", "iframe"));
		assertUnchanged(request("GET", "/g/global/file.css", "navigate", "document"));
		assertUnchanged(request("GET", "/library/file.pdf", "navigate", "iframe"));
		assertUnchanged(request("GET", "/edusharing/goto", "navigate", "iframe"));
		assertUnchanged(request("GET", "/robots.txt", null, null));
		// component URLs of the SCORM delivery window in the frame
		assertUnchanged(request("GET", "/auth/1%3A2%3A3%3A4/", "navigate", "iframe"));
		assertUnchanged(request("POST", "/auth/1%3A2%3A3%3A4/", "navigate", "iframe"));
		// not a navigation
		assertUnchanged(request("GET", "/auth/RepositoryEntry/123", "no-cors", "image"));
		assertUnchanged(request("POST", "/auth/1%3A2%3A3%3A4/", "cors", "empty"));
		// WebDAV clients
		MockHttpServletRequest webdav = request("PROPFIND", "/webdav/", null, null);
		webdav.addHeader("Accept", "text/html");
		assertUnchanged(webdav);

		// main domain
		MockHttpServletRequest mainDomain = request("GET", "/auth/RepositoryEntry/123", "navigate", "document");
		mainDomain.setServerName(SERVER);
		assertUnchanged(mainDomain);
	}

	@Test
	public void contentDomainDisabled() {
		Settings.setContentDomainName(null);
		assertUnchanged(request("GET", "/auth/RepositoryEntry/123", "navigate", "document"));

		Settings.setContentDomainName(SERVER);
		MockHttpServletRequest request = request("GET", "/auth/RepositoryEntry/123", "navigate", "document");
		request.setServerName(SERVER);
		assertUnchanged(request);
	}

	@Test
	public void contextPath() {
		WebappHelper.setServletContextPath("/olat");
		MockHttpServletRequest request = request("GET", "/olat/auth/RepositoryEntry/123", "navigate", "document");
		assertRedirect(request, "https://" + SERVER + "/olat/auth/RepositoryEntry/123");

		assertUnchanged(request("GET", "/olat/m/123/index.html", "navigate", "iframe"));
	}

	@Test
	public void documentGetOnContentHost() {
		Assert.assertTrue(ContentDomainNavigation.isDocumentGetOnContentHost(request("GET", "/m/123/index.html", "navigate", "document")));
		Assert.assertFalse(ContentDomainNavigation.isDocumentGetOnContentHost(request("GET", "/m/123/index.html", "navigate", "iframe")));
		Assert.assertFalse(ContentDomainNavigation.isDocumentGetOnContentHost(request("GET", "/m/123/image.png", "no-cors", "image")));
		Assert.assertFalse(ContentDomainNavigation.isDocumentGetOnContentHost(request("POST", "/m/123/index.html", "navigate", "document")));
	}

	@Test
	public void frameAncestorsConfigured() {
		String originalFrameAncestors = cspModule.getContentSecurityPolicyFrameAncestors();
		try {
			cspModule.setContentSecurityPolicyFrameAncestors("https://lms.example.com");
			String frameAncestors = ContentDomainNavigation.getFrameAncestors(cspModule,
					"https://" + SERVER, "https://" + CONTENT);
			Assert.assertTrue(frameAncestors.startsWith("frame-ancestors 'self'"));
			Assert.assertTrue(frameAncestors.contains(" https://lms.example.com"));
			Assert.assertTrue(frameAncestors.endsWith(" https://" + SERVER + " https://" + CONTENT + ";"));
		} finally {
			cspModule.setContentSecurityPolicyFrameAncestors(originalFrameAncestors);
		}

		Assert.assertEquals("frame-ancestors https://" + SERVER + " https://" + CONTENT + ";",
				ContentDomainNavigation.getFrameAncestors(null, "https://" + SERVER, "https://" + CONTENT));
	}

	@Test
	public void renderIntermediatePageEscaped() {
		String page = ContentDomainNavigation.renderIntermediatePage(Locale.GERMAN, "<b>Open</b>",
				"https://olat.example.org/auth/a?b=1&c=\"x\"", "/auth/a\"onclick=\"x", "https://olat.example.org", "/raw/1/js/openolat/contentnav.js");
		Assert.assertTrue(page.contains("&lt;b&gt;Open&lt;/b&gt;"));
		Assert.assertTrue(page.contains("b=1&amp;c=&quot;x&quot;"));
		Assert.assertTrue(page.contains("data-path=\"/auth/a&quot;onclick=&quot;x\""));
		Assert.assertTrue(page.contains("lang=\"de\""));
	}

	@Test
	public void mainDomainFramedFromContentDomain() throws Exception {
		MockHttpServletRequest request = mainDomainRequest("GET", "/auth/RepositoryEntry/123/CourseNode/456", "iframe",
				"https://" + CONTENT + "/");
		request.setQueryString("o_winrndo=1&a=b");
		MockHttpServletResponse response = new MockHttpServletResponse();
		response.setHeader("X-FRAME-OPTIONS", "SAMEORIGIN");

		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(200, response.getStatus());
		Assert.assertNull(response.getHeader("X-FRAME-OPTIONS"));
		Assert.assertNull(response.getHeader("Set-Cookie"));
		Assert.assertTrue(response.getHeader("Content-Security-Policy").contains(" https://" + SERVER + " https://" + CONTENT + ";"));
		Assert.assertNull(request.getSession(false));

		String page = response.getContentAsString();
		Assert.assertTrue(page.contains("data-path=\"/auth/RepositoryEntry/123/CourseNode/456?a=b\""));
		Assert.assertTrue(page.contains("href=\"https://" + SERVER + "/auth/RepositoryEntry/123/CourseNode/456?a=b\""));
	}

	@Test
	public void mainDomainFramedFromContentDomainFullReferer() {
		MockHttpServletRequest request = mainDomainRequest("GET", "/url/RepositoryEntry/123", "frame",
				"https://" + CONTENT + "/m/12345/schedule.pdf");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(200, response.getStatus());
	}

	@Test
	public void mainDomainUnchanged() {
		String contentReferer = "https://" + CONTENT + "/";
		// top-level navigation from the content domain (new tab)
		assertUnchanged(mainDomainRequest("GET", "/auth/RepositoryEntry/123", "document", contentReferer));
		// frame of the main domain itself
		assertUnchanged(mainDomainRequest("GET", "/auth/RepositoryEntry/123", "iframe", "https://" + SERVER + "/auth/"));
		// no referrer (policy no-referrer) or another site
		assertUnchanged(mainDomainRequest("GET", "/auth/RepositoryEntry/123", "iframe", null));
		assertUnchanged(mainDomainRequest("GET", "/auth/RepositoryEntry/123", "iframe", "https://" + CONTENT + ".evil.com/"));
		assertUnchanged(mainDomainRequest("GET", "/auth/RepositoryEntry/123", "iframe", "http://" + CONTENT + "/"));
		// not a business path or a component URL
		assertUnchanged(mainDomainRequest("GET", "/dmz/", "iframe", contentReferer));
		assertUnchanged(mainDomainRequest("GET", "/m/123/index.html", "iframe", contentReferer));
		assertUnchanged(mainDomainRequest("POST", "/auth/1%3A2%3A3%3A4/", "iframe", contentReferer));
		// not a navigation
		MockHttpServletRequest image = mainDomainRequest("GET", "/auth/RepositoryEntry/123", "iframe", contentReferer);
		image.removeHeader("Sec-Fetch-Mode");
		image.addHeader("Sec-Fetch-Mode", "no-cors");
		assertUnchanged(image);
		// content domain disabled
		Settings.setContentDomainName(null);
		assertUnchanged(mainDomainRequest("GET", "/auth/RepositoryEntry/123", "iframe", contentReferer));
	}

	@Test
	public void refererContentOrigin() {
		Assert.assertTrue(ContentDomainNavigation.isRefererContentOrigin(mainDomainRequest("GET", "/", "iframe", "https://" + CONTENT)));
		Assert.assertTrue(ContentDomainNavigation.isRefererContentOrigin(mainDomainRequest("GET", "/", "iframe", "https://" + CONTENT + "/")));
		Assert.assertTrue(ContentDomainNavigation.isRefererContentOrigin(mainDomainRequest("GET", "/", "iframe", "https://" + CONTENT + "/m/1/a.html")));
		Assert.assertFalse(ContentDomainNavigation.isRefererContentOrigin(mainDomainRequest("GET", "/", "iframe", "https://" + CONTENT + ":8443/")));
		Assert.assertFalse(ContentDomainNavigation.isRefererContentOrigin(mainDomainRequest("GET", "/", "iframe", "https://" + CONTENT + "x.org/")));
		Assert.assertFalse(ContentDomainNavigation.isRefererContentOrigin(mainDomainRequest("GET", "/", "iframe", null)));
	}

	private static MockHttpServletRequest mainDomainRequest(String method, String uri, String dest, String referer) {
		MockHttpServletRequest request = request(method, uri, "navigate", dest);
		request.setServerName(SERVER);
		if(referer != null) {
			request.addHeader("Referer", referer);
		}
		return request;
	}

	private void assertRedirect(MockHttpServletRequest request, String expectedUrl) {
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertTrue(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(302, response.getStatus());
		Assert.assertEquals(expectedUrl, response.getRedirectedUrl());
	}

	private void assertUnchanged(MockHttpServletRequest request) {
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertFalse(ContentDomainNavigation.handle(request, response));
		Assert.assertEquals(200, response.getStatus());
		Assert.assertFalse(response.isCommitted());
	}

	private static MockHttpServletRequest request(String method, String uri, String mode, String dest) {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		request.setServerName(CONTENT);
		if(mode != null) {
			request.addHeader("Sec-Fetch-Mode", mode);
		}
		if(dest != null) {
			request.addHeader("Sec-Fetch-Dest", dest);
		}
		return request;
	}
}
