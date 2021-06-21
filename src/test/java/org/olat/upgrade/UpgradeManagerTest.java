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
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.xml.XStreamHelper;

/**
 * 
 * Initial date: 18 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UpgradeManagerTest {
	
	private static final Logger log = Tracing.createLoggerFor(UpgradeManagerTest.class);
	
	@Test
	public void readUpgradesXml() throws Exception {
		URL upgradeUrl = UpgradeManagerTest.class.getResource("installed_upgrades.xml");
		File upgradeFile = new File(upgradeUrl.toURI());
		Map<String, UpgradeHistoryData> upgrades = UpgradeManager.read(upgradeFile);
		Assert.assertNotNull(upgrades);
		Assert.assertFalse(upgrades.isEmpty());
	}
	
	@Test
	public void readDatabaseUpgradesXml() throws Exception {
		URL upgradeUrl = UpgradeManagerTest.class.getResource("installed_database_upgrades.xml");
		File upgradeFile = new File(upgradeUrl.toURI());
		Map<String, UpgradeHistoryData> upgrades = UpgradeManager.read(upgradeFile);
		Assert.assertNotNull(upgrades);
		Assert.assertFalse(upgrades.isEmpty());
	}
	
	@Test
	public void writeDatabaseUpgradesXml() throws Exception {
		URL upgradeUrl = UpgradeManagerTest.class.getResource("installed_database_upgrades.xml");
		File upgradeFile = new File(upgradeUrl.toURI());
		Map<String, UpgradeHistoryData> upgrades = UpgradeManager.read(upgradeFile);
		Assert.assertNotNull(upgrades);
		Assert.assertFalse(upgrades.isEmpty());

		File savedFile = new File(WebappHelper.getTmpDir(), "upgradeDB-" + UUID.randomUUID() + ".xml");
		XStreamHelper.writeObject(UpgradeManager.upgradesXStream, savedFile, upgrades);
		if(!savedFile.delete()) {
			log.error("Cannot delete: {}", savedFile);
		}
	}
}
