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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.repository.RepositoryModule;
import org.olat.repository.manager.CatalogManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_2_2 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_2_2.class);

	private static final String VERSION = "OLAT_15.2.2";
	private static final String MIGRATE_CATALOG_SORTING = "MIGRATE CATALOG SORTING";

	private static final String CATALOG_ADD_LAST = "catalog.add.last";

	@Autowired
	RepositoryModule repositoryModule;
	@Autowired
	CatalogManager catalogManager;

	public OLATUpgrade_15_2_2() {
		super();
	}
	
	@Override
	public String getVersion() {
		return VERSION;
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
		allOk &= migrateCatalogSorting(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_2_2 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_2_2 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateCatalogSorting(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_CATALOG_SORTING)) {
			String userDataDirectory = WebappHelper.getUserDataRoot();
			File configurationPropertiesFile = Paths.get(userDataDirectory, "system", "configuration", "org.olat.repository.RepositoryModule.properties").toFile();
			if (configurationPropertiesFile.exists()) {
				try(InputStream is = new FileInputStream(configurationPropertiesFile)) {
					Properties configuredProperties = new Properties();
					configuredProperties.load(is);

					String addAtLast = configuredProperties.getProperty(CATALOG_ADD_LAST);
					if (addAtLast != null) {
						if (addAtLast.equals("true")) {
							// Add at last
							repositoryModule.setCatalogAddCategoryPosition(2);
							repositoryModule.setCatalogAddEntryPosition(2);
						} else if (addAtLast.equals("false")) {
							// Add at first
							repositoryModule.setCatalogAddCategoryPosition(1);
							repositoryModule.setCatalogAddEntryPosition(1);
						}
					} else {
						// Add alphabetically
						repositoryModule.setCatalogAddCategoryPosition(0);
						repositoryModule.setCatalogAddEntryPosition(0);
					}
				} catch (Exception e) {
					log.error("Error when reading / writing user properties config file from path::{}", configurationPropertiesFile.getAbsolutePath(), e);
					allOk &= false;
				}
			}

			uhd.setBooleanDataValue(MIGRATE_CATALOG_SORTING, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
}
