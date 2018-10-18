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

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LicenseTypeDAOTest extends OlatTestCase {

	private static final LicenseHandler LICENSE_HANDLER = new TestableLicenseHandler();
	private static final LicenseHandler OTHER_LICENSE_HANDLER = new TestableLicenseHandler("other");

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
	public void shouldCreateLicenseType() {
		String name = "name";
		
		LicenseType licenseType = licenseTypeDao.create(name);

		assertThat(licenseType).isNotNull();
		assertThat(licenseType.getName()).isEqualTo(name);
		assertThat(licenseType.isPredefined()).isFalse();
		assertThat(licenseType.getSortOrder()).isNotNull();
		assertThat(licenseType.getCreationDate()).isNotNull();
		assertThat(licenseType.getLastModified()).isNotNull();
	}
	
	@Test
	public void shouldCreateWithHighestSortOrder() {
		LicenseType licenseType = licenseTypeDao.create("name");
		int sortOrder = 22;
		licenseType.setSortOrder(sortOrder);
		licenseTypeDao.save(licenseType);
		dbInstance.commitAndCloseSession();

		LicenseType licenseType2 = licenseTypeDao.create("other");
		LicenseType licenseType3 = licenseTypeDao.create("other2");
		licenseType2 = licenseTypeDao.save(licenseType2);
		licenseType3 = licenseTypeDao.save(licenseType3);
		dbInstance.commitAndCloseSession();
		
		assertThat(licenseType2.getSortOrder()).isEqualTo(sortOrder + 1);
		assertThat(licenseType3.getSortOrder()).isEqualTo(sortOrder + 2);
	}

	@Test
	public void shouldPersistLicenseType() {
		String name = "name";
		String text = "url";
		String cssClass = "cssClass";
		int sortOrder = 88;
		LicenseType licenseType = licenseTypeDao.create(name);
		licenseType.setText(text);
		licenseType.setCssClass(cssClass);
		licenseType.setSortOrder(sortOrder);
		
		// create
		licenseType = licenseTypeDao.save(licenseType);
		dbInstance.commitAndCloseSession();

		assertThat(licenseType).isNotNull();
		assertThat(licenseType.getName()).isEqualTo(name);
		assertThat(licenseType.isPredefined()).isFalse();
		assertThat(licenseType.getText()).isEqualTo(text);
		assertThat(licenseType.getCssClass()).isEqualTo(cssClass);
		assertThat(licenseType.getSortOrder()).isEqualTo(sortOrder);
		assertThat(licenseType.getCreationDate()).isNotNull();
		assertThat(licenseType.getLastModified()).isNotNull();
		
		// small delay to be sure the modification time actually differs from the creation date
		sleep(100);

		// update
		licenseType = licenseTypeDao.save(licenseType);
		dbInstance.commitAndCloseSession();
		
		assertThat(licenseType).isNotNull();
		assertThat(licenseType.getName()).isEqualTo(name);
		assertThat(licenseType.isPredefined()).isFalse();
		assertThat(licenseType.getText()).isEqualTo(text);
		assertThat(licenseType.getCssClass()).isEqualTo(cssClass);
		assertThat(licenseType.getSortOrder()).isEqualTo(sortOrder);
		assertThat(licenseType.getCreationDate()).isNotNull();
		assertThat(licenseType.getLastModified()).isNotNull();
		assertThat(licenseType.getLastModified()).isNotEqualTo(licenseType.getCreationDate());
	}

	@Test
	public void shouldLoadNoLicenseType() {
		LicenseType noLicenseType = licenseTypeDao.loadNoLicenseType();
		
		assertThat(noLicenseType).isNotNull();
		assertThat(noLicenseType.getName()).isEqualTo(LicenseTypeDAO.NO_LICENSE_NAME);
	}

	@Test
	public void shouldLoadLicenseTypeByKey() {
		LicenseType licenseType = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType = licenseTypeDao.save(licenseType);
		LicenseType otherLicenseType = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseTypeDao.save(otherLicenseType);
		dbInstance.commitAndCloseSession();
		
		LicenseType loadedLicenseType = licenseTypeDao.loadLicenseTypeByKey(licenseType.getKey());
		
		assertThat(loadedLicenseType).isEqualTo(licenseType);
	}
	
	@Test
	public void shouldLoadLicenseTypeByName() {
		LicenseType licenseType = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType = licenseTypeDao.save(licenseType);
		LicenseType otherLicenseType = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseTypeDao.save(otherLicenseType);
		dbInstance.commitAndCloseSession();
		
		LicenseType loadedLicenseType = licenseTypeDao.loadLicenseTypeByName(licenseType.getName());
		
		assertThat(loadedLicenseType).isEqualTo(licenseType);
	}


	@Test
	public void shouldLoadAllLicensesTypes() {
		LicenseType licenseType1 = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType1 = licenseTypeDao.save(licenseType1);
		LicenseType licenseType2 = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType2 = licenseTypeDao.save(licenseType2);
		LicenseType licenseType3 = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType3 = licenseTypeDao.save(licenseType3);
		dbInstance.commitAndCloseSession();

		List<LicenseType> licenseType = licenseTypeDao.loadLicenseTypes();
		
		assertThat(licenseType).contains(licenseType1, licenseType2, licenseType3);
	}

	@Test
	public void shouldLoadActivatedLicenseTypes() {
		LicenseType licenseType1 = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType1 = licenseTypeDao.save(licenseType1);
		LicenseType licenseType2 = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType2 = licenseTypeDao.save(licenseType2);
		LicenseType licenseType3 = licenseTypeDao.create(UUID.randomUUID().toString());
		licenseType3 = licenseTypeDao.save(licenseType3);
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType1);
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType2);
		licenseTypeActivationDao.delete(LICENSE_HANDLER, licenseType2);
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType2);
		licenseTypeActivationDao.createAndPersist(LICENSE_HANDLER, licenseType3);
		licenseTypeActivationDao.createAndPersist(OTHER_LICENSE_HANDLER, licenseType3);
		dbInstance.commitAndCloseSession();
		
		List<LicenseType> activatedLicenseType = licenseTypeDao.loadActiveLicenseTypes(LICENSE_HANDLER);

		assertThat(activatedLicenseType).hasSize(3).contains(licenseType1, licenseType2, licenseType3);
	}

	@Test
	public void shouldCheckIfExists() {
		String name = UUID.randomUUID().toString();
		boolean exists = licenseTypeDao.exists(name);
		assertThat(exists).isFalse();
		
		LicenseType licenseType1 = licenseTypeDao.create(name);
		licenseType1 = licenseTypeDao.save(licenseType1);
		dbInstance.commitAndCloseSession();
		exists = licenseTypeDao.exists(name);
		assertThat(exists).isTrue();
	}

}
