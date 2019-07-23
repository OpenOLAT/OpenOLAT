/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.search.service.indexer.repository.course;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the regexp to find links to subpages.
 * 
 * @author Florian Gnägi, gnaegi@frentix.com, http://www.frentix.com
 */
public class SPCourseNodeIndexerTest {
	@Test
	public void testFindLinksRegexp()  {
		//VALID cases
		// direct
		Assert.assertEquals(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"gugs.html\" target=\"_blank\">yo man </a></body></html>"), "gugs.html");
		// subdirectory
		Assert.assertEquals(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"blabler/gugs.html\" target=\"_blank\">yo man </a></body></html>"), "blabler/gugs.html");
		Assert.assertEquals(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"gruebel/blabler/gugs.html\" target=\"_blank\">yo man </a></body></html>"), "gruebel/blabler/gugs.html");
		Assert.assertEquals(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"video-2/index.html\" class=\"s_goto_link s_goto_video\">yo man </a></body></html>"), "video-2/index.html");
		// with valid keywords
		
		// INVALID cases
		// absolute links
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"/gugs.html\" target=\"_blank\">yo man </a></body></html>"));
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"://gugs.html\" target=\"_blank\">yo man </a></body></html>"));
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"http://www.openolat.org/gugs.html\" target=\"_blank\">yo man </a></body></html>"));
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"https://www.openolat.org/gugs.html\" target=\"_blank\">yo man </a></body></html>"));
		// relative links
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"../gugs.html\" target=\"_blank\">yo man </a></body></html>"));
		// selfreference
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"#blub\" target=\"_blank\">yo man </a></body></html>"));
		// other protocol handlers
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"javascript:(void();)\" target=\"_blank\">yo man </a></body></html>"));
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"mailto:info@openolat.org\" target=\"_blank\">yo man </a></body></html>"));
		Assert.assertNull(findLink("<html><body><h1>asdf</h1>asdfkasdf <a href=\"tel:0435449000\" target=\"_blank\">yo man </a></body></html>"));
	}
	
	/**
	 * Helper to make it simpler to call in testcase
	 * @param page
	 * @return
	 */
	private String findLink(String page) {
		List<String> linkList = new ArrayList<>();
		SPCourseNodeIndexer.extractSubpageLinks(page, linkList);
		if (linkList.isEmpty()) {
			return null;
		} else {
			return linkList.get(0);
		}
	}
}
