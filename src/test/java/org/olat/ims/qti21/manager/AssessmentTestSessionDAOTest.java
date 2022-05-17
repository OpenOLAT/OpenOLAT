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
package org.olat.ims.qti21.manager;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.ResponseLegality;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.types.ResponseData.ResponseDataType;

/**
 * 
 * Initial date: 02.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestSessionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentResponseDAO responseDao;
	@Autowired
	private AssessmentItemSessionDAO itemSessionDao;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Test
	public void createTestSession_repo() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", null, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, null, 300, true);
		Assert.assertNotNull(testSession);
		dbInstance.commit();
	}
	
	@Test
	public void createTestSession_course() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commit();
	}
	
	@Test
	public void loadByKey() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commit();
		
		AssessmentTestSession reloadedTestSession = testSessionDao.loadByKey(testSession.getKey());
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reloadedTestSession);
		Assert.assertEquals(testSession, reloadedTestSession);
	}
	
	@Test
	public void loadFullByKey() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commit();
		
		AssessmentTestSession reloadedTestSession = testSessionDao.loadFullByKey(testSession.getKey());
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reloadedTestSession);
		Assert.assertEquals(testSession, reloadedTestSession);
		Assert.assertEquals(assessedIdentity, reloadedTestSession.getIdentity());
		Assert.assertEquals(assessedIdentity.getUser(), reloadedTestSession.getIdentity().getUser());
	}
	
	@Test
	public void extraTime() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 360, false);
		dbInstance.commit();
		
		testSessionDao.extraTime(testSession, 20);
		dbInstance.commitAndCloseSession();
		
		AssessmentTestSession reloadedTestSession = testSessionDao.loadByKey(testSession.getKey());
		Assert.assertNotNull(reloadedTestSession);
		Assert.assertEquals(testSession, reloadedTestSession);
		Assert.assertEquals(Integer.valueOf(20), reloadedTestSession.getExtraTime());
		Assert.assertEquals(Integer.valueOf(360), reloadedTestSession.getCompensationExtraTime());
	}
	
	@Test
	public void compensationExtraTime() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-22");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commit();
		
		testSessionDao.compensationExtraTime(testSession, 320);
		dbInstance.commitAndCloseSession();
		
		AssessmentTestSession reloadedTestSession = testSessionDao.loadByKey(testSession.getKey());
		Assert.assertNotNull(reloadedTestSession);
		Assert.assertEquals(testSession, reloadedTestSession);
		Assert.assertEquals(Integer.valueOf(320), reloadedTestSession.getCompensationExtraTime());
	}
	
	
	@Test
	public void getUserTestSessions() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, true);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentTestSession> sessions = testSessionDao.getUserTestSessions(courseEntry, subIdent, assessedIdentity, true);
		Assert.assertNotNull(sessions);
		Assert.assertEquals(1, sessions.size());
		Assert.assertEquals(testSession, sessions.get(0));
	}
	
	@Test
	public void getUserTestSessionsStatistics() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, testEntry);
		dbInstance.commit();
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commit();
		
		for(int i=0; i<4; i++) {
			ParentPartItemRefs parentParts = new ParentPartItemRefs();
			String sectionIdentifier = UUID.randomUUID().toString();
			parentParts.setSectionIdentifier(sectionIdentifier);
			String testPartIdentifier = UUID.randomUUID().toString();
			parentParts.setTestPartIdentifier(testPartIdentifier);
			AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, parentParts, UUID.randomUUID().toString(), null);
			Assert.assertNotNull(itemSession);
			dbInstance.commit();
			
			if(i%2 == 0) {
				itemSession.setManualScore(new BigDecimal(3));
				itemSessionDao.merge(itemSession);
			}
		}

		dbInstance.commitAndCloseSession();
		
		List<AssessmentTestSessionStatistics> sessionsStatistics = testSessionDao.getUserTestSessionsStatistics(courseEntry, subIdent, assessedIdentity, true);
		Assert.assertNotNull(sessionsStatistics);
		Assert.assertEquals(1, sessionsStatistics.size());
		
		AssessmentTestSessionStatistics sessionStatistics = sessionsStatistics.get(0);
		Assert.assertNotNull(sessionStatistics.getTestSession());
		Assert.assertEquals(testSession, sessionStatistics.getTestSession());
		Assert.assertEquals(2, sessionStatistics.getNumOfCorrectedItems());
	}
	
	@Test
	public void getLastTestSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession1);
		dbInstance.commitAndCloseSession();
		
		//to have a time difference
		sleep(1500);
		
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession2);
		dbInstance.commitAndCloseSession();
		
		//load the last session
		AssessmentTestSession lastTestSession = testSessionDao.getLastTestSession(testEntry, courseEntry, subIdent, assessedIdentity, null, false);
		Assert.assertNotNull(lastTestSession);
		Assert.assertEquals(testSession2, lastTestSession);
	}
	
	@Test
	public void getLastUserTestSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		testSession1.setTerminationTime(new Date());
		testSession1.setFinishTime(new Date());
		testSession1 = testSessionDao.update(testSession1);
		dbInstance.commitAndCloseSession();
		
		//to have a time difference
		sleep(1500);
		
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		testSession2.setTerminationTime(new Date());
		testSession2.setFinishTime(new Date());
		testSession2 = testSessionDao.update(testSession2);
		dbInstance.commitAndCloseSession();
		
		//load the last session
		AssessmentTestSession lastTestSession = testSessionDao.getLastUserTestSession(courseEntry, subIdent, testEntry, assessedIdentity);
		Assert.assertNotNull(lastTestSession);
		Assert.assertEquals(testSession2, lastTestSession);
	}
	
	@Test
	public void getTestSessions_withIdentity() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commitAndCloseSession();
		
		//check
		List<AssessmentTestSession> testSessions = testSessionDao.getTestSessions(testEntry, courseEntry, subIdent, assessedIdentity);
		Assert.assertNotNull(testSessions);
		Assert.assertEquals(1, testSessions.size());
		Assert.assertEquals(testSession, testSessions.get(0));
	}
	
	@Test
	public void getTestSessions_withIdentity_noCourse() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commitAndCloseSession();
		
		//check
		List<AssessmentTestSession> testSessions = testSessionDao.getTestSessions(testEntry, null, null, assessedIdentity);
		assertThat(testSessions)
			.containsExactly(testSession);
	}
	
	@Test
	public void getTestSessions_noIdentity() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commitAndCloseSession();
		
		//check
		List<AssessmentTestSession> testSessions = testSessionDao.getTestSessions(courseEntry, subIdent, testEntry);
		assertThat(testSessions)
			.containsExactly(testSession);
	}
	
	@Test
	public void getTestSessions_noIdentity_noCourseIdentifier() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-21");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry, assessedIdentity, null, null, false);
		dbInstance.commitAndCloseSession();
		
		//check
		List<AssessmentTestSession> testSessions = testSessionDao.getTestSessions(testEntry, null, testEntry);
		assertThat(testSessions)
			.containsExactly(testSession);
	}
	
	@Test
	public void hasRunningTestSessions() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-14");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-15");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasRunningTestSessions = testSessionDao.hasRunningTestSessions(courseEntry, subIdent, testEntry, Collections.singletonList(assessedIdentity));
		Assert.assertTrue(hasRunningTestSessions);
		
		//check negative
		boolean hasNotRunningTestSessions = testSessionDao.hasRunningTestSessions(courseEntry, subIdent, testEntry, Collections.singletonList(otherIdentity));
		Assert.assertFalse(hasNotRunningTestSessions);
		
		// check both
		List<IdentityRef> identities = Arrays.asList(assessedIdentity, otherIdentity);
		boolean hasAtLeastOneRunningTestSession = testSessionDao.hasRunningTestSessions(courseEntry, subIdent, testEntry, identities);
		Assert.assertTrue(hasAtLeastOneRunningTestSession);
	}
	
	@Test
	public void getRunningTestSessions() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("session-17");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("session-18");
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity1, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity2, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry1, assessedIdentity1, null, null, false);
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry2, assessedIdentity2, null, null, false);
		dbInstance.commitAndCloseSession();

		List<AssessmentTestSession> testSessions = testSessionDao.getRunningTestSessions(courseEntry, subIdent, testEntry);
		assertThat(testSessions)
			.containsExactlyInAnyOrder(testSession1, testSession2);
	}
	
	@Test
	public void hasRunningTestSessions_noCourse() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-14");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-15");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasRunningTestSessions = testSessionDao.hasRunningTestSessions(testEntry, null, testEntry, Collections.singletonList(assessedIdentity));
		Assert.assertTrue(hasRunningTestSessions);
		
		//check negative
		boolean hasNotRunningTestSessions = testSessionDao.hasRunningTestSessions(testEntry, null, testEntry, Collections.singletonList(otherIdentity));
		Assert.assertFalse(hasNotRunningTestSessions);
		
		// check both
		List<IdentityRef> identities = Arrays.asList(assessedIdentity, otherIdentity);
		boolean hasAtLeastOneRunningTestSession = testSessionDao.hasRunningTestSessions(testEntry, null, testEntry, identities);
		Assert.assertTrue(hasAtLeastOneRunningTestSession);
	}
	
	@Test
	public void hasRunningTestSessions_subIdentList() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-30");
		Identity otherIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-31");
		String subIdent = "OO-1234";
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, subIdent, Boolean.FALSE, testEntry);
		dbInstance.commit();
		
		//create an assessment test session
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, subIdent, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasRunningTestSessions = testSessionDao.hasRunningTestSessions(testEntry, List.of(subIdent), List.of(assessedIdentity));
		Assert.assertTrue(hasRunningTestSessions);
		
		boolean hasNotRunningTestSessions = testSessionDao.hasRunningTestSessions(testEntry, List.of(subIdent), List.of(otherIdentity));
		Assert.assertFalse(hasNotRunningTestSessions);
	}
	
	@Test
	public void getAllUserTestSessions() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-20");
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, Boolean.FALSE, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry1, assessedIdentity, null, null, false);
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry1, assessedIdentity, null, null, false);
		AssessmentTestSession testSession3 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry2, assessedIdentity, null, null, false);
		dbInstance.commitAndCloseSession();

		List<AssessmentTestSession> testSessions = testSessionDao.getAllUserTestSessions(assessedIdentity);
		assertThat(testSessions)
			.containsExactlyInAnyOrder(testSession1, testSession2, testSession3);
	}
	
	@Test
	public void hasActiveTestSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, null, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession1);
		dbInstance.commitAndCloseSession();
		
		//check
		boolean hasTestSession = testSessionDao.hasActiveTestSession(testEntry);
		Assert.assertTrue(hasTestSession);
	}

	@Test
	public void hasActiveTestSession_negative() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-5");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, null, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry, assessedIdentity, null, null, true);
		Assert.assertNotNull(testSession1);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any active test session (only author mode)
		boolean hasTestSession = testSessionDao.hasActiveTestSession(testEntry);
		Assert.assertFalse(hasTestSession);
	}
	
	@Test
	public void getAuthorAssessmentTestSession() {
		// prepare a test and 2 users
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author1 = JunitTestHelper.createAndPersistIdentityAsRndUser("session-6");
		Identity author2 = JunitTestHelper.createAndPersistIdentityAsRndUser("session-7");
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-8");
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(author1, null, testEntry, null, null, testEntry);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(author2, null, testEntry, null, null, testEntry);
		AssessmentEntry assessmentEntry3 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, null, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry1, author1, null, null, true);
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry2, author2, null, null, true);
		AssessmentTestSession testSession3 = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry3, assessedIdentity, null, null, false);
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any active test session (only author mode)
		List<AssessmentTestSession> authorSessions = testSessionDao.getAuthorAssessmentTestSession(testEntry);
		Assert.assertNotNull(authorSessions);
		Assert.assertEquals(2, authorSessions.size());
		Assert.assertTrue(authorSessions.contains(testSession1));
		Assert.assertTrue(authorSessions.contains(testSession2));
		Assert.assertFalse(authorSessions.contains(testSession3));
	}
	
	
	@Test
	public void getTestSessionsOfResponse_groupsAndIdentities() {
		// prepare a test and 2 users
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-9");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, null, testEntry);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(assessedIdentity, testEntry, GroupRoles.participant.name());
		Group testDefaultGroup = repositoryEntryRelationDao.getDefaultGroup(testEntry);
		
		//create an assessment test session with a response
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry, assessedIdentity, null, null, false);
		Assert.assertNotNull(testSession);
		AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, null, UUID.randomUUID().toString(), null);
		Assert.assertNotNull(itemSession);
		AssessmentResponse response = responseDao.createAssessmentResponse(testSession, itemSession, UUID.randomUUID().toString(), ResponseLegality.VALID, ResponseDataType.FILE);
		Assert.assertNotNull(response);
		
		dbInstance.commitAndCloseSession();
		
		// only finished count
		testSession.setDuration(100l);
		testSession.setFinishTime(new Date());
		testSession = testSessionDao.update(testSession);
		
		dbInstance.commitAndCloseSession();
		
		//check that there isn't any active test session (only author mode)
		ArchiveOptions options = new ArchiveOptions();
		options.setIdentities(Collections.singletonList(assessedIdentity));
		QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, testEntry, null, null);
		searchParams.setLimitToGroups(Collections.singletonList(testDefaultGroup));
		searchParams.setLimitToIdentities(Collections.singletonList(assessedIdentity));
		
		List<AssessmentTestSession> authorSessions = testSessionDao.getTestSessionsOfResponse(searchParams);
		Assert.assertNotNull(authorSessions);
		Assert.assertEquals(1, authorSessions.size());
		Assert.assertEquals(testSession, authorSessions.get(0));
	}
}
