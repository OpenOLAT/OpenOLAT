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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.modules.lecture.restapi.LectureBlockVO;
import org.olat.modules.lecture.restapi.RepositoryEntryLectureConfigurationVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatJerseyTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlocksTest extends OlatJerseyTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	/**
	 * Get the list of lecture block through the course.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_course()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("lectureblocks").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		Assert.assertEquals(1, voList.size());
		LectureBlockVO blockVo = voList.get(0);
		Assert.assertEquals(block.getKey(), blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
	}
	
	/**
	 *  Get the list of lecture block through the repository entry.
	 *  
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_repository()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity().getContent());
		Assert.assertNotNull(voList);
		Assert.assertEquals(1, voList.size());
		LectureBlockVO blockVo = voList.get(0);
		Assert.assertEquals(block.getKey(), blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
	}
	
	@Test
	public void putLecturesBlock_repository()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		String externalId = UUID.randomUUID().toString();
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("New block");
		lectureBlockVo.setDescription("A little description");
		lectureBlockVo.setComment("A comment");
		lectureBlockVo.setLocation("The secret location");
		lectureBlockVo.setManagedFlagsString("all");
		lectureBlockVo.setPreparation("Lot of");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setExternalId(externalId);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);

		LectureBlockVO blockVo = conn.parse(response.getEntity().getContent(), LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
		Assert.assertEquals("New block", blockVo.getTitle());
		Assert.assertEquals("A little description", blockVo.getDescription());
		Assert.assertEquals("A comment", blockVo.getComment());
		Assert.assertEquals("The secret location", blockVo.getLocation());
		Assert.assertEquals("all", blockVo.getManagedFlagsString());
		Assert.assertEquals(4, blockVo.getPlannedLectures());
		Assert.assertEquals(externalId, blockVo.getExternalId());
		Assert.assertNotNull(blockVo.getStartDate());
		Assert.assertNotNull(blockVo.getEndDate());
		
		// check the database
		LectureBlock dbBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(blockVo.getKey()));
		Assert.assertNotNull(dbBlock);
		Assert.assertEquals("New block", dbBlock.getTitle());
		Assert.assertEquals("A little description", dbBlock.getDescription());
		Assert.assertEquals("A comment", dbBlock.getComment());
		Assert.assertEquals("The secret location", dbBlock.getLocation());
		Assert.assertEquals("all", dbBlock.getManagedFlagsString());
		Assert.assertEquals(4, dbBlock.getPlannedLecturesNumber());
		Assert.assertEquals(externalId, dbBlock.getExternalId());
		Assert.assertNotNull(dbBlock.getStartDate());
		Assert.assertNotNull(dbBlock.getEndDate());
	}
	
	/**
	 * Check that the done and autoclosed status are set.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void putLecturesBlock_autoclosed()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		String externalId = UUID.randomUUID().toString();
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("A block to close");
		lectureBlockVo.setDescription("A description");
		lectureBlockVo.setManagedFlagsString("all");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setExternalId(externalId);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		lectureBlockVo.setStatus("done");
		lectureBlockVo.setRollCallStatus("autoclosed");

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);

		LectureBlockVO blockVo = conn.parse(response.getEntity().getContent(), LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		
		// check the database
		LectureBlock dbBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(blockVo.getKey()));
		Assert.assertNotNull(dbBlock);
		Assert.assertEquals("A block to close", dbBlock.getTitle());
		Assert.assertEquals("A description", dbBlock.getDescription());
		Assert.assertEquals("all", dbBlock.getManagedFlagsString());
		Assert.assertEquals(4, dbBlock.getPlannedLecturesNumber());
		Assert.assertEquals(externalId, dbBlock.getExternalId());
		Assert.assertNotNull(dbBlock.getStartDate());
		Assert.assertNotNull(dbBlock.getEndDate());
		Assert.assertEquals(LectureBlockStatus.done, dbBlock.getStatus());
		Assert.assertEquals(LectureRollCallStatus.autoclosed, dbBlock.getRollCallStatus());
	}
	
	@Test
	public void getLecturesBlockConfiguration()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").path("configuration").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryLectureConfigurationVO configVo = conn.parse(response, RepositoryEntryLectureConfigurationVO.class);
		Assert.assertNotNull(configVo);
	}
	
	@Test
	public void updateLecturesBlockConfiguration()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		
		RepositoryEntryLectureConfigurationVO configVo = new RepositoryEntryLectureConfigurationVO();
		configVo.setLectureEnabled(Boolean.TRUE);
		configVo.setCalculateAttendanceRate(Boolean.TRUE);
		configVo.setOverrideModuleDefault(Boolean.TRUE);
		configVo.setCourseCalendarSyncEnabled(Boolean.TRUE);
		configVo.setRequiredAttendanceRate(34.0d);
		configVo.setRollCallEnabled(Boolean.TRUE);
		configVo.setTeacherCalendarSyncEnabled(Boolean.TRUE);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").path("configuration").build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, configVo);
		HttpResponse response = conn.execute(method);

		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		RepositoryEntryLectureConfigurationVO updateConfigVo = conn.parse(response, RepositoryEntryLectureConfigurationVO.class);
		Assert.assertNotNull(updateConfigVo);
		Assert.assertEquals(Boolean.TRUE, updateConfigVo.getLectureEnabled());
		Assert.assertEquals(Boolean.TRUE, updateConfigVo.getCalculateAttendanceRate());
		Assert.assertEquals(Boolean.TRUE, updateConfigVo.getOverrideModuleDefault());
		Assert.assertEquals(Boolean.TRUE, updateConfigVo.getCourseCalendarSyncEnabled());
		Assert.assertEquals(34.0d, updateConfigVo.getRequiredAttendanceRate(), 0000.1);
		Assert.assertEquals(Boolean.TRUE, updateConfigVo.getRollCallEnabled());
		Assert.assertEquals(Boolean.TRUE, updateConfigVo.getTeacherCalendarSyncEnabled());
		
		// check the database
		RepositoryEntryLectureConfiguration dbConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		Assert.assertNotNull(dbConfig);
		Assert.assertTrue(dbConfig.isLectureEnabled());
		Assert.assertEquals(Boolean.TRUE, dbConfig.getCalculateAttendanceRate());
		Assert.assertTrue(dbConfig.isOverrideModuleDefault());
		Assert.assertEquals(Boolean.TRUE, dbConfig.getCourseCalendarSyncEnabled());
		Assert.assertEquals(34.0d, dbConfig.getRequiredAttendanceRate(), 0000.1);
		Assert.assertEquals(Boolean.TRUE, dbConfig.getRollCallEnabled());
		Assert.assertEquals(Boolean.TRUE, dbConfig.getTeacherCalendarSyncEnabled());
	}
	
	@Test
	public void getLectureBlock()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString()).build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		LectureBlockVO blockVo = conn.parse(response, LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		Assert.assertEquals(block.getKey(), blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
	}
	
	@Test
	public void deleteLectureBlock()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString()).build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		LectureBlock deletedBlock = lectureService.getLectureBlock(block);
		Assert.assertNull(deletedBlock);
	}
	
	@Test
	public void addRepositoryEntryDefaultGroupToLectureBlock()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("repositoryentry").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Group defGroup = repositoryService.getDefaultGroup(entry);
		Assert.assertEquals(defGroup, groups.get(0));
	}
	
	@Test
	public void removeRepositoryEntryDefaultGroupToLectureBlock()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureService.save(block, Collections.singletonList(defGroup));
		dbInstance.commit();

		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("repositoryentry").build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertEquals(0, groups.size());
	}

	@Test
	public void addTeacherToLectureBlock()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("teachers").path(teacher.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Identity> teachers = lectureService.getTeachers(block);
		Assert.assertTrue(teachers.contains(teacher));
	}
	
	@Test
	public void removeTeacherToLectureBlock()
	throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsAuthor("lect-1");
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2");
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, teacher1);
		lectureService.addTeacher(block, teacher2);
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("teachers").path(teacher1.getKey().toString()).build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Identity> teachers = lectureService.getTeachers(block);
		Assert.assertEquals(1, teachers.size());
		Assert.assertFalse(teachers.contains(teacher1));
		Assert.assertTrue(teachers.contains(teacher2));
	}
	
	private LectureBlock createLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
	protected List<LectureBlockVO> parseLectureBlockArray(InputStream body) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(body, new TypeReference<List<LectureBlockVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
