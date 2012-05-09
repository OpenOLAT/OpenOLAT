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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.notifications.restapi.vo.SubscriptionInfoVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.notification.UsersSubscriptionManager;

/**
 * 
 * <h3>Description:</h3>
 * Test if the web service for notifications
 * <p>
 * Initial Date:  26 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotificationsTest extends OlatJerseyTestCase {

	private static Identity userSubscriberId;
	private static Identity userAndForumSubscriberId;
	
	private static Forum forum;
	private RestConnection conn;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		conn = new RestConnection();
		
		userSubscriberId = JunitTestHelper.createAndPersistIdentityAsUser("rest-notifications-test-1");
		userAndForumSubscriberId = JunitTestHelper.createAndPersistIdentityAsUser("rest-notifications-test-2");
		JunitTestHelper.createAndPersistIdentityAsUser("rest-notifications-test-3");
		
		SubscriptionContext subContext = UsersSubscriptionManager.getInstance().getNewUsersSubscriptionContext();
		PublisherData publisherData = UsersSubscriptionManager.getInstance().getNewUsersPublisherData();
		NotificationsManager notifManager = NotificationsManager.getInstance();
		if(!notifManager.isSubscribed(userSubscriberId, subContext)) {
			notifManager.subscribe(userSubscriberId, subContext, publisherData);
		}
		if(!notifManager.isSubscribed(userAndForumSubscriberId, subContext)) {
			notifManager.subscribe(userAndForumSubscriberId, subContext, publisherData);
		}
		
		//create a forum
		ForumManager fm = ForumManager.getInstance();
		forum = ForumManager.getInstance().addAForum();
		Message m1 = fm.createMessage();
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		fm.addTopMessage(userSubscriberId, forum, m1);
		
		//subscribe
		SubscriptionContext forumSubContext = new SubscriptionContext("NotificationRestCourse", forum.getKey(), "2387");
		PublisherData forumPdata = new PublisherData(OresHelper.calculateTypeName(Forum.class), forum.getKey().toString(), "");
		if(!notifManager.isSubscribed(userAndForumSubscriberId, forumSubContext)) {
			notifManager.subscribe(userAndForumSubscriberId, forumSubContext, forumPdata);
		}
		notifManager.markPublisherNews(forumSubContext, userSubscriberId);
		
		//generate one notification
		String randomLogin = UUID.randomUUID().toString().replace("-", "");
		JunitTestHelper.createAndPersistIdentityAsUser(randomLogin);
		
		DBFactory.getInstance().commitAndCloseSession();
	}
	
  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
      e.printStackTrace();
      throw e;
		}
	}
	
	@Test
	public void testGetNotifications() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-notifications-test-1", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("notifications").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		List<SubscriptionInfoVO> infos = parseUserArray(body);
		
		assertNotNull(infos);
		assertFalse(infos.isEmpty());
		
		SubscriptionInfoVO infoVO = infos.get(0);
		assertNotNull(infoVO);
		assertNotNull(infoVO.getKey());
		assertNotNull("User", infoVO.getType());
		assertNotNull(infoVO.getTitle());
		assertNotNull(infoVO.getItems());
		assertFalse(infoVO.getItems().isEmpty());
	}
	
	@Test
	public void testGetUserNotifications() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-notifications-test-1", "A6B7C8"));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications").queryParam("type", "User");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		List<SubscriptionInfoVO> infos = parseUserArray(body);
		
		assertNotNull(infos);
		assertFalse(infos.isEmpty());
		
		SubscriptionInfoVO infoVO = infos.get(0);
		assertNotNull(infoVO);
		assertNotNull(infoVO.getKey());
		assertNotNull("User", infoVO.getType());
		assertNotNull(infoVO.getTitle());
		assertNotNull(infoVO.getItems());
		assertFalse(infoVO.getItems().isEmpty());
	}
	
	@Test
	public void testGetUserForumNotifications() throws URISyntaxException, IOException {
		assertTrue(conn.login(userAndForumSubscriberId.getName(), "A6B7C8"));
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR, -2);
		String date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.S").format(cal.getTime());

		URI uri = conn.getContextURI().path("notifications").queryParam("date", date).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response);
		assertNotNull(infos);
		assertTrue(2 <= infos.size());
	}
	
	@Test
	public void testGetUserForumNotificationsByType() throws IOException, URISyntaxException {
		assertTrue(conn.login(userAndForumSubscriberId.getName(), "A6B7C8"));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications").queryParam("type", "Forum");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		List<SubscriptionInfoVO> infos = parseUserArray(body);
		
		assertNotNull(infos);
		assertTrue(1 <= infos.size());
		
		SubscriptionInfoVO infoVO = infos.get(0);
		assertNotNull(infoVO);
		assertNotNull(infoVO.getKey());
		assertNotNull("Forum", infoVO.getType());
		assertNotNull(infoVO.getTitle());
		assertNotNull(infoVO.getItems());
		assertFalse(infoVO.getItems().isEmpty());
	}
	
	@Test
	public void testGetNoNotifications() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-notifications-test-3", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/notifications").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		List<SubscriptionInfoVO> infos = parseUserArray(body);
		
		assertNotNull(infos);
		assertTrue(infos.isEmpty());
	}
	
	protected List<SubscriptionInfoVO> parseUserArray(HttpResponse response) throws IOException, URISyntaxException {
		InputStream body = response.getEntity().getContent();
		return parseUserArray(body);
	}
	
	protected List<SubscriptionInfoVO> parseUserArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<SubscriptionInfoVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
