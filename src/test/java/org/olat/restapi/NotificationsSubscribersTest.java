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
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.restapi.vo.PublisherVO;
import org.olat.core.commons.services.notifications.restapi.vo.SubscriberVO;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.userview.CourseTreeVisitor;
import org.olat.modules.fo.Forum;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 19.01.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationsSubscribersTest extends OlatRestTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private NotificationsManager notificationsManager;
	
	@Test
	public void subscribe() throws IOException, URISyntaxException {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-sub-1");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-sub-2");
		
		//deploy a course with forums
		URL courseUrl = MyForumsTest.class.getResource("myCourseWS.zip");
		RepositoryEntry courseEntry = JunitTestHelper.deployCourse(null, "My course", courseUrl);// 4);	
		Assert.assertNotNull(courseEntry);
		
		//load the course and found the first forum
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		//find the forum
		IdentityEnvironment ienv = new IdentityEnvironment(id1, Roles.userRoles());
		ForumVisitor forumVisitor = new ForumVisitor(course);
		new CourseTreeVisitor(course, ienv).visit(forumVisitor);
		FOCourseNode courseNode = forumVisitor.firstNode;
		Forum forum = forumVisitor.firstForum;
		Assert.assertNotNull(courseNode);
		Assert.assertNotNull(forum);
		
		//put subscribers
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		PublisherVO subscribersVO = new PublisherVO();
		//publisher data
		subscribersVO.setType("Forum");
		subscribersVO.setData(forum.getKey().toString());
		subscribersVO.setBusinessPath("[RepositoryEntry:" + courseEntry.getKey() + "][CourseNode:" + courseNode.getIdent() + "]");
		//context
		subscribersVO.setResName("CourseModule");
		subscribersVO.setResId(course.getResourceableId());
		subscribersVO.setSubidentifier(courseNode.getIdent());
		subscribersVO.getUsers().add(UserVOFactory.get(id1));
		subscribersVO.getUsers().add(UserVOFactory.get(id2));
		
		//create the subscribers
		URI subscribersUri = UriBuilder.fromUri(getContextURI()).path("notifications").path("subscribers").build();
		HttpPut putMethod = conn.createPut(subscribersUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(putMethod, subscribersVO);
		HttpResponse response = conn.execute(putMethod);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//get publisher
		SubscriptionContext subsContext
			= new SubscriptionContext("CourseModule", course.getResourceableId(), courseNode.getIdent());
		Publisher publisher = notificationsManager.getPublisher(subsContext);
		Assert.assertNotNull(publisher);
		
		//get subscribers
		List<Subscriber> subscribers = notificationsManager.getSubscribers(publisher, true);
		Assert.assertNotNull(subscribers);
		Assert.assertEquals(2, subscribers.size());
		
		conn.shutdown();
	}
	
	@Test
	public void unsubscribe() throws IOException, URISyntaxException {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-sub-3");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-sub-4");
		Identity id3 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-sub-5");
		
		//deploy a course with forums
		URL courseUrl = MyForumsTest.class.getResource("myCourseWS.zip");
		RepositoryEntry courseEntry = JunitTestHelper.deployCourse(null, "My course", courseUrl);	
		Assert.assertNotNull(courseEntry);
		
		//load the course and found the first forum
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		//find the forum
		IdentityEnvironment ienv = new IdentityEnvironment(id1, Roles.userRoles());
		ForumVisitor forumVisitor = new ForumVisitor(course);
		new CourseTreeVisitor(course, ienv).visit(forumVisitor);
		FOCourseNode courseNode = forumVisitor.firstNode;
		Forum forum = forumVisitor.firstForum;
		Assert.assertNotNull(courseNode);
		Assert.assertNotNull(forum);
		
		// the 3 users subscribed to the forum
		PublisherData publisherData
			= new PublisherData("Forum", forum.getKey().toString(), "[RepositoryEntry:" + courseEntry.getKey() + "][CourseNode:" + courseNode.getIdent() + "]");
		SubscriptionContext subsContext
			= new SubscriptionContext("CourseModule", course.getResourceableId(), courseNode.getIdent());
		notificationsManager.subscribe(id1, subsContext, publisherData);
		notificationsManager.subscribe(id2, subsContext, publisherData);
		notificationsManager.subscribe(id3, subsContext, publisherData);
		dbInstance.commitAndCloseSession();
		
		//get the subscriber
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI subscribersUri = UriBuilder.fromUri(getContextURI()).path("notifications").path("subscribers")
				.path(subsContext.getResName()).path(subsContext.getResId().toString()).path(subsContext.getSubidentifier()).build();
		HttpGet getMethod = conn.createGet(subscribersUri, MediaType.APPLICATION_JSON, true);
		HttpResponse getResponse = conn.execute(getMethod);
		Assert.assertEquals(200, getResponse.getStatusLine().getStatusCode());
		List<SubscriberVO> subscriberVOes = parseGroupArray(getResponse.getEntity().getContent());
		Assert.assertNotNull(subscriberVOes);
		Assert.assertEquals(3, subscriberVOes.size());
		
		SubscriberVO subscriberId2VO = null;
		for(SubscriberVO subscriberVO:subscriberVOes) {
			if(subscriberVO.getIdentityKey().equals(id2.getKey())) {
				subscriberId2VO = subscriberVO;
			}
		}

		//delete id2
		URI deleteSubscriberUri = UriBuilder.fromUri(getContextURI()).path("notifications").path("subscribers")
				.path(subscriberId2VO.getSubscriberKey().toString()).build();
		HttpDelete deleteMethod = conn.createDelete(deleteSubscriberUri, MediaType.APPLICATION_JSON);
		HttpResponse deleteResponse = conn.execute(deleteMethod);
		Assert.assertEquals(200, deleteResponse.getStatusLine().getStatusCode());
		
		//check
		Publisher publisher = notificationsManager.getPublisher(subsContext);
		List<Subscriber> survivingSubscribers = notificationsManager.getSubscribers(publisher, true);
		Assert.assertNotNull(survivingSubscribers);
		Assert.assertEquals(2, survivingSubscribers.size());
		for(Subscriber subscriber:survivingSubscribers) {
			Assert.assertNotEquals(id2, subscriber.getIdentity());
		}
	}
	
	protected List<SubscriberVO> parseGroupArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<SubscriberVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static class ForumVisitor implements Visitor {
		private FOCourseNode firstNode ;
		private Forum firstForum;	
		
		private final ICourse course;
		
		public ForumVisitor(ICourse course) {
			this.course = course;
		}
		
		@Override
		public void visit(INode node) {
			if(firstNode == null && node instanceof FOCourseNode) {
				firstNode = (FOCourseNode)node;
				firstForum = firstNode.loadOrCreateForum(course.getCourseEnvironment());
			}
		}
	}
}
