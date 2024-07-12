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
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Jul 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_0_1 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_0_1.class);
	
	private static final String VERSION = "OLAT_19.0.1";

	private static final String UPDATE_FOLDER_USER_TOOL = "UPDATED FOLDER USER TOOL";
	
 	@Autowired
 	private UserToolsModule userToolsModule;

	public OLATUpgrade_19_0_1() {
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
		allOk &= updateFolderUserTool(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_0_1 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_0_1 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateFolderUserTool(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_FOLDER_USER_TOOL)) {
			try {
				String availableTools = userToolsModule.getAvailableUserTools();
				if(!"none".equals(availableTools) && StringHelper.containsNonWhitespace(availableTools)
						&& availableTools.contains("FolderRunController")
						&& !availableTools.contains("PersonalFileHubController")) {
					availableTools += ",org.olat.home.HomeMainController:org.olat.home.PersonalFileHubController";
					userToolsModule.setAvailableUserTools(availableTools);
				}
				String presetTools = userToolsModule.getDefaultPresetOfUserTools();
				if(!"none".equals(presetTools) && StringHelper.containsNonWhitespace(presetTools)
						&& presetTools.contains("FolderRunController")
						&& !presetTools.contains("PersonalFileHubController")) {
					presetTools += ",org.olat.home.HomeMainController:org.olat.home.PersonalFileHubController";
					userToolsModule.setDefaultPresetOfUserTools(presetTools);
				}
				log.info("Update folder user tool.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
			
			uhd.setBooleanDataValue(UPDATE_FOLDER_USER_TOOL, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
