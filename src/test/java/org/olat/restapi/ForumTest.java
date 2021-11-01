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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.restapi.MessageVO;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.modules.fo.restapi.ReplyVO;
import org.olat.restapi.support.vo.File64VO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ForumTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(ForumTest.class);
	
	private static Forum forum;
	private static Message m1, m2, m3, m4 ,m5;
	private static IdentityWithLogin id1;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	
	@Before
	public void setUp() throws Exception {

		id1 = JunitTestHelper.createAndPersistRndUser("rest-zero");
		
		forum = forumManager.addAForum();
		
		m1 = forumManager.createMessage(forum, id1.getIdentity(), false);
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		forumManager.addTopMessage(m1);
		
		m2 = forumManager.createMessage(forum, id1.getIdentity(), false);
		m2.setTitle("Thread-2");
		m2.setBody("Body of Thread-2");
		forumManager.addTopMessage(m2);
		
		dbInstance.intermediateCommit();
		
		m3 = forumManager.createMessage(forum, id1.getIdentity(), false);
		m3.setTitle("Message-1.1");
		m3.setBody("Body of Message-1.1");
		forumManager.replyToMessage(m3, m1);
		
		m4 = forumManager.createMessage(forum, id1.getIdentity(), false);
		m4.setTitle("Message-1.1.1");
		m4.setBody("Body of Message-1.1.1");
		forumManager.replyToMessage(m4, m3);
		
		m5 = forumManager.createMessage(forum, id1.getIdentity(), false);
		m5.setTitle("Message-1.2");
		m5.setBody("Body of Message-1.2");
		forumManager.replyToMessage(m5, m1);

		dbInstance.intermediateCommit();
	}
	
	@Test
	public void testGetThreads() throws IOException, URISyntaxException  {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getForumUriBuilder().path("threads").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<MessageVO> threads = parseMessageArray(response.getEntity());
		
		assertNotNull(threads);
		assertFalse(threads.isEmpty());	
		
		conn.shutdown();
	}
	
	@Test
	public void testGetThreadsWithPaging() throws IOException, URISyntaxException  {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getForumUriBuilder().path("threads")
				.queryParam("start", "0").queryParam("limit", "2").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVOes threads = conn.parse(response, MessageVOes.class);
		
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getTotalCount() >= 2);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetThread() throws IOException, URISyntaxException  {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<MessageVO> threads = parseMessageArray(response.getEntity());
		
		assertNotNull(threads);
		assertFalse(threads.isEmpty());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetThreadWithPaging() throws IOException, URISyntaxException  {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
				.queryParam("start", "0").queryParam("limit", "2").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVOes threads = conn.parse(response, MessageVOes.class);
		
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getTotalCount() >= 2);
		
		conn.shutdown();
	}

	@Test
	public void testNewThread() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getForumUriBuilder().path("threads").queryParam("authorKey", id1.getKey())
			.queryParam("title", "New thread")
			.queryParam("body", "A very interesting thread").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO thread = conn.parse(response, MessageVO.class);
		assertNotNull(thread);
		assertNotNull(thread.getKey());
		assertEquals(thread.getForumKey(), forum.getKey());
		assertEquals(thread.getAuthorKey(), id1.getKey());
		
		//really saved?
		boolean saved = false;
		List<Message> allMessages = forumManager.getMessagesByForum(forum);
		for(Message message:allMessages) {
			if(message.getKey().equals(thread.getKey())) {
				saved = true;
			}
		}
		assertTrue(saved);
		
		conn.shutdown();
	}
	
	@Test
	public void testNewMessage() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message")
			.queryParam("body", "A very interesting response in Thread-1").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		assertNotNull(message.getKey());
		assertEquals(message.getForumKey(), forum.getKey());
		assertEquals(message.getAuthorKey(), id1.getKey());
		assertEquals(message.getParentKey(), m1.getKey());
		
		//really saved?
		boolean saved = false;
		List<Message> allMessages = forumManager.getMessagesByForum(forum);
		for(Message msg:allMessages) {
			if(msg.getKey().equals(message.getKey())) {
				saved = true;
			}
		}
		assertTrue(saved);
		conn.shutdown();
	}
	
	@Test
	public void testNewMessageWithEntity() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		ReplyVO vo = new ReplyVO();
		vo.setTitle("Reply with attachment");
		vo.setBody("Reply with attachment body");
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		assertNotNull(message.getKey());
		assertEquals(message.getForumKey(), forum.getKey());
		assertEquals(message.getParentKey(), m1.getKey());
		
		//really saved?
		boolean saved = false;
		List<Message> allMessages = forumManager.getMessagesByForum(forum);
		for(Message msg:allMessages) {
			if(msg.getKey().equals(message.getKey())) {
				saved = true;
			}
		}
		assertTrue(saved);
		conn.shutdown();
	}
	
	@Test
	public void testGetAttachment() throws IOException, URISyntaxException {
		//set a attachment
		VFSContainer container = forumManager.getMessageContainer(m1.getForum().getKey(), m1.getKey());
		VFSLeaf attachment = container.createChildLeaf(UUID.randomUUID().toString().replace("-", "") + ".jpg");
		try(InputStream portraitIn = CoursesElementsTest.class.getResourceAsStream("portrait.jpg");
				OutputStream out=attachment.getOutputStream(false)) {
			assertNotNull(portraitIn);
			FileUtils.cpio(portraitIn, out, "");
		} catch(IOException e) {
			Assert.fail();
			log.error("", e);
		}

		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));

		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<FileVO> files = parseFileArray(response.getEntity());
		assertNotNull(files);
		
		FileVO attachmentVO = null;
		for(FileVO file:files) {
			if(attachment.getName().equals(file.getTitle())) {
				attachmentVO = file;
			}
		}
		
		assertNotNull(attachmentVO);
		
		URI downloadURI = new URI(attachmentVO.getHref());
		HttpGet download = conn.createGet(downloadURI, MediaType.APPLICATION_JSON, true);
		HttpResponse downloadResponse = conn.execute(download);
		assertEquals(200, downloadResponse.getStatusLine().getStatusCode());
		//String contentType = downloadResponse.getEntity().getContentType().getValue();
		//doesn't work with grizzly assertEquals("image/jpeg", contentType);
		conn.shutdown();
	}
	
	@Test
	public void testUploadAttachment() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		URL portraitUrl = CoursesElementsTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		HttpPost attachMethod = conn.createPost(attachUri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attachMethod, "portrait.jpg", portrait);
		HttpResponse attachResponse = conn.execute(attachMethod);
		assertEquals(200, attachResponse.getStatusLine().getStatusCode());
		
		//check if the file exists
		VFSContainer container = forumManager.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
				
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		try(InputStream uploadedStream = uploadedImage.getInputStream()) {
			BufferedImage image = ImageIO.read(uploadedStream);
			assertNotNull(image);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
		
		conn.shutdown();
	}
	
	@Test
	public void testUploadAttachmentOutOfBox() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		URL portraitUrl = CoursesElementsTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		HttpPost attachMethod = conn.createPost(attachUri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attachMethod, "../../portrait.jpg", portrait);
		HttpResponse attachResponse = conn.execute(attachMethod);
		assertEquals(200, attachResponse.getStatusLine().getStatusCode());
		
		//check if the file exists
		VFSContainer container = forumManager.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait.jpg");
		Assert.assertNull(uploadedFile);
		
		conn.shutdown();
	}
	
	@Test
	public void testUpload64Attachment() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		byte[] portraitBytes = getPortrait(); //attachment
		byte[] portrait64 = Base64.encodeBase64(portraitBytes, true);
		HttpPost attachMethod = conn.createPost(attachUri, MediaType.APPLICATION_JSON);
		
		attachMethod.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
		conn.addEntity(attachMethod, new BasicNameValuePair("file", new String(portrait64)),
				new BasicNameValuePair("filename", "portrait64.jpg"));

		HttpResponse attachCode = conn.execute(attachMethod);
		assertEquals(200, attachCode.getStatusLine().getStatusCode());
		
		//check if the file exists
		VFSContainer container = forumManager.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait64.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
		
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		try(InputStream uploadedStream = uploadedImage.getInputStream()) {
			BufferedImage image = ImageIO.read(uploadedStream);
			assertNotNull(image);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}

		conn.shutdown();
	}
	
	@Test
	public void testReplyWithTwoAttachments() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));

		ReplyVO vo = new ReplyVO();
		vo.setTitle("Reply with attachment");
		vo.setBody("Reply with attachment body");
		
		File64VO[] files = new File64VO[2];
		//upload portrait
		byte[] portraitBytes = getPortrait(); //attachment 
		
		byte[] portrait64 = Base64.encodeBase64(portraitBytes, true);
		files[0] = new File64VO("portrait64.jpg", new String(portrait64));
		//upload single page
		byte[] indexBytes = null;
		try(InputStream  indexStream = ForumTest.class.getResourceAsStream("singlepage.html")) {
			indexBytes = IOUtils.toByteArray(indexStream);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
		byte[] index64 = Base64.encodeBase64(indexBytes, true);
		files[1] = new File64VO("singlepage64.html", new String(index64));
		vo.setAttachments(files);

		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		method.addHeader("Accept-Language", "en");
		
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		assertNotNull(message.getAttachments());
		assertEquals(2, message.getAttachments().length);
		
		for(FileVO attachment:message.getAttachments()) {
			String title = attachment.getTitle();
			assertNotNull(title);
			String href = attachment.getHref();
			URI attachmentUri = new URI(href);
			HttpGet getAttachment = conn.createGet(attachmentUri, "*/*", true);
			HttpResponse attachmentCode = conn.execute(getAttachment);
			assertEquals(200, attachmentCode.getStatusLine().getStatusCode());
			EntityUtils.consume(attachmentCode.getEntity());
		}
		
		
		//check if the file existsforumManager
		VFSContainer container = forumManager.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait64.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
		
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		try(InputStream uploadedStream = uploadedImage.getInputStream()) {
			BufferedImage image = ImageIO.read(uploadedStream);
			FileUtils.closeSafely(uploadedStream);
			assertNotNull(image);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
		
		//check if the single page exists
		VFSItem uploadedPage = container.resolve("singlepage64.html");
		assertNotNull(uploadedPage);
		assertTrue(uploadedPage instanceof VFSLeaf);
		conn.shutdown();
	}
	
	@Test
	public void testUploadAttachmentWithFile64VO() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		byte[] portraitBytes = getPortrait();
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(message.getKey().toString()).path("attachments").build();
		HttpPut attachMethod = conn.createPut(attachUri, MediaType.APPLICATION_JSON, true);
		attachMethod.addHeader("Content-Type", MediaType.APPLICATION_JSON);
		
		
		byte[] portrait64 = Base64.encodeBase64(portraitBytes, true);
		File64VO fileVo = new File64VO();
		fileVo.setFile(new String(portrait64));
		fileVo.setFilename("portrait64vo.jpg");
		conn.addJsonEntity(attachMethod, fileVo);
		HttpResponse attachCode = conn.execute(attachMethod);
		assertEquals(200, attachCode.getStatusLine().getStatusCode());
		
		//check if the file exists
		VFSContainer container = forumManager.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait64vo.jpg");
		assertNotNull(uploadedFile);
		assertTrue(uploadedFile instanceof VFSLeaf);
		
		//check if the image is still an image
		VFSLeaf uploadedImage = (VFSLeaf)uploadedFile;
		try(InputStream uploadedStream = uploadedImage.getInputStream()) {
			BufferedImage image = ImageIO.read(uploadedStream);
			FileUtils.closeSafely(uploadedStream);
			assertNotNull(image);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
		
		conn.shutdown();
	}
	
	@Test
	public void testUploadAttachmentAndRename() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id1));
		
		URI uri = getForumUriBuilder().path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		//attachment
		URL portraitUrl = CoursesElementsTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		//upload portrait
		URI attachUri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpPost attachMethod = conn.createPost(attachUri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attachMethod, "portrait.jpg", portrait);
		HttpResponse attachCode = conn.execute(attachMethod);
		assertEquals(200, attachCode.getStatusLine().getStatusCode());
		EntityUtils.consume(attachCode.getEntity());

		//upload portrait a second time
		URI attach2Uri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpPost attach2Method = conn.createPost(attach2Uri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attach2Method, "portrait.jpg", portrait);
		HttpResponse attach2Code = conn.execute(attach2Method);
		assertEquals(200, attach2Code.getStatusLine().getStatusCode());
		EntityUtils.consume(attach2Code.getEntity());
		
		// load the attachments
		URI loadUri = getForumUriBuilder().path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpGet loadMethod = conn.createGet(loadUri, MediaType.APPLICATION_JSON, true);
		HttpResponse loadResponse = conn.execute(loadMethod);
		assertEquals(200, loadResponse.getStatusLine().getStatusCode());
		List<FileVO> files = parseFileArray(loadResponse.getEntity());
		assertNotNull(files);
		assertEquals(2, files.size());
		
		conn.shutdown();
	}
	
	private UriBuilder getForumUriBuilder() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("forums").path(forum.getKey().toString());
	}
	
	private byte[] getPortrait() {
		try(InputStream  portraitStream = CoursesElementsTest.class.getResourceAsStream("portrait.jpg")) {
			return IOUtils.toByteArray(portraitStream);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<MessageVO> parseMessageArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<MessageVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
