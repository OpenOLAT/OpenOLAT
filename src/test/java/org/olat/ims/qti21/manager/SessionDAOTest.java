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

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.UserTestSession;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SessionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private SessionDAO testSessionDao;
	@Autowired
	private AssessmentService assessmentService;
	
	@Test
	public void createTestSession_repo() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, testEntry, "-", testEntry);
		dbInstance.commit();
		
		UserTestSession testSession = testSessionDao.createTestSession(testEntry, testEntry, "-", assessmentEntry, assessedIdentity, true);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		
		UserTestSession testSession = testSessionDao.createTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, false);
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
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		
		UserTestSession testSession = testSessionDao.createTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, true);
		Assert.assertNotNull(testSession);
		dbInstance.commitAndCloseSession();
		
		List<UserTestSession> sessions = testSessionDao.getUserTestSessions(courseEntry, subIdent, assessedIdentity);
		Assert.assertNotNull(sessions);
		Assert.assertEquals(1, sessions.size());
		Assert.assertEquals(testSession, sessions.get(0));
	}
	
	@Test
	public void getLastTestSession() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("session-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, courseEntry, subIdent, testEntry);
		dbInstance.commit();
		
		UserTestSession testSession1 = testSessionDao.createTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, false);
		Assert.assertNotNull(testSession1);
		dbInstance.commitAndCloseSession();
		
		//to have a time difference
		sleep(1500);
		
		UserTestSession testSession2 = testSessionDao.createTestSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, false);
		Assert.assertNotNull(testSession2);
		dbInstance.commitAndCloseSession();
		
		//load the last session
		UserTestSession lastTestSession = testSessionDao.getLastTestSession(testEntry, courseEntry, subIdent, assessedIdentity);
		Assert.assertNotNull(lastTestSession);
		Assert.assertEquals(testSession2, lastTestSession);
	}

}
