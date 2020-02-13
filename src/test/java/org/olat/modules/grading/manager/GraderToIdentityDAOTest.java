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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.model.GraderStatistics;
import org.olat.modules.grading.model.GradersSearchParameters;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * Initial date: 21 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GraderToIdentityDAOTest extends OlatTestCase {
	
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
	public void createGraderRelation() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-1");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-2");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		
		Assert.assertNotNull(relation.getCreationDate());
		Assert.assertNotNull(relation.getLastModified());
		Assert.assertEquals(grader, relation.getIdentity());
		Assert.assertEquals(entry, relation.getEntry());
		Assert.assertEquals(GraderStatus.activated, relation.getGraderStatus());
	}
	
	@Test
	public void getGraders() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		
		List<GraderToIdentity> graders = gradedToIdentityDao.getGraders(entry);
		Assert.assertNotNull(graders);
		Assert.assertEquals(1, graders.size());
		
		GraderToIdentity reloadedGrader = graders.get(0);
		Assert.assertNotNull(reloadedGrader.getCreationDate());
		Assert.assertNotNull(reloadedGrader.getLastModified());
		Assert.assertEquals(grader, reloadedGrader.getIdentity());
		Assert.assertEquals(entry, reloadedGrader.getEntry());
		Assert.assertEquals(GraderStatus.activated, reloadedGrader.getGraderStatus());
	}
	
	@Test
	public void findGraders_all() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		List<GraderToIdentity> graders = gradedToIdentityDao.findGraders(searchParams);
		Assert.assertNotNull(graders);
		Assert.assertTrue(graders.size() >= 1);
		Assert.assertTrue(graders.contains(relation));
	}
	
	@Test
	public void findGraders_withReference() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setReferenceEntry(entry);
		List<GraderToIdentity> graders = gradedToIdentityDao.findGraders(searchParams);
		Assert.assertNotNull(graders);
		Assert.assertEquals(1, graders.size());
		Assert.assertEquals(grader, graders.get(0).getIdentity());
		Assert.assertEquals(entry, graders.get(0).getEntry());
	}
	
	@Test
	public void getGrader() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-5");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		
		GraderToIdentity reloadedRelation = gradedToIdentityDao.getGrader(entry, grader);
		Assert.assertNotNull(reloadedRelation);
		Assert.assertEquals(relation, reloadedRelation);
	}
	
	@Test
	public void getGraderRelations() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-25");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		
		List<GraderToIdentity> reloadedRelations = gradedToIdentityDao.getGraderRelations(grader);
		Assert.assertNotNull(reloadedRelations);
		Assert.assertEquals(1, reloadedRelations.size());
		Assert.assertEquals(relation, reloadedRelations.get(0));
	}
	
	@Test
	public void isGraderOf() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-6");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-7");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		
		Assert.assertNotNull(relation);
		
		boolean isGrader = gradedToIdentityDao.isGraderOf(entry, grader);
		Assert.assertTrue(isGrader);
	}
	
	@Test
	public void isGraderOf_notGrader() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-14");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();

		boolean isGrader = gradedToIdentityDao.isGraderOf(entry, author);
		Assert.assertFalse(isGrader);
	}
	
	@Test
	public void isGrader() {
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-10");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(grader);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		
		boolean isGrader = gradedToIdentityDao.isGrader(grader);
		Assert.assertTrue(isGrader);
	}
	
	@Test
	public void isGrader_notGrader() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("not-grader-11");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(identity);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(entry);

		boolean isGrader = gradedToIdentityDao.isGrader(identity);
		Assert.assertFalse(isGrader);
	}
	
	@Test
	public void isGraderManager() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-8");
		Identity notOwner = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-9");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(owner);
		OLATResource resource = entry.getOlatResource();
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		config.setGradingEnabled(true);
		gradingConfigurationDao.updateConfiguration(config);
		dbInstance.commitAndCloseSession();

		boolean manager = gradedToIdentityDao.isGradingManager(owner, resource.getResourceableTypeName());
		Assert.assertTrue(manager);
		boolean notManager = gradedToIdentityDao.isGradingManager(notOwner, resource.getResourceableTypeName());
		Assert.assertFalse(notManager);
	}
	
	@Test
	public void isGraderManager_configDisabled() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-10");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(owner);
		OLATResource resource = entry.getOlatResource();
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		config.setGradingEnabled(false);
		gradingConfigurationDao.updateConfiguration(config);
		dbInstance.commitAndCloseSession();

		boolean notManager = gradedToIdentityDao.isGradingManager(owner, resource.getResourceableTypeName());
		Assert.assertFalse(notManager);
	}
	
	@Test
	public void getReferenceRepository() {
		Identity owner = JunitTestHelper.createAndPersistIdentityAsRndUser("grading-15");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(owner);
		RepositoryEntryGradingConfiguration config = gradingConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		config.setGradingEnabled(true);
		gradingConfigurationDao.updateConfiguration(config);
		dbInstance.commitAndCloseSession();

		String resourceTypeName = entry.getOlatResource().getResourceableTypeName();
		List<RepositoryEntry> referenceEntries = gradedToIdentityDao.getReferenceRepositoryEntries(owner, resourceTypeName);
		Assert.assertNotNull(referenceEntries);
		Assert.assertEquals(1, referenceEntries.size());
		Assert.assertEquals(entry, referenceEntries.get(0));
	}
	
	@Test
	public void getReferenceRepositoryEntriesAsGrader() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-36");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-37");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		GraderToIdentity relation1 = gradedToIdentityDao.createRelation(entry1, grader);
		GraderToIdentity relation2 = gradedToIdentityDao.createRelation(entry2, grader);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntry> graderEntries = gradedToIdentityDao.getReferenceRepositoryEntriesAsGrader(grader);
		Assert.assertNotNull(graderEntries);
		Assert.assertEquals(2, graderEntries.size());
		Assert.assertTrue(graderEntries.contains(entry1));
		Assert.assertTrue(graderEntries.contains(entry2));
		Assert.assertNotNull(relation1);
		Assert.assertNotNull(relation2);
		
		List<RepositoryEntry> authorEntries = gradedToIdentityDao.getReferenceRepositoryEntriesAsGrader(author);
		Assert.assertTrue(authorEntries.isEmpty());
	}
	
	@Test
	public void getGraders_ofManager() {
		Identity manager = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-16");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-17");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(manager);
		dbInstance.commitAndCloseSession();
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		dbInstance.commit();
		Assert.assertNotNull(relation);
		
		List<Identity> graders = gradedToIdentityDao.getGraders(manager);
		Assert.assertNotNull(graders);
		Assert.assertEquals(1, graders.size());
		Assert.assertEquals(grader, graders.get(0));
	}
	
	@Test
	public void getGradedEntriesWithStatistics() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-16");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-17");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-18");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-19");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, Boolean.TRUE, entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		dbInstance.commit();
		dbInstance.commit();
		Assert.assertNotNull(assignment1);
		Assert.assertNotNull(assignment2);
		
		List<ReferenceEntryWithStatistics> statistics = gradedToIdentityDao.getReferenceEntriesStatistics(grader);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(1, statistics.size());
		
		ReferenceEntryWithStatistics stats = statistics.get(0);
		Assert.assertEquals(entry, stats.getEntry());
		Assert.assertEquals(2, stats.getTotalAssignments());
		Assert.assertEquals(2, stats.getNumOfOpenAssignments());
		Assert.assertEquals(0, stats.getNumOfDoneAssignments());
		Assert.assertEquals(0, stats.getNumOfOverdueAssignments());
	}
	
	@Test
	public void getGradersStatistics_manager() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-20");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-21");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-22");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-23");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry, null, Boolean.TRUE, entry);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry, null, Boolean.TRUE, entry);
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		dbInstance.commit();
		dbInstance.commit();
		Assert.assertNotNull(assignment1);
		Assert.assertNotNull(assignment2);
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setManager(author);
		
		List<GraderStatistics> statistics = gradedToIdentityDao.getGradersStatistics(searchParams);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(1, statistics.size());
		
		GraderStatistics stats = statistics.get(0);
		Assert.assertEquals(grader.getKey(), stats.getKey());
		Assert.assertEquals(2, stats.getTotalAssignments());
		Assert.assertEquals(2, stats.getNumOfOpenAssignments());
		Assert.assertEquals(0, stats.getNumOfDoneAssignments());
		Assert.assertEquals(0, stats.getNumOfOverdueAssignments());
	}
	
	@Test
	public void getGradersStatistics_grader() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-24");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-25");
		Identity student1 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-26");
		Identity student2 = JunitTestHelper.createAndPersistIdentityAsRndUser("graded-27");
		RepositoryEntry entry1 = JunitTestHelper.createRandomRepositoryEntry(author);
		RepositoryEntry entry2 = JunitTestHelper.createRandomRepositoryEntry(author);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry assessment1 = assessmentEntryDao
				.createAssessmentEntry(student1, null, entry1, null, Boolean.TRUE, entry1);
		AssessmentEntry assessment2 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry1, null, Boolean.TRUE, entry1);
		AssessmentEntry assessment3 = assessmentEntryDao
				.createAssessmentEntry(student2, null, entry2, null, Boolean.TRUE, entry2);
		
		GraderToIdentity relation1 = gradedToIdentityDao.createRelation(entry1, grader);
		GraderToIdentity relation2 = gradedToIdentityDao.createRelation(entry2, grader);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation1, entry1, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation1, entry1, assessment2, null, null);
		GradingAssignment assignment3 = gradingAssignmentDao.createGradingAssignment(relation2, entry2, assessment3, null, null);
		dbInstance.commit();
		dbInstance.commit();
		Assert.assertNotNull(assignment1);
		Assert.assertNotNull(assignment2);
		Assert.assertNotNull(assignment3);
		
		assignment3.setAssignmentStatus(GradingAssignmentStatus.done);
		gradingAssignmentDao.updateAssignment(assignment3);
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setGrader(grader);
		searchParams.setStatus(Arrays.asList(GraderStatus.values()));
		searchParams.setGradingTo(CalendarUtils.startOfDay(new Date()));
		searchParams.setGradingTo(CalendarUtils.addWorkingDays(new Date(), 10));
		
		List<GraderStatistics> statistics = gradedToIdentityDao.getGradersStatistics(searchParams);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(1, statistics.size());
		
		GraderStatistics stats = statistics.get(0);
		Assert.assertEquals(grader.getKey(), stats.getKey());
		Assert.assertEquals(3, stats.getTotalAssignments());
		Assert.assertEquals(2, stats.getNumOfOpenAssignments());
		Assert.assertEquals(1, stats.getNumOfDoneAssignments());
		Assert.assertEquals(0, stats.getNumOfOverdueAssignments());
	}
	
	@Test
	public void getGradersStatistics_reference() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-28");
		Identity grader = JunitTestHelper.createAndPersistIdentityAsRndUser("grader-29");
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
		
		GraderToIdentity relation = gradedToIdentityDao.createRelation(entry, grader);
		GradingAssignment assignment1 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment1, null, null);
		GradingAssignment assignment2 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment2, null, null);
		GradingAssignment assignment3 = gradingAssignmentDao.createGradingAssignment(relation, entry, assessment3, null, null);
		dbInstance.commit();
		
		assignment1.setAssignmentStatus(GradingAssignmentStatus.inProcess);
		gradingAssignmentDao.updateAssignment(assignment1);
		assignment2.setAssignmentStatus(GradingAssignmentStatus.inProcess);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		assignment2.setDeadline(cal.getTime());
		gradingAssignmentDao.updateAssignment(assignment2);
		assignment3.setAssignmentStatus(GradingAssignmentStatus.done);
		gradingAssignmentDao.updateAssignment(assignment3);
		dbInstance.commit();
		
		GradersSearchParameters searchParams = new GradersSearchParameters();
		searchParams.setReferenceEntry(entry);
		
		List<GraderStatistics> statistics = gradedToIdentityDao.getGradersStatistics(searchParams);
		Assert.assertNotNull(statistics);
		Assert.assertEquals(1, statistics.size());
		
		GraderStatistics stats = statistics.get(0);
		Assert.assertEquals(grader.getKey(), stats.getKey());
		Assert.assertEquals(3, stats.getTotalAssignments());
		Assert.assertEquals(2, stats.getNumOfOpenAssignments());
		Assert.assertEquals(1, stats.getNumOfDoneAssignments());
		Assert.assertEquals(1, stats.getNumOfOverdueAssignments());
	}
}
