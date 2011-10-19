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

package org.olat.restapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.restapi.MessageVO;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;

public class ForumTest extends OlatJerseyTestCase {
	
	private static Forum forum;
	private static Message m1, m2, m3, m4 ,m5;
	private static Identity id1;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();

		id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-zero");
		
		ForumManager fm = ForumManager.getInstance();
		forum = ForumManager.getInstance().addAForum();
		
		m1 = fm.createMessage();
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		fm.addTopMessage(id1, forum, m1);
		
		m2 = fm.createMessage();
		m2.setTitle("Thread-2");
		m2.setBody("Body of Thread-2");
		fm.addTopMessage(id1, forum, m2);
		
		DBFactory.getInstance().intermediateCommit();
		
		m3 = fm.createMessage();
		m3.setTitle("Message-1.1");
		m3.setBody("Body of Message-1.1");
		fm.replyToMessage(m3, id1, m1);
		
		m4 = fm.createMessage();
		m4.setTitle("Message-1.1.1");
		m4.setBody("Body of Message-1.1.1");
		fm.replyToMessage(m4, id1, m3);
		
		m5 = fm.createMessage();
		m5.setTitle("Message-1.2");
		m5.setBody("Body of Message-1.2");
		fm.replyToMessage(m5, id1, m1);

		DBFactory.getInstance().intermediateCommit();
	}
	
	@Test
	public void testGetThreads() throws IOException  {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("threads").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		List<MessageVO> threads = parseMessageArray(body);
		method.releaseConnection();
		assertNotNull(threads);
		assertFalse(threads.isEmpty());	
	}
	
	@Test
	public void testGetThreadsWithPaging() throws IOException  {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("threads")
				.queryParam("start", "0").queryParam("limit", "2").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		MessageVOes threads = parse(body, MessageVOes.class);
		method.releaseConnection();
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getTotalCount() >= 2);	
	}
	
	@Test
	public void testGetThread() throws IOException  {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		List<MessageVO> threads = parseMessageArray(body);
		method.releaseConnection();
		assertNotNull(threads);
		assertFalse(threads.isEmpty());	
	}
	
	@Test
	public void testGetThreadWithPaging() throws IOException  {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
				.queryParam("start", "0").queryParam("limit", "2").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		InputStream body = method.getResponseBodyAsStream();
		MessageVOes threads = parse(body, MessageVOes.class);
		method.releaseConnection();
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getTotalCount() >= 2);	
	}

	@Test
	public void testNewThread() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("threads").queryParam("authorKey", id1.getKey())
			.queryParam("title", "New thread")
			.queryParam("body", "A very interesting thread").build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		MessageVO thread = parse(body, MessageVO.class);
		assertNotNull(thread);
		assertNotNull(thread.getKey());
		assertEquals(thread.getForumKey(), forum.getKey());
		assertEquals(thread.getAuthorKey(), id1.getKey());
		
		//really saved?
		boolean saved = false;
		ForumManager fm = ForumManager.getInstance();
		List<Message> allMessages = fm.getMessagesByForum(forum);
		for(Message message:allMessages) {
			if(message.getKey().equals(thread.getKey())) {
				saved = true;
			}
		}
		assertTrue(saved);
	}
	
	@Test
	public void testNewMessage() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message")
			.queryParam("body", "A very interesting response in Thread-1").build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		MessageVO message = parse(body, MessageVO.class);
		assertNotNull(message);
		assertNotNull(message.getKey());
		assertEquals(message.getForumKey(), forum.getKey());
		assertEquals(message.getAuthorKey(), id1.getKey());
		assertEquals(message.getParentKey(), m1.getKey());
		
		//really saved?
		boolean saved = false;
		ForumManager fm = ForumManager.getInstance();
		List<Message> allMessages = fm.getMessagesByForum(forum);
		for(Message msg:allMessages) {
			if(msg.getKey().equals(message.getKey())) {
				saved = true;
			}
		}
		assertTrue(saved);
	}
	
	private UriBuilder getForumUriBuilder() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("forums").path(forum.getKey().toString());
	}
	
	protected List<MessageVO> parseMessageArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<MessageVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
