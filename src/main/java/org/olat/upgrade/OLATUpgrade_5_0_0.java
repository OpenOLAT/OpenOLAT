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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * Upgrades for OLAT 5.0
 * <P>
 * Initial Date: Aug 8, 2006 <br>
 * 
 * @author guido
 */
public class OLATUpgrade_5_0_0 extends OLATUpgrade {
	OLog log = Tracing.createLoggerFor(this.getClass());
	private static final String VERSION = "OLAT_5.0.0";
	private static final String TASK_DELETE_UNREFERENCED_REPOENTRIERS = "Delete all repository entries that do not have a reference in the database";
	private static final String TASK_CLEAN_UP_IM_AND_GUI_PREFERENCES_PROPERTIES_DONE = "IM and GUI preferences properties deleted";

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
		cleanRepositoryAndDeleteUnreferencedEntries(upgradeManager, uhd);
		cleanupIMAndGUIPreferencesProperties(upgradeManager, uhd);
		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		return true;
	}

	private void cleanRepositoryAndDeleteUnreferencedEntries(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		/**
		 * Due to a bug which was introduced in olat 4.1 there are possile zombie
		 * repository folders left on the disk. This can happen if someone tried to
		 * create a repo entry out of an existing file like a SCORM zip or an IMS
		 * content package and the file was not accepted due to errors in its
		 * structure.
		 */

		if (!uhd.getBooleanDataValue(TASK_DELETE_UNREFERENCED_REPOENTRIERS)) {
			// begin clean up

			String bcrootPath = FolderConfig.getCanonicalRoot();
			File bcRootDir = new File(bcrootPath);
			File repository = new File(bcRootDir, "repository");
			if (repository.exists()) {
				String[] repositoryFoldersAndFilesOnDisk = repository.list();
				List repositoryFoldersOnDisk = new ArrayList(repositoryFoldersAndFilesOnDisk.length);
				//filder for directories only as there are images as well in the repo folder
				for (int i = 0; i < repositoryFoldersAndFilesOnDisk.length; i++) {
					String repoId = repositoryFoldersAndFilesOnDisk[i];
					if(new File(repository, repoId).isDirectory()){
						repositoryFoldersOnDisk.add(repositoryFoldersAndFilesOnDisk[i]);
					}
				}
	
				// get all repository entries
				Roles roles = new Roles(true, true, true, true, false, true, false);
				List inDatabase = RepositoryManager.getInstance().genericANDQueryWithRolesRestriction(null, null, null, null, roles, null);
	
				Set inDatabaseIDs = new HashSet(inDatabase.size());
				for (Iterator iter = inDatabase.iterator(); iter.hasNext();) {
					RepositoryEntry element = (RepositoryEntry) iter.next();
					inDatabaseIDs.add(element.getOlatResource().getResourceableId());
				}
				
				// deleting all that are in repositoryFoldersOnDisk and not in the
				// inDatabaseIds
				for (Iterator iter = repositoryFoldersOnDisk.iterator(); iter.hasNext();) {
					String rescourcableId = (String) iter.next();
					try {
						if (!inDatabaseIDs.contains(Long.valueOf(rescourcableId))) {
							FileUtils.deleteDirsAndFiles(new File(repository, rescourcableId), true, true);
							log.audit("Deleting unreferenced folder in repository with id:" + rescourcableId);
						}
					} catch (NumberFormatException e) {
						log.audit("Could not delete unreferenced folder in repository with id:" + rescourcableId);
					}
				}
			
			} //end file exists
			
			// clean up finished
			uhd.setBooleanDataValue(TASK_DELETE_UNREFERENCED_REPOENTRIERS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

	}
	
	
	private void cleanupIMAndGUIPreferencesProperties(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		// Due to the package refactoring (separation of classes within framework and olat) the user settings for Instant Messaging and GUI preferences have to be deleted
		// <org.olat.util.prefs.ImPreferences> --> <org.olat.instantMessaging.ImPreferences>
		// <org.olat.util.prefs.GuiPreferences> --> <org.olat.preferences.DbPrefs>
		
		if (!uhd.getBooleanDataValue(TASK_CLEAN_UP_IM_AND_GUI_PREFERENCES_PROPERTIES_DONE)) {
			String query = "delete from o_property where name ='guipreferences' or name = 'impreferences';";
			executePlainSQLDBStatement(query, upgradeManager.getDataSource());
			uhd.setBooleanDataValue(TASK_CLEAN_UP_IM_AND_GUI_PREFERENCES_PROPERTIES_DONE, true);

			log.audit("+-------------------------------------------+");
			log.audit("+... Deleting all IM and GUI preferences ...+");
			log.audit("+-------------------------------------------+");
			
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
