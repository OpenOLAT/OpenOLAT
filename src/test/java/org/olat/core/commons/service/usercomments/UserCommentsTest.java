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
package org.olat.core.commons.service.usercomments;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.UserCommentsManager;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description:<br>
 * Test class for user comments package
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsTest extends OlatTestCase {
	
	@Test
	public void should_service_present() {
		CommentAndRatingService service = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		Assert.assertNotNull(service);
	}
	
	@Test
	public void testCRUDComment() {
		//init 
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		Identity ident1 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-1-" + UUID.randomUUID().toString());
		Identity ident2 = JunitTestHelper.createAndPersistIdentityAsUser("ucar-crud-2-" + UUID.randomUUID().toString());
		CommentAndRatingService service = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		Assert.assertNotNull(service);			
		service.init(ident1, ores, null, true, false);
		CommentAndRatingService serviceWithSubPath = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		Assert.assertNotNull(serviceWithSubPath);
		serviceWithSubPath.init(ident1, ores, "blubli", true, false);				

		//check if there is any comments
		assertEquals(0, service.getUserCommentsManager().countComments());
		assertEquals(0, serviceWithSubPath.getUserCommentsManager().countComments());

		// add comments
		UserCommentsManager ucm = service.getUserCommentsManager();
		UserCommentsManager ucm2 = serviceWithSubPath.getUserCommentsManager();
		
	  UserComment comment1 = ucm.createComment(ident1, "Hello World");
	  UserComment comment2 = ucm2.createComment(ident1, "Hello World with subpath");
	  // count must be 1 now. count without subpath should not include the results with subpath
		assertEquals(1, service.getUserCommentsManager().countComments());
		assertEquals(1, serviceWithSubPath.getUserCommentsManager().countComments());
		// 
	  UserComment comment3 = ucm.createComment(ident2, "Hello World");
	  Assert.assertNotNull(comment3);
	  UserComment comment4 = ucm2.createComment(ident2, "Hello World with subpath");
	  Assert.assertNotNull(comment4);
	  Assert.assertEquals(2, service.getUserCommentsManager().countComments());
	  Assert.assertEquals(2, serviceWithSubPath.getUserCommentsManager().countComments());
		// Same with get method
		List<UserComment> commentList = ucm.getComments();
		assertEquals(2, commentList.size());
		List<UserComment> commentList2 = ucm2.getComments();
		assertEquals(2, commentList2.size());
		// Create a reply to the first comments
		ucm.replyTo(comment1, ident2, "Reply 1");
		ucm2.replyTo(comment2, ident2, "Reply 1 with subpath");
		Assert.assertEquals(3, service.getUserCommentsManager().countComments());
		Assert.assertEquals(3, serviceWithSubPath.getUserCommentsManager().countComments());
		// Delete first created coment with one reply each
		ucm.deleteComment(comment1, true);
		ucm2.deleteComment(comment2, true);
		Assert.assertEquals(1, service.getUserCommentsManager().countComments());
		Assert.assertEquals(1, serviceWithSubPath.getUserCommentsManager().countComments());
		// Create reply to a comment that does not exis anymore -> should not create anything
		Assert.assertNull(ucm.replyTo(comment1, ident2, "Reply 1"));
		Assert.assertNull(ucm2.replyTo(comment2, ident2, "Reply 1 with subpath"));
		// Recreate first comment
	  comment1 = ucm.createComment(ident1, "Hello World");
	  comment2 = ucm2.createComment(ident1, "Hello World with subpath");
		// Recreate a reply to the first comments
		ucm.replyTo(comment1, ident2, "Reply 1");
		ucm2.replyTo(comment2, ident2, "Reply 1 with subpath");
		assertEquals(3, service.getUserCommentsManager().countComments());
		assertEquals(3, serviceWithSubPath.getUserCommentsManager().countComments());
		// Delete first created coment without the reply
		ucm.deleteComment(comment1, false);
		ucm2.deleteComment(comment2, false);
		assertEquals(2, service.getUserCommentsManager().countComments());
		assertEquals(2, serviceWithSubPath.getUserCommentsManager().countComments());
		
		// Delete all comments without subpath
		assertEquals(2, ucm.deleteAllComments());
		assertEquals(0, service.getUserCommentsManager().countComments());
		assertEquals(2, serviceWithSubPath.getUserCommentsManager().countComments());
		
		//add a comment without subpath
		comment1 = ucm.createComment(ident1, "Hello World");
		
		// Delete all comments with subpath
		assertEquals(2, ucm2.deleteAllComments());
		assertEquals(1, service.getUserCommentsManager().countComments());
		assertEquals(0, serviceWithSubPath.getUserCommentsManager().countComments());

		//add a comment with subpath
		comment2 = ucm2.createComment(ident2, "Hello World with subpath 2");
		
		// Delete ignoring subpath
		assertEquals(1, service.getUserCommentsManager().countComments());
		assertEquals(1, serviceWithSubPath.getUserCommentsManager().countComments());
		assertEquals(2, ucm2.deleteAllCommentsIgnoringSubPath());
		assertEquals(0, service.getUserCommentsManager().countComments());
		assertEquals(0, serviceWithSubPath.getUserCommentsManager().countComments());
	}
}
