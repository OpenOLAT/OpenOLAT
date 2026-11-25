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
package org.olat.commons.calendar.restapi;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;

/**
 * The values of an event of the REST API are untrusted.
 *
 * Initial date: 25 Nov 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class CalendarWSHelperTest {

	@Test
	public void transferLinks() {
		EventVO eventVo = new EventVO();
		eventVo.setLinks(new EventLinkVO[] {
				link("1", "https://example.org/doc.pdf", "x\" onmouseover=\"alert(1)\" x=\""),
				link("2", "javascript://%0aalert(1)", "o_icon_link"),
				link("3", "https://example.org/page", "o_icon_link"),
				link("4", "[RepositoryEntry:1]", null)
		});
		KalendarEvent kalEvent = new KalendarEvent();
		CalendarWSHelper.transfer(eventVo, kalEvent);

		List<KalendarEventLink> links = kalEvent.getKalendarEventLinks();
		Assert.assertEquals(3, links.size());
		Assert.assertEquals("1", links.get(0).getId());
		Assert.assertEquals("", links.get(0).getIconCssClass());
		Assert.assertEquals("3", links.get(1).getId());
		Assert.assertEquals("o_icon_link", links.get(1).getIconCssClass());
		Assert.assertEquals("4", links.get(2).getId());
		Assert.assertNull(links.get(2).getIconCssClass());
	}

	private static EventLinkVO link(String id, String uri, String iconCssClass) {
		EventLinkVO link = new EventLinkVO();
		link.setProvider("test");
		link.setId(id);
		link.setDisplayName("Link " + id);
		link.setUri(uri);
		link.setIconCssClass(iconCssClass);
		return link;
	}
}
