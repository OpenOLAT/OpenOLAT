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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.restapi.AssessmentEntryVO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.manager.GraderToIdentityDAO;
import org.olat.modules.grading.manager.GradingAssignmentDAO;
import org.olat.modules.grading.restapi.GradingAssignmentUserVisibilityVO;
import org.olat.modules.grading.restapi.GradingAssignmentWithInfosVO;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 8 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(GradingWebServiceTest.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	
	@Test
	public void testWithGrading() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getGradingUriBuilder().path("assignments").path("tests").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<RepositoryEntryVO> entriesVo = parseRepoArray(response.getEntity());
		Assert.assertNotNull(entriesVo);
		
		
		conn.shutdown();
	}
	
	@Test
	public void testAssignmentInfos() throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);	
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getGradingUriBuilder().path("test").path(entry.getKey().toString()).path("assignments").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		List<GradingAssignmentWithInfosVO> infosVoes = parseInfosArray(response.getEntity());
		Assert.assertNotNull(infosVoes);
		Assert.assertEquals(1, infosVoes.size());
		
		GradingAssignmentWithInfosVO infoVo = infosVoes.get(0);
		Assert.assertEquals(student.getKey(), infoVo.getAssessedIdentityKey());
		Assert.assertEquals(assignment.getAssignmentStatus().name(), infoVo.getAssignmentStatus());
		Assert.assertNotNull(infoVo.getAssessmentEntry());
		
		AssessmentEntryVO assessmentEntryVo = infoVo.getAssessmentEntry();
		
		// still not set
		Assert.assertNull(assessmentEntryVo.getAssessmentStatus());
		Assert.assertEquals(assessment.getKey(), assessmentEntryVo.getKey());
		Assert.assertEquals(entry.getKey(), assessmentEntryVo.getReferenceEntryKey());
		Assert.assertEquals(entry.getKey(), assessmentEntryVo.getRepositoryEntryKey());
		Assert.assertNotNull(infoVo.getAssessmentEntry());
		
		conn.shutdown();
	}
	
	@Test
	public void getTestUserVisibility() throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);	
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getGradingUriBuilder().path("test").path(entry.getKey().toString()).path("assignments")
				.path(assignment.getKey().toString()).path("uservisibility").build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		GradingAssignmentUserVisibilityVO userVisibility = conn.parse(response.getEntity(), GradingAssignmentUserVisibilityVO.class);
		Assert.assertNotNull(userVisibility);
		Assert.assertEquals(assignment.getKey(), userVisibility.getAssignmentKey());
		conn.shutdown();
	}
	
	/*
	@Test
	public void postTestUserVisibility() throws IOException, URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);	
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignmentUserVisibilityVO userVisibility = new GradingAssignmentUserVisibilityVO();
		userVisibility.setUserVisibility(Boolean.TRUE);
		userVisibility.setAssignmentKey(assignment.getKey());
		
		RestConnection conn = new RestConnection();
		assertTrue(conn.login("administrator", "openolat"));
		
		URI uri = getGradingUriBuilder().path("test").path(entry.getKey().toString()).path("assignments")
				.path(assignment.getKey().toString()).path("uservisibility").build();
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, userVisibility);
		HttpResponse response = conn.execute(method);
		assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		conn.shutdown();
	}
	*/
	
	private UriBuilder getGradingUriBuilder() {
		return UriBuilder.fromUri(getContextURI()).path("grading");
	}
	
	private List<GradingAssignmentWithInfosVO> parseInfosArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<GradingAssignmentWithInfosVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private List<RepositoryEntryVO> parseRepoArray(HttpEntity entity) {
		try(InputStream in=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<RepositoryEntryVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

}
