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
package org.olat.modules.qpool.manager;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.qpool.model.QLicense;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QLicenseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QLicenseDAO qpoolLicenseDao;
	
	@Test
	public void testCreate() {
		String licenseKey = "mit-license-" + UUID.randomUUID().toString();
		QLicense license = qpoolLicenseDao.create(licenseKey, null, true);
		dbInstance.commit();
		//check
		Assert.assertNotNull(license);
		Assert.assertNotNull(license.getKey());
		Assert.assertNotNull(license.getCreationDate());
		Assert.assertEquals(licenseKey, license.getLicenseKey());
		Assert.assertTrue(license.isDeletable());
	}
	
	@Test
	public void testCreateAndGet() {
		String licenseKey = "apache-license-" + UUID.randomUUID().toString();
		QLicense license = qpoolLicenseDao.create(licenseKey, null, true);
		dbInstance.commit();
		//load it
		QLicense reloadedLicense = qpoolLicenseDao.loadById(license.getKey());
		//check the values
		Assert.assertNotNull(reloadedLicense);
		Assert.assertEquals(license.getKey(), reloadedLicense.getKey());
		Assert.assertNotNull(reloadedLicense.getCreationDate());
		Assert.assertEquals(licenseKey, reloadedLicense.getLicenseKey());
		Assert.assertTrue(reloadedLicense.isDeletable());
	}
	
	@Test
	public void testCreateAndGet_byLicenseKey() {
		String licenseKey = "apache-license-" + UUID.randomUUID().toString();
		QLicense license = qpoolLicenseDao.create(licenseKey, null, true);
		dbInstance.commit();
		//load it
		QLicense reloadedLicense = qpoolLicenseDao.loadByLicenseKey(licenseKey);
		//check the values
		Assert.assertNotNull(reloadedLicense);
		Assert.assertEquals(license.getKey(), reloadedLicense.getKey());
		Assert.assertNotNull(reloadedLicense.getCreationDate());
		Assert.assertEquals(licenseKey, reloadedLicense.getLicenseKey());
		Assert.assertTrue(reloadedLicense.isDeletable());
	}
	
	@Test
	public void testGetItemLevels() {
		String licenseKey = "gnu-" + UUID.randomUUID().toString();
		QLicense license = qpoolLicenseDao.create(licenseKey, null, true);
		dbInstance.commit();
		//load it
		List<QLicense> allLicenses = qpoolLicenseDao.getLicenses();
		//check the values
		Assert.assertNotNull(allLicenses);
		Assert.assertTrue(allLicenses.contains(license));
	}
	
	@Test
	public void testDelete_deletable() {
		String licenseKey = "gpl-" + UUID.randomUUID().toString();
		QLicense license = qpoolLicenseDao.create(licenseKey, null, true);
		dbInstance.commitAndCloseSession();
		
		//delete it
		boolean deleted = qpoolLicenseDao.delete(license);
		dbInstance.commitAndCloseSession();
		Assert.assertTrue(deleted);

		//check that the type is really, really deleted
		QLicense reloadedLicense = qpoolLicenseDao.loadById(license.getKey());
		Assert.assertNull(reloadedLicense);
		List<QLicense> licenses = qpoolLicenseDao.getLicenses();
		Assert.assertFalse(licenses.contains(license));
	}
	
	@Test
	public void testDelete_notDeletable() {
		String licenseKey = "lgpl-" + UUID.randomUUID().toString();
		QLicense license = qpoolLicenseDao.create(licenseKey, null, false);
		dbInstance.commitAndCloseSession();
		
		//delete it
		boolean deleted = qpoolLicenseDao.delete(license);
		dbInstance.commitAndCloseSession();
		Assert.assertFalse(deleted);

		//check that the type is really, really deleted
		QLicense reloadedLicense = qpoolLicenseDao.loadById(license.getKey());
		Assert.assertNotNull(reloadedLicense);
		List<QLicense> allLicenses = qpoolLicenseDao.getLicenses();
		Assert.assertTrue(allLicenses.contains(license));
	}
}