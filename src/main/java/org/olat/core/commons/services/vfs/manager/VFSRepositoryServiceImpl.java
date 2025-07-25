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
package org.olat.core.commons.services.vfs.manager;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.ImageUtils;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailService;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.VFSContextInfoResolver;
import org.olat.core.commons.services.vfs.VFSFilterKeys;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSRevisionRef;
import org.olat.core.commons.services.vfs.VFSStatistics;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoUnknown;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoUnknownPathResolver;
import org.olat.core.commons.services.vfs.manager.MetaInfoReader.Thumbnail;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.vfs.model.VFSRevisionImpl;
import org.olat.core.commons.services.vfs.model.VFSTransientMetadata;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSExternalItem;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.version.RevisionFileImpl;
import org.olat.core.util.vfs.version.VersionsFileImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSRepositoryServiceImpl implements VFSRepositoryService, GenericEventListener, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(VFSRepositoryServiceImpl.class);
	private final OLATResourceable fileSizeSubscription = OresHelper.createOLATResourceableType("UpdateFileSizeAsync");
	private final OLATResourceable incrementFileDownload = OresHelper.createOLATResourceableType("IncrementFileDownloadAsync");
	private static final String CANONICAL_ROOT_REL_PATH = "/";
	private static final Comparator<VFSRevision> VERSION_ASC = comparing(VFSRevision::getRevisionNr)
				.thenComparing(comparing(VFSRevision::getRevisionTempNr, nullsFirst(Integer::compareTo)));
	private static final String POSTER_PREFIX = "._oo_poster_";
	
	private CacheWrapper<String,VFSItem> inMemoryItems;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ImageService imageService;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private VFSRevisionDAO revisionDao;
	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private VFSStatsDAO statsDao;
	@Autowired
	private VFSThumbnailDAO thumbnailDao;
	@Autowired
	private VFSRepositoryModule vfsModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private VFSVersionModule versionModule;
	@Autowired
	private FolderLicenseHandler licenseHandler;
	@Autowired
	private ThumbnailService thumbnailService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private BaseSecurity securityManager;
	// Autowired liste by setVfsContextInfoResolver() method
	private List<VFSContextInfoResolver> vfsContextInfoResolver;

	@Override
	public void afterPropertiesSet() throws Exception {
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, fileSizeSubscription);
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, incrementFileDownload);
		inMemoryItems = coordinatorManager.getCoordinator().getCacher().getCache("VFSRepository", "inMemoryItems");
	}

	@Override
	public void event(Event event) {
		if(event instanceof AsyncFileSizeUpdateEvent) {
			processFileSizeUpdateEvent((AsyncFileSizeUpdateEvent)event);
		} else if(event instanceof AsyncIncrementFileDownloadEvent) {
			processIncrementFileDownnload((AsyncIncrementFileDownloadEvent)event);
		}
	}
	
	private void processFileSizeUpdateEvent(AsyncFileSizeUpdateEvent event) {
		String relativePath = event.getRelativePath();
		String filename = event.getFilename();
		updateFileSize(relativePath, filename);
	}

	@Override
	public void updateParentLastModified(VFSMetadata metadata) {
		if (metadata == null) {
			return;
		}
		
		VFSMetadata parentMetadata = getParentMetadata(metadata);
		if (parentMetadata != null) {
			updateFileSize(parentMetadata.getRelativePath(), parentMetadata.getFilename());
		}
	}

	private void updateFileSize(String relativePath, String filename) {
		File file = toFile(relativePath, filename);
		if(file.exists()) {
			try {
				Date lastModified = new Date(file.lastModified());
				metadataDao.updateMetadata(file.length(), lastModified, relativePath, filename);
				dbInstance.commit();
			} catch (Exception e) {
				log.error("Cannot update file size of: {} {}", relativePath, filename, e);
			}
		}
	}
	
	private void processIncrementFileDownnload(AsyncIncrementFileDownloadEvent event) {
		try {
			metadataDao.increaseDownloadCount(event.getRelativePath(), event.getFilename());
			dbInstance.commit();
		} catch (Exception e) {
			log.error("Cannot increment file downloads of: {} {}", event.getRelativePath(), event.getFilename(), e);
		}
	}
	
	@Override
	public VFSMetadata getMetadataByUUID(String uuid) {
		if(StringHelper.containsNonWhitespace(uuid)) {
			VFSMetadata metadata = metadataDao.getMetadata(uuid);
			if(metadata == null && inMemoryItems.containsKey(uuid)) {
				metadata = inMemoryItems.get(uuid).getMetaInfo();
			}
			return metadata;
		}
		return null;
	}

	@Override
	public VFSMetadata getMetadata(VFSMetadataRef ref) {
		return metadataDao.loadMetadata(ref.getKey());
	}
	
	private VFSMetadata getParentMetadata(VFSMetadata metadata) {
		if (metadata instanceof VFSMetadataImpl impl && impl.getParent() != null) {
			return getMetadata(() -> impl.getParent().getKey());
		}
		return null;
	}

	@Override
	public void cleanMetadatas() {
		try {
			int loop = 0;
			int counter = 0;
			int processed = 0;
			int batchSize = 10000;
			int maxLoops = 10000;// allow 100'000'000 rows, but prevent an infinite loop in case
			int totalDeleted = 0;
			List<VFSMetadata> metadata;
			do {
				metadata = metadataDao.getMetadatas(counter, batchSize);
				int deleted = 0;
				for(VFSMetadata data:metadata) {
					// not sure if revision check needed at all
					deleted += checkMetadata(data, true);
				}
				counter += metadata.size() - deleted;
				totalDeleted += deleted;
				if(counter < 0) {
					counter = 0;
				}
				loop++;
				processed += metadata.size();
				log.info("Metadata processed: {}, deleted {}, total metadata processed ({})", metadata.size(), deleted, processed);
				dbInstance.commitAndCloseSession();
			} while(metadata.size() == batchSize && loop < maxLoops);
			
			log.info("Cleanup metadata ended: deleted {}, total metadata processed ({})", totalDeleted, processed);
		} catch (Exception e) {
			dbInstance.closeSession();
			log.error("", e);
		}
	}
	
	private int checkMetadata(VFSMetadata data, boolean preventDeleteIfRevision) {
		int deleted = 0;
		
		VFSItem item = getItemFor(data);
		if(item == null || !item.exists() || item.getName().startsWith("._oo_")) {
			boolean exists = false;
			if (preventDeleteIfRevision) {
				List<VFSRevision> revisions = getRevisions(data);
				for(VFSRevision revision:revisions) {
					File revFile = getRevisionFile(revision);
					exists = revFile != null && revFile.exists();
				}
			}
			
			if(!exists) {
				data = getMetadata(data);
				if(data != null) {
					log.info("Delete metadata and associated: {}/{}", data.getRelativePath(), data.getFilename());
					deleted = deleteMetadata(data);
					dbInstance.commit();
				}
			}
		} else if (item instanceof VFSContainer && data instanceof VFSMetadataImpl metadataImpl) {
			// Sanity check: Delete a container only if it is in a trash.
			// Some metadata are flagged as deleted due to bugs in previous OO releases.
			if (data.isDeleted() && !item.getRelPath().contains(TRASH_NAME)) {
				metadataImpl.setDeletedBy(null);
				metadataImpl.setDeleted(false);
				metadataImpl.setDeletedDate(null);
				metadataDao.updateMetadata(metadataImpl);
				dbInstance.commit();
			}
		}
		
		return deleted;
	}
	
	@Override
	public void synchMetadatas(VFSContainer vfsContainer) {
		if (vfsContainer == null) {
			return;
		}
		
		VFSMetadata vfsMetadata = vfsContainer.getMetaInfo();
		if (vfsMetadata == null) {
			return;
		}
		
		metadataDao.getDescendants(vfsMetadata, null).forEach(metadata -> checkMetadata(metadata, false));
		
		File directory = toFile(vfsMetadata);
		if (directory != null) {
			try {
				migrateDirectories(directory, false);
			} catch (IOException e) {
				dbInstance.closeSession();
				log.error("Error while cleaning metadata", e);
			}
		}
	}

	@Override
	public VFSMetadata getMetadataFor(VFSItem path) {
		if(path instanceof VFSExternalItem externalItem) {
			return externalItem.getMetaInfo();
		}
		
		File file = toFile(path);
		return getMetadataFor(file);
	}

	@Override
	public VFSMetadata getMetadataFor(File file) {
		if(file == null || file.getParentFile() == null) {
			return null;
		}
		
		String relativePath = getRelativePath(file.getParentFile());
		if(relativePath.equals("..")) {
			return null;
		}
		if(relativePath.equals("")) {
			relativePath = CANONICAL_ROOT_REL_PATH;
		}
		
		String filename = file.getName();
		VFSMetadata metadata = metadataDao.getMetadata(relativePath, filename, null);
		if(metadata == null) {
			metadata = createMetadata(file, relativePath, filename);
		} else if(file.isFile() && !metadata.isInTranscoding()
				&& (file.length() != metadata.getFileSize() || isDeletedInconsistent(file, metadata))) {
			AsyncFileSizeUpdateEvent event = new AsyncFileSizeUpdateEvent(relativePath, filename);
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, fileSizeSubscription);
		}
		dbInstance.commit();
		return metadata;
	}

	private boolean isDeletedInconsistent(File file, VFSMetadata metadata) {
		if (metadata.isDeleted()) {
			// Files in trash are deleted and existing
			if (metadata.getRelativePath().indexOf(VFSRepositoryService.TRASH_NAME) > -1) {
				return false;
			}
		}
		return !file.exists() != metadata.isDeleted();
	}

	public VFSMetadata createMetadata(File file, String relativePath, String filename) {
		String uuid = UUID.randomUUID().toString();
		String uri = file.toURI().toString();
		boolean directory = file.isDirectory();
		long size = directory ? 0l : file.length();
		
		VFSMetadata parent = getMetadataFor(file.getParentFile());
		VFSMetadata metadata = metadataDao.createMetadata(uuid, relativePath, filename, new Date(), size, directory, uri, "file", parent);
		updateParentLastModified(metadata);
		return metadata;
	}
	
	@Override
	public String getContextTypeFor(String relativePath, Locale locale) {
		if (relativePath == null) {
			return "No path";
		}
		for (VFSContextInfoResolver resolver : vfsContextInfoResolver) {
			String contextType = resolver.resolveContextTypeName(relativePath, locale);
			if (contextType != null) {
				return contextType;
			}
		}
		return VFSContextInfoUnknownPathResolver.UNKNOWN_TYPE;
	}
	
	@Override
	public VFSContextInfo getContextInfoFor(String relativePath, Locale locale) {
		for (VFSContextInfoResolver resolver : vfsContextInfoResolver) {
			VFSContextInfo contextInfo = resolver.resolveContextInfo(relativePath, locale);
			if (contextInfo != null) {
				return contextInfo;
			}
		}
		return new VFSContextInfoUnknown(VFSContextInfoUnknownPathResolver.UNKNOWN_CONTEXT);				
	}
	
	@Override
	public VFSItem getItemFor(VFSMetadata metadata) {
		if(metadata == null) return null;
		
		if(metadata instanceof VFSTransientMetadata) {
			if(metadata.getUuid() != null) {
				return inMemoryItems.get(metadata.getUuid());
			}
			return null;
		}
		
		File file = toFile(metadata);
		if(file.isDirectory()) {
			return new LocalFolderImpl(file);
		}
		return new LocalFileImpl(file);
	}

	@Override
	public VFSItem getItemFor(String uuid) {
		VFSMetadata metadata = metadataDao.getMetadata(uuid);
		if(metadata == null) {
			return inMemoryItems.get(uuid);
		}
		return getItemFor(metadata);
	}

	@Override
	public void registerInMemoryItem(String uuid, VFSItem item) {
		inMemoryItems.put(uuid, item);
	}

	@Override
	public VFSLeaf getLeafFor(URL url) {
		String filePath = url.getFile();
		if (filePath == null) {
			return null;
		}
		try {
			File file = new File(url.toURI());
			if (!file.exists()) {
				return null;
			}
			return new LocalFileImpl(file);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * This method doesn't create missing database entry.
	 * 
	 * @param file The file
	 * @return A metadata object or null if not found
	 */
	private VFSMetadata loadMetadata(File file) {
		if(file == null) return null;
		
		String relativePath = getRelativePath(file.getParentFile());
		if(relativePath.equals("..")) {
			return null;
		}
		if(relativePath.equals("")) {
			relativePath = CANONICAL_ROOT_REL_PATH;
		}
		
		String filename = file.getName();
		VFSMetadata metadata = metadataDao.getMetadata(relativePath, filename, file.isDirectory());
		if(metadata != null && !metadata.isDirectory() && !metadata.isInTranscoding()
				&& (metadata.getFileSize() != file.length() || file.lastModified() != metadata.getFileLastModified().getTime())) {
			AsyncFileSizeUpdateEvent event = new AsyncFileSizeUpdateEvent(relativePath, filename);
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, fileSizeSubscription);
		}
		return metadata;
	}
	
	private File toFile(VFSItem item) {
		String relPath = item.getRelPath();
		return relPath == null ? null : VFSManager.olatRootFile(relPath);
	}
	
	private File toFile(VFSMetadata metadata) {
		return toFile(metadata.getRelativePath(), metadata.getFilename());
	}
	
	private File toFile(String relativePath, String filename) {
		Path path = Paths.get(folderModule.getCanonicalRoot(), relativePath, filename);
		return path.toFile();
	}

	@Override
	public List<VFSMetadata> getChildren(String relativePath) {
		if(relativePath == null) return new ArrayList<>();
		if(!relativePath.equals(CANONICAL_ROOT_REL_PATH) && relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1, relativePath.length());
		}
		return metadataDao.getMetadatas(relativePath);
	}

	@Override
	public List<VFSMetadata> getChildren(VFSMetadataRef parentMetadata) {
		return metadataDao.getMetadatas(parentMetadata);
	}

	@Override
	public List<VFSMetadata> getDescendants(VFSMetadata parentMetadata, Boolean deleted) {
		return metadataDao.getDescendants(parentMetadata, deleted);
	}

	@Override
	public Long getDescendantsSize(VFSMetadata parentMetadata, Boolean directory, Boolean deleted) {
		Long descendantSize = metadataDao.getDescendantsSize(parentMetadata, directory, deleted);
		Long revisionsSize = revisionDao.getRevisionsSize(parentMetadata, deleted);
		return descendantSize + revisionsSize;
	}

	@Override
	public List<VFSMetadata> getNewest(VFSMetadata ancestorMetadata, int maxResults) {
		File file = toFile(ancestorMetadata);
		String path = getRelativePath(file);
		return metadataDao.getNewest(path, maxResults);
	}

	@Override
	public List<VFSMetadata> getMostDownloaded(VFSMetadata ancestorMetadata, int maxResults) {
		File file = toFile(ancestorMetadata);
		String path = getRelativePath(file);
		return metadataDao.getMostDownloaded(path, maxResults);
	}

	@Override
	public List<String> getRelativePaths(String relPathsSearchString) {
		return metadataDao.getRelativePaths(relPathsSearchString);
	}

	/**
	 * The relative path contains /bcroot/
	 * 
	 * @param file
	 * @return
	 */
	private String getRelativePath(File file) {
		return folderModule.getCanonicalRootPath().relativize(file.toPath()).toString();
	}
	
	private String getContainerRelativePath(VFSLeaf leaf) {
		String relativePath = null;
		
		VFSContainer parent = leaf.getParentContainer();
		if(parent != null && parent.getRelPath() != null) {
			relativePath = parent.getRelPath();
		} else {
			String leafRelPath = leaf.getRelPath();
			if(leafRelPath != null) {
				Path leafPath = Paths.get(this.folderModule.getCanonicalRoot(), leafRelPath);
				relativePath = getRelativePath(leafPath.getParent().toFile());
			}
		}
		
		if(relativePath != null && !relativePath.equals(CANONICAL_ROOT_REL_PATH) && relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1, relativePath.length());
		}
		return relativePath;
	}
	
	private VFSContainer getSecureParentContainer(VFSLeaf leaf) {
		VFSContainer parent = leaf.getParentContainer();
		if(parent != null) {
			return parent;
		}
		
		Path leafPath = Paths.get(this.folderModule.getCanonicalRoot(), leaf.getRelPath());
		File relativePath = leafPath.getParent().toFile();
		return new LocalFolderImpl(relativePath);
	}
	
	@Override
	public VFSMetadata updateMetadata(VFSMetadata data) {
		return metadataDao.updateMetadata(data);
	}

	@Override
	public void itemSaved(VFSLeaf leaf, Identity savedBy) {
		if(leaf == null || leaf.canMeta() != VFSStatus.YES) return; // nothing to do
		
		String relativePath = getContainerRelativePath(leaf);
		Date lastModified = new Date(leaf.getLastModified());
		boolean metadataExists = metadataDao.getMetadata(relativePath, leaf.getName(), false) != null;
		// Ensure the existence of the matadata before the update.
		VFSMetadata vfsMetadata = getMetadataFor(leaf);
		Identity initializedBy = metadataExists? vfsMetadata.getFileInitializedBy(): savedBy;
		metadataDao.updateMetadata(leaf.getSize(), lastModified, initializedBy, savedBy, relativePath, leaf.getName());
		
		// Should not happen, but happens due to errors in other code.
		// So make at least sure that the deleted flag is set,
		// because the updateMetadatae() always set it to false
		if (relativePath.indexOf(VFSRepositoryService.TRASH_NAME) > -1) {
			vfsMetadata = getMetadataFor(leaf);
			if (!vfsMetadata.isDeleted() && vfsMetadata instanceof VFSMetadataImpl impl) {
				impl.setDeleted(true);
				if (impl.getDeletedDate() == null) {
					impl.setDeletedDate(new Date());
				}
				vfsMetadata = metadataDao.updateMetadata(impl);
			}
		}
		dbInstance.commitAndCloseSession();
	}

	protected void deleteRetentionExceededPermanently(Date deletionDateBefore) {
		int count = 0;
		List<VFSMetadata> retentionExceeded = getDeletedDateBeforeMetadatas(deletionDateBefore);
		// Delete first all files and the all folders to prevent recursive mismatches.
		List<VFSContainer> containers = new ArrayList<>();
		for (VFSMetadata metadata : retentionExceeded) {
			VFSItem item = getItemFor(metadata);
			if (item instanceof VFSContainer container) {
				// Sanity check: Delete a container only if it is in a trash.
				// Some metadata are flagged as deleted due to bugs in previous OO releases.
				if (container.getRelPath().contains(TRASH_NAME) || !item.exists()) {
					containers.add(container);
				} else if (metadata instanceof VFSMetadataImpl metadataImpl) {
					metadataImpl.setDeletedBy(null);
					metadataImpl.setDeleted(false);
					metadataImpl.setDeletedDate(null);
					metadataDao.updateMetadata(metadataImpl);
					deleteThumbnailsOfMetadata(metadata);
					log.info(Tracing.M_AUDIT, "Container unmarked from deleted (was not in trash) {} {}", metadata, item);
				}
			} else {
				log.info(Tracing.M_AUDIT, "Delete file from trash: {} / {}", item.getRelPath(), item.getName());
				item.deleteSilently();
				count++;
			}
		}
		
		for (VFSContainer item : containers) {
			log.info(Tracing.M_AUDIT, "Delete directory from trash: {} / {}", item.getRelPath(), item.getName());
			item.deleteSilently();
			count++;
		}
		
		log.info(Tracing.M_AUDIT, "{} items deleted from trash", count);
	}

	protected void deleteExpiredFiles() {
		int count = 0;
		List<VFSMetadata> expiredList = metadataDao.getExpiredMetadatas(new Date());
		for(VFSMetadata metadata:expiredList) {
			VFSItem item = getItemFor(metadata);
			if(item instanceof VFSLeaf leaf) {
				log.info(Tracing.M_AUDIT, "Delete expired file: {} / {}", leaf.getRelPath(), leaf.getName());
				leaf.deleteSilently();
				count++;
			}
		}
		log.info(Tracing.M_AUDIT, "{} expired file(s) deleted", count);
	}
	@Override
	public List<VFSMetadata> getDeletedDateBeforeMetadatas(Date reference) {
		return metadataDao.getDeletedDateBeforeMetadatas(reference);
	}
	
	@Override
	public int deleteMetadata(VFSMetadata data) {
		if(data == null) return 0; // nothing to do
		
		int deleted = 0;
		List<VFSMetadata> children = metadataDao.getMetadatasOnly(data);
		for(VFSMetadata child:children) {
			deleted += deleteMetadata(child);
		}
		
		deleteThumbnailsOfMetadata(data);
		deleteRevisionsOfMetadata(data);
		
		data = dbInstance.getCurrentEntityManager().getReference(VFSMetadataImpl.class, data.getKey());
		metadataDao.removeMetadata(data);
		if(children.isEmpty()) {
			dbInstance.commit();
		} else {
			dbInstance.commitAndCloseSession();
		}
		
		deleted++;
		return deleted;
	}
	
	private void deleteRevisionsOfMetadata(VFSMetadata data) {
		List<VFSRevision> revisions = revisionDao.getRevisionsOnly(data);
		for(VFSRevision revision:revisions) {
			File revFile = getRevisionFile(revision);
			if(revFile != null && revFile.exists()) {
				try {
					Files.delete(revFile.toPath());
				} catch (IOException e) {
					log.error("Cannot delete revision: {}", revFile, e);
				}
			}
			revisionDao.deleteRevision(revision);
		}
		if(!revisions.isEmpty()) {
			dbInstance.commit();
		}
	}
	
	private void deleteThumbnailsOfMetadata(VFSMetadata data) {
		boolean hasThumbnailMetadata = false;
		List<VFSThumbnailMetadata> thumbnails = thumbnailDao.loadByMetadata(data);
		for(VFSThumbnailMetadata thumbnail:thumbnails) {
			VFSItem item = VFSManager.olatRootLeaf("/" + data.getRelativePath(), thumbnail.getFilename());
			if(item != null && item.exists()) {
				if(item instanceof LocalFileImpl) {
					File thumbnailFile = ((LocalFileImpl)item).getBasefile();
					try {
						Files.delete(thumbnailFile.toPath());
					} catch (IOException e) {
						log.error("Cannot delete thumbnail: {}", thumbnailFile, e);
					}
					
					VFSMetadata thumbnailMetadata = metadataDao.getMetadata(data.getRelativePath(), thumbnail.getFilename(), false);
					if(thumbnailMetadata != null) {
						metadataDao.removeMetadata(thumbnailMetadata);
						hasThumbnailMetadata = true;
					}
				} else {
					item.deleteSilently();
				}
			}
			thumbnailDao.removeThumbnail(thumbnail);
		}
		if(!thumbnails.isEmpty() || hasThumbnailMetadata) {
			dbInstance.commit();
		}
	}

	@Override
	public void deleteMetadata(File file) {
		VFSMetadata metadata = loadMetadata(file);
		if(metadata != null) {
			deleteMetadata(metadata);
		}
	}
	
	@Override
	public void markAsDeleted(Identity doer, VFSMetadata undeletedMetadata, File deletedFile) {
		if (undeletedMetadata instanceof VFSMetadataImpl) {
			VFSMetadataImpl metadata = (VFSMetadataImpl)undeletedMetadata;
			
			// Delete the metadata as long as the metadata have the original values (path, filename).
			// It's easier to delete and recreate instead of moving.
			deleteThumbnailsOfMetadata(metadata);
			
			moveRevisionsToTrash(metadata, deletedFile);
			
			if (!metadata.isDeleted() || metadata.getDeletedDate() == null) {
				metadata.setDeletedBy(doer);
				metadata.setDeleted(true);
				metadata.setDeletedDate(new Date());
				metadata = (VFSMetadataImpl)metadataDao.updateMetadata(metadata);
				log.debug("File marked as deleted {} {}", undeletedMetadata, deletedFile);
			}
			
			updateParent(metadata, deletedFile);
			dbInstance.intermediateCommit();
		}
	}
	
	@Override
	public void unmarkFromDeleted(Identity doer, VFSMetadata deletedMetadata, File restoredFile) {
		if (deletedMetadata instanceof VFSMetadataImpl) {
			VFSMetadataImpl metadata = (VFSMetadataImpl)deletedMetadata;
			deleteThumbnailsOfMetadata(metadata);
			moveRevisionsFromTrash(metadata, restoredFile);
			
			if (metadata.isDeleted() || metadata.getDeletedDate() != null) {
				metadata.setDeletedBy(null);
				metadata.setDeleted(false);
				metadata.setDeletedDate(null);
				metadata = (VFSMetadataImpl)metadataDao.updateMetadata(metadata);
				log.debug("File unmarked from deleted {} {}", deletedMetadata, restoredFile);
			}
			
			updateParent(metadata, restoredFile);
			dbInstance.intermediateCommit();
		}
	}

	private void updateParent(VFSMetadataImpl metadata, File file) {
		String prevFilename = metadata.getFilename();
		String filename = file.getName();
		String prevUri = metadata.getUri();
		String uri = file.toURI().toString();
		String prevRelativePath = metadata.getRelativePath();
		String relativePath = getRelativePath(file.getParentFile());
		
		if (!Objects.equals(prevFilename, filename)
				|| !Objects.equals(prevUri, uri)
				|| !Objects.equals(prevRelativePath, relativePath)) {
			VFSMetadata parent = getMetadataFor(file.getParentFile());
			metadata.setParent(parent);
			
			metadata.setFilename(filename);
			metadata.setRelativePath(relativePath);
			metadata.setUri(uri);
			
			VFSMetadata updatedMetadata = metadataDao.updateMetadata(metadata);
			log.debug("File parent updated {} {}", updatedMetadata, file);
			
			updateChildrenPaths(updatedMetadata, updatedMetadata.isDirectory(), prevUri, uri, prevRelativePath, relativePath, filename, true);
		}
	}
	
	private void moveRevisionsToTrash(VFSMetadata metadata, File deletedFile) {
		// Revisions have to be moved if file is directly in the trash folder
		// The filename of the revision has to be renamed accordingly to the deleted file.
		File parent = deletedFile.getParentFile();
		if (parent == null || !VFSRepositoryService.TRASH_NAME.equals(parent.getName())) {
			return;
		}
		
		List<VFSRevision> revisions = getRevisions(metadata);
		if (revisions == null || revisions.isEmpty()) {
			return;
		}
		
		Map<String, String> filenamePrevToNew = new HashMap<>(1);
		for (VFSRevision revision : revisions) {
			String prevRevFilename = metadata.getFilename();
			String newRevFilename = filenamePrevToNew.get(prevRevFilename);
			// Several revisions may have the same filename.
			// Move the file once, but change the filename in all revisions.
			if (newRevFilename != null) {
				((VFSRevisionImpl)revision).setFilename(newRevFilename);
				revisionDao.updateRevision(revision);
			} else {
				File revisionFile = getRevisionFile(revision);
				if (revisionFile != null && revisionFile.exists()) {
					newRevFilename = generateFilenameForRevision(deletedFile, revision.getRevisionNr(), revision.getRevisionTempNr());
					File newRevFile = new File(parent, newRevFilename);
					revisionFile.renameTo(newRevFile);
					((VFSRevisionImpl)revision).setFilename(newRevFilename);
					revisionDao.updateRevision(revision);
					filenamePrevToNew.put(prevRevFilename, newRevFilename);
				}
			}
		}
	}
	
	private void moveRevisionsFromTrash(VFSMetadata metadata, File restoredFile) {
		File parent = restoredFile.getParentFile();
		if (parent == null || !metadata.getRelativePath().contains(VFSRepositoryService.TRASH_NAME)) {
			return;
		}
		
		List<VFSRevision> revisions = getRevisions(metadata);
		if (revisions == null || revisions.isEmpty()) {
			return;
		}
		
		Map<String, String> filenamePrevToNew = new HashMap<>(1);
		for (VFSRevision revision : revisions) {
			String prevRevFilename = metadata.getFilename();
			String newRevFilename = filenamePrevToNew.get(prevRevFilename);
			// Several revisions may have the same filename.
			// Move the file once, but change the filename in all revisions.
			if (newRevFilename != null) {
				((VFSRevisionImpl)revision).setFilename(newRevFilename);
				revisionDao.updateRevision(revision);
			} else {
				File revisionFile = getRevisionFile(revision);
				if (revisionFile != null && revisionFile.exists()) {
					newRevFilename = generateFilenameForRevision(restoredFile, revision.getRevisionNr(), revision.getRevisionTempNr());
					File newRevFile = new File(parent, newRevFilename);
					revisionFile.renameTo(newRevFile);
					((VFSRevisionImpl)revision).setFilename(newRevFilename);
					revisionDao.updateRevision(revision);
					filenamePrevToNew.put(prevRevFilename, newRevFilename);
				}
			}
		}
	}

	@Override
	public void cleanTrash(Identity identity, VFSMetadata trashMetadata) {
		if (trashMetadata == null || !trashMetadata.isDeleted()) {
			return;
		}
		
		log.debug("Clean trash {}", trashMetadata);
		List<VFSMetadata> descendants = getDescendants(trashMetadata, null);
		for (VFSMetadata vfsMetadata : descendants) {
			// Is it a sub trash?
			if (VFSRepositoryService.TRASH_NAME.equals(vfsMetadata.getFilename())) {
				// Get the children in the sub trash ...
				List<VFSMetadata> subtrashChildrenMetadatas = getChildren(vfsMetadata);
				for (VFSMetadata subtrashChildMetadata : subtrashChildrenMetadatas) {
					VFSItem itemInSubtrash = getItemFor(subtrashChildMetadata);
					File fileInSubtrash = toFile(itemInSubtrash);
					if (itemInSubtrash != null && fileInSubtrash.exists()) {
						File parentDir = fileInSubtrash.getParentFile().getParentFile();
						if (parentDir.exists()) {
							// ... and move it to the parent directory.
							// The parent directory is in a trash itself.
							// before: dir1/.ootrash/dir2/dir3/.ootrash/file1.txt
							//  after: dir1/.ootrash/dir2/dir3/file1.txt
							VFSContainer parentContainer = new LocalFolderImpl(parentDir);
							String filenameInParent = VFSManager.similarButNonExistingName(parentContainer, itemInSubtrash.getName(), "_");
							File fileInParent = new File(parentDir, filenameInParent);
							boolean renamed = fileInSubtrash.renameTo(fileInParent);
							if (renamed) {
								moveRevisionsFromTrash(subtrashChildMetadata, fileInParent);
								updateParent((VFSMetadataImpl)subtrashChildMetadata, fileInParent);
							}
						}
					}
				}
				
				// Delete sub trash
				VFSItem subtrashItem = getItemFor(vfsMetadata);
				if (subtrashItem != null) {
					subtrashItem.deleteSilently();
				}
			}
		}
	}

	@Override
	public void copyBinaries(VFSMetadata metadata, InputStream in) {
		if(in == null) return;
		
		MetaInfoReader reader = new MetaInfoReader((VFSMetadataImpl)metadata, licenseService, securityManager);
		reader.fromBinaries(in);
	}

	@Override
	public void copyTo(VFSItem source, VFSItem target, VFSContainer parentTarget, Identity savedBy) {
		if(source.canMeta() != VFSStatus.YES || target.canMeta() != VFSStatus.YES) return;
		
		VFSMetadata sourceMetadata = source.getMetaInfo();
		if(sourceMetadata != null) {
			File targetFile = toFile(target);
			if(targetFile != null) {
				VFSMetadata targetMetadata = loadMetadata(targetFile);
				if(targetMetadata == null) {
					VFSMetadata parentMetadata = getMetadataFor(parentTarget);
					String relativePath = getRelativePath(targetFile.getParentFile());
					targetMetadata = metadataDao.createMetadata(UUID.randomUUID().toString(), relativePath,
							targetFile.getName(), new Date(), target instanceof VFSContainer ? 0 : targetFile.length(),
							target instanceof VFSContainer, targetFile.toURI().toString(), "file", parentMetadata);
				}
				targetMetadata.copyValues(sourceMetadata, true);
				if(source.canVersion() == VFSStatus.YES || target.canVersion() == VFSStatus.YES) {
					targetMetadata.setRevisionComment(sourceMetadata.getRevisionComment());
					targetMetadata.setRevisionNr(sourceMetadata.getRevisionNr());
					targetMetadata.setRevisionTempNr(sourceMetadata.getRevisionTempNr());
					copyRevisions(sourceMetadata, targetMetadata, savedBy);
				}
				metadataDao.updateMetadata(targetMetadata);
				
				updateParentLastModified(sourceMetadata);
			}
		}
	}

	private boolean copyRevisions(VFSMetadata sourceMetadata, VFSMetadata targetMetadata, Identity savedBy) {
		List<VFSRevision> sourceRevisions = getRevisions(sourceMetadata);

		boolean allOk = true;
		for (VFSRevision sourceRevision : sourceRevisions) {
			VFSLeaf sourceRevFile = getRevisionLeaf(sourceMetadata, (VFSRevisionImpl)sourceRevision);
			if(sourceRevFile != null && sourceRevFile.exists()) {
				VFSRevision targetRevision = revisionDao.createRevisionCopy(sourceRevision.getFileInitializedBy(),
						sourceRevision.getFileLastModifiedBy(), sourceRevision.getRevisionComment(), targetMetadata,
						sourceRevision);
				VFSLeaf targetRevFile = getRevisionLeaf(targetMetadata, (VFSRevisionImpl)targetRevision);
				VFSManager.copyContent(sourceRevFile, targetRevFile, true, savedBy);
			}
		}
		return allOk;
	}

	@Override
	public VFSMetadata rename(VFSItem item, String newName) {
		VFSMetadata metadata = getMetadataFor(item);
		
		// Is there already a metadata from an other file with the same name
		VFSMetadata currentMetadata = metadataDao.getMetadata(metadata.getRelativePath(), newName, (item instanceof VFSContainer));
		if(currentMetadata != null && !currentMetadata.equals(metadata)) {
			// Delete first all children metadata
			if(currentMetadata.isDirectory()) {
				List<VFSMetadata> children = metadataDao.getMetadatasOnly(currentMetadata);
				for(VFSMetadata child:children) {
					deleteMetadata(child);
				}
			}
			
			metadata.copyValues(currentMetadata, false);
			deleteThumbnailsOfMetadata(currentMetadata);
			deleteRevisionsOfMetadata(currentMetadata);
			currentMetadata = dbInstance.getCurrentEntityManager()
				.getReference(VFSMetadataImpl.class, currentMetadata.getKey());
			metadataDao.removeMetadata(currentMetadata);
			
			updateParentLastModified(currentMetadata);
		}
		
		deleteThumbnailsOfMetadata(metadata);
		
		String prevUri = metadata.getUri();
		String prevRelativePath = metadata.getRelativePath();
		Path newFile = Paths.get(folderModule.getCanonicalRoot(), metadata.getRelativePath(), newName);
		((VFSMetadataImpl)metadata).setFilename(newName);
		String uri = newFile.toFile().toURI().toString();
		((VFSMetadataImpl)metadata).setUri(uri);
		
		List<VFSRevision> revisions = getRevisions(metadata);
		for(VFSRevision revision:revisions) {
			VFSLeaf revFile = getRevisionLeaf(metadata, (VFSRevisionImpl)revision);
			if(revFile != null && revFile.exists()) {
				String newRevFilename = generateFilenameForRevision(newName, revision.getRevisionNr(), revision.getRevisionTempNr());
				revFile.rename(newRevFilename);
				((VFSRevisionImpl)revision).setFilename(newRevFilename);
				revisionDao.updateRevision(revision);
			}
		}
		VFSMetadata updateMetadata = metadataDao.updateMetadata(metadata);
		
		updateChildrenPaths(updateMetadata, metadata.isDirectory(), prevUri, uri, prevRelativePath, metadata.getRelativePath(), metadata.getFilename(), false);
		
		return updateMetadata;
	}

	private void updateChildrenPaths(VFSMetadataRef metadata, boolean directory, String prevUri, String uri,
			String prevRelativePath, String relativePath, String filename, boolean updateDeleted) {
		if (directory) {
			List<VFSMetadata> children = metadataDao.getMetadatasOnly(metadata);
			for(VFSMetadata child:children) {
				if (updateDeleted || !child.isDeleted()) {
					String childUri = child.getUri();
					if (StringHelper.containsNonWhitespace(childUri) && childUri.startsWith(prevUri)) {
						uri = uri.endsWith("/")? uri: uri + "/";
						childUri = uri + childUri.substring(prevUri.length());
						((VFSMetadataImpl)child).setUri(childUri);
					}
					
					String childRelativePath = child.getRelativePath();
					if (StringHelper.containsNonWhitespace(childRelativePath) && childRelativePath.startsWith(prevRelativePath)) {
						String childRelPathEnd = childRelativePath.substring(prevRelativePath.length() + 1);
						int nextSlash = childRelPathEnd.indexOf("/");
						if (nextSlash > -1) {
							childRelativePath = relativePath + "/" + filename + childRelPathEnd.substring(nextSlash);
						} else {
							childRelativePath = relativePath + "/" + filename;
						}
						
						
						((VFSMetadataImpl)child).setRelativePath(childRelativePath);
					}
					
					metadataDao.updateMetadata(child);
					log.debug("File path of child updated {}", metadata);
					updateChildrenPaths(child,  child.isDirectory(), prevUri, uri, prevRelativePath, relativePath, filename, updateDeleted);
				}
			}
		}
	}

	@Override
	public void increaseDownloadCount(VFSLeaf item) {
		String relPath = getContainerRelativePath(item);
		if(StringHelper.containsNonWhitespace(relPath)) {
			AsyncIncrementFileDownloadEvent event = new AsyncIncrementFileDownloadEvent(relPath, item.getName());
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, incrementFileDownload);
		}
	}
	
	@Override
	public VFSLeaf getThumbnail(VFSLeaf file, VFSMetadata metadata, int maxWidth, int maxHeight, boolean fill) {
		VFSLeaf thumbnailLeaf = null;
		
		VFSContainer parentContainer = getSecureParentContainer(file);
		String relativePath = getContainerRelativePath(file);
		if(relativePath != null) {
			VFSThumbnailMetadata thumbnail = thumbnailDao.findThumbnail(relativePath, file.getName(), fill, maxWidth, maxHeight);
			if(thumbnail == null
					&& !Boolean.TRUE.equals(metadata.getCannotGenerateThumbnails())) {
				thumbnailLeaf = generateThumbnail(file, metadata, fill, maxWidth, maxHeight);
			} else {
				VFSItem item = parentContainer.resolve(thumbnail.getFilename());
				if(item instanceof VFSLeaf leaf) {
					thumbnailLeaf = leaf;
				}
			}
		}
		return thumbnailLeaf;
	}

	@Override
	public VFSLeaf getThumbnail(VFSLeaf file, int maxWidth, int maxHeight, boolean fill) {
		VFSLeaf thumbnailLeaf = null;
		
		VFSContainer parentContainer = getSecureParentContainer(file);
		String relativePath = getContainerRelativePath(file);
		if(relativePath != null) {
			VFSThumbnailMetadata thumbnail = thumbnailDao.findThumbnail(relativePath, file.getName(), fill, maxWidth, maxHeight);
			if(thumbnail == null) {
				VFSMetadata metadata = metadataDao.getMetadata(relativePath, file.getName(), false);
				if(metadata == null) {// fallback and generated the needed database entries
					metadata = getMetadataFor(file);
				}
				if(isThumbnailAvailable(file, metadata)
						&& !Boolean.TRUE.equals(metadata.getCannotGenerateThumbnails())) {
					thumbnailLeaf = generateThumbnail(file, metadata, fill, maxWidth, maxHeight);
				}
			} else {
				VFSItem item = parentContainer.resolve(thumbnail.getFilename());
				if(item instanceof VFSLeaf leaf) {
					thumbnailLeaf = leaf;
				} else if(item == null) {
					thumbnailDao.removeThumbnail(thumbnail);
					dbInstance.commit();// free lock ASAP
				}
			}
		}
		return thumbnailLeaf;
	}
	
	private VFSLeaf generateThumbnail(VFSLeaf file, VFSMetadata metadata, boolean fill, int maxWidth, int maxHeight) {
		VFSContainer parentContainer = getSecureParentContainer(file);
		VFSLeaf poster = getPosterLeaf(file, parentContainer);
		String name = poster != null ? poster.getName() : file.getName();
		String thumbnailName = generateFilenameForThumbnail(name, fill, maxWidth, maxHeight);
		
		VFSLeaf thumbnailLeaf = parentContainer.createChildLeaf(thumbnailName);
		if(thumbnailLeaf == null) {
			// ooops, a thumbnail without a database entry
			VFSItem thumbnailItem = parentContainer.resolve(thumbnailName);
			if(thumbnailItem instanceof VFSLeaf leaf) {
				thumbnailLeaf = leaf;
				String suffix = FileUtils.getFileSuffix(thumbnailLeaf.getName());
				Size finalSize = imageService.getSize(thumbnailLeaf, suffix);
				if(finalSize != null) {
					thumbnailDao.createThumbnailMetadata(metadata, thumbnailName, thumbnailLeaf.getSize(),
							fill, maxWidth, maxHeight, finalSize.getWidth(), finalSize.getHeight());
					dbInstance.commit();
					return thumbnailLeaf;
				}
				if(thumbnailLeaf.exists()) {// unreadable image -> replace it
					thumbnailLeaf.deleteSilently();
					thumbnailLeaf = parentContainer.createChildLeaf(thumbnailName);	
				}
			}
		}
		
		if(thumbnailLeaf != null && thumbnailService.isThumbnailPossible(thumbnailLeaf)) {
			try {
				FinalSize finalSize = thumbnailService.generateThumbnail(poster != null ? poster : file, thumbnailLeaf,
						maxWidth, maxHeight, fill);
				if(finalSize == null) {
					thumbnailLeaf.deleteSilently();
					thumbnailLeaf = null;
					metadata.setCannotGenerateThumbnails(Boolean.TRUE);
					metadataDao.updateMetadata(metadata);
				} else {
					thumbnailDao.createThumbnailMetadata(metadata, thumbnailName, thumbnailLeaf.getSize(),
							fill, maxWidth, maxHeight, finalSize.getWidth(), finalSize.getHeight());
				}
			} catch (CannotGenerateThumbnailException e) {
				metadata.setCannotGenerateThumbnails(Boolean.TRUE);
				metadataDao.updateMetadata(metadata);
				thumbnailLeaf = null;
			}
		}
		return thumbnailLeaf;
	}

	private VFSLeaf getPosterLeaf(VFSLeaf sourceLeaf, VFSContainer parentContainer) {
		String fileNameWithoutSuffix = FileUtils.getFileNameWithoutSuffix(sourceLeaf.getName());
		String posterFileNamePrefix = POSTER_PREFIX + fileNameWithoutSuffix;
		VFSItemFilter vfsItemFilter = vfsItem -> vfsItem.getName().startsWith(posterFileNamePrefix);
		List<VFSItem> items = parentContainer.getItems(vfsItemFilter);
		if (!items.isEmpty() && items.get(0) instanceof VFSLeaf posterLeaf) {
			return posterLeaf;
		}
		return null;
	}

	private String preferedThumbnailType(String extension) {
		if(extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
			return extension;
		}
		if(extension.equalsIgnoreCase("pdf")) {
			return "png";
		}
		return "jpg";
	}

	@Override
	public void storePosterFile(VFSLeaf original, File posterFile) {
		String extension = ImageUtils.getImageExtension(posterFile);
		if (extension == null) {
			log.error("Poster file for source {} has unsupported mime type", original.getName());
			return;
		}
		VFSContainer parentContainer = getSecureParentContainer(original);
		String originalBaseName = FileUtils.getFileNameWithoutSuffix(original.getName());
		String posterName = POSTER_PREFIX + originalBaseName + "." + extension;
		if (parentContainer.createChildLeaf(posterName) instanceof LocalFileImpl targetPosterLeaf) {
			if (!copyContent(posterFile, targetPosterLeaf.getBasefile())) {
				log.error("Could not create poster file for source {}", original.getName());
			}
		}
	}

	@Override
	public void deletePosterFile(VFSLeaf original) {
		VFSContainer parentContainer = getSecureParentContainer(original);
		VFSLeaf posterLeaf = getPosterLeaf(original, parentContainer);
		if (posterLeaf != null) {
			posterLeaf.deleteSilently();
		}
	}

	@Override
	public boolean isThumbnailAvailable(VFSItem item, VFSMetadata metadata) {
		if(metadata == null) return false;
		
		if(metadata.isDirectory() || (metadata.getCannotGenerateThumbnails() != null && metadata.getCannotGenerateThumbnails().booleanValue())) { 
			return false;
		}
		return thumbnailService.isThumbnailPossible((VFSLeaf)item);
	}
	
	@Override
	public boolean isThumbnailAvailable(VFSMetadata metadata) {
		if(metadata == null) return false;
		
		if(metadata.isDirectory() || (metadata.getCannotGenerateThumbnails() != null && metadata.getCannotGenerateThumbnails().booleanValue())) { 
			return false;
		}
		VFSItem item = getItemFor(metadata);
		if(item instanceof VFSLeaf leaf) {
			return thumbnailService.isThumbnailPossible(leaf);
		}
		return false;
	}

	@Override
	public boolean isThumbnailAvailable(VFSItem item) {
		if(item instanceof VFSContainer) return false;
		
		File originFile = toFile(item);
		if(originFile == null || originFile.isHidden()) {
			return false;
		}
		
		VFSMetadata metadata = loadMetadata(originFile);
		if(metadata != null && metadata.getCannotGenerateThumbnails() != null && metadata.getCannotGenerateThumbnails().booleanValue()) {
			return false;
		}
		return thumbnailService.isThumbnailPossible((VFSLeaf)item);
	}

	@Override
	public void resetThumbnails(VFSLeaf file) {
		VFSContainer parentContainer = getSecureParentContainer(file);
		String relativePath = getContainerRelativePath(file);
		if(relativePath == null) return;
		
		List<VFSThumbnailMetadata> thumbnails = thumbnailDao.findThumbnails(relativePath, file.getName());
		for(VFSThumbnailMetadata thumbnail:thumbnails) {
			VFSItem item = parentContainer.resolve(thumbnail.getFilename());
			if(item != null) {
				item.deleteSilently();
			}
			thumbnailDao.removeThumbnail(thumbnail);
		}
		dbInstance.commit();
	}

	@Override
	public void resetThumbnails(File file) {
		if(file.isFile()) {
			VFSLeaf leaf = new LocalFileImpl(file);
			if(leaf.getRelPath() != null) {
				resetThumbnails(leaf);
			}
		} else if(file.isDirectory()) {
			try {
				Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
						VFSLeaf leaf = new LocalFileImpl(path.toFile());
						if(leaf.getRelPath() != null) {
							resetThumbnails(leaf);
						}
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				log.error("", e);
			}	
		}
	}
	
	@Override
	public List<VFSRevision> getRevisions(VFSMetadataRef metadata) {
		return revisionDao.getRevisions(metadata);
	}

	@Override
	public List<VFSRevision> getRevisions(List<VFSMetadataRef> metadatas) {
		return revisionDao.getRevisions(metadatas);
	}
	
	@Override
	public VFSRevision getRevision(VFSRevisionRef ref) {
		return revisionDao.loadRevision(ref.getKey());
	}

	@Override
	public long getRevisionsTotalSize() {
		return revisionDao.calculateRevisionsSize();
	}

	@Override
	public List<VFSMetadataRef> getMetadataWithMoreRevisionsThan(long numOfRevs) {
		return revisionDao.getMetadataWithMoreThan(numOfRevs);
	}

	@Override
	public boolean restoreRevision(Identity identity, VFSRevision revision, String comment) {
		VFSMetadata metadata = ((VFSRevisionImpl)revision).getMetadata();

		boolean allOk = false;
		File currentFile = toFile(metadata);
		if(!currentFile.exists()) {
			// restore a deleted file
			metadata = metadataDao.loadMetadata(metadata.getKey());
			((VFSMetadataImpl)metadata).setDeleted(false);
			metadata = metadataDao.updateMetadata(metadata);
			try {
				VFSLeaf revFile = getRevisionLeaf(metadata, ((VFSRevisionImpl)revision));
				if (FileUtils.copyToFile(revFile.getInputStream(), currentFile, "Restore")) {
					deleteRevisions(metadata, Collections.singletonList(revision));
					allOk = true;
				}
			} catch (IOException e) {
				log.error("", e);
			}
		} else {
		
			String olatLeafRelativePath = getRelativePath(currentFile);
			if(!olatLeafRelativePath.startsWith("/")) { // make the path starts with a "/"
				olatLeafRelativePath = "/".concat(olatLeafRelativePath);
			}
			VFSLeaf currentLeaf = VFSManager.olatRootLeaf(olatLeafRelativePath);
			// add current version to versions file
			if (addToRevisions(currentLeaf, metadata, identity, false, comment, false)) {
				// copy the content of the new file to the old
				VFSLeaf revFile = getRevisionLeaf(metadata, ((VFSRevisionImpl)revision));
				if (VFSManager.copyContent(revFile.getInputStream(), currentLeaf, identity)) {
					metadata = metadataDao.loadMetadata(metadata.getKey());
					((VFSMetadataImpl)metadata).copyValues((VFSRevisionImpl)revision);
					metadata = metadataDao.updateMetadata(metadata);

					// prune revisions now
					int maxNumOfVersions = versionModule.getMaxNumberOfVersions();
					List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
					if(maxNumOfVersions >= 0 && revisions.size() > maxNumOfVersions) {
						int numOfVersionsToDelete = Math.min(revisions.size(), (revisions.size() - maxNumOfVersions));
						if(numOfVersionsToDelete > 0) {
							List<VFSRevision> versionsToDelete = new ArrayList<>(revisions.subList(0, numOfVersionsToDelete));
							deleteRevisions(metadata, revisions, versionsToDelete);
						}
					}
					allOk = true;
				}	
			}
		}
		return allOk;
	}
	
	@Override
	public boolean addVersion(VFSLeaf currentFile, Identity identity, boolean tempVersion, String comment, InputStream newFile) {
		boolean allOk = false;
		VFSMetadata metadata = getMetadataFor(currentFile);
		if (addToRevisions(currentFile, metadata, identity, tempVersion, comment, true)) {
			// copy the content of the new file to the old
			if(newFile instanceof net.sf.jazzlib.ZipInputStream || newFile instanceof java.util.zip.ZipInputStream) {
				newFile = new ShieldInputStream(newFile);
			}
			allOk = VFSManager.copyContent(newFile, currentFile, identity);
		} else {
			log.error("Cannot create a version of this file: {}", currentFile);
		}
		dbInstance.commit();
		return allOk;
	}
	
	private boolean addToRevisions(VFSLeaf currentLeaf, VFSMetadata metadata, Identity identity, boolean tempVersion,
			String comment, boolean pruneRevision) {
		int maxNumOfVersions = versionModule.getMaxNumberOfVersions();
		if(maxNumOfVersions == 0) {
			return true;//deactivated, return all ok
		}
		File currentFile = toFile(currentLeaf);
		if(currentFile == null) {
			return false;
		}
		
		List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
		if (!tempVersion) {
			// Delete the temporary versions if a new stable version is set.
			List<VFSRevision> tempVersions = revisions.stream()
					.filter(rev -> rev.getRevisionTempNr() != null)
					.collect(Collectors.toList());
			deleteRevisions(metadata, revisions, tempVersions);
		}
		
		VFSRevisionImpl lastRevision = (VFSRevisionImpl)getLastRevision(revisions);
		RevisionNrs versionNrs = getNextRevisionNr(lastRevision, metadata.getRevisionTempNr() != null);
		
		boolean sameFile = isSameFile(currentLeaf, metadata, revisions);
		String uuid = sameFile && lastRevision != null 
				? lastRevision.getFilename()
				: generateFilenameForRevision(currentFile, versionNrs.getRevisionNr(), versionNrs.getRevisionTempNr());

		Date lastModifiedDate = metadata.getFileLastModified();
		if(lastModifiedDate == null) {
			lastModifiedDate = new Date(currentFile.lastModified());
		}
		long fileSize = metadata.getFileSize();
		if(fileSize <= 0l) {
			fileSize = currentFile.length();
		}
		
		// Don't make a revision if it is the first stable version after some temporary versions
		// It would be a stable revision of a temporary version. We do not want that.
		boolean noRevisionNeeded = true;
		if (tempVersion || metadata.getRevisionTempNr() == null) {
			VFSRevision newRevision = revisionDao.createRevision(metadata.getFileInitializedBy(),
					metadata.getFileLastModifiedBy(), uuid, versionNrs.getRevisionNr(), versionNrs.getRevisionTempNr(),
					fileSize, lastModifiedDate, metadata.getRevisionComment(), metadata);
			revisions.add(newRevision);
			noRevisionNeeded = false;
		}

		if(!sameFile) {
			resetThumbnails(currentLeaf);
		}

		File revFile = new File(currentFile.getParentFile(), uuid);
		if (sameFile || noRevisionNeeded || copyContent(currentFile, revFile)) {
			if(pruneRevision && !tempVersion && maxNumOfVersions >= 0 && revisions.size() > maxNumOfVersions) {
				int numOfVersionsToDelete = Math.min(revisions.size(), (revisions.size() - maxNumOfVersions));
				if(numOfVersionsToDelete > 0) {
					revisions.sort(VERSION_ASC);
					List<VFSRevision> versionsToDelete = new ArrayList<>(revisions.subList(0, numOfVersionsToDelete));
					deleteRevisions(metadata, revisions, versionsToDelete);
				}
			}
			metadata.setRevisionComment(comment);
			RevisionNrs revisionNrs = getNextRevisionNr(getLastRevision(revisions), tempVersion);
			metadata.setRevisionNr(revisionNrs.getRevisionNr());
			metadata.setRevisionTempNr(revisionNrs.getRevisionTempNr());
			if (metadata instanceof VFSMetadataImpl) {
				((VFSMetadataImpl)metadata).setFileInitializedBy(identity);
			}
			updateMetadata(metadata);
			
			if (!tempVersion) {
				// Delete the temporary versions if a new stable version is set (just in case...)
				revisions = revisionDao.getRevisions(metadata);
				List<VFSRevision> tempVersions = revisions.stream()
						.filter(rev -> rev.getRevisionTempNr() != null)
						.collect(Collectors.toList());
				deleteRevisions(metadata, revisions, tempVersions);
			}
			
			return true;
		} else {
			log.error("Cannot create a version of this file: {}", currentLeaf);
		}
		return false;
	}
	
	/**
	 * The method only copy and overwrite the file.
	 * 
	 * @param currentFile The file to copy
	 * @param targetFile The target
	 * @return true if successful
	 */
	private boolean copyContent(File currentFile, File targetFile) {
		try(InputStream in = new FileInputStream(currentFile);
				OutputStream out = new FileOutputStream(targetFile);
				OutputStream bout = new BufferedOutputStream(out)) {
			FileUtils.cpio(in, bout, "Copy revisions");
			return true;
		} catch(IOException e) {
			log.error("", e);
			return false;
		}
	}
	
	@Override
	public boolean deleteRevisions(Identity identity, List<VFSRevision> revisions) {
		if(revisions == null || revisions.isEmpty()) return true;// ok, nothing to do
		
		VFSMetadata metadata = ((VFSRevisionImpl)revisions.get(0)).getMetadata();
		List<VFSRevision> allRevisions = revisionDao.getRevisions(metadata);
		return deleteRevisions(metadata, allRevisions, revisions);
	}
	
	private boolean deleteRevisions(VFSMetadata metadata, List<VFSRevision> versionsToDelete) {
		List<VFSRevision> allRevisions = revisionDao.getRevisions(metadata);
		return deleteRevisions(metadata, allRevisions, versionsToDelete);
	}
	
	private boolean deleteRevisions(VFSMetadata metadata, List<VFSRevision> allVersions, List<VFSRevision> versionsToDelete) {
		List<VFSRevision> toDelete = new ArrayList<>(versionsToDelete);
		Map<String,VFSRevisionImpl> filenamesToDelete = new HashMap<>(allVersions.size());
		for (VFSRevision versionToDelete : versionsToDelete) {
			VFSRevisionImpl versionImpl = (VFSRevisionImpl) versionToDelete;
			for (Iterator<VFSRevision> allVersionIt = allVersions.iterator(); allVersionIt.hasNext();) {
				VFSRevisionImpl allVersionImpl = (VFSRevisionImpl) allVersionIt.next();
				if (allVersionImpl.getKey().equals(versionImpl.getKey())) {
					allVersionIt.remove();
					break;
				}
			}
			String fileToDelete = versionImpl.getFilename();
			if (fileToDelete != null) {
				filenamesToDelete.put(fileToDelete, versionImpl);
			}
		}

		File directory = toFile(metadata).getParentFile();
		List<VFSRevisionImpl> missingFiles = new ArrayList<>();
		for(VFSRevision survivingVersion:allVersions) {
			VFSRevisionImpl survivingVersionImpl = (VFSRevisionImpl)survivingVersion;
			String revFilename = survivingVersionImpl.getFilename();
			if(revFilename == null || !new File(directory, revFilename).exists()) {
				missingFiles.add(survivingVersionImpl);//file is missing
			} else if(filenamesToDelete.containsKey(revFilename)) {
				filenamesToDelete.remove(revFilename);
			}
		}

		toDelete.addAll(missingFiles);
		for(VFSRevision versionToDelete:toDelete) {
			revisionDao.deleteRevision(versionToDelete);
		}
		
		for(String fileToDelete:filenamesToDelete.keySet()) {
			try {
				File file = new File(directory, fileToDelete);
				Files.deleteIfExists(file.toPath());
			} catch (IOException e) {
				log.error("Cannot the version of a file", e);
			}
		}
		return true;
	}
	
	private VFSRevision getLastRevision(List<VFSRevision> revisions) {
		if (revisions == null || revisions.isEmpty()) return null;
		
		return revisions.stream()
				// sort by revisionNr and revisionTempNr
				.sorted(VERSION_ASC)
				// Get last of the sorted revisions
				.skip(revisions.size() - 1)
				.findFirst()
				.get();
	}
	
	private RevisionNrs getNextRevisionNr(VFSRevision revision, boolean tempVersion) {
		if (revision == null) {
			Integer numberTemp = tempVersion? 1: null;
			return new RevisionNrs(1, numberTemp);
		}
		
		if (tempVersion) {
			Integer numberTemp = revision.getRevisionTempNr() != null
					? revision.getRevisionTempNr().intValue() + 1
					: 1;
			return new RevisionNrs(revision.getRevisionNr(), numberTemp);
		}
		
		return new RevisionNrs(revision.getRevisionNr() + 1, null);
	}
	
	private static final class RevisionNrs {
		
		private final int revisionNr;
		private final Integer revisionTempNr;
		
		public RevisionNrs(int revisionNr, Integer revisionTempNr) {
			this.revisionNr = revisionNr;
			this.revisionTempNr = revisionTempNr;
		}

		public int getRevisionNr() {
			return revisionNr;
		}
		
		public Integer getRevisionTempNr() {
			return revisionTempNr;
		}
		
	}
	
	private boolean isSameFile(VFSLeaf currentFile, VFSMetadata metadata, List<VFSRevision> revisions) {
		boolean same = false;
		if(!revisions.isEmpty()) {
			VFSRevision lastRevision = getLastRevision(revisions);
			if(lastRevision != null) {
				long lastSize = lastRevision.getSize();
				long currentSize = currentFile.getSize();
				if(currentSize == lastSize && currentSize > 0
						&& lastRevision instanceof VFSRevisionImpl
						&& currentFile instanceof LocalFileImpl) {
					VFSRevisionImpl lastRev = ((VFSRevisionImpl)lastRevision);
					LocalFileImpl current = (LocalFileImpl)currentFile;
						//can be the same file
					try {
						VFSLeaf lastRevFile = getRevisionLeaf(metadata, lastRev);
						Checksum cm1 = org.apache.commons.io.FileUtils.checksum(toFile(lastRevFile), new Adler32());
						Checksum cm2 = org.apache.commons.io.FileUtils.checksum(toFile(current) , new Adler32());
						same = cm1.getValue() == cm2.getValue();
					} catch (IOException e) {
						log.debug("Error calculating the checksum of files");
					}	
				}
			}
		}
		return same;
	}
	
	private VFSLeaf getRevisionLeaf(VFSMetadata metadata, VFSRevisionImpl rev) {
		return VFSManager.olatRootLeaf("/" + metadata.getRelativePath(), rev.getFilename());
	}
	
	private Path getRevisionPath(String relativePath, String revFilename) {
		return Paths.get(folderModule.getCanonicalRoot(), relativePath, revFilename);
	}
	
	@Override
	public File getRevisionFile(VFSRevision revision) {
		VFSRevisionImpl rev = (VFSRevisionImpl)revision;
		return getRevisionPath(rev.getMetadata().getRelativePath(), rev.getFilename()).toFile();
	}

	@Override
	public VFSMetadata move(VFSLeaf currentLeaf, VFSLeaf targetLeaf,  Identity author) {
		VFSMetadata metadata = getMetadataFor(currentLeaf);

		File currentFile = toFile(currentLeaf);
		String currentRelativePath = getRelativePath(currentFile.getParentFile());

		File targetFile = toFile(targetLeaf);
		String targetRelativePath = getRelativePath(targetFile.getParentFile());
		String newTargetName = targetFile.getName();
		((VFSMetadataImpl)metadata).setFilename(newTargetName);
		((VFSMetadataImpl)metadata).setRelativePath(targetRelativePath);
		((VFSMetadataImpl)metadata).setUri(targetFile.toURI().toString());
		VFSMetadata targetParent = getMetadataFor(targetFile.getParentFile());
		((VFSMetadataImpl)metadata).setParent(targetParent);
		((VFSMetadataImpl)metadata).setFileInitializedBy(author);

		List<VFSRevision> revisions = getRevisions(metadata);
		for(VFSRevision revision:revisions) {
			VFSRevisionImpl revImpl = (VFSRevisionImpl)revision;
			Path path = getRevisionPath(currentRelativePath, revImpl.getFilename());
			File revFile = path.toFile();
			if(revFile.exists()) {
				String newRevFilename = generateFilenameForRevision(newTargetName, revision.getRevisionNr(), revision.getRevisionTempNr());
				Path targetRevPath = getRevisionPath(targetRelativePath, newRevFilename);
				try {
					Files.move(path, targetRevPath, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					log.error("", e);
				}
				((VFSRevisionImpl)revision).setFilename(newRevFilename);
				revisionDao.updateRevision(revision);
			}
		}
		
		metadata = metadataDao.updateMetadata(metadata);
		return metadata;
	}

	/**
	 * Get the license of the MetaInfo
	 *
	 * @param meta
	 * @return the license or null if no license is stored in the MetaInfo
	 */
	@Override
	public License getLicense(VFSMetadata meta) {
		if (meta == null || meta.isDirectory() || !StringHelper.containsNonWhitespace(meta.getLicenseTypeName())) {
			return null;
		}
		
		String licenseTypeName = meta.getLicenseTypeName();
		LicenseType licenseType = null;
		if(meta.getLicenseType() != null) {
			licenseType = meta.getLicenseType();
		} else {
			licenseType = licenseService.loadLicenseTypeByName(licenseTypeName);
		}
		if (licenseType == null) {
			licenseType = licenseService.createLicenseType(licenseTypeName);
			licenseType.setText(meta.getLicenseText());
			licenseService.saveLicenseType(licenseType);
		}
		License license = licenseService.createLicense(licenseType);
		license.setLicensor(meta.getLicensor());
		if (licenseService.isFreetext(licenseType)) {
			license.setFreetext(meta.getLicenseText());
		}
		return license;
	}
	
	/**
	 * Get the license of the MetaInfo or create a new default license.
	 *
	 * @param meta
	 * @param identity the current user
	 * @return the license
	 */
	@Override
	public License getOrCreateLicense(VFSMetadata meta, Identity identity) {
		if (meta != null && meta.isDirectory()) return null;
		
		License license = getLicense(meta);
		if (license == null) {
			license = licenseService.createDefaultLicense(licenseHandler, identity);
		}
		return license;
	}
	
	@Override
	public void migrate(VFSContainer container, VFSMetadata metadata) {
		if(vfsModule.isMigrated()) return;
		
		File directory = toFile(container);
		if(VFSRepositoryModule.canMeta(directory) == VFSStatus.YES) {
			try {
				migrateDirectories(directory, true);
			} catch (IOException e) {
				log.error("", e);
			}
		} else if(metadata != null) {
			((VFSMetadataImpl)metadata).setMigrated("migrated");
			metadataDao.updateMetadata(metadata);
			dbInstance.commit();
		}
	}
	
	private VFSMetadata checkParentLine(VFSMetadata metadata, VFSMetadata parent) {
		if(metadata == null || parent == null) {
			return metadata;
		}

		VFSMetadata persistedParent = ((VFSMetadataImpl)metadata).getParent();
		String materializedPath = metadataDao.getMaterializedPathKeys((VFSMetadataImpl)parent, (VFSMetadataImpl)metadata);
		if(persistedParent != null && !persistedParent.equals(parent)) {
			((VFSMetadataImpl)metadata).setMaterializedPathKeys(materializedPath);
			((VFSMetadataImpl)metadata).setParent(parent);
			return metadataDao.updateMetadata(metadata);
		}

		String pathKeys = ((VFSMetadataImpl)metadata).getMaterializedPathKeys();
		if(!pathKeys.equals(materializedPath)) {
			((VFSMetadataImpl)metadata).setMaterializedPathKeys(materializedPath);
			return metadataDao.updateMetadata(metadata);
		}
		return metadata;
	}

	public void migrateDirectories(File folder, boolean importFromFile) throws IOException {
		Deque<VFSMetadata> parentLine = new LinkedList<>();
		AtomicInteger migrationCounter = new AtomicInteger(0);
		
		Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				File directory = dir.toFile();
				if(directory.isHidden() || VFSRepositoryModule.canMeta(directory) != VFSStatus.YES) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				if(dir.getNameCount() > 50 || parentLine.size() > 50) {
					log.error("More than 50 directories deep. Stop migrating metadata: {}", directory);
					return FileVisitResult.SKIP_SUBTREE;
				}
				
				VFSMetadata parent = parentLine.peekLast();
				VFSMetadata metadata = migrateMetadata(dir.toFile(), parent, importFromFile);
				metadata = checkParentLine(metadata, parent);
				parentLine.add(metadata);
				dbInstance.commit();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				if(!f.isHidden() && VFSRepositoryModule.canMeta(f) == VFSStatus.YES) {
					VFSMetadata metadata = migrateMetadata(file.toFile(), parentLine.getLast(), importFromFile);
					checkParentLine(metadata, parentLine.getLast());
					
					migrationCounter.incrementAndGet();
					if(migrationCounter.get() % 25 == 0) {
						dbInstance.commitAndCloseSession();
					} else {
						dbInstance.commit();
					}
					if(migrationCounter.get() % 100 == 0) {
						log.info("Metadata: num. of files migrated: {}", migrationCounter);
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				VFSMetadata metadata = parentLine.pollLast();
				if (!importFromFile) {
					if(metadata instanceof VFSMetadataImpl) {
						((VFSMetadataImpl)metadata).setMigrated("migrated");
						metadataDao.updateMetadata(metadata);
					}
					dbInstance.commitAndCloseSession();
				}
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				log.warn("Failed to access file during migration: " + file.toString());
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public VFSMetadata migrate(File file, VFSMetadata parent, boolean importFromFile) {
		String relativePath = getRelativePath(file.getParentFile());
		if(relativePath.equals("..")) {
			return null;
		}
		if(relativePath.equals("")) {
			relativePath = CANONICAL_ROOT_REL_PATH;
		}
		
		String filename = file.getName();
		VFSMetadata metadata = metadataDao.getMetadata(relativePath, filename, file.isDirectory());
		if(metadata == null) {
			if(parent == null) {
				parent = migrate(file.getParentFile(), null, importFromFile);
			}
			metadata = migrateMetadata(file, parent, importFromFile);
		}
		return metadata;
	}
	
	private VFSMetadata migrateMetadata(File file, VFSMetadata parent, boolean importFromFile) {
		VFSMetadata metadata = null;
		String metaPath = getCanonicalMetaPath(file);
		String relativePath = getRelativePath(file.getParentFile());
		if(relativePath.equals("..")) {
			return null;
		}
		if(relativePath.equals("")) {
			relativePath = CANONICAL_ROOT_REL_PATH;
		}
		
		boolean directory = file.isDirectory();
		long size = directory ? 0l : file.length();

		Date fileLastModified = new Date(file.lastModified());
		if(importFromFile && metaPath != null) {
			File metaFile = new File(metaPath);
			if(metaFile.exists()) {
				List<Thumbnail> thumbnails = null;
				VFSMetadataImpl xmlMetadata = new VFSMetadataImpl(); 
				MetaInfoReader metaReader = new MetaInfoReader(xmlMetadata, licenseService, securityManager);
				if(metaReader.parseSAX(metaFile)) {
					xmlMetadata = metaReader.getMetadata();
					thumbnails = metaReader.getThumbnails();
				}
				
				metadata = metadataDao.getMetadata(relativePath, file.getName(), directory);
				if(metadata == null) {
					metadata = metadataDao.createMetadata(xmlMetadata, relativePath, file.getName(), fileLastModified,
							size, directory, file.toURI().toString(), "file", parent);
					if(xmlMetadata.getDownloadCount() > 0) {
						metadataDao.setDownloadCount(metadata, xmlMetadata.getDownloadCount());
					}
				}
				migrateThumbnails(metadata, file, thumbnails);
				
				File versionFile = getVersionFile(file);
				if(versionFile != null) {
					migrateVersions(file, versionFile, metadata);
				}
			}
		} 
		
		if(metadata == null) {
			metadata = metadataDao.getMetadata(relativePath, file.getName(), directory);
			if(metadata == null) {
				metadata = metadataDao.createMetadata(UUID.randomUUID().toString(), relativePath, file.getName(), fileLastModified,
						size, directory, file.toURI().toString(), "file", parent);
			}
		}
		if (!directory) {
			if (metadata instanceof VFSMetadataImpl impl) {
				// Update the size as well (some tolerance to avoid hardware differences).
				if (Math.abs(metadata.getFileSize() - size) > 1000) {
					impl.setFileSize(size);
					impl.setLastModified(new Date());
					metadata = updateMetadata(impl);
				}
			}
		}
		return metadata;
	}

	private String generateFilenameForThumbnail(String originalFilename, boolean fill, int maxWidth, int maxHeight) {
		String extension = FileUtils.getFileSuffix(originalFilename);
		String nameOnly = originalFilename.substring(0, originalFilename.length() - extension.length() - 1);
		String thumbnailExtension = preferedThumbnailType(extension);
		StringBuilder sb = new StringBuilder(128);
		sb.append("._oo_th_").append(fill).append("_").append(maxWidth).append("_").append(maxHeight).append("_");
		
		if(nameOnly.length() + sb.length() + thumbnailExtension.length() > 230) {
			log.info("File name too long: {}", nameOnly);
			int maxLength = 230 - sb.length() - thumbnailExtension.length();
			if(maxLength < 1) {
				maxLength = 1;
			}
			nameOnly = nameOnly.substring(0, maxLength);
		}
		
		sb.append(nameOnly).append(".").append(thumbnailExtension);
		return sb.toString();
	}
	
	/**
	 * @param filename The original file
	 * @param revisionNr The version number
	 * @return A name like ._oo_vr_filename.ext
	 */
	private String generateFilenameForRevision(File file, int revisionNr, Integer revisonTempNr) {
		return generateFilenameForRevision(file.getName(), revisionNr, revisonTempNr);
	}
	
	/**
	 * @param filename The original filename
	 * @param revisionNr The version number
	 * @param revisonTempNr The version temp number
	 * @return A name like ._oo_vr_filename.ext
	 */
	private String generateFilenameForRevision(String filename, int revisionNr, Integer revisonTempNr) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("._oo_vr_").append(revisionNr);
		if (revisonTempNr != null) {
			sb.append("_t").append(revisonTempNr);
		}
		sb.append("_").append(filename);
		return sb.toString();
	}
	
	private void migrateThumbnails(VFSMetadata metadata, File file, List<Thumbnail> thumbnails) {
		if(thumbnails == null || thumbnails.isEmpty()) return;
		
		String name = file.getName();
		// do copy them
		for(Thumbnail thumbnail:thumbnails) {
			try {
				File thumbnailFile = thumbnail.getThumbnailFile();
				if(thumbnailFile.exists()) {
					boolean fill = isFill(thumbnailFile);
					String thumbnailName = generateFilenameForThumbnail(name, fill, thumbnail.getMaxWidth(), thumbnail.getMaxHeight());
					VFSThumbnailMetadata thumbnailMetadata = thumbnailDao.findThumbnail(metadata, fill, thumbnail.getMaxWidth(), thumbnail.getMaxHeight());
					if(thumbnailMetadata == null) {
						thumbnailDao.createThumbnailMetadata(metadata, thumbnailName, thumbnailFile.length(),
								fill, thumbnail.getMaxWidth(), thumbnail.getMaxHeight(),
								thumbnail.getFinalWidth(), thumbnail.getFinalHeight());
						File target = new File(file.getParentFile(), thumbnailName);
						Files.move(thumbnailFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
	
	private boolean isFill(File file) {
		String name = file.getName().toLowerCase();
		String extension = FileUtils.getFileSuffix(name);
		String fillExtension = "xfill.".concat(extension);
		return name.endsWith(fillExtension);
	}
	
	private String getCanonicalMetaPath(File originFile) {
		String canonicalMetaPath;
		if (originFile == null || !originFile.exists()) {
			canonicalMetaPath = null;
		} else {
			String relPath = FolderConfig.getCanonicalRootPath().relativize(originFile.toPath()).toString();
			StringBuilder metaSb = new StringBuilder(128);
			metaSb.append(FolderConfig.getCanonicalMetaRoot()).append("/").append(relPath);
			if (originFile.isDirectory()) {
				metaSb.append("/.xml");
			} else {
				metaSb.append(".xml");
			}
			canonicalMetaPath = metaSb.toString();
		}
		return canonicalMetaPath;
	}
	
	private VFSMetadata migrateVersions(File file, File versionFile, VFSMetadata metadata) {
		VersionsFileImpl versions = (VersionsFileImpl)VFSXStream.read(versionFile);
		List<RevisionFileImpl> revisions = versions.getRevisions();
		if(revisions == null || revisions.isEmpty()) {
			return metadata;
		}
		List<VFSRevision> currentRevisions = revisionDao.getRevisions(metadata);
		if(!currentRevisions.isEmpty()) {
			return metadata;
		}
		
		metadata.setRevisionComment(versions.getComment());
		metadata.setRevisionNr(versions.getRevisionNr());
		metadata = metadataDao.updateMetadata(metadata);
		
		for(RevisionFileImpl revision:revisions) {
			String filename = revision.getFilename();
			File oldOne = new File(versionFile.getParentFile(), filename);
			if(oldOne.exists()) {
				try {
					String newRevisionFilename = generateFilenameForRevision(file, revision.getRevisionNr(), null);
					revisionDao.createRevision(revision.getFileInitializedBy(), revision.getFileLastModifiedBy(),
							newRevisionFilename, revision.getRevisionNr(), null,
							oldOne.length(), revision.getFileLastModified(), revision.getComment(), metadata);
					File target = new File(file.getParentFile(), newRevisionFilename);
					Files.move(oldOne.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
		return metadata;
	}
		
	/**
	 * @param file The regular file
	 * @return The XML versions file
	 */
	private File getVersionFile(File file) {
		if (file == null || !file.exists()) {
			return null;
		}
		String relPath = getRelativePath(file);
		if (relPath == null) {// cannot handle
			return null;
		}
		
		File versionFolder = new File(FolderConfig.getCanonicalVersionRoot(), relPath);
		File fVersion = new File(versionFolder.getParentFile(), file.getName() + ".xml");
		return fVersion.exists() ? fVersion : null;
	}
	
	@Override
	public List<VFSMetadata> getLargestFiles(int maxResult, 
			Date createdAtNewer, Date createdAtOlder, 
			Date editedAtNewer, Date editedAtOlder, 
			Date lockedAtNewer, Date lockedAtOlder,
			String deleted, String locked,
			Integer downloadCount, Long revisionCount,
			Integer size) {
		
		Boolean trashedVal;
		Boolean lockedVal;
		
		if(deleted.equals(VFSFilterKeys.DELETED.name())) {
			trashedVal = true;
		} else if(deleted.equals(VFSFilterKeys.NOT_DELETED.name())) {
			trashedVal = false;
		} else {
			trashedVal = null;
		}
		
		if(locked.equals(VFSFilterKeys.LOCKED.name())) {
			lockedVal = true;
		} else if(locked.equals(VFSFilterKeys.NOT_LOCKED.name())) {
			lockedVal = false;
		} else {
			lockedVal = null;
		}
		
		return metadataDao.getLargest(maxResult, 
				createdAtNewer, createdAtOlder, 
				editedAtNewer, editedAtOlder, 
				lockedAtNewer, lockedAtOlder,
				trashedVal, lockedVal,
				downloadCount, revisionCount, size);
	}
	
	@Override 
	public List<VFSRevision> getLargestRevisions(int maxResults, 
			Date createdAtNewer, Date createdAtOlder, 
			Date editedAtNewer, Date editedAtOlder, 
			Date lockedAtNewer, Date lockedAtOlder,
			String deleted, String locked,
			Integer downloadCount, Long revisionCount,
			Integer size) {

		Boolean trashedVal;
		Boolean lockedVal;
		
		if(deleted.equals(VFSFilterKeys.DELETED.name())) {
			trashedVal = true;
		} else if(deleted.equals(VFSFilterKeys.NOT_DELETED.name())) {
			trashedVal = false;
		} else {
			trashedVal = null;
		}
		
		if(locked.equals(VFSFilterKeys.LOCKED.name())) {
			lockedVal = true;
		} else if(locked.equals(VFSFilterKeys.NOT_LOCKED.name())) {
			lockedVal = false;
		} else {
			lockedVal = null;
		}
		
		return revisionDao.getLargest(maxResults, 
				createdAtNewer, createdAtOlder, 
				editedAtNewer, editedAtOlder, 
				lockedAtNewer, lockedAtOlder,
				trashedVal, lockedVal,
				downloadCount, revisionCount, size);
	}
	
	@Override 
	public VFSStatistics getStatistics(boolean recalculate) {
		VFSStatistics stats;
		if(recalculate) {
			stats = statsDao.createStatistics();
			dbInstance.commitAndCloseSession();
		} else {
			stats = statsDao.getLastStatistics();
			if(stats == null) {
				stats = statsDao.createStatistics();
				dbInstance.commitAndCloseSession();
			}
		}
		return stats;
	}

	/**
	 * Set list of context info resolver. Used to autowire resolvers from various implementers
	 * @param vfsContextInfoResolver
	 */
	@Autowired
	public void setVfsContextInfoResolvers(List<VFSContextInfoResolver> vfsContextInfoResolver) {
		this.vfsContextInfoResolver = vfsContextInfoResolver;
	}
	
}
