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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserLifecycleManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_19_0_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_19_0_0.class);
	
	private static final int BATCH_SIZE = 100;

	private static final String VERSION = "OLAT_19.0.0";

	private static final String PLANNED_INACTIVATION_DATE_IDENTITY = "PLANNED INACTIVATION DATE IDENTITY";
	private static final String COURSE_REFERENCES = "COURSES REFERENCES";
	private static final String VFS_DELETED_METADATA = "VFS DELETED METADATA";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSMetadataDAO metadataDao;

	public OLATUpgrade_19_0_0() {
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
		allOk &= updatePlannedInactivationDates(upgradeManager, uhd);
		allOk &= updateCoursesReferences(upgradeManager, uhd);
		// Should be the last one because it can take some time.
		allOk &= updateVfsDeletedMetadata(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_19_0_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_19_0_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean updatePlannedInactivationDates(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(PLANNED_INACTIVATION_DATE_IDENTITY)) {
			allOk &= userLifecycleManager.updatePlannedInactivationDates();
			uhd.setBooleanDataValue(PLANNED_INACTIVATION_DATE_IDENTITY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private boolean updateCoursesReferences(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(COURSE_REFERENCES)) {
			int counter = 0;
			List<Long> courses = getCourseEntries();
			for(Long courseKey:courses) {
				RepositoryEntry courseEntry = repositoryService.loadByKey(courseKey);
				try {
					ICourse course = CourseFactory.loadCourse(courseEntry);
					CourseConfig courseConfig = course.getCourseConfig();
					checkReference(courseEntry, courseConfig.getGlossarySoftKey(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);
					checkReference(courseEntry, courseConfig.getSharedFolderSoftkey(), SharedFolderManager.SHAREDFOLDERREF);
					if((++counter) % 25 == 0) {
						log.info("Courses references check: {} / {}", counter, courses.size());
						dbInstance.commitAndCloseSession();
					}
				} catch (CorruptedCourseException e) {
					log.debug("Course corrupted: {}", courseEntry, e);
				}
			}
			dbInstance.commitAndCloseSession();
			log.info("Courses references check finished.");
			
			uhd.setBooleanDataValue(COURSE_REFERENCES, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}
	
	private void checkReference(RepositoryEntry courseEntry, String softKey, String type) {
		if(!StringHelper.containsNonWhitespace(softKey)) {
			return;
		}
		
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntryBySoftkey(softKey, false);
		if(entry == null) {
			return;
		}

		List<Reference> references = referenceManager.getReferences(courseEntry.getOlatResource(), entry.getOlatResource());
		if(references.isEmpty()) {
			referenceManager.addReference(courseEntry.getOlatResource(), entry.getOlatResource(), type);
		}
		
		// Delete referecens from source Glossary/Shared folder to course
		List<Reference> referencesToDelete = referenceManager.getReferences(entry.getOlatResource(), courseEntry.getOlatResource());
		for(Reference referenceToDelete:referencesToDelete) {
			referenceManager.delete(referenceToDelete);
		}
	}
	
	private List<Long> getCourseEntries() {
		String sb = """
			select re.key from repositoryentry re
			inner join re.olatResource as ores
			where ores.resName = 'CourseModule'
			order by re.key asc""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, Long.class)
				.getResultList();
	}
	
	private boolean updateVfsDeletedMetadata(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		
		if (!uhd.getBooleanDataValue(VFS_DELETED_METADATA)) {
			try {
				log.info("Start migrating deleted vfs metadata.");
				
				prepareWikiFile();
				
				int counter = 0;
				Long lastKey = Long.valueOf(0);
				List<VFSMetadata> metadatas;
				do {
					metadatas = getDeletedMetadata(BATCH_SIZE, lastKey);
					for(int i=0; i<metadatas.size(); i++) {
						VFSMetadata metadata = metadatas.get(i);
						lastKey = metadata.getKey();
						migrateMetadata(metadata);
						if(i % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					counter += metadatas.size();
					log.info(Tracing.M_AUDIT, "Migrated of deleted vfs metadata: {} total processed ({})", metadatas.size(), counter);
					dbInstance.commitAndCloseSession();
				} while (metadatas.size() == BATCH_SIZE);

				log.info("Migration of deleted vfs metadata finished.");
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			
			uhd.setBooleanDataValue(VFS_DELETED_METADATA, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
		return allOk;
	}

	private List<VFSMetadata> getDeletedMetadata(int maxResults, Long lastKey) {
		String query = """
				select metadata from filemetadata as metadata 
				where metadata.deleted = true
				  and metadata.deletedDate is null
				  and metadata.key > :lastKey
				order by metadata.key""";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, VFSMetadata.class)
				.setParameter("lastKey", lastKey)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
	private void migrateMetadata(VFSMetadata metadata) {
		if (metadata.isDirectory()) {
			VFSItem item = vfsRepositoryService.getItemFor(metadata);
			if (item instanceof VFSContainer && !item.getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
				if (item.exists()) {
					unmarkDeleted(metadata);
				} else {
					deleteMetadata(metadata);
				}
			}
			return;
		}
		
		VFSItem vfsItem = vfsRepositoryService.getItemFor(metadata);
		if (vfsItem instanceof VFSLeaf && vfsItem.exists()) {
			unmarkDeleted(metadata);
			return;
		}
		
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		if (revisions.isEmpty()) {
			deleteMetadata(metadata);
			return;
		}
		
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			// The newest / highest revision
			VFSRevision revision = revisions.get(revisions.size() -1);
			File revisionFile = vfsRepositoryService.getRevisionFile(revision);
			if (revisionFile.exists()) {
				try (InputStream in = new FileInputStream(revisionFile);
						BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE);) {
					FileUtils.cpio(bis, vfsLeaf.getOutputStream(false), "Delete file migration");
					vfsLeaf.delete();
					VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
					if (vfsMetadata.getDeletedDate() != null) {
						// The deletedDate is set as of today because we do not want do delete a lot of
						// files directly in the night after the update.
						((VFSMetadataImpl)vfsMetadata).setDeletedDate(new Date());
						((VFSMetadataImpl)vfsMetadata).setDeletedBy(revision.getFileInitializedBy());
						vfsRepositoryService.updateMetadata(vfsMetadata);
					}
					log.info("Revision moved to trash: {}", metadata.getRelativePath() + "/" + metadata.getFilename());
					return;
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
		
		deleteMetadata(metadata);
	}
	private void unmarkDeleted(VFSMetadata metadata) {
		if (metadata instanceof VFSMetadataImpl metadataImpl) {
			metadataImpl.setDeletedBy(null);
			metadataImpl.setDeleted(false);
			metadataImpl.setDeletedDate(null);
			metadataDao.updateMetadata(metadataImpl);
			log.info("Metadata of existing file marked as not deleted: {}", metadataImpl.getRelativePath() + "/" + metadataImpl.getFilename());
		}
	}

	private void deleteMetadata(VFSMetadata metadata) {
		VFSMetadata reloadedMetadata = vfsRepositoryService.getMetadata(metadata);
		// May be deleted with parent directory
		if (reloadedMetadata != null) {
			vfsRepositoryService.deleteMetadata(metadata);
		}
		log.info("Metadata of not existing file deleted: {}", metadata.getRelativePath() + "/" + metadata.getFilename());
	}
	
	private void prepareWikiFile() {
		log.info("Start prepare wiki vfs metadata.");
		
		int counter = 0;
		List<VFSMetadata> metadatas;
		do {
			metadatas = getWikiMetadata(BATCH_SIZE);
			for(int i=0; i<metadatas.size(); i++) {
				VFSMetadata metadata = metadatas.get(i);
				if (metadata instanceof VFSMetadataImpl impl) {
					impl.setDeleted(false);
					impl.setDeletedBy(null);
					impl.setDeletedDate(null);
					vfsRepositoryService.updateMetadata(impl);
				}
				if(i % 25 == 0) {
					dbInstance.commitAndCloseSession();
				}
			}
			counter += metadatas.size();
			log.info(Tracing.M_AUDIT, "Prepared wiki vfs metadata: {} total processed ({})", metadatas.size(), counter);
			dbInstance.commitAndCloseSession();
		} while (metadatas.size() == BATCH_SIZE);
		
		
		log.info("End prepare wiki vfs metadata.");
	}

	private List<VFSMetadata> getWikiMetadata(int maxResults) {
		String query = """
				select metadata from filemetadata as metadata 
				where metadata.deleted = true
				  and metadata.deletedDate is null
				  and metadata.relativePath like '%wiki%'
				  and (metadata.filename like '%.wp' or metadata.filename like '%.properties')
				  """;
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, VFSMetadata.class)
				.setMaxResults(maxResults)
				.getResultList();
	}
	
}
