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
package org.olat.modules.grading.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GradedToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingTimeRecordDAO gradingTimesheetDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	
	@Test
	public void getOrCreateConfiguration() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryGradingConfiguration config = gradingService.getOrCreateConfiguration(entry);
		Assert.assertNotNull(config);
		
		RepositoryEntryGradingConfiguration config2 = gradingService.getOrCreateConfiguration(entry);
		Assert.assertNotNull(config2);
		Assert.assertEquals(config, config2);
		
		dbInstance.commit();
	}
	
	@Test
	public void deleteIdentity_like() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("time-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(grader, entry, assessment, new Date(), null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);
		
		GradingTimeRecord timeRecord = gradingTimesheetDao.createRecord(grader, assignment);
		dbInstance.commit();
		Assert.assertNotNull(timeRecord);

		((GradingServiceImpl)gradingService).deleteUserData(id, "del-726378");
		dbInstance.commit();

		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(id);
		Assert.assertTrue(assignments.isEmpty());
		List<GraderToIdentity> gradersRelations = gradedToIdentityDao.getGraderRelations(id);
		Assert.assertTrue(gradersRelations.isEmpty());
	}
	
	@Test
	public void selectGrader_onlyOnce() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		dbInstance.commit();
		Assert.assertNotNull(assessment);
		
		GraderToIdentity proposedGrader = ((GradingServiceImpl)gradingService).selectGrader(entry);
		Assert.assertNotNull(proposedGrader);
		Assert.assertEquals(grader, proposedGrader);
	}
	
	@Test
	public void selectGrader_notAvailable() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-2");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		dbInstance.commit();
		Assert.assertNotNull(assessment);
		
		GraderToIdentity proposedGrader = ((GradingServiceImpl)gradingService).selectGrader(entry);
		Assert.assertNull(proposedGrader);
	}
	
	@Test
	public void selectGrader_several() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		int numOfAssessmentEntries = 6;
		List<AssessmentEntry> assessmentEntries = new ArrayList<>();
		for(int i=0; i<numOfAssessmentEntries; i++) {
			Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3-" + i);
			AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
			assessmentEntries.add(assessment);
		}

		Identity grader1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-4");
		Identity grader2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-5");
		Identity grader3 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-6");
		
		GraderToIdentity graderRelation1 = gradedToIdentityDao.createRelation(entry, grader1);
		GraderToIdentity graderRelation2 = gradedToIdentityDao.createRelation(entry, grader2);
		GraderToIdentity graderRelation3 = gradedToIdentityDao.createRelation(entry, grader3);
		dbInstance.commitAndCloseSession();
		
		//first assignment
		for(int i=0; i<numOfAssessmentEntries; i++) {
			gradingService.assignGrader(entry, assessmentEntries.get(i), true);
		}
		
		List<GradingAssignment> assignmentsGrader1 = gradingAssignmentDao.getGradingAssignments(graderRelation1);
		Assert.assertEquals(2, assignmentsGrader1.size());
		List<GradingAssignment> assignmentsGrader2 = gradingAssignmentDao.getGradingAssignments(graderRelation2);
		Assert.assertEquals(2, assignmentsGrader2.size());
		List<GradingAssignment> assignmentsGrader3 = gradingAssignmentDao.getGradingAssignments(graderRelation3);
		Assert.assertEquals(2, assignmentsGrader3.size());
		
		// check that the assignment are unique
		Set<GradingAssignment> allAssignments = new HashSet<>();
		allAssignments.addAll(assignmentsGrader1);
		allAssignments.addAll(assignmentsGrader2);
		allAssignments.addAll(assignmentsGrader3);
		Assert.assertEquals(6, allAssignments.size());
	}
	
	
	
}
