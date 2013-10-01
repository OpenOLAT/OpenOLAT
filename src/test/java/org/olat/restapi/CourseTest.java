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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import junit.framework.Assert;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.repository.course.CoursesWebService;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.olat.user.restapi.UserVO;

public class CourseTest extends OlatJerseyTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(CourseTest.class);
	
	private Identity admin, auth0, auth1, auth2;
	private ICourse course1;
	private RestConnection conn;

	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		conn = new RestConnection();
		try {
			// create course and persist as OLATResourceImpl
			admin = BaseSecurityManager.getInstance().findIdentityByName("administrator");
			auth0 = JunitTestHelper.createAndPersistIdentityAsUser("rest-zero");
			auth1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
			auth2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
			
			course1 = CoursesWebService.createEmptyCourse(admin, "course1", "course1 long name", null);
			
			DBFactory.getInstance().closeSession();
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
		}
	}
	
  @After
	public void tearDown() throws Exception {
		try {
			if(conn != null) {
				conn.shutdown();
			}
		} catch (Exception e) {
      e.printStackTrace();
      throw e;
		}
	}
	
	@Test
	public void testGetCourse() throws IOException, URISyntaxException {
		assertTrue("Cannot login as administrator", conn.login("administrator", "openolat"));
		
		URI uri = conn.getContextURI().path("repo").path("courses").path(course1.getResourceableId().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseVO course = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertEquals(course1.getResourceableId(), course.getKey());
		assertEquals(course1.getCourseTitle(), course.getTitle());
	}
	
	@Test
	public void testGetCourse_keyRoundTrip() throws IOException, URISyntaxException {
		RepositoryEntry courseRe = RepositoryManager.getInstance().lookupRepositoryEntry(course1, false);
		Assert.assertNotNull(courseRe);
		assertTrue("Cannot login as administrator", conn.login("administrator", "openolat"));
		
		//get repository entry information
		URI repoUri = conn.getContextURI().path("repo").path("entries").path(courseRe.getKey().toString()).build();
		HttpGet repoMethod = conn.createGet(repoUri, MediaType.APPLICATION_JSON, true);
		HttpResponse repoResponse = conn.execute(repoMethod);
		assertEquals(200, repoResponse.getStatusLine().getStatusCode());
		RepositoryEntryVO repoEntry = conn.parse(repoResponse, RepositoryEntryVO.class);
		assertNotNull(repoEntry);
		assertEquals(courseRe.getKey(), repoEntry.getKey());
		assertEquals(course1.getResourceableId(), repoEntry.getOlatResourceId());
		
		//get the course
		URI courseUri = conn.getContextURI().path("repo").path("courses").path(repoEntry.getOlatResourceId().toString()).build();
		HttpGet courseMethod = conn.createGet(courseUri, MediaType.APPLICATION_JSON, true);
		HttpResponse courseResponse = conn.execute(courseMethod);
		assertEquals(200, courseResponse.getStatusLine().getStatusCode());
		CourseVO course = conn.parse(courseResponse, CourseVO.class);
		assertNotNull(course);
		assertEquals(course1.getResourceableId(), course.getKey());
		assertEquals(course1.getCourseTitle(), course.getTitle());
		
	}
	
	@Test
	public void testGetCourseRunStructure() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/runstructure").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String body = EntityUtils.toString(response.getEntity());
		assertNotNull(body);
		assertTrue(body.length() > 100);
		assertTrue(body.indexOf("<org.olat.course.Structure>") >= 0);
	}
	
	@Test
	public void testGetCourseEditorTreeModel() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/editortreemodel").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String body = EntityUtils.toString(response.getEntity());
		assertNotNull(body);
		assertTrue(body.length() > 100);
		assertTrue(body.indexOf("<org.olat.course.tree.CourseEditorTreeModel>") >= 0);
	}
	
	@Test
	public void testDeleteCourses() throws IOException, URISyntaxException {
		ICourse course = CoursesWebService.createEmptyCourse(admin, "courseToDel", "course to delete", null);
		DBFactory.getInstance().intermediateCommit();
		
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<String> courseType = new ArrayList<String>();
		courseType.add(CourseModule.getCourseTypeName());
		Roles roles = new Roles(true, true, true, true, false, true, false);
		//fxdiff VCRP-1,2: access control of resources
		List<RepositoryEntry> repoEntries = RepositoryManager.getInstance().genericANDQueryWithRolesRestriction("*", "*", "*", courseType, null, roles, "");
		assertNotNull(repoEntries);
		
		for(RepositoryEntry entry:repoEntries) {
			assertNotSame(entry.getOlatResource().getResourceableId(), course.getResourceableId());
		}
	}
	
	@Test
	public void testAddAuthor() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/authors/" + auth0.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 author
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		SecurityGroup authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
		boolean isAuthor = securityManager.isIdentityInSecurityGroup(auth0, authorGroup);
		DBFactory.getInstance().intermediateCommit();
		assertTrue(isAuthor);
		
		//is auth0 owner
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry repositoryEntry = rm.lookupRepositoryEntry(course1, true);
		SecurityGroup ownerGroup = repositoryEntry.getOwnerGroup();
		boolean isOwner = securityManager.isIdentityInSecurityGroup(auth0, ownerGroup);
		DBFactory.getInstance().intermediateCommit();
		assertTrue(isOwner);
	}
	
	@Test
	public void testGetAuthors() throws IOException, URISyntaxException {
		//make auth1 and auth2 authors
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		SecurityGroup authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
		if(!securityManager.isIdentityInSecurityGroup(auth1, authorGroup)) {
			securityManager.addIdentityToSecurityGroup(auth1, authorGroup);
		}
		if(!securityManager.isIdentityInSecurityGroup(auth2, authorGroup)) {
			securityManager.addIdentityToSecurityGroup(auth2, authorGroup);
		}
		DBFactory.getInstance().intermediateCommit();
		
		//make auth1 and auth2 owner
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry repositoryEntry = rm.lookupRepositoryEntry(course1, true);
		List<Identity> authors = new ArrayList<Identity>();
		authors.add(auth1);
		authors.add(auth2);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		rm.addOwners(admin, identitiesAddedEvent, repositoryEntry);
		DBFactory.getInstance().intermediateCommit();
		
		//get them
		assertTrue(conn.login("administrator", "openolat"));
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/authors").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		InputStream body = response.getEntity().getContent();
		assertNotNull(body);
		
		List<UserVO> authorVOs = parseUserArray(body);
		assertNotNull(authorVOs);
	}
	
	@Test
	public void testRemoveAuthor() throws IOException, URISyntaxException {
		//make auth1 and auth2 authors
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		SecurityGroup authorGroup = securityManager.findSecurityGroupByName(Constants.GROUP_AUTHORS);
		if(!securityManager.isIdentityInSecurityGroup(auth1, authorGroup)) {
			securityManager.addIdentityToSecurityGroup(auth1, authorGroup);
		}
		if(!securityManager.isIdentityInSecurityGroup(auth2, authorGroup)) {
			securityManager.addIdentityToSecurityGroup(auth2, authorGroup);
		}
		DBFactory.getInstance().intermediateCommit();
		
		//make auth1 and auth2 owner
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry repositoryEntry = rm.lookupRepositoryEntry(course1, true);
		List<Identity> authors = new ArrayList<Identity>();
		authors.add(auth1);
		authors.add(auth2);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		rm.addOwners(admin, identitiesAddedEvent, repositoryEntry);
		DBFactory.getInstance().intermediateCommit();
		//end setup
		
		//test
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/authors/" + auth1.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		URI request2 = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/authors/" + auth2.getKey()).build();
		HttpDelete method2 = conn.createDelete(request2, MediaType.APPLICATION_JSON);
		HttpResponse response2 = conn.execute(method2);
		assertEquals(200, response2.getStatusLine().getStatusCode());
		EntityUtils.consume(response2.getEntity());
		
		//control
		repositoryEntry = rm.lookupRepositoryEntry(course1, true);
		SecurityGroup ownerGroup = repositoryEntry.getOwnerGroup();
		assertFalse(securityManager.isIdentityInSecurityGroup(auth1, ownerGroup));
		assertFalse(securityManager.isIdentityInSecurityGroup(auth2, ownerGroup));
		DBFactory.getInstance().intermediateCommit();
	}
	
	@Test
	public void testAddCoach() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/tutors/" + auth1.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 coach/tutor
		RepositoryManager rm = RepositoryManager.getInstance();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		RepositoryEntry repositoryEntry = rm.lookupRepositoryEntry(course1, true);
		SecurityGroup tutorGroup = repositoryEntry.getTutorGroup();
		boolean isTutor = securityManager.isIdentityInSecurityGroup(auth1, tutorGroup);
		DBFactory.getInstance().intermediateCommit();
		assertTrue(isTutor);
	}
	
	@Test
	public void testAddParticipant() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/participants/" + auth2.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth2 participant
		RepositoryManager rm = RepositoryManager.getInstance();
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		RepositoryEntry repositoryEntry = rm.lookupRepositoryEntry(course1, true);
		SecurityGroup participant = repositoryEntry.getParticipantGroup();
		boolean isParticipant = securityManager.isIdentityInSecurityGroup(auth2, participant);
		DBFactory.getInstance().intermediateCommit();
		assertTrue(isParticipant);
	}
	
	protected List<UserVO> parseUserArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}