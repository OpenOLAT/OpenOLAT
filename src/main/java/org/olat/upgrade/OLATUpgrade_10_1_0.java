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

import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_10_1_0 extends OLATUpgrade {
	
	private static final String TASK_USER_TOOLS = "Upgrade user tools";
	private static final String VERSION = "OLAT_10.1.0";

	@Autowired
	private UserToolsModule userToolsModule;

	
	public OLATUpgrade_10_1_0() {
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
		allOk &= upgradeUserTools(upgradeManager, uhd);

		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_10_1_0 successfully!");
		} else {
			log.audit("OLATUpgrade_10_1_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	/**
	 * Add the static tools to the configurable ones.
	 * 
	 * @param upgradeManager
	 * @param uhd
	 * @return
	 */
	private boolean upgradeUserTools(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_USER_TOOLS)) {
			try {
				String tools = userToolsModule.getAvailableUserTools();
				if(!userToolsModule.isUserToolsDisabled() && StringHelper.containsNonWhitespace(tools)) {
					StringBuilder toolsSb = new StringBuilder(tools == null ? "" : tools);
					String[] defaultUserTools = new String[]{
							"org.olat.home.HomeMainController:org.olat.gui.control.PrintUserToolExtension",
							"org.olat.home.HomeMainController:org.olat.gui.control.SearchUserToolExtension",
							"org.olat.home.HomeMainController:org.olat.gui.control.HelpUserToolExtension",
							"org.olat.home.HomeMainController:org.olat.instantMessaging.ui.ImpressumMainController",
							"org.olat.home.HomeMainController:org.olat.instantMessaging.ui.InstantMessagingMainController"
					};
					
					for(String defaultUserTool:defaultUserTools) {
						if(toolsSb.indexOf(defaultUserTool) < 0) {
							if(toolsSb.length() > 0) toolsSb.append(",");
							toolsSb.append(defaultUserTool);
						}
					}
					userToolsModule.setAvailableUserTools(toolsSb.toString());
				}
	
				String defPreset = userToolsModule.getDefaultPresetOfUserTools();
				StringBuilder defPresetSb = new StringBuilder(defPreset == null ? "" : defPreset);
				String[] defaultPresets = new String[]{
						"org.olat.home.HomeMainController:org.olat.gui.control.PrintUserToolExtension",
						"org.olat.home.HomeMainController:org.olat.gui.control.HelpUserToolExtension",
						"org.olat.home.HomeMainController:org.olat.instantMessaging.ui.ImpressumMainController"
				};
				
				for(String defaultPreset:defaultPresets) {
					if(defPresetSb.indexOf(defaultPreset) < 0) {
						if(defPresetSb.length() > 0) defPresetSb.append(",");
						defPresetSb.append(defaultPreset);
					}
				}
				userToolsModule.setDefaultPresetOfUserTools(defPresetSb.toString());

				uhd.setBooleanDataValue(TASK_USER_TOOLS, false);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
			} catch (Exception e) {
				log.error("", e);
				return false;
			}
		}
		return true;
	}
}