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

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.vfs.manager.VFSRepositoryServiceImpl;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSSuccess;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_0_4 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_0_4.class);
	
	private static final int BATCH_SIZE = 100;
	private static final String VERSION = "OLAT_19.0.4";

	private static final String RESTORE_FROM_TRASH = "RESTORE FROM TRASH";
	private static final String CLEAN_UP_DELETED_METADATA = "CLEAN UP DELETED METADATA";
	private static final String CREATE_MISSING_METADATA = "CREATE MISSING METADATA";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSRepositoryServiceImpl vfsRepositoryService;
	@Autowired
	private VFSMetadataDAO metadataDao;

	public OLATUpgrade_19_0_4() {
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
		allOk &= restoreFromTrash(upgradeManager, uhd);
		allOk &= cleanUpDeletedMetadata(upgradeManager, uhd);
		if (allOk) {
			allOk &= createMissingMetadata(upgradeManager, uhd);
		}

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_0_4 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_0_4 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean restoreFromTrash(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(RESTORE_FROM_TRASH)) {
			try {
				log.info("Start restore from trash.");
				
				int counter = 0;
				int updated = 0;
				Long lastKey = Long.valueOf(0);
				List<VFSMetadata> metadatas;
				do {
					metadatas = getMetadataInTrash(BATCH_SIZE, lastKey);
					for(int i=0; i<metadatas.size(); i++) {
						VFSMetadata metadata = metadatas.get(i);
						lastKey = metadata.getKey();
						VFSItem item = vfsRepositoryService.getItemFor(metadata);
						if (metadata instanceof VFSMetadataImpl metadataImpl 
								&& item instanceof VFSLeaf
								&& item.exists()) {
							// Parent is thrash folder. Use the grand parent as target.
							VFSMetadata parentMetadata = metadataImpl.getParent();
							if (parentMetadata instanceof VFSMetadataImpl parentMetadataImpl) {
								VFSMetadata grandParentMetadate = parentMetadataImpl.getParent();
								VFSItem targetItem = vfsRepositoryService.getItemFor(grandParentMetadate);
								if (targetItem instanceof VFSContainer targetContainer
										&& !targetItem.getRelPath().contains(VFSRepositoryService.TRASH_NAME)
										&& targetItem.exists()) {
									// Check if already a new file with the same name in the target folder
									VFSItem resolvedItem = targetContainer.resolve(metadata.getFilename());
									if (resolvedItem == null) {
										List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
										// Previously, not all revisions had the initializedBy set, so that the
										// deletedBy was also missing in the metadata, although it was a deleted file.
										if (revisions.isEmpty()) {
											VFSSuccess restored = null;
											if (item instanceof LocalFileImpl localFile) {
												restored = localFile.restore(targetContainer, false);
											} else {
												restored = item.restore(targetContainer);
											}
											if (VFSSuccess.SUCCESS == restored) {
												log.info("File restored from trash: {}", metadata.getRelativePath() + "/" + metadata.getFilename());
											} else {
												log.warn("File not restored from trash ({}): {}", restored, metadata.getRelativePath() + "/" + metadata.getFilename());
											}
											dbInstance.commit();
											updated++;
										} else {
											log.warn("File not restored from trash (trashed because of revisions): {}", metadata.getRelativePath() + "/" + metadata.getFilename());
										}
									} else {
										log.warn("File not restored from trash (file with same name exists in target): {}", metadata.getRelativePath() + "/" + metadata.getFilename());
									}
								} else {
									log.warn("File not restored from trash (grand parent does not exist): {}", metadata.getRelativePath() + "/" + metadata.getFilename());
								}
							} else {
								log.warn("File not restored from trash (parent does not exist): {}", metadata.getRelativePath() + "/" + metadata.getFilename());
							}
						} else {
							log.warn("File not restored from trash (no leaf or does not exists): {}", metadata.getRelativePath() + "/" + metadata.getFilename());
						}
						
						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += metadatas.size();
					log.info(Tracing.M_AUDIT, "Restore from trash: {} updated, {} processed ({})", updated, metadatas.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (metadatas.size() == BATCH_SIZE);
				
				log.info("Restore from trash finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(RESTORE_FROM_TRASH, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<VFSMetadata> getMetadataInTrash(int maxResults, Long lastKey) {
		String query = """
				select metadata 
				  from filemetadata as metadata
				       left join fetch metadata.parent as parent
				       left join fetch parent.parent as grandParent
				where metadata.deleted = true
				  and metadata.directory = false
				  and metadata.deletedBy is null
				  and metadata.relativePath like '%_ootrash%'
				  and metadata.key > :lastKey
				order by metadata.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, VFSMetadata.class)
				.setParameter("lastKey", lastKey)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean cleanUpDeletedMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CLEAN_UP_DELETED_METADATA)) {
			try {
				log.info("Clean up deleted metadata.");
				
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
						if (metadata instanceof VFSMetadataImpl metadataImpl) {
							if (item.exists()) {
								if (!item.getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
									metadataImpl.setDeletedBy(null);
									metadataImpl.setDeleted(false);
									metadataImpl.setDeletedDate(null);
									metadataDao.updateMetadata(metadataImpl);
									log.info("Metadata of existing file marked as not deleted: {}", metadata.getRelativePath() + "/" + metadata.getFilename());
								} else {
									log.info("Metadata correctly marked as deleted: {}", metadata.getRelativePath() + "/" + metadata.getFilename());
								}
							} else {
								VFSMetadata reloadedMetadata = vfsRepositoryService.getMetadata(metadata);
								// May be deleted with parent directory
								if (reloadedMetadata != null) {
									vfsRepositoryService.deleteMetadata(metadata);
								}
								log.info("Metadata of not existing file deleted: {}", metadata.getRelativePath() + "/" + metadata.getFilename());
							}
							dbInstance.commit();
							updated++;
						}
						
						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += metadatas.size();
					log.info(Tracing.M_AUDIT, "Clean up deleted metadata: {} updated, {} processed ({})", updated, metadatas.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (metadatas.size() == BATCH_SIZE);
				
				log.info("Clean up deleted metadata finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(CLEAN_UP_DELETED_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private List<VFSMetadata> getDeletedMetadata(int maxResults, Long lastKey) {
		String query = """
				select metadata from filemetadata as metadata 
				where metadata.deleted = true
				  and metadata.deletedBy is null
				  and metadata.key > :lastKey
				order by metadata.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, VFSMetadata.class)
				.setParameter("lastKey", lastKey)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private boolean createMissingMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(CREATE_MISSING_METADATA)) {
			try {
				log.info("Create missing metadata.");
				
				vfsRepositoryService.migrateDirectories(new File(FolderConfig.getCanonicalRoot()), false);
				
				log.info("Create missing metadata finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(CREATE_MISSING_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
}
