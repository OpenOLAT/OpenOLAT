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

import static org.junit.Assert.assertEquals;
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
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.restapi.ForumVO;
import org.olat.modules.fo.restapi.ForumVOes;
import org.olat.modules.fo.restapi.MessageVO;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.modules.fo.restapi.ReplyVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.vo.File64VO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CoursesForumsTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CoursesForumsTest.class);

	private static ICourse course1;
	private static CourseNode forumNode;
	private static Identity admin;
	private static IdentityWithLogin id1;
	private static Message m1;
	private static Forum forum;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Before
	public void setUp() {
		if(course1 != null) return;
		
		admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		course1 = CourseFactory.loadCourse(courseEntry);
		dbInstance.intermediateCommit();
		
		//create a folder
		CourseNode rootNode = course1.getRunStructure().getRootNode();
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration("fo");
		forumNode = newNodeConfig.getInstance();
		forumNode.updateModuleConfigDefaults(true, rootNode, NodeAccessType.of(course1), null);
		forumNode.setShortTitle("Forum");
		forumNode.setNoAccessExplanation("You don't have access");
		course1.getEditorTreeModel().addCourseNode(forumNode, rootNode);
		
		forum = ((FOCourseNode)forumNode).loadOrCreateForum(course1.getCourseEnvironment());
		
		CourseFactory.publishCourse(course1, RepositoryEntryStatusEnum.published, admin, Locale.ENGLISH);

		id1 = JunitTestHelper.createAndPersistRndUser("rest-zero");
		m1 = forumManager.createMessage(forum, id1.getIdentity(), false);
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		forumManager.addTopMessage(m1);
	
		dbInstance.intermediateCommit();
		
		IdentitiesAddEvent identitiesAddEvent = new IdentitiesAddEvent(List.of(id1.getIdentity()));
		repositoryManager.addParticipants(admin, Roles.authorRoles(), identitiesAddEvent, courseEntry, null);
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testGetForumInfo() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getNodeURI()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		ForumVO forum = conn.parse(response, ForumVO.class);
		assertNotNull(forum);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetForumsInfo() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getNodesURI()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		ForumVOes forums = conn.parse(response, ForumVOes.class);
		assertNotNull(forums);
		assertEquals(1, forums.getTotalCount());
		assertNotNull(forums.getForums());
		assertEquals(1, forums.getForums().length);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetForum() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getForumURI()).path("threads").build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVOes threads = conn.parse(response, MessageVOes.class);
		assertNotNull(threads);
		
		conn.shutdown();
	}
	
	@Test
	public void testNewMessage() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString())
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
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		ReplyVO vo = new ReplyVO();
		vo.setTitle("Reply with attachment");
		vo.setBody("Reply with attachment body");
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString()).build();
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
			Assert.assertNotNull(portraitIn);
			FileUtils.cpio(portraitIn, out, "");
		} catch(IOException e) {
			Assert.fail();
			log.error("", e);
		}

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<FileVO> files = parseFileArray(response.getEntity());
		Assert.assertNotNull(files);
		
		FileVO attachmentVO = null;
		for(FileVO file:files) {
			if(attachment.getName().equals(file.getTitle())) {
				attachmentVO = file;
			}
		}
		
		Assert.assertNotNull(attachmentVO);
		conn.shutdown();
	}
	
	@Test
	public void testUploadAttachment() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString())
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
		URI attachUri = UriBuilder.fromUri(getForumURI()).path("posts").path(message.getKey().toString()).path("attachments").build();
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
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString())
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
		URI attachUri = UriBuilder.fromUri(getForumURI()).path("posts").path(message.getKey().toString()).path("attachments").build();
		HttpPost attachMethod = conn.createPost(attachUri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attachMethod, "../../portrait.jpg", portrait);
		HttpResponse attachResponse = conn.execute(attachMethod);
		assertEquals(406, attachResponse.getStatusLine().getStatusCode());
		
		//check if the file exists
		VFSContainer container = forumManager.getMessageContainer(message.getForumKey(), message.getKey());
		VFSItem uploadedFile = container.resolve("portrait.jpg");
		Assert.assertNull(uploadedFile);
		
		conn.shutdown();
	}
	
	@Test
	public void testUpload64Attachment() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString())
			.queryParam("authorKey", id1.getKey())
			.queryParam("title", "New message with attachment ")
			.queryParam("body", "A very interesting response in Thread-1 with an attachment").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		
		//upload portrait
		URI attachUri = UriBuilder.fromUri(getForumURI()).path("posts").path(message.getKey().toString()).path("attachments").build();
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
		RestConnection conn = new RestConnection(id1);

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
		try(InputStream  indexStream = CoursesForumsTest.class.getResourceAsStream("singlepage.html")) {
			indexBytes = IOUtils.toByteArray(indexStream);
		} catch(IOException e) {
			log.error("", e);
			Assert.fail();
		}
		byte[] index64 = Base64.encodeBase64(indexBytes, true);
		files[1] = new File64VO("singlepage64.html", new String(index64));
		vo.setAttachments(files);

		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		method.addHeader("Accept-Language", "en");
		
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		MessageVO message = conn.parse(response, MessageVO.class);
		assertNotNull(message);
		
		assertNotNull(message.getAttachments());
		assertEquals(2, message.getAttachments().length);
		
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
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString())
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
		URI attachUri = UriBuilder.fromUri(getForumURI()).path("posts").path(message.getKey().toString()).path("attachments").build();
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
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString())
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
		URI attachUri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpPost attachMethod = conn.createPost(attachUri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attachMethod, "portrait.jpg", portrait);
		HttpResponse attachCode = conn.execute(attachMethod);
		assertEquals(200, attachCode.getStatusLine().getStatusCode());
		EntityUtils.consume(attachCode.getEntity());

		//upload portrait a second time
		URI attach2Uri = UriBuilder.fromUri(getForumURI()).path("posts").path(m1.getKey().toString()).path("attachments").build();
		HttpPost attach2Method = conn.createPost(attach2Uri, MediaType.APPLICATION_JSON);
		conn.addMultipart(attach2Method, "portrait.jpg", portrait);
		HttpResponse attach2Code = conn.execute(attach2Method);
		assertEquals(200, attach2Code.getStatusLine().getStatusCode());
		EntityUtils.consume(attach2Code.getEntity());
		
		conn.shutdown();
	}
	
	private URI getNodeURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("elements").path("forum").path(forumNode.getIdent()).build();
	}
	
	private URI getForumURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("elements").path("forum").path(forumNode.getIdent()).path("forum").build();
	}
	
	private URI getNodesURI() {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course1.getResourceableId().toString())
			.path("elements").path("forum").build();
	}
	
	private byte[] getPortrait() {
		try(InputStream  portraitStream = CoursesForumsTest.class.getResourceAsStream("portrait.jpg")) {
			return IOUtils.toByteArray(portraitStream);
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
}
