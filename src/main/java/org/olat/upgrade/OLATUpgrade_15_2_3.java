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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_15_2_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_15_2_3.class);

	private static final String VERSION = "OLAT_15.2.3";
	private static final String CLEAN_UP_REPO_TO_GROUP_RELATION = "CLEAN UP REPOSITORY ENTRY TO GROUP RELATION";

	private static final int BATCH_SIZE = 1000;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	public OLATUpgrade_15_2_3() {
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
		allOk &= cleanBrokenRepositoryEntryToGroupRelations(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_15_2_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_15_2_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}

	private boolean cleanBrokenRepositoryEntryToGroupRelations(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_UP_REPO_TO_GROUP_RELATION)) {
			
			int counter = 0;
			List<Long> brokenRelationKeys;
			do {
				// start always at 0 because the relations are deleted
				brokenRelationKeys = repositoryEntryRelationDao.getBrokenGroupDependencies(0, BATCH_SIZE);
				cleanBrokenRepositoryEntryToGroupRelations(brokenRelationKeys);
				counter += brokenRelationKeys.size();
				log.info(Tracing.M_AUDIT, "Clean up broken repository entry to group relations: {}, total processed ({})", brokenRelationKeys.size(), counter);
				dbInstance.commitAndCloseSession();
			} while(brokenRelationKeys.size() == BATCH_SIZE);
			
			uhd.setBooleanDataValue(CLEAN_UP_REPO_TO_GROUP_RELATION, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void cleanBrokenRepositoryEntryToGroupRelations(List<Long> relationKeysToDelete) {
		int count = 0;
		for(Long relationKeyToDelete:relationKeysToDelete) {
			try {
				RepositoryEntryToGroupRelation relation = repositoryEntryRelationDao.loadRelationByKey(relationKeyToDelete);
				if(relation != null) {
					log.info("Remove relation {} from entry: {} to group: {}", relation.getKey(), relation.getEntry().getKey(), relation.getGroup().getKey());
					repositoryEntryRelationDao.removeRelation(relation);
				}
				
				if(count++ % 20 == 0) {
					dbInstance.commitAndCloseSession();
				} else {
					dbInstance.commit();
				}
			} catch (Exception e) {
				dbInstance.rollbackAndCloseSession();
				log.error("", e);
			}
		}
		dbInstance.commitAndCloseSession();
	}
}
