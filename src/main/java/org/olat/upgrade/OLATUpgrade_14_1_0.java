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
import org.olat.course.nodes.ms.MSService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_14_1_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_14_1_0.class);
	
	private static final String VERSION = "OLAT_14.1.0";
	private static final String SET_MS_ORES_TYPE_NAME = "SET MS ORES TYPE NAME";
	
	@Autowired
	private DB dbInstance;
	
	public OLATUpgrade_14_1_0() {
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
		allOk &= migrateMsOresTypeName(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_14_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_14_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	
	private boolean migrateMsOresTypeName(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(SET_MS_ORES_TYPE_NAME)) {
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("update evaluationformsurvey as survey");
				sb.append("   set survey.resName='").append(MSService.SURVEY_ORES_TYPE_NAME).append("'");
				sb.append(" where survey.resSubident2 is not null");
				
				dbInstance.getCurrentEntityManager()
						.createQuery(sb.toString())
						.executeUpdate();
				dbInstance.commitAndCloseSession();
				log.info("Survey resName of ms course nodes migrated.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(SET_MS_ORES_TYPE_NAME, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
