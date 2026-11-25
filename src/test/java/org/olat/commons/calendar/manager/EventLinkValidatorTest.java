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
package org.olat.commons.calendar.manager;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.helpers.Settings;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The links of a calendar event from an imported or a synchronized calendar.
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class EventLinkValidatorTest extends OlatTestCase {

	private static final String ROOT_URI = "https://learn.example.com/olat";

	@Autowired
	private ICalFileCalendarManager calendarManager;

	@Test
	public void isExternal() {
		Assert.assertTrue(EventLinkValidator.isExternal("https://example.org/page", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isExternal("javascript://x", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isExternal(ROOT_URI + "/url/RepositoryEntry/1", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isExternal("[RepositoryEntry:1][CourseNode:2]", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isExternal(null, ROOT_URI));
	}

	@Test
	public void isAllowedUri() {
		Assert.assertTrue(EventLinkValidator.isAllowedUri("https://example.org/page", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isAllowedUri("HTTP://example.org/page", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isAllowedUri(ROOT_URI + "/url/RepositoryEntry/1", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isAllowedUri("[RepositoryEntry:1][CourseNode:2]", ROOT_URI));

		Assert.assertFalse(EventLinkValidator.isAllowedUri("javascript://%0aalert(1)", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri("JavaScript://%0aalert(1)", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri(" javascript://%0aalert(1)", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri("data://text/html,x", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri("ftp://example.org/file", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri(null, ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri(" ", ROOT_URI));
	}

	/**
	 * An internal link is opened with a redirect of the server: only URLs and
	 * paths of this server and business paths are internal links.
	 */
	@Test
	public void isInternal() {
		Assert.assertTrue(EventLinkValidator.isInternal(ROOT_URI, ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isInternal(ROOT_URI + "/url/RepositoryEntry/1", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isInternal("[RepositoryEntry:1][CourseNode:2]", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isInternal("/olat/url/RepositoryEntry/1", ROOT_URI));

		Assert.assertFalse(EventLinkValidator.isInternal("//example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal("/\\example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal("/\t/example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal(" //example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal(ROOT_URI + ".example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal(ROOT_URI + "@example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal("javascript:alert(1)", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal("www.example.org", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal("https://example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isInternal(null, ROOT_URI));
	}

	@Test
	public void isAllowedUriRedirect() {
		Assert.assertFalse(EventLinkValidator.isAllowedUri("//example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri("/\\example.org/page", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri("javascript:alert(1)", ROOT_URI));
		Assert.assertFalse(EventLinkValidator.isAllowedUri("www.example.org", ROOT_URI));
		// a host which starts with the name of this server is an external link
		Assert.assertTrue(EventLinkValidator.isAllowedUri(ROOT_URI + ".example.org/page", ROOT_URI));
		Assert.assertTrue(EventLinkValidator.isExternal(ROOT_URI + ".example.org/page", ROOT_URI));
	}

	@Test
	public void validIconCssClass() {
		Assert.assertEquals("o_CourseModule_icon", EventLinkValidator.validIconCssClass("o_CourseModule_icon"));
		Assert.assertEquals("o_filetype_file o_filetype_pdf", EventLinkValidator.validIconCssClass(" o_filetype_file o_filetype_pdf "));
		Assert.assertNull(EventLinkValidator.validIconCssClass("x\" onmouseover=\"alert(1)\" x=\""));
		Assert.assertNull(EventLinkValidator.validIconCssClass("o_icon' x='"));
		Assert.assertNull(EventLinkValidator.validIconCssClass("o_icon<b>"));
		Assert.assertNull(EventLinkValidator.validIconCssClass(""));
		Assert.assertNull(EventLinkValidator.validIconCssClass(null));
	}

	@Test
	public void validLink() {
		KalendarEventLink link = new KalendarEventLink("p", "1", "Doc", "https://example.org/doc.pdf", "x\" onmouseover=\"alert(1)\" x=\"");
		KalendarEventLink validLink = EventLinkValidator.validLink(link, ROOT_URI, "test");
		Assert.assertNotNull(validLink);
		Assert.assertEquals("https://example.org/doc.pdf", validLink.getURI());
		Assert.assertEquals("", validLink.getIconCssClass());

		KalendarEventLink scriptLink = new KalendarEventLink("p", "2", "Doc", "javascript://%0aalert(1)", "o_icon_link");
		Assert.assertNull(EventLinkValidator.validLink(scriptLink, ROOT_URI, "test"));
		Assert.assertNull(EventLinkValidator.validLink(null, ROOT_URI, "test"));
	}

	/**
	 * An imported calendar is untrusted: a hostile icon class is dropped, a
	 * link with an external URI other than http or https and a link to an
	 * other server without scheme are dropped.
	 */
	@Test
	public void linksOfHostileCalendar() {
		String rootUri = Settings.getServerContextPathURI();
		String ics = """
				BEGIN:VCALENDAR
				VERSION:2.0
				PRODID:-//Test//Test//EN
				BEGIN:VEVENT
				UID:hostile-link-1
				DTSTAMP:20261125T100000Z
				DTSTART:20261125T100000Z
				DTEND:20261125T110000Z
				SUMMARY:Links
				X-OLAT-LINK:p§1§Doc§https://example.org/doc.pdf§x" onmouseover="alert(1)" x="
				X-OLAT-LINK:p§2§Script§javascript://%0aalert(1)§o_icon_link
				X-OLAT-LINK:p§3§Page§https://example.org/page§o_icon_link
				X-OLAT-LINK:p§4§Course§ROOT_URI/url/RepositoryEntry/1§o_CourseModule_icon
				X-OLAT-LINK:p§5§Business path§[RepositoryEntry:1]§o_CourseModule_icon
				X-OLAT-LINK:p§6§Other server§//example.org/page§o_icon_link
				END:VEVENT
				END:VCALENDAR
				""".replace("ROOT_URI", rootUri).replace("\n", "\r\n");
		Kalendar cal = calendarManager.buildKalendarFrom(new ByteArrayInputStream(ics.getBytes(StandardCharsets.UTF_8)),
				CalendarManager.TYPE_USER, "hostile-link");

		List<KalendarEventLink> links = cal.getEvent("hostile-link-1", null).getKalendarEventLinks();
		Assert.assertEquals(4, links.size());
		Assert.assertEquals("1", links.get(0).getId());
		Assert.assertEquals("https://example.org/doc.pdf", links.get(0).getURI());
		Assert.assertEquals("", links.get(0).getIconCssClass());
		Assert.assertEquals("3", links.get(1).getId());
		Assert.assertEquals("o_icon_link", links.get(1).getIconCssClass());
		Assert.assertEquals("4", links.get(2).getId());
		Assert.assertEquals(rootUri + "/url/RepositoryEntry/1", links.get(2).getURI());
		Assert.assertEquals("5", links.get(3).getId());
		Assert.assertEquals("[RepositoryEntry:1]", links.get(3).getURI());
	}
}
