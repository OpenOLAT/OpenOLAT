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

package org.olat.bookmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * SecurityTestSuite is a container of all Tests in this package.
 * 
 * @author Andreas Ch. Kapp
 */
public class BookmarkManagerTest extends OlatTestCase {

	private static Logger log = Logger.getLogger(BookmarkManagerTest.class.getName());
	private Identity s1;
	private Identity s2;
	private Identity s3;
	private OLATResourceable olatres, olatres2;
	private BookmarkManager bm1;
	private BookmarkImpl bo1, bo2, bo3, bo4, bo5;
	private static boolean isInitialized = false;


	/**
	 * Test create update and delete a bookmark
	 */
	@Test
	public void testCreateUpdateDeleteBookmark() {
		//create
		bm1.createAndPersistBookmark(bo1);
		bm1.createAndPersistBookmark(bo2);
		bm1.createAndPersistBookmark(bo3);
		bm1.createAndPersistBookmark(bo4);
		bo1.setDescription("changed1");
		bo2.setDescription("changed2");
		bo3.setDescription("changed3");
		bo4.setDescription("changed4");
		bo1.setDetaildata("substring1");
		bo2.setDetaildata("substring2");
		bo3.setDetaildata("substring3");
		bo4.setDetaildata("substring4");
		bm1.updateBookmark(bo1);
		bm1.updateBookmark(bo2);
		bm1.updateBookmark(bo3);
		bm1.updateBookmark(bo4);
		bm1.deleteBookmark(bo1);
		bm1.deleteBookmark(bo2);
		bm1.deleteBookmark(bo3);
		bm1.deleteBookmark(bo4);
	}
	@Test
	public void testFindBookmarksByIdentity() {
		bm1.createAndPersistBookmark(bo1);
		bm1.createAndPersistBookmark(bo2);
		bm1.createAndPersistBookmark(bo3);
		bm1.createAndPersistBookmark(bo4);
		bm1.createAndPersistBookmark(bo5);
		List bookmarkList = bm1.findBookmarksByIdentity(s1);
		assertNotNull(bookmarkList);
		assertEquals("Wrong bookmark-list size",3,bookmarkList.size());
		bm1.deleteBookmark(bo1);
		bm1.deleteBookmark(bo2);
		bm1.deleteBookmark(bo3);
		bm1.deleteBookmark(bo4);
		bm1.deleteBookmark(bo5);
	}
	@Test
	public void testFindBookmarksByIdentityAndType() {
		bm1.createAndPersistBookmark(bo1);
		bm1.createAndPersistBookmark(bo2);
		bm1.createAndPersistBookmark(bo3);
		bm1.createAndPersistBookmark(bo4);
		bm1.createAndPersistBookmark(bo5);
		List bookmarkList = bm1.findBookmarksByIdentity(s1,olatres.getResourceableTypeName());
		assertNotNull(bookmarkList);
		assertEquals("Wrong bookmark-list size",2,bookmarkList.size());
		bm1.deleteBookmark(bo1);
		bm1.deleteBookmark(bo2);
		bm1.deleteBookmark(bo3);
		bm1.deleteBookmark(bo4);
		bm1.deleteBookmark(bo5);
	}
	@Test
	public void testDeleteAllBookmarksFor() {
		bm1.createAndPersistBookmark(bo1);
		bm1.createAndPersistBookmark(bo2);
		bm1.createAndPersistBookmark(bo3);
		bm1.createAndPersistBookmark(bo4);
		bm1.createAndPersistBookmark(bo5);
		List bookmarkList = bm1.findBookmarksByIdentity(s1,olatres.getResourceableTypeName());
		assertNotNull(bookmarkList);
		assertEquals("Wrong bookmark-list size",2,bookmarkList.size());
		bm1.deleteAllBookmarksFor(olatres);
		bookmarkList = bm1.findBookmarksByIdentity(s1,olatres2.getResourceableTypeName());
		assertNotNull(bookmarkList);
		assertEquals("Wrong bookmark-list size",1,bookmarkList.size());
		bm1.deleteBookmark(bo5);
	}
	@Test
	public void testIsResourceableBookmarked() {
		bm1.createAndPersistBookmark(bo1);
		bm1.createAndPersistBookmark(bo2);
		bm1.createAndPersistBookmark(bo3);
		bm1.createAndPersistBookmark(bo4);
    assertTrue(bm1.isResourceableBookmarked(s1, olatres));
    assertFalse(bm1.isResourceableBookmarked(s1, olatres2));
    bm1.createAndPersistBookmark(bo5);
    assertTrue(bm1.isResourceableBookmarked(s1, olatres2));
    assertFalse(bm1.isResourceableBookmarked(s2, olatres2));
    bm1.deleteBookmark(bo1);
		bm1.deleteBookmark(bo2);
		bm1.deleteBookmark(bo3);
		bm1.deleteBookmark(bo4);
		bm1.deleteBookmark(bo5);
	}

	@Before
	public void setUp() throws Exception {
		olatres  = OresHelper.createOLATResourceableInstance("Kürsli",new Long("123"));
		olatres2 = OresHelper.createOLATResourceableInstance("Kürsli2",new Long("124"));
		
		if (BookmarkManagerTest.isInitialized == false) {
			BookmarkManagerTest.isInitialized = true;
		}
		s1 = JunitTestHelper.createAndPersistIdentityAsUser("sabina");
		s2 = JunitTestHelper.createAndPersistIdentityAsUser("coop");
		s3 = JunitTestHelper.createAndPersistIdentityAsUser("diesbach");
		bm1 = BookmarkManager.getInstance();
		bo1 = new BookmarkImpl(olatres.getResourceableTypeName(), olatres.getResourceableTypeName(), olatres.getResourceableId(), "favorit", "", s1);
		bo2 = new BookmarkImpl(olatres.getResourceableTypeName(), olatres.getResourceableTypeName(), olatres.getResourceableId(), "buechzeiche", "", s2);
		bo3 = new BookmarkImpl(olatres.getResourceableTypeName(), olatres.getResourceableTypeName(), olatres.getResourceableId(), "bookmark", "", s3);
		bo4 = new BookmarkImpl(olatres.getResourceableTypeName(), olatres.getResourceableTypeName(), olatres.getResourceableId(), "merkseite", "", s1);
		bo5 = new BookmarkImpl(olatres2.getResourceableTypeName(), olatres2.getResourceableTypeName(), olatres2.getResourceableId(), "merkseite2", "", s1);  
	}

	@After
	public void tearDown() throws Exception {
		try {
			//DB.getInstance().delete("select * from o_bookmark");
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("tearDown failed: ", e);
		}
	}
}