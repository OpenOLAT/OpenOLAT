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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters;
import org.olat.modules.grading.model.GradingAssignmentWithInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAssignmentDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	@Autowired
	private GradingConfigurationDAO gradingConfigurationDao;
	
	@Test
	public void createAssignment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		Assert.assertNotNull(assessment);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		Assert.assertNotNull(assignment);
	}
	
	@Test
	public void createAssignment_noGrader() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-4");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-5");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		Assert.assertNotNull(assessment);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment, null, null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);
	}
	
	@Test
	public void loadByKey() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		Assert.assertNotNull(assessment);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignment reloadedAssignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		dbInstance.commitAndCloseSession();
		
		// grader and identity of grader are fetched
		Assert.assertNotNull(reloadedAssignment);
		Assert.assertEquals(assignment, reloadedAssignment);
		Assert.assertEquals(relation, reloadedAssignment.getGrader());
		Assert.assertEquals(grader, reloadedAssignment.getGrader().getIdentity());
		Assert.assertNotNull(reloadedAssignment.getGrader().getIdentity().getUser());
	}
	
	@Test
	public void loadFullByKey() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		Assert.assertNotNull(assessment);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, new Date(), new Date());
		dbInstance.commit();
		
		GradingAssignment reloadedAssignment = gradingAssignmentDao.loadFullByKey(assignment.getKey());
		dbInstance.commitAndCloseSession();
		
		// grader and identity of grader are fetched
		Assert.assertNotNull(reloadedAssignment);
		Assert.assertEquals(assignment, reloadedAssignment);
		Assert.assertEquals(relation, reloadedAssignment.getGrader());
		Assert.assertEquals(grader, reloadedAssignment.getGrader().getIdentity());
		Assert.assertNotNull(reloadedAssignment.getGrader().getIdentity().getUser());
		// reference entry is fetch
		Assert.assertNotNull(reloadedAssignment.getReferenceEntry());
		Assert.assertNotNull(reloadedAssignment.getReferenceEntry().getDisplayname());
		// assessment entry repository entry is fetch
		Assert.assertNotNull(reloadedAssignment.getAssessmentEntry());
		Assert.assertNotNull(reloadedAssignment.getAssessmentEntry().getRepositoryEntry());
		Assert.assertNotNull(reloadedAssignment.getAssessmentEntry().getRepositoryEntry().getDisplayname());
	}
	
	@Test
	public void getEntries_repositoryEntries() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-2");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry referenceEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(referenceEntry, grader);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, referenceEntry);
		Assert.assertNotNull(assessment);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, referenceEntry, assessment, new Date(), new Date());
		dbInstance.commit();
		Assert.assertNotNull(assignment);
		
		List<RepositoryEntry> entries = gradingAssignmentDao.getEntries(referenceEntry);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(1, entries.size());
		Assert.assertEquals(entry, entries.get(0));
	}
	
	@Test
	public void getEntries_identity() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author10");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-11");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-12");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry referenceEntry = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation = gradedToIdentityDao.createRelation(referenceEntry, grader);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student, null, entry1, null, false, referenceEntry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student, null, entry2, null, false, referenceEntry);
		Assert.assertNotNull(assessment1);
		Assert.assertNotNull(assessment2);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, referenceEntry, assessment1, new Date(), new Date());
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, referenceEntry, assessment2, new Date(), new Date());
		dbInstance.commit();
		Assert.assertNotNull(assignment1);
		Assert.assertNotNull(assignment2);
		
		List<RepositoryEntry> entries = gradingAssignmentDao.getEntries(author);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(entries);
		Assert.assertEquals(2, entries.size());
		Assert.assertTrue(entries.contains(entry1));
		Assert.assertTrue(entries.contains(entry2));
	}
	
	@Test
	public void getGradingAssignments_referenceEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-6");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-7");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment, null, new Date());
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(entry);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertTrue(assignments.contains(assignment));
		
		GradingAssignment loadedAssignment = assignments.get(0);
		Assert.assertEquals(assignment, loadedAssignment);
		Assert.assertEquals(entry, loadedAssignment.getReferenceEntry());
	}
	
	@Test
	public void getGradingAssignments_gradersIdentity() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-26");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-27");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-28");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);

		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, null, new Date());
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(grader);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertTrue(assignments.contains(assignment));
	}
	
	@Test
	public void getGradingAssignments_graderToIdentity() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-60");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-61");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-62");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);

		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, null, new Date());
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(relation);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertTrue(assignments.contains(assignment));
	}
	
	@Test
	public void getGradingAssignments_graderToIdentityAndStatus() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-60");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-61");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-62");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);

		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment, null, new Date());
		dbInstance.commitAndCloseSession();
		
		// assigned and in progress
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(relation,
				GradingAssignmentStatus.assigned, GradingAssignmentStatus.inProcess);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertTrue(assignments.contains(assignment));
		
		// unassigned -> empty
		List<GradingAssignment> unAssignments = gradingAssignmentDao.getGradingAssignments(relation,
				GradingAssignmentStatus.unassigned);
		Assert.assertNotNull(unAssignments);
		Assert.assertTrue(unAssignments.isEmpty());
	}
	
	@Test
	public void findGradingAssignments_all() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-10");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-11");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment, null, null);
		dbInstance.commitAndCloseSession();
		
		GradingAssignmentSearchParameters searchParams = new GradingAssignmentSearchParameters();
		List<GradingAssignmentWithInfos> assignments = gradingAssignmentDao.findGradingAssignments(searchParams);
		Assert.assertNotNull(assignments);
		assertThat(assignments)
			.extracting(GradingAssignmentWithInfos::getAssignment)
			.contains(assignment);
	}
	
	@Test
	public void findGradingAssignments_withReferenceEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-12");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-13");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment, new Date(), null);
		dbInstance.commitAndCloseSession();
		
		GradingAssignmentSearchParameters searchParams = new GradingAssignmentSearchParameters();
		searchParams.setReferenceEntry(entry);
		List<GradingAssignmentWithInfos> assignments = gradingAssignmentDao.findGradingAssignments(searchParams);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(assignment, assignments.get(0).getAssignment());
		Assert.assertNull(assignments.get(0).getGrader());
		Assert.assertEquals(entry, assignments.get(0).getReferenceEntry());
	}
	
	@Test
	public void hasGradingAssignment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-8");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-9");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-9");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment1, null, null);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, false, entry);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignment);
		
		boolean hasAssignment = gradingAssignmentDao.hasGradingAssignment(entry, assessment1);
		Assert.assertTrue(hasAssignment);
		boolean hasNotAssignment = gradingAssignmentDao.hasGradingAssignment(entry, assessment2);
		Assert.assertFalse(hasNotAssignment);
	}
	
	@Test
	public void hasGradingAssignment_entryOnly() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-40");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-41");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry1, null, false, entry1);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry1, assessment, null, null);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignment);
		
		boolean hasAssignment = gradingAssignmentDao.hasGradingAssignment(entry1);
		Assert.assertTrue(hasAssignment);
		boolean hasNotAssignment = gradingAssignmentDao.hasGradingAssignment(entry2);
		Assert.assertFalse(hasNotAssignment);
	}
	
	@Test
	public void getGradingAssignment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-8");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-9");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment, new Date(), new Date());

		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(assignment);
		
		GradingAssignment reloadedAssignment = gradingAssignmentDao.getGradingAssignment(entry, assessment);
		Assert.assertNotNull(reloadedAssignment);
		Assert.assertNotNull(reloadedAssignment.getCreationDate());
		Assert.assertNotNull(reloadedAssignment.getLastModified());
		Assert.assertNotNull(reloadedAssignment.getAssessmentDate());
	}
	
	@Test
	public void getGradingAssignmentsToRemind() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-10");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-11");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, author);
		AssessmentEntry assessment = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment,  null, null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);
		
		config.setGradingEnabled(true);
		config.setFirstReminder(3);
		config.setFirstReminderSubject("Hello");
		config.setFirstReminderBody("Content");
		config.setSecondReminder(5);
		config.setSecondReminderSubject("Hello");
		config.setSecondReminderBody("Content");
		gradingConfigurationDao.updateConfiguration(config);

		assignment.setAssignmentDate(removeDays(5));
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsToRemind = gradingAssignmentDao.getGradingAssignmentsOpenWithPotentialToRemind();
		Assert.assertNotNull(assignmentsToRemind);
		Assert.assertTrue(assignmentsToRemind.contains(assignment));
	}
	
	@Test
	public void getGradersIdentityWithNewAssignments() {
		Identity grader1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-20");
		Identity grader2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-21");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-22");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader1);
	
		GraderToIdentity relation1 = gradedToIdentityDao.createRelation(entry, grader1);
		GraderToIdentity relation2 = gradedToIdentityDao.createRelation(entry, grader2);
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation1, entry, assessment1,  null, null);
		
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation2, entry, assessment2,  null, null);
		dbInstance.commit();
		Assert.assertNotNull(assignment1);
		
		assignment2.setAssignmentNotificationDate(new Date());
		assignment2 = gradingAssignmentDao.updateAssignment(assignment2);
		dbInstance.commitAndCloseSession();
		
		List<Identity> graders = gradingAssignmentDao.getGradersIdentityToNotify();
		Assert.assertNotNull(graders);
		Assert.assertFalse(graders.isEmpty());
		Assert.assertTrue(graders.contains(grader1));
		Assert.assertFalse(graders.contains(grader2));
	}
	
	@Test
	public void getAssignmentsForGradersNotify() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-grader-24");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-25");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-26");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
	
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, false, entry);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1,  null, null);
		
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, false, entry);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2,  null, null);
		dbInstance.commit();
		Assert.assertNotNull(assignment1);
		
		assignment2.setAssignmentNotificationDate(new Date());
		assignment2 = gradingAssignmentDao.updateAssignment(assignment2);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getAssignmentsForGradersNotify(grader);
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertTrue(assignments.contains(assignment1));
		Assert.assertFalse(assignments.contains(assignment2));
	}

	@Test
	public void removeDeadline() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-10");
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-11");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student, null, entry, null, false, entry);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(null, entry, assessment1,  null, null);
		dbInstance.commit();
		
		assignment.setDeadline(new Date());
		assignment = gradingAssignmentDao.updateAssignment(assignment);
		Assert.assertNotNull(assignment);
		dbInstance.commitAndCloseSession();
		
		GradingAssignment reloadedAssignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		Assert.assertNotNull(reloadedAssignment.getDeadline());
		
		gradingAssignmentDao.removeDeadline(entry);
		dbInstance.commitAndCloseSession();
		
		reloadedAssignment = gradingAssignmentDao.loadByKey(assignment.getKey());
		Assert.assertNull(reloadedAssignment.getDeadline());
	}
	
	@Test
	public void removeDeadline_negativeTest() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-author-10");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-11");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assignment-student-11");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry1, null, false, entry1);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry1, null, false, entry1);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(null, entry1, assessment1,  null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(null, entry2, assessment2,  null, null);
		dbInstance.commit();
		
		assignment1.setDeadline(new Date());
		assignment1 = gradingAssignmentDao.updateAssignment(assignment1);
		assignment2.setDeadline(new Date());
		assignment2 = gradingAssignmentDao.updateAssignment(assignment2);
		dbInstance.commit();
		
		GradingAssignment reloadedAssignment1 = gradingAssignmentDao.loadByKey(assignment1.getKey());
		Assert.assertNotNull(reloadedAssignment1.getDeadline());
		GradingAssignment reloadedAssignment2 = gradingAssignmentDao.loadByKey(assignment2.getKey());
		Assert.assertNotNull(reloadedAssignment2.getDeadline());
		
		gradingAssignmentDao.removeDeadline(entry1);
		dbInstance.commitAndCloseSession();
		
		reloadedAssignment1 = gradingAssignmentDao.loadByKey(assignment1.getKey());
		Assert.assertNull(reloadedAssignment1.getDeadline());
		reloadedAssignment2 = gradingAssignmentDao.loadByKey(assignment2.getKey());
		Assert.assertNotNull(reloadedAssignment2.getDeadline());
	}
	
	private Date removeDays(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		return cal.getTime();
	}
	
}
