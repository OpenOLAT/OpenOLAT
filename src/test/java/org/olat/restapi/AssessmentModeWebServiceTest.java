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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.restapi.AssessmentModeVO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.restapi.CurriculumElementVO;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.ObjectFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 4 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeWebServiceTest extends OlatRestTestCase {

	private static final Logger log = Tracing.createLoggerFor(AssessmentModeWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	
	@Test
	public void getAssessmentModes()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<AssessmentModeVO> modeVoes = parseAssessmentModeArray(response.getEntity());
		assertThat(modeVoes)
			.isNotNull()
			.isNotEmpty()
			.extracting(vo -> vo.getKey())
			.containsAnyOf(savedMode.getKey());
		
		conn.shutdown();
	}
	
	@Test
	public void getAssessmentModeByKey()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Get assessment");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		mode.setManagedFlagsString("all");
		
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		// Search with the external ID
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("assessmentmodes").path(savedMode.getKey().toString())
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		AssessmentModeVO modeVo = conn.parse(response.getEntity(), AssessmentModeVO.class);
		Assert.assertNotNull(modeVo);
		Assert.assertEquals(savedMode.getKey(), modeVo.getKey());
		Assert.assertEquals("Get assessment", modeVo.getName());

		conn.shutdown();
	}
	
	@Test
	public void findAssessmentModesByExternalId()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("External assessment");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		String externalId = UUID.randomUUID().toString();
		mode.setExternalId(externalId);
		mode.setManagedFlagsString("all");
		
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		// Search with the external ID
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes")
				.queryParam("externalId", externalId)
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		List<AssessmentModeVO> modeVoes = parseAssessmentModeArray(response.getEntity());
		assertThat(modeVoes)
			.isNotNull()
			.isNotEmpty()
			.extracting(vo -> vo.getKey())
			.containsExactly(savedMode.getKey());
		
		// Search with an ID which doesn't exist
		URI nothingRequest = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes")
				.queryParam("externalId", "this is not an external id")
				.build();
		HttpGet nothingMethod = conn.createGet(nothingRequest, MediaType.APPLICATION_JSON, true);
		HttpResponse nothingResponse = conn.execute(nothingMethod);
		Assert.assertEquals(200, nothingResponse.getStatusLine().getStatusCode());
		List<AssessmentModeVO> emptyVoes = parseAssessmentModeArray(nothingResponse.getEntity());
		assertThat(emptyVoes)
			.isNotNull()
			.isEmpty();

		conn.shutdown();
	}
	
	@Test
	public void findAssessmentModesByManaged()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("External assessment");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		mode.setManagedFlagsString("all");
		
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		// Search with the external ID
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes")
				.queryParam("managed", "true")
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		List<AssessmentModeVO> modeVoes = parseAssessmentModeArray(response.getEntity());
		// Is our assessment mode in the list?
		assertThat(modeVoes)
			.isNotNull()
			.isNotEmpty()
			.extracting(vo -> vo.getKey())
			.containsAnyOf(savedMode.getKey());
		
		// Is all results managed?
		for(AssessmentModeVO modeVo:modeVoes) {
			Assert.assertTrue(StringHelper.containsNonWhitespace(modeVo.getManagedFlagsString()));
		}

		conn.shutdown();
	}
	
	@Test
	public void findAssessmentModesByDates()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode futureMode = assessmentModeMgr.createAssessmentMode(entry);
		futureMode.setName("Future assessment");
		futureMode.setBegin(DateUtils.addDays(new Date(), 12));
		futureMode.setEnd(DateUtils.addHours(futureMode.getBegin(), 3));
		futureMode.setTargetAudience(Target.course);
		futureMode.setManagedFlagsString("all");
		futureMode = assessmentModeMgr.persist(futureMode);
		dbInstance.commitAndCloseSession();
		
		AssessmentMode pastMode = assessmentModeMgr.createAssessmentMode(entry);
		pastMode.setName("Past assessment");
		pastMode.setBegin(DateUtils.addDays(new Date(), -10));
		pastMode.setEnd(DateUtils.addHours(futureMode.getBegin(), 3));
		pastMode.setTargetAudience(Target.course);
		pastMode.setManagedFlagsString("all");
		pastMode = assessmentModeMgr.persist(pastMode);
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));			
		
		// Search with the external ID
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes")
				.queryParam("from", ObjectFactory.formatDate(DateUtils.addDays(new Date(), 10)))
				.queryParam("to", ObjectFactory.formatDate(DateUtils.addDays(new Date(), 14)))
				.build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		List<AssessmentModeVO> modeVoes = parseAssessmentModeArray(response.getEntity());
		// Is our assessment mode in the list?
		assertThat(modeVoes)
			.isNotNull()
			.isNotEmpty()
			.extracting(vo -> vo.getKey())
			.containsAnyOf(futureMode.getKey())
			.doesNotContain(pastMode.getKey());
		

		conn.shutdown();
	}
	
	@Test
	public void createAssessmentMode()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		AssessmentModeVO assessmentModeVo = new AssessmentModeVO();
		assessmentModeVo.setRepositoryEntryKey(entry.getKey());
		assessmentModeVo.setName("My new mode");
		String externalId = UUID.randomUUID().toString();
		assessmentModeVo.setExternalId(externalId);
		assessmentModeVo.setManagedFlagsString("all");
		assessmentModeVo.setTargetAudience(Target.course.name());
		
		Date begin = DateUtils.addDays(new Date(), 3);
		assessmentModeVo.setBegin(begin);
		assessmentModeVo.setEnd(DateUtils.addHours(begin, 2));
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));	
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, assessmentModeVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessmentModeVO savedAssessmentModeVo = conn.parse(response, AssessmentModeVO.class);
		Assert.assertNotNull(savedAssessmentModeVo);
		Assert.assertNotNull(savedAssessmentModeVo.getCreationDate());
		Assert.assertNotNull(savedAssessmentModeVo.getBegin());
		Assert.assertNotNull(savedAssessmentModeVo.getEnd());
		Assert.assertEquals("My new mode", savedAssessmentModeVo.getName());
		
		AssessmentMode savedAssessmentMode = assessmentModeMgr.getAssessmentModeById(savedAssessmentModeVo.getKey());
		Assert.assertNotNull(savedAssessmentMode);
		Assert.assertNotNull(savedAssessmentMode.getCreationDate());
		Assert.assertEquals("My new mode", savedAssessmentMode.getName());
		Assert.assertNotNull(savedAssessmentMode.getBegin());
		Assert.assertEquals(0, savedAssessmentMode.getLeadTime());
		Assert.assertNotNull(savedAssessmentMode.getEnd());
		Assert.assertEquals(0, savedAssessmentMode.getFollowupTime());
		
		Assert.assertEquals(externalId, savedAssessmentMode.getExternalId());
		Assert.assertEquals("all", savedAssessmentMode.getManagedFlagsString());
		
		conn.shutdown();
	}
	
	@Test
	public void createAssessmentModeWithCurriculumElements()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Curriculum curriculum = curriculumService.createCurriculum("CUR-MODE_1", "Curriculum modes", "Curriculum for assessment modes", false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Element-for-rel", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Element-for-del", "Element for relation",
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		dbInstance.commitAndCloseSession();
		
		
		AssessmentModeVO assessmentModeVo = new AssessmentModeVO();
		assessmentModeVo.setRepositoryEntryKey(entry.getKey());
		assessmentModeVo.setName("My new mode");
		String externalId = UUID.randomUUID().toString();
		assessmentModeVo.setExternalId(externalId);
		assessmentModeVo.setManagedFlagsString("all");
		assessmentModeVo.setTargetAudience(Target.curriculumEls.name());
		
		Date begin = DateUtils.addDays(new Date(), 3);
		assessmentModeVo.setBegin(begin);
		assessmentModeVo.setEnd(DateUtils.addHours(begin, 2));
		
		CurriculumElementVO[] elements = new CurriculumElementVO[] {
			CurriculumElementVO.valueOf(element1),
			CurriculumElementVO.valueOf(element2)
		};
		assessmentModeVo.setCurriculumElements(elements);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));	
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, assessmentModeVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessmentModeVO savedAssessmentModeVo = conn.parse(response, AssessmentModeVO.class);
		Assert.assertNotNull(savedAssessmentModeVo);
		Assert.assertEquals(Target.curriculumEls.name(), savedAssessmentModeVo.getTargetAudience());
		
		AssessmentMode savedAssessmentMode = assessmentModeMgr.getAssessmentModeById(savedAssessmentModeVo.getKey());
		Assert.assertNotNull(savedAssessmentMode);
		Assert.assertNotNull(savedAssessmentMode.getCreationDate());
		Assert.assertEquals(savedAssessmentModeVo.getKey(), savedAssessmentMode.getKey());
		
		Set<AssessmentModeToCurriculumElement> savedElements = savedAssessmentMode.getCurriculumElements();
		assertThat(savedElements)
			.isNotNull()
			.isNotEmpty()
			.extracting(savedElement -> savedElement.getCurriculumElement())
			.extracting(el -> el.getKey())
			.containsExactlyInAnyOrder(element1.getKey(), element2.getKey());
		
		conn.shutdown();
	}
	
	@Test
	public void updateAssessmentMode()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to load");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		// Update per REST
		AssessmentModeVO assessmentModeVo = new AssessmentModeVO();
		assessmentModeVo.setKey(savedMode.getKey());
		assessmentModeVo.setRepositoryEntryKey(entry.getKey());
		assessmentModeVo.setName("Updated assessment");
		String externalId = UUID.randomUUID().toString();
		assessmentModeVo.setExternalId(externalId);
		assessmentModeVo.setManagedFlagsString("general");
		
		Date begin = DateUtils.addDays(new Date(), 3);
		assessmentModeVo.setBegin(begin);
		assessmentModeVo.setLeadTime(5);
		assessmentModeVo.setEnd(DateUtils.addHours(begin, 2));
		assessmentModeVo.setFollowupTime(15);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));	
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("assessmentmodes").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, assessmentModeVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessmentModeVO updatedAssessmentModeVo = conn.parse(response, AssessmentModeVO.class);
		Assert.assertNotNull(updatedAssessmentModeVo);
		Assert.assertNotNull(updatedAssessmentModeVo.getCreationDate());
		Assert.assertNotNull(updatedAssessmentModeVo.getBegin());
		Assert.assertNotNull(updatedAssessmentModeVo.getEnd());
		Assert.assertEquals("Updated assessment", updatedAssessmentModeVo.getName());
		
		AssessmentMode updatedAssessmentMode = assessmentModeMgr.getAssessmentModeById(updatedAssessmentModeVo.getKey());
		Assert.assertNotNull(updatedAssessmentMode);
		Assert.assertNotNull(updatedAssessmentMode.getCreationDate());
		Assert.assertEquals("Updated assessment", updatedAssessmentMode.getName());
		Assert.assertNotNull(updatedAssessmentMode.getBegin());
		Assert.assertEquals(5, updatedAssessmentMode.getLeadTime());
		Assert.assertNotNull(updatedAssessmentMode.getEnd());
		Assert.assertEquals(15, updatedAssessmentMode.getFollowupTime());
		
		Assert.assertEquals(externalId, updatedAssessmentMode.getExternalId());
		Assert.assertEquals("general", updatedAssessmentMode.getManagedFlagsString());
		
		conn.shutdown();
	}
	
	@Test
	public void deleteAssessmentMode()
	throws IOException, URISyntaxException {
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentMode mode = assessmentModeMgr.createAssessmentMode(entry);
		mode.setName("Assessment to delete");
		mode.setBegin(new Date());
		mode.setEnd(new Date());
		mode.setTargetAudience(Target.course);
		AssessmentMode savedMode = assessmentModeMgr.persist(mode);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(savedMode);
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		// Search with the external ID
		URI request = UriBuilder.fromUri(getContextURI())
				.path("repo").path("assessmentmodes").path(savedMode.getKey().toString())
				.build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessmentMode deletedMode = assessmentModeMgr.getAssessmentModeById(savedMode.getKey());
		Assert.assertNull(deletedMode);
	}
	
	protected List<AssessmentModeVO> parseAssessmentModeArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(in, new TypeReference<List<AssessmentModeVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
