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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.restapi.EventVO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.restapi.MessageVO;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.GroupConfigurationVO;
import org.olat.restapi.support.vo.GroupInfoVO;
import org.olat.restapi.support.vo.GroupLifecycleVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Description:<br>
 * Test the learning group web service
 * 
 * <P>
 * Initial Date:  7 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class GroupMgmtTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(GroupMgmtTest.class);
	
	private Identity owner1, owner2, owner3, part1, part2, part3;
	private BusinessGroup g1, g2;
	private BusinessGroup g3, g4;
	private OLATResource course;
	private Message m1, m2, m3, m4, m5;
	private RestConnection conn;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	/**
	 * Set up a course with learn group and group area
	 */
	@Before
	public void setUp() throws Exception {
		conn = new RestConnection();
		//create a course with learn group

		owner1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
		owner2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
		owner3 = JunitTestHelper.createAndPersistIdentityAsUser("rest-three");
		part1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-four");
		part2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-five");
		part3 = JunitTestHelper.createAndPersistIdentityAsUser("rest-six");
		
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		course = rm.findOrPersistResourceable(resourceable);
		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, "administrator", "-", "rest-re", null, course,
				RepositoryEntryStatusEnum.trash, defOrganisation);
		dbInstance.commit();
		assertNotNull(re);
		
		//create learn group
		// 1) context one: learning groups
		RepositoryEntry c1 =  JunitTestHelper.createAndPersistRepositoryEntry();
		// create groups without waiting list
		g1 = businessGroupService.createBusinessGroup(null, "rest-g1", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, c1);
		g2 = businessGroupService.createBusinessGroup(null, "rest-g2", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, c1);
		dbInstance.commit();
		//permission to see owners and participants
		businessGroupService.updateDisplayMembers(g1, false, false, false, false, false, false, false);
		businessGroupService.updateDisplayMembers(g2, true, true, false, false, false, false, false);
		
		// members g1
		businessGroupRelationDao.addRole(owner1, g1, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(owner2, g1, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(part1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(part2, g1, GroupRoles.participant.name());
    
		// members g2
		businessGroupRelationDao.addRole(owner1, g2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(part1, g2, GroupRoles.participant.name());
    
    
		// 2) context two: right groups
		RepositoryEntry c2 =  JunitTestHelper.createAndPersistRepositoryEntry();
		// groups
		g3 = businessGroupService.createBusinessGroup(null, "rest-g3", null, BusinessGroup.BUSINESS_TYPE, -1, -1, false, false, c2);
		g4 = businessGroupService.createBusinessGroup(null, "rest-g4", null, BusinessGroup.BUSINESS_TYPE, -1, -1, false, false, c2);
		dbInstance.commit();
		// members
		businessGroupRelationDao.addRole(owner1, g3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(owner2, g4, GroupRoles.participant.name());
    
		//3) collaboration tools
		CollaborationTools collabTools1 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g1);
		collabTools1.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
		collabTools1.setToolEnabled(CollaborationTools.TOOL_WIKI, true);
		collabTools1.setToolEnabled(CollaborationTools.TOOL_CALENDAR, true);
		collabTools1.saveNews("<p>Hello world</p>");
    
		try {
			collabTools1.createForumController(null, null, true, false, null, false);
		} catch (Exception e) {
			//will fail but generate the forum key
		}
		
		CollaborationTools collabTools2 = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g2);
		collabTools2.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
    
		dbInstance.closeSession(); // simulate user clicks
    
		//4) fill forum for g1
		NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(g1);
		Property forumKeyProperty = npm.findProperty(null, null, CollaborationTools.PROP_CAT_BG_COLLABTOOLS, CollaborationTools.KEY_FORUM);
		Forum forum = forumManager.loadForum(forumKeyProperty.getLongValue());
		
		m1 = forumManager.createMessage(forum, owner1, false);
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		forumManager.addTopMessage(m1);
		
		m2 = forumManager.createMessage(forum, owner2, false);
		m2.setTitle("Thread-2");
		m2.setBody("Body of Thread-2");
		forumManager.addTopMessage(m2);
		
		dbInstance.intermediateCommit();
		
		m3 = forumManager.createMessage(forum, owner3, false);
		m3.setTitle("Message-1.1");
		m3.setBody("Body of Message-1.1");
		forumManager.replyToMessage(m3, m1);
		
		m4 = forumManager.createMessage(forum, part1, false);
		m4.setTitle("Message-1.1.1");
		m4.setBody("Body of Message-1.1.1");
		forumManager.replyToMessage(m4, m3);
		
		m5 = forumManager.createMessage(forum, part2, false);
		m5.setTitle("Message-1.2");
		m5.setBody("Body of Message-1.2");
		forumManager.replyToMessage(m5, m1);

		dbInstance.intermediateCommit();
	}
	
	@After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
			e.printStackTrace();
			throw e;
		}
	}
	
	
	@Test
	public void testGetGroupsAdmin() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<GroupVO> groups = parseGroupArray(response.getEntity());
		assertNotNull(groups);
		assertTrue(groups.size() >= 4);//g1, g2, g3 and g4 + from olat
		
		Set<Long> keys = new HashSet<>();
		for(GroupVO vo:groups) {
			keys.add(vo.getKey());
		}

		assertTrue(keys.contains(g1.getKey()));
		assertTrue(keys.contains(g2.getKey()));
		assertTrue(keys.contains(g3.getKey()));
		assertTrue(keys.contains(g4.getKey()));
	}
	
	@Test
	public void testGetGroups() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-four", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<GroupVO> groups = parseGroupArray(response.getEntity());
		assertNotNull(groups);
		assertTrue(groups.size() >= 2);//g1, g2, g3 and g4 + from olat
		
		Set<Long> keys = new HashSet<>();
		for(GroupVO vo:groups) {
			keys.add(vo.getKey());
		}

		assertTrue(keys.contains(g1.getKey()));
		assertTrue(keys.contains(g2.getKey()));
		assertFalse(keys.contains(g3.getKey()));
		assertFalse(keys.contains(g4.getKey()));
	}
	
	@Test
	public void testGetGroupAdmin() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").path(g1.getKey().toString()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		GroupVO vo = conn.parse(response, GroupVO.class);
		assertNotNull(vo);
		assertEquals(vo.getKey(), g1.getKey());
	}
	
	@Test
	public void testGetGroupInfos() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/infos").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		GroupInfoVO vo = conn.parse(response, GroupInfoVO.class);
		assertNotNull(vo);
		assertEquals(Boolean.TRUE, vo.getHasWiki());
		assertEquals("<p>Hello world</p>", vo.getNews());
		assertNotNull(vo.getForumKey());
	}
	
	//the web service generate the forum key
	@Test
	public void testGetGroupInfos2() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g2.getKey() + "/infos").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		GroupInfoVO vo = conn.parse(response, GroupInfoVO.class);
		assertNotNull(vo);
		assertEquals(Boolean.FALSE, vo.getHasWiki());
		assertNull(vo.getNews());
		assertNotNull(vo.getForumKey());
	}
	
	@Test
	public void testGetThreads() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-one", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/forum/threads").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<MessageVO> messages = parseMessageArray(response.getEntity());
		
		assertNotNull(messages);
		assertEquals(2, messages.size());
	}
	
	@Test
	public void testGetMessages() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-one", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/forum/posts/" + m1.getKey()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<MessageVO> messages = parseMessageArray(response.getEntity());
		
		assertNotNull(messages);
		assertEquals(4, messages.size());
	}
	
	@Test
	public void testGetGroupCalendarEvents() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create an event
		EventVO event = new EventVO();
		Calendar cal = Calendar.getInstance();
		event.setBegin(cal.getTime());
		cal.add(Calendar.HOUR_OF_DAY, 1);
		event.setEnd(cal.getTime());
		String subject = UUID.randomUUID().toString();
		event.setSubject(subject);

		URI eventUri = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/calendar/event").build();
		HttpPost postEventMethod = conn.createPost(eventUri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(postEventMethod, event);
		HttpResponse postEventResponse = conn.execute(postEventMethod);
		assertEquals(200, postEventResponse.getStatusLine().getStatusCode());
		
		// Get the event
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/calendar/events").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<EventVO> vos = parseEventArray(response);
		assertNotNull(vos);
	}
	
	private List<EventVO> parseEventArray(HttpResponse response) {
		try(InputStream body = response.getEntity().getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<EventVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	@Test
	public void testUpdateCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		GroupVO vo = new GroupVO();
		vo.setKey(g1.getKey());
		vo.setName("rest-g1-mod");
		vo.setDescription("rest-g1 description");
		vo.setMinParticipants(g1.getMinParticipants());
		vo.setMaxParticipants(g1.getMaxParticipants());
		vo.setType("LearningGroup");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(response.getEntity());
		
		BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey());
		assertNotNull(bg);
		assertEquals(bg.getKey(), vo.getKey());
		assertEquals(bg.getName(), "rest-g1-mod");
		assertEquals(bg.getDescription(), "rest-g1 description");
	}
	
	@Test
	public void testCreateCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		GroupVO vo = new GroupVO();
		vo.setName("rest-g5-new");
		vo.setDescription("rest-g5 description");
		vo.setType("BuddyGroup");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);

		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		GroupVO newGroupVo = conn.parse(response, GroupVO.class); 
		assertNotNull(newGroupVo);

		BusinessGroup bg = businessGroupService.loadBusinessGroup(newGroupVo.getKey());
		assertNotNull(bg);
		assertEquals(bg.getKey(), newGroupVo.getKey());
		assertEquals(bg.getName(), "rest-g5-new");
		assertEquals(bg.getDescription(), "rest-g5 description");
	}
	
	@Test
	public void createCourseGroupWithConfiguration() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create the group
		GroupVO vo = new GroupVO();
		vo.setName("rest-g6-new");
		vo.setDescription("rest-g6 description");
		vo.setType("BuddyGroup");
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);

		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		GroupVO newGroupVo = conn.parse(response, GroupVO.class); 
		assertNotNull(newGroupVo);
		
		//update the configuration
		GroupConfigurationVO configVo = new GroupConfigurationVO();
		configVo.setTools(new String[]{ "hasFolder", "hasNews" });
		HashMap<String, Integer> toolsAccess = new HashMap<>();
		toolsAccess.put("hasFolder", Integer.valueOf(CollaborationTools.FOLDER_ACCESS_OWNERS));
		configVo.setToolsAccess(toolsAccess);
		configVo.setOwnersVisible(Boolean.TRUE);
		configVo.setParticipantsVisible(Boolean.FALSE);
		URI configRequest = UriBuilder.fromUri(getContextURI()).path("groups").path(newGroupVo.getKey().toString()).path("configuration").build();
		HttpPost configMethod = conn.createPost(configRequest, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(configMethod, configVo);
		HttpResponse configResponse = conn.execute(configMethod);
		assertTrue(configResponse.getStatusLine().getStatusCode() == 200 || configResponse.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(configResponse.getEntity());

		//check group

		BusinessGroup bg = businessGroupService.loadBusinessGroup(newGroupVo.getKey());
		assertNotNull(bg);
		assertEquals(bg.getKey(), newGroupVo.getKey());
		assertEquals(bg.getName(), "rest-g6-new");
		assertEquals(bg.getDescription(), "rest-g6 description");
		//check collaboration tools configuration
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getCollaborationToolsIfExists(bg);
		assertNotNull(tools);
		assertTrue(tools.isToolEnabled(CollaborationTools.TOOL_FOLDER));
		assertTrue(tools.isToolEnabled(CollaborationTools.TOOL_NEWS));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_CALENDAR));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_CHAT));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_CONTACT));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_FORUM));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_PORTFOLIO));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_WIKI));
		// Check collab tools access configuration
		assertTrue(tools.lookupFolderAccess().intValue() == CollaborationTools.FOLDER_ACCESS_OWNERS);		// modified
		assertNull(tools.lookupCalendarAccess()); 	// not explicitly initialized -> null
		//check display members
		assertTrue(bg.isOwnersVisibleIntern());
		assertFalse(bg.isParticipantsVisibleIntern());
		assertFalse(bg.isWaitingListVisibleIntern());
	}
	
	@Test
	public void createCourseGroupWithNewsAndContact() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create the group
		GroupVO vo = new GroupVO();
		vo.setName("rest-g7-news");
		vo.setDescription("rest-g7 with news");
		vo.setType("BuddyGroup");
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);

		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		GroupVO newGroupVo = conn.parse(response, GroupVO.class); 
		assertNotNull(newGroupVo);
		
		//update the configuration
		GroupConfigurationVO configVo = new GroupConfigurationVO();
		configVo.setTools(new String[]{ "hasContactForm", "hasNews" });
		configVo.setNews("<p>News!</p>");
		URI configRequest = UriBuilder.fromUri(getContextURI()).path("groups").path(newGroupVo.getKey().toString()).path("configuration").build();
		HttpPost configMethod = conn.createPost(configRequest, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(configMethod, configVo);
		HttpResponse configResponse = conn.execute(configMethod);
		assertTrue(configResponse.getStatusLine().getStatusCode() == 200 || configResponse.getStatusLine().getStatusCode() == 201);
		EntityUtils.consume(configResponse.getEntity());

		//check group
		BusinessGroup bg = businessGroupService.loadBusinessGroup(newGroupVo.getKey());
		assertNotNull(bg);
		assertEquals(bg.getKey(), newGroupVo.getKey());
		assertEquals(bg.getName(), "rest-g7-news");
		assertEquals(bg.getDescription(), "rest-g7 with news");
		//check collaboration tools configuration
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getCollaborationToolsIfExists(bg);
		assertNotNull(tools);
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_FOLDER));
		assertTrue(tools.isToolEnabled(CollaborationTools.TOOL_NEWS));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_CALENDAR));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_CHAT));
		assertTrue(tools.isToolEnabled(CollaborationTools.TOOL_CONTACT));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_FORUM));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_PORTFOLIO));
		assertFalse(tools.isToolEnabled(CollaborationTools.TOOL_WIKI));
		// Check news tools access configuration
		assertEquals("<p>News!</p>", tools.lookupNews());
	}
	
	@Test
	public void getGroupConfiguration() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-1");
		BusinessGroup group = businessGroupService.createBusinessGroup(owner, "Configuration", "REST configuration",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		dbInstance.commitAndCloseSession();
		
		URI configRequest = UriBuilder.fromUri(getContextURI()).path("groups").path(group.getKey().toString()).path("configuration").build();
		HttpGet configMethod = conn.createGet(configRequest, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(configMethod);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupConfigurationVO config = conn.parse(response.getEntity(), GroupConfigurationVO.class);
		Assert.assertNotNull(config);
		Assert.assertEquals(Boolean.FALSE, config.getOwnersPublic());
		Assert.assertEquals(Boolean.FALSE, config.getOwnersVisible());
		Assert.assertEquals(Boolean.FALSE, config.getParticipantsPublic());
		Assert.assertEquals(Boolean.FALSE, config.getParticipantsVisible());
		Assert.assertEquals(Boolean.FALSE, config.getWaitingListPublic());
		Assert.assertEquals(Boolean.FALSE, config.getWaitingListVisible());
		
		Assert.assertNotNull(config.getTools());
		Assert.assertEquals(0, config.getTools().length);
		Assert.assertNotNull(config.getToolsAccess());
		Assert.assertTrue(config.getToolsAccess().isEmpty());	
	}
		
	@Test
	public void getConfiguredGroupConfiguration() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("Coach-2");
		BusinessGroup group = businessGroupService.createBusinessGroup(owner, "Configuration", "REST configuration",
				BusinessGroup.BUSINESS_TYPE, null, null, false, false, null);
		dbInstance.commit();
		group = businessGroupService.updateDisplayMembers(group, true, false, false, true, true, false, false);
		dbInstance.commit();
	
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
		String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools();
		for(String tool:availableTools) {
			tools.setToolEnabled(tool, true);
		}
		tools.setToolAccess(CollaborationTools.TOOL_FOLDER, CollaborationTools.FOLDER_ACCESS_ALL);
		tools.setToolAccess(CollaborationTools.TOOL_CALENDAR, CollaborationTools.CALENDAR_ACCESS_OWNERS);
		dbInstance.commitAndCloseSession();
		
		URI configRequest = UriBuilder.fromUri(getContextURI()).path("groups").path(group.getKey().toString()).path("configuration").build();
		HttpGet configMethod = conn.createGet(configRequest, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(configMethod);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupConfigurationVO config = conn.parse(response.getEntity(), GroupConfigurationVO.class);
		Assert.assertNotNull(config);
		Assert.assertEquals(Boolean.TRUE, config.getOwnersPublic());
		Assert.assertEquals(Boolean.TRUE, config.getOwnersVisible());
		Assert.assertEquals(Boolean.TRUE, config.getParticipantsPublic());
		Assert.assertEquals(Boolean.FALSE, config.getParticipantsVisible());
		Assert.assertEquals(Boolean.FALSE, config.getWaitingListPublic());
		Assert.assertEquals(Boolean.FALSE, config.getWaitingListVisible());
		
		Assert.assertNotNull(config.getTools());
		Assert.assertEquals(availableTools.length, config.getTools().length);
		Assert.assertNotNull(config.getToolsAccess());
		Assert.assertEquals(2, config.getToolsAccess().size());	
		Assert.assertEquals(Integer.valueOf(CollaborationTools.FOLDER_ACCESS_ALL), config.getToolsAccess().get(CollaborationTools.TOOL_FOLDER));	
		Assert.assertEquals(Integer.valueOf(CollaborationTools.CALENDAR_ACCESS_OWNERS), config.getToolsAccess().get(CollaborationTools.TOOL_CALENDAR));	
		
	}
	
	@Test
	public void updateDeleteNews() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		//create the group
		GroupVO vo = new GroupVO();
		vo.setName("rest-g8-news");
		vo.setDescription("rest-g8 for news operations");
		vo.setType("BuddyGroup");
		URI request = UriBuilder.fromUri(getContextURI()).path("groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		GroupVO newGroupVo = conn.parse(response, GroupVO.class); 
		assertNotNull(newGroupVo);
		
		//update the configuration
		GroupConfigurationVO configVo = new GroupConfigurationVO();
		configVo.setTools(new String[]{ "hasNews" });
		configVo.setNews("<p>News!</p>");
		URI configRequest = UriBuilder.fromUri(getContextURI()).path("groups").path(newGroupVo.getKey().toString()).path("configuration").build();
		HttpPost configMethod = conn.createPost(configRequest, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(configMethod, configVo);
		HttpResponse configResponse = conn.execute(configMethod);
		assertEquals(200, configResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(configResponse.getEntity());

		//update the news an contact node
		URI newsRequest = UriBuilder.fromUri(getContextURI()).path("groups").path(newGroupVo.getKey().toString()).path("news").build();
		HttpPost updateNewsMethod = conn.createPost(newsRequest, MediaType.APPLICATION_JSON);
		conn.addEntity(updateNewsMethod, new BasicNameValuePair("news", "<p>The last news</p>"));
		HttpResponse updateResponse = conn.execute(updateNewsMethod);
		assertEquals(200, updateResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(updateResponse.getEntity());
		
		//check the last news
		BusinessGroup bg = businessGroupService.loadBusinessGroup(newGroupVo.getKey());
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		String news = collabTools.lookupNews();
		assertEquals("<p>The last news</p>", news);
		
		//delete the news
		HttpDelete deleteNewsMethod = conn.createDelete(newsRequest, MediaType.APPLICATION_JSON);
		HttpResponse deleteResponse = conn.execute(deleteNewsMethod);
		assertEquals(200, deleteResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(deleteResponse.getEntity());
		
		// reload and check the news are empty
		dbInstance.commitAndCloseSession();
		CollaborationTools reloadedCollabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
		String deletedNews = reloadedCollabTools.lookupNews();
		assertNull(deletedNews);
	}
	
	@Test
	public void getGroupStatus() throws IOException, URISyntaxException {
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "rest-g10", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + businessGroup.getKey() + "/status").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupLifecycleVO lifecycleVo = conn.parse(response, GroupLifecycleVO.class);
		Assert.assertNotNull(lifecycleVo);
		Assert.assertEquals(BusinessGroupStatusEnum.active.name(), lifecycleVo.getStatus());
	}
	
	@Test
	public void getGroupDeletedStatus() throws IOException, URISyntaxException {
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(owner1, "rest-g10", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();
		businessGroupLifecycleManager.deleteBusinessGroupSoftly(businessGroup, owner1, false);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + businessGroup.getKey() + "/status").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupLifecycleVO lifecycleVo = conn.parse(response, GroupLifecycleVO.class);
		Assert.assertNotNull(lifecycleVo);
		Assert.assertEquals(BusinessGroupStatusEnum.trash.name(), lifecycleVo.getStatus());
	}
	
	@Test
	public void updateGroupStatus() throws IOException, URISyntaxException {
		BusinessGroup businessGroup = businessGroupService.createBusinessGroup(null, "rest-g11", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + businessGroup.getKey() + "/status").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addEntity(method, new BasicNameValuePair("newStatus", "inactive"));
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupLifecycleVO lifecycleVo = conn.parse(response, GroupLifecycleVO.class);
		Assert.assertNotNull(lifecycleVo);
		Assert.assertEquals(BusinessGroupStatusEnum.inactive.name(), lifecycleVo.getStatus());
	}
	
	@Test
	public void testDeleteCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey());
		assertNull(bg);
	}
	
	@Test
	public void testGetParticipantsAdmin() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/participants").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> participants = parseUserArray(response.getEntity());
		assertNotNull(participants);
		assertEquals(participants.size(), 2);
		
		Long idKey1 = null;
		Long idKey2 = null;
		for(UserVO participant:participants) {
			if(participant.getKey().equals(part1.getKey())) {
				idKey1 = part1.getKey();
			} else if(participant.getKey().equals(part2.getKey())) {
				idKey2 = part2.getKey();
			}
		}
		assertNotNull(idKey1);
		assertNotNull(idKey2);
	}
	
	@Test
	public void testGetParticipants() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-four", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/participants").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		//g1 not authorized
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testGetOwnersAdmin() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/owners").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> owners = parseUserArray(response.getEntity());
		assertNotNull(owners);
		assertEquals(owners.size(), 2);
		
		Long idKey1 = null;
		Long idKey2 = null;
		for(UserVO participant:owners) {
			if(participant.getKey().equals(owner1.getKey())) {
				idKey1 = owner1.getKey();
			} else if(participant.getKey().equals(owner2.getKey())) {
				idKey2 = owner2.getKey();
			}
		}
		assertNotNull(idKey1);
		assertNotNull(idKey2);
	}
	
	@Test
	public void testGetOwners() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-four", "A6B7C8"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/owners").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		//not authorized
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testAddParticipant() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/participants/" + part3.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		List<Identity> participants = businessGroupService.getMembers(g1, GroupRoles.participant.name());
		boolean found = false;
		for(Identity participant:participants) {
			if(participant.getKey().equals(part3.getKey())) {
				found = true;
			}
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testRemoveParticipant() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/participants/" + part2.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		

		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<Identity> participants = businessGroupService.getMembers(g1, GroupRoles.participant.name());
		boolean found = false;
		for(Identity participant:participants) {
			if(participant.getKey().equals(part2.getKey())) {
				found = true;
			}
		}
		
		assertFalse(found);
	}
	
	@Test
	public void testAddTutor() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/owners/" + owner3.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
		List<Identity> owners = businessGroupRelationDao.getMembers(g1, GroupRoles.coach.name());
		boolean found = false;
		for(Identity owner:owners) {
			if(owner.getKey().equals(owner3.getKey())) {
				found = true;
			}
		}
		
		assertTrue(found);
	}
	
	@Test
	public void testRemoveTutor() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/groups/" + g1.getKey() + "/owners/" + owner2.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);

		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<Identity> owners = businessGroupRelationDao.getMembers(g1, GroupRoles.coach.name());
		boolean found = false;
		for(Identity owner:owners) {
			if(owner.getKey().equals(owner2.getKey())) {
				found = true;
			}
		}
		
		assertFalse(found);
	}
	
	protected List<UserVO> parseUserArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<GroupVO> parseGroupArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<GroupVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<MessageVO> parseMessageArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<MessageVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
