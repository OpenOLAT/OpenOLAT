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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseHandler;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.model.LicenseImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * Initial date: 19.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class LicenseServiceImpl implements LicenseService {

	private static final Logger log = Tracing.createLoggerFor(LicenseServiceImpl.class);

	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private ResourceLicenseDAO licenseDao;
	@Autowired
	private LicenseTypeDAO licenseTypeDao;
	@Autowired
	private LicenseTypeActivationDAO licenseTypeActivationDao;
	@Autowired
	private LicensorFactory licensorFactory;
	@Autowired
	private LicenseXStreamHelper xStreamHelper;

	@Override
	public License createLicense(LicenseType licenseType) {
		License license = new LicenseImpl();
		license.setLicenseType(licenseType);
		return license; 
		
	}

	@Override
	public License createDefaultLicense(LicenseHandler handler, Identity licensor) {
		LicenseType defautlLicenseType = getDefaultLicenseType(handler);
		String licensorName = licensorFactory.create(handler, licensor);
		License license = new LicenseImpl();
		license.setLicenseType(defautlLicenseType);
		license.setLicensor(licensorName);
		return license;
	}

	@Override
	public String toXml(License license) {
		return xStreamHelper.toXml(license);
	}

	@Override
	public License licenseFromXml(String xml) {
		return xStreamHelper.licenseFromXml(xml);
	}
	
	@Override
	public ResourceLicense createDefaultLicense(OLATResourceable ores, LicenseHandler handler, Identity licensor) {
		LicenseType defautlLicenseType = getDefaultLicenseType(handler);
		String licensorName = licensorFactory.create(handler, licensor);
		return licenseDao.createAndPersist(ores, defautlLicenseType, licensorName);
	}

	private LicenseType getDefaultLicenseType(LicenseHandler handler) {
		String defaultLicenseTypeKey = licenseModule.getDefaultLicenseTypeKey(handler);
		LicenseType defautlLicenseType = loadLicenseTypeByKey(defaultLicenseTypeKey);
		if (defautlLicenseType == null) {
			defautlLicenseType = licenseTypeDao.loadNoLicenseType();
		}
		return defautlLicenseType;
	}
	
	@Override
	public ResourceLicense loadOrCreateLicense(OLATResourceable ores) {
		ResourceLicense license = licenseDao.loadByResource(ores);
		if (license == null) {
			LicenseType licenseType = licenseTypeDao.loadNoLicenseType();
			license = licenseDao.createAndPersist(ores, licenseType);
		}
		return license;
	}

	@Override
	public ResourceLicense update(ResourceLicense license) {
		return licenseDao.save(license);
	}

	@Override
	public ResourceLicense loadLicense(OLATResourceable resource) {
		return licenseDao.loadByResource(resource);
	}

	@Override
	public List<ResourceLicense> loadLicenses(Collection<? extends OLATResourceable> resources) {
		return licenseDao.loadLicenses(resources);
	}

	@Override
	public ResourceLicense copy(OLATResourceable source, OLATResourceable target) {
		if (source == null || target == null) return null;
		
		ResourceLicense targetLicense = null;
		ResourceLicense sourceLicense = licenseDao.loadByResource(source);
		if (sourceLicense != null) {
			licenseDao.delete(target);
			targetLicense = licenseDao.createAndPersist(target, sourceLicense);
		}
		return targetLicense;
	}

	@Override
	public void delete(OLATResourceable resource) {
		licenseDao.delete(resource);
	}

	@Override
	public boolean licenseTypeExists(String name) {
		return licenseTypeDao.exists(name);
	}
	
	@Override
	public LicenseType createLicenseType(String name) {
		LicenseType licenseType = licenseTypeDao.create(name);
		log.info("License type created: " + licenseType);
		return licenseType;
	}

	@Override
	public LicenseType saveLicenseType(LicenseType license) {
		return licenseTypeDao.save(license);
	}
	
	@Override
	public LicenseType loadLicenseTypeByKey(String licenseTypeKey) {
		Long key = null;
		try {
			key = Long.parseLong(licenseTypeKey);
		} catch (Exception e) {
			// bad luck
		}
		return licenseTypeDao.loadLicenseTypeByKey(key);
	}
	
	@Override
	public List<LicenseType> loadLicensesTypesByKeys(List<String> keys) {
		if (keys == null || keys.isEmpty()) return new ArrayList<>();
		
		List<Long> keysAsLong = keys.stream()
				.filter(StringHelper::isLong)
				.map(Long::parseLong)
				.toList();
		return licenseTypeDao.loadLicensesTypesByKeys(keysAsLong);
	}
	
	@Override
	public LicenseType loadLicenseTypeByName(String name) {
		return licenseTypeDao.loadLicenseTypeByName(name);
	}

	@Override
	public List<LicenseType> loadLicenseTypes() {
		return licenseTypeDao.loadLicenseTypes();
	}

	@Override
	public List<LicenseType> loadActiveLicenseTypes(LicenseHandler handler) {
		return licenseTypeDao.loadActiveLicenseTypes(handler);
	}

	@Override
	public LicenseType loadFreetextLicenseType() {
		return licenseTypeDao.loadLicenseTypeByName(LicenseTypeDAO.FREETEXT_NAME);
	}
	
	@Override
	public boolean isNoLicense(LicenseType licenseType) {
		return licenseTypeDao.isNoLicense(licenseType);
	}

	@Override
	public boolean isFreetext(LicenseType licenseType) {
		return licenseTypeDao.isFreetext(licenseType);
	}

	@Override
	public boolean isActive(LicenseHandler handler, LicenseType licenseType) {
		return licenseTypeActivationDao.isActive(handler, licenseType);
	}

	@Override
	public void activate(LicenseHandler handler, LicenseType licenseType) {
		boolean active = isActive(handler, licenseType);
		if (!active) {
			licenseTypeActivationDao.createAndPersist(handler, licenseType);
		}
	}

	@Override
	public void deactivate(LicenseHandler handler, LicenseType licenseType) {
		licenseTypeActivationDao.delete(handler, licenseType);
	}
}
