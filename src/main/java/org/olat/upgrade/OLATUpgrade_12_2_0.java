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

import org.olat.core.util.mail.MailModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_12_2_0 extends OLATUpgrade {

	private static final String VERSION = "OLAT_12.2.0";
	private static final String MAIL_CONFIG_SPLITTING = "MAIL CONFIG SPLITTING";
	
	@Autowired
	MailModule mailModule;
	
	public OLATUpgrade_12_2_0() {
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
		// The config of the visibility of email recipient name and address is
		// split in inbox and outbox specific configs. Get the old values and
		// transfer them to the new values.
		allOk &= splitMailConfigToInboxAndOutbox(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_12_2_0 successfully!");
		} else {
		log.audit("OLATUpgrade_12_2_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	private boolean splitMailConfigToInboxAndOutbox(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MAIL_CONFIG_SPLITTING)) {
			boolean showRecipientsNames = mailModule.isShowOutboxRecipientNames();
			mailModule.setShowInboxRecipientNames(showRecipientsNames);
			log.info("Migrated email config 'show inbox recipient name' to: " + showRecipientsNames);
			boolean showMailAddresses = mailModule.isShowOutboxMailAddresses();
			mailModule.setShowInboxMailAddresses(showMailAddresses);
			log.info("Migrated email config 'show inbox mail adresses' to: " + showMailAddresses);
			
			uhd.setBooleanDataValue(MAIL_CONFIG_SPLITTING, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

}
