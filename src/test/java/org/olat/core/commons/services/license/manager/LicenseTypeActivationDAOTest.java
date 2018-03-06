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
package org.olat.core.commons.services.license.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseTypeActivationDAOTest extends OlatTestCase {

	private static final LicenseHandler LICENSE_HANDLER = new TestableLicenseHandler();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LicenseTypeDAO licenseTypeDao;
	@Autowired
	private LicenseTypeActivationDAO licenseTypeActivationDao;
	@Autowired
	private LicenseCleaner licenseCleaner;
	
	@Before
	public void cleanUp() {
		licenseCleaner.deleteAll();
	}
	
	@Test
	public void shouldCreateActivation() {
		String name = "name";
		LicenseType licenseType = licenseTypeDao.create(name);
		licenseTypeDao.save(licenseType);
		dbInstance.commitAndCloseSession();
		
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType);
		dbInstance.commitAndCloseSession();
	
		boolean isActive = licenseTypeActivationDao.isActive(LICENSE_HANDLER, licenseType);
		
		assertThat(isActive).isTrue();
	}
	
	@Test
	public void shouldNotCreateActivationIfItIsAlreadyActive() {
		String name = "name";
		LicenseType licenseType = licenseTypeDao.create(name);
		licenseTypeDao.save(licenseType);
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType);
		dbInstance.commitAndCloseSession();
		
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType);
		dbInstance.commitAndCloseSession();
	
		boolean isActive = licenseTypeActivationDao.isActive(LICENSE_HANDLER, licenseType);
		
		assertThat(isActive).isTrue();
	}
	
	@Test
	public void shouldDeleteActivation() {
		String name = "name";
		LicenseType licenseType = licenseTypeDao.create(name);
		licenseTypeDao.save(licenseType);
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType);
		dbInstance.commitAndCloseSession();
		
		licenseTypeActivationDao.delete(LICENSE_HANDLER, licenseType);
		dbInstance.commitAndCloseSession();
	
		boolean isActive = licenseTypeActivationDao.isActive(LICENSE_HANDLER, licenseType);
		
		assertThat(isActive).isFalse();
	}

}
