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
package org.olat.modules.assessment.manager;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 20.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentEntryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	
	@Test
	public void createAssessmentEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = "39485349759";

		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, entry);
		Assert.assertNotNull(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		//check values
		Assert.assertNotNull(nodeAssessment.getKey());
		Assert.assertNotNull(nodeAssessment.getCreationDate());
		Assert.assertNotNull(nodeAssessment.getLastModified());
		Assert.assertEquals(assessedIdentity, nodeAssessment.getIdentity());
		Assert.assertEquals(entry, nodeAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, nodeAssessment.getSubIdent());
	}
	
	@Test
	public void createAssessmentEntry_anonymous() {
		String anonymousIdentifier = UUID.randomUUID().toString();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = "39485349759";
		

		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(null, anonymousIdentifier, entry, subIdent, entry);
		Assert.assertNotNull(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		//check values
		Assert.assertNotNull(nodeAssessment.getKey());
		Assert.assertNotNull(nodeAssessment.getCreationDate());
		Assert.assertNotNull(nodeAssessment.getLastModified());
		Assert.assertEquals(anonymousIdentifier, nodeAssessment.getAnonymousIdentifier());
		Assert.assertEquals(entry, nodeAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, nodeAssessment.getSubIdent());
	}
	
	@Test
	public void loadCourseNodeAssessmentById() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-2");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment.getKey());
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(assessedIdentity, reloadedAssessment.getIdentity());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
	}
	
	@Test
	public void loadAssessmentEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = assessmentEntryDao
				.loadAssessmentEntry(assessedIdentity, null, entry, subIdent);
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(assessedIdentity, reloadedAssessment.getIdentity());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
	}
	
	@Test
	public void loadAssessmentEntry_anonymous() {
		String anonymousIdentifier = UUID.randomUUID().toString();
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(null, anonymousIdentifier, entry, subIdent, entry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessment = assessmentEntryDao
				.loadAssessmentEntry(null, anonymousIdentifier, entry, subIdent);
		Assert.assertEquals(nodeAssessment.getKey(), reloadedAssessment.getKey());
		Assert.assertEquals(nodeAssessment, reloadedAssessment);
		Assert.assertEquals(anonymousIdentifier, reloadedAssessment.getAnonymousIdentifier());
		Assert.assertEquals(entry, reloadedAssessment.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessment.getSubIdent());
	}
	
	@Test
	public void loadAssessmentEntry_specificTest() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-5");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentRef = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, refEntry);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry reloadedAssessmentRef = assessmentEntryDao
				.loadAssessmentEntry(assessedIdentity, entry, subIdent, refEntry);
		Assert.assertEquals(nodeAssessmentRef.getKey(), reloadedAssessmentRef.getKey());
		Assert.assertEquals(nodeAssessmentRef, reloadedAssessmentRef);
		Assert.assertEquals(assessedIdentity, reloadedAssessmentRef.getIdentity());
		Assert.assertEquals(entry, reloadedAssessmentRef.getRepositoryEntry());
		Assert.assertEquals(subIdent, reloadedAssessmentRef.getSubIdent());
	}
	
	@Test
	public void resetAssessmentEntry() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentRef = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, refEntry, 2.0f, Boolean.TRUE, null, null);
		dbInstance.commitAndCloseSession();
		
		AssessmentEntry resetedAssessmentRef = assessmentEntryDao
				.resetAssessmentEntry(nodeAssessmentRef);
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(nodeAssessmentRef, resetedAssessmentRef);
		Assert.assertEquals(assessedIdentity, resetedAssessmentRef.getIdentity());
		Assert.assertNull(resetedAssessmentRef.getScore());
		Assert.assertNull(resetedAssessmentRef.getPassed());
		Assert.assertEquals(new Integer(0), resetedAssessmentRef.getAttempts());
		Assert.assertNull(resetedAssessmentRef.getCompletion());
		
		// double check by reloading the entry
		AssessmentEntry reloadedAssessmentRef = assessmentEntryDao
				.loadAssessmentEntryById(resetedAssessmentRef.getKey());
		dbInstance.commitAndCloseSession();

		Assert.assertEquals(nodeAssessmentRef, reloadedAssessmentRef);
		Assert.assertNull(reloadedAssessmentRef.getScore());
		Assert.assertNull(reloadedAssessmentRef.getPassed());
		Assert.assertEquals(new Integer(0), reloadedAssessmentRef.getAttempts());
		Assert.assertNull(reloadedAssessmentRef.getCompletion());
	}
	
	@Test
	public void setLastVisit() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, refEntry, 2.0f, Boolean.TRUE, null, null);
		dbInstance.commitAndCloseSession();
		
		Date firstDate = new GregorianCalendar(2013,1,28,13,24,56).getTime();
		Date secondDate = new GregorianCalendar(2014,1,1,1,1,1).getTime();
		
		nodeAssessment = assessmentEntryDao.setLastVisit(nodeAssessment, firstDate);
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(nodeAssessment.getFirstVisit(), firstDate);
		Assert.assertEquals(nodeAssessment.getLastVisit(), firstDate);
		Assert.assertEquals(nodeAssessment.getNumberOfVisits().intValue(), 1);
		
		nodeAssessment = assessmentEntryDao.setLastVisit(nodeAssessment, secondDate);
		dbInstance.commitAndCloseSession();
		
		Assert.assertEquals(nodeAssessment.getFirstVisit(), firstDate);
		Assert.assertEquals(nodeAssessment.getLastVisit(), secondDate);
		Assert.assertEquals(nodeAssessment.getNumberOfVisits().intValue(), 2);
	}
	
	@Test
	public void setAssessmentDone() {
		Identity assessedIdentity = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-6");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity, null, entry, subIdent, refEntry, 2.0f, Boolean.TRUE, null, null);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.done);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(null);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.inProgress);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNull(nodeAssessment.getAssessmentDone());
		
		nodeAssessment.setAssessmentStatus(AssessmentEntryStatus.done);
		nodeAssessment = assessmentEntryDao.updateAssessmentEntry(nodeAssessment);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(nodeAssessment.getAssessmentDone());
	}
	
	@Test
	public void loadAssessmentEntries_subIdent() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-7");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-8");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, null, entry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		dbInstance.commitAndCloseSession();
		
		// load with our subIdent above
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao
				.loadAssessmentEntryBySubIdent(entry, subIdent);
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(2, assessmentEntries.size());
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId1));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId4));
	}
	
	@Test
	public void getAllIdentitiesWithAssessmentData() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-9");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-10");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-11");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-12");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String subIdent = UUID.randomUUID().toString();
		assessmentEntryDao.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity3, null, entry, null, entry, 3.0f, Boolean.FALSE, null, null);
		assessmentEntryDao.createAssessmentEntry(assessedIdentity4, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		dbInstance.commitAndCloseSession();

		// id 1,2,3 are in the entry, but 4 is in an other entry and must not appears in the list
		List<Identity> assessedIdentities = assessmentEntryDao.getAllIdentitiesWithAssessmentData(entry);
		Assert.assertNotNull(assessedIdentities);
		Assert.assertEquals(3, assessedIdentities.size());
		Assert.assertTrue(assessedIdentities.contains(assessedIdentity1));
		Assert.assertTrue(assessedIdentities.contains(assessedIdentity2));
		Assert.assertTrue(assessedIdentities.contains(assessedIdentity3));
		Assert.assertFalse(assessedIdentities.contains(assessedIdentity4));
	}
	
	@Test
	public void loadAssessmentEntriesByAssessedIdentity() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-13");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-14");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-15");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, null, entry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		dbInstance.commitAndCloseSession();
		
		// load for identity 1
		List<AssessmentEntry> assessmentEntriesId1 = assessmentEntryDao
				.loadAssessmentEntriesByAssessedIdentity(assessedIdentity1, entry);
		Assert.assertNotNull(assessmentEntriesId1);
		Assert.assertEquals(1, assessmentEntriesId1.size());
		Assert.assertTrue(assessmentEntriesId1.contains(nodeAssessmentId1));
		Assert.assertFalse(assessmentEntriesId1.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntriesId1.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntriesId1.contains(nodeAssessmentId4));
		
		//load for identity 2
		List<AssessmentEntry> assessmentEntriesId2 = assessmentEntryDao
				.loadAssessmentEntriesByAssessedIdentity(assessedIdentity2, entry);
		Assert.assertNotNull(assessmentEntriesId2);
		Assert.assertEquals(2, assessmentEntriesId2.size());
		Assert.assertFalse(assessmentEntriesId2.contains(nodeAssessmentId1));
		Assert.assertTrue(assessmentEntriesId2.contains(nodeAssessmentId2));
		Assert.assertTrue(assessmentEntriesId2.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntriesId2.contains(nodeAssessmentId4));
		
		//load for identity 3
		List<AssessmentEntry> assessmentEntriesId3 = assessmentEntryDao
				.loadAssessmentEntriesByAssessedIdentity(assessedIdentity3, entry);
		Assert.assertNotNull(assessmentEntriesId3);
		Assert.assertEquals(0, assessmentEntriesId3.size());
	}
	
	@Test
	public void loadAssessmentEntryByGroup() {
		// a simulated course with 2 groups
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-16");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-17");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		BusinessGroup group1 = businessGroupDao.createAndPersist(null, "assessment-bg-1", "assessment-bg-1-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group1, entry);
		BusinessGroup group2 = businessGroupDao.createAndPersist(null, "assessment-bg-2", "assessment-bg-2-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group2, entry);
		
		businessGroupRelationDao.addRole(assessedIdentity1, group1, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group1, GroupRoles.participant.name());
		dbInstance.commitAndCloseSession();
		
		// some assessment entries
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, null, entry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		dbInstance.commitAndCloseSession();
		
		//load the assessment entries of entry
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao.loadAssessmentEntryByGroup(group1.getBaseGroup(), entry, subIdent);
		
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(2, assessmentEntries.size());
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId1));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId4));
	}
	
	@Test
	public void removeEntryForReferenceEntry() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-18");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-19");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment1 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		AssessmentEntry nodeAssessment2 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessment3 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, null, entry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessment4 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		dbInstance.commitAndCloseSession();
		
		// delete by reference
		int affectedRows = assessmentEntryDao.removeEntryForReferenceEntry(refEntry);
		dbInstance.commitAndCloseSession();
		Assert.assertEquals(3, affectedRows);

		//check
		AssessmentEntry deletedAssessmentEntry1 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment1.getKey());
		Assert.assertNotNull(deletedAssessmentEntry1);
		Assert.assertNull(deletedAssessmentEntry1.getReferenceEntry());
		AssessmentEntry deletedAssessmentEntry2 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment2.getKey());
		Assert.assertNotNull(deletedAssessmentEntry2);
		Assert.assertNull(deletedAssessmentEntry2.getReferenceEntry());
		AssessmentEntry deletedAssessmentEntry3 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment3.getKey());
		Assert.assertNotNull(deletedAssessmentEntry3);
		Assert.assertNotNull(deletedAssessmentEntry3.getReferenceEntry());
		Assert.assertEquals(entry, deletedAssessmentEntry3.getReferenceEntry());
		AssessmentEntry deletedAssessmentEntry4 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment4.getKey());
		Assert.assertNotNull(deletedAssessmentEntry4);
		Assert.assertNull(deletedAssessmentEntry4.getReferenceEntry());
	}
	
	@Test
	public void deleteEntryForRepositoryEntry() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-20");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-21");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		String subIdent = UUID.randomUUID().toString();
		AssessmentEntry nodeAssessment1 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		AssessmentEntry nodeAssessment2 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessment3 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, null, entry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessment4 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		dbInstance.commitAndCloseSession();
		
		// delete by reference
		assessmentEntryDao.deleteEntryForRepositoryEntry(entry);
		dbInstance.commitAndCloseSession();

		//check
		AssessmentEntry deletedAssessmentEntry1 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment1.getKey());
		Assert.assertNull(deletedAssessmentEntry1);
		AssessmentEntry deletedAssessmentEntry2 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment2.getKey());
		Assert.assertNull(deletedAssessmentEntry2);
		AssessmentEntry deletedAssessmentEntry3 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment3.getKey());
		Assert.assertNull(deletedAssessmentEntry3);
		AssessmentEntry deletedAssessmentEntry4 = assessmentEntryDao.loadAssessmentEntryById(nodeAssessment4.getKey());
		Assert.assertNotNull(deletedAssessmentEntry4);
	}
	
	@Test
	public void loadAssessmentEntryBySubIdentWithStatus() {
		Identity assessedIdentity1 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-22");
		Identity assessedIdentity2 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-23");
		Identity assessedIdentity3 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-24");
		Identity assessedIdentity4 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-25");
		Identity assessedIdentity5 = JunitTestHelper.createAndPersistIdentityAsRndUser("as-node-25");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry refEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = UUID.randomUUID().toString();
		BusinessGroup group = businessGroupDao.createAndPersist(null, "rel-bg-part-1", "rel-bgis-1-desc", -1, -1, false, false, false, false, false);
		businessGroupRelationDao.addRelationToResource(group, entry);
		businessGroupRelationDao.addRelationToResource(group, refEntry);
		businessGroupRelationDao.addRole(assessedIdentity1, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity2, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity3, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity4, group, GroupRoles.participant.name());
		businessGroupRelationDao.addRole(assessedIdentity5, group, GroupRoles.coach.name());
		
		AssessmentEntry nodeAssessmentId1 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity1, null, entry, subIdent, refEntry);
		AssessmentEntry nodeAssessmentId2 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, subIdent, refEntry, 0.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId3 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, entry, null, entry, 12.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId4 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity2, null, refEntry, subIdent, refEntry, 3.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId5 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity3, null, entry, subIdent, refEntry, 6.0f, Boolean.TRUE, null, null);
		AssessmentEntry nodeAssessmentId6 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity4, null, entry, subIdent, refEntry, 1.0f, Boolean.FALSE, null, null);
		AssessmentEntry nodeAssessmentId7 = assessmentEntryDao
				.createAssessmentEntry(assessedIdentity5, null, entry, subIdent, refEntry, 10.0f, Boolean.TRUE, null, null);
		dbInstance.commitAndCloseSession();
		// load with our subIdent above
		List<AssessmentEntry> assessmentEntries = assessmentEntryDao
				.loadAssessmentEntryBySubIdentWithStatus(entry, subIdent, null, true);
		Assert.assertNotNull(assessmentEntries);
		Assert.assertEquals(2, assessmentEntries.size());
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId1));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId2));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId3));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId4));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId5));
		Assert.assertTrue(assessmentEntries.contains(nodeAssessmentId6));
		Assert.assertFalse(assessmentEntries.contains(nodeAssessmentId7));
	}
}