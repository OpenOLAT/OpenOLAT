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

import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.core.util.xml.XStreamHelper;
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

	private static final XStream xstream = XStreamHelper.createXStreamInstance();

	static {
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.alias("prefstore", Map.class);
		xstream.ignoreUnknownElements();
	}

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

		boolean allOk = true;
		allOk &= migrateGuiPreferences(upgradeManager, uhd);
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
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
						// removing unnecessary root tag DbPrefs
						String normalizedGuiPrefStorage = guiPref.getTextValue().replace("<org.olat.core.util.prefs.db.DbPrefs>", "").replace("</org.olat.core.util.prefs.db.DbPrefs>", "");
						createOrUpdateGuiPref(identity, normalizedGuiPrefStorage);
						// commit after each property, old users could have a lot, so to prevent too much traffic at once
						dbInstance.commitAndCloseSession();
					}
					counter += oldGuiPreferences.size();
					log.info(Tracing.M_AUDIT, "Migrated gui preferences: {} total processed ({})", oldGuiPreferences.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (oldGuiPreferences.size() == BATCH_SIZE);

				log.info("Migration of gui preferences finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}

			uhd.setBooleanDataValue(MIGRATE_GUI_PREFERENCES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}

		return allOk;
	}

	private void createOrUpdateGuiPref(Identity identity, String normalizedGuiPrefStorage) {
		try {
			// if xstream throws exception, only that faulty property won't be transferred
			Map<String, Object> attrClassAndPrefKeyToPrefValueMap = (Map<String, Object>) xstream.fromXML(normalizedGuiPrefStorage);
			for (Map.Entry<String, Object> entry : attrClassAndPrefKeyToPrefValueMap.entrySet()) {
				String attributedClass = StringUtils.substringBefore(entry.getKey(), "::");
				String prefKey = StringUtils.substringAfter(entry.getKey(), "::");
				String guiPrefEntryValue = xstream.toXML(entry.getValue());

				List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, prefKey);
				// if upgrade happens more than once then update entry instead of creating
				if (guiPreferences.isEmpty()) {
					GuiPreference guiPreference = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey, guiPrefEntryValue);
					guiPreferenceService.persistOrLoad(guiPreference);
				} else if (guiPreferences.size() == 1) {
					// updating happens only if upgrade is happening more than once, e.g. because first migration had faulty entries
					// if all three parameters (identity, attributedClass and prefKey) are set, there can only be one entry, thus get(0)
					GuiPreference guiPrefToUpdate = guiPreferences.get(0);
					guiPrefToUpdate.setPrefValue(guiPrefEntryValue);
					guiPreferenceService.updateGuiPreferences(guiPrefToUpdate);
				}
			}
		} catch (Exception e) {
			log.error("Creating or updating for following entry failed: {}", normalizedGuiPrefStorage);
		}
	}

	private List<Property> getOldGuiPreferences(int firstResult, int maxResults) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select gp from property as gp")
				.append(" inner join fetch gp.identity as ident")
				.and().append("gp.name=:name");

		return dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), Property.class)
				.setParameter("name", "v2guipreferences")
				.setFirstResult(firstResult)
				.setMaxResults(maxResults)
				.getResultList();
	}
}