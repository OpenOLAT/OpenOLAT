/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_0_3 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_0_3.class);
	
	private static final int BATCH_SIZE = 100;
	private static final String VERSION = "OLAT_19.0.3";

	private static final String UPDATE_DELETED_FOLDER_METADATA = "UPDATE DELETED FOLDER METADATA";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSMetadataDAO metadataDao;

	public OLATUpgrade_19_0_3() {
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
		allOk &= updateDeletedFolderMetadata(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_0_3 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_0_3 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updateDeletedFolderMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(UPDATE_DELETED_FOLDER_METADATA)) {
			try {
				log.info("Start update deleted folder metadata.");
				
				int counter = 0;
				int updated = 0;
				Long lastKey = Long.valueOf(0);
				List<VFSMetadata> metadatas;
				do {
					metadatas = getDeletedMetadata(BATCH_SIZE, lastKey);
					for(int i=0; i<metadatas.size(); i++) {
						VFSMetadata metadata = metadatas.get(i);
						lastKey = metadata.getKey();
						
						VFSItem item = vfsRepositoryService.getItemFor(metadata);
						if (metadata instanceof VFSMetadataImpl metadataImpl 
								&& item instanceof VFSContainer
								&& item.exists()
								&& !item.getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
							metadataImpl.setDeletedBy(null);
							metadataImpl.setDeleted(false);
							metadataImpl.setDeletedDate(null);
							metadataDao.updateMetadata(metadataImpl);
							dbInstance.commit();
							updated++;
						}
						
						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += metadatas.size();
					log.info(Tracing.M_AUDIT, "Update of deleted folder metadata: {} updated, {} processed ({})", updated, metadatas.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (metadatas.size() == BATCH_SIZE);
				
				log.info("Update of deleted folder metadata finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(UPDATE_DELETED_FOLDER_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<VFSMetadata> getDeletedMetadata(int maxResults, Long lastKey) {
		String query = """
				select metadata from filemetadata as metadata 
				where metadata.deleted = true
				  and metadata.directory = true
				  and metadata.key > :lastKey
				order by metadata.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, VFSMetadata.class)
				.setParameter("lastKey", lastKey)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
}
