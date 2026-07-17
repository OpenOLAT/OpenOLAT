/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.model.LectureBlockRefImpl;
import org.olat.modules.lecture.restapi.LectureBlockVO;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementLectureBlocksWebServiceTest extends OlatRestTestCase {
	
	private static Identity author;
	private static Organisation defaultUnitTestOrganisation;
	private static IdentityWithLogin defaultUnitTestAdministrator;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
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
	
	@Test
	public void getLectureBlock()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(null, element);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		List<LectureBlockVO> blockVoes = conn.parseList(response, LectureBlockVO.class);
		Assertions.assertThat(blockVoes)
			.hasSize(1)
			.map(LectureBlockVO::getKey)
			.containsExactly(lectureBlock.getKey());
		
		Assert.assertEquals(element.getKey(), blockVoes.get(0).getCurriculumElementKey());
		Assert.assertEquals("Hello lecturers", blockVoes.get(0).getTitle());
	}
	
	@Test
	public void createLectureBlock()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum 1", "Element for with course and lecture",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, defaultUnitTestOrganisation);
		curriculumService.addRepositoryEntry(element, entry, false);
		dbInstance.commitAndCloseSession();
		
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("Block 1 in curriculum");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		lectureBlockVo.setCurriculumElementKey(element.getKey());

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);

		LectureBlockVO blockVo = conn.parse(response.getEntity(), LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		Assert.assertNotNull(blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
		Assert.assertEquals(element.getKey(), blockVo.getCurriculumElementKey());
		
		// check the database
		LectureBlock dbBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(blockVo.getKey()));
		Assert.assertNotNull(dbBlock);
		Assert.assertEquals("Block 1 in curriculum", dbBlock.getTitle());
		Assert.assertEquals(element, dbBlock.getCurriculumElement());
		Assert.assertEquals(entry, dbBlock.getEntry());
	}
	
	@Test
	public void createLectureBlockEmptyElement()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures but no courses", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum 2", "Element with a lecture and no course",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);

		dbInstance.commitAndCloseSession();
		
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("Block 1 in curriculum");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		lectureBlockVo.setCurriculumElementKey(element.getKey());

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);

		LectureBlockVO blockVo = conn.parse(response.getEntity(), LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		Assert.assertNotNull(blockVo.getKey());
		Assert.assertNull(blockVo.getRepoEntryKey());
		Assert.assertEquals(element.getKey(), blockVo.getCurriculumElementKey());
		
		// check the database
		LectureBlock dbBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(blockVo.getKey()));
		Assert.assertNotNull(dbBlock);
		Assert.assertEquals("Block 1 in curriculum", dbBlock.getTitle());
		Assert.assertEquals(element, dbBlock.getCurriculumElement());
		Assert.assertNull(dbBlock.getEntry());
	}
	
	@Test
	public void createLectureBlockWithCourse()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum 3", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, defaultUnitTestOrganisation);
		curriculumService.addRepositoryEntry(element, entry, false);
		RepositoryEntry otherEntry = JunitTestHelper.createRandomRepositoryEntry(author, defaultUnitTestOrganisation);
		curriculumService.addRepositoryEntry(element, otherEntry, false);
		dbInstance.commitAndCloseSession();
		
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("Block 2 in curriculum");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		lectureBlockVo.setRepoEntryKey(entry.getKey());
		lectureBlockVo.setCurriculumElementKey(element.getKey());

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode()).isIn(200, 201);

		LectureBlockVO blockVo = conn.parse(response.getEntity(), LectureBlockVO.class);
		Assert.assertNotNull(blockVo);
		Assert.assertNotNull(blockVo.getKey());
		Assert.assertEquals(entry.getKey(), blockVo.getRepoEntryKey());
		Assert.assertEquals(element.getKey(), blockVo.getCurriculumElementKey());
		
		// check the database
		LectureBlock dbBlock = lectureService.getLectureBlock(new LectureBlockRefImpl(blockVo.getKey()));
		Assert.assertNotNull(dbBlock);
		Assert.assertEquals("Block 2 in curriculum", dbBlock.getTitle());
		Assert.assertEquals(element, dbBlock.getCurriculumElement());
		Assert.assertEquals(entry, dbBlock.getEntry());
		
		List<LectureBlock> blocks = lectureService.getLectureBlocks(entry);
		Assertions.assertThat(blocks)
			.hasSize(1)
			.map(LectureBlock::getKey)
			.containsExactly(blockVo.getKey());

		List<LectureBlock> otherBlocks = lectureService.getLectureBlocks(otherEntry);
		Assertions.assertThat(otherBlocks)
			.isEmpty();
	}
	
	@Test
	public void createFailedLectureBlockWithCourses()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum 4", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, defaultUnitTestOrganisation);
		curriculumService.addRepositoryEntry(element, entry, false);
		RepositoryEntry otherEntry = JunitTestHelper.createRandomRepositoryEntry(author, defaultUnitTestOrganisation);
		curriculumService.addRepositoryEntry(element, otherEntry, false);
		dbInstance.commitAndCloseSession();
		
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("Block 2 in curriculum");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		lectureBlockVo.setCurriculumElementKey(element.getKey());

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode())
			.isEqualTo(409);
	}
	
	@Test
	public void createFailedLectureBlockWithNotLinkedCourses()
	throws IOException, URISyntaxException {
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum 5", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author, defaultUnitTestOrganisation);
		dbInstance.commitAndCloseSession();
		
		LectureBlockVO lectureBlockVo = new LectureBlockVO();
		lectureBlockVo.setTitle("Block 2 in curriculum");
		lectureBlockVo.setPlannedLectures(4);
		lectureBlockVo.setStartDate(new Date());
		lectureBlockVo.setEndDate(new Date());
		lectureBlockVo.setRepoEntryKey(entry.getKey());
		lectureBlockVo.setCurriculumElementKey(element.getKey());

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, lectureBlockVo);
		HttpResponse response = conn.execute(method);
		
		// check the response
		Assertions.assertThat(response.getStatusLine().getStatusCode())
			.isEqualTo(409);
	}
	
	@Test
	public void addTeacherToLectureBlock()
	throws IOException, URISyntaxException {
		Identity teacher = JunitTestHelper.createAndPersistIdentityAsRndUser("teacher-1", defaultUnitTestOrganisation, null);
		Curriculum curriculum = curriculumService.createCurriculum("Lectures-blocks-cur", "Curriculum with lectures", "Curriculum", false, defaultUnitTestOrganisation);
		CurriculumElement element = curriculumService.createCurriculumElement("Block to curriculum 6", "Element for lecture with teachers",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.enabled, CurriculumLearningProgress.disabled, curriculum);
		
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(null, element);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello teacher");
		lectureBlock = lectureBlockDao.update(lectureBlock);
		dbInstance.commitAndCloseSession();

		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);

		URI uri = UriBuilder.fromUri(getContextURI()).path("curriculum").path(curriculum.getKey().toString()).path("elements")
				.path(element.getKey().toString())
				.path("lectureblocks").path(lectureBlock.getKey().toString())
				.path("teachers").path(teacher.getKey().toString()).build();
		HttpPut method = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		// check the response
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		//check the database
		List<Identity> teachers = lectureService.getTeachers(lectureBlock);
		Assert.assertTrue(teachers.contains(teacher));
	}
}
