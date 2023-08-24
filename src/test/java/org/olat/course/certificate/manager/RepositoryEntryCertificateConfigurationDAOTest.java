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
package org.olat.course.certificate.manager;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.certificate.CertificationTimeUnit;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 avr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryCertificateConfigurationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryCertificateConfigurationDAO repositoryEntryCertificateConfigurationDao;
	
	@Test
	public void createConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(config);
		Assert.assertNotNull(config.getKey());
		Assert.assertNotNull(config.getCreationDate());
		Assert.assertNotNull(config.getLastModified());
		Assert.assertEquals(entry, config.getEntry());
		Assert.assertFalse(config.isAutomaticCertificationEnabled());
		Assert.assertFalse(config.isManualCertificationEnabled());
		Assert.assertFalse(config.isValidityEnabled());
	}
	
	@Test
	public void getConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.createConfiguration(entry);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryCertificateConfiguration reloadedConfig = repositoryEntryCertificateConfigurationDao.getConfiguration(entry);
		Assert.assertNotNull(config);
		Assert.assertNotNull(reloadedConfig);
		Assert.assertEquals(config, reloadedConfig);
	}
	
	@Test
	public void updateConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		
		config.setAutomaticCertificationEnabled(true);
		config.setManualCertificationEnabled(true);
		config.setValidityEnabled(true);
		
		repositoryEntryCertificateConfigurationDao.updateConfiguration(config);
		dbInstance.commit();
		
		RepositoryEntryCertificateConfiguration reloadedConfig = repositoryEntryCertificateConfigurationDao.getConfiguration(entry);
		Assert.assertNotNull(reloadedConfig);
		Assert.assertTrue(reloadedConfig.isAutomaticCertificationEnabled());
		Assert.assertTrue(reloadedConfig.isManualCertificationEnabled());
		Assert.assertTrue(reloadedConfig.isValidityEnabled());
	}
	
	@Test
	public void cloneConfiguration() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		
		config.setAutomaticCertificationEnabled(true);
		config.setManualCertificationEnabled(true);
		config.setCertificateCustom1("Hello");
		config.setCertificateCustom2("World");
		config.setCertificateCustom3("!");
		config.setValidityEnabled(true);
		config.setValidityTimelapse(2);
		config.setValidityTimelapseUnit(CertificationTimeUnit.month);
		config.setRecertificationLeadTimeEnabled(true);
		config.setRecertificationLeadTimeInDays(24);
		
		config = repositoryEntryCertificateConfigurationDao.updateConfiguration(config);
		dbInstance.commit();

		RepositoryEntry copyEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		repositoryEntryCertificateConfigurationDao.cloneConfiguration(config, copyEntry);
		
		// Reload the copy
		RepositoryEntryCertificateConfiguration reloadedConfig = repositoryEntryCertificateConfigurationDao.getConfiguration(copyEntry);
		Assert.assertNotNull(reloadedConfig);
		Assert.assertTrue(reloadedConfig.isAutomaticCertificationEnabled());
		Assert.assertTrue(reloadedConfig.isManualCertificationEnabled());
		Assert.assertEquals("Hello", reloadedConfig.getCertificateCustom1());
		Assert.assertEquals("World", config.getCertificateCustom2());
		Assert.assertEquals("!", config.getCertificateCustom3());
		Assert.assertTrue(reloadedConfig.isValidityEnabled());
		Assert.assertEquals(2, config.getValidityTimelapse());
		Assert.assertEquals(CertificationTimeUnit.month, config.getValidityTimelapseUnit());
		Assert.assertTrue(reloadedConfig.isRecertificationLeadTimeEnabled());
		Assert.assertEquals(24, config.getRecertificationLeadTimeInDays());
	}
	
	@Test
	public void isCertificateEnabled() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		
		boolean certificationNotYetEnabled = repositoryEntryCertificateConfigurationDao.isCertificateEnabled(entry);
		Assert.assertFalse(certificationNotYetEnabled);
		
		config.setManualCertificationEnabled(true);
		config = repositoryEntryCertificateConfigurationDao.updateConfiguration(config);
		dbInstance.commit();
		
		boolean certificationEnabled = repositoryEntryCertificateConfigurationDao.isCertificateEnabled(entry);
		Assert.assertTrue(certificationEnabled);
	}
	
	@Test
	public void isAutomaticCertificateEnabled() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();	
		RepositoryEntryCertificateConfiguration config = repositoryEntryCertificateConfigurationDao.createConfiguration(entry);
		dbInstance.commit();
		
		boolean certificationNotYetEnabled = repositoryEntryCertificateConfigurationDao.isCertificateEnabled(entry);
		Assert.assertFalse(certificationNotYetEnabled);
		
		config.setAutomaticCertificationEnabled(true);
		config = repositoryEntryCertificateConfigurationDao.updateConfiguration(config);
		dbInstance.commit();
		
		boolean certificationEnabled = repositoryEntryCertificateConfigurationDao.isAutomaticCertificationEnabled(entry);
		Assert.assertTrue(certificationEnabled);
	}
}
