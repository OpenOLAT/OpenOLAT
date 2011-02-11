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

import java.io.File;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.WebappHelper;
import org.olat.login.auth.WebDAVAuthManager;
import org.olat.shibboleth.ShibbolethDispatcher;

/**
 * Description:<br>
 * Upgrade to OLAT 6.2:
 * - Migration of old wiki-fields to flexiform 
 * 
 * Code is already here for every update. 
 * Method calls will be commented out step by step when corresponding new controllers are ready.
 * As long as there will be other things to migrate Upgrade won't be set to DONE!
 * 
 * <P>
 * Initial Date: 20.06.09 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_6_3_3 extends OLATUpgrade {
	private static final String VERSION = "OLAT_6.3.3";
	
	private boolean migrateOlatAuthToWebDAVAuth;

	private static final String TASK_MIGRATE_TO_WEBDAV_PASSWORDS = "Migrate to WebDAV passwords";
	
	private static final String TASK_MIGRATE_WRONGLY_ENCODED_ICAL_LINKS = "Migrate wrongly encoded ical links";
	
	public boolean isMigrateOlatAuthToWebDAVAuth() {
		return migrateOlatAuthToWebDAVAuth;
	}

	public void setMigrateOlatAuthToWebDAVAuth(boolean migrateOlatAuthToWebDAVAuth) {
		this.migrateOlatAuthToWebDAVAuth = migrateOlatAuthToWebDAVAuth;
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
		
		//upgrade to webdav password
		migrateToWebDAVPassword(upgradeManager, uhd);
		

		// OLAT-5736: migrate ical files containing wrong "Â§" characters in node-links (course and group cals effected)
		migrateWronglyEncodedICalLinks(upgradeManager, uhd);
		
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return true;
	}
	
	private void migrateWronglyEncodedICalLinks(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (uhd.getBooleanDataValue(TASK_MIGRATE_WRONGLY_ENCODED_ICAL_LINKS)) {
			// already migrated
			return;
		}
		
		// need to migrate now
		log.audit("migrateWronglyEncodedICalLinks: START");
		File userDataDir = new File(WebappHelper.getUserDataRoot());
		File calendarsDir = new File(userDataDir, "calendars");
		File courseDir = new File(calendarsDir, "course");
		log.audit("migrateWronglyEncodedICalLinks: Migrating course directory: "+courseDir);
		CalendarXOlatLinkUTF8Fix.migrate(courseDir);
		log.audit("migrateWronglyEncodedICalLinks: Done migrating course directory: "+courseDir);
		File groupDir = new File(calendarsDir, "group");
		log.audit("migrateWronglyEncodedICalLinks: Migrating group directory: "+groupDir);
		CalendarXOlatLinkUTF8Fix.migrate(groupDir);
		log.audit("migrateWronglyEncodedICalLinks: Done migrating group directory: "+groupDir);
		
		log.audit("migrateWronglyEncodedICalLinks: DONE");
		
		uhd.setBooleanDataValue(TASK_MIGRATE_WRONGLY_ENCODED_ICAL_LINKS, true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
	}
	
	private void migrateToWebDAVPassword(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_TO_WEBDAV_PASSWORDS)) {
			if(!migrateOlatAuthToWebDAVAuth) {
				//don't migrate the OLAT password
				uhd.setBooleanDataValue(TASK_MIGRATE_TO_WEBDAV_PASSWORDS, true);
				upgradeManager.setUpgradesHistory(uhd, VERSION);
				return;
			}
			
			BaseSecurity secMgr = BaseSecurityManager.getInstance();
			//filter all admins
			SecurityGroup adminGroup = secMgr.findSecurityGroupByName(Constants.GROUP_ADMIN);
			//get all identities
			
			int count = 0;
			List<Identity> identitiesList = secMgr.getIdentitiesByPowerSearch(null, null, true, null, null, null, null, null, null, null, null);
			DBFactory.getInstance().intermediateCommit();
			for(Identity identity:identitiesList) {
				if(count++ % 10 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
				
				boolean admin = secMgr.isIdentityInSecurityGroup(identity, adminGroup);
				if(admin) {
					log.audit("No OLAT Auth. provider migrated for admin: " + identity.getName());
					continue;
				}

				Authentication olatAuth = null, webDAVAuth = null, shibAuth = null;
				List<Authentication> auths = secMgr.getAuthentications(identity);
				for(Authentication auth:auths) {
					if(WebDAVAuthManager.PROVIDER_WEBDAV.equals(auth.getProvider())) {
						webDAVAuth = auth;
					} else if(BaseSecurityModule.getDefaultAuthProviderIdentifier().equals(auth.getProvider())) {
						olatAuth = auth;
					} else if(ShibbolethDispatcher.PROVIDER_SHIB.equals(auth.getProvider())) {
						shibAuth = auth;
					}
				}
				
				if(webDAVAuth == null && olatAuth != null && shibAuth != null) {
					String hashedPwd = olatAuth.getCredential();
					log.audit("Create WebDAV Auth. provider for: " + identity.getName());
					webDAVAuth = secMgr.createAndPersistAuthentication(identity, WebDAVAuthManager.PROVIDER_WEBDAV, identity.getName(), hashedPwd);
					if(webDAVAuth != null) {
						log.audit("Delete OLAT Auth. provider for: " + identity.getName());
						secMgr.deleteAuthentication(olatAuth);
					}
				}
			}

			uhd.setBooleanDataValue(TASK_MIGRATE_TO_WEBDAV_PASSWORDS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}

	public String getVersion() {
		return VERSION;
	}
}
