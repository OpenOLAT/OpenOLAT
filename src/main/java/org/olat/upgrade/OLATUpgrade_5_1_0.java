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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * Upgrades for OLAT 5.1.0
 * <P>
 * Initial Date: March 1, 2007 <br>
 * 
 * @author Alexander Schneider
 */
public class OLATUpgrade_5_1_0 extends OLATUpgrade {
	OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String VERSION = "OLAT_5.1.0";
	private static final String TASK_CLEAN_UP_OF_PUB_AND_SUB_OF_RETURNBOXES_DONE = "Publishers and subscribers of returnboxes deleted";

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
		cleanupPublishersAndSubscribersOfReturnBoxes(upgradeManager, uhd);
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		return true;
	}
	/**
	 * Deletes all publishers and subscribers of the publishertype "ReturnBoxController". The asynchronous notification of users when 
   * something changes in their returnboxes is removed, since it is already done synchronously 
	 */
	private void cleanupPublishersAndSubscribersOfReturnBoxes(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		
		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_OF_PUB_AND_SUB_OF_RETURNBOXES_DONE)) {
			String query_sub = "delete from o_noti_sub where fk_publisher in (select publisher_id from  o_noti_pub where publishertype='ReturnboxController');";
			String query_pub = "delete from  o_noti_pub where publishertype='ReturnboxController';";
			
			Connection con = null;
			Statement deleteStmt = null;
			boolean cleaned = false;
			
			log.audit("+--------------------------------------------------------------+");
			log.audit("+... Deleting all publishers and subscribers of returnboxes ...+");
			log.audit("+--------------------------------------------------------------+");
			
			try {
				con = upgradeManager.getDataSource().getConnection();
				deleteStmt = con.createStatement();
				deleteStmt.addBatch(query_sub);
				deleteStmt.addBatch(query_pub);
				deleteStmt.executeBatch();
			}	catch (SQLException e) {
				log.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
				log.warn("%%%          Please upgrade your database!          %%%");
				log.warn("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
				log.warn("Could not execute system upgrade! Your database does not support the following syntax: 'WHERE experession IN (subquery)'."
											+ "First query: "+ query_sub +" Second query: "+ query_pub,
												e);
			} finally {
				try {
					deleteStmt.close();
				} catch (SQLException e2){
					log.warn("Could not close sql delete statement of system upgrade 5.1.0", e2);
					throw new StartupException("Could not close sql delete statement of system upgrade 5.1.0", e2);
				} finally {
					try {
						con.close();
					} catch (SQLException e3){
						log.warn("Could not close db connection.", e3);
						throw new StartupException("Could not close db connection.", e3);
					}
				}
			}
			cleaned = true;
			uhd.setBooleanDataValue(TASK_CLEAN_UP_OF_PUB_AND_SUB_OF_RETURNBOXES_DONE, cleaned);
			
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
