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
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
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
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.model.GradeScaleWrapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.restapi.support.vo.AssessableResultsVO;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CourseAssessmentWebServiceTest extends OlatRestTestCase {

	private static final Logger log = Tracing.createLoggerFor(CourseAssessmentWebServiceTest.class);
	
	private static final String QTI_NODE_IDENT = "103769899903897";
	private static final String GTA_NODE_IDENT = "96185428288542";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	private static Organisation defaultUnitTestOrganisation;
	private static IdentityWithLogin defaultUnitTestAdministrator;
	
	@Before
	public void initDefaultUnitTestOrganisation() {
		if(defaultUnitTestOrganisation == null) {
			defaultUnitTestOrganisation = organisationService
					.createOrganisation("Org-course-assessment-unit-test", "Org-course-assessment-unit-test", "", null, null, JunitTestHelper.getDefaultActor());
			defaultUnitTestAdministrator = JunitTestHelper
					.createAndPersistRndAdmin("Cur-Elem-Web", defaultUnitTestOrganisation);
		}
	}
	
	@Test
	public void getCourseRootResultsAllParticipants()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		
		List<AssessableResultsVO> results = conn.parseList(response, AssessableResultsVO.class);
		Assert.assertNotNull(results);
		Assert.assertEquals(1, results.size());
		
		AssessableResultsVO result = results.get(0);
		Assert.assertEquals(participant.getKey(), result.getIdentityKey());
		Assert.assertNull(result.getAssessmentStatus());
		Assert.assertNotNull(result.getLastModifiedDate());
		Assert.assertNull(result.getLastUserModified());
		Assert.assertNull(result.getLastCoachModified());

		conn.shutdown();
	}
	
	@Test
	public void getCourseRootResultsByIdentityKey()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
	public void getCourseNodeResults()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		
		List<AssessableResultsVO> results = conn.parseList(response, AssessableResultsVO.class);
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
	public void getCourseNodeResultsByIdentity()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
	
	@Test
	public void getCourseNodeResultsByIdentityWithScore()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
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
		
		ScoreEvaluation userEval = new ScoreEvaluation(3.0f, 3.0f, new BigDecimal("1"), null, null, null, Boolean.FALSE,
				AssessmentEntryStatus.inProgress, Boolean.FALSE, new Date(), 50.0d, AssessmentRunStatus.running, null);
		courseAssessmentService.updateScoreEvaluation(courseNode, userEval, assessedUserCourseEnv, coach, true, Role.user);
		dbInstance.commitAndCloseSession();
		
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
		Assert.assertEquals(3.0f, result.getWeightedScore().floatValue(), 0.00001);
		// Not define
		Assert.assertNull(result.getWeightedMaxScore());
		Assert.assertEquals(Boolean.FALSE, result.getPassed());
		Assert.assertEquals(1, result.getAttempts().intValue());
		Assert.assertNull(result.getLastCoachModified());
		Assert.assertNotNull(result.getLastUserModified());
		Assert.assertEquals(AssessmentEntryStatus.inProgress.name(), result.getAssessmentStatus());
		Assert.assertNull(result.getAssessmentDone());
		
		// Update from coach
		ScoreEvaluation coachEval = new ScoreEvaluation(5.0f, 7.5f, new BigDecimal("1.5"), null, null, null, Boolean.TRUE,
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
	
	@Test
	public void postCourseNodeResultsByIdentityWithScore()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		ICourse course = deployGTACourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-13");
		Roles roles = securityManager.getRoles(participant);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant));
		repositoryManager.addParticipants(participant, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant, course);
		
		GTACourseNode courseNode = (GTACourseNode)course.getRunStructure().getNode(GTA_NODE_IDENT);
		
		AssessableResultsVO result = new AssessableResultsVO();
		result.setIdentityKey(participant.getKey());
		result.setNodeIdent(courseNode.getIdent());
		result.setScore(Float.valueOf(1.0f));
		result.setPassed(Boolean.FALSE);
		
		// Attempts user
		URI uri = getCourseURI(course).path(GTA_NODE_IDENT).build();
		HttpPut put = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(put, result);
		HttpResponse response = conn.execute(put);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());
		
		// Check the results saved on the database
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(participant, course.getCourseEnvironment());
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		Assert.assertNotNull(assessmentEntry);
		Assert.assertEquals(participant, assessmentEntry.getIdentity());
		Assert.assertEquals(Boolean.FALSE, assessmentEntry.getPassed());
		Assert.assertTrue(new BigDecimal("1.0").compareTo(assessmentEntry.getScore()) == 0);
		Assert.assertTrue(new BigDecimal("10.0").compareTo(assessmentEntry.getMaxScore()) == 0);
		Assert.assertTrue(new BigDecimal("1.0").compareTo(assessmentEntry.getScoreScale()) == 0);
		Assert.assertTrue(new BigDecimal("1.0").compareTo(assessmentEntry.getWeightedScore()) == 0);
		Assert.assertEquals(Boolean.TRUE, assessmentEntry.getUserVisibility());
	
		conn.shutdown();
	}
	
	@Test
	public void postCourseNodeResultsByIdentityWithAttemptsAndCompletion()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		ICourse course = deployGTACourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-13");
		Roles roles = securityManager.getRoles(participant);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant));
		repositoryManager.addParticipants(participant, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant, course);
		
		GTACourseNode courseNode = (GTACourseNode)course.getRunStructure().getNode(GTA_NODE_IDENT);
		
		AssessableResultsVO result = new AssessableResultsVO();
		result.setIdentityKey(participant.getKey());
		result.setNodeIdent(courseNode.getIdent());
		result.setScore(Float.valueOf(8.0f));
		result.setPassed(Boolean.TRUE);
		result.setAttempts(Integer.valueOf(3));
		result.setUserVisible(Boolean.FALSE);
		result.setCompletion(Double.valueOf(0.8d));
		
		// Attempts user
		URI uri = getCourseURI(course).path(GTA_NODE_IDENT).build();
		HttpPut put = conn.createPut(uri, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(put, result);
		HttpResponse response = conn.execute(put);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());
		
		// Check the results saved on the database
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(participant, course.getCourseEnvironment());
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		Assert.assertNotNull(assessmentEntry);
		Assert.assertEquals(participant, assessmentEntry.getIdentity());
		Assert.assertEquals(Boolean.TRUE, assessmentEntry.getPassed());
		Assert.assertTrue(new BigDecimal("8.0").compareTo(assessmentEntry.getScore()) == 0);
		Assert.assertTrue(new BigDecimal("10.0").compareTo(assessmentEntry.getMaxScore()) == 0);
		Assert.assertTrue(new BigDecimal("1.0").compareTo(assessmentEntry.getScoreScale()) == 0);
		Assert.assertTrue(new BigDecimal("8.0").compareTo(assessmentEntry.getWeightedScore()) == 0);
		Assert.assertEquals(Boolean.FALSE, assessmentEntry.getUserVisibility());
		Assert.assertEquals(Integer.valueOf(3), assessmentEntry.getAttempts());
		Assert.assertEquals(0.8d, assessmentEntry.getCompletion().doubleValue(), 0.001);
	
		conn.shutdown();
	}
	
	@Test
	public void postCourseNodeResultsByIdentityWithGradeAuto()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		ICourse course = deployGTAAutoGradedCourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-13");
		Roles roles = securityManager.getRoles(participant);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant));
		repositoryManager.addParticipants(participant, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant, course);
		
		GTACourseNode courseNode = (GTACourseNode)course.getRunStructure().getNode(GTA_NODE_IDENT);
		
		AssessableResultsVO result = new AssessableResultsVO();
		result.setIdentityKey(participant.getKey());
		result.setNodeIdent(courseNode.getIdent());
		result.setScore(Float.valueOf(20.0f));
		result.setPassed(Boolean.TRUE);
		result.setAttempts(Integer.valueOf(1));
		result.setUserVisible(Boolean.TRUE);
		result.setCompletion(Double.valueOf(1.0d));

		// Attempts user
		URI uri = getCourseURI(course).path(GTA_NODE_IDENT).build();
		HttpPost post = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(post, result);
		HttpResponse response = conn.execute(post);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());
		
		// Check the results saved on the database
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(participant, course.getCourseEnvironment());
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		Assert.assertNotNull(assessmentEntry);
		Assert.assertEquals(participant, assessmentEntry.getIdentity());
		
		Assert.assertEquals(Boolean.TRUE, assessmentEntry.getPassed());
		Assert.assertEquals("4", assessmentEntry.getGrade());
		Assert.assertTrue(new BigDecimal("20.0").compareTo(assessmentEntry.getScore()) == 0);
		Assert.assertTrue(new BigDecimal("30.0").compareTo(assessmentEntry.getMaxScore()) == 0);
		Assert.assertTrue(new BigDecimal("1.0").compareTo(assessmentEntry.getScoreScale()) == 0);
		Assert.assertTrue(new BigDecimal("20.0").compareTo(assessmentEntry.getWeightedScore()) == 0);
		
		Assert.assertEquals(Boolean.TRUE, assessmentEntry.getUserVisibility());
		Assert.assertEquals(Integer.valueOf(1), assessmentEntry.getAttempts());
		Assert.assertEquals(1.0d, assessmentEntry.getCompletion().doubleValue(), 0.001);
	}
	
	@Test
	public void postCourseNodeResultsByIdentityWithGradeManual()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection(defaultUnitTestAdministrator);
		
		ICourse course = deployGTAManualGradedCourse();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-course-assessment-14");
		Roles roles = securityManager.getRoles(participant);
		IdentitiesAddEvent identitiesAddedEvent = new IdentitiesAddEvent(List.of(participant));
		repositoryManager.addParticipants(participant, roles, identitiesAddedEvent, courseEntry, null);
		
		waitAssessmentEntries(participant, course);
		
		GTACourseNode courseNode = (GTACourseNode)course.getRunStructure().getNode(GTA_NODE_IDENT);
		
		AssessableResultsVO result = new AssessableResultsVO();
		result.setIdentityKey(participant.getKey());
		result.setNodeIdent(courseNode.getIdent());
		result.setScore(Float.valueOf(26.0f));
		result.setPassed(Boolean.FALSE);
		result.setGrade("4.5");
		result.setAttempts(Integer.valueOf(1));
		result.setUserVisible(Boolean.TRUE);
		result.setCompletion(Double.valueOf(1.0d));

		// Attempts user
		URI uri = getCourseURI(course).path(GTA_NODE_IDENT).build();
		HttpPost post = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(post, result);
		HttpResponse response = conn.execute(post);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(response.getEntity());
		
		// Check the results saved on the database
		UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(participant, course.getCourseEnvironment());
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		Assert.assertNotNull(assessmentEntry);
		Assert.assertEquals(participant, assessmentEntry.getIdentity());
		
		Assert.assertEquals(Boolean.FALSE, assessmentEntry.getPassed());
		Assert.assertEquals("4.5", assessmentEntry.getGrade());
		Assert.assertTrue(new BigDecimal("26.0").compareTo(assessmentEntry.getScore()) == 0);
		Assert.assertTrue(new BigDecimal("30.0").compareTo(assessmentEntry.getMaxScore()) == 0);
		Assert.assertTrue(new BigDecimal("1.0").compareTo(assessmentEntry.getScoreScale()) == 0);
		Assert.assertTrue(new BigDecimal("26.0").compareTo(assessmentEntry.getWeightedScore()) == 0);
		
		Assert.assertEquals(Boolean.TRUE, assessmentEntry.getUserVisibility());
		Assert.assertEquals(Integer.valueOf(1), assessmentEntry.getAttempts());
		Assert.assertEquals(1.0d, assessmentEntry.getCompletion().doubleValue(), 0.001);
	}
	
	private UriBuilder getCourseURI(ICourse course) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses").path(course.getResourceableId().toString())
			.path("assessments");
	}
	
	private void waitAssessmentEntries(Identity identity, ICourse course) {
		final RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		final String rootIdent = course.getRunStructure().getRootNode().getIdent();
		
		waitForCondition(() -> {
			AssessmentEntry entry = assessmentEntryDao.loadAssessmentEntry(identity, courseEntry, rootIdent);
			dbInstance.commitAndCloseSession();
			return entry != null;
		}, 10000);
	}
	
	private ICourse deployQtiCourse() {
		URL courseUrl = OlatRestTestCase.class.getResource("file_resources/course_with_qti21.zip");
		RepositoryEntry courseEntry = JunitTestHelper.deployCourse(defaultUnitTestAdministrator.getIdentity(), "QTI 2.1 Course",
				RepositoryEntryStatusEnum.published, courseUrl, defaultUnitTestOrganisation);
		return CourseFactory.loadCourse(courseEntry);
	}
	
	private ICourse deployGTACourse() {
		URL courseUrl = JunitTestHelper.class.getResource("file_resources/GTA_0_10_Course.zip");
		RepositoryEntry courseEntry = JunitTestHelper.deployCourse(defaultUnitTestAdministrator.getIdentity(), "GTA Course",
				RepositoryEntryStatusEnum.published, courseUrl, defaultUnitTestOrganisation);
		return CourseFactory.loadCourse(courseEntry);
	}
	
	private ICourse deployGTAAutoGradedCourse() {
		URL courseUrl = null;
		try {
			courseUrl = JunitTestHelper.class.getResource("file_resources/GTA_auto_graded_course.zip");
			File courseFile = new File(courseUrl.toURI());
			return deployGTAGradedCourse(courseFile);
		} catch(URISyntaxException e) {
			log.error("Cannot read course file: {}", courseUrl, e);
			return null;
		}
	}
	
	private ICourse deployGTAManualGradedCourse() {
		URL courseUrl = null;
		try {
			courseUrl = JunitTestHelper.class.getResource("file_resources/GTA_manual_graded_course.zip");
			File courseFile = new File(courseUrl.toURI());
			return deployGTAGradedCourse(courseFile);
		} catch(URISyntaxException e) {
			log.error("Cannot read course file: {}", courseUrl, e);
			return null;
		}
	}
		
	private ICourse deployGTAGradedCourse(File courseFile) {
		RepositoryHandler courseHandler = RepositoryHandlerFactory.getInstance()
					.getRepositoryHandler(CourseModule.getCourseTypeName());
		RepositoryEntry courseEntry = courseHandler.importResource(defaultUnitTestAdministrator.getIdentity(), null, "GTA graded", "A course",
				RepositoryEntryImportExportLinkEnum.WITH_REFERENCE, defaultUnitTestOrganisation, Locale.ENGLISH, courseFile, null);
		
		GradeSystem gradeSystem = gradeService.createGradeSystem(random(), GradeSystemType.numeric);
		gradeSystem.setLowestGrade(Integer.valueOf(0));
		gradeSystem.setEnabled(true);
		gradeSystem.setPassed(true);
		gradeSystem.setCutValue(new BigDecimal("4.0"));
		gradeSystem.setBestGrade(Integer.valueOf(6));
		gradeSystem.setResolution(NumericResolution.half);
		gradeSystem = gradeService.updateGradeSystem(gradeSystem);
		
		GradeScale gradeScale = new GradeScaleWrapper();
		gradeScale.setGradeSystem(gradeSystem);
		gradeScale.setMinScore(BigDecimal.valueOf(0.0));
		gradeScale.setMaxScore(BigDecimal.valueOf(30.0));
		gradeScale = gradeService.updateOrCreateGradeScale(courseEntry, GTA_NODE_IDENT, gradeScale);
		dbInstance.commit();
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseFactory.publishCourse(course, RepositoryEntryStatusEnum.published, defaultUnitTestAdministrator.getIdentity(), Locale.ENGLISH);
		return CourseFactory.loadCourse(courseEntry);
	}
}
