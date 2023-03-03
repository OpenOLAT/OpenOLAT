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

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.modules.project.ProjectModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_17_3_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_17_3_0.class);

	private static final String VERSION = "OLAT_17.3.0";
	private static final String INIT_PROJECTS_CONFIGS = "INIT PROJECTS CONFIG";

	@Autowired
	private ProjectModule projectModule;

	public OLATUpgrade_17_3_0() {
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
		allOk &= initProjectsConfigs(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_17_3_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_17_3_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean initProjectsConfigs(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INIT_PROJECTS_CONFIGS)) {
			try {
				String userDataDirectory = WebappHelper.getUserDataRoot();
				Path propsPath = Paths.get(userDataDirectory, "system", "configuration", "org.olat.modules.project.ProjectModule.properties");
				if (!Files.exists(propsPath)) {
					Properties props = new Properties();
					props.setProperty("project.enabled", Boolean.FALSE.toString());
					@SuppressWarnings("resource")
					Writer propWriter = Files.newBufferedWriter(propsPath);
					props.store(propWriter, "");
					propWriter.close();
					projectModule.setEnabled(false);
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INIT_PROJECTS_CONFIGS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
