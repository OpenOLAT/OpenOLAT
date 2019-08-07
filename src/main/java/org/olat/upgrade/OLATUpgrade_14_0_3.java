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
import org.olat.core.logging.Tracing;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_14_0_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_14_0_3.class);
	
	private static final String VERSION = "OLAT_14.0.3";
	private static final String PASSWORD_SYNTAX_CHECK_TYPO = "PASSWORD SYNTAX CHECK TYPO";
	
	@Autowired
	private LoginModule loginModule;

	
	public OLATUpgrade_14_0_3() {
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
		allOk &= migratePasswordSyntaxCheckTypo(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_14_0_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_14_0_3 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean migratePasswordSyntaxCheckTypo(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(PASSWORD_SYNTAX_CHECK_TYPO)) {
			try {
				if ("forbiddden".equals(loginModule.getPasswordDigits())) {
					loginModule.setPasswordDigits(LoginModule.FORBIDDEN);
				}
				if ("forbiddden".equals(loginModule.getPasswordDigitsAndSpecialSigns())) {
					loginModule.setPasswordDigitsAndSpecialSigns(LoginModule.FORBIDDEN);
				}
				if ("forbiddden".equals(loginModule.getPasswordLetters())) {
					loginModule.setPasswordLetters(LoginModule.FORBIDDEN);
				}
				if ("forbiddden".equals(loginModule.getPasswordLettersLowercase())) {
					loginModule.setPasswordLettersLowercase(LoginModule.FORBIDDEN);
				}
				if ("forbiddden".equals(loginModule.getPasswordLettersUppercase())) {
					loginModule.setPasswordLettersUppercase(LoginModule.FORBIDDEN);
				}
				if ("forbiddden".equals(loginModule.getPasswordSpecialSigns())) {
					loginModule.setPasswordSpecialSigns(LoginModule.FORBIDDEN);
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(PASSWORD_SYNTAX_CHECK_TYPO, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

}
