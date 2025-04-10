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
import org.olat.core.logging.Tracing;
import org.olat.repository.LifecycleModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Apr 10, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_20_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_1_0.class);

	private static final String VERSION = "OLAT_20.1.0";
	private static final String MIGRATE_LIFECYCLE_ENABLED = "MIGRATE_LIFECYCLE_ENABLED";

	@Autowired
	private DB dbInstance;
	@Autowired
	private LifecycleModule lifecycleModule;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk;
		allOk = migrateLifecycleModuleSetting(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateLifecycleModuleSetting(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(MIGRATE_LIFECYCLE_ENABLED)) {
			try {
				boolean exists = !dbInstance.getCurrentEntityManager()
						.createQuery("select rel.key from repositoryentrylifecycle rel", Long.class)
						.setMaxResults(1)
						.getResultList().isEmpty();

				lifecycleModule.setEnabled(exists);
				log.info("Set lifecycle module enabled = {}", exists);

				uhd.setBooleanDataValue(MIGRATE_LIFECYCLE_ENABLED, Boolean.TRUE);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
			} catch (Exception e) {
				log.error("Error migrating lifecycle.enabled setting", e);
				return false;
			}
		}
		return true;
	}
}

