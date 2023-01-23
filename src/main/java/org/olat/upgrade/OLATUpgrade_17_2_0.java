/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_17_2_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_2_0.class);

	private static final String VERSION = "OLAT_17.2.0";
	private static final String OER_INACTIVATION = "OER INACTIVATION";

	@Autowired
	private DB dbInstance;
	@Autowired
	private LicenseService licenseService;


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

		allOk &= setOerLicenseFlag(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean setOerLicenseFlag(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(OER_INACTIVATION)) {
			try {
				log.info("Start setting OER License Flag.");
				List<LicenseType> oerLicenseTypes = getOerLicenseTypes();
				for (LicenseType licenseType : oerLicenseTypes) {
					licenseType.setOerLicense(true);
					licenseService.saveLicenseType(licenseType);
				}

				dbInstance.commitAndCloseSession();
				log.info("All relevant licenses updated: {}", oerLicenseTypes.size());
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(OER_INACTIVATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<LicenseType> getOerLicenseTypes() {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select lt from licensetype lt where lt.name like 'CC%' OR lt.name like 'public domain'");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LicenseType.class).getResultList();

	}
}
