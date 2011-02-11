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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.StartupException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 * <P>
 * Initial Date:  15.08.2005 <br>
 * @author gnaegi
 * @author guido
 */
public class UpgradeManagerImpl extends UpgradeManager {
	
	/**
	 * used by spring
	 */
	private UpgradeManagerImpl() {
		//
	}
	
	/**
	 * Execute the pre system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	public void doPreSystemInitUpgrades() {
		Iterator<OLATUpgrade> iter = upgrades.iterator();
		OLATUpgrade upgrade = null;
		try {
			while (iter.hasNext()) {
				upgrade = iter.next();
				if (upgrade.doPreSystemInitUpgrade(this))
					logAudit("Successfully installed PreSystemInitUpgrade::" + upgrade.getVersion());
				//no DB Module is initialized in PreSystemInit State - no intermediate commit necessary.
			}
		} catch (Throwable e) {
			logWarn("Error upgrading PreSystemInitUpgrade::" + upgrade.getVersion(), e);
			abort(e);
		}
	}

	/**
	 * Execute the post system init code of all upgrades in the order as they were configured
	 * in the configuration file
	 */
	public void doPostSystemInitUpgrades() {
		Iterator<OLATUpgrade> iter = upgrades.iterator();
		OLATUpgrade upgrade = null; 
		try {
			while (iter.hasNext()) {
				upgrade = iter.next();
				if (upgrade.doPostSystemInitUpgrade(this))
					logAudit("Successfully installed PostSystemInitUpgrade::" + upgrade.getVersion());
				//just in case a doPostSystemInitUpgrade did forget it.
				DBFactory.getInstance(false).commitAndCloseSession();
			}
		} catch (Throwable e) {
			DBFactory.getInstance(false).rollbackAndCloseSession();
			logWarn("Error upgrading PostSystemInitUpgrade::" + upgrade.getVersion(), e);
			abort(e);
		} 
	}

	/**
	 * @see org.olat.upgrade.UpgradeManager#runAlterDbStatements()
	 */
	@Override
	public void runAlterDbStatements() {
		String dialect = "";
		//only run upgrades on mysql or postgresql
		if (dataSource.getUrl().contains("mysql")) dialect = "mysql";
		else if (dataSource.getUrl().contains("postgresql")) dialect = "postgresql";
		else if (dataSource.getUrl().contains("hsqldb")) return;
		else return;
			
		Statement statement = null;
		try {
			
			logAudit("+--------------------------------------------------------------+");
			logAudit("+... DB upgrade: Starting alter DB statements ...+");
			logAudit("+ If it fails, do it manually by applying the content of the alter_X_to_Y.sql files.+");
			logAudit("+ For each file you upgraded to add an entry like this to the [pathToOlat]/olatdata/system/installed_upgrades.xml: +");
			logAudit("+ <entry><string>Database update</string><boolean>true</boolean></entry>+");
			logAudit("+--------------------------------------------------------------+");
			
			statement  = dataSource.getConnection().createStatement();
			
			Iterator<OLATUpgrade> iter = upgrades.iterator();
			OLATUpgrade upgrade = null;
			while (iter.hasNext()) {
				upgrade = iter.next();
				String alterDbStatementsFilename = upgrade.getAlterDbStatements();
				if (alterDbStatementsFilename != null) {
					UpgradeHistoryData uhd = getUpgradesHistory(upgrade.getVersion());
					if (uhd == null) {
						// has never been called, initialize
						uhd = new UpgradeHistoryData();
					} 
						
					if (!uhd.getBooleanDataValue(upgrade.TASK_DP_UPGRADE)) {
						loadAndExecuteSqlStatements(statement, alterDbStatementsFilename, dialect);
						uhd.setBooleanDataValue(upgrade.TASK_DP_UPGRADE, true);
						setUpgradesHistory(uhd, upgrade.getVersion());
						logAudit("Successfully executed alter DB statements for Version::" + upgrade.getVersion());
					}
					
				}
			}
			
		}	catch (SQLException e) {
			logError("Could not upgrade your database! Please do it manually and add ", e);
			throw new StartupException("Could not execute alter db statements. Please do it manually.", e);
			
		} catch (Throwable e) {
			logWarn("Error executing alter DB statements::", e);
			abort(e);
			
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e2){
				logWarn("Could not close sql statement", e2);
				throw new StartupException("Could not close sql statements.", e2);
			}
		}
		
	}

	/**
	 * load file with alter statements and add to batch
	 * @param statements
	 * @param alterDbStatements
	 */
	private void loadAndExecuteSqlStatements(Statement statement, String alterDbStatements, String dialect) {
		try {
			Resource setupDatabaseFile = new ClassPathResource("/resources/database/"+dialect+"/"+alterDbStatements);
			if (!setupDatabaseFile.exists()) {
				throw new StartupException("The database upgrade file was not found on the classpath: "+"/database/"+dialect+"/"+alterDbStatements);
			}
			InputStream in = setupDatabaseFile.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			StringBuilder sb = new StringBuilder();
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				if (strLine.length() > 1 && (!strLine.startsWith("--") && !strLine.startsWith("#"))) {
						sb.append(strLine.trim());
				}
			}
			
			StringTokenizer tokenizer = new StringTokenizer(sb.toString(), ";");
			String sql = null;
				while (tokenizer.hasMoreTokens()) {
					try {
						sql = tokenizer.nextToken()+";".toLowerCase();
						if (sql.startsWith("update") || sql.startsWith("delete") || sql.startsWith("alter") || sql.startsWith("insert")) {
							statement.executeUpdate(sql);
						} else {
							statement.execute(sql);
						}
						logInfo("Successfully upgraded database with the following sql: "+sql);
					} catch (SQLException e) {
						String msg = e.getMessage();
						//stop upgrading database if already done
						if (e.getMessage()!= null && (msg.contains("already exists") || msg.contains("Duplicate") || msg.contains("Can't create table") || msg.contains("column/key exists"))) {
							logError("Error while trying to upgrade the database with:("+sql+"). We will continue with upgrading but check the errors manually! Error says:", e);
						}
					} catch (Exception e) {
						//handle non sql errors
						logError("Could not upgrade your database!",e);
						throw new StartupException("Could not add alter db statements to batch.", e);
					}
				}
				
			in.close();
		} catch (FileNotFoundException e1) {
			logError("could not find deleteDatabase.sql file!", e1);
			abort(e1);
		} catch (IOException e) {
			logError("could not read deleteDatabase.sql file!", e);
			abort(e);
		}
	}

}
