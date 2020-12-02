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
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_4_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_4_0.class);

	private static final String VERSION = "OLAT_15.4.0";
	private static final String INIT_VFS_MODIFIED_BY = "INIT VFS MODIFIED BY";
	
	@Autowired
	private DB dbInstance;

	public OLATUpgrade_15_4_0() {
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
		allOk &= initVfsModifiedBy(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_4_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_4_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initVfsModifiedBy(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_VFS_MODIFIED_BY)) {
			
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("update filemetadata meta");
				sb.append("   set meta.fileLastModifiedBy = meta.author");
				sb.append(" where meta.author is not null");
				dbInstance.getCurrentEntityManager()
						.createQuery(sb.toString())
						.executeUpdate();
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				dbInstance.rollbackAndCloseSession();
				log.error("", e);
				return false;
			}
			
			uhd.setBooleanDataValue(INIT_VFS_MODIFIED_BY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
