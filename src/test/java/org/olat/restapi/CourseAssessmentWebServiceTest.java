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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.restapi.support.vo.AssessableResultsVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 31 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseAssessmentWebServiceTest extends OlatRestTestCase {

	private static final String QTI_NODE_IDENT = "103769899903897";
	private static final String GTA_NODE_IDENT = "96185428288542";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	
	@Test
	public void getCourseRootResultsAllParticipants() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		ICourse course = deployQtiCourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-1");
		Roles roles = securityManager.getRoles(participant);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(participant);
		repositoryManager.addParticipants(participant, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant, course);

		URI uri = getCourseURI(course).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<AssessableResultsVO> results = parseResultsArray(response.getEntity());
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		
		AssessableResultsVO result = results.get(0);
		Assert.assertEquals(participant.getKey(), result.getIdentityKey());
		Assert.assertEquals(AssessmentRunStatus.notStarted.name(), result.getAssessmentStatus());
		Assert.assertNotNull(result.getLastModifiedDate());
		Assert.assertNull(result.getLastUserModified());
		Assert.assertNull(result.getLastCoachModified());

		conn.shutdown();
	}
	
	@Test
	public void getCourseRootResultsByIdentityKey() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		ICourse course = deployQtiCourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-4");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-5");
		Roles roles = securityManager.getRoles(participant1);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant1, participant2));
		repositoryManager.addParticipants(participant1, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant1, course);
		waitAssessmentEntries(participant2, course);

		URI uri = getCourseURI(course).path("users").path(participant2.getKey().toString()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessableResultsVO result = conn.parse(response.getEntity(), AssessableResultsVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(participant2.getKey(), result.getIdentityKey());
		Assert.assertNotNull(result.getLastModifiedDate());
		Assert.assertNull(result.getLastUserModified());
		Assert.assertNull(result.getLastCoachModified());

		conn.shutdown();
	}
	
	@Test
	public void getCourseNodeResults() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		ICourse course = deployQtiCourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-6");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-7");
		Roles roles = securityManager.getRoles(participant1);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant1, participant2));
		repositoryManager.addParticipants(participant1, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant1, course);
		waitAssessmentEntries(participant2, course);

		URI uri = getCourseURI(course).path(QTI_NODE_IDENT).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		List<AssessableResultsVO> results = parseResultsArray(response.getEntity());
		Assert.assertNotNull(results);
		Assert.assertEquals(2, results.size());
		for(AssessableResultsVO result:results) {
			Assert.assertTrue(participant1.getKey().equals(result.getIdentityKey()) || participant2.getKey().equals(result.getIdentityKey()));
			Assert.assertEquals(QTI_NODE_IDENT, result.getNodeIdent());
			Assert.assertNull(result.getAssessmentStatus());
		}

		conn.shutdown();
	}
	
	@Test
	public void getCourseNodeResultsByIdentity() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		ICourse course = deployQtiCourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity participant1 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-8");
		Identity participant2 = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-9");
		Roles roles = securityManager.getRoles(participant1);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant1, participant2));
		repositoryManager.addParticipants(participant1, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant1, course);
		waitAssessmentEntries(participant2, course);

		URI uri = getCourseURI(course).path(QTI_NODE_IDENT).path("users").path(participant1.getKey().toString()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessableResultsVO result = conn.parse(response.getEntity(), AssessableResultsVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(participant1.getKey(), result.getIdentityKey());
		Assert.assertNull(result.getAssessmentStatus());

		conn.shutdown();
	}
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@Test
	public void getCourseNodeResultsByIdentityWithScore() throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login("administrator", "openolat"));
		
		ICourse course = deployGTACourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity coach = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-10");
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-11");
		Roles roles = securityManager.getRoles(participant);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant));
		repositoryManager.addParticipants(participant, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant, course);
		
		GTACourseNode courseNode = (GTACourseNode)course.getRunStructure().getNode(GTA_NODE_IDENT);
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(participant, course.getCourseEnvironment());
		
		ScoreEvaluation userEval = new ScoreEvaluation(3.0f, null, null, null, Boolean.FALSE,
				AssessmentEntryStatus.inProgress, Boolean.FALSE, new Date(), 50.0d, AssessmentRunStatus.running, null);
		courseAssessmentService.updateScoreEvaluation(courseNode, userEval, assessedUserCourseEnv, coach, true, Role.user);
		
		// Attempts user
		
		URI uri = getCourseURI(course).path(GTA_NODE_IDENT).path("users").path(participant.getKey().toString()).build();
		HttpGet get = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(get);
		assertEquals(200, response.getStatusLine().getStatusCode());
		
		AssessableResultsVO result = conn.parse(response.getEntity(), AssessableResultsVO.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(participant.getKey(), result.getIdentityKey());
		Assert.assertEquals(GTA_NODE_IDENT, result.getNodeIdent());
		Assert.assertEquals(3.0f, result.getScore().floatValue(), 0.00001);
		Assert.assertEquals(10.0f, result.getMaxScore().floatValue(), 0.00001);
		Assert.assertEquals(Boolean.FALSE, result.getPassed());
		Assert.assertEquals(1, result.getAttempts().intValue());
		Assert.assertNull(result.getLastCoachModified());
		Assert.assertNotNull(result.getLastUserModified());
		Assert.assertEquals(AssessmentEntryStatus.inProgress.name(), result.getAssessmentStatus());
		Assert.assertNull(result.getAssessmentDone());
		
		// Update from coach
		ScoreEvaluation coachEval = new ScoreEvaluation(5.0f, null, null, null, Boolean.TRUE,
				AssessmentEntryStatus.done, Boolean.TRUE, new Date(), 50.0d, AssessmentRunStatus.done, null);
		courseAssessmentService.updateScoreEvaluation(courseNode, coachEval, assessedUserCourseEnv, coach, false, Role.coach);
		
		URI uriCoach = getCourseURI(course).path(GTA_NODE_IDENT).path("users").path(participant.getKey().toString()).build();
		HttpGet getCoach = conn.createGet(uriCoach, MediaType.APPLICATION_JSON, true);
		HttpResponse responseCoach = conn.execute(getCoach);
		assertEquals(200, responseCoach.getStatusLine().getStatusCode());
		
		AssessableResultsVO resultCoach = conn.parse(responseCoach.getEntity(), AssessableResultsVO.class);
		Assert.assertNotNull(resultCoach);
		Assert.assertEquals(participant.getKey(), resultCoach.getIdentityKey());
		Assert.assertEquals(GTA_NODE_IDENT, resultCoach.getNodeIdent());
		Assert.assertEquals(5.0f, resultCoach.getScore().floatValue(), 0.00001);
		Assert.assertEquals(10.0f, resultCoach.getMaxScore().floatValue(), 0.00001);
		Assert.assertEquals(Boolean.TRUE, resultCoach.getPassed());
		Assert.assertEquals(1, resultCoach.getAttempts().intValue());
		Assert.assertNotNull(resultCoach.getLastCoachModified());
		Assert.assertNotNull(resultCoach.getLastUserModified());
		Assert.assertEquals(AssessmentEntryStatus.done.name(), resultCoach.getAssessmentStatus());
		Assert.assertNotNull(resultCoach.getAssessmentDone());

		conn.shutdown();
	}

	
	private UriBuilder getCourseURI(ICourse course) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course.getResourceableId().toString())
			.path("assessments");
	}
	
	private void waitAssessmentEntries(Identity identity, ICourse course) {
		final RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		final String rootIdent = course.getRunStructure().getRootNode().getIdent();
		
		this.waitForCondition(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				AssessmentEntry entry = assessmentEntryDao.loadAssessmentEntry(identity, courseEntry, rootIdent);
				dbInstance.commitAndCloseSession();
				return entry != null;
			}
		}, 10000);
	}
	
	private ICourse deployQtiCourse() {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		URL courseUrl = OlatRestTestCase.class.getResource("file_resources/course_with_qti21.zip");
		RepositoryEntry courseEntry = JunitTestHelper.deployCourse(admin, "QTI 2.1 Course", courseUrl);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.published, true, false, admin, Locale.ENGLISH);
		return course;
	}
	
	private ICourse deployGTACourse() {
		Identity admin = JunitTestHelper.findIdentityByLogin("administrator");
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/GTA_0_10_Course.zip");

		RepositoryEntry courseEntry = JunitTestHelper.deployCourse(admin, "GTA Course", courseUrl);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.published, true, false, admin, Locale.ENGLISH);
		return course;
	}
	
	private List<AssessableResultsVO> parseResultsArray(HttpEntity body) {
		try(InputStream in=body.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(in, new TypeReference<List<AssessableResultsVO>>(){/* */});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
