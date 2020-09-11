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
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Sept 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_3_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_3_0.class);

	private static final String VERSION = "OLAT_15.3.0";
	private static final String MIGRATE_ONLYOFFICE_CONFIGS = "MIGRATE ONLYOFFICE CONFIGS";
	private static final String RESET_FAILED_THUMBNAILS = "RESET FAILED THUMBNAILS";

	@Autowired
	private DB dbInstance;
	@Autowired
	private OnlyOfficeModule onlyofficeModule;

	public OLATUpgrade_15_3_0() {
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
		allOk &= migrateOnlyOfficeConfigs(upgradeManager, uhd);
		allOk &= resetFailedThumbnails(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_3_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_3_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean migrateOnlyOfficeConfigs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_ONLYOFFICE_CONFIGS)) {
			String userDataDirectory = WebappHelper.getUserDataRoot();
			File configurationPropertiesFile = Paths.get(userDataDirectory, "system", "configuration", "org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule.properties").toFile();
			if (configurationPropertiesFile.exists()) {
				Boolean enabled = null;
				try (InputStream is = new FileInputStream(configurationPropertiesFile);) {
					Properties configuredProperties = new Properties();
					configuredProperties.load(is);

					String enabledProp = configuredProperties.getProperty("onlyoffice.enabled");
					if (StringHelper.containsNonWhitespace(enabledProp)) {
						enabled = Boolean.valueOf(enabledProp);
					}
				} catch (Exception e) {
					log.error("Error when reading / writing user properties config file from path::" + configurationPropertiesFile.getAbsolutePath(), e);
					allOk &= false;
				}
				if (enabled != null && enabled.booleanValue()) {
					onlyofficeModule.setEditorEnabled(true);
				}
			}
			
			uhd.setBooleanDataValue(MIGRATE_ONLYOFFICE_CONFIGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean resetFailedThumbnails(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RESET_FAILED_THUMBNAILS)) {
			try {
				String query = "update filemetadata metadata set metadata.cannotGenerateThumbnails = false where metadata.cannotGenerateThumbnails = true";
				dbInstance.getCurrentEntityManager()
						.createQuery(query)
						.executeUpdate();
				dbInstance.commitAndCloseSession();
				log.info("Failed thumbnails reste.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(RESET_FAILED_THUMBNAILS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
