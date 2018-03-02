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
public class LicenseDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LicenseDAO licenseDao;
	@Autowired
	private LicenseTypeDAO licenseTypeDao;
	
	@Before
	public void cleanUp() {
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from license")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from licensetypeactivation")
				.executeUpdate();
		dbInstance.getCurrentEntityManager()
				.createQuery("delete from licensetype")
				.executeUpdate();
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void shouldCreateAndPersistLicense() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		String licensor = "licensor";

		License license = licenseDao.createAndPersist(ores, licenseType, licensor);
		dbInstance.commitAndCloseSession();
		
		assertThat(license.getResName()).isEqualTo(ores.getResourceableTypeName());
		assertThat(license.getResId()).isEqualTo(ores.getResourceableId());
		assertThat(license.getLicenseType()).isEqualTo(licenseType);
		assertThat(license.getLicensor()).isEqualTo(licensor);
		assertThat(license.getCreationDate()).isNotNull();
		assertThat(license.getLastModified()).isNotNull();
	}

	@Test
	public void shouldUpdateLicense() {
		OLATResourceable ores = JunitTestHelper.createRandomResource();
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		License license = licenseDao.createAndPersist(ores, licenseType);
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
		
		License loadedLicense = licenseDao.loadByResource(ores);
		
		assertThat(loadedLicense).isEqualTo(license);
	}
	
	@Test
	public void shouldLoadLicenseForResources() {
		String resName = "res";
		LicenseType licenseType = licenseTypeDao.create("name");
		licenseType = licenseTypeDao.save(licenseType);
		OLATResourceable ores1 = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		License license1 = licenseDao.createAndPersist(ores1, licenseType);
		OLATResourceable ores2 = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		License license2 = licenseDao.createAndPersist(ores2, licenseType);
		OLATResourceable ores3 = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		License license3 = licenseDao.createAndPersist(ores3, licenseType);
		OLATResourceable oresSameName = OresHelper.createOLATResourceableInstance(resName, (new Random()).nextLong());
		licenseDao.createAndPersist(oresSameName, licenseType);
		OLATResourceable oresSameId = OresHelper.createOLATResourceableInstance("other", ores1.getResourceableId());
		licenseDao.createAndPersist(oresSameId, licenseType);
		dbInstance.commitAndCloseSession();
		
		List<License> loadedLicenses = licenseDao.loadLicenses(Arrays.asList(ores1, ores2, ores3));
		
		assertThat(loadedLicenses).containsExactly(license1, license2, license3);
	}


}
