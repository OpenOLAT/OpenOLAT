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
 * <p>
 */ 

package org.olat.modules.wiki;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.olat.commons.coordinate.cluster.ClusterSyncer;
import org.olat.core.commons.persistence.DBImpl;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.persistence.DBQueryImpl;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.wiki.versioning.ChangeInfo;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.lock.pessimistic.PessimisticLockManager;
import org.olat.test.MockServletContextWebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * 
 * Description:<br>
 * WikiMockUnitTest: Mocking done with Mockito
 * 
 * <P>
 * Initial Date:  29.04.2010 <br>
 * @author guido
 * @author mila
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
		"classpath*:org/olat/fileresource/_spring/fileresourceContext.xml",
		"classpath*:org/olat/resource/_spring/resourceContext.xml",
		"classpath*:org/olat/test/_spring/coordinatorAndDatabaseContextMock.xml",
		"classpath*:org/olat/modules/wiki/_spring/wikiContext.xml",
		"classpath*:org/olat/test/_spring/webapphelperMock.xml",
		"classpath*:org/olat/core/util/vfs/version/_spring/versioningCorecontext.xml"})

@RunWith(SpringJUnit4ClassRunner.class)
public class WikiMockUnitTest {
	private static final String WIKI_CONTENT = "==heading==\n'''bold'''\n[[Image:test.jpg|bla]]";

	@Autowired
	WikiManager wikiMgr;
	@Autowired
	ClusterSyncer syncer;
	@Autowired
	OLATResourceManager resourceManager;

	private void setupMocks() {
		PessimisticLockManager mockLock = Mockito.mock(PessimisticLockManager.class);

		DBQuery query = Mockito.mock(DBQueryImpl.class);
		DBImpl mockDB = Mockito.mock(DBImpl.class);
		Mockito.when(mockDB.createQuery(Matchers.anyString())).thenReturn(query);
		
		resourceManager.setDbInstance(mockDB);
		syncer.setDbInstance(mockDB);
		syncer.setPessimisticLockManager(mockLock);
	}

	@Test public void testWikiStuff() {
		setupMocks();

		OLATResourceable ores = wikiMgr.createWiki();
		Wiki wiki = wikiMgr.getOrLoadWiki(ores);
		// add pages
		WikiPage page1 = new WikiPage("test-ä");
		page1.setContent(WIKI_CONTENT);

		WikiPage page2 = new WikiPage("test-ü");
		page1.setContent(WIKI_CONTENT);
		wiki.addPage(page1);
		wiki.addPage(page2);
		wikiMgr.saveWikiPage(ores, page1, true, wiki);
		wikiMgr.saveWikiPage(ores, page2, true, wiki);

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

		wikiMgr.saveWikiPage(ores, page1, true, wiki);

		List diffs = wiki.getDiff(page1, page1.getVersion() -1, page1.getVersion());
		ChangeInfo change = (ChangeInfo)diffs.get(0);

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
