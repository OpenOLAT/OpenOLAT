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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
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

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	
	private static Organisation defaultUnitTestOrganisation;
	private static IdentityWithLogin defaultUnitTestAdministrator;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-service-unit-test", "Org-service-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper
					.createAndPersistRndAdmin("Cur-Elem-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void testGetCourse() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-1", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		URI uri = conn.getContextURI().path("repo").path("courses").path(course.getResourceableId().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseVO courseVo = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertEquals(course.getResourceableId(), courseVo.getKey());
		assertEquals(course.getCourseTitle(), courseVo.getTitle());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCourseConfig() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-2", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		URI uri = conn.getContextURI().path("repo").path("courses").path(course.getResourceableId().toString()).path("configuration").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		CourseConfigVO courseConfig = conn.parse(response, CourseConfigVO.class);
		Assert.assertNotNull(courseConfig);
		Assert.assertNotNull(courseConfig.getCssLayoutRef());
		Assert.assertNotNull(courseConfig.getCalendar());
		Assert.assertNotNull(courseConfig.getChat());
		Assert.assertNotNull(courseConfig.getEfficencyStatement());
		
		conn.shutdown();
	}
	
	@Test
	public void updateCourseConfig() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-3", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		CourseConfigVO config = new CourseConfigVO();
		config.setCalendar(Boolean.TRUE);
		config.setChat(Boolean.TRUE);
		config.setEfficencyStatement(Boolean.TRUE);
		config.setCssLayoutRef("pink");

		URI uri = conn.getContextURI().path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("configuration")
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
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCourse_keyRoundTrip() throws IOException, URISyntaxException {
		RepositoryEntry courseRe = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-4", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseRe);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		//get repository entry information
		URI repoUri = conn.getContextURI().path("repo").path("entries").path(courseRe.getKey().toString()).build();
		HttpGet repoMethod = conn.createGet(repoUri, MediaType.APPLICATION_JSON, true);
		HttpResponse repoResponse = conn.execute(repoMethod);
		assertEquals(200, repoResponse.getStatusLine().getStatusCode());
		RepositoryEntryVO repoEntry = conn.parse(repoResponse, RepositoryEntryVO.class);
		assertNotNull(repoEntry);
		assertEquals(courseRe.getKey(), repoEntry.getKey());
		assertEquals(course.getResourceableId(), repoEntry.getOlatResourceId());
		
		//get the course
		URI courseUri = conn.getContextURI().path("repo").path("courses").path(repoEntry.getOlatResourceId().toString()).build();
		HttpGet courseMethod = conn.createGet(courseUri, MediaType.APPLICATION_JSON, true);
		HttpResponse courseResponse = conn.execute(courseMethod);
		assertEquals(200, courseResponse.getStatusLine().getStatusCode());
		CourseVO courseVo = conn.parse(courseResponse, CourseVO.class);
		assertNotNull(course);
		assertEquals(course.getResourceableId(), courseVo.getKey());
		assertEquals(course.getCourseTitle(), courseVo.getTitle());
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCourseRunStructure() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-5", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/runstructure").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String body = EntityUtils.toString(response.getEntity());
		assertNotNull(body);
		assertTrue(body.length() > 100);
		assertTrue(body.indexOf("<org.olat.course.Structure>") >= 0);
		
		conn.shutdown();
	}
	
	@Test
	public void testGetCourseEditorTreeModel() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-6", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/editortreemodel").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_XML, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		String body = EntityUtils.toString(response.getEntity());
		assertNotNull(body);
		assertTrue(body.length() > 100);
		assertTrue(body.indexOf("<org.olat.course.tree.CourseEditorTreeModel>") >= 0);
		
		conn.shutdown();
	}
	
	@Test
	public void testDeleteCourses() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-7", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.intermediateCommit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<String> courseType = new ArrayList<>();
		courseType.add(CourseModule.getCourseTypeName());
		Roles roles = Roles.administratorRoles();

		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters("*", "*", "*", courseType, null, roles);
		params.setIdentity(defaultUnitTestAdministrator.getIdentity());
		List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, true);
		assertNotNull(repoEntries);
		
		for(RepositoryEntry entry:repoEntries) {
			assertNotSame(entry.getOlatResource().getResourceableId(), course.getResourceableId());
		}
		
		conn.shutdown();
	}
	
	@Test
	public void addAuthor() throws IOException, URISyntaxException {
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-zero");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-8", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/authors/" + auth.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 author
		boolean isAuthor = organisationService.hasRole(auth, OrganisationRoles.author);
		dbInstance.intermediateCommit();
		Assert.assertTrue(isAuthor);
		
		//is auth0 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		boolean isOwner = repositoryService.hasRole(auth, repositoryEntry, GroupRoles.owner.name());
		dbInstance.intermediateCommit();
		Assert.assertTrue(isOwner);
		
		conn.shutdown();
	}
	
	@Test
	public void addAuthors() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Identity author1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-auth-1");
		Identity author2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-auth-2");
		dbInstance.commitAndCloseSession();
		
		UserVO[] newAuthors = new UserVO[2];
		newAuthors[0] = UserVOFactory.get(author1);
		newAuthors[1] = UserVOFactory.get(author2);

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
		
		conn.shutdown();
	}
	
	@Test
	public void getAuthors() throws IOException, URISyntaxException {
		Identity auth1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-4");
		Identity auth2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-5");
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-9", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		//make auth1 and auth2 authors
		organisationService.addMember(auth1, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		organisationService.addMember(auth2, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//make auth1 and auth2 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		List<Identity> authors = new ArrayList<>();
		authors.add(auth1);
		authors.add(auth2);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		repositoryManager.addOwners(defaultUnitTestAdministrator.getIdentity(), identitiesAddedEvent, repositoryEntry, null);
		dbInstance.commitAndCloseSession();
		
		//get them
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/authors").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<UserVO> authorVOs = parseUserArray(response.getEntity());
		Assert.assertNotNull(authorVOs);
		
		conn.shutdown();
	}
	
	@Test
	public void removeAuthor() throws IOException, URISyntaxException {
		Identity auth1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-6");
		Identity auth2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-7");
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-10", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		//make auth1 and auth2 authors
		organisationService.addMember(auth1, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		organisationService.addMember(auth2, OrganisationRoles.author, JunitTestHelper.getDefaultActor());
		dbInstance.commitAndCloseSession();
		
		//make auth1 and auth2 owner
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		List<Identity> authors = new ArrayList<>();
		authors.add(auth1);
		authors.add(auth2);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(authors);
		repositoryManager.addOwners(defaultUnitTestAdministrator.getIdentity(), identitiesAddedEvent, repositoryEntry, null);
		dbInstance.commitAndCloseSession();
		//end setup
		
		//test
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/authors/" + auth1.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		URI request2 = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/authors/" + auth2.getKey()).build();
		HttpDelete method2 = conn.createDelete(request2, MediaType.APPLICATION_JSON);
		HttpResponse response2 = conn.execute(method2);
		assertEquals(200, response2.getStatusLine().getStatusCode());
		EntityUtils.consume(response2.getEntity());
		
		//control
		repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		assertFalse(repositoryService.hasRole(auth1, repositoryEntry, GroupRoles.owner.name()));
		assertFalse(repositoryService.hasRole(auth2, repositoryEntry, GroupRoles.owner.name()));
		dbInstance.intermediateCommit();
		
		conn.shutdown();
	}
	
	@Test
	public void getTutors() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-11", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-coach");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		repositoryService.addRole(coach, repositoryEntry, GroupRoles.coach.name());
		dbInstance.intermediateCommit();
		
		//get them
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/tutors").build();
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
		
		conn.shutdown();
	}
	
	@Test
	public void addCoach() throws IOException, URISyntaxException {
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-8");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-12", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/tutors/" + auth.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth0 coach/tutor
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		boolean isTutor = repositoryService.hasRole(auth, repositoryEntry, GroupRoles.coach.name());
		dbInstance.intermediateCommit();
		assertTrue(isTutor);
		
		conn.shutdown();
	}
	
	@Test
	public void removeCoach() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-14", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		//add a coach
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-coach");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		repositoryService.addRole(coach, repositoryEntry, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		boolean isTutor = repositoryService.hasRole(coach, repositoryEntry, GroupRoles.coach.name());
		Assert.assertTrue(isTutor);
		
		//test remove
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/tutors/" + coach.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check database
		boolean deletedCoach = repositoryService.hasRole(coach, repositoryEntry, GroupRoles.coach.name());
		Assert.assertFalse(deletedCoach);
		
		conn.shutdown();
	}

	@Test
	public void addCoaches() throws IOException, URISyntaxException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");

		RestConnection conn = new RestConnection("administrator", "openolat");
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
		
		conn.shutdown();
	}
	
	
	@Test
	public void getParticipants() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-15", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-participant");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		repositoryService.addRole(participant, repositoryEntry, GroupRoles.participant.name());
		dbInstance.intermediateCommit();
		
		//get them
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/participants").build();
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
		
		conn.shutdown();
	}
	
	@Test
	public void addParticipant() throws IOException, URISyntaxException {
		Identity auth = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-11");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-16", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/participants/" + auth.getKey()).build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());

		//is auth2 participant
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		boolean isParticipant = repositoryService.hasRole(auth, repositoryEntry, GroupRoles.participant.name());
		dbInstance.commit();
		Assert.assertTrue(isParticipant);
		
		conn.shutdown();
	}
	
	@Test
	public void removeParticipant() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				"REST-Course-17", defaultUnitTestOrganisation, RepositoryEntryStatusEnum.preparation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		//add a coach
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("Course-part");
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(course, true);
		repositoryService.addRole(participant, repositoryEntry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		boolean isParticipant = repositoryService.hasRole(participant, repositoryEntry, GroupRoles.participant.name());
		Assert.assertTrue(isParticipant);
		
		//test remove
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses/" + course.getResourceableId() + "/participants/" + participant.getKey()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check database
		boolean stillParticipant = repositoryService.hasRole(participant, repositoryEntry, GroupRoles.participant.name());
		Assert.assertFalse(stillParticipant);
		
		conn.shutdown();
	}
	
	@Test
	public void addParticipants() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
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
		
		conn.shutdown();
	}
	
	@Test
	public void getMetadata() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		
		List<RepositoryEntryEducationalType> educationalTypes = repositoryManager.getAllEducationalTypes();
		RepositoryEntryEducationalType educationalType = educationalTypes.get(0);
		courseEntry = repositoryManager.setDescriptionAndName(courseEntry, courseEntry.getDisplayname(), "Course ref.", "Course authors",
				"Course description", "Course teaser", "Course objectives", "Course requirements", "Course credits", "DE", "Zurich", "5 days",
				null, null, null, educationalType);
		Assert.assertEquals("Course ref.", courseEntry.getExternalRef());

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
		
		conn.shutdown();
	}
	
	@Test
	public void headCourseImage() throws IOException, URISyntaxException {
		URL imageUrl = CourseTest.class.getResource("portrait.jpg");
		Assert.assertNotNull(imageUrl);
		File image = new File(imageUrl.toURI());
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		repositoryManager.setImage(new VFSJavaIOFile(image), courseEntry, defaultUnitTestAdministrator.getIdentity());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		repositoryManager.setImage(new VFSJavaIOFile(image), courseEntry, defaultUnitTestAdministrator.getIdentity());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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

		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
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
		
		conn.shutdown();
	}
	
	/**
	 * Because it's the mose used one.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void closeCourse() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();

		// Set the course to coach published
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("courses").path(course.getResourceableId().toString())
				.path("status")
				.queryParam("newStatus", RepositoryEntryStatusEnum.closed.name())
				.build();
		
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		RepositoryEntry updatedEntry = repositoryService.loadByKey(courseEntry.getKey());
		Assert.assertEquals(RepositoryEntryStatusEnum.closed, updatedEntry.getEntryStatus());
		
		conn.shutdown();
	}
	
	@Test
	public void updateMetadata() throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		
		conn.shutdown();
	}
	
	@Test
	public void changedStatus_closed() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
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
		
		conn.shutdown();
	}
	
	@Test
	public void changedStatus_deleted() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(defaultUnitTestAdministrator.getIdentity(),
				defaultUnitTestOrganisation);
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
		
		conn.shutdown();
	}
	
	@Test
	public void exportCourse()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("course-owner");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		dbInstance.closeSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getOlatResource().getResourceableId().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);	
		
		conn.shutdown();
	}
	
	@Test
	public void exportCourse_owner()
	throws IOException, URISyntaxException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndUser("course-owner-2");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(author.getIdentity());
		dbInstance.closeSession();

		RestConnection conn = new RestConnection(author);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getOlatResource().getResourceableId().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		byte[] exportedFile = EntityUtils.toByteArray(response.getEntity());
		Assert.assertTrue(exportedFile.length > 1000);
		
		conn.shutdown();
	}
	
	@Test
	public void exportCourse_notOwner()
	throws IOException, URISyntaxException {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("course-owner-3");
		IdentityWithLogin otherUser = JunitTestHelper.createAndPersistRndUser("course-owner-4");
		RepositoryEntry course = JunitTestHelper.deployBasicCourse(owner);
		dbInstance.closeSession();

		RestConnection conn = new RestConnection(otherUser);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getOlatResource().getResourceableId().toString()).path("file").build();
		HttpGet method = conn.createGet(request, "application/zip", true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(403, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		conn.shutdown();
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