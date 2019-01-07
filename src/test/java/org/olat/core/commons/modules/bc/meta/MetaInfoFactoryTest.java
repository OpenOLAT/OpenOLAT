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
package org.olat.core.commons.modules.bc.meta;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.manager.LicenseCleaner;
import org.olat.core.util.vfs.meta.MetaInfo;
import org.olat.core.util.vfs.meta.MetaInfoFactory;
import org.olat.core.util.vfs.meta.MetaInfoFileImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MetaInfoFactoryTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MetaInfoFactory metaInfoFactory;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseCleaner licenseCleaner;
	
	@Before
	public void cleanUp() {
		licenseCleaner.deleteAll();
	}
	
	@Test
	public void shouldLoadExistingLicenseType() {
		String typeName = "name";
		LicenseType licenseType = licenseService.createLicenseType(typeName);
		licenseType = licenseService.saveLicenseType(licenseType);
		dbInstance.commitAndCloseSession();
		String licensor = "licensor";
		String name = licenseType.getName();
		File file = new File("");
		MetaInfo meta = new MetaInfoFileImpl(file);
		meta.setLicenseTypeName(name);
		meta.setLicensor(licensor);
		
		License license = metaInfoFactory.getLicense(meta);

		assertThat(license.getLicensor()).isEqualTo(licensor);
		LicenseType loadedLicenseType = license.getLicenseType();
		assertThat(loadedLicenseType).isEqualTo(licenseType);
	}

	@Test
	public void shouldCreateNonExistingLicenseType() {
		String typeName = "name";
		LicenseType licenseType = licenseService.createLicenseType(typeName);
		licenseType = licenseService.saveLicenseType(licenseType);
		dbInstance.commitAndCloseSession();
		String licensor = "licensor";
		String name = "new";
		String text = "text";
		File file = new File("");
		MetaInfo meta = new MetaInfoFileImpl(file);
		meta.setLicenseTypeName(name);
		meta.setLicensor(licensor);
		meta.setLicenseText(text);
		
		License license = metaInfoFactory.getLicense(meta);

		assertThat(license.getLicensor()).isEqualTo(licensor);
		LicenseType loadedLicenseType = license.getLicenseType();
		assertThat(loadedLicenseType.getName()).isEqualTo(name);
		assertThat(loadedLicenseType.getText()).isEqualTo(text);
		
		LicenseType createdLicenseType = licenseService.loadLicenseTypeByName(name);
		assertThat(createdLicenseType).isNotNull();
	}

}
