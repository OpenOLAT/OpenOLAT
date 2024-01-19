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
import org.olat.course.assessment.model.AssessmentInspectionConfigurationWithUsage;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AssessmentInspectionDAO inspectionDao;
	@Autowired
	private AssessmentInspectionConfigurationDAO inspectionConfigurationDao;
	
	@Test
	public void createInspectionConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(config);
		Assert.assertNotNull(config.getKey());
		Assert.assertNotNull(config.getCreationDate());
		Assert.assertNotNull(config.getLastModified());	
	}
	
	@Test
	public void loadConfigurationsByEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		dbInstance.commitAndCloseSession();
		
		List<AssessmentInspectionConfiguration> configurations = inspectionConfigurationDao.loadConfigurationsByEntry(entry);
		assertThat(configurations)
			.hasSize(1);
		
		AssessmentInspectionConfiguration reloadedConfig = configurations.get(0);
		Assert.assertNotNull(reloadedConfig);
		Assert.assertNotNull(reloadedConfig.getKey());
		Assert.assertNotNull(reloadedConfig.getCreationDate());
		Assert.assertNotNull(reloadedConfig.getLastModified());
		Assert.assertEquals(config, reloadedConfig);
		Assert.assertEquals(entry, reloadedConfig.getRepositoryEntry());
	}
	
	@Test
	public void loadConfigurationsWithUsageByEntry() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		AssessmentInspectionConfiguration config = inspectionConfigurationDao.createInspectionConfiguration(entry);
		config = inspectionConfigurationDao.saveConfiguration(config);
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("config-1-");
		AssessmentInspection inspection = inspectionDao
				.createInspection(id, new Date(), new Date(), 5, "access-code", "abc-123", config);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(inspection);
		
		List<AssessmentInspectionConfigurationWithUsage> configurations = inspectionConfigurationDao.loadConfigurationsWithUsageByEntry(entry);
		assertThat(configurations)
			.hasSize(1);
		
		AssessmentInspectionConfiguration reloadedConfig = configurations.get(0).configuration();
		Assert.assertNotNull(reloadedConfig);
		Assert.assertNotNull(reloadedConfig.getKey());
		Assert.assertNotNull(reloadedConfig.getCreationDate());
		Assert.assertNotNull(reloadedConfig.getLastModified());
		Assert.assertEquals(config, reloadedConfig);
		Assert.assertEquals(entry, reloadedConfig.getRepositoryEntry());
		
		Assert.assertEquals(1, configurations.get(0).usage());
	}

}
