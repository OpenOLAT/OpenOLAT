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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;

/**
 * Upgrades for OLAT 5.2.0
 * 
 * @author Christian Guretzki
 */
public class OLATUpgrade_5_2_0 extends OLATUpgrade {
	OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String VERSION = "OLAT_5.2.0";
	private static final String TASK_MIGRATE_LOGIN_DENIED_SECURITY_GROUP_DONE = "Migrate login-denied security-group DONE";
	private static final String TASK_INITIALIZE_LAST_LOGIN_DONE = "Initialize identity.lastlogin, group.lastusage DONE";
	private static final String TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE = "v2guipreferences where ajax-beta-on = false deleted";

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
		migrateLoginDeniedSecurityGroup(uhd);
		initializeLastLogin(uhd, upgradeManager);
		
		logDetailsOfUsersAffectedByV2guipreferencesDeletion(upgradeManager);
		cleanupV2guiPreferences(upgradeManager, uhd);
	
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		return true;
	}

	/**
	 * Migrate all user in logon-denied security-group to users with status=login_denied.
	 */
	private void migrateLoginDeniedSecurityGroup(UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_LOGIN_DENIED_SECURITY_GROUP_DONE)) {
			BaseSecurity secManager = BaseSecurityManager.getInstance();
			SecurityGroup loginDeniedGroup = BaseSecurityManager.getInstance().findSecurityGroupByName("logondenied");// so far: Constants.GROUP_LOGON_DENIED
			if (loginDeniedGroup != null) {
				// 1. Migrate users from logon-denied security-group to users with status=login_denied
				List identitiesList = secManager.getIdentitiesByPowerSearch(null, null, true,
						new SecurityGroup[] {loginDeniedGroup}, null, null, null, null,null, null, null);
				for (Iterator iter = identitiesList.iterator(); iter.hasNext();) {
					Identity identity = (Identity) iter.next();
					secManager.saveIdentityStatus(identity,Identity.STATUS_LOGIN_DENIED);
					secManager.removeIdentityFromSecurityGroup(identity, loginDeniedGroup);
					log.audit("Identity was in security-group 'Logon_denied' => set status of identity to 'logon_denied'; identity=" + identity);
				}
	
				// 2. Delete named group logon_denied
				DB db = DBFactory.getInstance();
				db.delete("from org.olat.basesecurity.NamedGroupImpl as ngroup where ngroup.groupName = ?", new Object[] { "logondenied" },// so far: Constants.GROUP_LOGON_DENIED
						new Type[] { Hibernate.STRING });
	      // 3. Delete security-group 'Logon_denied'
				secManager.deleteSecurityGroup(loginDeniedGroup);
				log.audit("Delete security-group 'Logon_denied'");
			}
			uhd.setBooleanDataValue(TASK_MIGRATE_LOGIN_DENIED_SECURITY_GROUP_DONE, true);

			log.audit("+---------------------------------------------------------------------------------+");
			log.audit("+... Migrate all logon-denied users from security-group to status=login_denied ...+");
			log.audit("+................... (details of affected users are listed above)  ...............+");
			log.audit("+---------------------------------------------------------------------------------+");
		}
	}

	private void initializeLastLogin(UpgradeHistoryData uhd, UpgradeManager upgradeManager) {
		
		// Set last-login for all idenitity with last-login == null
		String initDateLastLogin = Formatter.formatDatetime(new Date()); 
		String lastLoginCountQuery = "select count(*) from o_bs_identity where lastlogin IS NULL;";
		String lastLoginQuery = "update o_bs_identity set lastlogin = '" + initDateLastLogin + "' where lastlogin IS NULL;";
		// Set last-usage for all business-group with last-usage == null
		String initDateLastUsage = Formatter.formatDatetime(new Date());
		String lastUsageCountQuery = "select count(*) from o_gp_business where lastusage IS NULL;";
		String lastUsageQuery = "update o_gp_business set lastusage = '" + initDateLastUsage + "' where lastusage IS NULL;";
		// Set last-usage for all repository-entries with last-usage == null
		String repositoryLastUsageCountQuery = "select count(*) from o_repositoryentry where lastusage IS NULL;";
		String repositoryLastUsageQuery = "update o_repositoryentry set lastusage = '" + initDateLastUsage + "' where lastusage IS NULL;";
		
		int countIdentityLastLoginUpdates;
		int countGroupLastUsageUpdates;
		int countRepositoryLastUsageUpdates;
		try {
			Connection con = upgradeManager.getDataSource().getConnection();
			Statement countStmt = con.createStatement();
			ResultSet resultSet = countStmt.executeQuery(lastLoginCountQuery);
			resultSet.next();
			countIdentityLastLoginUpdates = resultSet.getInt(1);
			resultSet = countStmt.executeQuery(lastUsageCountQuery);
			resultSet.next();
			countGroupLastUsageUpdates = resultSet.getInt(1);
			resultSet = countStmt.executeQuery(repositoryLastUsageCountQuery);
			resultSet.next();
			countRepositoryLastUsageUpdates = resultSet.getInt(1);

			Statement updateStmt = con.createStatement();
			updateStmt.addBatch(lastLoginQuery);
			updateStmt.addBatch(lastUsageQuery);
			updateStmt.addBatch(repositoryLastUsageQuery);
			updateStmt.executeBatch();
		} catch (SQLException e) {
			log.warn("Could not execute system upgrade sql query. ", e);
			throw new StartupException("Could not execute system upgrade sql query. ", e);
		}

		uhd.setBooleanDataValue(TASK_INITIALIZE_LAST_LOGIN_DONE, true);

		log.audit("+------------------------------------------------------------------------------------+");
		log.audit("+... Initialize all idenitity with last-login == null to start-last-login date    ...+");
		log.audit("+... Update " + countIdentityLastLoginUpdates + " identities with lastlogin-date = '" + initDateLastLogin + "'  ...+");
		log.audit("+... Initialize all business-groups with last-usage == null to date of upgrade    ...+");
		log.audit("+... Update " + countGroupLastUsageUpdates + " groups with lastusage-date = '" + initDateLastUsage + "'  ...+");		
		log.audit("+... Initialize all repository-entries with last-usage == null to date of upgrade ...+");
		log.audit("+... Update " + countRepositoryLastUsageUpdates + " repository-entries with lastusage-date = '" + initDateLastUsage + "'  ...+");		
		log.audit("+------------------------------------------------------------------------------------+");
	}
	
	
	/**
	 * Deletes all v2guipreference with textvalues containing '.*ajax-beta-on</string><boolean>false</boolean>'
	 */
	private void cleanupV2guiPreferences(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {

		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE)) {
			String query = "delete from o_property where name = 'v2guipreferences' and textvalue like '%ajax-beta-on</string>%<boolean>false%'";
			
			try {
				Connection con = upgradeManager.getDataSource().getConnection();
				Statement deleteStmt = con.createStatement();
				deleteStmt.execute(query);
				
				con.close();
				con = null;

			} catch (SQLException e) {
				log.warn("Could not execute system upgrade sql query. Query:"+ query, e);
				throw new StartupException("Could not execute system upgrade sql query. Query:"+ query, e);
			}

			uhd.setBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE, true);

			log.audit("+---------------------------------------------------------------------------------------+");
			log.audit("+... Deleted all v2guipreferences with textvalues containing 'ajax-beta-on' -> false ...+");
			log.audit("+................... (details of affected users are listed above)  .....................+");
			log.audit("+---------------------------------------------------------------------------------------+");
			
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}

	private void logDetailsOfUsersAffectedByV2guipreferencesDeletion(UpgradeManager upgradeManager){
		
		String checkVersionQuery = "select count(*) from o_userproperty";
		
		try{
			Connection con = upgradeManager.getDataSource().getConnection();
			Statement selectStmt = con.createStatement();
			selectStmt.executeQuery(checkVersionQuery);
		 return;
		} catch (SQLException e){
			log.warn("Version before 5.2.0 detected! Continue running upgrade for 5.2.0 ...", e);
		}
		
		String query = "select u.firstname, u.lastname, u.email from o_property as p, o_bs_identity as i, o_user as u " + 
									 "where p.name = 'v2guipreferences' " +
									 "and p.textvalue like '%ajax-beta-on</string>%<boolean>false%' " +
									 "and p.identity = i.id " +
									 "and i.fk_user_id = u.user_id;";
		
		try {
			Connection con = upgradeManager.getDataSource().getConnection();
			Statement selectStmt = con.createStatement();
			ResultSet res = selectStmt.executeQuery(query);

			while (res.next()){
				log.audit(res.getString(1)+", "+res.getString(2)+", "+res.getString(3)+" ");
			}
			
		} catch (SQLException e) {
			log.warn("Could not execute system upgrade sql query. Query:"+ query, e);
			throw new StartupException("Could not execute system upgrade sql query. Query:"+ query, e);
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