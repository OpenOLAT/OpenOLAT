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
package org.olat.modules.video.manager;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.video.VideoTaskSession;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskSessionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private VideoTaskSessionDAO taskSessionDao;
	
	/**
	 * Both the reference entries are the video repository entry
	 */
	@Test
	public void createTaskSession_repo() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-1");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);
		dbInstance.commit();
		
		VideoTaskSession taskSession = taskSessionDao.createAndPersistTaskSession(videoEntry, videoEntry, "-", assessmentEntry, assessedIdentity, null, 1, true);
		Assert.assertNotNull(taskSession);
		dbInstance.commit();
		
		Assert.assertNotNull(taskSession.getCreationDate());
		Assert.assertNotNull(taskSession.getLastModified());
		Assert.assertEquals(assessedIdentity, taskSession.getIdentity());
		Assert.assertEquals(assessmentEntry, taskSession.getAssessmentEntry());
		Assert.assertEquals(videoEntry, taskSession.getVideoEntry());
		Assert.assertEquals(videoEntry, taskSession.getRepositoryEntry());
	}
	
	@Test
	public void createTasktSession_course() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-2");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);
		dbInstance.commit();
		
		VideoTaskSession taskSession = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, "263478236478", assessmentEntry, assessedIdentity, null, 1, true);
		Assert.assertNotNull(taskSession);
		dbInstance.commit();
		
		Assert.assertNotNull(taskSession.getCreationDate());
		Assert.assertNotNull(taskSession.getLastModified());
		Assert.assertEquals(assessedIdentity, taskSession.getIdentity());
		Assert.assertEquals(assessmentEntry, taskSession.getAssessmentEntry());
		Assert.assertEquals(videoEntry, taskSession.getVideoEntry());
		Assert.assertEquals(courseEntry, taskSession.getRepositoryEntry());
	}
	
	@Test
	public void loadByKey() {
		// prepare a test and a user
		RepositoryEntry testEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-3");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, Boolean.FALSE, testEntry);
		VideoTaskSession taskSession = taskSessionDao.createAndPersistTaskSession(testEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 2, false);
		dbInstance.commit();
		
		VideoTaskSession reloadedTestSession = taskSessionDao.loadByKey(taskSession.getKey());
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(reloadedTestSession);
		Assert.assertEquals(taskSession, reloadedTestSession);
	}
	
	@Test
	public void getLastTasktSession() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, videoEntry);
		dbInstance.commit();
		
		VideoTaskSession taskSession1 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 1, false);
		Assert.assertNotNull(taskSession1);
		dbInstance.commitAndCloseSession();
		
		// To have a time difference
		sleep(1500);
		
		VideoTaskSession taskSession2 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 2, false);
		Assert.assertNotNull(taskSession2);
		dbInstance.commitAndCloseSession();
		
		// Load the last session
		VideoTaskSession lastTestSession = taskSessionDao.getLastTaskSession(videoEntry, courseEntry, subIdent, assessedIdentity, null, false);
		Assert.assertNotNull(lastTestSession);
		Assert.assertEquals(taskSession2, lastTestSession);
	}
	
	@Test
	public void getTaskSessions() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-4");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, courseEntry, subIdent, null, videoEntry);
		dbInstance.commit();
		
		VideoTaskSession taskSession1 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 1, false);
		VideoTaskSession taskSession2 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 2, false);

		dbInstance.commitAndCloseSession();

		//Load the sessions
		List<VideoTaskSession> taskSessions = taskSessionDao.getTaskSessions(courseEntry, subIdent, List.of(assessedIdentity), null);
		assertThat(taskSessions)
			.hasSize(2)
			.containsExactlyInAnyOrder(taskSession1, taskSession2);
	}
	
	@Test
	public void getLastAttempt() {
		// Prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-6");
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, videoEntry, "-", null, videoEntry);
		dbInstance.commit();
		
		final String subIdent = "263478236480";
		long noAttempts = taskSessionDao.getLastAttempt(courseEntry, subIdent, assessedIdentity, null);
		Assert.assertEquals(0l, noAttempts);
		
		// Add a task session
		VideoTaskSession taskSession = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 1, true);
		Assert.assertNotNull(taskSession);
		dbInstance.commitAndCloseSession();
		
		long firstAttempt = taskSessionDao.getLastAttempt(courseEntry, subIdent, assessedIdentity, null);
		Assert.assertEquals(1l, firstAttempt);

		// Add a second task session
		VideoTaskSession task2Session = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry, assessedIdentity, null, 2, true);
		Assert.assertNotNull(task2Session);
		dbInstance.commitAndCloseSession();
		
		long lastAttempt = taskSessionDao.getLastAttempt(courseEntry, subIdent, assessedIdentity, null);
		Assert.assertEquals(2l, lastAttempt);
	}
	
	@Test
	public void countTaskSessions() {
		// prepare a test and a user
		RepositoryEntry videoEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry courseEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-7");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("vsession-8");
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity1, null, courseEntry, subIdent, null, videoEntry);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(assessedIdentity2, null, courseEntry, subIdent, null, videoEntry);
		dbInstance.commit();
		
		VideoTaskSession taskSession1_1 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry1, assessedIdentity1, null, 1, false);
		VideoTaskSession taskSession1_2 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry1, assessedIdentity1, null, 2, false);
		VideoTaskSession taskSession2_1 = taskSessionDao.createAndPersistTaskSession(videoEntry, courseEntry, subIdent, assessmentEntry2, assessedIdentity2, null, 2, false);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(taskSession1_1);
		Assert.assertNotNull(taskSession1_2);
		Assert.assertNotNull(taskSession2_1);

		//Load the sessions
		long taskSessions = taskSessionDao.countTaskSessions(courseEntry, subIdent);
		Assert.assertEquals(2, taskSessions);
	}
	

}
