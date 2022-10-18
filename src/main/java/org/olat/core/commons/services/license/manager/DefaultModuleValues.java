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

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.model.LicenseTypeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 21.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class DefaultModuleValues {
	
	@Autowired
	private LicenseTypeDAO licenseTypeDao;
	@Autowired
	private LicenseTypeActivationDAO licenseTypeActivationDao;
	
	@PostConstruct
	void initPredefinedLicenseTypes() {
		createAndPersistPredefined(LicenseTypeDAO.NO_LICENSE_NAME, "o_icon_lic_no_license", null);
		createAndPersistPredefined(LicenseTypeDAO.FREETEXT_NAME, "o_icon_lic_freetext", null);
		createAndPersistPredefined("public domain", "o_icon_lic_public_domain", "https://creativecommons.org/share-your-work/public-domain/pdm/");
		createAndPersistPredefined("CC0", "o_icon_lic_cc0", "https://creativecommons.org/share-your-work/public-domain/cc0/");
		createAndPersistPredefined("CC BY", "o_icon_lic_by", "https://creativecommons.org/licenses/by/4.0/");
		createAndPersistPredefined("CC BY-SA", "o_icon_lic_by_sa", "https://creativecommons.org/licenses/by-sa/4.0/");
		createAndPersistPredefined("CC BY-ND", "o_icon_lic_by_nd", "https://creativecommons.org/licenses/by-nd/4.0/");
		createAndPersistPredefined("CC BY-NC", "o_icon_lic_by_nc", "https://creativecommons.org/licenses/by-nc/4.0/");
		createAndPersistPredefined("CC BY-NC-SA", "o_icon_lic_by_nc_sa", "https://creativecommons.org/licenses/by-nc-sa/4.0/");
		createAndPersistPredefined("CC BY-NC-ND", "o_icon_lic_by_nc_nd", "https://creativecommons.org/licenses/by-nc-nd/4.0/");
		createAndPersistPredefined("all rights reserved", "o_icon_lic_all_rights_reserved", null);
		createAndPersistPredefined("youtube", "o_icon_lic_youtube", "https://www.youtube.com/static?template=terms");
	}
	
	private void createAndPersistPredefined(String name, String cssClass, String text) {
		if (!licenseTypeDao.exists(name)) {
			LicenseTypeImpl licenseType = (LicenseTypeImpl) licenseTypeDao.create(name);
			licenseType.setCssClass(cssClass);
			licenseType.setText(text);
			licenseType.setPredefined(true);
			licenseTypeDao.save(licenseType);
		}
	}
	
	public Boolean isEnabled() {
		return Boolean.TRUE;
	}
	
	public void activateLicenseTypes(LicenseHandler handler) {
		List<LicenseType> licenseTypes = licenseTypeDao.loadPredefinedLicenseTypes();
		for (LicenseType licenseType: licenseTypes) {
			if (!licenseTypeActivationDao.isActive(handler, licenseType)) {
				licenseTypeActivationDao.createAndPersist(handler, licenseType);
			}
		}
	}

	public LicenseType getLicenseType() {
		return licenseTypeDao.loadLicenseTypeByName(LicenseTypeDAO.NO_LICENSE_NAME);
	}
	
}
