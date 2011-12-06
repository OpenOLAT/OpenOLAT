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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.restapi.MessageVO;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.restapi.support.vo.File64VO;
import org.olat.restapi.support.vo.FileVO;
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
	
	@Test
	public void testGetAttachment() throws IOException, URISyntaxException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		GetMethod method = createGet(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		List<FileVO> files = parseFileArray(body);
		assertNotNull(files);
	}
	
	@Test
	public void testUploadAttachment() throws IOException, URISyntaxException {
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		MessageVO message = parse(body, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		URL portraitUrl = RepositoryEntriesTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		PostMethod attachMethod = createPost(attachUri, MediaType.APPLICATION_JSON, true);
		attachMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
			new FilePart("file", portrait),
			new StringPart("filename","portrait.jpg")
		};
		attachMethod.setRequestEntity(new MultipartRequestEntity(parts, attachMethod.getParams()));
		int attachCode = c.executeMethod(attachMethod);
		assertEquals(200, attachCode);
		attachMethod.releaseConnection();
		
		
		//check if the file exists
		ForumManager fm = ForumManager.getInstance();
		VFSContainer container = fm.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
				
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		InputStream uploadedStream = uploadedImage.getInputStream();
		BufferedImage image = ImageIO.read(uploadedStream);
		FileUtils.closeSafely(uploadedStream);
		assertNotNull(image);
	}
	
	@Test
	public void testUpload64Attachment() throws IOException, URISyntaxException {
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		MessageVO message = parse(body, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		InputStream  portraitStream = RepositoryEntriesTest.class.getResourceAsStream("portrait.jpg");
		assertNotNull(portraitStream);
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		byte[] portraitBytes = IOUtils.toByteArray(portraitStream);
		byte[] portrait64 = Base64.encodeBase64(portraitBytes, true);
		PostMethod attachMethod = createPost(attachUri, MediaType.APPLICATION_JSON, true);
		attachMethod.addRequestHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
		attachMethod.addParameter("file", new String(portrait64));
		attachMethod.addParameter("filename", "portrait64.jpg");

		int attachCode = c.executeMethod(attachMethod);
		assertEquals(200, attachCode);
		attachMethod.releaseConnection();
		
		//check if the file exists
		ForumManager fm = ForumManager.getInstance();
		VFSContainer container = fm.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait64.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
		
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		InputStream uploadedStream = uploadedImage.getInputStream();
		BufferedImage image = ImageIO.read(uploadedStream);
		FileUtils.closeSafely(uploadedStream);
		assertNotNull(image);
	}
	
	
	@Test
	public void testUploadAttachmentWithFile64VO() throws IOException, URISyntaxException {
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		MessageVO message = parse(body, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		InputStream  portraitStream = RepositoryEntriesTest.class.getResourceAsStream("portrait.jpg");
		assertNotNull(portraitStream);
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		PutMethod attachMethod = createPut(attachUri, MediaType.APPLICATION_JSON, true);
		attachMethod.addRequestHeader("Content-Type", MediaType.APPLICATION_JSON);
		
		byte[] portraitBytes = IOUtils.toByteArray(portraitStream);
		byte[] portrait64 = Base64.encodeBase64(portraitBytes, true);
		File64VO fileVo = new File64VO();
		fileVo.setFile(new String(portrait64));
		fileVo.setFilename("portrait64vo.jpg");
		String stringuifiedFileVo = stringuified(fileVo);
		RequestEntity entity = new StringRequestEntity(stringuifiedFileVo, MediaType.APPLICATION_JSON, "UTF-8");
		attachMethod.setRequestEntity(entity);
		int attachCode = c.executeMethod(attachMethod);
		assertEquals(200, attachCode);
		attachMethod.releaseConnection();
		
		//check if the file exists
		ForumManager fm = ForumManager.getInstance();
		VFSContainer container = fm.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait64vo.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
		
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		InputStream uploadedStream = uploadedImage.getInputStream();
		BufferedImage image = ImageIO.read(uploadedStream);
		FileUtils.closeSafely(uploadedStream);
		assertNotNull(image);
	}
	
	
	
	
	
	@Test
	public void testUploadAttachmentAndRename() throws IOException, URISyntaxException {
		HttpClient c = loginWithCookie(id1.getName(), "A6B7C8");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		PutMethod method = createPut(uri, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		InputStream body = method.getResponseBodyAsStream();
		MessageVO message = parse(body, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		URL portraitUrl = RepositoryEntriesTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		PostMethod attachMethod = createPost(attachUri, MediaType.APPLICATION_JSON, true);
		attachMethod.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
			new FilePart("file", portrait),
			new StringPart("filename","portrait.jpg")
		};
		attachMethod.setRequestEntity(new MultipartRequestEntity(parts, attachMethod.getParams()));
		int attachCode = c.executeMethod(attachMethod);
		assertEquals(200, attachCode);
		attachMethod.releaseConnection();

		//upload portrait a second time
		URI attach2Uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		PostMethod attach2Method = createPost(attach2Uri, MediaType.APPLICATION_JSON, true);
		attach2Method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts2 = { 
			new FilePart("file", portrait),
			new StringPart("filename","portrait.jpg")
		};
		attach2Method.setRequestEntity(new MultipartRequestEntity(parts2, attach2Method.getParams()));
		int attach2Code = c.executeMethod(attach2Method);
		assertEquals(200, attach2Code);
		attach2Method.releaseConnection();
		
		// load the attachments
		
		URI loadUri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		GetMethod loadMethod = createGet(loadUri, MediaType.APPLICATION_JSON, true);
		int loadCode = c.executeMethod(loadMethod);
		assertEquals(200, loadCode);
		InputStream loadBody = loadMethod.getResponseBodyAsStream();
		List<FileVO> files = parseFileArray(loadBody);
		assertNotNull(files);
		assertEquals(2, files.size());
		loadMethod.releaseConnection();
		
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
