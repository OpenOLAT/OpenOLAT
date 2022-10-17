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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nModule;
import org.olat.repository.CatalogEntry;
import org.olat.upgrade.model.UpgradeCatalogEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 feb 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_pre_6 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_pre_6.class);

	private static final String VERSION = "OLAT_15.pre.6";
	private static final String CATALOG_ORDER_INDEX = "CATALOG ORDER INDEX";

	private AtomicInteger migrationCounter = new AtomicInteger(0);

	@Autowired
	private DB dbInstance;

	public OLATUpgrade_15_pre_6() {
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
		allOk &= migrateCatalog(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_pre_5 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_pre_5 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}


	private boolean migrateCatalog(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CATALOG_ORDER_INDEX)) {
			try {
				migrateCatalogSorting(null);
				dbInstance.commitAndCloseSession();
				log.info("All catalog order indices migrated.");
				log.info("Total: {}", migrationCounter);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}

			uhd.setBooleanDataValue(CATALOG_ORDER_INDEX, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private void migrateCatalogSorting(Long parentID) {
		List<UpgradeCatalogEntry> catalogEntries = getCatalogEntries(parentID);
		AtomicInteger entriesIndex = new AtomicInteger(0);

		for (UpgradeCatalogEntry catalogEntry : catalogEntries) {
			catalogEntry.setPosition(entriesIndex.get());
			dbInstance.getCurrentEntityManager().merge(catalogEntry);
			entriesIndex.incrementAndGet();

			migrationCounter.incrementAndGet();
			if(migrationCounter.get() % 25 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
			if(migrationCounter.get() % 100 == 0) {
				log.info("Catalog: Order indices migrated: {}", migrationCounter);
			}

			if (catalogEntry.getType() == CatalogEntry.TYPE_NODE) {
				migrateCatalogSorting(catalogEntry.getKey());
			}
		}
	}

	private List<UpgradeCatalogEntry> getCatalogEntries(Long parentID) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cei from upgradecatalogentry as cei ")
		.append("where cei.parent");
		if (parentID == null) {
			sb.append(" is null ");
		} else {
			sb.append("=:parentID ");
		}
		sb.append("order by cei.type ASC, cei.name ASC ");

		TypedQuery<UpgradeCatalogEntry> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), UpgradeCatalogEntry.class);
		if (parentID != null) {
			query.setParameter("parentID", parentID);
		}

		List<UpgradeCatalogEntry> entries = query.getResultList();


		Collator collator = Collator.getInstance(I18nModule.getDefaultLocale());
		collator.setStrength(Collator.IDENTICAL);
		Comparator<UpgradeCatalogEntry> comparator = (catalogEntry1, catalogEntry2) -> {
			if ((catalogEntry1.getType() == CatalogEntry.TYPE_NODE && catalogEntry2.getType() == CatalogEntry.TYPE_LEAF)) {
				return -1;
			} else if (catalogEntry1.getType() == CatalogEntry.TYPE_LEAF && catalogEntry2.getType() == CatalogEntry.TYPE_NODE) {
				return 1;
			} else if (catalogEntry1.getType() == CatalogEntry.TYPE_LEAF && catalogEntry2.getType() == CatalogEntry.TYPE_LEAF) {
				if (catalogEntry1.getRepositoryEntry().getEntryStatus().ordinal() > catalogEntry2.getRepositoryEntry().getEntryStatus().ordinal()) {
					return 1;
				} else if (catalogEntry1.getRepositoryEntry().getEntryStatus().ordinal() < catalogEntry2.getRepositoryEntry().getEntryStatus().ordinal()) {
					return -1;
				} 
			} if (catalogEntry1.getName() == null && catalogEntry2.getName() == null) {
				return 0;
			} else if (catalogEntry1.getName() == null && catalogEntry2.getName() != null) {
				return 1;
			} else if (catalogEntry1.getName() != null && catalogEntry2.getName() == null) {
				return -1;
			} else {
				return collator.compare(catalogEntry1.getName(), catalogEntry2.getName());
			}
		};

		Collections.sort(entries, comparator);

		return entries;
	}
}
