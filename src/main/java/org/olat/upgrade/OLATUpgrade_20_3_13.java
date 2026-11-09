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
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 9 Nov 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_3_13 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_3_13.class);

	private static final String VERSION = "OLAT_20.3.13";

	private static final String DELETE_ORPHANED_PRIVATE_LIFECYCLES = "DELETE ORPHANED PRIVATE LIFECYCLES";

	@Autowired
	private DB dbInstance;

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

		boolean allOk = true;
		allOk &= deleteOrphanedPrivateLifecycles(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_3_13 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_3_13 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean deleteOrphanedPrivateLifecycles(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DELETE_ORPHANED_PRIVATE_LIFECYCLES)) {
			try {
				log.info("Delete orphaned private repository entry lifecycles");

				String query = "delete from repositoryentrylifecycle cycle"
						+ " where cycle.privateCycle=true"
						+ " and not exists (select re.key from repositoryentry re where re.lifecycle.key=cycle.key)";
				int deleted = dbInstance.getCurrentEntityManager().createQuery(query).executeUpdate();
				dbInstance.commitAndCloseSession();

				log.info("End delete orphaned private repository entry lifecycles: {}", deleted);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(DELETE_ORPHANED_PRIVATE_LIFECYCLES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
