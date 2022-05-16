/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.CourseVO;
import org.olat.restapi.support.vo.CourseVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserCoursesTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(UserCoursesTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void testMyCourses() throws IOException, URISyntaxException {
		//prepare a course with a participant
		IdentityWithLogin user = JunitTestHelper.createAndPersistRndUser("My-course-");
		
		RepositoryEntry courseRe = JunitTestHelper.deployBasicCourse(user.getIdentity());
		repositoryManager.setAccess(courseRe, RepositoryEntryStatusEnum.published, false, false);
		repositoryService.addRole(user.getIdentity(), courseRe, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login(user));
		
		//without paging
		URI request = UriBuilder.fromUri(getContextURI()).path("users").path(user.getKey().toString()).path("courses").path("my").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CourseVO> courses = parseCourseArray(response.getEntity());
		Assert.assertNotNull(courses);
		Assert.assertEquals(1, courses.size());

		//with paging
		URI pagedRequest = UriBuilder.fromUri(getContextURI()).path("users").path(user.getKey().toString()).path("courses").path("my")
				.queryParam("start", "0").queryParam("limit", "10").build();
		HttpGet pagedMethod = conn.createGet(pagedRequest, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse pagedResponse = conn.execute(pagedMethod);
		Assert.assertEquals(200, pagedResponse.getStatusLine().getStatusCode());
		CourseVOes pagedCourses = conn.parse(pagedResponse.getEntity(), CourseVOes.class);
		Assert.assertNotNull(pagedCourses);
		Assert.assertEquals(1, pagedCourses.getTotalCount());
		Assert.assertNotNull(pagedCourses.getCourses());
		Assert.assertEquals(1, pagedCourses.getCourses().length);

		conn.shutdown();
	}
	
	@Test
	public void testTeachedCourses() throws IOException, URISyntaxException {
		//prepare a course with a tutor
		IdentityWithLogin teacher = JunitTestHelper.createAndPersistRndUser("Course-teacher-");
		RepositoryEntry courseRe = JunitTestHelper.deployBasicCourse(teacher.getIdentity());
		repositoryManager.setAccess(courseRe, RepositoryEntryStatusEnum.published, false, false);
		repositoryService.addRole(teacher.getIdentity(), courseRe, GroupRoles.coach.name());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login(teacher));
		
		//without paging
		URI request = UriBuilder.fromUri(getContextURI()).path("/users").path(teacher.getKey().toString()).path("/courses/teached").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CourseVO> courses = parseCourseArray(response.getEntity());
		Assert.assertNotNull(courses);
		Assert.assertEquals(1, courses.size());

		//with paging
		URI pagedRequest = UriBuilder.fromUri(getContextURI()).path("/users").path(teacher.getKey().toString()).path("/courses/teached")
				.queryParam("start", "0").queryParam("limit", "10").build();
		HttpGet pagedMethod = conn.createGet(pagedRequest, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse pagedResponse = conn.execute(pagedMethod);
		Assert.assertEquals(200, pagedResponse.getStatusLine().getStatusCode());
		CourseVOes pagedCourses = conn.parse(pagedResponse.getEntity(), CourseVOes.class);
		Assert.assertNotNull(pagedCourses);
		Assert.assertEquals(1, pagedCourses.getTotalCount());
		Assert.assertNotNull(pagedCourses.getCourses());
		Assert.assertEquals(1, pagedCourses.getCourses().length);

		conn.shutdown();
	}
	
    @Test
    public void testOwnedCourses() throws IOException, URISyntaxException {
        //prepare a course with a owner
        IdentityWithLogin owner = JunitTestHelper.createAndPersistRndUser("Course-owner-");
        RepositoryEntry courseRe = JunitTestHelper.deployBasicCourse(owner.getIdentity());
        repositoryManager.setAccess(courseRe, RepositoryEntryStatusEnum.published, false, false);
        dbInstance.commitAndCloseSession();
        
		RestConnection conn = new RestConnection();
        Assert.assertTrue(conn.login(owner));
        
        //without paging
        URI request = UriBuilder.fromUri(getContextURI()).path("/users").path(owner.getKey().toString()).path("/courses/owned").build();
        HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
        HttpResponse response = conn.execute(method);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        List<CourseVO> courses = parseCourseArray(response.getEntity());
        Assert.assertNotNull(courses);
        Assert.assertEquals(1, courses.size());

        //with paging
        URI pagedRequest = UriBuilder.fromUri(getContextURI()).path("/users").path(owner.getKey().toString()).path("/courses/owned")
                .queryParam("start", "0").queryParam("limit", "10").build();
        HttpGet pagedMethod = conn.createGet(pagedRequest, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
        HttpResponse pagedResponse = conn.execute(pagedMethod);
        Assert.assertEquals(200, pagedResponse.getStatusLine().getStatusCode());
        CourseVOes pagedCourses = conn.parse(pagedResponse.getEntity(), CourseVOes.class);
        Assert.assertNotNull(pagedCourses);
        Assert.assertEquals(1, pagedCourses.getTotalCount());
        Assert.assertNotNull(pagedCourses.getCourses());
        Assert.assertEquals(1, pagedCourses.getCourses().length);

        conn.shutdown();
    }

    @Test
	public void testFavoritCourses() throws IOException, URISyntaxException {
		//prepare a course with a tutor
		IdentityWithLogin me = JunitTestHelper.createAndPersistRndUser("Course-teacher-");
		RepositoryEntry courseRe = JunitTestHelper.deployBasicCourse(me.getIdentity());
		repositoryManager.setAccess(courseRe, RepositoryEntryStatusEnum.published, true, false);
		markManager.setMark(courseRe, me.getIdentity(), null, "[RepositoryEntry:" + courseRe.getKey() + "]");	
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login(me));
		
		//without paging
		URI request = UriBuilder.fromUri(getContextURI()).path("/users").path(me.getKey().toString()).path("/courses/favorite").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<CourseVO> courses = parseCourseArray(response.getEntity());
		Assert.assertNotNull(courses);
		Assert.assertEquals(1, courses.size());
		
		//with paging
		URI pagedRequest = UriBuilder.fromUri(getContextURI()).path("/users").path(me.getKey().toString()).path("/courses/favorite")
				.queryParam("start", "0").queryParam("limit", "10").build();
		HttpGet pagedMethod = conn.createGet(pagedRequest, MediaType.APPLICATION_JSON + ";pagingspec=1.0", true);
		HttpResponse pagedResponse = conn.execute(pagedMethod);
		Assert.assertEquals(200, pagedResponse.getStatusLine().getStatusCode());
		CourseVOes pagedCourses = conn.parse(pagedResponse.getEntity(), CourseVOes.class);
		Assert.assertNotNull(pagedCourses);
		Assert.assertEquals(1, pagedCourses.getTotalCount());
		Assert.assertNotNull(pagedCourses.getCourses());
		Assert.assertEquals(1, pagedCourses.getCourses().length);
		

		conn.shutdown();
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
