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
import static org.junit.Assert.assertTrue;

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
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.CourseVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoursesTest extends OlatRestTestCase {

	private static final Logger log = Tracing.createLoggerFor(CoursesTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryEntryLifecycleDAO reLifecycleDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Test
	public void testGetCourses() throws IOException, URISyntaxException, InterruptedException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(admin, RepositoryEntryStatusEnum.preparation);

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<CourseVO> courses = conn.parseList(response, CourseVO.class);
		Assert.assertNotNull(courses);
		Assert.assertFalse(courses.isEmpty());

		boolean vo = false;
		for(CourseVO course:courses) {
			Long repoEntryKey = course.getRepoEntryKey();
			if(repoEntryKey != null && re.getKey().equals(repoEntryKey)) {
				vo = true;
			}

		}
		Assert.assertTrue(vo);
		conn.close();

	}

	@Test
	public void testGetCourses_searchExternalID() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(admin, RepositoryEntryStatusEnum.preparation);
		String externalId = UUID.randomUUID().toString();
		re = repositoryManager.setDescriptionAndName(re, "courses ext. id.", "courses desc", null, null, null, externalId, "ref-" + externalId, null, null);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses").queryParam("externalId", externalId).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<CourseVO> courses = conn.parseList(response, CourseVO.class);
		
		Assert.assertNotNull(courses);
		Assert.assertEquals(1, courses.size());
		CourseVO vo = courses.get(0);
		Assert.assertEquals(re.getOlatResource().getResourceableId(), vo.getKey());
		Assert.assertEquals(re.getOlatResource().getKey(), vo.getOlatResourceKey());
		Assert.assertEquals(re.getOlatResource().getResourceableTypeName(), vo.getOlatResourceTypeName());
		Assert.assertEquals(re.getKey(), vo.getRepoEntryKey());
		conn.close();

	}

	@Test
	public void testGetCourses_searchExternalID_withLifecycle() throws IOException, URISyntaxException, InterruptedException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(admin, RepositoryEntryStatusEnum.preparation);
		dbInstance.commit();

		String externalId = UUID.randomUUID().toString();
		RepositoryEntryLifecycle lifecycle = reLifecycleDao.create("course3 lifecycle", UUID.randomUUID().toString(), true, new Date(), new Date());
		re = repositoryManager.setDescriptionAndName(re, "courses3", "courses3 desc", null, null, null, externalId, null, "all", lifecycle);
		dbInstance.commitAndCloseSession();
		
		ICourse course = CourseFactory.loadCourse(re);

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses").queryParam("externalId", externalId).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		List<CourseVO> courses = conn.parseList(response, CourseVO.class);
		Assert.assertNotNull("Course list cannot be null", courses);
		Assert.assertEquals(1, courses.size());
		CourseVO vo = courses.get(0);
		Assert.assertNotNull("Course cannot be null", vo);
		Assert.assertEquals(course.getResourceableId(), vo.getKey());
		Assert.assertEquals(re.getKey(), vo.getRepoEntryKey());
		Assert.assertNotNull("Has a lifecycle", vo.getLifecycle());
		Assert.assertNotNull("Life cycle has a soft key", vo.getLifecycle().getSoftkey());
		conn.close();

	}

	@Test
	public void testGetCourses_searchExternalRef() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(admin, RepositoryEntryStatusEnum.preparation);
		String externalRef = UUID.randomUUID().toString();
		re = repositoryManager.setDescriptionAndName(re, "courses ext. ref.", "courses desc", null, null, null, "ID-" + externalRef, externalRef, null, null);
		dbInstance.commitAndCloseSession();

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses").queryParam("externalRef", externalRef).build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		
		List<CourseVO> courses = conn.parseList(response, CourseVO.class);
		Assert.assertNotNull(courses);
		Assert.assertEquals(1, courses.size());
		CourseVO vo = courses.get(0);
		Assert.assertEquals(re.getKey(), vo.getRepoEntryKey());
		Assert.assertEquals(re.getOlatResource().getResourceableId(), vo.getKey());
		conn.close();

	}

	@Test
	public void testGetCourses_managed() throws IOException, URISyntaxException, InterruptedException {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		RepositoryEntry re = JunitTestHelper.deployBasicCourse(admin, RepositoryEntryStatusEnum.preparation);
		re = repositoryManager.setDescriptionAndName(re, "Course managed", "A managed course", null, null, null, null, null, "all", null);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses").queryParam("managed", "true").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<CourseVO> courses = conn.parseList(response, CourseVO.class);
		assertNotNull(courses);
		assertTrue(courses.size() >= 1);

		for(CourseVO course:courses) {
			boolean managed = StringHelper.containsNonWhitespace(course.getManagedFlags());
			Assert.assertTrue(managed);
		}
		conn.close();

	}

	@Test
	public void testGetCourses_notManaged() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri(getContextURI()).path("/repo/courses").queryParam("managed", "false").build();
		HttpRequest method = conn.createGet(request, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		List<CourseVO> courses = conn.parseList(response, CourseVO.class);
		assertNotNull(courses);
		assertTrue(courses.size() >= 1);

		for(CourseVO course:courses) {
			boolean managed = StringHelper.containsNonWhitespace(course.getManagedFlags());
			Assert.assertFalse(managed);
		}
		conn.close();

	}

	@Test
	public void testGetCoursesWithPaging() throws IOException, URISyntaxException, InterruptedException {
		IdentityWithLogin author = JunitTestHelper.createAndPersistRndAuthor("rest-courses");
		RestConnection conn = new RestConnection(author);
		
		// prepare 3 courses
		String ref = UUID.randomUUID().toString();
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author.getIdentity(), RepositoryEntryStatusEnum.published);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author.getIdentity(), RepositoryEntryStatusEnum.published);
		RepositoryEntry entry3 = JunitTestHelper.deployBasicCourse(author.getIdentity(), RepositoryEntryStatusEnum.published);
		repositoryManager.setDescriptionAndName(entry1, null, null, null, null, null, null, ref, null, null);
		repositoryManager.setDescriptionAndName(entry2, null, null, null, null, null, null, ref, null, null);
		repositoryManager.setDescriptionAndName(entry3, null, null, null, null, null, null, ref, null, null);
		dbInstance.commitAndCloseSession();

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.queryParam("externalRef", ref)
				.queryParam("start", "0").queryParam("limit", "1").build();
		HttpRequest method = conn.createGet(uri, MediaType.APPLICATION_JSON + ";pagingspec=1.0");
		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		CourseVOes courses = conn.parse(response, CourseVOes.class);
		assertNotNull(courses);
		assertNotNull(courses.getCourses());
		assertEquals(1, courses.getCourses().length);
		conn.close();

	}

	@Test
	public void testCreateEmptyCourse() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
			.queryParam("shortTitle", "course3").queryParam("title", "course3 long name").build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		CourseVO course = conn.parse(response, CourseVO.class);
		assertNotNull(course);
		assertEquals("course3 long name", course.getTitle());
		//check repository entry
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(course.getRepoEntryKey());
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		conn.close();

	}
	
	@Test
	public void testCreateEmptyCourse_CourseVO() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		CourseVO courseVo = new CourseVO();
		courseVo.setTitle("Course 14 long name");
		courseVo.setAuthors("Prof.Dr. 14");
		courseVo.setLocation("Z\u00FCrich");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").build();
		HttpRequest method = conn.createPut(uri, courseVo, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CourseVO course = conn.parse(response, CourseVO.class);
		Assert.assertNotNull(course);
		Assert.assertEquals("Course 14 long name", course.getTitle());
		Assert.assertEquals("Z\u00FCrich", course.getLocation());
		Assert.assertEquals("Prof.Dr. 14", course.getAuthors());
		
		//check repository entry
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(course.getRepoEntryKey());
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertEquals("Course 14 long name", re.getDisplayname());
		Assert.assertEquals("Z\u00FCrich", re.getLocation());
		Assert.assertEquals("Prof.Dr. 14", re.getAuthors());
		conn.close();

	}

	@Test
	public void testCreateEmptyWithoutAuthorCourse() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
			.queryParam("shortTitle", "Course without author")
			.queryParam("title", "Course without author")
			.queryParam("setAuthor", "false").build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CourseVO courseVo = conn.parse(response, CourseVO.class);
		Assert.assertNotNull(courseVo);
		Assert.assertEquals("Course without author", courseVo.getTitle());

		// load repository entry
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseVo.getRepoEntryKey());
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertEquals("Course without author", re.getDisplayname());

		// load the course
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertNotNull(course);
		Assert.assertEquals("Course without author", course.getCourseTitle());
		Assert.assertEquals(re, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());

		// check the list of owners
		List<Identity> owners = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertTrue(owners.isEmpty());
		conn.close();
	}

	@Test
	public void testCreateEmptyWithInitialAuthor() throws IOException, URISyntaxException, InterruptedException {
		Identity adhocAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("adhoc-author");
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
			.queryParam("shortTitle", "Course without author")
			.queryParam("title", "Course without author")
			.queryParam("initialAuthor", adhocAuthor.getKey().toString())
			.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CourseVO courseVo = conn.parse(response, CourseVO.class);
		Assert.assertNotNull(courseVo);
		Assert.assertEquals("Course without author", courseVo.getTitle());

		// load repository entry
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseVo.getRepoEntryKey());
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertEquals("Course without author", re.getDisplayname());

		// load the course
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertNotNull(course);
		Assert.assertEquals("Course without author", course.getCourseTitle());
		Assert.assertEquals(re, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry());

		// check the list of owners
		List<Identity> owners = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.owner.name());
		Assert.assertNotNull(owners);
		Assert.assertEquals(1, owners.size());
		Assert.assertEquals(adhocAuthor, owners.get(0));
		conn.close();

	}
	
	@Test
	public void testCreateEmptyWithMetadata() throws IOException, URISyntaxException, InterruptedException {
		Identity adhocAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("adhoc-author");
		dbInstance.commit();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
			.queryParam("shortTitle", "Course with metadata")
			.queryParam("title", "Course with metadata")
			.queryParam("initialAuthor", adhocAuthor.getKey().toString())
			.queryParam("objectives", "My objectives")
			.queryParam("requirements", "My requirements")
			.queryParam("credits", "My credits")
			.queryParam("expenditureOfWork", "Take a long time")
			.queryParam("location", "Zurich")
			.queryParam("externalId", "825761")
			.queryParam("externalRef", "AC-825761")
			.queryParam("teaser", "Little teaser")
			.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(200, response.statusCode());
		CourseVO courseVo = conn.parse(response, CourseVO.class);
		Assert.assertNotNull(courseVo);
		Assert.assertEquals("Course with metadata", courseVo.getTitle());

		// load repository entry
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseVo.getRepoEntryKey());
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertEquals("Course with metadata", re.getDisplayname());
		Assert.assertEquals("My objectives", re.getObjectives());
		Assert.assertEquals("My requirements", re.getRequirements());
		Assert.assertEquals("My credits", re.getCredits());
		Assert.assertEquals("Take a long time", re.getExpenditureOfWork());
		Assert.assertEquals("Zurich", re.getLocation());
		Assert.assertEquals("825761", re.getExternalId());
		Assert.assertEquals("AC-825761", re.getExternalRef());
		Assert.assertEquals("Little teaser", re.getTeaser());
		conn.close();

	}

	@Test
	public void testCreateEmptyNodeAccessType() throws IOException, URISyntaxException, InterruptedException {
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
			.queryParam("shortTitle", "courseCC")
			.queryParam("title", "course cc long name")
			.queryParam("nodeAccessType", ConditionNodeAccessProvider.TYPE)
			.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);

		HttpResponse<InputStream> response = conn.execute(method);
		assertEquals(200, response.statusCode());
		CourseVO courseVO = conn.parse(response, CourseVO.class);
		assertNotNull(courseVO);
		//check course config
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(courseVO.getRepoEntryKey());
		ICourse course = CourseFactory.loadCourse(re);
		CourseConfig courseConfig = course.getCourseConfig();
		assertEquals(ConditionNodeAccessProvider.TYPE, courseConfig.getNodeAccessType().getType());
		conn.close();

	}

	@Test
	public void testImportCourse() throws IOException, URISyntaxException, InterruptedException {
		URL cpUrl = CoursesTest.class.getResource("Course_with_blog.zip");
		assertNotNull(cpUrl);
		File cp = new File(cpUrl.toURI());

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri(getContextURI()).path("repo/courses").build();
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("foldername", "New folder 1 2 3"),
				new NameValuePair("resourcename", "Very small course"),
				new NameValuePair("displayname", "Very small course"),
				new NameValuePair("access", "3"),
				new NameValuePair("softkey", softKey));
		HttpRequest method = conn.createPost(request, cp, "Very_small_course.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

		CourseVO vo = conn.parse(response, CourseVO.class);
		assertNotNull(vo);
		assertNotNull(vo.getRepoEntryKey());
		assertNotNull(vo.getKey());

		Long repoKey = vo.getRepoEntryKey();
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoKey);
		assertNotNull(re);
		assertNotNull(re.getOlatResource());
		assertEquals("Very small course", re.getDisplayname());
		assertEquals(softKey, re.getSoftkey());
		conn.close();

	}

	@Test
	public void testImportCourseOwner() throws IOException, URISyntaxException, InterruptedException {
		URL cpUrl = CoursesTest.class.getResource("Course_with_blog.zip");
		File cp = new File(cpUrl.toURI());

		String username = "ownerImportCourse";
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndUser(username);

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri( getContextURI())
				.path("repo/courses")
				.queryParam("ownerUsername", owner.getLogin()).build();
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("foldername", "New folder 1 2 3"),
				new NameValuePair("resourcename", "Very small course"),
				new NameValuePair("displayname", "Very small course"),
				new NameValuePair("access", "3"),
				new NameValuePair("softkey", softKey));
		HttpRequest method = conn.createPost(request, cp, "Very_small_course.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

		CourseVO vo = conn.parse(response, CourseVO.class);
		Long repoKey = vo.getRepoEntryKey();
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoKey);
		assertTrue(repositoryEntryRelationDao.hasRole(owner.getIdentity(), re, GroupRoles.owner.name()));
		conn.close();

	}
	
	@Test
	public void importCourseMetadata() throws IOException, URISyntaxException, InterruptedException {
		URL courseUrl = CoursesTest.class.getResource("Course_with_blog.zip");
		File courseZip = new File(courseUrl.toURI());

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI request = UriBuilder.fromUri( getContextURI())
				.path("repo/courses").build();
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		String externalId = softKey + "-Ext-ID";
		String externalRef = softKey + "-Ext-Ref";
		
		List<NameValuePair> formParameters = List.of(
				new NameValuePair("displayname", "External course"),
				new NameValuePair("externalId", externalId),
				new NameValuePair("externalRef", externalRef),
				new NameValuePair("softkey", softKey));
		HttpRequest method = conn.createPost(request, courseZip, "Very_small_course.zip", formParameters, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

		CourseVO vo = conn.parse(response, CourseVO.class);
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(vo.getRepoEntryKey());
		Assert.assertNotNull(re);
		Assert.assertEquals(softKey, re.getSoftkey());
		Assert.assertEquals(externalId, re.getExternalId());
		Assert.assertEquals(externalRef, re.getExternalRef());
		Assert.assertEquals("External course", re.getDisplayname());
		
		ICourse course = CourseFactory.loadCourse(re);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		Assert.assertEquals("External course", rootNode.getLongTitle());
		Assert.assertEquals("Course including Blog", rootNode.getShortTitle());
		conn.close();

	}

	@Test
	public void testCopyCourse() throws IOException, URISyntaxException, InterruptedException {
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Assert.assertNotNull(entry);

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.queryParam("shortTitle", "Course copy")
				.queryParam("title", "Course copy")
				.queryParam("initialAuthor", author.getKey().toString())
				.queryParam("copyFrom", entry.getKey().toString())
				.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

		CourseVO vo = conn.parse(response, CourseVO.class);
		assertNotNull(vo);
		assertNotNull(vo.getRepoEntryKey());
		assertNotNull(vo.getKey());
		conn.close();

	}
	
	@Test
	public void testCopyCourseMetadata() throws IOException, URISyntaxException, InterruptedException {
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Assert.assertNotNull(entry);
		
		// set some metadata
		RepositoryEntryEducationalType educationalType = repositoryManager.getAllEducationalTypes().get(0);
		repositoryManager.setDescriptionAndName(entry, "REST Course copy", "external-ref", "Prof.Dr. Mueller",
				"A very descriptive course", "Tease", "With high objectives", "Lots of requirements", "But credited",
				"English", "Bienne", "Expedited in 2 hours", null, null, null, educationalType);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.queryParam("shortTitle", "Course copy")
				.queryParam("title", "Course copy")
				.queryParam("initialAuthor", author.getKey().toString())
				.queryParam("copyFrom", entry.getKey().toString())
				.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

		CourseVO vo = conn.parse(response, CourseVO.class);
		Assert.assertNotNull(vo);
		Assert.assertNotNull(vo.getRepoEntryKey());
		Assert.assertNotNull(vo.getKey());
		
		// check metadata
		RepositoryEntry courseEntry = repositoryManager.lookupRepositoryEntry(vo.getRepoEntryKey());
		Assert.assertNotNull(courseEntry);
		Assert.assertNull(courseEntry.getExternalId());
		Assert.assertNull(courseEntry.getExternalRef());
		Assert.assertEquals("A very descriptive course", courseEntry.getDescription());
		Assert.assertEquals("Tease", courseEntry.getTeaser());
		Assert.assertEquals("With high objectives", courseEntry.getObjectives());
		Assert.assertEquals("Lots of requirements", courseEntry.getRequirements());
		Assert.assertEquals("But credited", courseEntry.getCredits());
		Assert.assertEquals("English", courseEntry.getMainLanguage());
		Assert.assertEquals("Bienne", courseEntry.getLocation());
		Assert.assertEquals("Expedited in 2 hours", courseEntry.getExpenditureOfWork());
		Assert.assertEquals(entry.getTechnicalType(), courseEntry.getTechnicalType());
		Assert.assertEquals(educationalType.getKey(), courseEntry.getEducationalType().getKey());
		conn.close();

	}

	@Test
	public void testCopyCourseUnkownCourse() throws IOException, URISyntaxException, InterruptedException {
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		Assert.assertNotNull(entry);

		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.queryParam("shortTitle", "Course copy")
				.queryParam("title", "Course copy")
				.queryParam("initialAuthor", author.getKey().toString())
				.queryParam("copyFrom", "-2")
				.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		Assert.assertEquals(404, response.statusCode());
		RestConnection.consume(response);
		conn.close();

	}
	
	@Test
	public void testCopyCourse_withMetadata() throws IOException, URISyntaxException, InterruptedException {
		Identity author = JunitTestHelper.getDefaultAuthor();
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		
		RepositoryEntryEducationalType educationalType = repositoryManager.createEducationalType(UUID.randomUUID().toString());
		entry = repositoryManager.setDescriptionAndName(entry, entry.getDisplayname(),
				null, entry.getDescription(), entry.getAuthors(), null, null, null, null, null, null, null, null, null, null, educationalType);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(entry);
		
		RestConnection conn = new RestConnection("administrator", "openolat");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.queryParam("shortTitle", "Course copy")
				.queryParam("title", "Course copy")
				.queryParam("initialAuthor", author.getKey().toString())
				.queryParam("copyFrom", entry.getKey().toString())
				.queryParam("objectives", "My copied objectives")
				.queryParam("requirements", "My copied requirements")
				.queryParam("credits", "My copied credits")
				.queryParam("expenditureOfWork", "Take a long time")
				.queryParam("location", "Basel")
				.queryParam("externalId", "825762")
				.queryParam("externalRef", "AC-825762")
				
				.build();
		HttpRequest method = conn.createPut(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = conn.execute(method);
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201);

		CourseVO vo = conn.parse(response, CourseVO.class);
		assertNotNull(vo);
		assertNotNull(vo.getRepoEntryKey());
		assertNotNull(vo.getKey());
		
		// load repository entry
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(vo.getRepoEntryKey());
		Assert.assertEquals("My copied objectives", re.getObjectives());
		Assert.assertEquals("My copied requirements", re.getRequirements());
		Assert.assertEquals("My copied credits", re.getCredits());
		Assert.assertEquals("Take a long time", re.getExpenditureOfWork());
		Assert.assertEquals("Basel", re.getLocation());
		Assert.assertEquals("825762", re.getExternalId());
		Assert.assertEquals("AC-825762", re.getExternalRef());
		Assert.assertEquals(educationalType, re.getEducationalType());
		conn.close();

	}

	protected List<CourseVO> parseCourseArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(in, new TypeReference<List<CourseVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}