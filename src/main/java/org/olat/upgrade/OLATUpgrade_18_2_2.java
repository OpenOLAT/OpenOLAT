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
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_18_2_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_2_2.class);

	private static final String VERSION = "OLAT_18.2.2";

	private static final String DELETE_PROJECT_HIBERNATE_PROXY_DATA = "DELETE PROJECT HIBERNATE PROXY DATA";
	
	@Autowired
	private DB dbInstance;

	public OLATUpgrade_18_2_2() {
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
		allOk &= deleteProjActivityLogHibernateProxyData(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_2_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_2_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean deleteProjActivityLogHibernateProxyData(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(DELETE_PROJECT_HIBERNATE_PROXY_DATA)) {
			String query = """
					delete from projactivity as activity
					where activity.before like '%ibernate%'
					   or activity.after like '%ibernate%'""";
			dbInstance.getCurrentEntityManager()
					.createQuery(query)
					.executeUpdate();
			log.info(Tracing.M_AUDIT, "Project activity log with hibarnate proxy data deleted.");
			
			uhd.setBooleanDataValue(DELETE_PROJECT_HIBERNATE_PROXY_DATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
}
