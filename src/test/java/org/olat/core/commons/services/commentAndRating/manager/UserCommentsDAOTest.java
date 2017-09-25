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
package org.olat.core.commons.services.commentAndRating.manager;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Test class for user comments package
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsDAOTest extends OlatTestCase {
	
	@Autowired
	private UserCommentsDAO userCommentsDao;

	
	@Test
	public void should_service_present() {
		Assert.assertNotNull(userCommentsDao);
	}
	
	@Test
	public void testCRUDComment() {
		//init 
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-2-" + UUID.randomUUID().toString());
				

		//check if there is any comments
		assertEquals(0, userCommentsDao.countComments(ores, null));
		assertEquals(0, userCommentsDao.countComments(ores, "blubli"));

		// add comments
		
		UserComment comment1 = userCommentsDao.createComment(ident1, ores, null, "Hello World");
		UserComment comment2 = userCommentsDao.createComment(ident1, ores, "blubli", "Hello World with subpath");
	  	// count must be 1 now. count without subpath should not include the results with subpath
		assertEquals(1, userCommentsDao.countComments(ores, null));
		assertEquals(1, userCommentsDao.countComments(ores, "blubli"));
		// 
		UserComment comment3 = userCommentsDao.createComment(ident2, ores, null, "Hello World");
		Assert.assertNotNull(comment3);
		UserComment comment4 = userCommentsDao.createComment(ident2, ores, "blubli", "Hello World with subpath");
		Assert.assertNotNull(comment4);
		Assert.assertEquals(2, userCommentsDao.countComments(ores, null));
		Assert.assertEquals(2, userCommentsDao.countComments(ores, "blubli"));
		// Same with get method
		List<UserComment> commentList = userCommentsDao.getComments(ores, null);
		assertEquals(2, commentList.size());
		List<UserComment> commentList2 = userCommentsDao.getComments(ores, "blubli");
		assertEquals(2, commentList2.size());
		// Create a reply to the first comments
		userCommentsDao.replyTo(comment1, ident2, "Reply 1");
		userCommentsDao.replyTo(comment2, ident2, "Reply 1 with subpath");
		Assert.assertEquals(3, userCommentsDao.countComments(ores, null));
		Assert.assertEquals(3, userCommentsDao.countComments(ores, "blubli"));
		// Delete first created coment with one reply each
		userCommentsDao.deleteComment(comment1, true);
		userCommentsDao.deleteComment(comment2, true);
		Assert.assertEquals(1, userCommentsDao.countComments(ores, null));
		Assert.assertEquals(1, userCommentsDao.countComments(ores, "blubli"));
		// Create reply to a comment that does not exis anymore -> should not create anything
		Assert.assertNull(userCommentsDao.replyTo(comment1, ident2, "Reply 1"));
		Assert.assertNull(userCommentsDao.replyTo(comment2, ident2, "Reply 1 with subpath"));
		// Recreate first comment
		comment1 = userCommentsDao.createComment(ident1, ores, null, "Hello World");
		comment2 = userCommentsDao.createComment(ident1, ores, "blubli", "Hello World with subpath");
		// Recreate a reply to the first comments
		userCommentsDao.replyTo(comment1, ident2, "Reply 1");
		userCommentsDao.replyTo(comment2, ident2, "Reply 1 with subpath");
		assertEquals(3, userCommentsDao.countComments(ores, null));
		assertEquals(3, userCommentsDao.countComments(ores, "blubli"));
		// Delete first created coment without the reply
		userCommentsDao.deleteComment(comment1, false);
		userCommentsDao.deleteComment(comment2, false);
		assertEquals(2, userCommentsDao.countComments(ores, null));
		assertEquals(2, userCommentsDao.countComments(ores, "blubli"));
		
		// Delete all comments without subpath
		assertEquals(2, userCommentsDao.deleteAllComments(ores, null));
		assertEquals(0, userCommentsDao.countComments(ores, null));
		assertEquals(2, userCommentsDao.countComments(ores, "blubli"));
		
		//add a comment without subpath
		comment1 = userCommentsDao.createComment(ident1, ores, null, "Hello World");
		
		// Delete all comments with subpath
		assertEquals(2, userCommentsDao.deleteAllComments(ores, "blubli"));
		assertEquals(1, userCommentsDao.countComments(ores, null));
		assertEquals(0, userCommentsDao.countComments(ores, "blubli"));

		//add a comment with subpath
		comment2 = userCommentsDao.createComment(ident2, ores, "blubli", "Hello World with subpath 2");
		
		// Delete ignoring subpath
		assertEquals(1, userCommentsDao.countComments(ores, null));
		assertEquals(1, userCommentsDao.countComments(ores, "blubli"));
		assertEquals(2, userCommentsDao.deleteAllCommentsIgnoringSubPath(ores));
		assertEquals(0, userCommentsDao.countComments(ores, null));
		assertEquals(0, userCommentsDao.countComments(ores, "blubli"));
	}
}
