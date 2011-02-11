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
* <p>
*/ 

package org.olat.upgrade;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Upgrades for OLAT 6.0.0
 * <P>
 * Initial Date: March 12, 2007 <br>
 * 
 * @author Alexander Schneider
 */
public class OLATUpgrade_6_0_0 extends OLATUpgrade {
	OLog log = Tracing.createLoggerFor(this.getClass());

	private static final String VERSION = "OLAT_6.0.0";
	private static final String TASK_CLEAN_UP_DROPBOX_SUBSCRIPTION_DONE = "dropboxsubscription migrated";
	private static final String TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE = "V2GUI preferences properties deleted";

	/**
	 * @see org.olat.upgrade.OLATUpgrade#getVersion()
	 */
	public String getVersion() {
		return VERSION;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}
		
		migrateDropboxSubscription(upgradeManager, uhd);
		cleanupV2GUIPreferencesProperties(upgradeManager, uhd);
		
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		
		return true;
	}
	/**
	 * Rename all Dropbox Subscription with resname='DropboxController' to 'CourseModule' 
	 * because they could not be started from portal.
	 */
	private void migrateDropboxSubscription(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		
		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_DROPBOX_SUBSCRIPTION_DONE)) {
			String query = "update o_noti_pub set resname='CourseModule' where resname='DropboxController';";
			executePlainSQLDBStatement(query, upgradeManager.getDataSource());
			uhd.setBooleanDataValue(TASK_CLEAN_UP_DROPBOX_SUBSCRIPTION_DONE, true);

			log.audit("+---------------------------------------------------------------------------------------+");
			log.audit("+... Migrated all dropbox subscriptions, rename 'DropboxController' to 'CourseModule'...+");
			log.audit("+---------------------------------------------------------------------------------------+");
			
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private void cleanupV2GUIPreferencesProperties(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		// Due to the YAMLizing of OLAT the GUI preferences have to be deleted

		
		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE)) {
			String query = "delete from o_property where name ='v2guipreferences';";
			executePlainSQLDBStatement(query, upgradeManager.getDataSource());
			uhd.setBooleanDataValue(TASK_CLEAN_UP_GUI_PREFERENCES_PROPERTIES_DONE, true);

			log.audit("+--------------------------------------+");
			log.audit("+... Deleting all V2GUI preferences ...+");
			log.audit("+--------------------------------------+");
			
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	/**
	 * 
	 * @see org.olat.upgrade.OLATUpgrade#getAlterDbStatements()
	 */
	public String getAlterDbStatements() {
		return null; //till 6.1 was manual upgrade
	}

}