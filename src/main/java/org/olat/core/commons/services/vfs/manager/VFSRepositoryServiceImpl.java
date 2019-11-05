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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSRevisionRef;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.manager.MetaInfoReader.Thumbnail;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.vfs.model.VFSRevisionImpl;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.ShieldInputStream;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
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
	
	@Override
	public void afterPropertiesSet() throws Exception {
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, fileSizeSubscription);
		coordinatorManager.getCoordinator().getEventBus().registerFor(this, null, incrementFileDownload);
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
		File file = toFile(event.getRelativePath(), event.getFilename());
		if(file.exists()) {
			try {
				Date lastModified = new Date(file.lastModified());
				metadataDao.updateMetadata(file.length(), lastModified, event.getRelativePath(), event.getFilename());
				dbInstance.commit();
			} catch (Exception e) {
				log.error("Cannot update file size of: " + event.getRelativePath() + " " + event.getFilename(), e);
			}
		}
	}
	
	private void processIncrementFileDownnload(AsyncIncrementFileDownloadEvent event) {
		try {
			metadataDao.increaseDownloadCount(event.getRelativePath(), event.getFilename());
			dbInstance.commit();
		} catch (Exception e) {
			log.error("Cannot increment file downloads of: " + event.getRelativePath() + " " + event.getFilename(), e);
		}
	}
	
	@Override
	public VFSMetadata getMetadataByUUID(String uuid) {
		if(StringHelper.containsNonWhitespace(uuid)) {
			return metadataDao.getMetadata(uuid);
		}
		return null;
	}

	@Override
	public VFSMetadata getMetadata(VFSMetadataRef ref) {
		return metadataDao.loadMetadata(ref.getKey());
	}

	@Override
	public VFSMetadata getMetadataFor(VFSItem path) {
		File file = toFile(path);
		return getMetadataFor(file);
	}

	@Override
	public VFSMetadata getMetadataFor(File file) {
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
			String uuid = UUID.randomUUID().toString();
			String uri = file.toURI().toString();
			boolean directory = file.isDirectory();
			long size = directory ? 0l : file.length();
			
			VFSMetadata parent = getMetadataFor(file.getParentFile());
			metadata = metadataDao.createMetadata(uuid, relativePath, filename, new Date(), size, directory, uri, "file", parent);
		} else if(file.isFile() && (file.length() != metadata.getFileSize() || !file.exists() != metadata.isDeleted())) {
			AsyncFileSizeUpdateEvent event = new AsyncFileSizeUpdateEvent(relativePath, filename);
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, fileSizeSubscription);
		}
		dbInstance.commit();
		return metadata;
	}
	
	@Override
	public VFSItem getItemFor(VFSMetadata metadata) {
		if(metadata == null) return null;
		
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
			return null;
		}
		return getItemFor(metadata);
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
		if(metadata != null && !metadata.isDirectory()
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
	public void itemSaved(VFSLeaf leaf) {
		if(leaf == null || leaf.canMeta() != VFSConstants.YES) return; // nothing to do
		
		String relativePath = getContainerRelativePath(leaf);
		Date lastModified = new Date(leaf.getLastModified());
		metadataDao.updateMetadata(leaf.getSize(), lastModified, relativePath, leaf.getName());
	}

	@Override
	public void deleteMetadata(VFSMetadata data) {
		if(data == null) return; // nothing to do
		
		List<VFSThumbnailMetadata> thumbnails = thumbnailDao.loadByMetadata(data);
		for(VFSThumbnailMetadata thumbnail:thumbnails) {
			VFSItem item = VFSManager.olatRootLeaf("/" + data.getRelativePath(), thumbnail.getFilename());
			if(item != null && item.exists()) {
				item.deleteSilently();
			}
			thumbnailDao.removeThumbnail(thumbnail);
		}
		
		List<VFSRevision> revisions = getRevisions(data);
		for(VFSRevision revision:revisions) {
			File revFile = getRevisionFile(revision);
			if(revFile != null && revFile.exists()) {
				try {
					Files.delete(revFile.toPath());
				} catch (IOException e) {
					log.error("Cannot delete thumbnail: " + revFile, e);
				}
			}
			revisionDao.deleteRevision(revision);
		}
		
		List<VFSMetadata> children = getChildren(data);
		for(VFSMetadata child:children) {
			deleteMetadata(child);
		}
		metadataDao.removeMetadata(data);
	}

	@Override
	public void deleteMetadata(File file) {
		VFSMetadata metadata = loadMetadata(file);
		if(metadata != null) {
			deleteMetadata(metadata);
		}
	}

	@Override
	public void markAsDeleted(VFSItem item, Identity author) {
		if(item.canMeta() != VFSConstants.YES) return;
		
		VFSMetadataImpl metadata = (VFSMetadataImpl)getMetadataFor(item);
		if(metadata != null) { // concurrent delete possible
			metadata.setDeleted(true);
			if(item instanceof VFSLeaf) {
				VFSLeaf file = (VFSLeaf)item;
				if(isThumbnailAvailable(file, metadata)) {
					resetThumbnails(file);
				}
				if(file.canVersion() == VFSConstants.YES) {
					addToRevisions(file, metadata, author, "", true);
				}
			}
			metadataDao.updateMetadata(metadata);
		}
	}

	@Override
	public void copyBinaries(VFSMetadata metadata, byte[] binaries) {
		if(binaries == null || binaries.length == 0) return;
		
		MetaInfoReader reader = new MetaInfoReader((VFSMetadataImpl)metadata, licenseService, securityManager);
		reader.fromBinaries(binaries);
	}

	@Override
	public void copyTo(VFSLeaf source, VFSLeaf target, VFSContainer parentTarget) {
		if(source.canMeta() != VFSConstants.YES || target.canMeta() != VFSConstants.YES) return;
		
		VFSMetadataImpl sourceMetadata = (VFSMetadataImpl)loadMetadata(toFile(source));
		if(sourceMetadata != null) {
			File targetFile = toFile(target);
			if(targetFile != null) {
				VFSMetadata targetMetadata = loadMetadata(targetFile);
				if(targetMetadata == null) {
					VFSMetadata parentMetadata = getMetadataFor(parentTarget);
					String relativePath = getRelativePath(targetFile.getParentFile());
					targetMetadata = metadataDao.createMetadata(UUID.randomUUID().toString(), relativePath, targetFile.getName(),
							new Date(), targetFile.length(), false, targetFile.toURI().toString(), "file", parentMetadata);
				}
				targetMetadata.copyValues(sourceMetadata);
				if(source.canVersion() == VFSConstants.YES || target.canVersion() == VFSConstants.YES) {
					targetMetadata.setRevisionComment(sourceMetadata.getRevisionComment());
					targetMetadata.setRevisionNr(sourceMetadata.getRevisionNr());
					copy(sourceMetadata, targetMetadata);
				}
				metadataDao.updateMetadata(targetMetadata);
			}
		}
	}
	
	private boolean copy(VFSMetadata sourceMetadata, VFSMetadata targetMetadata) {
		List<VFSRevision> sourceRevisions = getRevisions(sourceMetadata);

		boolean allOk = true;
		for (VFSRevision sourceRevision : sourceRevisions) {
			VFSLeaf sourceRevFile = getRevisionLeaf(sourceMetadata, (VFSRevisionImpl)sourceRevision);
			if(sourceRevFile != null && sourceRevFile.exists()) {
				VFSRevision targetRevision = revisionDao.createRevisionCopy(sourceRevision.getAuthor(), sourceRevision.getRevisionComment(),
						sourceRevision, targetMetadata);
				VFSLeaf targetRevFile = getRevisionLeaf(targetMetadata, (VFSRevisionImpl)targetRevision);
				VFSManager.copyContent(sourceRevFile, targetRevFile, false);
			}
		}
		return allOk;
	}

	@Override
	public VFSMetadata rename(VFSItem item, String newName) {
		VFSMetadata metadata = getMetadataFor(item);

		((VFSMetadataImpl)metadata).setFilename(newName);
		Path newFile = Paths.get(folderModule.getCanonicalRoot(), metadata.getRelativePath(), newName);
		String uri = newFile.toFile().toURI().toString();
		((VFSMetadataImpl)metadata).setUri(uri);
			
		if(item instanceof VFSContainer) {
			//TODO rename container ???
		}
		
		List<VFSRevision> revisions = getRevisions(metadata);
		for(VFSRevision revision:revisions) {
			VFSLeaf revFile = getRevisionLeaf(metadata, (VFSRevisionImpl)revision);
			if(revFile != null && revFile.exists()) {
				String newRevFilename = generateFilenameForRevision(newName, revision.getRevisionNr());
				revFile.rename(newRevFilename);
				((VFSRevisionImpl)revision).setFilename(newRevFilename);
				revisionDao.updateRevision(revision);
			}
		}
		return metadataDao.updateMetadata(metadata);
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
			if(thumbnail == null) {
				thumbnailLeaf = generateThumbnail(file, metadata, fill, maxWidth, maxHeight);
			} else {
				VFSItem item = parentContainer.resolve(thumbnail.getFilename());
				if(item instanceof VFSLeaf) {
					thumbnailLeaf = (VFSLeaf)item;
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
				thumbnailLeaf = generateThumbnail(file, metadata, fill, maxWidth, maxHeight);
			} else {
				VFSItem item = parentContainer.resolve(thumbnail.getFilename());
				if(item instanceof VFSLeaf) {
					thumbnailLeaf = (VFSLeaf)item;
				} else if(item == null) {
					thumbnailDao.removeThumbnail(thumbnail);
				}
			}
		}
		return thumbnailLeaf;
	}
	
	private VFSLeaf generateThumbnail(VFSLeaf file, VFSMetadata metadata, boolean fill, int maxWidth, int maxHeight) {
		String name = file.getName();
		String thumbnailName = generateFilenameForThumbnail(name, fill, maxWidth, maxHeight);
		
		VFSContainer parentContainer = getSecureParentContainer(file);
		VFSLeaf thumbnailLeaf = parentContainer.createChildLeaf(thumbnailName);
		if(thumbnailLeaf == null) {
			// ooops, a thumbnail without a database entry
			VFSItem thumbnailItem = parentContainer.resolve(thumbnailName);
			if(thumbnailItem instanceof VFSLeaf) {
				thumbnailLeaf = (VFSLeaf)thumbnailItem;
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
				FinalSize finalSize = thumbnailService.generateThumbnail(file, thumbnailLeaf, maxWidth, maxHeight, fill);
				if(finalSize == null) {
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
	public boolean isThumbnailAvailable(VFSItem item, VFSMetadata metadata) {
		if(metadata == null) return false;
		
		if(metadata.isDirectory() || (metadata.getCannotGenerateThumbnails() != null && metadata.getCannotGenerateThumbnails().booleanValue())) { 
			return false;
		}
		return thumbnailService.isThumbnailPossible((VFSLeaf)item);
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
	public long getRevisionsTotalSizeOfDeletedFiles() {
		return revisionDao.getRevisionsSizeOfDeletedFiles();
	}
	
	@Override
	public List<VFSRevision> getRevisionsOfDeletedFiles() {
		return revisionDao.getRevisionsOfDeletedFiles();
	}

	@Override
	public List<VFSMetadataRef> getMetadataOfDeletedFiles() {
		return revisionDao.getMetadataOfDeletedFiles();
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
			if (addToRevisions(currentLeaf, metadata, identity, comment, false)) {
				// copy the content of the new file to the old
				VFSLeaf revFile = getRevisionLeaf(metadata, ((VFSRevisionImpl)revision));
				if (VFSManager.copyContent(revFile.getInputStream(), currentLeaf)) {
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
	public boolean addVersion(VFSLeaf currentFile, Identity identity, String comment, InputStream newFile) {
		boolean allOk = false;
		VFSMetadata metadata = getMetadataFor(currentFile);
		if (addToRevisions(currentFile, metadata, identity, comment, true)) {
			// copy the content of the new file to the old
			if(newFile instanceof net.sf.jazzlib.ZipInputStream || newFile instanceof java.util.zip.ZipInputStream) {
				newFile = new ShieldInputStream(newFile);
			}
			allOk = VFSManager.copyContent(newFile, currentFile);
		} else {
			log.error("Cannot create a version of this file: " + currentFile);
		}
		dbInstance.commit();
		return allOk;
	}
	
	public boolean addToRevisions(VFSLeaf currentLeaf, VFSMetadata metadata, Identity identity, String comment, boolean pruneRevision) {
		int maxNumOfVersions = versionModule.getMaxNumberOfVersions();
		if(maxNumOfVersions == 0) {
			return true;//deactivated, return all ok
		}
		File currentFile = toFile(currentLeaf);
		if(currentFile == null) {
			return false;
		}

		// read from the
		List<VFSRevision> revisions = revisionDao.getRevisions(metadata);
		VFSRevisionImpl lastRevision = (VFSRevisionImpl)getLastRevision(revisions);
		int versionNr = getNextRevisionNr(revisions);
		
		boolean sameFile = isSameFile(currentLeaf, metadata, revisions);
		String uuid = sameFile && lastRevision != null ? lastRevision.getFilename()
				: generateFilenameForRevision(currentFile, versionNr);

		Date lastModifiedDate = metadata.getFileLastModified();
		if(lastModifiedDate == null) {
			lastModifiedDate = new Date(currentFile.lastModified());
		}
		long fileSize = metadata.getFileSize();
		if(fileSize <= 0l) {
			fileSize = currentFile.length();
		}

		VFSRevision newRevision = revisionDao.createRevision(metadata.getAuthor(), uuid, versionNr,
				fileSize, lastModifiedDate, metadata.getRevisionComment(), metadata);
		revisions.add(newRevision);

		if(!sameFile) {
			resetThumbnails(currentLeaf);
		}

		File revFile = new File(currentFile.getParentFile(), uuid);
		if (sameFile || copyContent(currentFile, revFile)) {
			if(pruneRevision && maxNumOfVersions >= 0 && revisions.size() > maxNumOfVersions) {
				int numOfVersionsToDelete = Math.min(revisions.size(), (revisions.size() - maxNumOfVersions));
				if(numOfVersionsToDelete > 0) {
					List<VFSRevision> versionsToDelete = new ArrayList<>(revisions.subList(0, numOfVersionsToDelete));
					deleteRevisions(metadata, revisions, versionsToDelete);
				}
			}
			metadata.setRevisionComment(comment);
			metadata.setRevisionNr(getNextRevisionNr(revisions));
			metadata.setAuthor(identity);
			updateMetadata(metadata);
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
		VFSRevision last = null;
		for (VFSRevision version : revisions) {
			if (last == null || version.getRevisionNr() > last.getRevisionNr()) {
				last = version;
			}
		}
		return last;
	}
	
	private int getNextRevisionNr(List<VFSRevision> revisions) {
		int maxNumber = 0;
		for (VFSRevision version : revisions) {
			int versionNr = version.getRevisionNr();
			if (versionNr > 0 && versionNr > maxNumber) {
				maxNumber = versionNr;
			}
		}
		return maxNumber + 1;
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
		((VFSMetadataImpl)metadata).setAuthor(author);

		List<VFSRevision> revisions = getRevisions(metadata);
		for(VFSRevision revision:revisions) {
			VFSRevisionImpl revImpl = (VFSRevisionImpl)revision;
			Path path = getRevisionPath(currentRelativePath, revImpl.getFilename());
			File revFile = path.toFile();
			if(revFile.exists()) {
				String newRevFilename = generateFilenameForRevision(newTargetName, revision.getRevisionNr());
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
		License license = null;
		boolean hasLicense = meta != null && StringHelper.containsNonWhitespace(meta.getLicenseTypeName());
		if (hasLicense) { 
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
			license = licenseService.createLicense(licenseType);
			license.setLicensor(meta.getLicensor());
			if (licenseService.isFreetext(licenseType)) {
				license.setFreetext(meta.getLicenseText());
			}
		}
		return license;
	}
	
	/**
	 * Get the license of the MetaInfo or create a new default license:
	 *
	 * @param meta
	 * @param itentity the current user
	 * @return
	 */
	@Override
	public License getOrCreateLicense(VFSMetadata meta, Identity itentity) {
		License license = getLicense(meta);
		if (license == null) {
			license = licenseService.createDefaultLicense(licenseHandler, itentity);
		}
		return license;
	}
	
	@Override
	public void migrate(VFSContainer container, VFSMetadata metadata) {
		if(vfsModule.isMigrated()) return;
		
		File directory = toFile(container);
		if(VFSRepositoryModule.canMeta(directory) == VFSConstants.YES) {
			try {
				migrateDirectories(directory);
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
		if(!persistedParent.equals(parent)) {
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

	public void migrateDirectories(File folder) throws IOException {
		Deque<VFSMetadata> parentLine = new LinkedList<>();
		AtomicInteger migrationCounter = new AtomicInteger(0);
		
		Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				File directory = dir.toFile();
				if(directory.isHidden() || VFSRepositoryModule.canMeta(directory) != VFSConstants.YES) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				if(dir.getNameCount() > 50 || parentLine.size() > 50) {
					log.error("More than 50 directories deep. Stop migrating metadata: {}", directory);
					return FileVisitResult.SKIP_SUBTREE;
				}
				
				VFSMetadata parent = parentLine.peekLast();
				VFSMetadata metadata = migrateMetadata(dir.toFile(), parent);
				metadata = checkParentLine(metadata, parent);
				parentLine.add(metadata);
				dbInstance.commit();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				if(!f.isHidden() && VFSRepositoryModule.canMeta(f) == VFSConstants.YES) {
					VFSMetadata metadata = migrateMetadata(file.toFile(), parentLine.getLast());
					checkParentLine(metadata, parentLine.getLast());
					
					migrationCounter.incrementAndGet();
					if(migrationCounter.get() % 25 == 0) {
						dbInstance.commitAndCloseSession();
					} else {
						dbInstance.commit();
					}
					if(migrationCounter.get() % 100 == 0) {
						log.info("Metadata: num. of files migrated: " + migrationCounter);
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				VFSMetadata metadata = parentLine.pollLast();
				if(metadata instanceof VFSMetadataImpl) {
					((VFSMetadataImpl)metadata).setMigrated("migrated");
					metadataDao.updateMetadata(metadata);
				}
				dbInstance.commitAndCloseSession();
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	public VFSMetadata migrate(File file, VFSMetadata parent) {
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
				parent = migrate(file.getParentFile(), null);
			}
			metadata = migrateMetadata(file, parent);
		}
		return metadata;
	}
	
	private VFSMetadata migrateMetadata(File file, VFSMetadata parent) {
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
		if(metaPath != null) {
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
	private String generateFilenameForRevision(File file, int revisionNr) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("._oo_vr_").append(revisionNr).append("_").append(file.getName());
		return sb.toString();
	}
	
	/**
	 * @param filename The original filename
	 * @param revisionNr The version number
	 * @return A name like ._oo_vr_filename.ext
	 */
	private String generateFilenameForRevision(String filename, int revisionNr) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("._oo_vr_").append(revisionNr).append("_").append(filename);
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
					String newRevisionFilename = generateFilenameForRevision(file, revision.getRevisionNr());
					revisionDao.createRevision(revision.getAuthor(), newRevisionFilename, revision.getRevisionNr(),
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

}
