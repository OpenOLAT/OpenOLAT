package org.olat.upgrade;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.olat.core.logging.StartupException;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;


/**
 * 
 * Description:<br>
 * Upgrade the database
 * 
 * <P>
 * Initial Date:  8 sept. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DatabaseUpgradeManager extends UpgradeManagerImpl {

	protected UpgradesDefinitions olatUpgradesDefinitions;
	
	public DatabaseUpgradeManager() {
		INSTALLED_UPGRADES_XML = "installed_database_upgrades.xml";
	}
	
	/**
	 * [used by Spring]
	 * @param olatUpgradesDefinitions
	 */
	public void setOlatUpgradesDefinitions(UpgradesDefinitions olatUpgradesDefinitions) {
		this.olatUpgradesDefinitions = olatUpgradesDefinitions;
	}
	
	@Override
	public void init() {
		// load upgrades using spring framework 
		upgrades = upgradesDefinitions.getUpgrades();
		// load history of previous upgrades using xstream
		initUpgradesHistories();
		if (autoUpgradeDatabase) {
			runAlterDbStatements();
		} else {
			logInfo("Auto upgrade of the database is disabled. Make sure you do it manually by applying the " +
					"alter*.sql scripts and adding an entry to system/installed_upgrades.xml file.");
		}
	}

	/**
	 * @see org.olat.upgrade.UpgradeManager#runAlterDbStatements()
	 */
	@Override
	public void runAlterDbStatements() {
		String dialect = "";
		//only run upgrades on mysql or postgresql
		if (getDbVendor().contains("mysql")) dialect = "mysql";
		else return;
			
		Statement statement = null;
		try {
			
			logAudit("+--------------------------------------------------------------+");
			logAudit("+... Pure database upgrade: starting alter DB statements ...+");
			logAudit("+ If it fails, do it manually by applying the content of the alter_X_to_Y.sql files.+");
			logAudit("+ For each file you upgraded to add an entry like this to the [pathToOlat]/olatdata/system/installed_database_upgrades.xml: +");
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
						
					if (!uhd.getBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE)) {
						loadAndExecuteSqlStatements(statement, alterDbStatementsFilename, dialect);
						uhd.setBooleanDataValue(OLATUpgrade.TASK_DP_UPGRADE, true);
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
	
	@SuppressWarnings("unchecked")
	protected void initUpgradesHistories() {
		File upgradesDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
		File upgradesHistoriesFile = new File(upgradesDir, INSTALLED_UPGRADES_XML);
		if (upgradesHistoriesFile.exists()) {
			upgradesHistories = (Map<String, UpgradeHistoryData>) XStreamHelper.readObject(upgradesHistoriesFile);
		}
		if (upgradesHistories == null) {
			upgradesHistories = new HashMap<String, UpgradeHistoryData>();
		}
		transferToDBUpgrade(upgradesDir, upgradesHistories);
	}
	
	private void transferToDBUpgrade(File upgradesDir, Map<String, UpgradeHistoryData> dbUpgradeHistory) {
		File stdUpgradesHistoriesFile = new File(upgradesDir, "installed_upgrades.xml");
		if (stdUpgradesHistoriesFile.exists()) {
			Set<String> versions = new HashSet<String>();
			for(OLATUpgrade upgrade:upgradesDefinitions.getUpgrades()) {
				versions.add(upgrade.getVersion());
			}

			Map<String, UpgradeHistoryData> stdUpgradesHistories = (Map<String, UpgradeHistoryData>) XStreamHelper.readObject(stdUpgradesHistoriesFile);
			for(Map.Entry<String, UpgradeHistoryData> entry: stdUpgradesHistories.entrySet()) {
				String version = entry.getKey();
				UpgradeHistoryData data = entry.getValue();
				boolean updated = data.getBooleanDataValue("Database update");
				if(versions.contains(version) && updated && !dbUpgradeHistory.containsKey(version)) {
					dbUpgradeHistory.put(version, data);
				}
			}
		}
	}

	@Override
	public void doPreSystemInitUpgrades() {
		//
	}

	@Override
	public void doPostSystemInitUpgrades() {
		//
	}
}
