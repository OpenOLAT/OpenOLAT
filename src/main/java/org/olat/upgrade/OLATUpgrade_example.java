/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.upgrade;

/**
 * Description:<br>
 * Example code on how to use this upgrade thing
 * <P>
 * Initial Date: 15.08.2005 <br>
 * 
 * @author gnaegi
 */
public class OLATUpgrade_example extends OLATUpgrade {
	private static final String VERSION = "example_1.0";
	
	/**
	 * [objects are created by Spring]
	 */
	private OLATUpgrade_example() {
		//
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize the upgrade history object
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}

		// START with real upgrade code
		if (!uhd.getBooleanDataValue("fooPreMethodInstalled")) {
			// do fooMethod here, only if not already done in a previous attempt
			uhd.setBooleanDataValue("fooPreMethodPassed", true);
		}

		if (!uhd.getBooleanDataValue("BarPreMethodInstalled")) {
			// do BarMethod here, only if not already done in a previous attempt
			uhd.setBooleanDataValue("BarPreMethodPassed", true);
		}

		uhd.setBooleanDataValue("preUpgradeFinished", true);
		// END upgrade code

		// persist infos
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return true;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd.isInstallationComplete()) return false;

		// START with real upgrade code
		if (!uhd.getBooleanDataValue("fooPostMethodInstalled")) {
			// do fooMethod here, only if not already done in a previous attempt
			uhd.setBooleanDataValue("fooPostMethodPassed", true);
		}

		if (!uhd.getBooleanDataValue("BarPostMethodInstalled")) {
			// do BarMethod here, only if not already done in a previous attempt
			uhd.setBooleanDataValue("BarPostMethodPassed", true);
		}

		uhd.setBooleanDataValue("postUpgradeFinished", true);
		// END upgrade code

		// now pre and post code was ok, finish installation
		uhd.setInstallationComplete(true);

		// persist infos
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return true;
	}

	public String getVersion() {
		return VERSION;
	}

}
