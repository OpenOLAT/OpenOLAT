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

package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.restapi.ForumVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyForumsTest extends OlatRestTestCase {


	@Autowired
	private DB dbInstance;
	@Autowired
	private NotificationsManager notificationsManager;
	
	/**
	 * Test retrieve the forum which the user subscribe in a course.
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void myForums() throws IOException, URISyntaxException {
		URL courseUrl = MyForumsTest.class.getResource("myCourseWS.zip");
		RepositoryEntry myCourseRe = JunitTestHelper.deployCourse(null, "My course", courseUrl);// 4);	
		Assert.assertNotNull(myCourseRe);
		ICourse myCourse = CourseFactory.loadCourse(myCourseRe);
		
		final IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("my-");
		dbInstance.commitAndCloseSession();
		
		//load my forums
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		
		//subscribed to nothing
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id.getKey().toString()).path("forums").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		ForumVOes forums = conn.parse(response.getEntity(), ForumVOes.class);
		Assert.assertNotNull(forums);
		Assert.assertNotNull(forums.getForums());
		Assert.assertEquals(0, forums.getForums().length);
		
		//subscribe to the forum
		IdentityEnvironment ienv = new IdentityEnvironment(id.getIdentity(), Roles.userRoles());
		new CourseTreeVisitor(myCourse, ienv).visit(node -> {
			if(node instanceof FOCourseNode) {
				FOCourseNode forumNode = (FOCourseNode)node;	
				Forum forum = forumNode.loadOrCreateForum(myCourse.getCourseEnvironment());
				String businessPath = "[RepositoryEntry:" + myCourseRe.getKey() + "][CourseNode:" + forumNode.getIdent() + "]";
				SubscriptionContext forumSubContext = new SubscriptionContext("CourseModule", myCourse.getResourceableId(), forumNode.getIdent());
				PublisherData forumPdata = new PublisherData(OresHelper.calculateTypeName(Forum.class), forum.getKey().toString(), businessPath);
				notificationsManager.subscribe(id.getIdentity(), forumSubContext, forumPdata);
			}
		});
		dbInstance.commitAndCloseSession();
		
		//retrieve my forums
		HttpGet method2 = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response2 = conn.execute(method2);
		assertEquals(200, response2.getStatusLine().getStatusCode());
		ForumVOes forums2 = conn.parse(response2.getEntity(), ForumVOes.class);
		Assert.assertNotNull(forums2);
		Assert.assertNotNull(forums2.getForums());
		Assert.assertEquals(1, forums2.getForums().length);
	}
}