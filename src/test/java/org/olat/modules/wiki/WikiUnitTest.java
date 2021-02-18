/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.modules.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.wiki.versioning.ChangeInfo;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * WikiUnitTest with the whole framework started (will be deleted soon!)
 * <P>
 * Initial Date: May 7, 2006 <br>
 * 
 * @author guido
 */
public class WikiUnitTest extends OlatTestCase {
	private static final String WIKI_CONTENT = "==heading==\n'''bold'''\n[[Image:test.jpg|bla]]";
	
	@Test
	public void alternativeId() {
		String alternativeId = "SW5kZXg_.wp";
		String convertedId = WikiManager.convertAlternativeFilename(alternativeId);
		
		String indexId = WikiManager.generatePageId(WikiPage.WIKI_INDEX_PAGE) + WikiManager.WIKI_DOT_FILE_SUFFIX;
		Assert.assertEquals(indexId, convertedId);
	}

	@Test
	public void testWikiStuff() {
		WikiManager wikiMgr = WikiManager.getInstance();
		OLATResourceable ores = wikiMgr.createWiki(null);
		Wiki wiki = wikiMgr.getOrLoadWiki(ores);
		
		// add pages
		WikiPage page1 = new WikiPage("test-ä");
		page1.setContent(WIKI_CONTENT);
		WikiPage page2 = new WikiPage("test-ü");
		page1.setContent(WIKI_CONTENT);
		wiki.addPage(page1);
		wiki.addPage(page2);
		wikiMgr.saveWikiPage(ores, page1, true, wiki, true, null);
		wikiMgr.saveWikiPage(ores, page2, true, wiki, true, null);
		
		// reset wiki and load again from filesysetm
		wiki = null;
		page1 = null;
		page2 = null;
		wiki = wikiMgr.getOrLoadWiki(ores);
		assertNotNull("could not load wiki from repository", wiki);
		page1 = wiki.getPage("test-ä",true);
		page2 = wiki.getPage("test-ü",true);
		WikiPage pageTest = wiki.getPage(WikiManager.generatePageId("test-ä"));
		pageTest.setContent(WIKI_CONTENT);
		assertEquals(page1.getContent(), pageTest.getContent());
		assertEquals("Content of loaded wiki page is not the same after loading from filesystem", WIKI_CONTENT, page1.getContent());
		
		page1.setContent(WIKI_CONTENT+"\nThis is a new line");
		wikiMgr.saveWikiPage(ores, page1, true, wiki, true, null);
		List<ChangeInfo> diffs = wiki.getDiff(page1, page1.getVersion() -1, page1.getVersion());
		ChangeInfo change = diffs.get(0);
		
		assertEquals("INSERT", change.getType());
		assertEquals("This is a new line", change.getLines()[0]);
		
		//remove page and reloading should result in error page
		wiki.removePage(page1);
		wikiMgr.deleteWikiPage(ores, page1);
		wiki = wikiMgr.getOrLoadWiki(ores);
		assertEquals("wiki.error.page.not.found", wiki.getPage("test-ä", true).getContent());
		
		//clean up
		wikiMgr.deleteWiki(ores);
	}
}
