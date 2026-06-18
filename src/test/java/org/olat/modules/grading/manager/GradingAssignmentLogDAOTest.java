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
package org.olat.modules.grading.manager;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentLog;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.model.GradingAssignmentLogSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GradingAssignmentLogDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	@Autowired
	private GradingAssignmentLogDAO gradingAssignmentLogDao;
	
	@Test
	public void createAssignmentLog() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-2");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-3");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(graderRelation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignmentLog assignmentLog = gradingAssignmentLogDao.createLog(assignment, grader, student, 10l, 10l, entry, entry);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(assignmentLog);
		Assert.assertEquals(assignmentLog.getKey(), assignmentLog.getGradingAssignmentKey());
		Assert.assertEquals(student, assignmentLog.getAssignee());
		Assert.assertEquals(grader, assignmentLog.getGrader());
		Assert.assertEquals(entry.getDisplayname(), assignmentLog.getRepositoryEntryDisplayName());
		Assert.assertEquals(entry.getDisplayname(), assignmentLog.getReferenceEntryDisplayName());
		Assert.assertEquals(GradingAssignmentStatus.assigned, assignmentLog.getStatus());
	}
	
	@Test
	public void markAsDeleted() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-4");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-5");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-6");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(graderRelation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		GradingAssignmentLog assignmentLog = gradingAssignmentLogDao.createLog(assignment, grader, student, 10l, 10l, entry, entry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignmentLog);
		
		int deleted = gradingAssignmentLogDao.markAsDeleted(assignment);
		Assert.assertEquals(1, deleted);
	}
	
	@Test
	public void getGradingAssignmentsLogs() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-7");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-8");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-9");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(graderRelation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignmentLog assignmentLog = gradingAssignmentLogDao.createLog(assignment, grader, student, 30l, 30l, entry, entry);
		dbInstance.commitAndCloseSession();
		
		// Retrieve the log
		GradingAssignmentLogSearchParameters searchParams = new GradingAssignmentLogSearchParameters();
		searchParams.setEntry(entry);
		searchParams.setReferenceEntry(entry);
		searchParams.setGrader(grader);
		
		List<GradingAssignmentLog> logs = gradingAssignmentLogDao.getGradingAssignmentsLogs(searchParams);
		Assertions.assertThat(logs)
			.hasSize(1)
			.containsExactly(assignmentLog);
	}
	
	@Test
	public void loadLastGradingAssignment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-10");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-11");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-12");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(graderRelation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignmentLog assignmentLog = gradingAssignmentLogDao.createLog(assignment, grader, student, 30l, 30l, entry, entry);
		dbInstance.commitAndCloseSession();
		
		// Retrieve the log
		GradingAssignmentLog lastLog = gradingAssignmentLogDao.loadLastGradingAssignment(assignment);
		Assert.assertEquals(assignment.getKey(), lastLog.getGradingAssignmentKey());
		Assert.assertEquals(assignmentLog, lastLog);
	}
	
	@Test
	public void hasGradingAssignmentLog() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-14");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-15");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-log-16");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(graderRelation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignmentLog assignmentLog = gradingAssignmentLogDao.createLog(assignment, grader, student, 30l, 30l, entry, entry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignmentLog);
		
		// Has grader an assignment
		boolean isGrader = gradingAssignmentLogDao.hasGradingAssignmentLog(grader);
		Assert.assertTrue(isGrader);
		
		// Has grader an assignment
		boolean isNotGrader = gradingAssignmentLogDao.hasGradingAssignmentLog(author);
		Assert.assertFalse(isNotGrader);
	}
}
