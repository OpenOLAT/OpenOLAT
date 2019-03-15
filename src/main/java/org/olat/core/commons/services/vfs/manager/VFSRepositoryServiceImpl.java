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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.thumbnail.CannotGenerateThumbnailException;
import org.olat.core.commons.services.thumbnail.FinalSize;
import org.olat.core.commons.services.thumbnail.ThumbnailService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.commons.services.vfs.manager.MetaInfoReader.Thumbnail;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class VFSRepositoryServiceImpl implements VFSRepositoryService {
	
	private static final OLog log = Tracing.createLoggerFor(VFSRepositoryServiceImpl.class);
	private static final String CANONICAL_ROOT_REL_PATH = "/";
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private VFSThumbnailDAO thumbnailDao;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private FolderLicenseHandler licenseHandler;
	@Autowired
	private ThumbnailService thumbnailService;
	@Autowired
	private BaseSecurity securityManager;

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
		}
		return metadata;
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
		return metadataDao.getMetadata(relativePath, filename, file.isDirectory());
	}
	
	private File toFile(VFSItem item) {
		String relPath = item.getRelPath();
		return relPath == null ? null : VFSManager.olatRootFile(relPath);
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

	/**
	 * The relative path contains /bcroot/
	 * 
	 * @param file
	 * @return
	 */
	private String getRelativePath(File file) {
		return folderModule.getCanonicalRootPath().relativize(file.toPath()).toString();
	}
	
	private String getRelativePath(VFSItem item) {
		String relativePath = item.getRelPath();
		if(!relativePath.equals(CANONICAL_ROOT_REL_PATH) && relativePath.startsWith("/")) {
			relativePath = relativePath.substring(1, relativePath.length());
		}
		return relativePath;
	}
	
	@Override
	public VFSMetadata updateMetadata(VFSMetadata data) {
		return metadataDao.updateMetadata(data);
	}

	@Override
	public void deleteMetadata(VFSMetadata data) {
		List<VFSThumbnailMetadata> thumbnails = thumbnailDao.loadByMetadata(data);
		for(VFSThumbnailMetadata thumbnail:thumbnails) {
			VFSItem item = VFSManager.olatRootLeaf("/" + data.getRelativePath(), thumbnail.getFilename());
			if(item != null && item.exists()) {
				item.deleteSilently();
			}
			thumbnailDao.removeThumbnail(thumbnail);
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
					String relativePath = this.getRelativePath(targetFile.getParentFile());
					targetMetadata = metadataDao.createMetadata(UUID.randomUUID().toString(), relativePath, targetFile.getName(),
							new Date(), targetFile.length(), false, targetFile.toURI().toString(), "file", parentMetadata);
				}
				targetMetadata.copyValues(sourceMetadata);
				metadataDao.updateMetadata(targetMetadata);
			}
		}
	}

	@Override
	public VFSMetadata rename(VFSMetadata data, String newName) {
		((VFSMetadataImpl)data).setFilename(newName);
		Path newFile = Paths.get(folderModule.getCanonicalRoot(), data.getRelativePath(), newName);
		String uri = newFile.toFile().toURI().toString();
		((VFSMetadataImpl)data).setUri(uri);
		return metadataDao.updateMetadata(data);
	}

	@Override
	public void increaseDownloadCount(VFSItem item) {
		String relPath = item.getRelPath();
		if(StringHelper.containsNonWhitespace(relPath)) {
			metadataDao.increaseDownloadCount(relPath, item.getName());
		}
	}
	
	@Override
	public VFSLeaf getThumbnail(VFSLeaf file, VFSMetadata metadata, int maxWidth, int maxHeight, boolean fill) {
		VFSLeaf thumbnailLeaf = null;
		
		VFSContainer parentContainer = file.getParentContainer();
		String relativePath = getRelativePath(parentContainer);
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
		
		VFSContainer parentContainer = file.getParentContainer();
		String relativePath = getRelativePath(parentContainer);
		if(relativePath != null) {
			VFSThumbnailMetadata thumbnail = thumbnailDao.findThumbnail(relativePath, file.getName(), fill, maxWidth, maxHeight);
			if(thumbnail == null) {
				VFSMetadata metadata = metadataDao.getMetadata(relativePath, file.getName(), false);
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
	
	private VFSLeaf generateThumbnail(VFSLeaf file, VFSMetadata metadata, boolean fill, int maxWidth, int maxHeight) {
		String name = file.getName();
		String extension = FileUtils.getFileSuffix(name);
		String nameOnly = name.substring(0, name.length() - extension.length() - 1);
		String thumbnailExtension = preferedThumbnailType(extension);
		String thumbnailName = generateFilenameForThumbnail(nameOnly, thumbnailExtension, fill, maxWidth, maxHeight);
		
		VFSContainer parentContainer = file.getParentContainer();
		VFSLeaf thumbnailLeaf = parentContainer.createChildLeaf(thumbnailName);
		if(thumbnailService.isThumbnailPossible(thumbnailLeaf)) {
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
		VFSContainer parentContainer = file.getParentContainer();
		String relativePath = getRelativePath(parentContainer);
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
	
	public void migrateDirectories(File folder) throws IOException {
		Deque<VFSMetadata> parentLine = new LinkedList<>();
		AtomicInteger migrationCounter = new AtomicInteger(0);
		
		Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				File directory = dir.toFile();
				if(directory.isHidden() || directory.getName().equals("__MACOSX")) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				VFSMetadata parent = parentLine.peekLast();
				VFSMetadata metadata = migrateMetadata(dir.toFile(), parent);
				parentLine.add(metadata);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				if(!f.isHidden() && !f.getName().equals("__MACOSX")) {
					migrateMetadata(file.toFile(), parentLine.getLast());
					
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
				parentLine.pollLast();
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
				metadata = metadataDao.createMetadata(xmlMetadata, relativePath, file.getName(), fileLastModified,
						size, directory, file.toURI().toString(), "file", parent);
				migrateThumbnails(metadata, file, thumbnails);
			}
		} 
		
		if(metadata == null) {
			metadata = metadataDao.createMetadata(UUID.randomUUID().toString(), relativePath, file.getName(), fileLastModified,
					size, directory, file.toURI().toString(), "file", parent);
		}
		return metadata;
	}
	
	private String generateFilenameForThumbnail(File file, boolean fill, int maxWidth, int maxHeight) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("._oo_th_").append(fill).append("_").append(maxWidth).append("_").append(maxHeight)
		  .append("_").append(file.getName());
		return sb.toString();
	}
	
	private String generateFilenameForThumbnail(String name, String extension, boolean fill, int maxWidth, int maxHeight) {
		StringBuilder sb = new StringBuilder(128);
		sb.append("._oo_th_").append(fill).append("_").append(maxWidth).append("_").append(maxHeight)
		  .append("_").append(name).append(".").append(extension);
		return sb.toString();
	}
	
	private void migrateThumbnails(VFSMetadata metadata, File file, List<Thumbnail> thumbnails) {
		if(thumbnails == null || thumbnails.isEmpty()) return;
		
		// do copy them
		for(Thumbnail thumbnail:thumbnails) {
			try {
				File thumbnailFile = thumbnail.getThumbnailFile();
				if(thumbnailFile.exists()) {
					String filename = generateFilenameForThumbnail(file, thumbnail.isFill(), thumbnail.getMaxWidth(), thumbnail.getMaxHeight());
					thumbnailDao.createThumbnailMetadata(metadata, filename, thumbnailFile.length(),
							thumbnail.isFill(), thumbnail.getMaxWidth(), thumbnail.getMaxHeight(),
							thumbnail.getFinalWidth(), thumbnail.getFinalHeight());
					File target = new File(file.getParentFile(), filename);
					Files.move(thumbnailFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
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
}
