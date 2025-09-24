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
package org.olat.course.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.model.AssessmentEntryInspection;
import org.olat.course.assessment.ui.inspection.SearchAssessmentInspectionParameters;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentInspectionDAO inspectionDao;
	@Autowired
	private AssessmentInspectionConfigurationDAO inspectionConfigurationDao;
	
	@Test
	public void createInspectionRelation() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-1-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection = inspectionDao
				.createInspection(id, new Date(), new Date(), 5, "access-code", "123456", config);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(inspection);
		Assert.assertNotNull(inspection.getKey());
		Assert.assertNotNull(inspection.getCreationDate());
		Assert.assertNotNull(inspection.getLastModified());
		Assert.assertNotNull(inspection.getFromDate());
		Assert.assertNotNull(inspection.getToDate());
		Assert.assertEquals("access-code", inspection.getAccessCode());
		Assert.assertEquals(Integer.valueOf(5), inspection.getExtraTime());
		Assert.assertEquals(config, inspection.getConfiguration());
	}
	
	@Test
	public void searchInspectionRelationByCourseElement() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-2-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection = inspectionDao
				.createInspection(id, new Date(), new Date(), 15, "access-code-v1", "123456", config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(inspection);
		
		List<AssessmentInspection> inspectionlist = inspectionDao.searchInspection(entry, "123456");
		assertThat(inspectionlist)
			.hasSize(1)
			.containsExactly(inspection);
		
		AssessmentInspection loadedInspection = inspectionlist.get(0);
		Assert.assertEquals(config, loadedInspection.getConfiguration());
		Assert.assertEquals("access-code-v1", inspection.getAccessCode());
		Assert.assertEquals(Integer.valueOf(15), inspection.getExtraTime());
	}
	
	@Test
	public void searchInspectionForIdentity() {
		String subIdent = "123456B";
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-3-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-4-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		// Test
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(id1, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry1, id1, null, 300, true);
		testSession1.setFinishTime(new Date());
		testSession1.setTerminationTime(new Date());
		testSessionDao.update(testSession1);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(id2, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry2, id2, null, 300, true);
		testSession2.setFinishTime(new Date());
		testSession2.setTerminationTime(new Date());
		testSessionDao.update(testSession2);
		// Configuration
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);

		Date now = new Date();
		AssessmentInspection inspection1 = inspectionDao
				.createInspection(id1, DateUtils.addHours(now, -2), DateUtils.addHours(now, 2), null, null, "123456B", config);
		dbInstance.commitAndCloseSession();

		// Search by identity 1;
		List<AssessmentInspection> inspectionsToIdentity1 = inspectionDao.searchInspectionFor(id1, now);
		assertThat(inspectionsToIdentity1)
			.hasSize(1)
			.containsExactly(inspection1);
		AssessmentInspection loadedInspection = inspectionsToIdentity1.get(0);
		Assert.assertEquals(config, loadedInspection.getConfiguration());
		
		// Search empty
		List<AssessmentInspection> inspectionsToIdentity2 = inspectionDao.searchInspectionFor(id2, now);
		assertThat(inspectionsToIdentity2)
			.isEmpty();
	}
	
	@Test
	public void searchInspectionForIdentityByInspection() {
		String subIdent = "123456C";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-5-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		// Test
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(id, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry, id, null, 300, true);
		testSession.setFinishTime(new Date());
		testSession.setTerminationTime(new Date());
		testSessionDao.update(testSession);
		// Inspection
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection = inspectionDao
				.createInspection(id, DateUtils.addHours(new Date(), -1), DateUtils.addHours(new Date(), 1), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		
		// Search;
		AssessmentInspection loadedInspection = inspectionDao.searchInspectionFor(id, new Date(), inspection.getKey());
		Assert.assertEquals(inspection, loadedInspection);
		Assert.assertEquals(config, loadedInspection.getConfiguration());
		
		// Search empty
		AssessmentInspection noInspection = inspectionDao.searchInspectionFor(id, new Date(), 27347382978l);
		Assert.assertNull(noInspection);
	}
	
	@Test
	public void hasAssessmentTestSession() {
		String subIdent = "123465H";
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-9-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-10-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		// Test
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(id1, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry, id1, null, 300, false);
		testSession.setFinishTime(new Date());
		testSession.setTerminationTime(new Date());
		testSessionDao.update(testSession);
		dbInstance.commitAndCloseSession();

		// Id 1 has test
		boolean hasTestSession = inspectionDao.hasAssessmentTestSession(id1, entry, subIdent);
		Assert.assertTrue(hasTestSession);

		boolean hasNoTestSession = inspectionDao.hasAssessmentTestSession(id2, entry, subIdent);
		Assert.assertFalse(hasNoTestSession);
	}
	
	@Test
	public void hasInspection() {
		String subIdent = "123266H";
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-30-");
		// A configuration
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		
		// Test
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(id, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry, id, null, 300, false);
		testSession.setFinishTime(new Date());
		testSession.setTerminationTime(new Date());
		testSessionDao.update(testSession);
		inspectionDao.createInspection(id, new Date(), new Date(), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();

		// Configuration has one inspection
		int hasTestSession = inspectionDao.hasInspection(config);
		Assert.assertEquals(1, hasTestSession);
	}
	
	@Test
	public void searchInspection() {
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-6-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-7-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection1 = inspectionDao
				.createInspection(id1, new Date(), new Date(), null, null, "123456D", config);
		AssessmentInspection inspection2 = inspectionDao
				.createInspection(id2, new Date(), new Date(), null, null, "123456D", config);
		AssessmentInspection inspection3 = inspectionDao
				.createInspection(id2, new Date(), new Date(), null, null, "123456D", config);
		dbInstance.commitAndCloseSession();
		
		// Search matches
		SearchAssessmentInspectionParameters params = new SearchAssessmentInspectionParameters();
		params.setEntry(entry);
		params.setSubIdents(List.of("123456D"));
		List<AssessmentEntryInspection> loadedInspectionsToIdentities = inspectionDao.searchInspection(params);
		assertThat(loadedInspectionsToIdentities)
			.hasSize(3)
			.map(AssessmentEntryInspection::inspection)
			.containsExactlyInAnyOrder(inspection1, inspection2, inspection3);
		
		// Search empty
		SearchAssessmentInspectionParameters paramsEmpty = new SearchAssessmentInspectionParameters();
		paramsEmpty.setEntry(entry);
		paramsEmpty.setSubIdents(List.of("1234DFG"));
		List<AssessmentEntryInspection> noInspectionsToIdentities = inspectionDao.searchInspection(paramsEmpty);
		assertThat(noInspectionsToIdentities)
			.isEmpty();
	}
	
	@Test
	public void searchActiveInspection() {
		Date now = new Date();
		String subIdent = "123456E";
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-6-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-7-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection1 = inspectionDao
				.createInspection(id1, DateUtils.addHours(now, -3), DateUtils.addHours(now, -1), null, null, subIdent, config);
		AssessmentInspection inspection2 = inspectionDao
				.createInspection(id2, DateUtils.addHours(now, -1), DateUtils.addHours(now, 1), null, null, subIdent, config);
		AssessmentInspection inspection3 = inspectionDao
				.createInspection(id2, DateUtils.addHours(now, 1), DateUtils.addHours(now, 3), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		
		// Search active matches
		SearchAssessmentInspectionParameters activeParams = new SearchAssessmentInspectionParameters();
		activeParams.setEntry(entry);
		activeParams.setSubIdents(List.of(subIdent));
		activeParams.setActiveInspections(Boolean.TRUE);
		List<AssessmentEntryInspection> activeInspections = inspectionDao.searchInspection(activeParams);
		assertThat(activeInspections)
			.hasSize(1)
			.map(AssessmentEntryInspection::inspection)
			.containsExactlyInAnyOrder(inspection2);
		
		// Search inactive matches
		SearchAssessmentInspectionParameters inactiveParams = new SearchAssessmentInspectionParameters();
		inactiveParams.setEntry(entry);
		inactiveParams.setSubIdents(List.of(subIdent));
		inactiveParams.setActiveInspections(Boolean.FALSE);
		List<AssessmentEntryInspection> inactiveInspections = inspectionDao.searchInspection(inactiveParams);
		assertThat(inactiveInspections)
			.hasSize(2)
			.map(AssessmentEntryInspection::inspection)
			.containsExactlyInAnyOrder(inspection1, inspection3);
	}
	
	@Test
	public void searchNoShowInspections() {
		Date now = new Date();
		String subIdent = "123799A";
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-31-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection = inspectionDao
				.createInspection(id, DateUtils.addHours(now, -3), DateUtils.addHours(now, -1), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentInspection> noShowInspections = inspectionDao.searchNoShowInspections(new Date());
		Assertions.assertThat(noShowInspections)
			.contains(inspection);
	}
	
	@Test
	public void searchInspectionsToStart() {
		Date now = new Date();
		String subIdent = "123799B";
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-32-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		
		// Test for id1
		AssessmentEntry assessmentEntryId = assessmentService.getOrCreateAssessmentEntry(id, null, entry, subIdent, null, entry);
		AssessmentTestSession testSessionId = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntryId, id, null, 300, false);
		testSessionId.setFinishTime(new Date());
		testSessionId.setTerminationTime(new Date());
		testSessionDao.update(testSessionId);
		dbInstance.commitAndCloseSession();

		AssessmentInspection inspectionToStart = inspectionDao
				.createInspection(id, DateUtils.addHours(now, -1), DateUtils.addHours(now, 1), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentInspection> currentInspections = inspectionDao.searchInspectionsToStart(new Date());
		Assertions.assertThat(currentInspections)
			.contains(inspectionToStart);
	}
	
	@Test
	public void searchInspectionsToStartNegativeTest() {
		Date now = new Date();
		String subIdent = "123799C";
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-33-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		
		// Test for id1
		AssessmentEntry assessmentEntryId = assessmentService.getOrCreateAssessmentEntry(id, null, entry, subIdent, null, entry);
		AssessmentTestSession testSessionId = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntryId, id, null, 300, false);
		testSessionId.setFinishTime(new Date());
		testSessionId.setTerminationTime(new Date());
		testSessionDao.update(testSessionId);
		dbInstance.commitAndCloseSession();

		// Inspection in the past
		AssessmentInspection inspectionDone = inspectionDao
				.createInspection(id, DateUtils.addHours(now, -3), DateUtils.addHours(now, -2), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentInspection> currentInspections = inspectionDao.searchInspectionsToStart(new Date());
		Assertions.assertThat(currentInspections)
			.doesNotContain(inspectionDone);
	}
	
	@Test
	public void searchInProgressInspectionsToClose() {
		Date now = new Date();
		String subIdent = "123799C";
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-34-");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-35-");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);

		// Inspection still in progress with time left
		AssessmentInspection inspectionInProgress = inspectionDao
				.createInspection(id1, DateUtils.addHours(now, -2), DateUtils.addHours(now, 2), null, null, subIdent, config);
		inspectionInProgress.setInspectionStatus(AssessmentInspectionStatusEnum.inProgress);
		inspectionInProgress = inspectionDao.updateInspection(inspectionInProgress);
		
		// Inspection in progress but dates are in the past
		AssessmentInspection inspectionToClose = inspectionDao
				.createInspection(id2, DateUtils.addHours(now, -2), DateUtils.addHours(now, -1), null, null, subIdent, config);
		inspectionToClose.setInspectionStatus(AssessmentInspectionStatusEnum.inProgress);
		inspectionToClose = inspectionDao.updateInspection(inspectionToClose);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentInspection> currentInspections = inspectionDao.searchInProgressInspectionsToClose(new Date());
		Assertions.assertThat(currentInspections)
			.contains(inspectionToClose)
			.doesNotContain(inspectionInProgress);
	}
	
	@Test
	public void deleteInspection() {
		Date now = new Date();
		String subIdent = "123456F";
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-36");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-37");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection1 = inspectionDao
				.createInspection(id1, DateUtils.addHours(now, -3), DateUtils.addHours(now, -1), null, null, subIdent, config);
		AssessmentInspection inspection2 = inspectionDao
				.createInspection(id2, DateUtils.addHours(now, -1), DateUtils.addHours(now, 1), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(inspection1);
		Assert.assertNotNull(inspection2);
		
		inspectionDao.deleteInspection(inspection1);
		dbInstance.commitAndCloseSession();
		
		AssessmentInspection reloadedInspection1 = inspectionDao.loadByKey(inspection1.getKey());
		Assert.assertNull(reloadedInspection1);
		AssessmentInspection reloeadedInspection2 = inspectionDao.loadByKey(inspection2.getKey());
		Assert.assertNotNull(reloeadedInspection2);
	}
	
	@Test
	public void deleteInspectionsByConfig() {
		Date now = new Date();
		String subIdent = "123456F";
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-8");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-9");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection1 = inspectionDao
				.createInspection(id1, DateUtils.addHours(now, -3), DateUtils.addHours(now, -1), null, null, subIdent, config);
		AssessmentInspection inspection2 = inspectionDao
				.createInspection(id2, DateUtils.addHours(now, -1), DateUtils.addHours(now, 1), null, null, subIdent, config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(inspection1);
		Assert.assertNotNull(inspection2);
		
		inspectionDao.deleteInspections(config);
		dbInstance.commitAndCloseSession();
		
		AssessmentInspection deletedInspection1 = inspectionDao.loadByKey(inspection1.getKey());
		Assert.assertNull(deletedInspection1);
		AssessmentInspection deletedInspection2 = inspectionDao.loadByKey(inspection2.getKey());
		Assert.assertNull(deletedInspection2);
	}
	
	@Test
	public void deleteInspectionParano() {
		Date now = new Date();
		String subIdent = "123456G";
		
		Identity id1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-10");
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspect-11");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration configToDelete = inspectionConfigurationDao.createInspectionConfiguration(entry);
		configToDelete = inspectionConfigurationDao.saveConfiguration(configToDelete);
		AssessmentInspectionConfiguration configToKeep = inspectionConfigurationDao.createInspectionConfiguration(entry);
		configToKeep = inspectionConfigurationDao.saveConfiguration(configToKeep);

		AssessmentInspection inspectionToDelete = inspectionDao
				.createInspection(id1, DateUtils.addHours(now, -3), DateUtils.addHours(now, -1), null, null, subIdent, configToDelete);
		AssessmentInspection inspectionToKeep = inspectionDao
				.createInspection(id2, DateUtils.addHours(now, -1), DateUtils.addHours(now, 1), null, null, subIdent, configToKeep);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(inspectionToDelete);
		Assert.assertNotNull(inspectionToKeep);
		
		inspectionDao.deleteInspections(configToDelete);
		dbInstance.commitAndCloseSession();
		
		AssessmentInspection deletedInspection = inspectionDao.loadByKey(inspectionToDelete.getKey());
		Assert.assertNull(deletedInspection);
		AssessmentInspection keepedInspection = inspectionDao.loadByKey(inspectionToKeep.getKey());
		Assert.assertNotNull(keepedInspection);
		Assert.assertEquals(inspectionToKeep, keepedInspection);
	}

}
