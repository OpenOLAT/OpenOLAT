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

import java.util.List;

import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.id.Identity;
import org.olat.registration.RegistrationManager;

/**
 * Description:<br>
 * Automatic upgrade code for the OLAT 6.1.0 release
 * 
 * <P>
 * Initial Date:  01.09.2008 <br>
 * @author gnaegi
 */
public class OLATUpgrade_6_1_0 extends OLATUpgrade {
	private static final String VERSION = "OLAT_6.1.0";
	private static final String TASK_DELETE_OLD_GUEST_USERS = "Old guest users deleted";
	private static final String TASK_CLEANUP_NOTIFICATIONS = "Old notifications cleaned up";
	private static final String TASK_CREATE_DISCLAIMER_CONFIRMATION = "Disclaimer confirmation for existing users created";
	
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

		//cleanup old guest user accounts - not needed anymore with new i18n system
		deleteOldGuestUsers(upgradeManager, uhd);
		
		//clean up notification tables
		cleanUpNotifications(upgradeManager);

		//create disclaimer confirmation for the already existing users, this might take a while!
		createDisclaimerConfirmationForExistingUsers(upgradeManager);
		
		// mark upgrade as finished, never run it again
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return true;
	}

	/**
	 * Deletes the guest users from 6.0 release, the guest users are now created
	 * using other user names using an automated naming schema
	 * 
	 * @param upgradeManager
	 * @param uhd
	 */
	private void deleteOldGuestUsers(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_DELETE_OLD_GUEST_USERS)) {
			log.audit("+---------------------------------------------------------------+");
			log.audit("+... Deleting old guest users - OLAT 6.1 uses new gues users ...+");
			log.audit("+---------------------------------------------------------------+");

			String[] oldGuestUserNames = new String[] { "gast", "guest", "ospite", "invité", "invitado", "episkeptis", "gost", "gosc", "kerencn",
					"kerentw", "gaest", "host", "svecas", "mehman", "convidadopt", "convidadobr", "misafir", "vend&#233;g", "mysafir", "tamu",
					"dhaif", "giast", "gas", "oreah", "khachmoi", "zochin" };
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			for (String guestUserName : oldGuestUserNames) {
				Identity oldGuest = secMgr.findIdentityByName(guestUserName);
				if (oldGuest == null) {
					// skip this one, seems already to be deleted
					continue;
				}
				UserDeletionManager.getInstance().deleteIdentity(oldGuest);
			}
			DBFactory.getInstance().intermediateCommit();
			uhd.setBooleanDataValue(TASK_DELETE_OLD_GUEST_USERS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);

		}
		
	}
	
	
	private void cleanUpNotifications(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (!uhd.getBooleanDataValue(TASK_CLEANUP_NOTIFICATIONS)) {
			
			//delete all subscribers where the state is 1
			String query1 = "delete from o_noti_sub where fk_publisher in (select publisher_id from o_noti_pub where state=1);";
			executePlainSQLDBStatement(query1, upgradeManager.getDataSource());
			
			//delete subscribers from the deleted wiki entries
			String query2 = "delete from o_noti_sub where fk_publisher in (select publisher_id from o_noti_pub, o_olatresource where (o_noti_pub.resid=o_olatresource.resid) AND (o_noti_pub.resname='FileResource.WIKI') AND (o_olatresource.resid IS null));";
			executePlainSQLDBStatement(query2, upgradeManager.getDataSource());
			
			//delete all publishers where the wiki resource is deleted
			String query3 = "delete from o_noti_pub where resid in (select resid from o_olatresource where (o_olatresource.resid IS null)) AND (o_noti_pub.resname='FileResource.WIKI');";
			executePlainSQLDBStatement(query3, upgradeManager.getDataSource());
			
			//delete all publishers where the state is 1
			String query4 = "delete from o_noti_pub where state=1;";
			executePlainSQLDBStatement(query4, upgradeManager.getDataSource());
			
			
			uhd.setBooleanDataValue(TASK_CLEANUP_NOTIFICATIONS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);

		}
	}

	private void createDisclaimerConfirmationForExistingUsers(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (!uhd.getBooleanDataValue(TASK_CREATE_DISCLAIMER_CONFIRMATION)) {
			// Get all system users
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			RegistrationManager regMgr = RegistrationManager.getInstance();
			DB db = DBFactory.getInstance();
			// Get all users
			List<Identity> identities = secMgr.getVisibleIdentitiesByPowerSearch(null, null, false, null, null, null, null, null);
			// Remove the users that did already confirm the disclaimer
			List<Identity> confirmedIdentities = regMgr.getIdentitiesWithConfirmedDisclaimer();
			PersistenceHelper.removeObjectsFromList(identities, confirmedIdentities);
			// Set the disclaimer property for the remaining users
			for (int i = 0; i < identities.size(); i++) {
				Identity identity = identities.get(i);
				regMgr.setHasConfirmedDislaimer(identity);
				// write something to the console after each 100 user, this can take a
				// while with many users and it is handy to know that the system is
				// doing something
				if (i % 250 == 0) {
					log.audit("Busy creating disclaimer confirmation. Done with " + i + " of a total of " + identities.size() + " users. Please wait ...");
					db.intermediateCommit();
				}
			}
			log.audit("Done with creating disclaimer confirmation for " + identities.size() + " users");
			
			uhd.setBooleanDataValue(TASK_CREATE_DISCLAIMER_CONFIRMATION, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}


}
