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
package org.olat.upgrade;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.03.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_4_0 extends OLATUpgrade {

	private static final int BATCH_SIZE = 500;
	
	private static final String VERSION = "OLAT_12.4.0";
	private static final String MIGRATE_QPOOL_LICENSE = "MIGRATE QPOOL LICENSE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	
	public OLATUpgrade_12_4_0() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= migrateQpoolLicenses(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_12_4_0 successfully!");
		} else {
			log.audit("OLATUpgrade_12_4_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	
	private boolean migrateQpoolLicenses(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_QPOOL_LICENSE)) {
			try {
				licenseModule.setEnabled(licenseHandler.getType(), true);
				
				migrateQPoolLicenseTypes();
				dbInstance.commitAndCloseSession();
				
				migrateQpoolItemLicenses();
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_QPOOL_LICENSE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateQPoolLicenseTypes() {
		List<QLicense> qpoolLicenseTypes = getQpoolLicenseTypes();
		for (QLicense qlicense: qpoolLicenseTypes) {
			String licenseKey = qlicense.getLicenseKey();
			if (StringHelper.containsNonWhitespace(licenseKey) && isNotFreetextLicense(licenseKey)) {
				String licenseTypeName = mapLicenseTypeName(licenseKey);
				if (isLicenseTypeMissing(licenseTypeName)) {
					LicenseType licenseType = licenseService.createLicenseType(licenseTypeName);
					licenseService.saveLicenseType(licenseType);
					log.info("LicenseType created: " + licenseTypeName);
				} else {
					log.info("LicenseType not created (exists): " + licenseTypeName);
				}
				LicenseType licenseType = licenseService.loadLicenseTypeByName(licenseTypeName);
				licenseService.activate(licenseHandler, licenseType);
			}
		}
	}

	public boolean isLicenseTypeMissing(String licenseTypeName) {
		return !licenseService.licenseTypeExists(licenseTypeName);
	}
	
	private boolean isNotFreetextLicense(String licenseKey) {
		return !licenseKey.contains("perso");
	}

	private String mapLicenseTypeName(String licenseKey) {
		switch (licenseKey) {
			case "CC by": return "CC BY";
			case "CC by-sa": return "CC BY-SA";
			case "CC by-nd": return "CC BY-ND";
			case "CC by-nc": return "CC BY-NC";
			case "CC by-nc-sa": return "CC BY-NC-SA";
			case "CC by-nc-nd": return "CC BY-NC-ND";
			case "all rights reserved": return "all rights reserved";
			default: return licenseKey;
		}
	}

	private List<QLicense> getQpoolLicenseTypes()  {
		String query = "select item from qlicense item order by key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, QLicense.class)
				.getResultList();
	}

	private void migrateQpoolItemLicenses() {
		int counter = 0;
		List<QuestionItemImpl> items;
		
		do {
			items = getQuestionItems(counter, BATCH_SIZE);
			for (QuestionItemImpl item: items) {
				try {
					migrateQpoolLicense(item);
					log.info("QPool item license successfully migrated: " + item);
				} catch (Exception e) {
					log.error("Not able to migrate question item license: " + item, e);
				}
			}
			counter += items.size();
			dbInstance.commitAndCloseSession();
			log.info(counter + " QPool items processed.");
		} while(items.size() == BATCH_SIZE);
	}

	private void migrateQpoolLicense(QuestionItemImpl item) {
		QLicense license = item.getLicense();
		if (license != null) {
			String licenseKey = license.getLicenseKey();
			if (isNotFreetextLicense(licenseKey)) {
				saveLicense(item);
			} else {
				saveFreetextLicense(item);
			}
		}
	}

	private void saveLicense(QuestionItemImpl item) {
		QLicense qlicense = item.getLicense();
		String name = mapLicenseTypeName(qlicense.getLicenseKey());
		LicenseType licenseType = licenseService.loadLicenseTypeByName(name);
		ResourceLicense license = licenseService.loadOrCreateLicense(item);
		license.setLicenseType(licenseType);
		license.setFreetext(qlicense.getLicenseText());
		license.setLicensor(item.getCreator());
		licenseService.update(license);
	}

	private void saveFreetextLicense(QuestionItemImpl item) {
		QLicense qlicense = item.getLicense();
		ResourceLicense license = licenseService.loadOrCreateLicense(item);
		license.setLicenseType(getFreetextLicenseType());
		license.setFreetext(qlicense.getLicenseText());
		license.setLicensor(item.getCreator());
		licenseService.update(license);
	}
	
	private LicenseType getFreetextLicenseType() {
		List<LicenseType> licenseTypes = licenseService.loadLicenseTypes();
		for (LicenseType licenseType: licenseTypes) {
			if (licenseService.isFreetext(licenseType)) {
				return licenseType;
			}
		}
		return null;
	}

	private List<QuestionItemImpl> getQuestionItems(int firstResults, int maxResult)  {
		String query = "select item from questionitem item order by key";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, QuestionItemImpl.class)
				.setFirstResult(firstResults)
				.setMaxResults(maxResult)
				.getResultList();
	}

}
