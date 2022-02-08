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
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.GraderWithStatistics;
import org.olat.modules.grading.model.GradersSearchParameters;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.manager.AbsenceLeaveDAO;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SmtpMessage;

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
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	@Autowired
	private AbsenceLeaveDAO absenceLeaveDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingTimeRecordDAO gradingTimeRecordDao;
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
	public void isGradingEnable() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-config-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		// no configuration -> not enabled
		boolean noGradingConfiguration = gradingService.isGradingEnabled(entry, null);
		Assert.assertFalse(noGradingConfiguration);
		
		// configuration not enabled
		RepositoryEntryGradingConfiguration config = gradingService.getOrCreateConfiguration(entry);
		config.setGradingEnabled(false);
		gradingService.updateConfiguration(config);
		dbInstance.commit();
		boolean notEnabled = gradingService.isGradingEnabled(entry, null);
		Assert.assertFalse(notEnabled);
		
		// configuration is enabled
		RepositoryEntryGradingConfiguration enableConfig = gradingService.getOrCreateConfiguration(entry);
		enableConfig.setGradingEnabled(true);
		gradingService.updateConfiguration(enableConfig);
		dbInstance.commit();
		boolean enabled = gradingService.isGradingEnabled(entry, null);
		Assert.assertTrue(enabled);
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
		
		GradingTimeRecord timeRecord = gradingTimeRecordDao.createRecord(grader, assignment, new Date());
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
			gradingService.assignGrader(entry, assessmentEntries.get(i), new Date(), true);
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
	
	@Test
	public void selectGrader_absences() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-8");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		int numOfAssessmentEntries = 6;
		List<AssessmentEntry> assessmentEntries = new ArrayList<>();
		for(int i=0; i<numOfAssessmentEntries; i++) {
			Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3-" + i);
			AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
			assessmentEntries.add(assessment);
		}

		Identity grader1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-9");
		Identity grader2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-10");
		Identity grader3 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-11");
		
		GraderToIdentity graderRelation1 = gradedToIdentityDao.createRelation(entry, grader1);
		GraderToIdentity graderRelation2 = gradedToIdentityDao.createRelation(entry, grader2);
		GraderToIdentity graderRelation3 = gradedToIdentityDao.createRelation(entry, grader3);
		absenceLeaveDao.createAbsenceLeave(grader3, addDaysToNow(-5), addDaysToNow(5), null, null);
		dbInstance.commitAndCloseSession();
		
		//first assignment
		for(int i=0; i<numOfAssessmentEntries; i++) {
			gradingService.assignGrader(entry, assessmentEntries.get(i), new Date(), true);
		}
		
		List<GradingAssignment> assignmentsGrader1 = gradingAssignmentDao.getGradingAssignments(graderRelation1);
		Assert.assertEquals(3, assignmentsGrader1.size());
		List<GradingAssignment> assignmentsGrader2 = gradingAssignmentDao.getGradingAssignments(graderRelation2);
		Assert.assertEquals(3, assignmentsGrader2.size());
		List<GradingAssignment> assignmentsGrader3 = gradingAssignmentDao.getGradingAssignments(graderRelation3);
		Assert.assertTrue(assignmentsGrader3.isEmpty());
		
		// check that the assignment are unique
		Set<GradingAssignment> allAssignments = new HashSet<>();
		allAssignments.addAll(assignmentsGrader1);
		allAssignments.addAll(assignmentsGrader2);
		allAssignments.addAll(assignmentsGrader3);
		Assert.assertEquals(numOfAssessmentEntries, allAssignments.size());
	}
	
	@Test
	public void selectGrader_absencesSpecific() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-12");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		int numOfAssessmentEntries = 3;
		List<AssessmentEntry> assessmentEntries = new ArrayList<>();
		for(int i=0; i<numOfAssessmentEntries; i++) {
			Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-12-" + i);
			AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
			assessmentEntries.add(assessment);
		}

		Identity grader1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-14");
		Identity grader2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-15");
		
		GraderToIdentity graderRelation1 = gradedToIdentityDao.createRelation(entry, grader1);
		GraderToIdentity graderRelation2 = gradedToIdentityDao.createRelation(entry, grader2);
		absenceLeaveDao.createAbsenceLeave(grader2, addDaysToNow(0), addDaysToNow(5), entry.getOlatResource(), null);
		dbInstance.commitAndCloseSession();
		
		//first assignment
		for(int i=0; i<numOfAssessmentEntries; i++) {
			gradingService.assignGrader(entry, assessmentEntries.get(i), new Date(), true);
		}
		
		List<GradingAssignment> assignmentsGrader1 = gradingAssignmentDao.getGradingAssignments(graderRelation1);
		Assert.assertEquals(3, assignmentsGrader1.size());
		List<GradingAssignment> assignmentsGrader2 = gradingAssignmentDao.getGradingAssignments(graderRelation2);
		Assert.assertTrue(assignmentsGrader2.isEmpty());
		
		// check that the assignment are unique
		Set<GradingAssignment> allAssignments = new HashSet<>();
		allAssignments.addAll(assignmentsGrader1);
		allAssignments.addAll(assignmentsGrader2);
		Assert.assertEquals(numOfAssessmentEntries, allAssignments.size());
	}
	
	@Test
	public void selectGrader_absencesInPast() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-16");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-17");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-18");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		dbInstance.commitAndCloseSession();
		
		absenceLeaveDao.createAbsenceLeave(grader, addDaysToNow(-12), addDaysToNow(-1), entry.getOlatResource(), null);
		dbInstance.commitAndCloseSession();

		gradingService.assignGrader(entry, assessment, null, true);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertEquals(1, assignmentsGrader.size());
	}
	
	@Test
	public void selectGrader_otherResourceAbsences() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-20");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-21");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-22");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		dbInstance.commitAndCloseSession();
		
		absenceLeaveDao.createAbsenceLeave(grader, addDaysToNow(-12), addDaysToNow(12),
				OresHelper.createOLATResourceableInstance("Holydays", 28l), null);
		dbInstance.commitAndCloseSession();

		gradingService.assignGrader(entry, assessment, null, true);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertEquals(1, assignmentsGrader.size());
	}
	
	@Test
	public void selectGrader_otherCourseNodeAbsences() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-23");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-24");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-25");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, "1200012", false, entry);
		dbInstance.commitAndCloseSession();
		
		absenceLeaveDao.createAbsenceLeave(grader, addDaysToNow(-12), addDaysToNow(12),
				entry.getOlatResource(), "1200013");
		dbInstance.commitAndCloseSession();

		gradingService.assignGrader(entry, assessment, new Date(), true);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertEquals(1, assignmentsGrader.size());
	}
	
	@Test
	public void selectGrader_onVacation() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-27");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-28");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-29");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, "1200012", false, entry);
		dbInstance.commitAndCloseSession();
		
		absenceLeaveDao.createAbsenceLeave(grader, addDaysToNow(-12), addDaysToNow(12),
				entry.getOlatResource(), null);
		dbInstance.commitAndCloseSession();

		gradingService.assignGrader(entry, assessment, new Date(), true);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertTrue(assignmentsGrader.isEmpty());
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(entry);		
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(GradingAssignmentStatus.unassigned, assignments.get(0).getAssignmentStatus());
	}
	
	/**
	 * A grader cannot become work if it's in holidays the next day.
	 */
	@Test
	public void selectGrader_onVacationNextDay() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-30");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-31");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-32");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, "1200012", false, entry);
		dbInstance.commitAndCloseSession();
		
		absenceLeaveDao.createAbsenceLeave(grader, addDaysToNow(1), addDaysToNow(12),
				entry.getOlatResource(), null);
		dbInstance.commitAndCloseSession();

		gradingService.assignGrader(entry, assessment, new Date(), true);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertTrue(assignmentsGrader.isEmpty());
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(entry);		
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(GradingAssignmentStatus.unassigned, assignments.get(0).getAssignmentStatus());
	}
	
	@Test
	public void selectGrader_onVacationInFourDays() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-30");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-31");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-32");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, "1200012", false, entry);
		dbInstance.commitAndCloseSession();
		
		absenceLeaveDao.createAbsenceLeave(grader, addDaysToNow(4), addDaysToNow(12),
				entry.getOlatResource(), "1200012");
		dbInstance.commitAndCloseSession();

		gradingService.assignGrader(entry, assessment, new Date(), true);
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertEquals(1, assignmentsGrader.size());
		
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(entry);		
		Assert.assertNotNull(assignments);
		Assert.assertEquals(1, assignments.size());
		Assert.assertEquals(GradingAssignmentStatus.assigned, assignments.get(0).getAssignmentStatus());
	}
	
	@Test
	public void sendReminders() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-100");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		RepositoryEntryGradingConfiguration config = gradingService.getOrCreateConfiguration(entry);
		config.setGradingEnabled(false);
		config.setFirstReminder(1);
		config.setSecondReminder(2);
		gradingService.updateConfiguration(config);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-100-1");
		
		AssessmentEntry assessmentEntry = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);

		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-101");
		grader.getUser().getPreferences().setLanguage("en");
		userManager.updateUserFromIdentity(grader);
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commitAndCloseSession();
		
		//first assignment
		gradingService.assignGrader(entry, assessmentEntry, DateUtils.addDays(new Date(), -5), true);
		
		List<GradingAssignment> assignmentsGrader = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertEquals(1, assignmentsGrader.size());
		
		// set the reminder date
		GradingAssignment assignmentGrader = assignmentsGrader.get(0);
		assignmentGrader.setReminder1Date(DateUtils.addDays(new Date(), -5));
		
		// send reminders
		gradingService.sendReminders();
		
		// check e-mails
		List<SmtpMessage> reminders = getSmtpServer().getReceivedEmails();
		SmtpMessage reminder = reminders.stream()
				.filter(r ->  r.getHeaderValue("To").contains(grader.getUser().getEmail()))
				.findFirst().orElse(null);
		Assert.assertNotNull(reminder);
		String body = reminder.getBody();
		Assert.assertNotNull(body);
		Assert.assertTrue(body.contains("New grading assignment"));

		getSmtpServer().reset();
	}
	
	@Test
	public void getGradersWithStatistics_recordingReassigned() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-28");
		Identity graderId = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-29");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-30");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-31");
		Identity student3 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-32");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment3 = assessmentEntryDao
				.createAssessmentEntry(student3, null, entry, null, Boolean.TRUE, entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, graderId);
		GraderToIdentity authorRelation = gradedToIdentityDao.createRelation(entry, author);
		
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		GradingAssignment assignment3 = gradingAssignmentDao.createGradingAssignment(authorRelation, entry, assessment3, null, null);
		dbInstance.commit();
		
		// record 3 is off
		GradingTimeRecord record1 = gradingTimeRecordDao.createRecord(relation, assignment1, new Date());
		GradingTimeRecord record2 = gradingTimeRecordDao.createRecord(relation, assignment2, new Date());
		GradingTimeRecord record2b = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-2));
		GradingTimeRecord record2c = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-4));
		GradingTimeRecord record3 = gradingTimeRecordDao.createRecord(relation, assignment3, new Date());
		GradingTimeRecord record3b = gradingTimeRecordDao.createRecord(relation, assignment3, addDaysToNow(-2));
		GradingTimeRecord recordAuthor3 = gradingTimeRecordDao.createRecord(authorRelation, assignment3, new Date());
		GradingTimeRecord recordAuthor3b = gradingTimeRecordDao.createRecord(authorRelation, assignment3, addDaysToNow(-2));
		dbInstance.commit();
		
		gradingTimeRecordDao.appendTimeInSeconds(record1, 120l);
		gradingTimeRecordDao.appendTimeInSeconds(record2, 1300l);
		gradingTimeRecordDao.appendTimeInSeconds(record2b, 10l);
		gradingTimeRecordDao.appendTimeInSeconds(record2c, 13l);
		gradingTimeRecordDao.appendTimeInSeconds(record3, 14000l);
		gradingTimeRecordDao.appendTimeInSeconds(record3b, 15000l);
		gradingTimeRecordDao.appendTimeInSeconds(recordAuthor3, 555l);
		gradingTimeRecordDao.appendTimeInSeconds(recordAuthor3b, 55l);
		dbInstance.commitAndCloseSession();
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setReferenceEntry(entry);
		List<GraderWithStatistics> statistics = gradingService.getGradersWithStatistics(searchParams);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(2, statistics.size());
		
		GraderWithStatistics graderStatistics = statistics.stream()
				.filter(stats -> graderId.getKey().equals(stats.getGrader().getKey())).findFirst().orElse(null);
		Assert.assertEquals(30443l, graderStatistics.getRecordedTimeInSeconds());
		Assert.assertEquals(2, graderStatistics.getStatistics().getTotalAssignments());
		Assert.assertEquals(2, graderStatistics.getStatistics().getNumOfOpenAssignments());
		
		GraderWithStatistics authorStatistics = statistics.stream()
				.filter(stats -> author.getKey().equals(stats.getGrader().getKey())).findFirst().orElse(null);
		Assert.assertEquals(610l, authorStatistics.getRecordedTimeInSeconds());
		Assert.assertEquals(1, authorStatistics.getStatistics().getTotalAssignments());
		Assert.assertEquals(1, authorStatistics.getStatistics().getNumOfOpenAssignments());
	}
	
	@Test
	public void getGradersWithStatistics_checkDates() {
		Identity graderId = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-41");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-42");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-43");
		Identity student3 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-44");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(graderId);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment3 = assessmentEntryDao
				.createAssessmentEntry(student3, null, entry, null, Boolean.TRUE, entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, graderId);
		
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		GradingAssignment assignment3 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment3, null, null);
		dbInstance.commit();
		
		// r
		GradingTimeRecord record1 = gradingTimeRecordDao.createRecord(relation, assignment1, new Date());
		GradingTimeRecord record2 = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-1));
		GradingTimeRecord record2b = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-2));
		GradingTimeRecord record2c = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-4));
		GradingTimeRecord record3 = gradingTimeRecordDao.createRecord(relation, assignment3, new Date());
		GradingTimeRecord record3b = gradingTimeRecordDao.createRecord(relation, assignment3, addDaysToNow(-2));
		GradingTimeRecord record3c = gradingTimeRecordDao.createRecord(relation, assignment3, addDaysToNow(2));
		dbInstance.commit();
		
		assignment1.setClosingDate(addDaysToNow(1));
		assignment1.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment1 = gradingAssignmentDao.updateAssignment(assignment1);
		assignment2.setClosingDate(addDaysToNow(-1));
		assignment2.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment2 = gradingAssignmentDao.updateAssignment(assignment2);
		assignment3.setClosingDate(addDaysToNow(4));
		assignment3.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment3 = gradingAssignmentDao.updateAssignment(assignment3);
		
		gradingTimeRecordDao.appendTimeInSeconds(record1, 120l);
		gradingTimeRecordDao.appendTimeInSeconds(record2, 1300l);
		gradingTimeRecordDao.appendTimeInSeconds(record2b, 10l);
		gradingTimeRecordDao.appendTimeInSeconds(record2c, 13l);
		gradingTimeRecordDao.appendTimeInSeconds(record3, 14000l);
		gradingTimeRecordDao.appendTimeInSeconds(record3b, 15000l);
		gradingTimeRecordDao.appendTimeInSeconds(record3c, 55l);
		dbInstance.commitAndCloseSession();
		
		{// get all data
			GradersSearchParameters searchParams = new GradersSearchParameters();
			searchParams.setReferenceEntry(entry);
			List<GraderWithStatistics> statistics = gradingService.getGradersWithStatistics(searchParams);
			Assert.assertNotNull(statistics);
			Assert.assertEquals(1, statistics.size());
			Assert.assertEquals(30498l, statistics.get(0).getRecordedTimeInSeconds());
			Assert.assertEquals(3, statistics.get(0).getStatistics().getTotalAssignments());
			Assert.assertEquals(3, statistics.get(0).getStatistics().getNumOfDoneAssignments());
		}
		
		{// get dates around data
			GradersSearchParameters searchParams = new GradersSearchParameters();
			searchParams.setReferenceEntry(entry);
			searchParams.setGradingFrom(addDaysToNow(-20));
			searchParams.setGradingTo(addDaysToNow(20));
			List<GraderWithStatistics> statistics = gradingService.getGradersWithStatistics(searchParams);
			Assert.assertNotNull(statistics);
			Assert.assertEquals(1, statistics.size());
			Assert.assertEquals(30498l, statistics.get(0).getRecordedTimeInSeconds());
			Assert.assertEquals(3, statistics.get(0).getStatistics().getTotalAssignments());
			Assert.assertEquals(3, statistics.get(0).getStatistics().getNumOfDoneAssignments());
		}
		
		{// get dates between
			GradersSearchParameters searchParams = new GradersSearchParameters();
			searchParams.setReferenceEntry(entry);
			searchParams.setGradingFrom(addDaysToNow(-3));
			searchParams.setGradingTo(addDaysToNow(1));
			List<GraderWithStatistics> statistics = gradingService.getGradersWithStatistics(searchParams);
			Assert.assertEquals(1, statistics.size());
			Assert.assertEquals(30430l, statistics.get(0).getRecordedTimeInSeconds());
			Assert.assertEquals(3, statistics.get(0).getStatistics().getTotalAssignments());
			Assert.assertEquals(3, statistics.get(0).getStatistics().getNumOfDoneAssignments());
		}
		
		{// get dates out
			GradersSearchParameters searchParams = new GradersSearchParameters();
			searchParams.setReferenceEntry(entry);
			searchParams.setGradingFrom(addDaysToNow(13));
			searchParams.setGradingTo(addDaysToNow(18));
			List<GraderWithStatistics> statistics = gradingService.getGradersWithStatistics(searchParams);
			Assert.assertEquals(1, statistics.size());
			Assert.assertEquals(0l, statistics.get(0).getRecordedTimeInSeconds());
			Assert.assertEquals(0, statistics.get(0).getStatistics().getTotalAssignments());
			Assert.assertEquals(0, statistics.get(0).getStatistics().getNumOfDoneAssignments());
		}
	}
	
	@Test
	public void getGradedEntriesWithStatistics_recordingReassigned() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-28");
		Identity graderId = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-29");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-30");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-31");
		Identity student3 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-32");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment3 = assessmentEntryDao
				.createAssessmentEntry(student3, null, entry, null, Boolean.TRUE, entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, graderId);
		GraderToIdentity authorRelation = gradedToIdentityDao.createRelation(entry, author);
		
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		GradingAssignment assignment3 = gradingAssignmentDao.createGradingAssignment(authorRelation, entry, assessment3, null, null);
		dbInstance.commit();
		
		// record 3 is off
		GradingTimeRecord record1 = gradingTimeRecordDao.createRecord(relation, assignment1, new Date());
		GradingTimeRecord record2 = gradingTimeRecordDao.createRecord(relation, assignment2, new Date());
		GradingTimeRecord record2b = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-1));
		GradingTimeRecord record2c = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-2));
		GradingTimeRecord record3 = gradingTimeRecordDao.createRecord(relation, assignment3, new Date());
		GradingTimeRecord recordAuthor3 = gradingTimeRecordDao.createRecord(authorRelation, assignment3, new Date());
		dbInstance.commit();
		
		gradingTimeRecordDao.appendTimeInSeconds(record1, 120l);
		gradingTimeRecordDao.appendTimeInSeconds(record2, 1300l);
		gradingTimeRecordDao.appendTimeInSeconds(record2b, 31l);
		gradingTimeRecordDao.appendTimeInSeconds(record2c, 54l);
		gradingTimeRecordDao.appendTimeInSeconds(record3, 14000l);
		gradingTimeRecordDao.appendTimeInSeconds(recordAuthor3, 555l);
		dbInstance.commitAndCloseSession();
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setReferenceEntry(entry);
		List<ReferenceEntryWithStatistics> statistics = gradingService.getGradedEntriesWithStatistics(graderId);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(1, statistics.size());
		
		ReferenceEntryWithStatistics stats = statistics.get(0);
		Assert.assertEquals(2, stats.getStatistics().getTotalAssignments());
		Assert.assertEquals(2, stats.getStatistics().getNumOfOpenAssignments());
		Assert.assertEquals(15505l, stats.getRecordedTimeInSeconds());
	}
	
	@Test
	public void getGradedEntriesWithStatistics_dateFromTo() {
		Identity graderId = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-36");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-37");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-38");
		Identity student3 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-39");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(graderId);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment3 = assessmentEntryDao
				.createAssessmentEntry(student3, null, entry, null, Boolean.TRUE, entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, graderId);
		
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		GradingAssignment assignment3 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment3, null, null);
		dbInstance.commit();
		
		// record 3 is off
		GradingTimeRecord record1 = gradingTimeRecordDao.createRecord(relation, assignment1, new Date());
		GradingTimeRecord record2 = gradingTimeRecordDao.createRecord(relation, assignment2, new Date());
		GradingTimeRecord record2b = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-1));
		GradingTimeRecord record2c = gradingTimeRecordDao.createRecord(relation, assignment2, addDaysToNow(-2));
		GradingTimeRecord record3 = gradingTimeRecordDao.createRecord(relation, assignment3, new Date());
		GradingTimeRecord record3b = gradingTimeRecordDao.createRecord(relation, assignment3, addDaysToNow(3));
		dbInstance.commit();
		
		assignment1.setClosingDate(addDaysToNow(1));
		assignment1.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment1 = gradingAssignmentDao.updateAssignment(assignment1);
		assignment2.setClosingDate(addDaysToNow(2));
		assignment2.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment2 = gradingAssignmentDao.updateAssignment(assignment2);
		assignment3.setClosingDate(addDaysToNow(4));
		assignment3.setAssignmentStatus(GradingAssignmentStatus.done);
		assignment3 = gradingAssignmentDao.updateAssignment(assignment3);

		gradingTimeRecordDao.appendTimeInSeconds(record1, 120l);
		gradingTimeRecordDao.appendTimeInSeconds(record2, 1300l);
		gradingTimeRecordDao.appendTimeInSeconds(record2b, 31l);
		gradingTimeRecordDao.appendTimeInSeconds(record2c, 54l);
		gradingTimeRecordDao.appendTimeInSeconds(record3, 14000l);
		gradingTimeRecordDao.appendTimeInSeconds(record3b, 555l);
		dbInstance.commitAndCloseSession();

		List<ReferenceEntryWithStatistics> statistics = gradingService.getGradedEntriesWithStatistics(graderId);
		Assert.assertEquals(1, statistics.size());
			
		ReferenceEntryWithStatistics stats = statistics.get(0);
		Assert.assertEquals(3, stats.getStatistics().getTotalAssignments());
		Assert.assertEquals(3, stats.getStatistics().getNumOfDoneAssignments());
		Assert.assertEquals(16060l, stats.getRecordedTimeInSeconds());
	}
	
	@Test
	public void graderAbsencesLeavesReassignment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-40");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		int numOfAssessmentEntries = 6;
		List<AssessmentEntry> assessmentEntries = new ArrayList<>();
		for(int i=0; i<numOfAssessmentEntries; i++) {
			Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3-" + i);
			AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
			assessmentEntries.add(assessment);
		}

		Identity grader1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-41");
		Identity grader2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-42");
		GraderToIdentity graderRelation1 = gradedToIdentityDao.createRelation(entry, grader1);
		GraderToIdentity graderRelation2 = gradedToIdentityDao.createRelation(entry, grader2);
		dbInstance.commit();
		Assert.assertNotNull(graderRelation1);
		Assert.assertNotNull(graderRelation2);
		
		for(AssessmentEntry assessmentEntry:assessmentEntries) {
			gradingService.assignGrader(entry, assessmentEntry, new Date(), true);
		}
		dbInstance.commit();

		absenceLeaveDao.createAbsenceLeave(grader1, addDaysToNow(-5), addDaysToNow(5), null, null);
		dbInstance.commitAndCloseSession();
		
		((GradingServiceImpl)gradingService).graderAbsenceLeavesCheck();
		dbInstance.commitAndCloseSession();
		
		List<GradingAssignment> assignments1 = gradingAssignmentDao.getGradingAssignments(grader1);
		Assert.assertTrue(assignments1.isEmpty());
		
		List<GradingAssignment> assignments2 = gradingAssignmentDao.getGradingAssignments(grader2);
		Assert.assertEquals(6, assignments2.size());
	}
	
	@Test
	public void deactivateGrader() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-40");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		
		int numOfAssessmentEntries = 4;
		List<AssessmentEntry> assessmentEntries = new ArrayList<>();
		for(int i=0; i<numOfAssessmentEntries; i++) {
			Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-3-" + i);
			AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
			assessmentEntries.add(assessment);
		}

		Identity grader1 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-41");
		Identity grader2 = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-42");
		GraderToIdentity graderRelation1 = gradedToIdentityDao.createRelation(entry, grader1);
		dbInstance.commit();
		Assert.assertNotNull(graderRelation1);
		
		for(AssessmentEntry assessmentEntry:assessmentEntries) {
			gradingService.assignGrader(entry, assessmentEntry, new Date(), true);
		}
		dbInstance.commit();

		// deactivate the first grader
		MailerResult result = new MailerResult();
		gradingService.deactivateGrader(grader1, grader2, null, result);
		dbInstance.commitAndCloseSession();
		
		// checked that the assignments was transfered
		List<GradingAssignment> assignments1 = gradingAssignmentDao.getGradingAssignments(grader1);
		Assert.assertTrue(assignments1.isEmpty());
		List<GradingAssignment> assignments2 = gradingAssignmentDao.getGradingAssignments(grader2);
		Assert.assertEquals(4, assignments2.size());
	}
	
	@Test
	public void unassignGrader() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-50");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);

		Identity student = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-51");
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(student, null, entry, null, false, entry);
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("assign-41");
		GraderToIdentity graderRelation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		
		gradingService.assignGrader(entry, assessment, new Date(), true);
		dbInstance.commit();
		
		// check assignments
		List<GradingAssignment> assignments = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertEquals(1, assignments.size());
		GradingAssignment assignment = assignments.get(0);
		Assert.assertEquals(assessment, assignment.getAssessmentEntry());
		
		// unassign
		gradingService.unassignGrader(assignment);
		dbInstance.commitAndCloseSession();
		
		// check
		List<GradingAssignment> unassignments = gradingAssignmentDao.getGradingAssignments(graderRelation);
		Assert.assertTrue(unassignments.isEmpty());
	}
	
	private Date addDaysToNow(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}
}
