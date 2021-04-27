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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.GroupVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * 
 * Description:<br>
 * Test the learning group management of a course
 * 
 * <P>
 * Initial Date:  6 mai 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CourseGroupMgmtTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CourseGroupMgmtTest.class);
	
	private Identity id1, id2;
	private BusinessGroup g1, g2;
	private BusinessGroup g3, g4;
	private RepositoryEntry courseRepoEntry;

	private RestConnection conn;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	
	/**
	 * Set up a course with learn group and group area
	 * @see org.olat.test.OlatRestTestCase#setUp()
	 */
	@Before
	public void setUp() throws Exception {
		conn = new RestConnection();
		//create a course with learn group
		
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-1");
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-2");
		JunitTestHelper.createAndPersistIdentityAsUser("rest-c-g-3");
		Identity auth = JunitTestHelper.createAndPersistIdentityAsUser("rest-course-grp-one");
		
		courseRepoEntry = JunitTestHelper.deployBasicCourse(auth);

		// create groups without waiting list
		g1 = businessGroupService.createBusinessGroup(null, "rest-g1", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, courseRepoEntry);
		g2 = businessGroupService.createBusinessGroup(null, "rest-g2", null, BusinessGroup.BUSINESS_TYPE, 0, 10, false, false, courseRepoEntry);
		// members
		businessGroupRelationDao.addRole(id1, g2, GroupRoles.coach.name());
		businessGroupRelationDao.addRole(id1, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g2, GroupRoles.participant.name());
    
		// groups
		g3 = businessGroupService.createBusinessGroup(null, "rest-g3", null, BusinessGroup.BUSINESS_TYPE, -1, -1, false, false, courseRepoEntry);
		g4 = businessGroupService.createBusinessGroup(null, "rest-g4", null, BusinessGroup.BUSINESS_TYPE, -1, -1, false, false, courseRepoEntry);
		// members
		businessGroupRelationDao.addRole(id1, g3, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(id2, g4, GroupRoles.participant.name());
    
		dbInstance.commitAndCloseSession(); // simulate user clicks
	}
	
  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
			log.error("Exception in tearDown(): " + e);
      throw e;
		}
	}
	
	@Test
	public void testGetCourseGroups() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));

		Long courseId = courseRepoEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<GroupVO> vos = parseGroupArray(response.getEntity());
		assertNotNull(vos);
		assertEquals(4, vos.size());//g1, g2, g3, g4
		
		List<Long> voKeys = new ArrayList<>(4);
		for(GroupVO vo:vos) {
			voKeys.add(vo.getKey());
		}
		assertTrue(voKeys.contains(g1.getKey()));
		assertTrue(voKeys.contains(g2.getKey()));
		assertTrue(voKeys.contains(g3.getKey()));
		assertTrue(voKeys.contains(g4.getKey()));
	}
	
	@Test
	public void testGetCourseGroups_unkownId() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));

		Long courseId = 1l;
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	@Test
	public void testGetCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		Long courseId = courseRepoEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups/" + g1.getKey()).build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupVO vo = conn.parse(response, GroupVO.class);
		assertNotNull(vo);
		assertEquals(g1.getKey(), vo.getKey());
	}
	
	@Test
	public void testPutCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		GroupVO vo = new GroupVO();
		vo.setName("hello");
		vo.setDescription("hello description");
		vo.setMinParticipants(Integer.valueOf(-1));
		vo.setMaxParticipants(Integer.valueOf(-1));
		
		Long courseId = courseRepoEntry.getOlatResource().getResourceableId();
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseId + "/groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		GroupVO responseVo = conn.parse(response, GroupVO.class);
		assertNotNull(responseVo);
		assertEquals(vo.getName(), responseVo.getName());

    BusinessGroup bg = businessGroupService.loadBusinessGroup(responseVo.getKey());
    assertNotNull(bg);
    assertEquals(bg.getKey(), responseVo.getKey());
    assertEquals(bg.getName(), vo.getName());
    assertEquals(bg.getDescription(), vo.getDescription());
    assertNull(bg.getMinParticipants());
    assertNull(bg.getMaxParticipants());
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
		vo.setType("LeanringGroup");
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getOlatResource().getResourceableId() + "/groups/" + g1.getKey()).build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, vo);
		HttpResponse response = conn.execute(method);
		EntityUtils.consume(response.getEntity());
		assertTrue(response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201);
		
    BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey());
    assertNotNull(bg);
    assertEquals(bg.getKey(), vo.getKey());
    assertEquals("rest-g1-mod", bg.getName());
    assertEquals("rest-g1 description", bg.getDescription());
	}
	
	@Test
	public void testDeleteCourseGroup() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getOlatResource().getResourceableId() + "/groups/" + g1.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		EntityUtils.consume(response.getEntity());
		assertEquals(200, response.getStatusLine().getStatusCode());
		
    BusinessGroup bg = businessGroupService.loadBusinessGroup(g1.getKey());
    assertNull(bg);
	}
	
	@Test
	public void testBasicSecurityDeleteCall() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-c-g-3", "A6B7C8"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getOlatResource().getResourceableId() + "/groups/" + g2.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		EntityUtils.consume(response.getEntity());
		
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testBasicSecurityPutCall() throws IOException, URISyntaxException {
		assertTrue(conn.login("rest-c-g-3", "A6B7C8"));
		
		GroupVO vo = new GroupVO();
		vo.setName("hello dont put");
		vo.setDescription("hello description dont put");
		vo.setMinParticipants(Integer.valueOf(-1));
		vo.setMaxParticipants(Integer.valueOf(-1));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + courseRepoEntry.getOlatResource().getResourceableId() + "/groups").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, vo);
		
		HttpResponse response = conn.execute(method);
		EntityUtils.consume(response.getEntity());
		assertEquals(401, response.getStatusLine().getStatusCode());
	}
	
	protected List<GroupVO> parseGroupArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<GroupVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
