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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManager;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.context.BGContext;
import org.olat.group.context.BGContextManager;
import org.olat.group.context.BGContextManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.ErrorVO;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.restapi.UserVO;

/**
 * 
 * Description:<br>
 * Test the <code>UserWebservice</code>
 * 
 * <P>
 * Initial Date:  15 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class UserMgmtTest extends OlatJerseyTestCase {
	
	private OLog log = Tracing.createLoggerFor(UserMgmtTest.class);
	
	private Identity owner1, id1, id2;
	private BusinessGroup g1, g2, g3, g4;
	
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		//create identities
		owner1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-zero");
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
		DBFactory.getInstance().intermediateCommit();
		id2.getUser().setProperty("telMobile", "39847592");
		id2.getUser().setProperty("gender", "female");
		id2.getUser().setProperty("birthDay", "20091212");
		DBFactory.getInstance().updateObject(id2.getUser());
		DBFactory.getInstance().intermediateCommit();

		
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		OLATResource course =  rm.createOLATResourceInstance(resourceable);
		DBFactory.getInstance().saveObject(course);
		DBFactory.getInstance().intermediateCommit();
		
		//create learn group

    BGContextManager cm = BGContextManagerImpl.getInstance();
    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
    BaseSecurity secm = BaseSecurityManager.getInstance();
		
    // 1) context one: learning groups
    BGContext c1 = cm.createAndAddBGContextToResource("c1name-learn", course, BusinessGroup.TYPE_LEARNINGROUP, owner1, true);
    // create groups without waiting list
    g1 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g1", null, new Integer(0), new Integer(10), false, false, c1);
    g2 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g2", null, new Integer(0), new Integer(10), false, false, c1);
    // members g1
    secm.addIdentityToSecurityGroup(id1, g1.getOwnerGroup());
    secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
    // members g2
    secm.addIdentityToSecurityGroup(id2, g2.getOwnerGroup());
    secm.addIdentityToSecurityGroup(id1, g2.getPartipiciantGroup());

    // 2) context two: right groups
    BGContext c2 = cm.createAndAddBGContextToResource("c2name-area", course, BusinessGroup.TYPE_RIGHTGROUP, owner1, true);
    // groups
    g3 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g3", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
    g4 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g4", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
    // members
    secm.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
    secm.addIdentityToSecurityGroup(id2, g4.getPartipiciantGroup());
		DBFactory.getInstance().closeSession();
	}
	
  @After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		try {
      DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
      e.printStackTrace();
      throw e;
		}
	}

	@Test
	public void testGetUsers() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<UserVO> vos = parseUserArray(body);
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);

		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(vos.size(), identities.size());
	}
	
	@Test
	public void testFindUsersByLogin() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("login","administrator"),
				new NameValuePair("authProvider","OLAT")
		});
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<UserVO> vos = parseUserArray(body);
		String[] authProviders = new String[]{"OLAT"};
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch("administrator", null, true, null, null, authProviders, null, null, null, null, Identity.STATUS_VISIBLE_LIMIT);

		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(vos.size(), identities.size());
		boolean onlyLikeAdmin = true;
		for(UserVO vo:vos) {
			if(!vo.getLogin().startsWith("administrator")) {
				onlyLikeAdmin = false;
			}
		}
		assertTrue(onlyLikeAdmin);
	}
	
	@Test
	public void testFindUsersByProperty() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("telMobile","39847592"),
				new NameValuePair("gender","Female"),
				new NameValuePair("birthDay", "12/12/2009")
		});
		method.addRequestHeader("Accept-Language", "en");
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<UserVO> vos = parseUserArray(body);
	
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
	}
	
	@Test
	public void testFindAdminByAuth() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		GetMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		method.setQueryString(new NameValuePair[]{
				new NameValuePair("authUsername","administrator"),
				new NameValuePair("authProvider","OLAT")
		});
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<UserVO> vos = parseUserArray(body);
	
		assertNotNull(vos);
		assertFalse(vos.isEmpty());
		assertEquals(1, vos.size());
		assertEquals("administrator",vos.get(0).getLogin());
	}
	
	@Test
	public void testGetUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO vo = parse(body, UserVO.class);

		assertNotNull(vo);
		assertEquals(vo.getKey(), id1.getKey());
		assertEquals(vo.getLogin(), id1.getName());
		//are the properties there?
		assertFalse(vo.getProperties().isEmpty());
	}
	
	@Test
	public void testGetUserNotAdmin() throws IOException {
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		
		HttpMethod method = createGet("/users/" + id2.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO vo = parse(body, UserVO.class);

		assertNotNull(vo);
		assertEquals(vo.getKey(), id2.getKey());
		assertEquals(vo.getLogin(), id2.getName());
		//no properties for security reason
		assertTrue(vo.getProperties().isEmpty());
	}
	
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test
	public void testGetRawJsonUsers() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users", MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String bodyJsons = method.getResponseBodyAsString();
		System.out.println("Users JSON");
		System.out.println(bodyJsons);
		System.out.println("Users JSON");
	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test
	public void testGetRawXmlUsers() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users", MediaType.APPLICATION_XML, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String bodyXmls = method.getResponseBodyAsString();
		System.out.println("Users XML");
		System.out.println(bodyXmls);
		System.out.println("Users XML");
	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test	
	public void testGetRawJsonUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String bodyJson = method.getResponseBodyAsString();
		System.out.println("User");
		System.out.println(bodyJson);
		System.out.println("User");
	}
		
	/**
	 * Only print out the raw body of the response
	 * @throws IOException
	 */
	@Test	
	public void testGetRawXmlUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");	
		HttpMethod method = createGet("/users/" + id1.getKey(), MediaType.APPLICATION_XML, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		String bodyXml = method.getResponseBodyAsString();
		System.out.println("User");
		System.out.println(bodyXml);
		System.out.println("User");
	}
	
	@Test
	public void testCreateUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "Female");//male or female
		vo.putProperty("birthDay", "12/12/2009");

		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);
		method.addRequestHeader("Accept-Language", "en");
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO savedVo = parse(body, UserVO.class);
		
		Identity savedIdent = BaseSecurityManager.getInstance().findIdentityByName(username);

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		assertEquals(savedVo.getLogin(), savedIdent.getName());
		assertEquals("Female", savedIdent.getUser().getProperty("gender", Locale.ENGLISH));
		assertEquals("39847592", savedIdent.getUser().getProperty("telPrivate", Locale.ENGLISH));
		assertEquals("12/12/09", savedIdent.getUser().getProperty("birthDay", Locale.ENGLISH));
	}
	
	/**
	 * Test machine format for gender and date
	 */
	@Test
	public void testCreateUser2() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		UserVO vo = new UserVO();
		String username = UUID.randomUUID().toString();
		vo.setLogin(username);
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail(username + "@frentix.com");
		vo.putProperty("telOffice", "39847592");
		vo.putProperty("telPrivate", "39847592");
		vo.putProperty("telMobile", "39847592");
		vo.putProperty("gender", "female");//male or female
		vo.putProperty("birthDay", "20091212");

		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);
		method.addRequestHeader("Accept-Language", "en");
		
		int code = c.executeMethod(method);
		assertTrue(code == 200 || code == 201);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		UserVO savedVo = parse(body, UserVO.class);
		
		Identity savedIdent = BaseSecurityManager.getInstance().findIdentityByName(username);

		assertNotNull(savedVo);
		assertNotNull(savedIdent);
		assertEquals(savedVo.getKey(), savedIdent.getKey());
		assertEquals(savedVo.getLogin(), savedIdent.getName());
		assertEquals("Female", savedIdent.getUser().getProperty("gender", Locale.ENGLISH));
		assertEquals("39847592", savedIdent.getUser().getProperty("telPrivate", Locale.ENGLISH));
		assertEquals("12/12/09", savedIdent.getUser().getProperty("birthDay", Locale.ENGLISH));
	}
	
	@Test
	public void testCreateUserWithValidationError() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		UserVO vo = new UserVO();
		vo.setLogin("rest-809");
		vo.setFirstName("John");
		vo.setLastName("Smith");
		vo.setEmail("");
		vo.putProperty("gender", "lu");

		String stringuifiedAuth = stringuified(vo);
		PutMethod method = createPut("/users", MediaType.APPLICATION_JSON, true);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
    method.setRequestEntity(entity);
		
		int code = c.executeMethod(method);
		assertTrue(code == 406);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<ErrorVO> errors = parseErrorArray(body);
 		assertNotNull(errors);
		assertFalse(errors.isEmpty());
		assertTrue(errors.size() >= 2);
		assertNotNull(errors.get(0).getCode());
		assertNotNull(errors.get(0).getTranslation());
		assertNotNull(errors.get(1).getCode());
		assertNotNull(errors.get(1).getTranslation());
		
		Identity savedIdent = BaseSecurityManager.getInstance().findIdentityByName("rest-809");
		assertNull(savedIdent);
	}
	
	@Test
	public void testDeleteUser() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//delete an authentication token
		String request = "/users/" + id2.getKey();
		DeleteMethod method = createDelete(request, MediaType.APPLICATION_XML, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		method.releaseConnection();
		
		Identity deletedIdent = BaseSecurityManager.getInstance().loadIdentityByKey(id2.getKey());
		assertNotNull(deletedIdent);//Identity aren't deleted anymore
		assertEquals(Identity.STATUS_DELETED, deletedIdent.getStatus());
	}
	
	@Test
	public void testUserGroup() throws IOException {
		HttpClient c = loginWithCookie("administrator", "olat");
		
		//retrieve all groups
		String request = "/users/" + id1.getKey() + "/groups";
		GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(code, 200);

		String body = method.getResponseBodyAsString();
		List<GroupVO> groups = parseGroupArray(body);
		assertNotNull(groups);
		assertTrue(groups.size() >= 4);
	}
	
	@Test
	public void testPortrait() throws IOException, URISyntaxException {
		URL portraitUrl = RepositoryEntriesTest.class.getResource("portrait.jpg");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		HttpClient c = loginWithCookie("rest-one", "A6B7C8");
		
		//upload portrait
		String request = "/users/" + id1.getKey() + "/portrait";
		PostMethod method = createPost(request, MediaType.APPLICATION_JSON, true);
		method.addRequestHeader("Content-Type", MediaType.MULTIPART_FORM_DATA);
		Part[] parts = { 
				new FilePart("file", portrait),
				new StringPart("filename","portrait.jpg")
		};
		method.setRequestEntity(new MultipartRequestEntity(parts, method.getParams()));
		int code = c.executeMethod(method);
		assertEquals(code, 200);
		method.releaseConnection();
		
		//check if big and small portraits exist
		DisplayPortraitManager dps = DisplayPortraitManager.getInstance();
		File uploadDir = dps.getPortraitDir(id1);
		assertTrue(new File(uploadDir, DisplayPortraitManager.PORTRAIT_SMALL_FILENAME).exists());
		assertTrue(new File(uploadDir, DisplayPortraitManager.PORTRAIT_BIG_FILENAME).exists());
		
		//check get portrait
		String getRequest = "/users/" + id1.getKey() + "/portrait";
		GetMethod getMethod = createGet(getRequest, MediaType.APPLICATION_OCTET_STREAM, true);
		int getCode = c.executeMethod(getMethod);
		assertEquals(getCode, 200);
		InputStream in = getMethod.getResponseBodyAsStream();
		int b = 0;
		int count = 0;
		while((b = in.read()) > -1) {
			count++;
		}
		assertEquals(-1, b);//up to end of file
		assertTrue(count > 1000);//enough bytes
		method.releaseConnection();
	}
	
	protected List<UserVO> parseUserArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<GroupVO> parseGroupArray(String body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<GroupVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}