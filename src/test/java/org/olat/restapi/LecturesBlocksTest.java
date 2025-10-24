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

import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;
import org.olat.modules.bigbluebutton.restapi.BigBlueButtonMeetingVO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureBlockToTaxonomyLevel;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.manager.LectureBlockToTaxonomyLevelDAO;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.modules.lecture.restapi.LectureBlockVO;
import org.olat.modules.lecture.restapi.RepositoryEntryLectureConfigurationVO;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * 
 * Initial date: 8 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlocksTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(LecturesBlocksTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private LectureBlockToTaxonomyLevelDAO lectureBlockToTaxonomyLevelDao;
	
	private static Identity author;
	private static Organisation defaultUnitTestOrganisation;
	private static IdentityWithLogin defaultUnitTestAdministrator;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-lectures-unit-test", "Org-lectures-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			author = JunitTestHelper.createAndPersistIdentityAsRndUser("lecture_author", defaultUnitTestOrganisation, null);
			defaultUnitTestAdministrator = JunitTestHelper
					.createAndPersistRndAdmin("Cur-Elem-Web", defaultUnitTestOrganisation);
		}
	}
	
	/**
	 * Get the list of lecture block through the course.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLecturesBlock_course()
	throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("lectureblocks").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity());
		Assert.assertNotNull(voList);
		Assert.assertEquals(1, voList.size());
		LectureBlockVO blockVo = voList.get(0);
		Assert.assertEquals(block.getKey(), blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
	}
	
	
	/**
	 * Get the list of lecture block with meetings
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getLectureBlockWithBigBlueButtonMeeting()
	throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);

		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		BigBlueButtonMeetingTemplate template = templates.get(0);
		BigBlueButtonMeeting meeting = bigBlueButtonManager.createAndPersistMeeting("Big blue button 1", entry, null, null, author);
		meeting.setTemplate(template);
		block.setBBBMeeting(meeting);
		block = lectureService.save(block, null);

		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("lectureblocks").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity());
		Assert.assertNotNull(voList);
		Assert.assertEquals(1, voList.size());
		LectureBlockVO blockVo = voList.get(0);
		Assert.assertEquals(block.getKey(), blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
		
		Long meetingKey = blockVo.getBigBlueButtonMeetingKey();
		Assert.assertEquals(meeting.getKey(), meetingKey);
	}
	
	
	/**
	 * Get the meeting of a specific of lecture block
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void getBigBlueButtonMeetingOfLectureBlock()
	throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);

		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		BigBlueButtonMeetingTemplate template = templates.get(0);
		BigBlueButtonMeeting meeting = bigBlueButtonManager.createAndPersistMeeting("Big blue button 2", entry, null, null, author);
		meeting.setTemplate(template);
		block.setBBBMeeting(meeting);
		block = lectureService.save(block, null);
		meeting = block.getBBBMeeting();

		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("lectureblocks").path(block.getKey().toString()).path("bigbluebuttonmeeting").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);

		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		BigBlueButtonMeetingVO meetingVo = conn.parse(response, BigBlueButtonMeetingVO.class);
		Assert.assertNotNull(meetingVo);
		Assert.assertEquals(meeting.getKey(), meetingVo.getKey());
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
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> voList = parseLectureBlockArray(response.getEntity());
		Assert.assertNotNull(voList);
		Assert.assertEquals(1, voList.size());
		LectureBlockVO blockVo = voList.get(0);
		Assert.assertEquals(block.getKey(), blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
	}
	
	@Test
	public void putLecturesBlock_repository()
	throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
		lectureBlockVo.setExternalRef("lot-of-0001");
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);

		LectureBlockVO blockVo = conn.parse(response.getEntity(), LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
		Assert.assertEquals("New block", blockVo.getTitle());
		Assert.assertEquals("A little description", blockVo.getDescription());
		Assert.assertEquals("A comment", blockVo.getComment());
		Assert.assertEquals("The secret location", blockVo.getLocation());
		Assert.assertEquals("all", blockVo.getManagedFlagsString());
		Assert.assertEquals(4, blockVo.getPlannedLectures());
		Assert.assertEquals(externalId, blockVo.getExternalId());
		Assert.assertEquals("lot-of-0001", blockVo.getExternalRef());
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
	
	@Test
	public void putLecturesBlockInCourseWithBigBlueButtonMeeting()
	throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		BigBlueButtonMeetingTemplate template = bigBlueButtonManager.createAndPersistTemplate("Lectures-" + random());
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		String externalId = UUID.randomUUID().toString();
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("New block");
		lectureBlockVo.setDescription("A little description");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setExternalId(externalId);
		lectureBlockVo.setExternalRef("lot-of-0001");
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);
		// Check the lecture block
		LectureBlockVO blockVo = conn.parse(response.getEntity(), LectureBlockVO.class);
		
		// Set an online meeting
		BigBlueButtonMeetingVO meetingVo = new BigBlueButtonMeetingVO();
		meetingVo.setName("New block meeting");
		meetingVo.setLeadTime(15);
		meetingVo.setFollowupTime(20);
		meetingVo.setTemplateKey(template.getKey());
		
		URI meetingUri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("lectureblocks")
				.path(blockVo.getKey().toString()).path("bigbluebuttonmeeting").build();
		HttpPut meetingMethod = conn.createPut(meetingUri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(meetingMethod, meetingVo);
		HttpResponse meetingResponse = conn.execute(meetingMethod);
		// check the response
		Assertions.assertThat(meetingResponse.getStatusLine().getStatusCode()).isIn(200, 201);
		
		BigBlueButtonMeetingVO persistedMeetingVo = conn.parse(meetingResponse, BigBlueButtonMeetingVO.class);
		Assert.assertNotNull(persistedMeetingVo);
		Assert.assertNotNull(persistedMeetingVo.getKey());
		Assert.assertNotNull(persistedMeetingVo.getStartDate());
		Assert.assertNotNull(persistedMeetingVo.getEndDate());
		Assert.assertEquals(15, persistedMeetingVo.getLeadTime());
		Assert.assertEquals(20, persistedMeetingVo.getFollowupTime());
	}
	
	
	/**
	 * Get the list of lecture block with meetings
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void deleteBigBlueButtonMeetingInLectureBlock()
	throws IOException, URISyntaxException {
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entry);

		List<BigBlueButtonMeetingTemplate> templates = bigBlueButtonManager.getTemplates();
		BigBlueButtonMeetingTemplate template = templates.get(0);
		BigBlueButtonMeeting meeting = bigBlueButtonManager.createAndPersistMeeting("Big blue button 1", entry, null, null, author);
		meeting.setTemplate(template);
		block.setBBBMeeting(meeting);
		block = lectureService.save(block, null);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(course.getResourceableId().toString()).path("lectureblocks")
				.path(block.getKey().toString()).path("bigbluebuttonmeeting").build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(block.getKey()));
		Assert.assertNull(reloadedBlock.getBBBMeeting());
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
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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

		LectureBlockVO blockVo = conn.parse(response.getEntity(), LectureBlockVO.class);
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
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
	
	/**
	 * Check if setting several times the group leads to problems.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void addRepositoryEntryDefaultGroupToLectureBlockMultipleTimes()
	throws IOException, URISyntaxException {
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("repositoryentry").build();
		
		for(int i=0; i<5; i++) {
			HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
			HttpResponse response = conn.execute(method);
			// check the response
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
		}
		
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
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		Group defGroup = repositoryService.getDefaultGroup(entry);
		lectureService.save(block, Collections.singletonList(defGroup));
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
	public void syncRepositoryEntryCurriculumElementToLectureBlock()
	throws IOException, URISyntaxException {
		// prepare a course with a curriculum element and a lecture block
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		
		Curriculum curriculum = curriculumService.createCurriculum("add-group", "Add group REST", "", false, defaultUnitTestOrganisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement("add-group",
				"Add element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		curriculumService.addRepositoryEntry(curriculumElement, entry, true);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("curriculum").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(curriculumElement.getGroup()));
	}
	
	@Test
	public void syncRepositoryEntryCurriculumElementToLectureBlockAddRemove()
	throws IOException, URISyntaxException {
		// prepare a course with a curriculum element and a lecture block
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		
		Curriculum curriculum = curriculumService.createCurriculum("add-groups", "Add groups REST", "", false, defaultUnitTestOrganisation);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement("add-group-1",
				"Add element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement("add-group-2",
				"Add element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		
		curriculumService.addRepositoryEntry(curriculumElement1, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, true);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("curriculum").build();
		
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertEquals(2, groups.size());
		
		// remove a curriculum
		curriculumService.removeRepositoryEntry(curriculumElement2, entry);
		dbInstance.commit();
		
		method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		response = conn.execute(method);
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		List<Group> synchedGroups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(synchedGroups);
		Assert.assertEquals(1, synchedGroups.size());
		Assert.assertEquals(curriculumElement1.getGroup(), synchedGroups.get(0));	
	}
	
	@Test
	public void syncRepositoryEntryCurriculumElementToLectureBlockAddRemoveOtherGroups()
	throws IOException, URISyntaxException {
		// prepare a course with a curriculum element and a lecture block
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		
		Curriculum curriculum = curriculumService.createCurriculum("add-groups-but-notall", "Add groups REST", "", false, defaultUnitTestOrganisation);
		CurriculumElement curriculumElement1 = curriculumService.createCurriculumElement("add-group-1",
				"Add element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement curriculumElement2 = curriculumService.createCurriculumElement("add-group-2",
				"Add element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		CurriculumElement curriculumElement3 = curriculumService.createCurriculumElement("add-group-3",
				"Add element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		
		curriculumService.addRepositoryEntry(curriculumElement1, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement2, entry, true);
		curriculumService.addRepositoryEntry(curriculumElement3, entry, true);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		// add curriculum elements groups
		URI cUri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("curriculum").build();
		
		HttpPut cMethod = conn.createPut(cUri, MediaType.APPLICATION_JSON, true);
		HttpResponse cResponse = conn.execute(cMethod);
		// check the response
		Assert.assertEquals(200, cResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(cResponse.getEntity());
		
		// add repository entry default group
		URI rUri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("repositoryentry").build();
		
		HttpPut rMethod = conn.createPut(rUri, MediaType.APPLICATION_JSON, true);
		HttpResponse rResponse = conn.execute(rMethod);
		// check the response
		Assert.assertEquals(200, rResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(rResponse.getEntity());
		
		//check the lecture block has 4 groups (3 from curriculum elements and 1 default from course)
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertEquals(4, groups.size());
		
		// remove a curriculum
		curriculumService.removeRepositoryEntry(curriculumElement2, entry);
		dbInstance.commit();
		
		// re-sync
		
		HttpPut syncMethod = conn.createPut(cUri, MediaType.APPLICATION_JSON, true);
		HttpResponse syncResponse = conn.execute(syncMethod);
		// check the response
		Assert.assertEquals(200, syncResponse.getStatusLine().getStatusCode());
		EntityUtils.consume(syncResponse.getEntity());
		
		List<Group> synchedGroups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(synchedGroups);
		Assert.assertEquals(3, synchedGroups.size());
		Assert.assertTrue(synchedGroups.contains(curriculumElement1.getGroup()));
		Assert.assertFalse(synchedGroups.contains(curriculumElement2.getGroup()));
		Assert.assertTrue(synchedGroups.contains(curriculumElement3.getGroup()));
		
		Group defGroup = repositoryService.getDefaultGroup(entry);
		Assert.assertTrue(synchedGroups.contains(defGroup));
	}
	
	@Test
	public void syncRepositoryEntryCurriculumElementToLectureBlockSeveralAdd()
	throws IOException, URISyntaxException {
		// prepare a course with a curriculum element and a lecture block
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		
		Curriculum curriculum = curriculumService.createCurriculum("add-group-serveral", "Add several times group REST", "", false, defaultUnitTestOrganisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement("add-group",
				"Add several times element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		curriculumService.addRepositoryEntry(curriculumElement, entry, true);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("curriculum").build();
		
		for(int i=0; i<4; i++) {
			HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
			HttpResponse response = conn.execute(method);
			
			// check the response
			Assert.assertEquals(200, response.getStatusLine().getStatusCode());
			EntityUtils.consume(response.getEntity());
		}
		
		//check the database
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertEquals(1, groups.size());
		Assert.assertTrue(groups.contains(curriculumElement.getGroup()));
	}
	
	@Test
	public void removeRepositoryEntryCurriculumElementToLectureBlock()
	throws IOException, URISyntaxException {
		// prepare a course with a curriculum element and a lecture block
		// the lecture block use already the curriculum element as source of participants
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		Curriculum curriculum = curriculumService.createCurriculum("rm-group", "Remove group REST", "", false, defaultUnitTestOrganisation);
		CurriculumElement curriculumElement = curriculumService.createCurriculumElement("rm-group",
				"Remove element group", CurriculumElementStatus.active, null, null, null, null,
				CurriculumCalendars.disabled, CurriculumLectures.disabled, CurriculumLearningProgress.disabled,
				curriculum);
		curriculumService.addRepositoryEntry(curriculumElement, entry, true);
		dbInstance.commit();
		lectureService.save(block, Collections.singletonList(curriculumElement.getGroup()));
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("participants").path("curriculum").build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Group> groups = lectureService.getLectureBlockToGroups(block);
		Assert.assertNotNull(groups);
		Assert.assertTrue(groups.isEmpty());
	}

	@Test
	public void addTeacherToLectureBlock()
	throws IOException, URISyntaxException {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
		Identity teacher1 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2", defaultUnitTestOrganisation, null);
		Identity teacher2 = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-3", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, teacher1);
		lectureService.addTeacher(block, teacher2);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

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
	
	/**
	 * Move a lecture block from one course to the other.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void moveLectureBlock()
	throws IOException, URISyntaxException {
		RepositoryEntry entryOrigin = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		RepositoryEntry entryTarget = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		ICourse courseOrigin = CourseFactory.loadCourse(entryOrigin);
		entryOrigin = courseOrigin.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		LectureBlock block = createLectureBlock(entryOrigin);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(courseOrigin.getResourceableId().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("entry").path(entryTarget.getKey().toString()).build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		LectureBlockVO movedBlock = conn.parse(response, LectureBlockVO.class);
		Assert.assertNotNull(movedBlock);
		Assert.assertEquals(entryTarget.getKey(), movedBlock.getRepoEntryKey());
		
		// check lecture blocks of origin
		List<LectureBlock> originBlocks = lectureService.getLectureBlocks(entryOrigin);
		Assert.assertNotNull(originBlocks);
		Assert.assertTrue(originBlocks.isEmpty());
		
		// check lecture block of target
		List<LectureBlock> targetBlocks = lectureService.getLectureBlocks(entryTarget);
		Assert.assertNotNull(targetBlocks);
		Assert.assertEquals(1, targetBlocks.size());
		Assert.assertEquals(block.getKey(), targetBlocks.get(0).getKey());		
	}
	
	@Test
	public void getTaxonomyLevels()
	throws IOException, URISyntaxException {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, teacher);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-200", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		lectureBlockToTaxonomyLevelDao.createRelation(block, level);
		dbInstance.commit();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("taxonomy").path("levels").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<TaxonomyLevelVO> levelVoes = parseTaxonomyLevelArray(response.getEntity());
		Assert.assertNotNull(levelVoes);
		Assert.assertEquals(1, levelVoes.size());
	}
	
	@Test
	public void addTaxonomyLevels()
	throws IOException, URISyntaxException {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, teacher);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-200", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("taxonomy").path("levels").path(level.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		Set<LectureBlockToTaxonomyLevel> relationToLevels = reloadedBlock.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		LectureBlockToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
	}
	
	@Test
	public void addTwiceTaxonomyLevels()
	throws IOException, URISyntaxException {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, teacher);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-200", "Leveled taxonomy", null, null);
		TaxonomyLevel level = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		lectureBlockToTaxonomyLevelDao.createRelation(block, level);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("taxonomy").path("levels").path(level.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(304, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		LectureBlock reloadedBlock = lectureService.getLectureBlock(block);
		Set<LectureBlockToTaxonomyLevel> relationToLevels = reloadedBlock.getTaxonomyLevels();
		Assert.assertNotNull(relationToLevels);
		Assert.assertEquals(1, relationToLevels.size());
		LectureBlockToTaxonomyLevel relationToLevel = relationToLevels.iterator().next();
		Assert.assertEquals(level, relationToLevel.getTaxonomyLevel());
	}
	
	@Test
	public void deleteTaxonomyLevel()
	throws IOException, URISyntaxException {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-2", defaultUnitTestOrganisation, null);
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author, defaultUnitTestOrganisation);
		LectureBlock block = createLectureBlock(entry);
		dbInstance.commit();
		lectureService.addTeacher(block, teacher);
		
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-202", "Leveled taxonomy", null, null);
		TaxonomyLevel level1 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		TaxonomyLevel level2 = taxonomyLevelDao.createTaxonomyLevel("ID-Level-0", random(), "My first taxonomy level", "A basic level", null, null, null, null, taxonomy);
		lectureBlockToTaxonomyLevelDao.createRelation(block, level1);
		lectureBlockToTaxonomyLevelDao.createRelation(block, level2);
		dbInstance.commitAndCloseSession();
		
		// make sure we have something to delete
		List<TaxonomyLevel> levels = lectureBlockToTaxonomyLevelDao.getTaxonomyLevels(block);
		Assert.assertEquals(2, levels.size());
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString())
				.path("lectureblocks").path(block.getKey().toString())
				.path("taxonomy").path("levels").path(level1.getKey().toString()).build();
		HttpDelete method = conn.createDelete(uri, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		// check that the right relation was deleted
		List<TaxonomyLevel> survivingLevels = lectureBlockToTaxonomyLevelDao.getTaxonomyLevels(block);
		Assert.assertEquals(1, survivingLevels.size());
		Assert.assertEquals(level2, survivingLevels.get(0));
	}
	
	
	
	private LectureBlock createLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureService.createLectureBlock(entry);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock.setPlannedLecturesNumber(4);
		return lectureService.save(lectureBlock, null);
	}
	
	protected List<TaxonomyLevelVO> parseTaxonomyLevelArray(HttpEntity entity) {
		try(InputStream in = entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<TaxonomyLevelVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected List<LectureBlockVO> parseLectureBlockArray(HttpEntity entity) {
		try(InputStream in = entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<LectureBlockVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
