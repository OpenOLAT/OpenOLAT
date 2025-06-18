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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureParticipantSummary;
import org.olat.modules.lecture.model.LectureParticipantSummaryImpl;
import org.olat.modules.lecture.model.ParticipantAndLectureSummary;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LectureParticipantSummaryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private LectureParticipantSummaryDAO lectureParticipantSummaryDao;
	
	
	@Test
	public void createAndPersistSummary() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("summary-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		lectureParticipantSummaryDao.createSummary(entry, id, null);//null must be accepted
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createAndGetSummary() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("summary-2");
		lectureParticipantSummaryDao.createSummary(entry, id, new Date());//null must be accepted
		dbInstance.commitAndCloseSession();

		LectureParticipantSummary summary = lectureParticipantSummaryDao.getSummary(entry, id);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(summary);
		Assert.assertNotNull(summary.getKey());
		Assert.assertNotNull(summary.getCreationDate());
		Assert.assertNotNull(summary.getLastModified());
		Assert.assertNotNull(summary.getFirstAdmissionDate());
		
		LectureParticipantSummaryImpl summaryImpl = (LectureParticipantSummaryImpl)summary;
		Assert.assertEquals(entry, summaryImpl.getEntry());
		Assert.assertEquals(id, summaryImpl.getIdentity());
	}
	
	@Test
	public void getEnrollmentDateByRepositoryEntry() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-summary-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock block = createMinimalLectureBlock(entry);

		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();

		Date enrollmentDate = lectureParticipantSummaryDao.getEnrollmentDate(entry, participant);
		Assert.assertNotNull(enrollmentDate);
		Assert.assertNotNull(block);
	}
	
	@Test
	public void getEnrollmentDateByLectureBlock() {
		Identity participant = JunitTestHelper.createAndPersistIdentityAsRndUser("participant-summary-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock lectureBlock = createMinimalLectureBlock(entry);
		dbInstance.commit();

		repositoryEntryRelationDao.addRole(participant, entry, GroupRoles.participant.name());
		Group entryGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		lectureBlockDao.addGroupToLectureBlock(lectureBlock, entryGroup);
		
		dbInstance.commitAndCloseSession();

		Date enrollmentDate = lectureParticipantSummaryDao.getEnrollmentDate(lectureBlock, participant);
		Assert.assertNotNull(enrollmentDate);
		Assert.assertNotNull(lectureBlock);
	}
	
	@Test
	public void getLectureParticipantSummaries_lectureBlock() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		LectureBlock block = createMinimalLectureBlock(entry);
		
		dbInstance.commitAndCloseSession();

		List<ParticipantAndLectureSummary> summaries = lectureParticipantSummaryDao.getLectureParticipantSummaries(block);
		Assert.assertNotNull(summaries);
	}
	
	@Test
	public void updateSummary() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("summary-3");
		lectureParticipantSummaryDao.createSummary(entry, id, new Date());//null must be accepted
		dbInstance.commitAndCloseSession();

		int numOfUpdatedRows = lectureParticipantSummaryDao.updateCalendarSynchronization(entry, id);
		Assert.assertEquals(1, numOfUpdatedRows);
		dbInstance.commitAndCloseSession();
		
		LectureParticipantSummaryImpl summary = (LectureParticipantSummaryImpl)lectureParticipantSummaryDao.getSummary(entry, id);
		Assert.assertNotNull(summary);
		Assert.assertNotNull(summary.getKey());
		Assert.assertTrue(summary.isCalendarSync());
		Assert.assertNotNull(summary.getCalendarLastSyncDate());
	}
	
	private LectureBlock createMinimalLectureBlock(RepositoryEntry entry) {
		LectureBlock lectureBlock = lectureBlockDao.createLectureBlock(entry, null);
		lectureBlock.setStartDate(new Date());
		lectureBlock.setEndDate(new Date());
		lectureBlock.setTitle("Hello lecturers");
		return lectureBlockDao.update(lectureBlock);
	}

}
