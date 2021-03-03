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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Mar 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_3_12 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_3_12.class);

	private static final String VERSION = "OLAT_15.3.12";
	private static final String DELETE_APPOINTMENT_USER_RESTRICTIONS = "DELETE APPOINTMENT USER RESTRICTIONS";

	@Autowired
	private DB dbInstance;
	@Autowired
	private OnlyOfficeModule onlyofficeModule;

	public OLATUpgrade_15_3_12() {
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
		allOk &= deleteAppointmentUserRestriction(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_3_12 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_3_12 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean deleteAppointmentUserRestriction(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DELETE_APPOINTMENT_USER_RESTRICTIONS)) {
			try {
				String query = "delete from appointmenttopictogroup ttg where ttg.group is null";
				dbInstance.getCurrentEntityManager()
						.createQuery(query)
						.executeUpdate();
				dbInstance.commitAndCloseSession();
				log.info("Deleted ppointment restriction with no group.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(DELETE_APPOINTMENT_USER_RESTRICTIONS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
