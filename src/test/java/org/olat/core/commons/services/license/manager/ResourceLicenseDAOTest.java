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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 22.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ResourceLicenseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ResourceLicenseDAO licenseDao;
	@Autowired
	private LicenseTypeDAO licenseTypeDao;
	@Autowired
	private LicenseCleaner licenseCleaner;
	
	@Before
	public void cleanUp() {
		licenseCleaner.deleteAll();
	}
	
	@Test
	public void shouldCreateAndPersistLicense() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		String licensor = "licensor";

		ResourceLicense license = licenseDao.createAndPersist(ores, licenseType, licensor);
		dbInstance.commitAndCloseSession();
		
		assertThat(license.getResName()).isEqualTo(ores.getResourceableTypeName());
		assertThat(license.getResId()).isEqualTo(ores.getResourceableId());
		assertThat(license.getLicenseType()).isEqualTo(licenseType);
		assertThat(license.getLicensor()).isEqualTo(licensor);
		assertThat(license.getCreationDate()).isNotNull();
		assertThat(license.getLastModified()).isNotNull();
	}
	
	@Test
	public void shouldCreateAndPersistRawLicense() {
		OLATResourceable oresTarget = JunitTestHelper.createRandomResource();
		OLATResourceable oresSource = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		String licensor = "licensor";
		String freetext = "freetext";
		ResourceLicense sourceLicense = licenseDao.createAndPersist(oresSource, licenseType, licensor);
		sourceLicense.setFreetext(freetext);
		sourceLicense = licenseDao.save(sourceLicense);
		dbInstance.commitAndCloseSession();
		
		ResourceLicense targetLicense = licenseDao.createAndPersist(oresTarget, sourceLicense);
		
		assertThat(targetLicense.getResName()).isEqualTo(oresTarget.getResourceableTypeName());
		assertThat(targetLicense.getResId()).isEqualTo(oresTarget.getResourceableId());
		assertThat(targetLicense.getLicenseType()).isEqualTo(sourceLicense.getLicenseType());
		assertThat(targetLicense.getLicensor()).isEqualTo(sourceLicense.getLicensor());
		assertThat(targetLicense.getFreetext()).isEqualTo(sourceLicense.getFreetext());
		assertThat(targetLicense.getCreationDate()).isNotNull();
		assertThat(targetLicense.getLastModified()).isNotNull();
	}

	@Test
	public void shouldUpdateLicense() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		ResourceLicense license = licenseDao.createAndPersist(ores, licenseType);
		dbInstance.commitAndCloseSession();
		String freetext = "freetext";
		license.setFreetext(freetext);
		String licensor = "licensor";
		license.setLicensor(licensor);
		
		license = licenseDao.save(license);
		
		assertThat(license.getFreetext()).isEqualTo(freetext);
		assertThat(license.getLicensor()).isEqualTo(licensor);
		assertThat(license.getLicenseType()).isEqualTo(licenseType);
	}
	
	@Test
	public void shouldLoadLicenseForResource() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		License license = licenseDao.createAndPersist(ores, licenseType);
		dbInstance.commitAndCloseSession();
		
		ResourceLicense loadedLicense = licenseDao.loadByResource(ores);
		
		assertThat(loadedLicense).isEqualTo(license);
	}
	
	@Test
	public void shouldLoadLicenseForResources() {
		String resName = "res";
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		OLATResourceable ores1 = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		ResourceLicense license1 = licenseDao.createAndPersist(ores1, licenseType);
		OLATResourceable ores2 = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		ResourceLicense license2 = licenseDao.createAndPersist(ores2, licenseType);
		OLATResourceable ores3 = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		ResourceLicense license3 = licenseDao.createAndPersist(ores3, licenseType);
		OLATResourceable oresSameName = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		licenseDao.createAndPersist(oresSameName, licenseType);
		OLATResourceable oresSameId = OresHelper.createOLATResourceableInstance("other", ores1.getResourceableId());
		licenseDao.createAndPersist(oresSameId, licenseType);
		dbInstance.commitAndCloseSession();
		
		List<ResourceLicense> loadedLicenses = licenseDao.loadLicenses(Arrays.asList(ores1, ores2, ores3));
		
		assertThat(loadedLicenses).containsExactlyInAnyOrder(license1, license2, license3);
	}
	
	@Test
	public void shoulDeleteLicenseOfResource() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		licenseDao.createAndPersist(ores, licenseType);
		dbInstance.commitAndCloseSession();
		
		ResourceLicense loadedLicense = licenseDao.loadByResource(ores);
		assertThat(loadedLicense).isNotNull();
		
		licenseDao.delete(ores);
		dbInstance.commitAndCloseSession();
		
		loadedLicense = licenseDao.loadByResource(ores);
		assertThat(loadedLicense).isNull();
	}



}
