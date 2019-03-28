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

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_13_0_0_beta9 extends OLATUpgrade {
	
	private static final String VERSION = "OLAT_13.0.0.beta9";
	private static final String MIGRATE_BOOKABLE = "MIGRATE BOOKABLE";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private RepositoryService repositoryService;
	
	public OLATUpgrade_13_0_0_beta9() {
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
		allOk &= migrateBookable(upgradeManager, uhd);
		
		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.audit("Finished OLATUpgrade_13_0_0_beta9 successfully!");
		} else {
			log.audit("OLATUpgrade_13_0_0_beta9 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}

	private boolean migrateBookable(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_BOOKABLE)) {
			try {
				int count = 0;
				List<Long> entryKeys = getRepositoryEntryKeys();
				for(Long entryKey:entryKeys) {
					migrateBookableFlag(entryKey);
					if(++count % 25 == 0) {
						dbInstance.commitAndCloseSession();
					}
				}
				dbInstance.commitAndCloseSession();
			} catch (Exception e) {
				log.error("", e);
				allOk &= false;
			}

			uhd.setBooleanDataValue(MIGRATE_BOOKABLE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateBookableFlag(Long entrykey) {
		RepositoryEntry entry = repositoryService.loadByKey(entrykey);
		if(entry != null && !entry.isBookable() && entry.isAllUsers()) {
			OLATResource resource = entry.getOlatResource();
			// need at least one "not deleted" offer
			boolean bookable = acService.isResourceAccessControled(resource, null);
			if(bookable != entry.isBookable()) {
				entry.setBookable(bookable);
				if(bookable) {
					entry.setAllUsers(false);
					entry.setGuests(false);
				}
				repositoryService.update(entry);
				dbInstance.commit();
			}
		}
	}

	private List<Long> getRepositoryEntryKeys() {
		String q = "select v.key from repositoryentry as v";
		return dbInstance.getCurrentEntityManager()
				.createQuery(q, Long.class)
				.getResultList();
	}
}
