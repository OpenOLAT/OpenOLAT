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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.modules.bc.BriefcaseWebDAVProvider;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.httpclient.ConnectionUtilities;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.restapi.ForumVO;
import org.olat.modules.fo.restapi.ForumVOes;
import org.olat.modules.fo.restapi.MessageVOes;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.FileVO;
import org.olat.restapi.support.vo.FolderVO;
import org.olat.restapi.support.vo.FolderVOes;
import org.olat.restapi.support.vo.GroupInfoVOes;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.restapi.support.vo.GroupVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.PortraitSize;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.olat.user.propertyhandlers.ui.UsrPropCfgManager;
import org.olat.user.restapi.ManagedUserVO;
import org.olat.user.restapi.PreferencesVO;
import org.olat.user.restapi.RolesVO;
import org.olat.user.restapi.StatusVO;
import org.olat.user.restapi.UserLifecycleVO;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Test the <code>UserWebservice</code>
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class UserMgmtTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(UserMgmtTest.class);
	
	private static IdentityWithLogin id1;
	private static IdentityWithLogin id2;
	private static IdentityWithLogin id3;
	private static IdentityWithLogin owner1;
	private static BusinessGroup g1, g2, g3, g4;
	private static String g1externalId, g3ExternalId;
	
	private static ICourse demoCourse;
	private static FOCourseNode demoForumNode;
	private static BCCourseNode demoBCCourseNode;
	
	private static boolean setuped = false;

	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private HomePageConfigManager homePageConfigManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UsrPropCfgManager usrPropCfgManager;
	@Autowired
	private UserPortraitService userPortraitService;
	
	@Before
	public void setUp() throws Exception {
		if(setuped) return;
		
		usrPropCfgManager.resetToSystemDefault(JunitTestHelper.getDefaultActor());
		waitMessageAreConsumed();
		
		//create identities
		owner1 = JunitTestHelper.createAndPersistRndUser("user-rest-zero");
		assertNotNull(owner1);
		id1 = JunitTestHelper.createAndPersistRndUser("user-rest-one");
		id2 = JunitTestHelper.createAndPersistRndUser("user-rest-two");
		dbInstance.intermediateCommit();
		id2.getUser().setProperty("telMobile", "39847592");
		id2.getUser().setProperty("gender", "female");
		id2.getUser().setProperty("birthDay", "20091212");
		dbInstance.updateObject(id2.getUser());
		dbInstance.intermediateCommit();
		
		id3 = JunitTestHelper.createAndPersistRndUser("user-rest-three");
		VFSContainer id3HomeFolder = VFSManager.olatRootContainer(FolderConfig.getUserHome(id3.getIdentity()), null);
		VFSContainer id3PublicFolder = (VFSContainer)id3HomeFolder.resolve("public");
		if(id3PublicFolder == null) {
			id3PublicFolder = id3HomeFolder.createChildContainer("public");
		}
		VFSItem portrait = id3PublicFolder.resolve("portrait.jpg");
		if(portrait == null) {
			URL portraitUrl = CoursesElementsTest.class.getResource("portrait.jpg");
			File ioPortrait = new File(portraitUrl.toURI());
			FileUtils.copyFileToDirectory(ioPortrait, ((LocalImpl)id3PublicFolder).getBasefile(), false);
		}

		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		OLATResource course =  rm.createOLATResourceInstance(resourceable);
		dbInstance.saveObject(course);
		dbInstance.intermediateCommit();
		
		//create learn group	
		// 1) context one: learning groups
		RepositoryEntry c1 = JunitTestHelper.createAndPersistRepositoryEntry();
		// create groups without waiting list
		g1externalId = UUID.randomUUID().toString();
		g1 = businessGroupService.createBusinessGroup(null, "user-rest-g1", null, BusinessGroup.BUSINESS_TYPE, g1externalId, "all", 0, 10, false, false, c1);
		g2 = businessGroupService.createBusinessGroup(null, "user-rest-g2", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, c1);
		// members g1
		businessGroupRelationDao.addRole(id1.getIdentity(), g1, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id2.getIdentity(), g1, GroupRoles.participant.name());
		// members g2
		businessGroupRelationDao.addRole(id2.getIdentity(), g2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id1.getIdentity(), g2, GroupRoles.participant.name());

		// 2) context two: right groups
		RepositoryEntry c2 = JunitTestHelper.createAndPersistRepositoryEntry();
		// groups
		g3ExternalId = UUID.randomUUID().toString();
		g3 = businessGroupService.createBusinessGroup(null, "user-rest-g3", null, BusinessGroup.BUSINESS_TYPE, g3ExternalId, "all", -1, -1, false, false, c2);
		g4 = businessGroupService.createBusinessGroup(null, "user-rest-g4", null, BusinessGroup.BUSINESS_TYPE, -1, -1, false, false, c2);
		// members
		businessGroupRelationDao.addRole(id1.getIdentity(), g3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2.getIdentity(), g4, GroupRoles.participant.name());
		dbInstance.closeSession();
		
		//add some collaboration tools
		CollaborationTools g1CTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g1);
		g1CTSMngr.setToolEnabled(CollaborationTools.TOOL_FORUM, true);
		Forum g1Forum = g1CTSMngr.getForum();//create the forum
		Message m1 = forumManager.createMessage(g1Forum, id1.getIdentity(), false);
		m1.setTitle("Thread-1");
		m1.setBody("Body of Thread-1");
		forumManager.addTopMessage(m1);
		
		dbInstance.commitAndCloseSession();
		
		//add some folder tool
		CollaborationTools g2CTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(g2);
		g2CTSMngr.setToolEnabled(CollaborationTools.TOOL_FOLDER, true);
		LocalFolderImpl g2Folder = VFSManager.olatRootContainer(g2CTSMngr.getFolderRelPath(), null);
		g2Folder.getBasefile().mkdirs();
		VFSItem groupPortrait = g2Folder.resolve("portrait.jpg");
		if(groupPortrait == null) {
			URL portraitUrl = UserMgmtTest.class.getResource("portrait.jpg");
			File ioPortrait = new File(portraitUrl.toURI());
			FileUtils.copyFileToDirectory(ioPortrait, g2Folder.getBasefile(), false);
		}
		
		dbInstance.commitAndCloseSession();
		
		//prepare some courses
		Identity author = JunitTestHelper.createAndPersistIdentityAsUser("auth-" + UUID.randomUUID().toString());
		RepositoryEntry entry = JunitTestHelper.deployDemoCourse(author);
		if (!repositoryService.hasRole(id1.getIdentity(), entry, GroupRoles.participant.name())){
			repositoryService.addRole(id1.getIdentity(), entry, GroupRoles.participant.name());
		}
		
		demoCourse = CourseFactory.loadCourse(entry);
		TreeVisitor visitor = new TreeVisitor(new Visitor() {
			@Override
			public void visit(INode node) {
				if(node instanceof FOCourseNode) {
					if(demoForumNode == null) {
						demoForumNode = (FOCourseNode)node;
						Forum courseForum = demoForumNode.loadOrCreateForum(demoCourse.getCourseEnvironment());
						Message message1 = forumManager.createMessage(courseForum, id1.getIdentity(), false);
						message1.setTitle("Thread-1");
						message1.setBody("Body of Thread-1");
						forumManager.addTopMessage(message1);
					}	
				} else if (node instanceof BCCourseNode) {
					if(demoBCCourseNode == null) {
						demoBCCourseNode = (BCCourseNode)node;
						VFSContainer container = BCCourseNode.getNodeFolderContainer(demoBCCourseNode, demoCourse.getCourseEnvironment());
						VFSItem example = container.resolve("singlepage.html");
						if(example == null) {
							try(InputStream htmlUrl = UserMgmtTest.class.getResourceAsStream("singlepage.html")) {
								VFSLeaf htmlLeaf = container.createChildLeaf("singlepage.html");
								IOUtils.copy(htmlUrl, htmlLeaf.getOutputStream(false));
							} catch (IOException e) {
								log.error("", e);
							}
						}
					}
				}
			}
		}, demoCourse.getRunStructure().getRootNode(), false);
		visitor.visitAll();

		dbInstance.commitAndCloseSession();
		setuped = true;
	}

	@Test
	public void testGetUsers() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
		Assert.assertNotNull(vos);
		Assert.assertFalse(vos.isEmpty());
		int voSize = vos.size();
		vos = null;
		
		List<Identity> identities = securityManager
				.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);
		Assert.assertEquals(voSize, identities.size());
		conn.close();

	}
	
	@Test
	public void testFindUsersByLogin() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("login","administrator")
				.queryParam("authProvider","OLAT").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
		
		String[] authProviders = new String[]{"OLAT"};
		List<Identity> identities = securityManager
				.getIdentitiesByPowerSearch("administrator", null, true, null, authProviders, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);

		Assert.assertNotNull(vos);
		Assert.assertFalse(vos.isEmpty());
		Assert.assertEquals(vos.size(), identities.size());
		conn.close();
	}
	
	@Test
	public void testFindUsersByLogin_notFuzzy() throws IOException, URISyntaxException, InterruptedException {
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-rest");
		Assert.assertNotNull(id);

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("login", id.getLogin()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertEquals(1, vos.size());
		Assert.assertEquals(id.getIdentity().getKey(), vos.get(0).getKey());
		Assert.assertNull(vos.get(0).getLogin());
		conn.close();
	}
	
	@Test
	public void testFindUsersByLogin_manualIdentityName() throws IOException, URISyntaxException, InterruptedException {
		securityModule.setIdentityName("manual");
		
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("u-rest-manual");
		Assert.assertNotNull(id);

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("login", id.getLogin()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertEquals(1, vos.size());
		Assert.assertEquals(id.getIdentity().getKey(), vos.get(0).getKey());
		Assert.assertEquals(id.getIdentity().getName(), vos.get(0).getLogin());
		
		securityModule.setIdentityName("auto");
		waitMessageAreConsumed();
		conn.close();
	}
	
	@Test
	public void testFindUsersByExternalId() throws IOException, URISyntaxException, InterruptedException {
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-external-id");
		Assert.assertNotNull(id);
		
		String externalId = UUID.randomUUID().toString();
		Identity identity = securityManager.setExternalId(id.getIdentity(), externalId);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("externalId", externalId)
				.queryParam("statusVisibleLimit", "all")
				.build();

		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertEquals(1, vos.size());
		Assert.assertEquals(identity.getKey(), vos.get(0).getKey());
		Assert.assertEquals(Identity.STATUS_ACTIV, vos.get(0).getStatus());
		Assert.assertFalse(vos.get(0).getProperties().isEmpty());
		Assert.assertNull(vos.get(0).getLogin());
		conn.close();
	}
	
	/**
	 * This make sure that inactive user are returned. Really important for
	 * synchronization operations.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testFindInactiveUsersByExternalId() throws IOException, URISyntaxException, InterruptedException {
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-external-inactive-id");
		Assert.assertNotNull(id);
		String externalId = UUID.randomUUID().toString();
		Identity identity = securityManager.setExternalId(id.getIdentity(), externalId);
		dbInstance.commit();
		identity = securityManager.saveIdentityStatus(identity, Identity.STATUS_INACTIVE, identity);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("externalId", externalId)
				.queryParam("statusVisibleLimit", "all")
				.build();

		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertEquals(1, vos.size());
		Assert.assertEquals(identity.getKey(), vos.get(0).getKey());
		Assert.assertNull(vos.get(0).getLogin());
		conn.close();
	}
	
	
	/**
	 * This make sure that inactive user are returned. Really important for
	 * synchronization operations.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testFindInactiveUsers() throws IOException, URISyntaxException, InterruptedException {
		//there is user-rest-...
		IdentityWithLogin inactiveId = JunitTestHelper.createAndPersistRndUser("user-inactive-1-id");
		IdentityWithLogin loginDeniedId = JunitTestHelper.createAndPersistRndUser("user-inactive-2-id");
		Identity inactiveIdentity = inactiveId.getIdentity();
		Identity loginDeniedIdentity = loginDeniedId.getIdentity();
		dbInstance.commit();
		inactiveIdentity = securityManager.saveIdentityStatus(inactiveIdentity, Identity.STATUS_INACTIVE, inactiveIdentity);
		loginDeniedIdentity = securityManager.saveIdentityStatus(loginDeniedIdentity, Identity.STATUS_LOGIN_DENIED, loginDeniedIdentity);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("status", Integer.toString(Identity.STATUS_INACTIVE))
				.build();

		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
		
		Assertions.assertThat(vos)
			.map(UserVO::getKey)
			.contains(inactiveIdentity.getKey())
			.doesNotContain(loginDeniedIdentity.getKey());
		conn.close();

	}
	
	@Test
	public void testFindUsersByAuthusername() throws IOException, URISyntaxException, InterruptedException {
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-auth-name");
		Assert.assertNotNull(id);

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("authProvider", "OLAT")
				.queryParam("authUsername", id.getLogin())
				.queryParam("statusVisibleLimit", "all")
				.build();

		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertEquals(1, vos.size());
		Assert.assertEquals(id.getKey(), vos.get(0).getKey());
		Assert.assertNull(vos.get(0).getLogin());
		conn.close();
	}
	
	@Test
	public void testFindUsersByAuthusernameShib() throws IOException, URISyntaxException, InterruptedException {
		// Make sure, we test explicitly if the returned login name is null
		securityModule.setIdentityName("auto");
		waitMessageAreConsumed();
		
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-auth-name");
		Assert.assertNotNull(id);
		String shibIdent = UUID.randomUUID().toString();
		securityManager.createAndPersistAuthentication(id.getIdentity(), "Shib", BaseSecurity.DEFAULT_ISSUER, null, shibIdent, null, null);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("authProvider", "Shib")
				.queryParam("authUsername", shibIdent)
				.queryParam("statusVisibleLimit", "all")
				.build();

		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertEquals(1, vos.size());
		Assert.assertEquals(id.getKey(), vos.get(0).getKey());
		Assert.assertNull(vos.get(0).getLogin());
		
		// false check
		URI negativeRequest = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("authProvider", "OLAT")
				.queryParam("authUsername", shibIdent)
				.queryParam("statusVisibleLimit", "all")
				.build();

		HttpRequest negativeMethod = conn.createGet(negativeRequest, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> negativeResponse = conn.execute(negativeMethod);
		Assert.assertEquals(200, negativeResponse.statusCode());
		List<UserVO> negativeVos = conn.parseList(negativeResponse, UserVO.class);

		Assert.assertNotNull(negativeVos);
		Assert.assertTrue(negativeVos.isEmpty());
		conn.close();
	}
	
	@Test
	public void testFindUsersByExternalId_negatif() throws IOException, URISyntaxException, InterruptedException {
		//there is user-rest-...
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-external-id-2");
		Assert.assertNotNull(id);
		String externalId = UUID.randomUUID().toString();
		Identity identity = securityManager.setExternalId(id.getIdentity(), externalId);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(identity);

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("externalId", "a-non-existing-external-key")
				.queryParam("statusVisibleLimit", "all")
				.build();

		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);

		Assert.assertNotNull(vos);
		Assert.assertTrue(vos.isEmpty());
		conn.close();
	}
	
	@Test
	public void testFindUsersByProperty() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("telMobile","39847592")
				.queryParam("gender","Female")
				.queryParam("birthDay", "12/12/2009").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
	
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		conn.close();
	}
	
	@Test
	public void testFindAdminByAuth() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("authUsername","administrator")
				.queryParam("authProvider","OLAT").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
	
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(1, vos.size());
		Identity admin = securityManager.findIdentityByLogin("administrator");
		assertEquals(admin.getKey(), vos.get(0).getKey());
		conn.close();
	}
	
	@Test
	public void findByUsername() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("username")
				.queryParam("username","administrator").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		UserVO userVo = conn.parse(response, UserVO.class);
		
		Assert.assertNotNull(userVo);
		conn.close();
	}
	
	@Test
	public void findByUsernameCheckProperties() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		IdentityWithLogin meLogin = JunitTestHelper.createAndPersistRndUser("rest-users");
		Identity me = meLogin.getIdentity();
		me.getUser().setProperty("telMobile", "92737");
		me.getUser().setProperty("telOffice", "92737");
		me.getUser().setProperty("telPrivate", "92737");
		me.getUser().setProperty("gender", "female");
		me.getUser().setProperty("birthDay", "19980405");
		userManager.updateUserFromIdentity(me);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("username")
				.queryParam("username", meLogin.getLogin()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		UserVO userVo = conn.parse(response, UserVO.class);
		
		Assert.assertNotNull(userVo);
		Assert.assertEquals("92737", userVo.getProperty("telPrivate"));
		conn.close();
	}
	
	@Test
	public void getUserLifecycle() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		IdentityWithLogin identityLogin = JunitTestHelper.createAndPersistRndUser("rest-life-user-1");
		Identity identity  = identityLogin.getIdentity();
		dbInstance.commit();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("lifecycle").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		UserLifecycleVO lifecycleVo = conn.parse(response, UserLifecycleVO.class);
		
		Assert.assertEquals(identity.getKey(), lifecycleVo.getKey());
		Assert.assertEquals(Integer.valueOf(Identity.STATUS_ACTIV), lifecycleVo.getStatus());
		Assert.assertNotNull(lifecycleVo.getCreationDate());
		Assert.assertNull(lifecycleVo.getExpirationDate());
		Assert.assertNull(lifecycleVo.getInactivationDate());
		conn.close();

	}
	
	@Test
	public void updateUserLifecycle() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		IdentityWithLogin identityLogin = JunitTestHelper.createAndPersistRndUser("rest-life-user-2");
		Identity identity  = identityLogin.getIdentity();
		dbInstance.commit();
		
		UserLifecycleVO lifecycleVo = new  UserLifecycleVO();
		lifecycleVo.setKey(identity.getKey());
		lifecycleVo.setExpirationDate(DateUtils.addDays(new Date(), 12));
		
		URI updateRequest = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("lifecycle").build();
		HttpRequest updateMethod = conn.createPost(updateRequest, lifecycleVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(updateMethod);
		assertEquals(200, response.statusCode());
		
		UserLifecycleVO updatedLifecycleVo = conn.parse(response, UserLifecycleVO.class);
		Assert.assertEquals(identity.getKey(), updatedLifecycleVo.getKey());
		Assert.assertEquals(Integer.valueOf(Identity.STATUS_ACTIV), updatedLifecycleVo.getStatus());
		Assert.assertNotNull(updatedLifecycleVo.getCreationDate());
		Assert.assertNotNull(updatedLifecycleVo.getExpirationDate());
		Assert.assertNull(updatedLifecycleVo.getInactivationDate());
		conn.close();

	}
	
	@Test
	public void reactivateUserLifecycle() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		// Create an inactive user
		IdentityWithLogin identityLogin = JunitTestHelper.createAndPersistRndUser("rest-life-user-2");
		Identity identity = identityLogin.getIdentity();
		((IdentityImpl)identity).setStatus(Identity.STATUS_INACTIVE);
		Date now = new Date();
		((IdentityImpl)identity).setInactivationDate(now);
		((IdentityImpl)identity).setInactivationEmailDate(now);
		((IdentityImpl)identity).setReactivationDate(now);
		((IdentityImpl)identity).setExpirationDate(DateUtils.addDays(now, -5));
		((IdentityImpl)identity).setExpirationEmailDate(DateUtils.addDays(now, -5));
		identity = dbInstance.getCurrentEntityManager().merge(identity);
		dbInstance.commitAndCloseSession();
		
		UserLifecycleVO lifecycleVo = new  UserLifecycleVO();
		lifecycleVo.setKey(identity.getKey());
		lifecycleVo.setExpirationDate(null);
		lifecycleVo.setInactivationDate(null);
		lifecycleVo.setStatus(Identity.STATUS_ACTIV);

		URI updateRequest = UriBuilder.fromUri(getContextURI()).path("users").path(identity.getKey().toString())
				.path("lifecycle").build();
		HttpRequest updateMethod = conn.createPost(updateRequest, lifecycleVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(updateMethod);
		assertEquals(200, response.statusCode());
		
		UserLifecycleVO updatedLifecycleVo = conn.parse(response, UserLifecycleVO.class);
		Assert.assertEquals(identity.getKey(), updatedLifecycleVo.getKey());
		Assert.assertEquals(Integer.valueOf(Identity.STATUS_ACTIV), updatedLifecycleVo.getStatus());
		Assert.assertNotNull(updatedLifecycleVo.getCreationDate());
		Assert.assertNull(updatedLifecycleVo.getExpirationDate());
		Assert.assertNull(updatedLifecycleVo.getInactivationDate());
		conn.close();

	}
	
	@Test
	public void testGetMe() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin meLogin = JunitTestHelper.createAndPersistRndUser("rest-users");
		Identity me = meLogin.getIdentity();
		me.getUser().setProperty("telMobile", "39847593");
		me.getUser().setProperty("telOffice", "39847593");
		me.getUser().setProperty("telPrivate", "39847593");
		me.getUser().setProperty("gender", "female");
		me.getUser().setProperty("birthDay", "20040405");
		dbInstance.updateObject(me.getUser());
		dbInstance.commit();
		
		HomePageConfig hpc = homePageConfigManager.loadConfigFor(me);
		hpc.setEnabled("telOffice", true);
		hpc.setEnabled("telMobile", true);
		hpc.setEnabled("birthDay", true);
		homePageConfigManager.saveConfigTo(me, hpc);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(meLogin.getLogin(), meLogin.getPassword());
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/me").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		UserVO vo = conn.parse(response, UserVO.class);

		assertNotNull(vo);
		assertEquals(vo.getKey(), me.getKey());
		//are the properties there?
		assertFalse(vo.getProperties().isEmpty());
		conn.close();
	}
	
	
	@Test
	public void testGetUser() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		UserVO vo = conn.parse(response, UserVO.class);

		Assert.assertNotNull(vo);
		Assert.assertEquals(vo.getKey(), id1.getKey());
		Assert.assertEquals(Identity.STATUS_ACTIV, vo.getStatus());
		//are the properties there?
		Assert.assertFalse(vo.getProperties().isEmpty());
		conn.close();
	}
	
	@Test
	public void getUserLastModified() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("user-lastmod-1");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(id.getKey().toString()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		UserVO vo = conn.parse(response, UserVO.class);
		conn.close();
		
		User user = userManager.loadUserByKey(id.getIdentity().getUser().getKey());
		Assert.assertNotNull(vo.getLastModified());
		Assert.assertEquals(user.getLastModified().getTime(), vo.getLastModified().getTime());
	}
	
	@Test
	public void getUsersModifiedSince() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin changedId = JunitTestHelper.createAndPersistRndUser("user-lastmod-2");
		IdentityWithLogin inactiveId = JunitTestHelper.createAndPersistRndUser("user-lastmod-3");
		IdentityWithLogin unchangedId = JunitTestHelper.createAndPersistRndUser("user-lastmod-4");
		dbInstance.commitAndCloseSession();
		
		Date before = DateUtils.addDays(new Date(), -2);
		JunitTestHelper.setUserLastModified(changedId.getIdentity(), before);
		JunitTestHelper.setUserLastModified(inactiveId.getIdentity(), before);
		JunitTestHelper.setUserLastModified(unchangedId.getIdentity(), before);
		
		Identity changedIdentity = securityManager.loadIdentityByKey(changedId.getKey());
		changedIdentity.getUser().setProperty(UserConstants.FIRSTNAME, "Modified");
		userManager.updateUserFromIdentity(changedIdentity);
		securityManager.saveIdentityStatus(inactiveId.getIdentity(), Identity.STATUS_INACTIVE, inactiveId.getIdentity());
		dbInstance.commitAndCloseSession();
		
		Date modifiedSince = DateUtils.addDays(new Date(), -1);
		RestConnection conn = new RestConnection("administrator", "openolat");
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("modifiedSince", modifiedSince.toInstant().toString())
				.queryParam("statusVisibleLimit", "all")
				.build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
		conn.close();
		
		Assertions.assertThat(vos)
			.map(UserVO::getKey)
			.contains(changedId.getKey(), inactiveId.getKey())
			.doesNotContain(unchangedId.getKey());
		
		UserVO changedVo = vos.stream()
				.filter(vo -> changedId.getKey().equals(vo.getKey()))
				.findFirst().orElse(null);
		Assert.assertNotNull(changedVo);
		Assert.assertEquals("Modified", changedVo.getFirstName());
		Assert.assertFalse(changedVo.getProperties().isEmpty());
		Assertions.assertThat(changedVo.getLastModified()).isAfter(modifiedSince);
	}
	
	@Test
	public void getUsersModifiedSinceLocalFormat() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin changedId = JunitTestHelper.createAndPersistRndUser("user-lastmod-5");
		IdentityWithLogin unchangedId = JunitTestHelper.createAndPersistRndUser("user-lastmod-6");
		dbInstance.commitAndCloseSession();
		JunitTestHelper.setUserLastModified(unchangedId.getIdentity(), DateUtils.addDays(new Date(), -2));
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("modifiedSince", ObjectFactory.formatDate(DateUtils.addDays(new Date(), -1)))
				.build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		List<UserVO> vos = conn.parseList(response, UserVO.class);
		conn.close();
		
		Assertions.assertThat(vos)
			.map(UserVO::getKey)
			.contains(changedId.getKey())
			.doesNotContain(unchangedId.getKey());
	}
	
	@Test
	public void getUsersModifiedSinceInvalid() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		URI request = UriBuilder.fromUri(getContextURI()).path("users")
				.queryParam("modifiedSince", "yesterday")
				.build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(400, response.statusCode());
		conn.close();
	}
	
	@Test
	public void createUserIgnoresLastModified() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		String username = UUID.randomUUID().toString();
		UserVO vo = new UserVO();
		vo.setLogin(username);
		vo.setFirstName("Lucie");
		vo.setLastName("Modified");
		vo.setEmail(username + "@frentix.com");
		vo.setLastModified(DateUtils.addDays(new Date(), -10));

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		conn.close();
		
		Identity savedIdentity = securityManager.loadIdentityByKey(savedVo.getKey());
		Assertions.assertThat(savedIdentity.getUser().getLastModified())
			.isAfter(DateUtils.addDays(new Date(), -1));
	}
	
	@Test
	public void testGetUserNotFound() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/1").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(404, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
	}
	
	@Test
	public void testGetUserMe()
	throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		UserVO vo = conn.parse(response, UserVO.class);

		Assert.assertNotNull(vo);
		Assert.assertEquals(vo.getKey(), id1.getKey());
		Assert.assertFalse(vo.getProperties().isEmpty());
		conn.close();
	}
	
	/**
	 * User without managers permission cannot see profile informations of
	 * other users.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException 
	 */
	@Test
	public void testGetUserNotAdmin() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id2.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(403, response.statusCode());
		ConnectionUtilities.consume(response);
		conn.close();
	}
	
	@Test
	public void testGetManagedUser() throws IOException, URISyntaxException, InterruptedException {
		String externalId = UUID.randomUUID().toString();
		Identity managedId = JunitTestHelper.createAndPersistIdentityAsRndUser("managed-1");
		dbInstance.commitAndCloseSession();
		securityManager.setExternalId(managedId, externalId);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("managed").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<ManagedUserVO> managedUsers = conn.parseList(response, ManagedUserVO.class);

		boolean found = false;
		for(ManagedUserVO managedUser:managedUsers) {
			if(managedUser.getKey().equals(managedId.getKey())) {
				found = true;
				Assert.assertEquals(externalId, managedUser.getExternalId());
				Assert.assertEquals(Identity.STATUS_ACTIV, managedUser.getStatus());
			}
			Assert.assertNotNull(managedUser.getExternalId());
			Assert.assertNotNull(managedUser.getStatus());
		}
		Assert.assertTrue(found);
		conn.close();

	}
	
	@Test
	public void testGetManagedUser_onlyUserManagers() throws IOException, URISyntaxException, InterruptedException {
		String externalId = UUID.randomUUID().toString();
		Identity managedId = JunitTestHelper.createAndPersistIdentityAsRndUser("managed-1");
		dbInstance.commitAndCloseSession();
		securityManager.setExternalId(managedId, externalId);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(id1);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("managed").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(403, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test	
	public void testGetRawJsonUser() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		String bodyJson = RestConnection.toString(response);
		log.info("User JSON: {}", bodyJson);
		conn.close();
	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test	
	public void testGetRawXmlUser() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey()).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_XML);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		String bodyXml = RestConnection.toString(response);
		log.info("User XML: {}", bodyXml);
		conn.close();
	}
	
	@Test
	public void testCreateUser() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847594");
		vo.putProperty("telPrivate", "39847594");
		vo.putProperty("telMobile", "39847594");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "11/12/2009");

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedIdent);
		Assert.assertEquals(savedVo.getKey(), savedIdent.getKey());
		Assert.assertEquals(Identity.STATUS_ACTIV, savedVo.getStatus());
		Assert.assertEquals("Female", savedIdent.getUser().getProperty("gender", Locale.ENGLISH));
		Assert.assertEquals("39847594", savedIdent.getUser().getProperty("telPrivate", Locale.ENGLISH));
		Assert.assertEquals("11/12/2009", savedIdent.getUser().getProperty("birthDay", Locale.ENGLISH));
		conn.close();
	}
	
	@Test
	public void testCreateUser_emptyLogin() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		UserVO vo = new UserVO();
		vo.setLogin("");
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(UUID.randomUUID() + "@frentix.com");
		vo.putProperty("telOffice", "39847595");
		vo.putProperty("telPrivate", "39847595");
		vo.putProperty("telMobile", "39847595");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		int statusCode = response.statusCode();
		RestConnection.consume(response);
		Assert.assertEquals(406, statusCode);
		conn.close();
	}
	
	@Test
	public void testCreateUser_special() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		UserVO vo = new UserVO();
		vo.setLogin("tes @-()_" + CodeHelper.getForeverUniqueID());
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(UUID.randomUUID() + "@frentix.com");
		vo.putProperty("telOffice", "39847596");
		vo.putProperty("telPrivate", "39847596");
		vo.putProperty("telMobile", "39847596");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		int statusCode = response.statusCode();
		RestConnection.consume(response);
		Assert.assertEquals(200, statusCode);
		conn.close();
	}
	
	/**
	 * Test machine format for gender and date
	 */
	@Test
	public void testCreateUser2() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		UserVO vo = new UserVO();
		vo.setLogin(UUID.randomUUID().toString());
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(UUID.randomUUID() + "@frentix.com");
		vo.putProperty("telOffice", "39847597");
		vo.putProperty("telPrivate", "39847597");
		vo.putProperty("telMobile", "39847597");
		vo.putProperty("gender", "female");//male or female
		vo.putProperty("birthDay", "20091212");

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedIdent);
		Assert.assertEquals(Identity.STATUS_ACTIV, savedVo.getStatus());
		Assert.assertEquals(savedVo.getKey(), savedIdent.getKey());
		Assert.assertEquals("Female", savedIdent.getUser().getProperty("gender", Locale.ENGLISH));
		Assert.assertEquals("39847597", savedIdent.getUser().getProperty("telPrivate", Locale.ENGLISH));
		Assert.assertEquals("12/12/2009", savedIdent.getUser().getProperty("birthDay", Locale.ENGLISH));
		conn.close();
	}
	
	/**
	 * Test the trim of email
	 */
	@Test
	public void testCreateUser_emailWithTrailingSpace() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com ");
		vo.putProperty("gender", "male");//male or female


		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		assertEquals(username + "@frentix.com", savedIdent.getUser().getProperty(UserConstants.EMAIL, null));
		conn.close();

	}
	
	/**
	 * Test if we can create two users with the same email addresses.
	 */
	@Test
	public void testCreateUser_same_email() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		String email = UUID.randomUUID() + "@frentix.com";
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(email);
		vo.putProperty("gender", "male");//male or female

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		
		//second 
		UserVO vo2 = new UserVO();
		vo2.setLogin(UUID.randomUUID().toString());
		vo2.setFirstName("Eva");
		vo2.setLastName("Smith");
		vo2.setEmail(email);
		vo2.putProperty("gender", "female");

		URI request2 = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method2 = conn.createPut(request2, vo2, MediaType.APPLICATION_JSON, "en");
		
		HttpResponse<InputStream> response2 = conn.execute(method2);
		int  statusCode2 = response2.statusCode();
		Assert.assertEquals(406, statusCode2);
		String errorMessage = RestConnection.toString(response2);
		Assert.assertNotNull(errorMessage);
		conn.close();

	}
	
	/**
	 * Test if we can create two users with the same login.
	 */
	@Test
	public void testCreateUser_same_login() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Dupont");
		vo.setEmail(UUID.randomUUID() + "@frentix.com");
		vo.putProperty("gender", "male");//male or female

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		
		//second 
		UserVO vo2 = new UserVO();
		vo2.setLogin(username);
		vo2.setFirstName("Eva");
		vo2.setLastName("Smith");
		vo2.setEmail(UUID.randomUUID() + "@frentix.com");
		vo2.putProperty("gender", "female");

		URI request2 = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method2 = conn.createPut(request2, vo2, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response2 = conn.execute(method2);
		int  statusCode2 = response2.statusCode();
		Assert.assertEquals(406, statusCode2);
		String errorMessage = RestConnection.toString(response2);
		Assert.assertNotNull(errorMessage);
		conn.close();

	}
	
	@Test
	public void testCreateUserWithValidationError() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		String login = "rest-809-" + UUID.randomUUID();
		
		UserVO vo = new UserVO();
		vo.setLogin(login);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail("");
		vo.putProperty("gender", "lu");

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(406, response.statusCode());
		List<ErrorVO> errors = parseErrorArray(response);
 		assertNotNull(errors);
		assertFalse(errors.isEmpty());
		assertTrue(errors.size() >= 2);
		assertNotNull(errors.get(0).getCode());
		assertNotNull(errors.get(0).getTranslation());
		assertNotNull(errors.get(1).getCode());
		assertNotNull(errors.get(1).getTranslation());
		
		Identity savedIdent = securityManager.findIdentityByName(login);
		assertNull(savedIdent);
		conn.close();
	}
	
	/**
	 * Test if we can create two users with the same email addresses.
	 */
	@Test
	public void testCreateAndUpdateUser() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		String username = UUID.randomUUID().toString();
		String email = UUID.randomUUID() + "@frentix.com";
		
		UserVO vo = new UserVO();
		vo.setLogin(username);
		vo.setFirstName("Terence");
		vo.setLastName("Smith");
		vo.setEmail(email);
		vo.putProperty("gender", "male");//male or female

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedIdent);
		Assert.assertEquals(savedVo.getKey(), savedIdent.getKey());
		Assert.assertEquals("Terence", savedIdent.getUser().getFirstName());
		
		//second 
		UserVO updateVo = new UserVO();
		updateVo.setKey(savedVo.getKey());
		updateVo.setLogin(username);
		updateVo.setFirstName("Maximilien");
		updateVo.setLastName("Smith");
		updateVo.setEmail(email);
		updateVo.putProperty("gender", "male");

		URI updateRequest = UriBuilder.fromUri(getContextURI()).path("users").path(savedVo.getKey().toString()).build();
		HttpRequest updateMethod = conn.createPost(updateRequest, updateVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> updateResponse = conn.execute(updateMethod);
		int  statusCode = updateResponse.statusCode();
		Assert.assertEquals(200, statusCode);
		UserVO updatedVo = conn.parse(updateResponse, UserVO.class);
		dbInstance.commitAndCloseSession();
		Identity updatedIdent = securityManager.loadIdentityByKey(savedVo.getKey());
		
		Assert.assertNotNull(updatedVo);
		Assert.assertNotNull(updatedIdent);
		Assert.assertEquals(updatedVo.getKey(), savedIdent.getKey());
		Assert.assertEquals(updatedVo.getKey(), updatedIdent.getKey());
		Assert.assertEquals("Maximilien", updatedIdent.getUser().getFirstName());
		conn.close();

	}
	
	/**
	 * Test if we can create two users with the same email addresses.
	 */
	@Test
	public void testCreateAndUpdateUser_same_email() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		String email = UUID.randomUUID() + "@frentix.com";
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(email);
		vo.putProperty("gender", "male");//male or female

		URI request = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest method = conn.createPut(request, vo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);
		UserVO savedVo = conn.parse(response, UserVO.class);
		Identity savedIdent = securityManager.loadIdentityByKey(savedVo.getKey());

		Assert.assertNotNull(savedVo);
		Assert.assertNotNull(savedIdent);
		
		//second user
		String secondEmail = UUID.randomUUID() + "@frentix.com";
		String secondUsername = UUID.randomUUID().toString();
		
		UserVO secondVo = new UserVO();
		secondVo.setLogin(secondUsername);
		secondVo.setFirstName("Eva");
		secondVo.setLastName("Smith");
		secondVo.setEmail(secondEmail);
		secondVo.putProperty("gender", "female");

		URI secondRequest = UriBuilder.fromUri(getContextURI()).path("users").build();
		HttpRequest secondMethod = conn.createPut(secondRequest, secondVo, MediaType.APPLICATION_JSON, "en");
		HttpResponse<InputStream> secondResponse = conn.execute(secondMethod);
		int secondStatusCode = secondResponse.statusCode();
		Assert.assertEquals(200, secondStatusCode);
		UserVO secondSavedVo = conn.parse(secondResponse, UserVO.class);
		Assert.assertNotNull(secondSavedVo);
		
		dbInstance.commitAndCloseSession();
		Identity secondSavedIdent = securityManager.loadIdentityByKey(secondSavedVo.getKey());
		Assert.assertNotNull(secondSavedIdent);
		Assert.assertEquals(secondEmail, secondSavedIdent.getUser().getEmail());

		// update second with new first name and the mail of the first user
		UserVO updateVo = new UserVO();
		updateVo.setKey(secondSavedVo.getKey());
		updateVo.setLogin(secondUsername);
		updateVo.setFirstName("Caprice");
		updateVo.setLastName("Smith");
		updateVo.setEmail(email);
		updateVo.putProperty("gender", "female");

		URI updateRequest = UriBuilder.fromUri(getContextURI()).path("users").path(secondSavedVo.getKey().toString()).build();
		HttpRequest updateMethod = conn.createPost(updateRequest, updateVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> updateResponse = conn.execute(updateMethod);
		int  statusCode = updateResponse.statusCode();
		Assert.assertEquals(406, statusCode);
		String errorMessage = RestConnection.toString(updateResponse);
		Assert.assertNotNull(errorMessage);
		
		// check that nothing has changed for the second user
		dbInstance.commitAndCloseSession();
		Identity notUpdatedIdent = securityManager.loadIdentityByKey(secondSavedVo.getKey());
		Assert.assertNotNull(notUpdatedIdent);
		Assert.assertEquals("Eva", notUpdatedIdent.getUser().getFirstName());
		Assert.assertEquals("Smith", notUpdatedIdent.getUser().getLastName());
		Assert.assertEquals(secondEmail, notUpdatedIdent.getUser().getEmail());
		conn.close();

	}
	
	@Test
	public void testUpdateUser_emptyInstitutionalEmail() throws IOException, URISyntaxException, InterruptedException {
		String login = "update-" + UUID.randomUUID();
		User user = userManager.createUser(login, login, login + "@openolat.com");
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, "inst" + login + "@openolat.com");
		Identity id = securityManager.createAndPersistIdentityAndUser(null, login, null, user, "OLAT", BaseSecurity.DEFAULT_ISSUER, null, login, "secret", null);
		Organisation organisation = organisationService.getDefaultOrganisation();
		organisationService.addMember(organisation, id, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		Assert.assertEquals("inst" + login + "@openolat.com", id.getUser().getInstitutionalEmail());

		RestConnection conn = new RestConnection("administrator", "openolat");

		// set the institutional email empty 
		UserVO updateVo = new UserVO();
		updateVo.setKey(id.getKey());
		updateVo.setLogin(id.getName());
		updateVo.setFirstName(id.getUser().getFirstName());
		updateVo.setLastName(id.getUser().getLastName());
		updateVo.setEmail(id.getUser().getEmail());
		updateVo.putProperty(UserConstants.INSTITUTIONALEMAIL, "");
		
		URI updateRequest = UriBuilder.fromUri(getContextURI()).path("users").path(id.getKey().toString()).build();
		HttpRequest updateMethod = conn.createPost(updateRequest, updateVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> updateResponse = conn.execute(updateMethod);
		int  statusCode = updateResponse.statusCode();
		Assert.assertEquals(200, statusCode);
		RestConnection.consume(updateResponse);
		
		Identity identity = securityManager.loadIdentityByKey(id.getKey());
		String institutionalEmail = identity.getUser().getInstitutionalEmail();
		Assert.assertTrue(institutionalEmail == null || institutionalEmail.isEmpty());
		conn.close();
	}
	
	@Test
	public void updateAdminUser_denied() throws IOException, URISyntaxException, InterruptedException {
		String login = "rolemanager-" + UUID.randomUUID();
		User user = userManager.createUser(login, login, login + "@openolat.com");
		Identity id = securityManager.createAndPersistIdentityAndUser(null, login, null, user, "OLAT", BaseSecurity.DEFAULT_ISSUER, null, login, "2change-very-often", null);
		Organisation organisation = organisationService.getDefaultOrganisation();
		organisationService.addMember(organisation, id, OrganisationRoles.user, JunitTestHelper.getDefaultActor());
		organisationService.addMember(organisation, id, OrganisationRoles.rolesmanager, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
	
		RestConnection conn = new RestConnection(login, "2change-very-often");
		
		String newEmail = login + "@on.frentix.com";
		// Find administrator
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path("username")
				.queryParam("username","administrator").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		UserVO adminVo = conn.parse(response, UserVO.class);
		adminVo.setEmail(newEmail);
		
		URI updateRequest = UriBuilder.fromUri(getContextURI()).path("users").path(adminVo.getKey().toString()).build();
		HttpRequest updateMethod = conn.createPost(updateRequest, adminVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> updateResponse = conn.execute(updateMethod);
		int  statusCode = updateResponse.statusCode();
		Assert.assertEquals(403, statusCode);
		RestConnection.consume(updateResponse);
		
		Identity identity = securityManager.loadIdentityByKey(adminVo.getKey());
		Assert.assertNotEquals(newEmail, identity.getUser().getEmail());
		conn.close();
	}
	
	
	@Test
	public void testDeleteUser() throws IOException, URISyntaxException, InterruptedException {
		Identity idToDelete = JunitTestHelper.createAndPersistIdentityAsUser("user-to-delete-" + UUID.randomUUID());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");

		//delete an authentication token
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + idToDelete.getKey()).build();
		HttpRequest method = conn.createDelete(request, MediaType.APPLICATION_XML);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		Identity deletedIdent = securityManager.loadIdentityByKey(idToDelete.getKey());
		assertNotNull(deletedIdent);//Identity aren't deleted anymore
		assertEquals(Identity.STATUS_DELETED, deletedIdent.getStatus());
		conn.close();
	}
	
	@Test
	public void testGetRoles() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + author.getKey() + "/roles").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RolesVO roles = conn.parse(response, RolesVO.class);
		Assert.assertNotNull(roles);
		Assert.assertTrue(roles.isAuthor());
		Assert.assertFalse(roles.isGroupManager());
		Assert.assertFalse(roles.isGuestOnly());
		Assert.assertFalse(roles.isInstitutionalResourceManager());
		Assert.assertFalse(roles.isInvitee());
		Assert.assertFalse(roles.isOlatAdmin());
		Assert.assertFalse(roles.isUserManager());
		conn.close();
	}
	
	@Test
	public void testGetRoles_xml() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + author.getKey() + "/roles").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_XML);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		String xmlOutput = RestConnection.toString(response);
		Assert.assertTrue(xmlOutput.contains("<rolesVO>"));
		Assert.assertTrue(xmlOutput.contains("<olatAdmin>"));
		conn.close();
	}
	
	@Test
	public void getRoles_itself() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);

		URI rolesUri = UriBuilder.fromUri(getContextURI())
			.path("users").path(id1.getKey().toString()).path("roles").build();
		
		HttpRequest method = conn.createGet(rolesUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RolesVO vo = conn.parse(response, RolesVO.class);
		Assert.assertNotNull(vo);
		Assert.assertFalse(vo.isInvitee());
		Assert.assertFalse(vo.isGuestOnly());
		conn.close();
	}
	
	@Test
	public void getRoles_notItself() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);

		URI rolesUri = UriBuilder.fromUri(getContextURI())
			.path("users").path(id2.getKey().toString()).path("roles").build();
		
		HttpRequest method = conn.createGet(rolesUri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(403, response.statusCode());
		conn.close();
	}
	
	@Test
	public void testUpdateRolesAsAdmin() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("author-1");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		RolesVO roles = new RolesVO();
		roles.setAuthor(true);
		roles.setUserManager(true);
		roles.setSystemAdmin(true);
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + author.getKey() + "/roles").build();
		HttpRequest method = conn.createPost(request, roles, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RolesVO modRoles = conn.parse(response, RolesVO.class);
		Assert.assertNotNull(modRoles);
		
		//check the roles
		Roles reloadRoles = securityManager.getRoles(author);
		Assert.assertFalse(reloadRoles.isGuestOnly());
		Assert.assertFalse(reloadRoles.isInvitee());
		Assert.assertTrue(reloadRoles.isAuthor());
		Assert.assertFalse(reloadRoles.isGroupManager());
		Assert.assertFalse(reloadRoles.isLearnResourceManager());
		Assert.assertFalse(reloadRoles.isPoolManager());
		Assert.assertFalse(reloadRoles.isCurriculumManager());
		Assert.assertTrue(reloadRoles.isUserManager());
		Assert.assertFalse(reloadRoles.isAdministrator());
		Assert.assertTrue(reloadRoles.isSystemAdmin());
		conn.close();
	}
	
	@Test
	public void testUpdateRolesAsUserManager() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-2");
		dbInstance.commitAndCloseSession();
		
		IdentityWithLogin userMgr = JunitTestHelper.createAndPersistRndUser("usermanager-1");
		organisationService.addMember(userMgr.getIdentity(), OrganisationRoles.usermanager, userMgr.getIdentity());
		dbInstance.commitAndCloseSession();
				
		RestConnection conn = new RestConnection(userMgr);
		
		RolesVO roles = new RolesVO();
		roles.setAuthor(true);
		roles.setUserManager(true);
		roles.setCurriculumManager(true);
		roles.setOlatAdmin(true);
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + user.getKey() + "/roles").build();
		HttpRequest method = conn.createPost(request, roles, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RolesVO modRoles = conn.parse(response, RolesVO.class);
		Assert.assertNotNull(modRoles);
		
		//check the roles
		Roles reloadRoles = securityManager.getRoles(user);
		Assert.assertFalse(reloadRoles.isGuestOnly());
		Assert.assertFalse(reloadRoles.isInvitee());
		Assert.assertTrue(reloadRoles.isAuthor());
		Assert.assertFalse(reloadRoles.isGroupManager());
		Assert.assertFalse(reloadRoles.isLearnResourceManager());
		Assert.assertFalse(reloadRoles.isPoolManager());
		Assert.assertFalse(reloadRoles.isCurriculumManager());
		Assert.assertFalse(reloadRoles.isUserManager());
		Assert.assertFalse(reloadRoles.isAdministrator());
		Assert.assertFalse(reloadRoles.isSystemAdmin());
		conn.close();
	}
	
	@Test
	public void testUpdateRolesAsRolesManager() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity user = JunitTestHelper.createAndPersistIdentityAsRndUser("user-3");
		dbInstance.commitAndCloseSession();
		
		IdentityWithLogin rolesManager = JunitTestHelper.createAndPersistRndUser("rolesmanager-1");
		organisationService.addMember(rolesManager.getIdentity(), OrganisationRoles.rolesmanager, rolesManager.getIdentity());
		dbInstance.commitAndCloseSession();
				
		RestConnection conn = new RestConnection(rolesManager);
		
		RolesVO roles = new RolesVO();
		roles.setAuthor(true);
		roles.setUserManager(true);
		roles.setCurriculumManager(true);
		roles.setOlatAdmin(true);
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + user.getKey() + "/roles").build();
		HttpRequest method = conn.createPost(request, roles, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RolesVO modRoles = conn.parse(response, RolesVO.class);
		Assert.assertNotNull(modRoles);
		
		//check the roles
		Roles reloadRoles = securityManager.getRoles(user);
		Assert.assertFalse(reloadRoles.isGuestOnly());
		Assert.assertFalse(reloadRoles.isInvitee());
		Assert.assertTrue(reloadRoles.isAuthor());
		Assert.assertFalse(reloadRoles.isGroupManager());
		Assert.assertFalse(reloadRoles.isLearnResourceManager());
		Assert.assertFalse(reloadRoles.isPoolManager());
		Assert.assertTrue(reloadRoles.isCurriculumManager());
		Assert.assertTrue(reloadRoles.isUserManager());
		Assert.assertFalse(reloadRoles.isAdministrator());
		Assert.assertFalse(reloadRoles.isSystemAdmin());
		conn.close();
	}
	
	@Test
	public void testGetStatus() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("status-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + user.getKey() + "/status").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		StatusVO status = conn.parse(response, StatusVO.class);
		Assert.assertNotNull(status);
		Assert.assertNotNull(status.getStatus());
		Assert.assertEquals(2, status.getStatus().intValue());
		conn.close();
	}
	
	@Test
	public void testUpdateStatus() throws IOException, URISyntaxException, InterruptedException {
		//create a user
		Identity user = JunitTestHelper.createAndPersistIdentityAsUser("login-denied-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		StatusVO status = new StatusVO();
		status.setStatus(101);
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + user.getKey() + "/status").build();
		HttpRequest method = conn.createPost(request, status, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		StatusVO modStatus = conn.parse(response, StatusVO.class);
		Assert.assertNotNull(modStatus);
		Assert.assertNotNull(modStatus.getStatus());
		Assert.assertEquals(101, modStatus.getStatus().intValue());
		
		//check the roles
		Identity reloadIdentity = securityManager.loadIdentityByKey(user.getKey());
		Assert.assertNotNull(reloadIdentity);
		Assert.assertNotNull(reloadIdentity.getStatus());
		Assert.assertEquals(101, reloadIdentity.getStatus().intValue());
		conn.close();
	}
	
	/**
	 * Test if a standard user can change the status of someone else
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testUpdateStatus_denied() throws IOException, URISyntaxException, InterruptedException {
		//create a user
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("login-denied-2");
		IdentityWithLogin hacker = JunitTestHelper.createAndPersistRndUser("login-denied-2");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(hacker);
		
		StatusVO status = new StatusVO();
		status.setStatus(101);
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + user.getKey() + "/status").build();
		HttpRequest method = conn.createPost(request, status, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(403, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
	
	@Test
	public void testGetPreferences() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity prefsId = JunitTestHelper.createAndPersistIdentityAsAuthor("prefs-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		prefsId.getUser().getPreferences().setLanguage("fr");
		userManager.updateUserFromIdentity(prefsId);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//get preferences of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + prefsId.getKey() + "/preferences").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		PreferencesVO prefsVo = conn.parse(response, PreferencesVO.class);
		Assert.assertNotNull(prefsVo);
		Assert.assertEquals("fr", prefsVo.getLanguage());
		conn.close();
	}
	
	@Test
	public void testUpdatePreferences() throws IOException, URISyntaxException, InterruptedException {
		//create an author
		Identity prefsId = JunitTestHelper.createAndPersistIdentityAsAuthor("prefs-1-" + UUID.randomUUID().toString());
		dbInstance.commitAndCloseSession();
		prefsId.getUser().getPreferences().setLanguage("de");
		userManager.updateUserFromIdentity(prefsId);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");
		
		PreferencesVO prefsVo = new PreferencesVO();
		prefsVo.setLanguage("fr");
		
		//get roles of author
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + prefsId.getKey() + "/preferences").build();
		HttpRequest method = conn.createPost(request, prefsVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		PreferencesVO modPrefs = conn.parse(response, PreferencesVO.class);
		Assert.assertNotNull(modPrefs);
		Assert.assertEquals("fr", prefsVo.getLanguage());
		
		//double check
		Identity reloadedPrefsId = securityManager.loadIdentityByKey(prefsId.getKey());
		Assert.assertNotNull(reloadedPrefsId);
		Assert.assertEquals("fr", reloadedPrefsId.getUser().getPreferences().getLanguage());
		conn.close();

	}
	
	@Test
	public void testUserForums() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("forums")
				.queryParam("start", 0).queryParam("limit", 20).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		ForumVOes forums = conn.parse(response, ForumVOes.class);
		
		assertNotNull(forums);
		assertNotNull(forums.getForums());
		assertTrue(forums.getForums().length > 0);

		for(ForumVO forum:forums.getForums()) {
			Long groupKey = forum.getGroupKey();
			if(groupKey != null) {
				BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey);
				assertNotNull(bg);
				CollaborationTools bgCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
				assertTrue(bgCTSMngr.isToolEnabled(CollaborationTools.TOOL_FORUM));
				
				assertNotNull(forum.getForumKey());
				assertEquals(bg.getName(), forum.getName());
				assertEquals(bg.getKey(), forum.getGroupKey());
				assertTrue(businessGroupService.isIdentityInBusinessGroup(id1.getIdentity(), bg));
			} else {
				assertNotNull(forum.getCourseKey());
			}
		}
		conn.close();
	}
	
	@Test
	public void testUserGroupForum() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("forums")
				.path("group").path(g1.getKey().toString())
				.path("threads").queryParam("start", "0").queryParam("limit", "25").build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		MessageVOes threads = conn.parse(response, MessageVOes.class);
		
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getMessages().length > 0);
		conn.close();
	}
	
	@Test
	public void testUserCourseForum() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("forums")
				.path("course").path(demoCourse.getResourceableId().toString()).path(demoForumNode.getIdent())
				.path("threads").queryParam("start", "0").queryParam("limit", 25).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		MessageVOes threads = conn.parse(response, MessageVOes.class);
		
		assertNotNull(threads);
		assertNotNull(threads.getMessages());
		assertTrue(threads.getMessages().length > 0);
		conn.close();
	}
	
	@Test
	public void testUserFolders() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders").build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		FolderVOes folders = conn.parse(response, FolderVOes.class);
		
		assertNotNull(folders);
		assertNotNull(folders.getFolders());
		assertTrue(folders.getFolders().length > 0);

		boolean matchG2 = false;
		
		for(FolderVO folder:folders.getFolders()) {
			Long groupKey = folder.getGroupKey();
			if(groupKey != null) {
				BusinessGroup bg = businessGroupService.loadBusinessGroup(groupKey);
				assertNotNull(bg);
				CollaborationTools bgCTSMngr = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(bg);
				assertTrue(bgCTSMngr.isToolEnabled(CollaborationTools.TOOL_FOLDER));
				
				assertEquals(bg.getName(), folder.getName());
				assertEquals(bg.getKey(), folder.getGroupKey());
				assertTrue(businessGroupService.isIdentityInBusinessGroup(id1.getIdentity(), bg));
				if(g2.getKey().equals(groupKey)) {
					matchG2 = true;
				}
			} else {
				assertNotNull(folder.getCourseKey());
			}
		}
		
		//id1 is participant of g2. Make sure it found the folder
		assertTrue(matchG2);
		conn.close();
	}
	
	@Test
	public void testUserGroupFolder() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders")
				.path("group").path(g2.getKey().toString()).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<FileVO> folders = parseFileArray(response);

		assertNotNull(folders);
		assertFalse(folders.isEmpty());
		assertEquals(1, folders.size()); //private and public
		
		FileVO portrait = folders.get(0);
		assertEquals("portrait.jpg", portrait.getTitle());
		conn.close();
	}
	
	@Test
	public void testUserBCCourseNodeFolder() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders")
				.path("course").path(demoCourse.getResourceableId().toString()).path(demoBCCourseNode.getIdent()).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());

		List<FileVO> folders = parseFileArray(response);

		assertNotNull(folders);
		assertFalse(folders.isEmpty());
		assertEquals(1, folders.size()); //private and public
		
		FileVO singlePage = folders.get(0);
		assertEquals("singlepage.html", singlePage.getTitle());
		conn.close();
	}
	
	@Test
	public void testUserPersonalFolder() throws Exception {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders").path("personal").build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		List<FileVO> files = parseFileArray(response);
		
		assertNotNull(files);
		assertFalse(files.isEmpty());
		assertEquals(2, files.size()); //private and public
		conn.close();
	}
	
	@Test
	public void testUserPersonalFolderUpload() throws Exception {
		RestConnection conn = new RestConnection(id1);
		
		URL fileUrl = CoursesFoldersTest.class.getResource("singlepage.html");
		Assert.assertNotNull(fileUrl);
		File file = new File(fileUrl.toURI());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders")
				.path("personal").path("private").build();

		HttpRequest method = conn.createPut(uri, file, file.getName(), List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		ConnectionUtilities.consume(response);
		
		Roles roles = Roles.userRoles();
		VFSContainer myFolder = new BriefcaseWebDAVProvider().getContainer(id1.getIdentity(), roles);
		VFSItem item = myFolder.resolve("private/singlepage.html");
		Assert.assertNotNull(item);
		Assert.assertTrue(item instanceof VFSLeaf);
		Assert.assertTrue(item.exists());
		
		conn.close();
	}
	
	@Test
	public void testUserPersonalFolderTraversalUpload() throws Exception {
		RestConnection conn = new RestConnection(id1);
		
		URL fileUrl = CoursesFoldersTest.class.getResource("singlepage.html");
		Assert.assertNotNull(fileUrl);
		File file = new File(fileUrl.toURI());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("folders")
				.path("personal").path("private").build();

		String filename = "../public/" + file.getName();
		HttpRequest method = conn.createPut(uri, file, filename, List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(406, response.statusCode());
		ConnectionUtilities.consume(response);
		
		Roles roles = Roles.userRoles();
		VFSContainer myFolder = new BriefcaseWebDAVProvider().getContainer(id1.getIdentity(), roles);
		VFSItem item = myFolder.resolve("public/singlepage.html");
		Assert.assertNull(item);
		
		conn.close();
	}
	
	@Test
	public void testOtherUserPersonalFolder() throws Exception {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id2.getKey().toString()).path("folders").path("personal").build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		List<FileVO> files = parseFileArray(response);
		
		assertNotNull(files);
		assertTrue(files.isEmpty());
		assertEquals(0, files.size()); //private and public
		conn.close();
	}
	
	@Test
	public void testOtherUserPersonalFolderOfId3() throws Exception {
		RestConnection conn = new RestConnection(id1);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("users").path(id3.getKey().toString()).path("folders").path("personal").build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		List<FileVO> files = parseFileArray(response);
		
		assertNotNull(files);
		assertFalse(files.isEmpty());
		assertEquals(1, files.size()); //private and public
		
		FileVO portrait = files.get(0);
		assertEquals("portrait.jpg", portrait.getTitle());
			conn.close();
	}
	
	@Test
	public void testUserGroup() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve all groups
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey() + "/groups").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());

		List<GroupVO> groups = conn.parseList(response, GroupVO.class);
		assertNotNull(groups);
		assertEquals(3, groups.size());//g1, g2 and g3
		conn.close();
	}
	
	@Test
	public void testUserGroup_managed() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve managed groups
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
				.queryParam("managed", "true").build();
		
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());

		List<GroupVO> groups = conn.parseList(response, GroupVO.class);
		assertNotNull(groups);
		assertEquals(2, groups.size());//g1 and g3
		conn.close();
	}
	
	@Test
	public void testUserGroup_notManaged() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve free groups
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
				.queryParam("managed", "false").build();
		
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());

		List<GroupVO> groups = conn.parseList(response, GroupVO.class);
		assertNotNull(groups);
		assertEquals(1, groups.size());//g2
		conn.close();
	}
	
	@Test
	public void testUserGroup_externalId() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve g1
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
				.queryParam("externalId", g1externalId).build();
		
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());

		List<GroupVO> groups = conn.parseList(response, GroupVO.class);
		assertNotNull(groups);
		assertEquals(1, groups.size());
		assertEquals(g1.getKey(), groups.get(0).getKey());
		assertEquals(g1externalId, groups.get(0).getExternalId());

		conn.close();
	}
	
	@Test
	public void testUserGroupWithPaging() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
			.queryParam("start", 0).queryParam("limit", 1).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		GroupVOes groups = conn.parse(response, GroupVOes.class);
		
		assertNotNull(groups);
		assertNotNull(groups.getGroups());
		assertEquals(1, groups.getGroups().length);
		assertEquals(3, groups.getTotalCount());//g1, g2 and g3
		conn.close();
	}
	
	@Test
	public void testUserGroup_checkRefusedAccess() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin alien = JunitTestHelper.createAndPersistRndUser("user-group-alien-");
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(alien);
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
			.queryParam("start", 0).queryParam("limit", 1).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(Status.FORBIDDEN.getStatusCode(), response.statusCode());
		RestConnection.consume(response);

		conn.close();
	}
	
	@Test
	public void testUserGroup_checkAllowedAccess() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection(id1);
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups")
			.queryParam("start", 0).queryParam("limit", 1).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		GroupVOes groups = conn.parse(response, GroupVOes.class);
		
		assertNotNull(groups);
		assertNotNull(groups.getGroups());

		conn.close();
	}
	
	@Test
	public void testUserGroup_owner() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
			.path("groups").path("owner").queryParam("start", 0).queryParam("limit", 1).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		GroupVOes groups = conn.parse(response, GroupVOes.class);
		
		assertNotNull(groups);
		assertNotNull(groups.getGroups());
		assertEquals(1, groups.getGroups().length);
		assertEquals(1, groups.getTotalCount());//g1
		conn.close();
	}
	
	@Test
	public void testUserGroup_participant() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString())
			.path("groups").path("participant").queryParam("start", 0).queryParam("limit", 1).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		GroupVOes groups = conn.parse(response, GroupVOes.class);
		
		assertNotNull(groups);
		assertNotNull(groups.getGroups());
		assertEquals(1, groups.getGroups().length);
		assertEquals(2, groups.getTotalCount());//g2 and g3
		conn.close();
	}
	
	@Test
	public void testUserGroupInfosWithPaging() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		//retrieve all groups
		URI uri =UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).path("groups").path("infos")
			.queryParam("start", 0).queryParam("limit", 1).build();

		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		GroupInfoVOes groups = conn.parse(response, GroupInfoVOes.class);
		
		assertNotNull(groups);
		assertNotNull(groups.getGroups());
		assertEquals(1, groups.getGroups().length);
		assertEquals(3, groups.getTotalCount());//g1, g2 and g3
		conn.close();
	}
	
	@Test
	public void testPortrait() throws IOException, URISyntaxException, InterruptedException {
		URL portraitUrl = UserMgmtTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		RestConnection conn = new RestConnection(id1);
		
		//upload portrait
		URI request = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey() + "/portrait").build();
		HttpRequest method = conn.createPost(request, portrait, "portrait.jpg", List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);

		
		//check if portraits exist
		Identity identity1 = securityManager.loadIdentityByKey(id1.getIdentity().getKey());
		VFSLeaf bigPortraitImage = userPortraitService.getPortraitImage(identity1, PortraitSize.medium);
		assertNotNull(bigPortraitImage);
		assertTrue(bigPortraitImage.exists());
		
		//check get portrait
		URI getRequest = UriBuilder.fromUri(getContextURI()).path("/users/" + id1.getKey() + "/portrait").build();
		HttpRequest getMethod = conn.createGet(getRequest, MediaType.APPLICATION_OCTET_STREAM);
		HttpResponse<InputStream> getResponse = conn.execute(getMethod);
		assertEquals(200, getResponse.statusCode());
		InputStream in = getResponse.body();
		int b = 0;
		int count = 0;
		while((b = in.read()) > -1) {
			count++;
		}
		
		assertEquals(-1, b);//up to end of file
		assertTrue(count > 1000);//enough bytes
		bigPortraitImage = userPortraitService.getPortraitImage(identity1, PortraitSize.medium);
		assertNotNull(bigPortraitImage);
		assertEquals(count, bigPortraitImage.getSize());

		//check get portrait as Base64
		UriBuilder getRequest2 = UriBuilder.fromUri(getContextURI()).path("users").path(id1.getKey().toString()).queryParam("withPortrait", "true");
		HttpRequest getMethod2 = conn.createGet(getRequest2.build(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> getCode2 = conn.execute(getMethod2);
		assertEquals(200, getCode2.statusCode());
		UserVO userVo = conn.parse(getCode2, UserVO.class);
		assertNotNull(userVo);
		assertNotNull(userVo.getPortrait());
		byte[] datas = Base64.decodeBase64(userVo.getPortrait().getBytes());
		assertNotNull(datas);
		assertTrue(datas.length > 0);
		
		VFSLeaf smallPortraitImage = userPortraitService.getPortraitImage(identity1, PortraitSize.xsmall);
		assertNotNull(smallPortraitImage);
		assertEquals(datas.length, smallPortraitImage.getSize());
		
		try {
			ImageIO.read(new ByteArrayInputStream(datas));
		} catch (Exception e) {
			assertFalse("Cannot read the portrait after Base64 encoding/decoding", false);
		}
		conn.close();

	}
	
	@Test
	public void testPortrait_HEAD() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("portrait-1");
		Identity idWithoutPortrait = JunitTestHelper.createAndPersistIdentityAsRndUser("portrait-2");
		
		URL portraitUrl = UserMgmtTest.class.getResource("portrait.jpg");
		Assert.assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		RestConnection conn = new RestConnection(id);
		
		//upload portrait
		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(id.getKey().toString()).path("portrait").build();
		HttpRequest method = conn.createPost(request, portrait, "portrait.jpg", List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		//check 200
		URI headRequest = UriBuilder.fromUri(getContextURI())
				.path("users").path(id.getKey().toString()).path("portrait").build();
		HttpRequest headMethod = conn.createHead(headRequest, MediaType.APPLICATION_OCTET_STREAM);
		HttpResponse<InputStream> headResponse = conn.execute(headMethod);
		assertEquals(200, headResponse.statusCode());
		RestConnection.consume(headResponse);
		
		//check 404
		URI headNoRequest = UriBuilder.fromUri(getContextURI())
				.path("users").path(idWithoutPortrait.getKey().toString()).path("portrait").build();
		HttpRequest headNoMethod = conn.createHead(headNoRequest, MediaType.APPLICATION_OCTET_STREAM);
		HttpResponse<InputStream> headNoResponse = conn.execute(headNoMethod);
		assertEquals(404, headNoResponse.statusCode());
		RestConnection.consume(headNoResponse);
		conn.close();
	}
	
	/**
	 * Check the 3 sizes
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void testPortrait_HEAD_sizes() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("portrait-3");
		
		URL portraitUrl = UserMgmtTest.class.getResource("portrait.jpg");
		Assert.assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		RestConnection conn = new RestConnection(id);
		
		//upload portrait
		URI request = UriBuilder.fromUri(getContextURI())
				.path("users").path(id.getKey().toString()).path("portrait").build();
		HttpRequest method = conn.createPost(request, portrait, "portrait.jpg", List.of(), MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		//check 200
		URI headMasterRequest = UriBuilder.fromUri(getContextURI())
				.path("users").path(id.getKey().toString()).path("portrait").path("master").build();
		HttpRequest headMasterMethod = conn.createHead(headMasterRequest, MediaType.APPLICATION_OCTET_STREAM);
		HttpResponse<InputStream> headMasterResponse = conn.execute(headMasterMethod);
		assertEquals(200, headMasterResponse.statusCode());
		RestConnection.consume(headMasterResponse);
		
		//check 200
		URI headBigRequest = UriBuilder.fromUri(getContextURI())
				.path("users").path(id.getKey().toString()).path("portrait").path("big").build();
		HttpRequest headBigMethod = conn.createHead(headBigRequest, MediaType.APPLICATION_OCTET_STREAM);
		HttpResponse<InputStream> headBigResponse = conn.execute(headBigMethod);
		assertEquals(200, headBigResponse.statusCode());
		RestConnection.consume(headBigResponse);
		
		//check 200
		URI headSmallRequest = UriBuilder.fromUri(getContextURI())
				.path("users").path(id.getKey().toString()).path("portrait").path("small").build();
		HttpRequest headSmallMethod = conn.createHead(headSmallRequest, MediaType.APPLICATION_OCTET_STREAM);
		HttpResponse<InputStream> headSmallResponse = conn.execute(headSmallMethod);
		assertEquals(200, headSmallResponse.statusCode());
		RestConnection.consume(headSmallResponse);
		conn.close();
	}
	
	@Test
	public void rename() throws URISyntaxException, IOException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		IdentityWithLogin id = JunitTestHelper.createAndPersistRndUser("portrait-3");
		String newUsername = UUID.randomUUID().toString();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(id.getKey().toString())
			.path("username").queryParam("username", newUsername).build();
		
		HttpRequest method = conn.createPut(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		RestConnection.consume(response);
		
		Identity renamedId = securityManager.loadIdentityByKey(id.getKey());
		Assert.assertEquals(newUsername, renamedId.getUser().getNickName());
		
		Authentication auth = securityManager.findAuthentication(renamedId, "OLAT", BaseSecurity.DEFAULT_ISSUER);
		Assert.assertNotNull(auth);
		Assert.assertEquals(newUsername, auth.getAuthusername());
		conn.close();
	}
}