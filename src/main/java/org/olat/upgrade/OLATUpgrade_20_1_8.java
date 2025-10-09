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
 * Initial date: 23 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_20_1_8 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_20_1_8.class);

	private static final String VERSION = "OLAT_20.1.8";
	private static final String MIGRATION_USER_CONFIRMABLE = "MIGRATION USER CONFIRMABLE";

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
		allOk &= migrateUserConfirmableFlag(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_20_1_8 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_20_1_8 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateUserConfirmableFlag(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATION_USER_CONFIRMABLE)) {
			try {
				log.info("Migration of user confirmable flag of reservation");
				
				String confirmedByParticipantQuery = "update resourcereservation set confirmableBy='PARTICIPANT' where confirmableBy is null and (userConfirmable is null or userConfirmable=true)";
				int count = dbInstance.getCurrentEntityManager().createQuery(confirmedByParticipantQuery)
						.executeUpdate();
				dbInstance.commit();
				
				String confirmedByManagersQuery = "update resourcereservation set confirmableBy='ADMINISTRATIVE_ROLE' where confirmableBy is null and userConfirmable=false";
				count += dbInstance.getCurrentEntityManager().createQuery(confirmedByManagersQuery)
						.executeUpdate();
				dbInstance.commitAndCloseSession();

				log.info("End migration of user confirmable flag of reservation: {} changed", count);
				allOk = true;
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATION_USER_CONFIRMABLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}
}
