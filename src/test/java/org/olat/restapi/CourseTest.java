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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.restapi.support.vo.CourseConfigVO;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.RepositoryEntryEducationalTypeVO;
import org.olat.restapi.support.vo.RepositoryEntryMetadataVO;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.olat.test.VFSJavaIOFile;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CourseTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(CourseTest.class);
	
	private Identity admin, auth0, auth1, auth2;
	private ICourse course1;
	private RestConnection conn;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;

	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() throws Exception {
		conn = new RestConnection();
		try {
			// create course and persist as OLATResourceImpl
			admin = JunitTestHelper.findIdentityByLogin("administrator");
			auth0 = JunitTestHelper.createAndPersistIdentityAsUser("rest-zero");
			auth1 = JunitTestHelper.createAndPersistIdentityAsUser("rest-one");
			auth2 = JunitTestHelper.createAndPersistIdentityAsUser("rest-two");
			
			RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin,
					RepositoryEntryStatusEnum.preparation, false, false);
			course1 = CourseFactory.loadCourse(courseEntry);
			
			dbInstance.closeSession();
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
			log.error("", e);
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
	public void testGetCourseConfig() throws IOException, URISyntaxException {
		assertTrue("Cannot login as administrator", conn.login("administrator", "openolat"));
		
		URI uri = conn.getContextURI().path("repo").path("courses").path(course1.getResourceableId().toString()).path("configuration").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseConfigVO courseConfig = conn.parse(response, CourseConfigVO.class);
		Assert.assertNotNull(courseConfig);
		Assert.assertNotNull(courseConfig.getCssLayoutRef());
		Assert.assertNotNull(courseConfig.getCalendar());
		Assert.assertNotNull(courseConfig.getChat());
		Assert.assertNotNull(courseConfig.getEfficencyStatement());
	}
	
	@Test
	public void updateCourseConfig() throws IOException, URISyntaxException {
		assertTrue("Cannot login as administrator", conn.login("administrator", "openolat"));
		
		CourseConfigVO config = new CourseConfigVO();
		config.setCalendar(Boolean.TRUE);
		config.setChat(Boolean.TRUE);
		config.setEfficencyStatement(Boolean.TRUE);
		config.setCssLayoutRef("pink");

		URI uri = conn.getContextURI().path("repo").path("courses")
				.path(course1.getResourceableId().toString()).path("configuration")
				.build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, config);
		
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseConfigVO courseConfig = conn.parse(response, CourseConfigVO.class);
		Assert.assertNotNull(courseConfig);
		Assert.assertEquals("pink", courseConfig.getCssLayoutRef());
		Assert.assertEquals(Boolean.TRUE, courseConfig.getCalendar());
		Assert.assertEquals(Boolean.TRUE, courseConfig.getChat());
		Assert.assertEquals(Boolean.TRUE, courseConfig.getEfficencyStatement());
	}
	
	@Test
	public void testGetCourse_keyRoundTrip() throws IOException, URISyntaxException {
		RepositoryEntry courseRe = repositoryManager.lookupRepositoryEntry(course1, false);
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
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin,
				RepositoryEntryStatusEnum.preparation, false, false);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.intermediateCommit();
		
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<String> courseType = new ArrayList<>();
		courseType.add(CourseModule.getCourseTypeName());
		Roles roles = Roles.administratorRoles();

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters("*", "*", "*", courseType, null, roles);
		params.setIdentity(admin);
		List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(repoEntries);
		
		for(RepositoryEntry entry:repoEntries) {
			assertNotSame(entry.getOlatResource().getResourceableId(), course.getResourceableId());
		}
	}
	
	@Test
	public void addAuthor() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/authors/" + auth0.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 author
		boolean isAuthor = organisationService.hasRole(auth0, OrganisationRoles.author);
		dbInstance.intermediateCommit();
		Assert.assertTrue(isAuthor);
		
		//is auth0 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		boolean isOwner = repositoryService.hasRole(auth0, repositoryEntry, GroupRoles.owner.name());
		dbInstance.intermediateCommit();
		Assert.assertTrue(isOwner);
	}
	
	@Test
	public void addAuthors() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Identity author1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-auth-1");
		Identity author2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-auth-2");
		dbInstance.commitAndCloseSession();
		
		UserVO[] newAuthors = new UserVO[2];
		newAuthors[0] = UserVOFactory.get(author1);
		newAuthors[1] = UserVOFactory.get(author2);

		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("authors").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newAuthors);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 author
		boolean isAuthor1 = organisationService.hasRole(author1, OrganisationRoles.author);
		boolean isAuthor2 = organisationService.hasRole(author2, OrganisationRoles.author);
		dbInstance.commit();
		Assert.assertTrue(isAuthor1);
		Assert.assertTrue(isAuthor2);
		
		//is auth0 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		boolean isOwner1 = repositoryService.hasRole(author1, repositoryEntry, GroupRoles.owner.name());
		boolean isOwner2 = repositoryService.hasRole(author2, repositoryEntry, GroupRoles.owner.name());
		dbInstance.commit();
		Assert.assertTrue(isOwner1);
		Assert.assertTrue(isOwner2);
	}
	
	@Test
	public void getAuthors() throws IOException, URISyntaxException {
		//make auth1 and auth2 authors
		organisationService.addMember(auth1, OrganisationRoles.author);
		organisationService.addMember(auth2, OrganisationRoles.author);
		dbInstance.commitAndCloseSession();
		
		//make auth1 and auth2 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		List<Identity> authors = new ArrayList<>();
		authors.add(auth1);
		authors.add(auth2);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		repositoryManager.addOwners(admin, identitiesAddedEvent, repositoryEntry, null);
		dbInstance.commitAndCloseSession();
		
		//get them
		assertTrue(conn.login("administrator", "openolat"));
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/authors").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> authorVOs = parseUserArray(response.getEntity());
		Assert.assertNotNull(authorVOs);
	}
	
	@Test
	public void removeAuthor() throws IOException, URISyntaxException {
		//make auth1 and auth2 authors
		organisationService.addMember(auth1, OrganisationRoles.author);
		organisationService.addMember(auth2, OrganisationRoles.author);
		dbInstance.commitAndCloseSession();
		
		//make auth1 and auth2 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		List<Identity> authors = new ArrayList<>();
		authors.add(auth1);
		authors.add(auth2);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		repositoryManager.addOwners(admin, identitiesAddedEvent, repositoryEntry, null);
		dbInstance.commitAndCloseSession();
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
		repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		assertFalse(repositoryService.hasRole(auth1, repositoryEntry, GroupRoles.owner.name()));
		assertFalse(repositoryService.hasRole(auth2, repositoryEntry, GroupRoles.owner.name()));
		dbInstance.intermediateCommit();
	}
	
	@Test
	public void getTutors() throws IOException, URISyntaxException {
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-coach");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		repositoryService.addRole(coach, repositoryEntry, GroupRoles.coach.name());
		dbInstance.intermediateCommit();
		
		//get them
		Assert.assertTrue(conn.login("administrator", "openolat"));
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/tutors").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> tutorVOs = parseUserArray(response.getEntity());
		Assert.assertNotNull(tutorVOs);
		boolean found = false;
		for(UserVO tutorVo:tutorVOs) {
			if(tutorVo.getKey().equals(coach.getKey())) {
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void addCoach() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/tutors/" + auth1.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 coach/tutor
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		boolean isTutor = repositoryService.hasRole(auth1, repositoryEntry, GroupRoles.coach.name());
		dbInstance.intermediateCommit();
		assertTrue(isTutor);
	}
	
	@Test
	public void removeCoach() throws IOException, URISyntaxException {
		//add a coach
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-coach");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		repositoryService.addRole(coach, repositoryEntry, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		boolean isTutor = repositoryService.hasRole(coach, repositoryEntry, GroupRoles.coach.name());
		Assert.assertTrue(isTutor);
		
		//test remove
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/tutors/" + coach.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check database
		boolean deletedCoach = repositoryService.hasRole(coach, repositoryEntry, GroupRoles.coach.name());
		Assert.assertFalse(deletedCoach);
	}

	@Test
	public void addCoaches() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Identity coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-coach-1");
		Identity coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-coach-2");
		dbInstance.commitAndCloseSession();
		
		//add the 2 participants to the course
		UserVO[] newCoaches = new UserVO[2];
		newCoaches[0] = UserVOFactory.get(coach1);
		newCoaches[1] = UserVOFactory.get(coach2);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("tutors").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newCoaches);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 coach/tutor
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		boolean isTutor1 = repositoryService.hasRole(coach1, repositoryEntry, GroupRoles.coach.name());
		boolean isTutor2 = repositoryService.hasRole(coach2, repositoryEntry, GroupRoles.coach.name());
		dbInstance.commit();
		Assert.assertTrue(isTutor1);
		Assert.assertTrue(isTutor2);
	}
	
	
	@Test
	public void getParticipants() throws IOException, URISyntaxException {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-participant");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		repositoryService.addRole(participant, repositoryEntry, GroupRoles.participant.name());
		dbInstance.intermediateCommit();
		
		//get them
		Assert.assertTrue(conn.login("administrator", "openolat"));
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/participants").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> participantVOs = parseUserArray(response.getEntity());
		Assert.assertNotNull(participantVOs);
		boolean found = false;
		for(UserVO participantVo:participantVOs) {
			if(participantVo.getKey().equals(participant.getKey())) {
				found = true;
			}
		}
		Assert.assertTrue(found);
	}
	
	@Test
	public void addParticipant() throws IOException, URISyntaxException {
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/participants/" + auth2.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth2 participant
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		boolean isParticipant = repositoryService.hasRole(auth2, repositoryEntry, GroupRoles.participant.name());
		dbInstance.commit();
		Assert.assertTrue(isParticipant);
	}
	
	@Test
	public void removeParticipant() throws IOException, URISyntaxException {
		//add a coach
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-part");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course1, true);
		repositoryService.addRole(participant, repositoryEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		boolean isParticipant = repositoryService.hasRole(participant, repositoryEntry, GroupRoles.participant.name());
		Assert.assertTrue(isParticipant);
		
		//test remove
		assertTrue(conn.login("administrator", "openolat"));
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course1.getResourceableId() + "/participants/" + participant.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check database
		boolean stillParticipant = repositoryService.hasRole(participant, repositoryEntry, GroupRoles.participant.name());
		Assert.assertFalse(stillParticipant);
	}
	
	@Test
	public void addParticipants() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-part-1");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-part-2");
		dbInstance.commitAndCloseSession();
		
		//add the 2 participants to the course
		UserVO[] newParticipants = new UserVO[2];
		newParticipants[0] = UserVOFactory.get(participant1);
		newParticipants[1] = UserVOFactory.get(participant2);

		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("participants").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, newParticipants);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check that they are course members
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		boolean isParticipant1 = repositoryService.hasRole(participant1, repositoryEntry, GroupRoles.participant.name());
		boolean isParticipant2 = repositoryService.hasRole(participant2, repositoryEntry, GroupRoles.participant.name());
		dbInstance.commit();
		Assert.assertTrue(isParticipant1);
		Assert.assertTrue(isParticipant2);
	}
	
	@Test
	public void getMetadata() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntryEducationalType> educationalTypes = repositoryManager.getAllEducationalTypes();
		RepositoryEntryEducationalType educationalType = educationalTypes.get(0);
		courseEntry = repositoryManager.setDescriptionAndName(courseEntry, courseEntry.getDisplayname(), "Course ref.", "Course authors",
				"Course description", "Course teaser", "Course objectives", "Course requirements", "Course credits", "DE", "Zurich", "5 days",
				null, null, null, educationalType);
		dbInstance.commitAndCloseSession();
		course.getCourseEnvironment().updateCourseEntry(courseEntry);

		//remove the owner
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString()).path("metadata").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryMetadataVO metadataVo = conn.parse(response, RepositoryEntryMetadataVO.class);
		conn.shutdown();
		
		//check
		Assert.assertNotNull(metadataVo);
		Assert.assertEquals(courseEntry.getKey(), metadataVo.getKey());
		Assert.assertEquals(courseEntry.getDisplayname(), metadataVo.getDisplayname());
		Assert.assertEquals("Course ref.", metadataVo.getExternalRef());
		Assert.assertEquals("Course authors", metadataVo.getAuthors());
		Assert.assertEquals("Course description", metadataVo.getDescription());
		Assert.assertEquals("Course teaser", metadataVo.getTeaser());
		Assert.assertEquals("Course objectives", metadataVo.getObjectives());
		Assert.assertEquals("Course requirements", metadataVo.getRequirements());
		Assert.assertEquals("Course credits", metadataVo.getCredits());
		Assert.assertEquals("DE", metadataVo.getMainLanguage());
		Assert.assertEquals("Zurich", metadataVo.getLocation());
		Assert.assertEquals("5 days", metadataVo.getExpenditureOfWork());
		
		RepositoryEntryEducationalTypeVO educationTypeVo = metadataVo.getEducationalType();
		Assert.assertNotNull(educationTypeVo);
		Assert.assertEquals(educationalType.getKey(), educationTypeVo.getKey());
		Assert.assertEquals(educationalType.getIdentifier(), educationTypeVo.getIdentifier());
	}
	
	@Test
	public void headCourseImage() throws IOException, URISyntaxException {
		URL imageUrl = CourseTest.class.getResource("portrait.jpg");
		Assert.assertNotNull(imageUrl);
		File image = new File(imageUrl.toURI());
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		repositoryManager.setImage(new VFSJavaIOFile(image), courseEntry, admin);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		//remove the owner
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString())
				.path("image").build();
		HttpHead method = conn.createHead(request, "image/jpg", true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	@Test
	public void getCourseImage() throws IOException, URISyntaxException {
		URL imageUrl = CourseTest.class.getResource("portrait.jpg");
		Assert.assertNotNull(imageUrl);
		File image = new File(imageUrl.toURI());
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		repositoryManager.setImage(new VFSJavaIOFile(image), courseEntry, admin);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		//remove the owner
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString())
				.path("image").build();
		HttpGet method = conn.createGet(request, "image/jpg", true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] imageArr = EntityUtils.toByteArray(response.getEntity());
		Assert.assertNotNull(imageArr);
		Assert.assertEquals(image.length(), imageArr.length);
	}
	
	@Test
	public void postCourseImage() throws IOException, URISyntaxException {
		URL imageUrl = CourseTest.class.getResource("portrait.jpg");
		Assert.assertNotNull(imageUrl);
		File image = new File(imageUrl.toURI());
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		//remove the owner
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString())
				.path("image").build();
		
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addMultipart(method, "image.jpg", image);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		VFSLeaf imageLeaf = repositoryManager.getImage(courseEntry);
		Assert.assertNotNull(imageLeaf);
		// Because the image is small, it's not scaled
		Assert.assertEquals(image.length(), imageLeaf.getSize());
	}
	
	@Test
	public void updateStatus() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();

		// Set the course to coach published
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString())
				.path("status")
				.queryParam("newStatus", RepositoryEntryStatusEnum.review.name())
				.build();
		
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RepositoryEntry updatedEntry = repositoryService.loadByKey(courseEntry.getKey());
		Assert.assertEquals(RepositoryEntryStatusEnum.review, updatedEntry.getEntryStatus());
	}
	
	@Test
	public void updateMetadata() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();
		
		assertTrue(conn.login("administrator", "openolat"));
		
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString()).path("metadata").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryMetadataVO metadataVo = conn.parse(response, RepositoryEntryMetadataVO.class);
		
		// fill the metadata
		metadataVo.setAuthors("Kurs Authors");
		metadataVo.setCredits("Kurs credits");
		metadataVo.setDescription("Kurs Beschreibung");
		metadataVo.setExpenditureOfWork("4 weeks");
		metadataVo.setExternalRef("Kurs Reference");
		metadataVo.setLocation("Solothurn");
		metadataVo.setMainLanguage("English");
		metadataVo.setObjectives("Our objectives");
		metadataVo.setRequirements("Their requirements");
		
		List<RepositoryEntryEducationalType> educationalTypes = repositoryManager.getAllEducationalTypes();
		RepositoryEntryEducationalType educationalType = educationalTypes.get(0);
		metadataVo.setEducationalType(RepositoryEntryEducationalTypeVO.valueOf(educationalType));

		URI updateRequest = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString()).path("metadata").build();
		HttpPost updateMethod = conn.createPost(updateRequest, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(updateMethod, metadataVo);
		HttpResponse updateResponse = conn.execute(updateMethod);
		Assert.assertEquals(200, updateResponse.getStatusLine().getStatusCode());
		RepositoryEntryMetadataVO updatedMetadataVo = conn.parse(updateResponse, RepositoryEntryMetadataVO.class);

		//check the response
		Assert.assertNotNull(metadataVo);
		Assert.assertEquals(courseEntry.getKey(), updatedMetadataVo.getKey());
		Assert.assertEquals(courseEntry.getDisplayname(), updatedMetadataVo.getDisplayname());
		Assert.assertEquals("Kurs Reference", updatedMetadataVo.getExternalRef());
		Assert.assertEquals("Kurs Authors", updatedMetadataVo.getAuthors());
		Assert.assertEquals("Kurs Beschreibung", updatedMetadataVo.getDescription());
		Assert.assertEquals("Our objectives", updatedMetadataVo.getObjectives());
		Assert.assertEquals("Their requirements", updatedMetadataVo.getRequirements());
		Assert.assertEquals("Kurs credits", updatedMetadataVo.getCredits());
		Assert.assertEquals("English", updatedMetadataVo.getMainLanguage());
		Assert.assertEquals("Solothurn", updatedMetadataVo.getLocation());
		Assert.assertEquals("4 weeks", updatedMetadataVo.getExpenditureOfWork());
		
		RepositoryEntryEducationalTypeVO educationTypeVo = updatedMetadataVo.getEducationalType();
		Assert.assertNotNull(educationTypeVo);
		Assert.assertEquals(educationalType.getKey(), educationTypeVo.getKey());
		Assert.assertEquals(educationalType.getIdentifier(), educationTypeVo.getIdentifier());

		RepositoryEntry updatedRe = repositoryService.loadByKey(courseEntry.getKey());
		Assert.assertEquals(courseEntry.getKey(), updatedRe.getKey());
		Assert.assertEquals(courseEntry.getDisplayname(), updatedRe.getDisplayname());
		Assert.assertEquals("Kurs Reference", updatedRe.getExternalRef());
		Assert.assertEquals("Kurs Authors", updatedRe.getAuthors());
		Assert.assertEquals("Kurs Beschreibung", updatedRe.getDescription());
		Assert.assertEquals("Our objectives", updatedRe.getObjectives());
		Assert.assertEquals("Their requirements", updatedRe.getRequirements());
		Assert.assertEquals("Kurs credits", updatedRe.getCredits());
		Assert.assertEquals("English", updatedRe.getMainLanguage());
		Assert.assertEquals("Solothurn", updatedRe.getLocation());
		Assert.assertEquals("4 weeks", updatedRe.getExpenditureOfWork());
		Assert.assertEquals(educationalType, updatedRe.getEducationalType());
	}
	
	@Test
	public void changedStatus_closed() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse courseToClose = CourseFactory.loadCourse(courseEntry);
		dbInstance.closeSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseToClose.getResourceableId().toString()).path("status").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addEntity(method, new BasicNameValuePair("newStatus", "closed"));
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(courseToClose, true);
		Assert.assertEquals(RepositoryEntryStatusEnum.closed, repositoryEntry.getEntryStatus());
	}
	
	@Test
	public void changedStatus_deleted() throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(admin);
		ICourse courseToClose = CourseFactory.loadCourse(courseEntry);
		dbInstance.closeSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseToClose.getResourceableId().toString()).path("status").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addEntity(method, new BasicNameValuePair("newStatus", "deleted"));
		
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(courseToClose, true);
		Assert.assertEquals(RepositoryEntryStatusEnum.trash, repositoryEntry.getEntryStatus());
	}
	
	@Test
	public void exportCourse()
	throws IOException, URISyntaxException {
		Assert.assertTrue(conn.login("administrator", "openolat"));
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("course-owner");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author);
		dbInstance.closeSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getOlatResource().getResourceableId().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);	
	}
	
	@Test
	public void exportCourse_owner()
	throws IOException, URISyntaxException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndUser("course-owner-2");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author.getIdentity());
		dbInstance.closeSession();
		
		Assert.assertTrue(conn.login(author));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getOlatResource().getResourceableId().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);	
	}
	
	@Test
	public void exportCourse_notOwner()
	throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("course-owner-3");
		IdentityWithLogin otherUser = JunitTestHelper.createAndPersistRndUser("course-owner-4");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(owner);
		dbInstance.closeSession();
		
		Assert.assertTrue(conn.login(otherUser));
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getOlatResource().getResourceableId().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	protected List<UserVO> parseUserArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<UserVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}