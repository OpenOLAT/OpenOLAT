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

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionLog;
import org.olat.course.assessment.AssessmentInspectionLog.Action;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionLogDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentInspectionDAO inspectionDao;
	@Autowired
	private AssessmentInspectionLogDAO inspectionLogDao;
	@Autowired
	private AssessmentInspectionConfigurationDAO inspectionConfigurationDao;
	
	@Test
	public void createInspectionLog() {
		Identity assessedId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		Identity doerId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection = inspectionDao
				.createInspection(assessedId, new Date(), new Date(), null, null, "123456", config);
		AssessmentInspectionLog inspectionLog = inspectionLogDao.createLog(Action.create, null, null, inspection, doerId);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(inspectionLog);
		Assert.assertNotNull(inspectionLog.getKey());
		Assert.assertNotNull(inspectionLog.getCreationDate());
		Assert.assertEquals(inspection, inspectionLog.getInspection());
		Assert.assertEquals(doerId, inspectionLog.getDoer());
	}
	
	@Test
	public void loadLogsByInspection() {
		Identity assessedId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		Identity doerId = JunitTestHelper.createAndPersistIdentityAsRndUser("inspection-log");
		
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		AssessmentInspection inspection = inspectionDao
				.createInspection(assessedId, new Date(), new Date(), null, null, "123456", config);
		AssessmentInspectionLog inspectionLog = inspectionLogDao.createLog(Action.create, null, null, inspection, doerId);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentInspectionLog> loadedInspectionLogs = inspectionLogDao.loadLogs(inspection, null, null);
		assertThat(loadedInspectionLogs)
			.hasSize(1)
			.containsExactly(inspectionLog);
		
		AssessmentInspectionLog loadedInspectionLog = loadedInspectionLogs.get(0);
		
		Assert.assertNotNull(loadedInspectionLog);
		Assert.assertNotNull(loadedInspectionLog.getKey());
		Assert.assertNotNull(loadedInspectionLog.getCreationDate());
		Assert.assertEquals(Action.create, loadedInspectionLog.getAction());
		Assert.assertEquals(inspection, loadedInspectionLog.getInspection());
		Assert.assertEquals(doerId, loadedInspectionLog.getDoer());
	}
}
