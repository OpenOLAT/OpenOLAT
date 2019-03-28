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
import org.olat.home.HomeMainController;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.PortfolioPersonalToolController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_11_0_2 extends OLATUpgrade {
	
	private static final String PORTFOLIO_SETTINGS = "PORTFOLIO v2 USER TOOL";
	private static final String VERSION = "OLAT_11.0.2";
	
	private final String PORTFOLIO_V2_TOOL = HomeMainController.class.getCanonicalName() + ":" + PortfolioPersonalToolController.class.getCanonicalName();

	@Autowired
	private UserToolsModule userToolsModule;
	@Autowired
	private PortfolioV2Module portfolioV2Module;

	public OLATUpgrade_11_0_2() {
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
		allOk &= upgradePortfolioSettings(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_11_0_2 successfully!");
		} else {
			log.audit("OLATUpgrade_11_0_2 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean upgradePortfolioSettings(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(PORTFOLIO_SETTINGS)) {
			if(portfolioV2Module.isEnabled()) {
				String availableUserTools = userToolsModule.getAvailableUserTools();
				if(StringHelper.containsNonWhitespace(availableUserTools)) {
					availableUserTools += "," + PORTFOLIO_V2_TOOL;
					userToolsModule.setAvailableUserTools(availableUserTools);
				}
			}
			uhd.setBooleanDataValue(PORTFOLIO_SETTINGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
