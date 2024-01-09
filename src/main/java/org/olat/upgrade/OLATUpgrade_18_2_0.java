/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Dez 11, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OLATUpgrade_18_2_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_18_2_0.class);

	private static final int BATCH_SIZE = 1000;

	private static final String VERSION = "OLAT_18.2.0";
	private static final String MIGRATE_GUI_PREFERENCES = "MIGRATE GUI PREFERENCES";

	@Autowired
	private DB dbInstance;
	@Autowired
	private GuiPreferenceService guiPreferenceService;

	public OLATUpgrade_18_2_0() {
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

		boolean allOk;
		allOk = migrateGuiPreferences(upgradeManager, uhd);
		uhd.setInstallationComplete(allOk);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_18_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_18_2_0 not finished, try to restart OpenOlat!");
		}

		return allOk;
	}

	private boolean migrateGuiPreferences(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;

		if (!uhd.getBooleanDataValue(MIGRATE_GUI_PREFERENCES)) {
			try {
				log.info("Start migrating gui preferences.");

				int counter = 0;
				List<Property> oldGuiPreferences;
				do {
					oldGuiPreferences = getOldGuiPreferences(counter, BATCH_SIZE);

					for (Property guiPref : oldGuiPreferences) {
						Identity identity = guiPref.getIdentity();

						List<String> guiPrefEntries = new ArrayList<>(List.of(guiPref.getTextValue().split("\\s+(?=<entry>)|(?<=</entry>)\\s+")));
						// remove elements which are not an entry (only entries contain relevant data)
						guiPrefEntries.removeIf(g -> !g.contains("entry"));

						for (String guiPrefEntry : guiPrefEntries) {
							String attributedClass = StringUtils.substringBefore(guiPrefEntry.split("<string>")[1].split("</string>")[0], "::");
							String prefKey = StringUtils.substringAfter(guiPrefEntry.split("<string>")[1].split("</string>")[0], "::");

							// only get relevant value
							guiPrefEntry = StringUtils.substringBefore(StringUtils.substringAfter(guiPrefEntry, "</string>"), "</entry>").replaceAll("\\s+", "");
							List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, prefKey);
							// if upgrade happens more than once then ignore existing prefs
							if (guiPreferences.isEmpty()) {
								GuiPreference guiPreference = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey, guiPrefEntry);
								guiPreferenceService.persistOrLoad(guiPreference);
							}
						}
					}
					counter += oldGuiPreferences.size();
					log.info(Tracing.M_AUDIT, "Migrated gui preferences: {} total processed ({})", oldGuiPreferences.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (oldGuiPreferences.size() == BATCH_SIZE);

				dbInstance.commitAndCloseSession();
				log.info("Migration of gui preferences finished.");
			} catch (Exception e) {
				log.error("", e);
				return false;
			}

			uhd.setBooleanDataValue(MIGRATE_GUI_PREFERENCES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}

	private List<Property> getOldGuiPreferences(int firstResult, int maxResults) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select gp from property as gp")
				.and().append("gp.name=:name");

		return dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), Property.class)
				.setParameter("name", "v2guipreferences")
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}