/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 feb. 2022<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_16_2_0 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_16_2_0.class);
	
	private static final String VERSION = "OLAT_16.2.0";
	private static final String MIGRATE_HELP_PROVIDER = "MIGRATE HELP PROVIDER";
	
	@Autowired
	private HelpModule helpModule;

	
	public OLATUpgrade_16_2_0() {
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
		allOk &= migrateHelpProvider(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_16_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_16_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	/**
	 * OpenOlat 16.2 has a new manual and a new context help provider. All help
	 * references have been migrated and the old confluence provider is no longer
	 * supported
	 * 
	 * @param upgradeManager
	 * @param uhd
	 * @return
	 */
	private boolean migrateHelpProvider(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_HELP_PROVIDER)) {
			try {
				helpModule.migrateConfluenceToOODocs();
				

				log.info("OO Help provider migrated");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(MIGRATE_HELP_PROVIDER, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	

}
