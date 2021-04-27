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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.restapi.vo.PublisherVO;
import org.olat.core.commons.services.notifications.restapi.vo.SubscriptionInfoVO;
import org.olat.core.commons.services.notifications.restapi.vo.SubscriptionListItemVO;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.FOCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.notification.UsersSubscriptionManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * <h3>Description:</h3>
 * Test if the web service for notifications
 * <p>
 * Initial Date:  26 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotificationsTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(NotificationsTest.class);

	private static IdentityWithLogin id3;
	private static IdentityWithLogin userSubscriberId;
	private static IdentityWithLogin userAndForumSubscriberId;
	
	private static Forum forum;
	private static boolean setup = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private NotificationsManager notificationManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private UsersSubscriptionManager usersSubscriptionManager;
	
	@Before
	public void setUp() throws Exception {
		if(!setup) {
			userSubscriberId = JunitTestHelper.createAndPersistRndUser("rest-notifications-test-1");
			userAndForumSubscriberId = JunitTestHelper.createAndPersistRndUser("rest-notifications-test-2");
			id3 = JunitTestHelper.createAndPersistRndUser("rest-notifications-test-3");
			//for the news
			organisationService.addMember(userSubscriberId.getIdentity(), OrganisationRoles.usermanager);
			organisationService.addMember(userAndForumSubscriberId.getIdentity(), OrganisationRoles.usermanager);
			
			SubscriptionContext subContext = usersSubscriptionManager.getNewUsersSubscriptionContext();
			PublisherData publisherData = usersSubscriptionManager.getNewUsersPublisherData();
			if(!notificationManager.isSubscribed(userSubscriberId.getIdentity(), subContext)) {
				notificationManager.subscribe(userSubscriberId.getIdentity(), subContext, publisherData);
			}
			if(!notificationManager.isSubscribed(userAndForumSubscriberId.getIdentity(), subContext)) {
				notificationManager.subscribe(userAndForumSubscriberId.getIdentity(), subContext, publisherData);
			}
			
			//create a forum
			forum = forumManager.addAForum();
			Message m1 = createMessage(userSubscriberId.getIdentity(), forum);
			Assert.assertNotNull(m1);
			
			//subscribe
			SubscriptionContext forumSubContext = new SubscriptionContext("NotificationRestCourse", forum.getKey(), "2387");
			PublisherData forumPdata = new PublisherData(OresHelper.calculateTypeName(Forum.class), forum.getKey().toString(), "");
			if(!notificationManager.isSubscribed(userAndForumSubscriberId.getIdentity(), forumSubContext)) {
				notificationManager.subscribe(userAndForumSubscriberId.getIdentity(), forumSubContext, forumPdata);
			}
			notificationManager.markPublisherNews(forumSubContext, userSubscriberId.getIdentity(), true);

			//generate one notification
			JunitTestHelper.createAndPersistIdentityAsRndUser("rnd");
			setup = true;
		}
		
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void testGetNotifications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(userSubscriberId));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("notifications").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		
		assertNotNull(infos);
		assertFalse(infos.isEmpty());
		
		SubscriptionInfoVO infoVO = infos.get(0);
		assertNotNull(infoVO);
		assertNotNull(infoVO.getKey());
		assertNotNull("User", infoVO.getType());
		assertNotNull(infoVO.getTitle());
		assertNotNull(infoVO.getItems());
		assertFalse(infoVO.getItems().isEmpty());

		conn.shutdown();
	}
	
	@Test
	public void testGetUserNotifications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(userSubscriberId));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications").queryParam("type", "User");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		
		assertNotNull(infos);
		assertFalse(infos.isEmpty());
		
		SubscriptionInfoVO infoVO = infos.get(0);
		assertNotNull(infoVO);
		assertNotNull(infoVO.getKey());
		assertNotNull("User", infoVO.getType());
		assertNotNull(infoVO.getTitle());
		assertNotNull(infoVO.getItems());
		assertFalse(infoVO.getItems().isEmpty());

		conn.shutdown();
	}
	
	@Test
	public void testGetUserForumNotifications() throws URISyntaxException, IOException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(userAndForumSubscriberId));
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR, -2);
		String date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.S").format(cal.getTime());

		URI uri = conn.getContextURI().path("notifications").queryParam("date", date).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		assertNotNull(infos);
		assertTrue(2 <= infos.size());

		conn.shutdown();
	}
	
	@Test
	public void testGetUserForumNotificationsByType() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(userAndForumSubscriberId));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications").queryParam("type", "Forum");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		
		assertNotNull(infos);
		assertTrue(1 <= infos.size());
		
		SubscriptionInfoVO infoVO = infos.get(0);
		assertNotNull(infoVO);
		assertNotNull(infoVO.getKey());
		assertNotNull("Forum", infoVO.getType());
		assertNotNull(infoVO.getTitle());
		assertNotNull(infoVO.getItems());
		assertFalse(infoVO.getItems().isEmpty());

		conn.shutdown();
	}
	
	@Test
	public void testGetNoNotifications() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id3));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/notifications").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		
		assertNotNull(infos);
		assertTrue(infos.isEmpty());

		conn.shutdown();
	}
	
	@Test
	public void testGetBusinessGroupForumNotifications() throws IOException, URISyntaxException {
		//create a business group with forum notifications
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rest-not-4-");
		BusinessGroup group = businessGroupService.createBusinessGroup(id.getIdentity(), "Notifications 1", "REST forum notifications for group",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
		tools.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
		Forum groupForum = tools.getForum();
		dbInstance.commitAndCloseSession();
		
		//publish
		String businessPath = "[BusinessGroup:" + group.getKey() + "][toolforum:0]";
		SubscriptionContext forumSubContext = new SubscriptionContext("BusinessGroup", group.getKey(), "toolforum");
		PublisherData forumPdata =
				new PublisherData(OresHelper.calculateTypeName(Forum.class), groupForum.getKey().toString(), businessPath);
		notificationManager.subscribe(id.getIdentity(), forumSubContext, forumPdata);
		Message message = createMessage(id.getIdentity(), groupForum);
		notificationManager.markPublisherNews(forumSubContext, null, true);
		dbInstance.commitAndCloseSession();
		
		//get the notification
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		SubscriptionInfoVO infoVO = infos.get(0);
		Assert.assertNotNull(infoVO.getItems());
		Assert.assertEquals(1, infoVO.getItems().size());
		SubscriptionListItemVO itemVO = infoVO.getItems().get(0);
		Assert.assertNotNull(itemVO);
		Assert.assertEquals(group.getKey(), itemVO.getGroupKey());
		Assert.assertEquals(message.getKey(), itemVO.getMessageKey());
	}
	
	@Test
	public void testGetBusinessGroupFolderNotifications() throws IOException, URISyntaxException {
		//create a business group with folder notifications
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("rest-not-5-");
		BusinessGroup group = businessGroupService.createBusinessGroup(id.getIdentity(), "Notifications 2", "REST folder notifications for group",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
		tools.setToolEnabled(CollaborationTools.TOOL_FOLDER, true);
		String relPath = tools.getFolderRelPath();
		dbInstance.commitAndCloseSession();
		
		//publish
		String businessPath = "[BusinessGroup:" + group.getKey() + "][toolfolder:0]";
		SubscriptionContext folderSubContext = new SubscriptionContext("BusinessGroup", group.getKey(), "toolfolder");
		PublisherData folderPdata = new PublisherData("FolderModule", relPath, businessPath);
		notificationManager.subscribe(id.getIdentity(), folderSubContext, folderPdata);
		//add a file
		VFSContainer folder = tools.getSecuredFolder(group, folderSubContext, id.getIdentity(), true);
		String filename = addFile(folder);
		
		//mark as published
		notificationManager.markPublisherNews(folderSubContext, null, true);
		dbInstance.commitAndCloseSession();
		
		//get the notification
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		SubscriptionInfoVO infoVO = infos.get(0);
		Assert.assertNotNull(infoVO.getItems());
		Assert.assertEquals(1, infoVO.getItems().size());
		SubscriptionListItemVO itemVO = infoVO.getItems().get(0);
		Assert.assertNotNull(itemVO);
		Assert.assertEquals(group.getKey(), itemVO.getGroupKey());
		Assert.assertEquals("/" + filename, itemVO.getPath());
	}
	
	@Test
	public void testGetCourseForumNotifications() throws IOException, URISyntaxException {
		//create a course with a forum
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndAuthor("rest-not-6-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(id.getIdentity());
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.intermediateCommit();
		//create the forum
		CourseNode rootNode = course.getRunStructure().getRootNode();
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration("fo");
		FOCourseNode forumNode = (FOCourseNode)newNodeConfig.getInstance(rootNode);
		forumNode.setShortTitle("Forum");
		forumNode.setLearningObjectives("forum objectives");
		forumNode.setNoAccessExplanation("You don't have access");
		Forum courseForum = forumNode.loadOrCreateForum(course.getCourseEnvironment());
		course.getEditorTreeModel().addCourseNode(forumNode, rootNode);
		CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.published, true, false, id.getIdentity(), Locale.ENGLISH);
		dbInstance.intermediateCommit();
		
		//add message and publisher
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(course.getCourseEnvironment().getCourseGroupManager().getCourseResource(), true);
		String businessPath = "[RepositoryEntry:" + re.getKey() + "][CourseNode:" + forumNode.getIdent() + "]";
		SubscriptionContext forumSubContext = new SubscriptionContext("CourseModule", course.getResourceableId(), forumNode.getIdent());
		PublisherData forumPdata =
				new PublisherData(OresHelper.calculateTypeName(Forum.class), courseForum.getKey().toString(), businessPath);
		notificationManager.subscribe(id.getIdentity(), forumSubContext, forumPdata);
		Message message = createMessage(id.getIdentity(), courseForum);
		notificationManager.markPublisherNews(forumSubContext, null, true);
		dbInstance.commitAndCloseSession();
		
		//get the notification
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		SubscriptionInfoVO infoVO = infos.get(0);
		Assert.assertNotNull(infoVO.getItems());
		Assert.assertEquals(1, infoVO.getItems().size());
		SubscriptionListItemVO itemVO = infoVO.getItems().get(0);
		Assert.assertNotNull(itemVO);
		Assert.assertEquals(course.getResourceableId(), itemVO.getCourseKey());
		Assert.assertEquals(forumNode.getIdent(), itemVO.getCourseNodeId());
		Assert.assertEquals(message.getKey(), itemVO.getMessageKey());
	}
	
	@Test
	public void testGetCourseFolderNotifications() throws IOException, URISyntaxException {
		//create a course with a forum
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndAuthor("rest-not-7-");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(id.getIdentity());
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.intermediateCommit();
		//create the folder
		CourseNode rootNode = course.getRunStructure().getRootNode();
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration("bc");
		BCCourseNode folderNode = (BCCourseNode)newNodeConfig.getInstance(rootNode);
		folderNode.setShortTitle("Folder");
		folderNode.setLearningObjectives("folder objectives");
		folderNode.setNoAccessExplanation("You don't have access");
		String relPath = BCCourseNode.getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), folderNode);
		VFSContainer folder = BCCourseNode.getNodeFolderContainer(folderNode, course.getCourseEnvironment());
		course.getEditorTreeModel().addCourseNode(folderNode, rootNode);
		CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.published, true, false, id.getIdentity(), Locale.ENGLISH);
		dbInstance.intermediateCommit();
		
		//add message and publisher
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(course.getCourseEnvironment().getCourseGroupManager().getCourseResource(), true);
		String businessPath = "[RepositoryEntry:" + re.getKey() + "][CourseNode:" + folderNode.getIdent() + "]";
		SubscriptionContext folderSubContext = new SubscriptionContext("CourseModule", course.getResourceableId(), folderNode.getIdent());
		PublisherData folderPdata = new PublisherData("FolderModule", relPath, businessPath);
		notificationManager.subscribe(id.getIdentity(), folderSubContext, folderPdata);
		String filename = addFile(folder);
		notificationManager.markPublisherNews(folderSubContext, null, true);
		dbInstance.commitAndCloseSession();

		//get the notification
		RestConnection conn = new RestConnection();
		assertTrue(conn.login(id));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<SubscriptionInfoVO> infos = parseUserArray(response.getEntity());
		Assert.assertNotNull(infos);
		Assert.assertEquals(1, infos.size());
		SubscriptionInfoVO infoVO = infos.get(0);
		Assert.assertNotNull(infoVO.getItems());
		Assert.assertEquals(1, infoVO.getItems().size());
		SubscriptionListItemVO itemVO = infoVO.getItems().get(0);
		Assert.assertNotNull(itemVO);
		Assert.assertEquals(course.getResourceableId(), itemVO.getCourseKey());
		Assert.assertEquals(folderNode.getIdent(), itemVO.getCourseNodeId());
		Assert.assertEquals("/" + filename, itemVO.getPath());
	}
	
	@Test
	public void testGetPublisher() throws IOException, URISyntaxException {
		//create a business group with forum notifications
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-not-9");
		BusinessGroup group = businessGroupService.createBusinessGroup(id, "Notifications 1", "REST forum notifications for group",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
		tools.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
		Forum groupForum = tools.getForum();
		dbInstance.commitAndCloseSession();
		
		//publish
		String businessPath = "[BusinessGroup:" + group.getKey() + "][toolforum:0]";
		SubscriptionContext forumSubContext = new SubscriptionContext("BusinessGroup", group.getKey(), "toolforum");
		PublisherData forumPdata =
				new PublisherData(OresHelper.calculateTypeName(Forum.class), groupForum.getKey().toString(), businessPath);
		notificationManager.subscribe(id, forumSubContext, forumPdata);
		dbInstance.commitAndCloseSession();
		
		// GET publisher
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		UriBuilder request = UriBuilder.fromUri(getContextURI()).path("notifications/publisher/BusinessGroup/" + group.getKey() + "/toolforum");
		HttpGet method = conn.createGet(request.build(), MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		PublisherVO publisher = conn.parse(response, PublisherVO.class);
		Assert.assertNotNull(publisher);
		Assert.assertEquals("BusinessGroup", publisher.getResName());
		Assert.assertEquals(group.getKey(), publisher.getResId());
		Assert.assertEquals("toolforum", publisher.getSubidentifier());
		Assert.assertEquals("Forum", publisher.getType());
		Assert.assertEquals(groupForum.getKey().toString(), publisher.getData());
	}
	
	private String addFile(VFSContainer folder) throws IOException {
		String filename = UUID.randomUUID().toString();
		VFSLeaf file = folder.createChildLeaf(filename + ".jpg");
		try(OutputStream out = file.getOutputStream(true);
			InputStream in = UserMgmtTest.class.getResourceAsStream("portrait.jpg")) {
			IOUtils.copy(in, out);
		} catch(IOException e) {
			log.error("", e);
		}
		return file.getName();
	}
	
	private Message createMessage(Identity id, Forum fo) {
		Message m1 = forumManager.createMessage(fo, id, false);
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		forumManager.addTopMessage(m1);
		return m1;
	}
	
	protected List<SubscriptionInfoVO> parseUserArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<SubscriptionInfoVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
