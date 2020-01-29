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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, "-", testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, null, true);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commit();
	}
	
	@Test
	public void getUserTestSessions() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, true);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentTestSession> sessions = testSessionDao.getUserTestSessions(courseEntry, subIdent, assessedIdentity);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, false);
		Assert.assertNotNull(testSession);
		dbInstance.commit();
		
		for(int i=0; i<4; i++) {
			ParentPartItemRefs parentParts = new ParentPartItemRefs();
			String sectionIdentifier = UUID.randomUUID().toString();
			parentParts.setSectionIdentifier(sectionIdentifier);
			String testPartIdentifier = UUID.randomUUID().toString();
			parentParts.setTestPartIdentifier(testPartIdentifier);
			AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, parentParts, UUID.randomUUID().toString());
			Assert.assertNotNull(itemSession);
			dbInstance.commit();
			
			if(i%2 == 0) {
				itemSession.setManualScore(new BigDecimal(3));
				itemSessionDao.merge(itemSession);
			}
		}

		dbInstance.commitAndCloseSession();
		
		List<AssessmentTestSessionStatistics> sessionsStatistics = testSessionDao.getUserTestSessionsStatistics(courseEntry, subIdent, assessedIdentity);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, false);
		Assert.assertNotNull(testSession1);
		dbInstance.commitAndCloseSession();
		
		//to have a time difference
		sleep(1500);
		
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, false);
		Assert.assertNotNull(testSession2);
		dbInstance.commitAndCloseSession();
		
		//load the last session
		AssessmentTestSession lastTestSession = testSessionDao.getLastTestSession(testEntry, courseEntry, subIdent, assessedIdentity, null, false);
		Assert.assertNotNull(lastTestSession);
		Assert.assertEquals(testSession2, lastTestSession);
	}
	
	@Test
	public void hasActiveTestSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry, assessedIdentity, null, false);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry, assessedIdentity, null, true);
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
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(author1, null, testEntry, null, testEntry);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(author2, null, testEntry, null, testEntry);
		AssessmentEntry assessmentEntry3 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, testEntry);
		dbInstance.commit();
		//create an assessment test session
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry1, author1, null, true);
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry2, author2, null, true);
		AssessmentTestSession testSession3 = testSessionDao.createAndPersistTestSession(testEntry, null, null, assessmentEntry3, assessedIdentity, null, false);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, testEntry, null, testEntry);
		dbInstance.commit();
		repositoryEntryRelationDao.addRole(assessedIdentity, testEntry, GroupRoles.participant.name());
		Group testDefaultGroup = repositoryEntryRelationDao.getDefaultGroup(testEntry);
		
		//create an assessment test session with a response
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(testEntry, testEntry, null, assessmentEntry, assessedIdentity, null, false);
		Assert.assertNotNull(testSession);
		AssessmentItemSession itemSession = itemSessionDao.createAndPersistAssessmentItemSession(testSession, null, UUID.randomUUID().toString());
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
