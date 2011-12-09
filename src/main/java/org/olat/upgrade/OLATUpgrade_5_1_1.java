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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Upgrades for OLAT 5.1.1
 * <P>
 * Initial Date: March 12, 2007 <br>
 * 
 * @author Alexander Schneider
 */
public class OLATUpgrade_5_1_1 extends OLATUpgrade {
	OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String VERSION = "OLAT_5.1.1";
	private static final String TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE = "v2guipreferences deleted";
	private static final String TASK_UPDATE_LANGUAGE_ACCORDING_ISO936_DONE = "languages according iso 936 updated";

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
		
		logDetailsOfUsersAffectedByV2guipreferencesDeletion(upgradeManager);
		cleanupV2guiPreferences(upgradeManager, uhd);
		
		logDetailsOfUsersAffectedByLanguageUpdate(upgradeManager);
		updateLanguagesAccordingISO639(upgradeManager, uhd);
		
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		return true;
	}
	/**
	 * Deletes all v2guipreference with textvalues containing '.*<int>2[0-9]</int>.*', since the feature
	 * multiselect reduced the number of table columns
	 */
	private void cleanupV2guiPreferences(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		
		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE)) {
			String query = "delete from o_property where name = 'v2guipreferences' and textvalue like '%<int>2_</int>%'";
			executePlainSQLDBStatement(query, upgradeManager.getDataSource());
			uhd.setBooleanDataValue(TASK_CLEAN_UP_OF_V2GUIPREFERENCES_DONE, true);

			log.audit("+---------------------------------------------------------------------------------------+");
			log.audit("+... Deleted all v2guipreferences with textvalues containing '.*<int>2[0-9]</int>.*' ...+");
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
			log.warn("Version before 5.1.1 detected! Continue running upgrade for 5.1.1 ...", e);
		}
		
		String query = "select u.firstname, u.lastname, u.email from o_property as p, o_bs_identity as i, o_user as u " + 
									 "where p.name = 'v2guipreferences' " +
									 "and p.textvalue like '%<int>2_</int>%' " +
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
	 * updates all languages codes which are saved in the o_user table according the iso 936
	 *
	 */
	private void updateLanguagesAccordingISO639(UpgradeManager upgradeManager, UpgradeHistoryData uhd){
		if (!uhd.getBooleanDataValue(TASK_UPDATE_LANGUAGE_ACCORDING_ISO936_DONE)) {
			String query_cn_zh = "update o_user set language='zh' where language='cn';";
			String query_cz_cs = "update o_user set language='cs' where language='cz';";
			String query_dk_da = "update o_user set language='da' where language='dk';";
			String query_gr_el = "update o_user set language='el' where language='gr';";
			String query_pe_fa = "update o_user set language='fa' where language='pe';";
			String query_tc_tr = "update o_user set language='tc' where language='tr';";
			
			Connection con = null;
			Statement updateStmt = null;
			
			try {
				con = upgradeManager.getDataSource().getConnection();
				updateStmt = con.createStatement();
				updateStmt.addBatch(query_cn_zh);
				updateStmt.addBatch(query_cz_cs);
				updateStmt.addBatch(query_dk_da);
				updateStmt.addBatch(query_gr_el);
				updateStmt.addBatch(query_pe_fa);
				updateStmt.addBatch(query_tc_tr);

				updateStmt.executeBatch();
			}	catch (SQLException e) {
				log.warn("Could not execute system upgrade sql query composed of : "
						+ query_cn_zh +" and "
						+ query_cz_cs +" and "
						+ query_dk_da +" and "
						+ query_gr_el +" and "
						+ query_pe_fa +" and "
						+ query_tc_tr, e);
				throw new StartupException("Could not execute system upgrade sql query composed of : "
						+ query_cn_zh +" and "
						+ query_cz_cs +" and "
						+ query_dk_da +" and "
						+ query_gr_el +" and "
						+ query_pe_fa +" and "
						+ query_tc_tr, e);
			} finally {
				try {
					updateStmt.close();
				} catch (SQLException e2){
					log.warn("Could not close sql update statement of system upgrade 5.1.1", e2);
					throw new StartupException("Could not close sql update statement of system upgrade 5.1.1", e2);
				} finally {
					try {
						con.close();
					} catch (SQLException e3){
						log.warn("Could not close db connection.", e3);
						throw new StartupException("Could not close db connection.", e3);
					}
				}
			}

			uhd.setBooleanDataValue(TASK_UPDATE_LANGUAGE_ACCORDING_ISO936_DONE, true);

			log.audit("+---------------------------------------------------+");
			log.audit("+....... updated languages according iso 936 .......+");
			log.audit("+...(details of affected users are listed above) ...+");
			log.audit("+---------------------------------------------------+");
			
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private void logDetailsOfUsersAffectedByLanguageUpdate(UpgradeManager upgradeManager){
		
		String checkVersionQuery = "select count(*) from o_userproperty";
		
		try{
			Connection con = upgradeManager.getDataSource().getConnection();
			Statement selectStmt = con.createStatement();
			selectStmt.executeQuery(checkVersionQuery);
		 return;
		} catch (SQLException e){
			log.warn("Version before 5.1.1 detected! Continue running upgrade for 5.1.1 ...", e);
		}
		
		String query = "select u.language, u.firstname, u.lastname, i.name " +
									 "from o_user as u, o_bs_identity as i " +
									 "where i.fk_user_id = u.user_id " +
									 "and (language='cn' or language='cz' or language='dk' or language='gr' or language='pe' or language='tc') order by language;";
		
		try {
			Connection con = upgradeManager.getDataSource().getConnection();
			Statement selectStmt = con.createStatement();
			ResultSet res = selectStmt.executeQuery(query);

			while (res.next()){
				log.audit(res.getString(1)+", "+res.getString(2)+", "+res.getString(3)+", "+res.getString(4)+" ");
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