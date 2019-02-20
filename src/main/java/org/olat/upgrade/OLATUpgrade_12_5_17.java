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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.course.statistic.StatisticUpdateManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_5_17 extends OLATUpgrade {
	

	private static final String VERSION = "OLAT_12.5.17";
	private static final String UPDATE_COURSE_STATS = "UPDATE COURSE STATS";
	
	@Autowired
	private DB dbInstance;
	
	public OLATUpgrade_12_5_17() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
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
		allOk &= updateCourseStatistics(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_12_5_17 successfully!");
		} else {
			log.audit("OLATUpgrade_12_5_17 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	private boolean updateCourseStatistics(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_COURSE_STATS)) {
			if(dbInstance.isPostgreSQL()) {
				StatisticUpdateManager statisticManager = (StatisticUpdateManager) CoreSpringFactory.getBean("org.olat.course.statistic.StatisticUpdateManager");
				if(statisticManager != null) {
					statisticManager.updateStatistics(true, new Runnable() {
						@Override
						public void run() {
							//
						}
					});
				}
			}
			uhd.setBooleanDataValue(UPDATE_COURSE_STATS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
