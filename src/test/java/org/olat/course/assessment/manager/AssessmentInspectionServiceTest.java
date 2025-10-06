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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionLog;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private AssessmentTestSessionDAO testSessionDao;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	@Test
	public void createInspectionForIdentity() {
		Identity assessedId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		Identity doerId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		
		String subIdent = "123456";
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		// prepare a test and a user
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(assessedId, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry, assessedId, null, 300, true);
		testSession.setFinishTime(new Date());
		testSession.setTerminationTime(new Date());
		testSessionDao.update(testSession);
		// Inspection
		AssessmentInspectionConfiguration config = inspectionService.createInspectionConfiguration(entry);
		config = inspectionService.saveConfiguration(config);
		
		Date now = new Date();
		inspectionService.addInspection(config, DateUtils.addDays(now, -2), DateUtils.addDays(now, 2),
				null, false, subIdent, List.of(assessedId), doerId);
		dbInstance.commitAndCloseSession();
		
		// Check inspection to identity
		List<AssessmentInspection> inspectionList = inspectionService.getInspectionFor(assessedId, new Date());
		Assertions.assertThat(inspectionList)
			.hasSize(1);
		AssessmentInspection inspection = inspectionList.get(0);
		Assert.assertEquals(assessedId, inspection.getIdentity());
		Assert.assertEquals(config, inspection.getConfiguration());
		
		// Check log
		List<AssessmentInspectionLog> inspectionLogList = inspectionService.getLogFor(inspection, null, null);
		Assertions.assertThat(inspectionLogList)
			.hasSize(1);
		AssessmentInspectionLog inspectionLog = inspectionLogList.get(0);
		Assert.assertEquals(inspection, inspectionLog.getInspection());
		Assert.assertEquals(doerId, inspectionLog.getDoer());
		// Check XML deserialization
		Object after = AssessmentInspectionXStream.fromXml(inspectionLog.getAfter(), AssessmentInspection.class);
		Assert.assertNotNull(after);
	}
	
	@Test
	public void deleteRepositoryEntryWithInspections() {
		Identity assessedId1 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log-2");
		Identity assessedId2 = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log-3");
		Identity doerId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		
		String subIdent = "123456";
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		// prepare a test and a user
		AssessmentEntry assessmentEntry1 = assessmentService.getOrCreateAssessmentEntry(assessedId1, null, entry, subIdent, null, entry);
		AssessmentEntry assessmentEntry2 = assessmentService.getOrCreateAssessmentEntry(assessedId2, null, entry, subIdent, null, entry);
		AssessmentTestSession testSession1 = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry1, assessedId1, null, 300, true);
		AssessmentTestSession testSession2 = testSessionDao.createAndPersistTestSession(entry, entry, subIdent, assessmentEntry2, assessedId2, null, null, true);
		testSession1.setFinishTime(new Date());
		testSessionDao.update(testSession1);
		testSession2.setFinishTime(new Date());
		testSessionDao.update(testSession2);
		// Inspection
		AssessmentInspectionConfiguration config = inspectionService.createInspectionConfiguration(entry);
		config = inspectionService.saveConfiguration(config);
		
		Date now = new Date();
		inspectionService.addInspection(config, DateUtils.addDays(now, -2), DateUtils.addDays(now, 2),
				null, false, subIdent, List.of(assessedId1), doerId);
		inspectionService.addInspection(config, DateUtils.addDays(now, -2), DateUtils.addDays(now, 2),
				null, false, subIdent, List.of(assessedId2), doerId);
		dbInstance.commitAndCloseSession();
		
		ErrorList errors = repositoryService.deletePermanently(entry, doerId, Roles.administratorRoles(), Locale.ENGLISH);
		Assert.assertFalse(errors.hasErrors());
	}
}
