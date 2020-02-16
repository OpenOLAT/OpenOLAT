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

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.grading.GraderToIdentity;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingTimeRecord;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingTimeRecordDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private GraderToIdentityDAO gradedToIdentityDao;
	@Autowired
	private GradingTimeRecordDAO gradingTimesheetDao;
	@Autowired
	private GradingAssignmentDAO gradingAssignmentDao;
	
	@Test
	public void createTime() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("time-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(grader, entry, assessment, new Date(), null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);

		GradingTimeRecord timerecord = gradingTimesheetDao.createRecord(grader, assignment, new Date());
		dbInstance.commit();
		Assert.assertNotNull(timerecord);
		Assert.assertNotNull(timerecord.getCreationDate());
		Assert.assertNotNull(timerecord.getLastModified());
		Assert.assertEquals(grader, timerecord.getGrader());
		Assert.assertEquals(assignment, timerecord.getAssignment());
		Assert.assertEquals(0l, timerecord.getTime());
	}
	
	@Test
	public void loadTimeByKey() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("time-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(grader, entry, assessment, new Date(), null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);

		GradingTimeRecord record = gradingTimesheetDao.createRecord(grader, assignment, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(record);
		
		GradingTimeRecord reloadedRecord = gradingTimesheetDao.loadByKey(record.getKey());
		Assert.assertNotNull(reloadedRecord);
		Assert.assertEquals(record, reloadedRecord);
		Assert.assertNotNull(reloadedRecord.getCreationDate());
		Assert.assertNotNull(reloadedRecord.getLastModified());
		Assert.assertEquals(grader, reloadedRecord.getGrader());
		Assert.assertEquals(assignment, reloadedRecord.getAssignment());
		Assert.assertEquals(0l, reloadedRecord.getTime());
	}
	
	@Test
	public void loadTimeByAssignment() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("time-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(grader, entry, assessment, new Date(), null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);

		GradingTimeRecord record = gradingTimesheetDao.createRecord(grader, assignment, new Date());
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(record);
		
		GradingTimeRecord reloadedRecord = gradingTimesheetDao.loadRecord(grader, assignment, new Date());
		Assert.assertNotNull(reloadedRecord);
		Assert.assertEquals(record, reloadedRecord);
		Assert.assertEquals(grader, reloadedRecord.getGrader());
		Assert.assertEquals(assignment, reloadedRecord.getAssignment());
	}
	
	@Test
	public void appendTime() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("time-1");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(grader, entry, assessment, new Date(), null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);

		// create record
		GradingTimeRecord record = gradingTimesheetDao.createRecord(grader, assignment, new Date());
		dbInstance.commit();
		Assert.assertNotNull(record);
		Assert.assertEquals(0l, record.getTime());
		
		// append time
		gradingTimesheetDao.appendTimeInSeconds(grader, assignment, 2l, new Date());
		dbInstance.commitAndCloseSession();
		gradingTimesheetDao.appendTimeInSeconds(grader, assignment, 9l, new Date());
		dbInstance.commit();
		
		// retrieve the record
		GradingTimeRecord reloadedRecord = gradingTimesheetDao.loadByKey(record.getKey());
		Assert.assertEquals(11l, reloadedRecord.getTime());
	}
	
	@Test
	public void appendTime_byKey() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("time-3");
		RepositoryEntry entry = JunitTestHelper.createRandomRepositoryEntry(id);
		AssessmentEntry assessment = assessmentEntryDao.createAssessmentEntry(id, null, entry, null, false, entry);
		GraderToIdentity grader = gradedToIdentityDao.createRelation(entry, id);
		GradingAssignment assignment = gradingAssignmentDao.createGradingAssignment(grader, entry, assessment, new Date(), null);
		dbInstance.commit();
		Assert.assertNotNull(assignment);

		// create record
		GradingTimeRecord record = gradingTimesheetDao.createRecord(grader, assignment, new Date());
		dbInstance.commit();
		Assert.assertNotNull(record);
		Assert.assertEquals(0l, record.getTime());
		
		// append time
		gradingTimesheetDao.appendTimeInSeconds(record, 6l);
		dbInstance.commitAndCloseSession();
		gradingTimesheetDao.appendTimeInSeconds(record, 96l);
		dbInstance.commit();
		
		// retrieve the record
		GradingTimeRecord reloadedRecord = gradingTimesheetDao.loadByKey(record.getKey());
		Assert.assertEquals(102l, reloadedRecord.getTime());
	}
}
