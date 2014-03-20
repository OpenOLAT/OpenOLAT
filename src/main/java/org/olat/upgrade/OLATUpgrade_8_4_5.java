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


/**
 * Description:<br>
 * upgrade code for OpenOLAT 8.3.x -> OpenOLAT 8.4.0
 * - Upgrade bookmarks to new database structure
 * - Recalculate small user avatar images to new size 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_8_4_5 extends OLATUpgrade {

	private static final String TASK_COURSE_TO_GROUP_PERMISSIONS = "Upgrade course to group permissions";
	private static final String VERSION = "OLAT_8.4.5";

	public OLATUpgrade_8_4_5() {
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
		} else {
			if (uhd.isInstallationComplete()) {
				return false;
			}
		}
		
		boolean allOk = upgradeCourseToGroup(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_8_4_5 successfully!");
		} else {
			log.audit("OLATUpgrade_8_4_5 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradeCourseToGroup(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_COURSE_TO_GROUP_PERMISSIONS)) {
			//do nothing
			uhd.setBooleanDataValue(TASK_COURSE_TO_GROUP_PERMISSIONS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
}