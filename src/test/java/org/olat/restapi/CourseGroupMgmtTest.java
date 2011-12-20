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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;


/**
 * 
 * Description:<br>
 * Test the learning group management of a course
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseGroupMgmtTest extends OlatJerseyTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(CourseGroupMgmtTest.class);
	
	private Identity id1, id2;
	private BusinessGroup g1, g2;
	private BusinessGroup g3, g4;
	private OLATResource course;
	
	/**
	 * Set up a course with learn group and group area
	 * @see org.olat.test.OlatJerseyTestCase#setUp()
	 */
	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		//create a course with learn group
		
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-1");
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-2");
		JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-3");
		
		OLATResourceManager rm = OLATResourceManager.getInstance();
		// create course and persist as OLATResourceImpl
		OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitcourse",System.currentTimeMillis());
		course =  rm.createOLATResourceInstance(resourceable);
		DBFactory.getInstance().saveObject(course);
		DBFactory.getInstance().closeSession();
		
		//create learn group

    BGContextManager cm = BGContextManagerImpl.getInstance();
    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
    BaseSecurity secm = BaseSecurityManager.getInstance();
		
    // 1) context one: learning groups
    BGContext c1 = cm.createAndAddBGContextToResource("c1name-learn", course, BusinessGroup.TYPE_LEARNINGROUP, id1, true);
    // create groups without waiting list
    g1 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g1", null, new Integer(0), new Integer(10), false, false, c1);
    g2 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_LEARNINGROUP, null, "rest-g2", null, new Integer(0), new Integer(10), false, false, c1);
    // members
    secm.addIdentityToSecurityGroup(id1, g2.getOwnerGroup());
    secm.addIdentityToSecurityGroup(id1, g1.getPartipiciantGroup());
    secm.addIdentityToSecurityGroup(id2, g1.getPartipiciantGroup());
    secm.addIdentityToSecurityGroup(id2, g2.getPartipiciantGroup());
    
    
    // 2) context two: right groups
    BGContext c2 = cm.createAndAddBGContextToResource("c2name-area", course, BusinessGroup.TYPE_RIGHTGROUP, id2, true);
    // groups
    g3 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g3", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
    g4 = bgm.createAndPersistBusinessGroup(BusinessGroup.TYPE_RIGHTGROUP, null, "rest-g4", null, null, null, null/* enableWaitinglist */, null/* enableAutoCloseRanks */, c2);
    // members
    secm.addIdentityToSecurityGroup(id1, g3.getPartipiciantGroup());
    secm.addIdentityToSecurityGroup(id2, g4.getPartipiciantGroup());
    
    DBFactory.getInstance().closeSession(); // simulate user clicks
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
	public void testGetCourseGroups() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		String request = "/repo/courses/" + course.getResourceableId() + "/groups";
		GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		List<GroupVO> vos = parseGroupArray(body);
		assertNotNull(vos);
		assertEquals(2, vos.size());//g1 and g2	
		assertTrue(vos.get(0).getKey().equals(g1.getKey()) || vos.get(0).getKey().equals(g2.getKey()));
		assertTrue(vos.get(1).getKey().equals(g1.getKey()) || vos.get(1).getKey().equals(g2.getKey()));
	}
	
	@Test
	public void testGetCourseGroup() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		String request = "/repo/courses/" + course.getResourceableId() + "/groups/" + g1.getKey();
		GetMethod method = createGet(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		GroupVO vo = parse(body, GroupVO.class);
		assertNotNull(vo);
		assertEquals(g1.getKey(), vo.getKey());
	}
	
	@Test
	public void testPutCourseGroup() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		GroupVO vo = new GroupVO();
		vo.setName("hello");
		vo.setDescription("hello description");
		vo.setMinParticipants(new Integer(-1));
		vo.setMaxParticipants(new Integer(-1));
		
		String stringuifiedAuth = stringuified(vo);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
		String request = "/repo/courses/" + course.getResourceableId() + "/groups";
		PutMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
		method.setRequestEntity(entity);
		
		int code = c.executeMethod(method);
		assertEquals(200, code);
		String body = method.getResponseBodyAsString();
		method.releaseConnection();
		GroupVO responseVo = parse(body, GroupVO.class);
		assertNotNull(responseVo);
		assertEquals(vo.getName(), responseVo.getName());

    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
    BusinessGroup bg = bgm.loadBusinessGroup(responseVo.getKey(), false);
    assertNotNull(bg);
    assertEquals(bg.getKey(), responseVo.getKey());
    assertEquals(bg.getName(), vo.getName());
    assertEquals(bg.getDescription(), vo.getDescription());
    assertNull(bg.getMinParticipants());
    assertNull(bg.getMaxParticipants());
	}
	
	@Test
	public void testUpdateCourseGroup() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		GroupVO vo = new GroupVO();
		vo.setKey(g1.getKey());
		vo.setName("rest-g1-mod");
		vo.setDescription("rest-g1 description");
		vo.setMinParticipants(g1.getMinParticipants());
		vo.setMaxParticipants(g1.getMaxParticipants());
		vo.setType(g1.getType());
		
		String stringuifiedAuth = stringuified(vo);
    RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
		String request = "/repo/courses/" + course.getResourceableId() + "/groups/" + g1.getKey();
		PostMethod method = createPost(request, MediaType.APPLICATION_JSON, true);
		method.setRequestEntity(entity);
		int code = c.executeMethod(method);
		method.releaseConnection();
		assertTrue(code == 200 || code == 201);
		
    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
    BusinessGroup bg = bgm.loadBusinessGroup(g1.getKey(), false);
    assertNotNull(bg);
    assertEquals(bg.getKey(), vo.getKey());
    assertEquals("rest-g1-mod", bg.getName());
    assertEquals("rest-g1 description", bg.getDescription());
	}
	
	@Test
	public void testDeleteCourseGroup() throws IOException {
		HttpClient c = loginWithCookie("administrator", "openolat");
		
		String request = "/repo/courses/" + course.getResourceableId() + "/groups/" + g1.getKey();
		DeleteMethod method = createDelete(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		method.releaseConnection();
		assertEquals(200, code);
		
    BusinessGroupManager bgm = BusinessGroupManagerImpl.getInstance();
    BusinessGroup bg = bgm.loadBusinessGroup(g1.getKey(), false);
    assertNull(bg);
	}
	
	@Test
	public void testBasicSecurityDeleteCall() throws IOException {
		HttpClient c = loginWithCookie("rest-c-g-3", "A6B7C8");
		
		String request = "/repo/courses/" + course.getResourceableId() + "/groups/" + g2.getKey();
		DeleteMethod method = createDelete(request, MediaType.APPLICATION_JSON, true);
		int code = c.executeMethod(method);
		method.releaseConnection();
		
		assertEquals(401, code);
	}
	
	@Test
	public void testBasicSecurityPutCall() throws IOException {
		HttpClient c = loginWithCookie("rest-c-g-3", "A6B7C8");
		
		GroupVO vo = new GroupVO();
		vo.setName("hello dont put");
		vo.setDescription("hello description dont put");
		vo.setMinParticipants(new Integer(-1));
		vo.setMaxParticipants(new Integer(-1));
		
		String stringuifiedAuth = stringuified(vo);
	  RequestEntity entity = new StringRequestEntity(stringuifiedAuth, MediaType.APPLICATION_JSON, "UTF-8");
		String request = "/repo/courses/" + course.getResourceableId() + "/groups";
		PutMethod method = createPut(request, MediaType.APPLICATION_JSON, true);
		method.setRequestEntity(entity);
		int code = c.executeMethod(method);
		
		assertEquals(401, code);
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
